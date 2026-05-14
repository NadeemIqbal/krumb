package io.krumb.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import io.krumb.compose.animations.enterFor
import io.krumb.compose.animations.exitFor
import io.krumb.core.Toast
import io.krumb.core.ToastController
import io.krumb.core.ToastPosition

private class ToastSlot(
    val toast: Toast,
    val transitionState: MutableTransitionState<Boolean>,
)

/**
 * Renders a vertical stack of toasts at a single [position] with a depth /
 * scale effect: the front item is full size, items behind it shrink and
 * fade. Handles enter AND exit animations by tracking per-item transition
 * state — when a toast leaves [toasts] its slot animates out before being
 * removed from composition.
 */
@Composable
internal fun ToastStack(
    toasts: List<Toast>,
    position: ToastPosition,
    controller: ToastController,
    style: KrumbStyle,
    modifier: Modifier = Modifier,
) {
    val slots: SnapshotStateList<ToastSlot> = remember { mutableStateListOf() }

    // Sync incoming toasts into slots.
    LaunchedEffect(toasts) {
        val incomingIds = toasts.map { it.id }.toSet()
        // add new
        toasts.forEach { toast ->
            if (slots.none { it.toast.id == toast.id }) {
                slots.add(
                    ToastSlot(
                        toast = toast,
                        transitionState = MutableTransitionState(false).apply { targetState = true },
                    ),
                )
            }
        }
        // mark removed slots as leaving
        slots.forEach { slot ->
            if (slot.toast.id !in incomingIds) {
                slot.transitionState.targetState = false
            }
        }
    }

    val isBottom = position in listOf(
        ToastPosition.BottomStart,
        ToastPosition.BottomCenter,
        ToastPosition.BottomEnd,
    )

    val ordered = if (isBottom) slots.toList() else slots.toList().asReversed()

    Box(modifier) {
        ordered.forEachIndexed { index, slot ->
            val frontIndex = ordered.lastIndex - index
            val scale by animateFloatAsState(
                targetValue = when (frontIndex) {
                    0 -> 1f
                    1 -> 0.94f
                    else -> 0.88f
                },
                animationSpec = spring(),
                label = "toast-scale",
            )
            val alpha by animateFloatAsState(
                targetValue = when (frontIndex) {
                    0 -> 1f
                    1 -> 0.85f
                    else -> 0.6f
                },
                animationSpec = spring(),
                label = "toast-alpha",
            )
            val yOffset by animateFloatAsState(
                targetValue = frontIndex * if (isBottom) -8f else 8f,
                animationSpec = spring(),
                label = "toast-yoffset",
            )

            key(slot.toast.id) {
                // remove slot once it has fully exited
                LaunchedEffect(slot.transitionState.isIdle, slot.transitionState.currentState) {
                    if (slot.transitionState.isIdle && !slot.transitionState.currentState) {
                        slots.remove(slot)
                    }
                }

                Box(
                    modifier = Modifier
                        .align(stackAlignmentFor(position))
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationY = yOffset
                            transformOrigin = if (isBottom) {
                                TransformOrigin(0.5f, 1f)
                            } else {
                                TransformOrigin(0.5f, 0f)
                            }
                        }
                        .alpha(alpha),
                ) {
                    AnimatedVisibility(
                        visibleState = slot.transitionState,
                        enter = enterFor(position),
                        exit = exitFor(position),
                    ) {
                        ToastItem(
                            toast = slot.toast,
                            controller = controller,
                            style = style,
                        )
                    }
                }
            }
        }
    }
}

internal fun stackAlignmentFor(position: ToastPosition): Alignment = when (position) {
    ToastPosition.TopStart -> Alignment.TopStart
    ToastPosition.TopCenter -> Alignment.TopCenter
    ToastPosition.TopEnd -> Alignment.TopEnd
    ToastPosition.BottomStart -> Alignment.BottomStart
    ToastPosition.BottomCenter -> Alignment.BottomCenter
    ToastPosition.BottomEnd -> Alignment.BottomEnd
}
