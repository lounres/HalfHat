import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose) apply false
}

val jvmTargetVersion: String by project

fun PluginManager.withPlugin(pluginDep: PluginDependency, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDep.pluginId, block)
fun PluginManager.withPlugin(pluginDepProvider: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDepProvider.get().pluginId, block)

stal {
    action {
        "server" {
            pluginManager.withPlugin(rootProject.libs.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    jvm()
                }
            }
        }
        "desktop" {
            pluginManager.withPlugin(rootProject.libs.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    jvm()
                }
            }
        }
        "web" {
            pluginManager.withPlugin(rootProject.libs.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    @OptIn(ExperimentalWasmDsl::class)
                    wasmJs {
                        moduleName = project.path.substring(startIndex = 1).replace(':', '-')
                        browser()
                        nodejs()
                        d8()
                    }
                }
            }
        }
//        "android" {
//            pluginManager.withPlugin(rootProject.libs.plugins.kotlin.multiplatform) {
//                configure<KotlinMultiplatformExtension> {
//                    android() {
//                        attributes.attribute(platformAttribute, "android")
//                    }
//                }
//            }
//        }
        "kotlin" {
            apply<KotlinMultiplatformPluginWrapper>()
            configure<KotlinMultiplatformExtension> {

                sourceSets {
                    all {
                        languageSettings {
                            progressiveMode = true
                            enableLanguageFeature("ContextReceivers")
                            optIn("kotlin.contracts.ExperimentalContracts")
                            optIn("kotlin.ExperimentalStdlibApi")
                        }
                    }
                    commonTest {
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
                pluginManager.withPlugin("org.gradle.java") {
                    configure<JavaPluginExtension> {
                        sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion.toInt())
                    }
                }
            }
        }
        "library" {
            pluginManager.withPlugin(rootProject.libs.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    explicitApi = ExplicitApiMode.Warning
                }
            }
        }
    }
}