import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

allprojects {
    group = "dev.lounres"
    version = "1.0.0"
}

val jvmTargetVersion: String by project

fun PluginManager.withPlugin(pluginDep: PluginDependency, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDep.pluginId, block)
fun PluginManager.withPlugin(pluginDepProvider: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDepProvider.get().pluginId, block)

val platformAttribute = Attribute.of("dev.lounres.platform", String::class.java)

stal {
    action {
        on("server") {
            pluginManager.withPlugin(libs.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    jvm("server") {
                        attributes.attribute(platformAttribute, "server")
                    }
                }
            }
        }
        on("desktop") {
            pluginManager.withPlugin(libs.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    jvm("desktop") {
                        attributes.attribute(platformAttribute, "desktop")
                    }
                }
            }
        }
//        on("web") {
//            pluginManager.withPlugin(libs.plugins.kotlin.multiplatform) {
//                @OptIn(ExperimentalWasmDsl::class)
//                configure<KotlinMultiplatformExtension> {
//                    wasm("web") {
//                        attributes.attribute(platformAttribute, "web")
//                    }
//                }
//            }
//        }
//        on("android") {
//            pluginManager.withPlugin(libs.plugins.kotlin.multiplatform) {
//                configure<KotlinMultiplatformExtension> {
//                    android() {
//                        attributes.attribute(platformAttribute, "android")
//                    }
//                }
//            }
//        }
        on("kotlin") {
            apply<KotlinMultiplatformPluginWrapper>()
            configure<KotlinMultiplatformExtension> {

                @Suppress("UNUSED_VARIABLE")
                sourceSets {
                    all {
                        languageSettings {
                            progressiveMode = true
                            languageVersion = "1.9"
                            enableLanguageFeature("ContextReceivers")
                            optIn("kotlin.contracts.ExperimentalContracts")
                            optIn("kotlin.ExperimentalStdlibApi")
                        }
                    }
                    val commonTest by getting {
                        dependencies {
                            implementation(kotlin("test"))
                        }
                    }
                }
            }
            afterEvaluate {
                the<KotlinMultiplatformExtension>().targets.withType<KotlinJvmTarget> {
                    compilations.all {
                        kotlinOptions {
                            jvmTarget = jvmTargetVersion
                            freeCompilerArgs += listOf(
                                "-Xlambdas=indy"
                            )
                        }
                    }
                    testRuns.all {
                        executionTask {
                            useJUnitPlatform()
                        }
                    }
                }
                yarn.lockFileDirectory = rootDir.resolve("gradle")
            }
        }
        on("library") {
            pluginManager.withPlugin(libs.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    explicitApi = ExplicitApiMode.Warning
                }
            }
        }
    }
}