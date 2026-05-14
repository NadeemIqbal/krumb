@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.krumb.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ToastControllerTest {

    private fun controllerOn(scope: TestScope, maxVisible: Int = 3): ToastController {
        val dispatcher = StandardTestDispatcher(scope.testScheduler)
        return ToastController(maxVisible = maxVisible, scope = CoroutineScope(dispatcher))
    }

    @Test
    fun success_returns_handle_synchronously_and_appears_visible() = runTest {
        val c = controllerOn(this)
        val handle = c.success("hello", duration = Duration.INFINITE)
        assertTrue(handle.id.startsWith("t_"))
        assertTrue(c.visible.value.isEmpty())  // launched, not yet executed
        runCurrent()
        assertEquals(1, c.visible.value.size)
        assertEquals("hello", c.visible.value.first().message)
        assertEquals(ToastType.Success, c.visible.value.first().type)
    }

    @Test
    fun ids_are_unique() = runTest {
        val c = controllerOn(this)
        val ids = (0 until 100).map { c.success("t$it", duration = Duration.INFINITE).id }
        assertEquals(100, ids.toSet().size)
    }

    @Test
    fun dismiss_via_handle_removes_toast() = runTest {
        val c = controllerOn(this)
        val h = c.info("temp", duration = Duration.INFINITE)
        runCurrent()
        assertEquals(1, c.visible.value.size)
        h.dismiss()
        runCurrent()
        assertTrue(c.visible.value.isEmpty())
    }

    @Test
    fun update_via_handle_changes_message_and_type() = runTest {
        val c = controllerOn(this)
        val h = c.loading("Uploading…")
        runCurrent()
        h.update(message = "Done", type = ToastType.Success)
        runCurrent()
        val toast = c.visible.value.first()
        assertEquals("Done", toast.message)
        assertEquals(ToastType.Success, toast.type)
    }

    @Test
    fun finite_duration_auto_dismisses() = runTest {
        val c = controllerOn(this)
        c.success("bye", duration = 1.seconds)
        runCurrent()
        assertEquals(1, c.visible.value.size)
        advanceTimeBy(900.milliseconds)
        runCurrent()
        assertEquals(1, c.visible.value.size)
        advanceTimeBy(200.milliseconds)
        runCurrent()
        assertTrue(c.visible.value.isEmpty(), "toast should be dismissed after duration")
    }

    @Test
    fun infinite_duration_does_not_auto_dismiss() = runTest {
        val c = controllerOn(this)
        c.loading("persistent")
        runCurrent()
        advanceTimeBy(60.seconds)
        runCurrent()
        assertEquals(1, c.visible.value.size)
    }

    @Test
    fun dismiss_cancels_timer_before_firing() = runTest {
        val c = controllerOn(this)
        val h = c.info("x", duration = 5.seconds)
        runCurrent()
        advanceTimeBy(1.seconds)
        runCurrent()
        h.dismiss()
        runCurrent()
        assertTrue(c.visible.value.isEmpty())
        // advance well past original duration — no double dismiss / no exception
        advanceTimeBy(60.seconds)
        advanceUntilIdle()
        assertTrue(c.visible.value.isEmpty())
    }

    @Test
    fun overflow_promotes_when_visible_toast_expires() = runTest {
        val c = controllerOn(this, maxVisible = 1)
        c.show("first") { duration = 1.seconds }
        c.show("second") { duration = 5.seconds }
        runCurrent()
        assertEquals(listOf("first"), c.visible.value.map { it.message })
        assertEquals(listOf("second"), c.pendingSnapshot().map { it.message })
        advanceTimeBy(1100.milliseconds)
        runCurrent()
        assertEquals(listOf("second"), c.visible.value.map { it.message })
        assertTrue(c.pendingSnapshot().isEmpty())
    }

    @Test
    fun dismissAll_clears_everything_and_cancels_timers() = runTest {
        val c = controllerOn(this, maxVisible = 2)
        c.info("a", duration = 5.seconds)
        c.info("b", duration = 5.seconds)
        c.info("c", duration = 5.seconds)  // pending
        runCurrent()
        c.dismissAll()
        runCurrent()
        assertTrue(c.visible.value.isEmpty())
        assertTrue(c.pendingSnapshot().isEmpty())
        // no exceptions when timers fire later
        advanceTimeBy(60.seconds)
        advanceUntilIdle()
    }

    @Test
    fun pause_cancels_dismissal_timer() = runTest {
        val c = controllerOn(this)
        val h = c.info("hover me", duration = 2.seconds)
        runCurrent()
        c.pause(h.id)
        runCurrent()
        // Without pause the timer would dismiss after 2s. Advance 10s and confirm it's still there.
        advanceTimeBy(10.seconds)
        runCurrent()
        assertEquals(1, c.visible.value.size, "still visible while paused")
    }

    @Test
    fun resume_re_arms_dismissal_timer() = runTest {
        val c = controllerOn(this)
        val h = c.info("hover me", duration = 2.seconds)
        runCurrent()
        c.pause(h.id)
        runCurrent()
        advanceTimeBy(10.seconds)
        runCurrent()
        assertEquals(1, c.visible.value.size)
        c.resume(h.id)
        runCurrent()
        // resume re-arms with the remaining duration captured at pause time.
        // In real usage that's `duration - real_elapsed`; in virtual-time tests where the
        // wall-clock doesn't move, remaining ~= duration. Either way, eventually it dismisses.
        advanceTimeBy(5.seconds)
        runCurrent()
        assertTrue(c.visible.value.isEmpty(), "should expire after resume + advance past remaining")
    }

    @Test
    fun show_builder_applies_options() = runTest {
        val c = controllerOn(this)
        var clicked = false
        c.show("deleted") {
            type = ToastType.Warning
            duration = Duration.INFINITE
            priority = Priority.HIGH
            position = ToastPosition.BottomEnd
            action("Undo") { clicked = true }
        }
        runCurrent()
        val t = c.visible.value.first()
        assertEquals("deleted", t.message)
        assertEquals(ToastType.Warning, t.type)
        assertEquals(Priority.HIGH, t.priority)
        assertEquals(ToastPosition.BottomEnd, t.position)
        assertNotNull(t.action)
        t.action!!.onClick()
        assertTrue(clicked)
    }
}
