package io.krumb.compose.animations

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import io.krumb.core.ToastPosition

internal fun enterFor(position: ToastPosition): EnterTransition {
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    val slideSpec = spring<androidx.compose.ui.unit.IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )
    return when (position) {
        ToastPosition.TopStart,
        ToastPosition.TopCenter,
        ToastPosition.TopEnd -> slideInVertically(slideSpec) { -it } + fadeIn(springSpec)
        ToastPosition.BottomStart,
        ToastPosition.BottomCenter,
        ToastPosition.BottomEnd -> slideInVertically(slideSpec) { it } + fadeIn(springSpec)
    }
}

internal fun exitFor(position: ToastPosition): ExitTransition {
    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val slideSpec = spring<androidx.compose.ui.unit.IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    return when (position) {
        ToastPosition.TopStart,
        ToastPosition.TopCenter,
        ToastPosition.TopEnd -> slideOutVertically(slideSpec) { -it - 100 } + fadeOut(springSpec)
        ToastPosition.BottomStart,
        ToastPosition.BottomCenter,
        ToastPosition.BottomEnd -> slideOutVertically(slideSpec) { it + 100 } + fadeOut(springSpec)
    }
}
