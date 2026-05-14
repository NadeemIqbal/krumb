package io.krumb.compose.gestures

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Drag the receiver horizontally. If the drag distance crosses ~96dp, fires
 * [onDismiss]. Otherwise snaps back to its original position.
 */
internal fun Modifier.swipeToDismiss(onDismiss: () -> Unit): Modifier = composed {
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val dismissThresholdPx = with(density) { 96.dp.toPx() }

    this
        .graphicsLayer {
            translationX = offsetX.value
            alpha = (1f - (abs(offsetX.value) / (dismissThresholdPx * 3)).coerceIn(0f, 0.7f))
        }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    if (abs(offsetX.value) > dismissThresholdPx) {
                        scope.launch {
                            offsetX.animateTo(
                                if (offsetX.value > 0) 1500f else -1500f,
                                animationSpec = spring(stiffness = 500f),
                            )
                            onDismiss()
                        }
                    } else {
                        scope.launch {
                            offsetX.animateTo(0f, animationSpec = spring())
                        }
                    }
                },
                onDragCancel = {
                    scope.launch { offsetX.animateTo(0f) }
                },
            ) { _, dragAmount ->
                scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
            }
        }
}
