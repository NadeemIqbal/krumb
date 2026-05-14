package io.krumb.compose.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import io.krumb.core.ExperimentalKrumbApi
import io.krumb.core.Priority
import io.krumb.core.ToastController
import io.krumb.core.ToastHandle
import io.krumb.core.ToastPosition
import io.krumb.core.Toaster
import kotlin.random.Random
import kotlin.time.Duration

/**
 * Maps `Toast.customContentKey` → renderable composable. Built by
 * `ToasterHost` and provided down the tree via [LocalCustomContentRegistry].
 *
 * Library users never interact with this directly — they call
 * [io.krumb.core.Toaster.custom] (extension in this module) which registers
 * a key and shows a custom-type toast.
 */
public class CustomContentRegistry internal constructor() {
    internal val entries: SnapshotStateMap<String, @Composable () -> Unit> = mutableStateMapOf()

    internal fun register(key: String, content: @Composable () -> Unit) {
        entries[key] = content
    }

    internal fun unregister(key: String) {
        entries.remove(key)
    }

    public operator fun get(key: String): (@Composable () -> Unit)? = entries[key]
}

internal val LocalCustomContentRegistry = compositionLocalOf<CustomContentRegistry?> { null }

internal fun rememberCustomContentRegistry(): CustomContentRegistry = CustomContentRegistry()

private fun newKey(): String = "ck_${Random.nextLong().toULong().toString(16)}"

/**
 * Render arbitrary [content] inside a toast.
 *
 * The composable is held in a process-scoped registry attached to the active
 * [io.krumb.compose.ToasterHost]; calling [ToastHandle.dismiss] also clears
 * the registry entry.
 *
 * Requires a [io.krumb.compose.ToasterHost] to be present in the composition.
 */
@ExperimentalKrumbApi
public fun Toaster.custom(
    duration: Duration = Duration.INFINITE,
    priority: Priority = Priority.NORMAL,
    position: ToastPosition = ToastPosition.TopCenter,
    content: @Composable () -> Unit,
): ToastHandle {
    val key = newKey()
    GlobalCustomContent.register(key, content)
    val handle = controller.showCustom(
        customContentKey = key,
        duration = duration,
        priority = priority,
        position = position,
    )
    return CustomDismissingHandle(handle, key)
}

/**
 * Custom-content extension for a non-global [ToastController].
 */
@ExperimentalKrumbApi
public fun ToastController.custom(
    duration: Duration = Duration.INFINITE,
    priority: Priority = Priority.NORMAL,
    position: ToastPosition = ToastPosition.TopCenter,
    content: @Composable () -> Unit,
): ToastHandle {
    val key = newKey()
    GlobalCustomContent.register(key, content)
    val handle = this.showCustom(
        customContentKey = key,
        duration = duration,
        priority = priority,
        position = position,
    )
    return CustomDismissingHandle(handle, key)
}

/**
 * Process-wide registry used by [Toaster.custom] when no
 * `ToasterHost`-scoped registry is available. The active host syncs its
 * scoped registry from this global one on each composition.
 */
internal object GlobalCustomContent {
    private val map = mutableStateMapOf<String, @Composable () -> Unit>()

    fun register(key: String, content: @Composable () -> Unit) {
        map[key] = content
    }

    fun unregister(key: String) {
        map.remove(key)
    }

    fun get(key: String): (@Composable () -> Unit)? = map[key]

    fun snapshot(): Map<String, @Composable () -> Unit> = map.toMap()
}

private class CustomDismissingHandle(
    private val delegate: ToastHandle,
    private val key: String,
) : ToastHandle {
    override val id: String get() = delegate.id
    override fun dismiss() {
        delegate.dismiss()
        GlobalCustomContent.unregister(key)
    }
    override fun update(message: String?, type: io.krumb.core.ToastType?) {
        delegate.update(message, type)
    }
}
