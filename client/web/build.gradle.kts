import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.compose)
//    alias(libs.plugins.ktor)
//    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "client-web"
        browser {
            commonWebpackConfig {
                outputFileName = "client-web.js"
//                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                    // Uncomment and configure this if you want to open a browser different from the system default
//                    // open = mapOf(
//                    //     "app" to mapOf(
//                    //         "name" to "google chrome"
//                    //     )
//                    // )
//
//                    static = (static ?: mutableListOf()).apply {
//                        // Serve sources to debug inside browser
//                        add(project.rootDir.path)
//                        add(project.rootDir.path + "/client/web/")
//                    }
//                }
            }
        }
        binaries.executable()
    }
    sourceSets {
        wasmJsMain {
            dependencies {
                implementation(projects.client.common)

                // Compose
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
            }
        }
    }
}

compose {
//    kotlinCompilerPlugin.set("1.5.3")
    experimental.web.application {}
}