import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(versions.plugins.kotlin.compose)
    alias(versions.plugins.compose.multiplatform)
//    alias(versions.plugins.ktor)
//    alias(versions.plugins.kotlinx.serialization)
}

kotlin {
//    js {
//        moduleName = "client-web"
//        browser {
//            commonWebpackConfig {
//                outputFileName = "HalfHat.js"
//            }
//        }
//        binaries.executable()
//    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "HalfHat.js"
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
    resources {
        packageOfResClass = "dev.lounres.halfhat.client.web.resources"
        generateResClass = always
        publicResClass = true
    }
}