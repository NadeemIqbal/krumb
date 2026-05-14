package io.krumb.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.krumb.core.ToastType

/**
 * Visual style tokens for toasts.
 *
 * Krumb's base library ships a generic style; `krumb-material3` provides an
 * `M3` mapping that pulls colors from `MaterialTheme.colorScheme`.
 */
@Immutable
public data class KrumbStyle(
    val backgroundColor: (ToastType) -> Color,
    val contentColor: (ToastType) -> Color,
    val iconTint: (ToastType) -> Color,
    val cornerRadius: Dp,
    val elevation: Dp,
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val itemSpacing: Dp,
)

public object KrumbDefaults {
    public val cornerRadius: Dp = 12.dp
    public val elevation: Dp = 8.dp
    public val horizontalPadding: Dp = 16.dp
    public val verticalPadding: Dp = 12.dp
    public val itemSpacing: Dp = 8.dp

    @Composable
    @ReadOnlyComposable
    public fun defaultStyle(): KrumbStyle = KrumbStyle(
        backgroundColor = { type ->
            when (type) {
                ToastType.Success -> Color(0xFF1F8A4C)
                ToastType.Error -> Color(0xFFC0392B)
                ToastType.Warning -> Color(0xFFD68910)
                ToastType.Info -> Color(0xFF2563EB)
                ToastType.Loading -> Color(0xFF374151)
                ToastType.Custom -> Color(0xFF1F2937)
            }
        },
        contentColor = { Color.White },
        iconTint = { Color.White },
        cornerRadius = cornerRadius,
        elevation = elevation,
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        itemSpacing = itemSpacing,
    )
}
