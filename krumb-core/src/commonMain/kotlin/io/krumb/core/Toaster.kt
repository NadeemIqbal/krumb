package io.krumb.core

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

/**
 * Global toast facade — the one-liner API.
 *
 * Backed by an internally-replaceable [ToastController]. Tests swap the
 * controller via [setControllerForTesting]; library users typically use the
 * defaults and let `ToasterHost` from `krumb-compose` pick everything up.
 *
 * Example:
 * ```
 * Toaster.success("Profile saved")
 * Toaster.error("Network failed")
 * Toaster.show("Deleted item") {
 *     action("Undo") { viewModel.undo() }
 *     duration = 5.seconds
 * }
 * ```
 */
public object Toaster {

    /**
     * The controller currently backing the global facade. Readable everywhere
     * (e.g. `ToasterHost` defaults to it); replace it only via
     * [setControllerForTesting].
     */
    public var controller: ToastController = ToastController()
        internal set

    public val visible: StateFlow<List<Toast>> get() = controller.visible

    public fun success(
        message: String,
        duration: Duration? = null,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = controller.success(
        message = message,
        duration = duration ?: io.krumb.core.internal.DefaultDurations.SUCCESS,
        priority = priority,
        position = position,
        action = action,
    )

    public fun error(
        message: String,
        duration: Duration? = null,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = controller.error(
        message = message,
        duration = duration ?: io.krumb.core.internal.DefaultDurations.ERROR,
        priority = priority,
        position = position,
        action = action,
    )

    public fun info(
        message: String,
        duration: Duration? = null,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = controller.info(
        message = message,
        duration = duration ?: io.krumb.core.internal.DefaultDurations.INFO,
        priority = priority,
        position = position,
        action = action,
    )

    public fun warning(
        message: String,
        duration: Duration? = null,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = controller.warning(
        message = message,
        duration = duration ?: io.krumb.core.internal.DefaultDurations.WARNING,
        priority = priority,
        position = position,
        action = action,
    )

    public fun loading(
        message: String,
        duration: Duration? = null,
        priority: Priority = Priority.NORMAL,
        position: ToastPosition = ToastPosition.TopCenter,
        action: ToastAction? = null,
    ): ToastHandle = controller.loading(
        message = message,
        duration = duration ?: io.krumb.core.internal.DefaultDurations.LOADING,
        priority = priority,
        position = position,
        action = action,
    )

    public fun show(
        message: String,
        configure: ToastBuilder.() -> Unit = {},
    ): ToastHandle = controller.show(message, configure)

    @ExperimentalKrumbApi
    public suspend fun <T> promise(
        block: suspend () -> T,
        loading: String,
        success: (T) -> String,
        error: (Throwable) -> String,
        position: ToastPosition = ToastPosition.TopCenter,
    ): T = controller.promise(block, loading, success, error, position)

    public fun dismiss(id: String): Unit = controller.dismiss(id)
    public fun dismissAll(): Unit = controller.dismissAll()

    /**
     * Replace the global controller. Intended for tests that want a
     * deterministic [kotlinx.coroutines.test.TestScope]-driven controller.
     * The previous controller is **not** disposed — callers manage lifetime.
     */
    public fun setControllerForTesting(controller: ToastController) {
        this.controller = controller
    }
}
