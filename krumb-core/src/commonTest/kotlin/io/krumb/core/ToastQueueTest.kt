package io.krumb.core

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

private fun toast(id: String, priority: Priority = Priority.NORMAL) = Toast(
    id = id,
    message = id,
    type = ToastType.Info,
    duration = 1.seconds,
    priority = priority,
    action = null,
    position = ToastPosition.TopCenter,
    customContentKey = null,
)

class ToastQueueTest {

    @Test
    fun visible_respects_maxVisible() = runTest {
        val q = ToastQueue(maxVisible = 3)
        repeat(5) { q.add(toast("t$it")) }
        val visible = q.visible.value
        assertEquals(3, visible.size)
        assertEquals(listOf("t0", "t1", "t2"), visible.map { it.id })
        assertEquals(listOf("t3", "t4"), q.pendingSnapshot().map { it.id })
    }

    @Test
    fun high_preempts_oldest_low_in_visible() = runTest {
        val q = ToastQueue(maxVisible = 3)
        q.add(toast("low1", Priority.LOW))
        q.add(toast("low2", Priority.LOW))
        q.add(toast("norm1", Priority.NORMAL))
        // queue full: low1, low2, norm1
        val result = q.add(toast("hi", Priority.HIGH))
        assertTrue(result is ToastQueue.AddResult.Visible)
        assertEquals("low1", (result as ToastQueue.AddResult.Visible).preempted?.id)
        assertEquals(listOf("hi", "low2", "norm1"), q.visible.value.map { it.id })
        // demoted low1 went to pending
        assertEquals(listOf("low1"), q.pendingSnapshot().map { it.id })
    }

    @Test
    fun high_does_not_preempt_normal() = runTest {
        val q = ToastQueue(maxVisible = 2)
        q.add(toast("n1", Priority.NORMAL))
        q.add(toast("n2", Priority.NORMAL))
        val result = q.add(toast("hi", Priority.HIGH))
        // no LOW present — HIGH goes to pending at front
        assertTrue(result is ToastQueue.AddResult.Pending)
        assertEquals(listOf("n1", "n2"), q.visible.value.map { it.id })
        assertEquals(listOf("hi"), q.pendingSnapshot().map { it.id })
    }

    @Test
    fun pending_orders_high_before_normal_before_low() = runTest {
        val q = ToastQueue(maxVisible = 1)
        q.add(toast("v", Priority.NORMAL))
        q.add(toast("p_low", Priority.LOW))
        q.add(toast("p_norm", Priority.NORMAL))
        q.add(toast("p_high", Priority.HIGH))
        assertEquals(listOf("p_high", "p_norm", "p_low"), q.pendingSnapshot().map { it.id })
    }

    @Test
    fun fifo_within_same_priority_in_pending() = runTest {
        val q = ToastQueue(maxVisible = 1)
        q.add(toast("v", Priority.NORMAL))
        q.add(toast("p1", Priority.NORMAL))
        q.add(toast("p2", Priority.NORMAL))
        q.add(toast("p3", Priority.NORMAL))
        assertEquals(listOf("p1", "p2", "p3"), q.pendingSnapshot().map { it.id })
    }

    @Test
    fun remove_visible_promotes_head_of_pending() = runTest {
        val q = ToastQueue(maxVisible = 2)
        q.add(toast("a"))
        q.add(toast("b"))
        q.add(toast("c"))  // pending
        q.remove("a")
        assertEquals(listOf("b", "c"), q.visible.value.map { it.id })
        assertTrue(q.pendingSnapshot().isEmpty())
    }

    @Test
    fun remove_unknown_id_returns_null() = runTest {
        val q = ToastQueue(maxVisible = 2)
        q.add(toast("a"))
        assertNull(q.remove("nope"))
        assertEquals(1, q.visible.value.size)
    }

    @Test
    fun remove_pending_does_not_affect_visible() = runTest {
        val q = ToastQueue(maxVisible = 1)
        q.add(toast("v"))
        q.add(toast("p"))
        val removed = q.remove("p")
        assertNotNull(removed)
        assertEquals("p", removed.id)
        assertEquals(listOf("v"), q.visible.value.map { it.id })
        assertTrue(q.pendingSnapshot().isEmpty())
    }

    @Test
    fun update_mutates_visible_toast() = runTest {
        val q = ToastQueue(maxVisible = 2)
        q.add(toast("a"))
        val updated = q.update("a") { it.copy(message = "changed", type = ToastType.Success) }
        assertNotNull(updated)
        assertEquals("changed", updated.message)
        assertEquals(ToastType.Success, updated.type)
        assertEquals("changed", q.visible.value.first().message)
    }

    @Test
    fun update_mutates_pending_toast() = runTest {
        val q = ToastQueue(maxVisible = 1)
        q.add(toast("v"))
        q.add(toast("p"))
        val updated = q.update("p") { it.copy(message = "p-new") }
        assertNotNull(updated)
        assertEquals("p-new", updated.message)
        assertEquals("p-new", q.pendingSnapshot().first().message)
    }

    @Test
    fun removeAll_clears_visible_and_pending() = runTest {
        val q = ToastQueue(maxVisible = 1)
        q.add(toast("v"))
        q.add(toast("p"))
        q.removeAll()
        assertTrue(q.visible.value.isEmpty())
        assertTrue(q.pendingSnapshot().isEmpty())
    }
}
