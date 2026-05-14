package io.krumb.core

import io.krumb.core.internal.DefaultDurations
import io.krumb.core.internal.IdGen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * The main entry point to Krumb.
 *
 * Construct your own instance if you want a controller scoped to a test or a
 * specific feature, or use the global [Toaster] facade for the common case.
 *
 * All show methods ([success], [error], [info], [warning], [loading], [show])
 * are non-suspending — they launch work onto the controller's internal scope
 * and return a [ToastHandle] synchronously, matching sonner's "callable from
 * anywhere" DX.
 *
 * @param maxVisible upper bound on concurrently-rendered toasts.
 * @param scope coroutine scope used for queue mutations and dismissal timers.
 *   Defaults to `Dispatchers.Default + SupervisorJob()`.
 */
public class ToastController(
    maxVisible: Int = DEFAULT_MAX_VISIBLE,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    public companion object {
        public const val DEFAULT_MAX_VISIBLE: Int = 3
    }

    private val queue = ToastQueue(maxVisible)
    public val visible: StateFlow<List<Toast>> get() = queue.visible

    private val timersMutex = Mutex()
    // id -> dismissal job (null while paused)
    private val timers = HashMap<String, Job?>()
    // id -> total duration (for resuming with remainder)
    private val durations = HashMap<String, Duration>()
    // id -> mark when current timer was started
    private val timerStartedAt = HashMap<String, TimeSource.Monotonic.ValueTimeMark>()
    // id -> remaining duration captured at pause()
    private val remainingAtPause = HashMap<String, Duration>()

    public fun success(
        message: String,
        duration: Duration = DefaultDurations.SUCCESS,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = enqueue(message, ToastType.Success, duration, priority, position, action, null)

    public fun error(
        message: String,
        duration: Duration = DefaultDurations.ERROR,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = enqueue(message, ToastType.Error, duration, priority, position, action, null)

    public fun info(
        message: String,
        duration: Duration = DefaultDurations.INFO,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = enqueue(message, ToastType.Info, duration, priority, position, action, null)

    public fun warning(
        message: String,
        duration: Duration = DefaultDurations.WARNING,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = enqueue(message, ToastType.Warning, duration, priority, position, action, null)

    public fun loading(
        message: String,
        duration: Duration = DefaultDurations.LOADING,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = enqueue(message, ToastType.Loading, duration, priority, position, action, null)

    /**
     * Configurable show variant. Pick a [type] and tune via the builder.
     */
    public fun show(
        message: String,
        configure: ToastBuilder.() -> Unit = {},
    ): ToastHandle {
        val builder = ToastBuilder(message).apply(configure)
        return enqueue(
            builder.message,
            builder.type,
            builder.duration,
            builder.priority,
            builder.position,
            builder.action,
            builder.customContentKey,
        )
    }

    /**
     * Internal entry point for `krumb-compose`'s custom-composable extension.
     * Most callers should use [show] or [success]/[error]/etc.
     */
    public fun showCustom(
        customContentKey: String,
        duration: Duration = DefaultDurations.CUSTOM,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
    ): ToastHandle = enqueue(
        message = "",
        type = ToastType.Custom,
        duration = duration,
        priority = priority,
        position = position,
        action = null,
        customContentKey = customContentKey,
    )

    /**
     * Run [block], showing a loading toast while it executes and converting
     * it to a success/error toast based on the outcome.
     *
     * Cancellation: if the calling coroutine is cancelled, the loading toast
     * is dismissed and the [CancellationException] is rethrown.
     */
    @ExperimentalKrumbApi
    public suspend fun <T> promise(
        block: suspend () -> T,
        loading: String,
        success: (T) -> String,
        error: (Throwable) -> String,
        position: ToastPosition = ToastPosition.TopCenter,
    ): T {
        val handle = this.loading(message = loading, position = position)
        return try {
            val result = block()
            handle.update(message = success(result), type = ToastType.Success)
            rearmTimer(handle.id, DefaultDurations.SUCCESS)
            result
        } catch (ce: CancellationException) {
            handle.dismiss()
            throw ce
        } catch (t: Throwable) {
            handle.update(message = error(t), type = ToastType.Error)
            rearmTimer(handle.id, DefaultDurations.ERROR)
            throw t
        }
    }

    public fun dismiss(id: String) {
        scope.launch {
            cancelTimer(id)
            queue.remove(id)?.also {
                onToastRemoved(it.id)
            }
            // If a queued toast was promoted to visible, start its timer.
            val current = queue.visible.value
            current.forEach { v ->
                if (!timers.containsKey(v.id) && v.duration != Duration.INFINITE) {
                    armTimer(v.id, v.duration)
                }
            }
        }
    }

    public fun dismissAll() {
        scope.launch {
            timersMutex.withLock {
                timers.values.forEach { it?.cancel() }
                timers.clear()
                durations.clear()
                timerStartedAt.clear()
                remainingAtPause.clear()
            }
            queue.removeAll()
        }
    }

    /**
     * Snapshot of pending (overflow) toasts in priority order. Exposed for
     * tests and debugging — UI hosts should consume [visible] only.
     */
    public suspend fun pendingSnapshot(): List<Toast> = queue.pendingSnapshot()

    // --- pause / resume hooks for krumb-compose pause-on-hover ---

    /** Pauses the auto-dismiss timer for the toast with [id], if any. */
    public fun pause(id: String) {
        scope.launch {
            timersMutex.withLock {
                val job = timers[id] ?: return@withLock
                val total = durations[id] ?: return@withLock
                val startedAt = timerStartedAt[id] ?: return@withLock
                val elapsed = startedAt.elapsedNow()
                val remaining = (total - elapsed).coerceAtLeast(Duration.ZERO)
                job.cancel()
                timers[id] = null
                remainingAtPause[id] = remaining
            }
        }
    }

    /** Resumes a previously-paused timer for [id]. */
    public fun resume(id: String) {
        scope.launch {
            timersMutex.withLock {
                if (timers[id] != null) return@withLock // already running
                val remaining = remainingAtPause.remove(id) ?: return@withLock
                if (remaining <= Duration.ZERO) {
                    // expired while paused — dismiss now
                    scope.launch { dismiss(id) }
                    return@withLock
                }
                durations[id] = remaining
                val mark = TimeSource.Monotonic.markNow()
                timerStartedAt[id] = mark
                val job = scope.launch {
                    delay(remaining)
                    dismiss(id)
                }
                timers[id] = job
            }
        }
    }

    // --- internals ---

    private fun enqueue(
        message: String,
        type: ToastType,
        duration: Duration,
        priority: Priority,
        position: ToastPosition,
        action: ToastAction?,
        customContentKey: String?,
    ): ToastHandle {
        val id = IdGen.next()
        val toast = Toast(
            id = id,
            message = message,
            type = type,
            duration = duration,
            priority = priority,
            action = action,
            position = position,
            customContentKey = customContentKey,
        )
        val handle = HandleImpl(id)
        scope.launch {
            val result = queue.add(toast)
            when (result) {
                is ToastQueue.AddResult.Visible -> {
                    result.preempted?.let { onToastRemoved(it.id) }
                    if (duration != Duration.INFINITE) armTimer(id, duration)
                }
                is ToastQueue.AddResult.Pending -> {
                    // timer is armed only when toast becomes visible
                }
            }
        }
        return handle
    }

    private suspend fun armTimer(id: String, duration: Duration) {
        timersMutex.withLock {
            durations[id] = duration
            val mark = TimeSource.Monotonic.markNow()
            timerStartedAt[id] = mark
            val job = scope.launch {
                delay(duration)
                dismiss(id)
            }
            timers[id] = job
        }
    }

    private suspend fun rearmTimer(id: String, duration: Duration) {
        cancelTimer(id)
        armTimer(id, duration)
    }

    private suspend fun cancelTimer(id: String) {
        timersMutex.withLock {
            timers.remove(id)?.cancel()
            durations.remove(id)
            timerStartedAt.remove(id)
            remainingAtPause.remove(id)
        }
    }

    private suspend fun onToastRemoved(id: String) {
        cancelTimer(id)
    }

    private inner class HandleImpl(override val id: String) : ToastHandle {
        override fun dismiss() = this@ToastController.dismiss(id)
        override fun update(message: String?, type: ToastType?) {
            if (message == null && type == null) return
            scope.launch {
                queue.update(id) { existing ->
                    existing.copy(
                        message = message ?: existing.message,
                        type = type ?: existing.type,
                    )
                }
            }
        }
    }
}

/**
 * DSL builder used by [ToastController.show].
 */
public class ToastBuilder internal constructor(
    public var message: String,
) {
    public var type: ToastType = ToastType.Info
    public var duration: Duration = DefaultDurations.INFO
    public var priority: Priority = Priority.NORMAL
    public var position: ToastPosition = ToastPosition.TopCenter
    public var action: ToastAction? = null
    internal var customContentKey: String? = null

    public fun action(label: String, onClick: () -> Unit) {
        action = ToastAction(label, onClick)
    }
}
