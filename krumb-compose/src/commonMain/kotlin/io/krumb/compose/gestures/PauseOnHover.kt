package io.krumb.compose.gestures

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import io.krumb.core.ToastController

/**
 * Pauses [controller]'s dismissal timer for [id] while the receiver is
 * hovered; resumes on hover exit. No-op on touch-only platforms.
 */
internal fun Modifier.pauseOnHover(
    controller: ToastController,
    id: String,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered, id) {
        if (isHovered) controller.pause(id) else controller.resume(id)
    }
    this.hoverable(interactionSource)
}
