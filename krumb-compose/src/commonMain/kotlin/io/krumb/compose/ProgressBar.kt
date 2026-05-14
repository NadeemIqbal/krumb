package io.krumb.compose

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.time.Duration

/**
 * Thin linear bar at the bottom of a toast, animating from 1f → 0f over
 * [duration]. If [paused] is true, the animation is frozen at its current
 * value.
 *
 * Returns nothing visible if [duration] is [Duration.INFINITE].
 */
@Composable
internal fun ToastProgressBar(
    duration: Duration,
    paused: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    if (duration == Duration.INFINITE) return

    var target by remember { mutableStateOf(1f) }

    LaunchedEffect(duration, paused) {
        if (!paused) {
            target = 0f
        }
    }

    val progress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(
            durationMillis = if (paused) 0 else duration.inWholeMilliseconds.toInt(),
        ),
        label = "toast-progress",
    )

    Box(
        modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(tint.copy(alpha = 0.4f)),
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress)
                .height(2.dp)
                .background(tint),
        )
    }
}
