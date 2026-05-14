package io.krumb.core

import kotlin.time.Duration

/**
 * An opt-in marker for Krumb APIs that may change before v1.0.
 */
@RequiresOptIn(
    message = "This Krumb API is experimental and may change in a future release.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
public annotation class ExperimentalKrumbApi

/**
 * A single toast notification.
 *
 * Instances are immutable; use [ToastHandle.update] to change a live toast.
 */
public data class Toast(
    val id: String,
    val message: String,
    val type: ToastType,
    val duration: Duration,
    val priority: Priority,
    val action: ToastAction?,
    val position: ToastPosition,
    val customContentKey: String?,
)

/**
 * The semantic category of a toast, used by UI layers to pick icons/colors.
 *
 * `Custom` indicates the toast carries a [Toast.customContentKey] which the UI
 * resolves through its own registry to render an arbitrary composable body.
 */
public sealed class ToastType {
    public data object Success : ToastType()
    public data object Error : ToastType()
    public data object Info : ToastType()
    public data object Warning : ToastType()
    public data object Loading : ToastType()
    public data object Custom : ToastType()
}

/**
 * An interactive button rendered inside the toast. Clicking it does not
 * automatically dismiss the toast — the [onClick] handler may call
 * [ToastHandle.dismiss] explicitly if desired.
 */
public data class ToastAction(
    val label: String,
    val onClick: () -> Unit,
)

/** Where a toast is rendered relative to the [ToasterHost]. */
public enum class ToastPosition {
    TopStart, TopCenter, TopEnd,
    BottomStart, BottomCenter, BottomEnd,
}

/**
 * Queue priority. `HIGH` preempts the oldest visible `LOW` toast when the
 * queue is full (it never preempts `NORMAL`). Within the same priority,
 * insertion order is preserved (FIFO).
 */
public enum class Priority { LOW, NORMAL, HIGH }
