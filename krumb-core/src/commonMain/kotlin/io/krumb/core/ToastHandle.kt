package io.krumb.core

/**
 * A handle to a toast that has been added to the controller.
 *
 * Callers retain handles to dismiss or update a specific toast without
 * needing to know whether it is currently visible or queued.
 */
public interface ToastHandle {
    public val id: String

    /** Removes the toast immediately and cancels any pending auto-dismiss timer. */
    public fun dismiss()

    /**
     * Mutates the toast's [message] and/or [type] in place.
     *
     * If the toast has already been dismissed, this is a no-op.
     */
    public fun update(message: String? = null, type: ToastType? = null)
}
