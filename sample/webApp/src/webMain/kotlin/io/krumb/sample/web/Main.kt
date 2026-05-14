package io.krumb.sample.web

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.krumb.material3.Material3ToasterHost
import io.krumb.sample.SampleApp
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        MaterialTheme {
            Material3ToasterHost {
                SampleApp()
            }
        }
    }
}
