package io.krumb.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.window.ComposeUIViewController
import io.krumb.material3.Material3ToasterHost
import platform.UIKit.UIViewController

/** Entry point consumed by `iosApp` (Swift): `MainViewControllerKt.MainViewController()`. */
fun MainViewController(): UIViewController = ComposeUIViewController {
    MaterialTheme {
        Material3ToasterHost {
            SampleApp()
        }
    }
}
