package io.krumb.sample.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.krumb.material3.Material3ToasterHost
import io.krumb.sample.SampleApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Krumb Sample",
        state = rememberWindowState(width = 480.dp, height = 800.dp),
    ) {
        MaterialTheme {
            Material3ToasterHost {
                SampleApp()
            }
        }
    }
}
