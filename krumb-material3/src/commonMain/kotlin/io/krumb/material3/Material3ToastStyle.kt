package io.krumb.material3

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import io.krumb.compose.KrumbDefaults
import io.krumb.compose.KrumbStyle
import io.krumb.core.ToastType

/**
 * A [KrumbStyle] tuned for Material 3 apps.
 *
 * Success / Error / Warning use **semantic colors** (green / red / amber) so a
 * success toast is always recognisably green regardless of the app's theme —
 * this is the convention users expect from a toast library. Info / Loading /
 * Custom defer to [MaterialTheme.colorScheme] so they blend with the app's
 * branding and light/dark mode.
 */
@Composable
@ReadOnlyComposable
public fun material3ToastStyle(): KrumbStyle {
    val cs = MaterialTheme.colorScheme

    // Semantic colors — readable in both light and dark.
    val successBg = Color(0xFF1F8A4C)
    val errorBg = Color(0xFFC0392B)
    val warningBg = Color(0xFFB26A00)
    val onSemantic = Color.White

    return KrumbStyle(
        backgroundColor = { type ->
            when (type) {
                ToastType.Success -> successBg
                ToastType.Error -> errorBg
                ToastType.Warning -> warningBg
                ToastType.Info -> cs.inverseSurface
                ToastType.Loading -> cs.surfaceVariant
                ToastType.Custom -> cs.surfaceContainerHigh
            }
        },
        contentColor = { type ->
            when (type) {
                ToastType.Success, ToastType.Error, ToastType.Warning -> onSemantic
                ToastType.Info -> cs.inverseOnSurface
                ToastType.Loading -> cs.onSurfaceVariant
                ToastType.Custom -> cs.onSurface
            }
        },
        iconTint = { type ->
            when (type) {
                ToastType.Success, ToastType.Error, ToastType.Warning -> onSemantic
                ToastType.Info -> cs.inverseOnSurface
                ToastType.Loading -> cs.onSurfaceVariant
                ToastType.Custom -> cs.onSurface
            }
        },
        cornerRadius = KrumbDefaults.cornerRadius,
        elevation = KrumbDefaults.elevation,
        horizontalPadding = KrumbDefaults.horizontalPadding,
        verticalPadding = KrumbDefaults.verticalPadding,
        itemSpacing = KrumbDefaults.itemSpacing,
    )
}
