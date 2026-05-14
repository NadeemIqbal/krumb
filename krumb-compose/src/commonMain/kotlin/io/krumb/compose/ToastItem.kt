package io.krumb.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.krumb.compose.custom.GlobalCustomContent
import io.krumb.compose.gestures.pauseOnHover
import io.krumb.compose.gestures.swipeToDismiss
import io.krumb.core.Toast
import io.krumb.core.ToastController
import io.krumb.core.ToastType

@Composable
internal fun ToastItem(
    toast: Toast,
    controller: ToastController,
    style: KrumbStyle,
    modifier: Modifier = Modifier,
) {
    val bg = style.backgroundColor(toast.type)
    val fg = style.contentColor(toast.type)

    Box(
        modifier
            .widthIn(min = 240.dp, max = 420.dp)
            .shadow(elevation = style.elevation, shape = RoundedCornerShape(style.cornerRadius))
            .clip(RoundedCornerShape(style.cornerRadius))
            .background(bg)
            .pauseOnHover(controller, toast.id)
            .swipeToDismiss { controller.dismiss(toast.id) },
    ) {
        val customKey = toast.customContentKey
        if (toast.type == ToastType.Custom && customKey != null) {
            val custom = remember(customKey) { GlobalCustomContent.get(customKey) }
            if (custom != null) {
                custom()
            } else {
                Box(Modifier.padding(style.horizontalPadding, style.verticalPadding))
            }
        } else {
            DefaultToastBody(toast, style, fg)
        }
        ToastProgressBar(
            duration = toast.duration,
            paused = false,
            tint = fg.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun DefaultToastBody(toast: Toast, style: KrumbStyle, contentColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = style.horizontalPadding,
                vertical = style.verticalPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = iconFor(toast.type),
            contentDescription = null,
            tint = style.iconTint(toast.type),
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(style.itemSpacing))
        Column(Modifier.weight(1f)) {
            Text(
                text = toast.message,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        val action = toast.action
        if (action != null) {
            Spacer(Modifier.width(style.itemSpacing))
            TextButton(
                onClick = action.onClick,
                colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
            ) {
                Text(action.label)
            }
        }
    }
}

private fun iconFor(type: ToastType): ImageVector = when (type) {
    ToastType.Success -> Icons.Filled.CheckCircle
    ToastType.Error -> Icons.Outlined.ErrorOutline
    ToastType.Warning -> Icons.Filled.Warning
    ToastType.Info -> Icons.Filled.Info
    ToastType.Loading -> Icons.Filled.HourglassBottom
    ToastType.Custom -> Icons.Filled.Info
}
