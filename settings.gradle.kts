pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "krumb"

include(":krumb-core")
include(":krumb-compose")
include(":krumb-material3")

include(":sample:shared")
include(":sample:androidApp")
include(":sample:desktopApp")
include(":sample:webApp")
// iOS sample is an Xcode project under sample/iosApp consuming the shared framework.
