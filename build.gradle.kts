import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.vanniktech.maven.publish) apply false
}

// Shared publishing config applied to every module that opts in by applying
// the `com.vanniktech.maven.publish` plugin (krumb-core / -compose / -material3).
val krumbGroup = "io.github.nadeemiqbal"
val krumbVersion = "0.1.0"

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        extensions.configure(MavenPublishBaseExtension::class.java) {
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = false)
            // Only sign when GPG credentials are available (real Central
            // releases). Keeps `publishToMavenLocal` working credential-free.
            if (
                project.hasProperty("signingInMemoryKey") ||
                project.hasProperty("signing.keyId")
            ) {
                signAllPublications()
            }
            coordinates(krumbGroup, project.name, krumbVersion)

            pom {
                name.set(project.name)
                description.set(
                    "Krumb — a Compose Multiplatform toast / snackbar / notification library.",
                )
                url.set("https://github.com/NadeemIqbal/krumb")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("NadeemIqbal")
                        name.set("Nadeem Iqbal")
                        url.set("https://github.com/NadeemIqbal")
                    }
                }
                scm {
                    url.set("https://github.com/NadeemIqbal/krumb")
                    connection.set("scm:git:git://github.com/NadeemIqbal/krumb.git")
                    developerConnection.set("scm:git:ssh://git@github.com/NadeemIqbal/krumb.git")
                }
            }
        }
    }
}
