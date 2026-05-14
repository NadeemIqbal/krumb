package io.krumb.material3

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.krumb.compose.ToasterHost
import io.krumb.core.ToastController
import io.krumb.core.Toaster

/**
 * A [ToasterHost] pre-wired with the Material 3 style ([material3ToastStyle]).
 *
 * Drop-in replacement for `ToasterHost` when your app uses `MaterialTheme`:
 * ```
 * MaterialTheme {
 *     Material3ToasterHost {
 *         // app content
 *     }
 * }
 * ```
 */
@Composable
public fun Material3ToasterHost(
    modifier: Modifier = Modifier,
    controller: ToastController = Toaster.controller,
    content: @Composable () -> Unit,
) {
    ToasterHost(
        modifier = modifier,
        controller = controller,
        style = material3ToastStyle(),
        content = content,
    )
}
