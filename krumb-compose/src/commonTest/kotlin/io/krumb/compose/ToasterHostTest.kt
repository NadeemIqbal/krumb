@file:OptIn(ExperimentalTestApi::class)

package io.krumb.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import io.krumb.core.ToastController
import kotlin.test.Test
import kotlin.time.Duration

class ToasterHostTest {

    @Test
    fun host_renders_a_toast_pushed_through_the_controller() = runComposeUiTest {
        val controller = ToastController()

        setContent {
            MaterialTheme {
                ToasterHost(controller = controller) {
                    Text("app content")
                }
            }
        }

        onNodeWithText("app content").assertIsDisplayed()

        controller.success("Saved!", duration = Duration.INFINITE)

        waitUntilExactlyOneExists(
            hasText("Saved!"),
            timeoutMillis = 3_000,
        )
        onNodeWithText("Saved!").assertIsDisplayed()
    }

    @Test
    fun host_renders_multiple_toasts_in_a_stack() = runComposeUiTest {
        val controller = ToastController(maxVisible = 3)

        setContent {
            MaterialTheme {
                ToasterHost(controller = controller) {
                    Text("root")
                }
            }
        }

        controller.info("one", duration = Duration.INFINITE)
        controller.info("two", duration = Duration.INFINITE)
        controller.info("three", duration = Duration.INFINITE)

        waitUntilExactlyOneExists(hasText("one"), timeoutMillis = 3_000)
        waitUntilExactlyOneExists(hasText("two"), timeoutMillis = 3_000)
        waitUntilExactlyOneExists(hasText("three"), timeoutMillis = 3_000)

        onNodeWithText("one").assertIsDisplayed()
        onNodeWithText("two").assertIsDisplayed()
        onNodeWithText("three").assertIsDisplayed()
    }

    @Test
    fun dismiss_removes_a_toast_from_the_host() = runComposeUiTest {
        val controller = ToastController()

        setContent {
            MaterialTheme {
                ToasterHost(controller = controller) {
                    Text("base")
                }
            }
        }

        val handle = controller.info("ephemeral", duration = Duration.INFINITE)
        waitUntilExactlyOneExists(hasText("ephemeral"), timeoutMillis = 3_000)

        handle.dismiss()
        waitUntil(timeoutMillis = 3_000) {
            onAllNodesWithText("ephemeral").fetchSemanticsNodes().isEmpty()
        }
    }
}
