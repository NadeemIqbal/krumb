package io.krumb.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.krumb.compose.custom.custom
import io.krumb.core.ExperimentalKrumbApi
import io.krumb.core.Priority
import io.krumb.core.ToastPosition
import io.krumb.core.Toaster
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * The shared showcase UI. Each platform entry point simply hosts this inside
 * a `Material3ToasterHost { ... }`.
 */
@OptIn(ExperimentalKrumbApi::class)
@Composable
fun SampleApp() {
    val scope = rememberCoroutineScope()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                "Krumb",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "The sonner of Compose Multiplatform.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Section("One-liners") {
                SampleButton("Success") { Toaster.success("Profile saved") }
                SampleButton("Error") { Toaster.error("Network failed") }
                SampleButton("Info") { Toaster.info("3 new messages") }
                SampleButton("Warning") { Toaster.warning("Battery low") }
                SampleButton("Loading (manual dismiss)") {
                    val h = Toaster.loading("Uploading…")
                    scope.launchAfter(2.seconds) {
                        h.update(message = "Uploaded!", type = io.krumb.core.ToastType.Success)
                    }
                }
            }

            Section("Builder + action") {
                SampleButton("Undo toast") {
                    Toaster.show("Deleted item") {
                        action("Undo") { Toaster.info("Restored") }
                        duration = 5.seconds
                        position = ToastPosition.BottomCenter
                    }
                }
                SampleButton("Bottom-end, 10s") {
                    Toaster.show("Pinned to bottom-end") {
                        duration = 10.seconds
                        position = ToastPosition.BottomEnd
                    }
                }
            }

            Section("Promise") {
                SampleButton("Promise — success") {
                    scope.launchCatching {
                        Toaster.promise(
                            block = { delay(1500); "done" },
                            loading = "Saving…",
                            success = { "Saved successfully" },
                            error = { e -> "Failed: ${e.message}" },
                        )
                    }
                }
                SampleButton("Promise — failure") {
                    scope.launchCatching {
                        Toaster.promise(
                            block = { delay(1500); error("server exploded") },
                            loading = "Processing…",
                            success = { "ok" },
                            error = { e -> "Failed: ${e.message}" },
                        )
                    }
                }
            }

            Section("Custom content") {
                SampleButton("Custom composable toast") {
                    Toaster.custom(duration = 6.seconds) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Fully custom!", fontWeight = FontWeight.Bold)
                                Text(
                                    "Any composable you want.",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }

            Section("Queue & priority") {
                SampleButton("Spam 8 normal toasts") {
                    repeat(8) { i -> Toaster.info("Toast #${i + 1}") }
                }
                SampleButton("LOW spam, then HIGH") {
                    repeat(5) { i ->
                        Toaster.show("Low #${i + 1}") { priority = Priority.LOW }
                    }
                    Toaster.show("HIGH — jumps the queue") {
                        priority = Priority.HIGH
                        duration = 6.seconds
                    }
                }
            }

            Section("Dismiss") {
                SampleButton("Dismiss all") { Toaster.dismissAll() }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun SampleButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(label)
    }
}
