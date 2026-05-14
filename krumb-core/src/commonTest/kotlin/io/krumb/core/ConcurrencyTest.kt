@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package io.krumb.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration

class ConcurrencyTest {

    @Test
    fun concurrent_adds_produce_unique_ids_and_no_loss() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val c = ToastController(maxVisible = 5, scope = CoroutineScope(dispatcher + SupervisorJob()))

        val count = 50
        val handles = (0 until count).map { i ->
            async(dispatcher) {
                c.info("t-$i", duration = Duration.INFINITE)
            }
        }.awaitAll()

        runCurrent()

        // unique ids
        assertEquals(count, handles.map { it.id }.toSet().size)

        // visible + pending == count
        val visible = c.visible.value.size
        val pending = c.pendingSnapshot().size
        assertEquals(count, visible + pending)
        assertTrue(visible <= 5)
    }
}
