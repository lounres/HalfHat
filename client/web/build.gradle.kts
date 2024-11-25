import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(versions.plugins.compose)
    alias(versions.plugins.jetbrains.compose)
//    alias(versions.plugins.ktor)
//    alias(versions.plugins.kotlinx.serialization)
}

kotlin {
//    js {
//        moduleName = "client-web"
//        browser {
//            commonWebpackConfig {
//                outputFileName = "client-web.js"
//            }
//        }
//        binaries.executable()
//    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "client-web.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    // Uncomment and configure this if you want to open a browser different from the system default
                    // open = mapOf(
                    //     "app" to mapOf(
                    //         "name" to "google chrome"
                    //     )
                    // )

                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDir.path + "/api")
                        add(rootDir.path + "/client/common")
                        add(rootDir.path + "/client/web/")
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.client.common)
            }
        }
        wasmJsMain {
            dependencies {
//                implementation(project.dependencies.enforcedPlatform(versions.kotlin.wrappers.bom))
//                implementation(versions.kotlin.wrappers.browser)
            }
        }
    }
}

compose {
    experimental.web.application {}
}