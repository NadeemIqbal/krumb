package io.krumb.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Mutex-guarded toast queue. Internal — all public access goes through
 * [ToastController].
 *
 * State model:
 *  - `_visible` is the [StateFlow] of currently rendered toasts (cap [maxVisible]).
 *  - `pending` is an overflow buffer, ordered by [Priority] (HIGH first, then
 *    NORMAL, then LOW). FIFO within each priority tier.
 */
internal class ToastQueue(private val maxVisible: Int) {

    init {
        require(maxVisible >= 1) { "maxVisible must be >= 1, was $maxVisible" }
    }

    private val mutex = Mutex()
    private val _visible = MutableStateFlow<List<Toast>>(emptyList())
    val visible: StateFlow<List<Toast>> = _visible.asStateFlow()

    private val pending = ArrayDeque<Toast>()

    suspend fun add(toast: Toast): AddResult = mutex.withLock {
        val v = _visible.value
        if (v.size < maxVisible) {
            _visible.value = v + toast
            return@withLock AddResult.Visible(toast)
        }

        if (toast.priority == Priority.HIGH) {
            val oldestLowIndex = v.indexOfFirst { it.priority == Priority.LOW }
            if (oldestLowIndex >= 0) {
                val demoted = v[oldestLowIndex]
                _visible.value = v.toMutableList().apply {
                    set(oldestLowIndex, toast)
                }
                insertPending(demoted)
                return@withLock AddResult.Visible(toast, preempted = demoted)
            }
        }

        insertPending(toast)
        AddResult.Pending(toast)
    }

    suspend fun remove(id: String): Toast? = mutex.withLock {
        val v = _visible.value
        val idx = v.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val removed = v[idx]
            val remaining = v.toMutableList().apply { removeAt(idx) }
            val promoted = if (pending.isNotEmpty()) pending.removeFirst() else null
            _visible.value = if (promoted != null) remaining + promoted else remaining
            return@withLock removed
        }
        // not visible — might be pending
        val pIdx = pending.indexOfFirst { it.id == id }
        if (pIdx >= 0) {
            val removed = pending.removeAt(pIdx)
            return@withLock removed
        }
        null
    }

    suspend fun removeAll() = mutex.withLock {
        _visible.value = emptyList()
        pending.clear()
    }

    /**
     * Locates the toast (in visible or pending), replaces it with the result
     * of [transform], and returns the new [Toast] (or `null` if not found).
     */
    suspend fun update(id: String, transform: (Toast) -> Toast): Toast? = mutex.withLock {
        val v = _visible.value
        val idx = v.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val updated = transform(v[idx])
            _visible.value = v.toMutableList().apply { set(idx, updated) }
            return@withLock updated
        }
        val pIdx = pending.indexOfFirst { it.id == id }
        if (pIdx >= 0) {
            val updated = transform(pending[pIdx])
            pending[pIdx] = updated
            return@withLock updated
        }
        null
    }

    /**
     * For tests: returns a snapshot of pending toasts in priority order.
     */
    suspend fun pendingSnapshot(): List<Toast> = mutex.withLock { pending.toList() }

    private fun insertPending(toast: Toast) {
        // pending order: HIGH first, then NORMAL, then LOW. FIFO within tier.
        // Find first index where existing toast has lower priority.
        val insertIdx = pending.indexOfFirst { it.priority.ordinal < toast.priority.ordinal }
        if (insertIdx < 0) {
            pending.addLast(toast)
        } else {
            pending.add(insertIdx, toast)
        }
    }

    internal sealed class AddResult {
        data class Visible(val toast: Toast, val preempted: Toast? = null) : AddResult()
        data class Pending(val toast: Toast) : AddResult()
    }
}
