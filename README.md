# Krumb

**The sonner of Compose Multiplatform.** A toast / snackbar / in-app notification
library for Compose Multiplatform — callable from anywhere, beautiful by default,
feature-complete.

Targets **Android · iOS · Desktop (JVM) · Web (Wasm)**.

> Status: `0.1.0` — pre-release. API surface is stable except for members marked
> `@ExperimentalKrumbApi` (`promise`, `custom`).

## Modules

| Artifact | What it is |
|---|---|
| `io.github.nadeemiqbal:krumb-core` | Pure-Kotlin engine — queue, controller, `Toaster` facade. No UI dependency. |
| `io.github.nadeemiqbal:krumb-compose` | `ToasterHost` composable, stacking, spring animations, swipe-to-dismiss, pause-on-hover, progress bar, custom-content. |
| `io.github.nadeemiqbal:krumb-material3` | Material 3 themed defaults — colors keyed off `MaterialTheme.colorScheme`. |

## Install

In `gradle/libs.versions.toml`:

```toml
[libraries]
krumb-compose    = { module = "io.github.nadeemiqbal:krumb-compose",    version = "0.1.0" }
krumb-material3  = { module = "io.github.nadeemiqbal:krumb-material3",  version = "0.1.0" }
```

In your shared module's `commonMain`:

```kotlin
commonMain.dependencies {
    implementation(libs.krumb.material3) // pulls in krumb-compose + krumb-core
}
```

## Setup

Wrap your app once:

```kotlin
@Composable
fun App() {
    MaterialTheme {
        Material3ToasterHost {
            // your app content
        }
    }
}
```

## Usage

```kotlin
// One-liners — callable from anywhere (ViewModels, coroutines, composables)
Toaster.success("Profile saved")
Toaster.error("Network failed")
Toaster.info("3 new messages")
Toaster.warning("Battery low")
val handle = Toaster.loading("Uploading…")   // returns a handle

// Builder + action button
Toaster.show("Deleted item") {
    action("Undo") { viewModel.undo() }
    duration = 5.seconds
    position = ToastPosition.BottomCenter
}

// Promise pattern
Toaster.promise(
    block = { repository.save() },
    loading = "Saving…",
    success = { "Saved successfully" },
    error = { e -> "Failed: ${e.message}" },
)

// Fully custom composable content
Toaster.custom(duration = 4.seconds) {
    Row { /* anything */ }
}

// Programmatic control
handle.update(message = "Done", type = ToastType.Success)
handle.dismiss()
Toaster.dismissAll()
```

## Building this repo

```bash
./gradlew :krumb-core:desktopTest        # engine unit tests
./gradlew :krumb-compose:desktopTest     # host UI tests
./gradlew :sample:desktopApp:run         # run the desktop showcase
./gradlew :sample:androidApp:installDebug
./gradlew :sample:webApp:wasmJsBrowserDevelopmentRun
# iOS: open sample/iosApp/iosApp.xcodeproj in Xcode and run
```

## Publishing

Artifacts publish to Maven Central (Sonatype Central Portal) via the
Vanniktech plugin. Provide credentials in `~/.gradle/gradle.properties`:

```properties
mavenCentralUsername=<central-portal-token-user>
mavenCentralPassword=<central-portal-token-pass>
signingInMemoryKey=<armored-gpg-private-key>
signingInMemoryKeyId=<key-id>
signingInMemoryKeyPassword=<key-password>
```

Then:

```bash
./gradlew publishToMavenLocal                                  # local verification (no keys needed)
./gradlew publishAllPublicationsToMavenCentralRepository       # real release (keys required)
```

`automaticRelease` is off — approve the release in the Central Portal UI.

## License

Apache 2.0.
