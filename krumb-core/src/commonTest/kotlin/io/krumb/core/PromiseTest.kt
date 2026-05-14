@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.krumb.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalKrumbApi::class)
class PromiseTest {

    @Test
    fun success_path_loading_then_success_then_auto_dismiss() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val c = ToastController(scope = CoroutineScope(dispatcher))

        val deferred = async(dispatcher) {
            c.promise(
                block = { delay(500); "ok" },
                loading = "Saving…",
                success = { "Saved: $it" },
                error = { "Failed: ${it.message}" },
            )
        }
        runCurrent()
        // loading visible
        val loading = c.visible.value.firstOrNull()
        assertNotNull(loading)
        assertEquals(ToastType.Loading, loading.type)
        assertEquals("Saving…", loading.message)

        // complete the block
        advanceTimeBy(600.milliseconds)
        runCurrent()

        assertEquals("ok", deferred.await())

        // toast updated to Success
        val updated = c.visible.value.firstOrNull()
        assertNotNull(updated)
        assertEquals(ToastType.Success, updated.type)
        assertEquals("Saved: ok", updated.message)

        // success default duration is 4s — advance past and confirm dismissal
        advanceTimeBy(5.seconds)
        runCurrent()
        assertTrue(c.visible.value.isEmpty(), "success toast should auto-dismiss after rearm")
    }

    @Test
    fun error_path_loading_then_error_and_rethrow() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val c = ToastController(scope = CoroutineScope(dispatcher))

        val thrown = assertFailsWith<IllegalStateException> {
            c.promise(
                block = { error("boom") },
                loading = "Working…",
                success = { "no" },
                error = { "Err: ${it.message}" },
            )
        }
        assertEquals("boom", thrown.message)

        runCurrent()
        val t = c.visible.value.firstOrNull()
        assertNotNull(t)
        assertEquals(ToastType.Error, t.type)
        assertTrue(t.message.startsWith("Err:"))
    }

    @Test
    fun cancellation_dismisses_loading_and_rethrows() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val c = ToastController(scope = CoroutineScope(dispatcher))

        val started = CompletableDeferred<Unit>()
        val job: Job = launch(dispatcher) {
            val r: String = c.promise(
                block = {
                    started.complete(Unit)
                    delay(60.seconds)
                    "ok"
                },
                loading = "Long…",
                success = { it },
                error = { it.message ?: "" },
            )
            check(r.isNotEmpty())
        }
        runCurrent()
        assertTrue(started.isCompleted)
        assertEquals(1, c.visible.value.size)

        job.cancelAndJoin()
        runCurrent()
        assertTrue(c.visible.value.isEmpty(), "loading toast should be dismissed on cancellation")
    }
}
