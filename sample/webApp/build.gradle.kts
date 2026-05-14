import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs("web") {
        outputModuleName.set("krumb-sample")
        browser {
            commonWebpackConfig {
                outputFileName = "krumb-sample.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val webMain by getting {
            dependencies {
                implementation(project(":sample:shared"))
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.material3)
                implementation(compose.foundation)
            }
        }
    }
}
