package io.krumb.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.krumb.compose.custom.CustomContentRegistry
import io.krumb.compose.custom.LocalCustomContentRegistry
import io.krumb.core.ToastController
import io.krumb.core.ToastPosition
import io.krumb.core.Toaster

/**
 * Host that overlays toasts on top of [content].
 *
 * Place this once near the root of your app:
 * ```
 * MaterialTheme {
 *     ToasterHost {
 *         // your app content
 *     }
 * }
 * ```
 *
 * Position and visible-count are intentionally NOT host parameters:
 *  - **Position** is a per-toast property ([io.krumb.core.Toast.position]);
 *    the host renders a separate stack for every [ToastPosition] in use.
 *  - **maxVisible** is owned by the [ToastController]. To change it, build a
 *    `ToastController(maxVisible = n)` and pass it here (and/or install it
 *    via `Toaster.setControllerForTesting`).
 *
 * @param controller the controller whose toasts are rendered. Defaults to
 *   the global [Toaster] controller, so `Toaster.success(...)` from anywhere
 *   shows up here.
 * @param style visual style applied to every toast.
 */
@Composable
public fun ToasterHost(
    modifier: Modifier = Modifier,
    controller: ToastController = Toaster.controller,
    style: KrumbStyle = KrumbDefaults.defaultStyle(),
    content: @Composable () -> Unit,
) {
    val registry: CustomContentRegistry = remember { CustomContentRegistry() }
    val visible by controller.visible.collectAsState()

    CompositionLocalProvider(LocalCustomContentRegistry provides registry) {
        Box(modifier.fillMaxSize()) {
            content()

            val byPosition = remember(visible) { visible.groupBy { it.position } }

            Box(
                Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(12.dp),
            ) {
                ToastPosition.entries.forEach { position ->
                    val toastsHere = byPosition[position].orEmpty()
                    if (toastsHere.isNotEmpty()) {
                        ToastStack(
                            toasts = toastsHere,
                            position = position,
                            controller = controller,
                            style = style,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
