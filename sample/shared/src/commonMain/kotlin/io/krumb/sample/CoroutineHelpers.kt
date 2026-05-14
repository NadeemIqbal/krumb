package io.krumb.sample

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

/** Launches [block] after [delay], swallowing any failure (sample-only convenience). */
internal fun CoroutineScope.launchAfter(delay: Duration, block: suspend () -> Unit) {
    launch {
        delay(delay)
        runCatching { block() }
    }
}

/** Launches [block], swallowing any thrown exception so the sample never crashes. */
internal fun CoroutineScope.launchCatching(block: suspend () -> Unit) {
    launch {
        runCatching { block() }
    }
}
