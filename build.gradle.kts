@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension
import org.gradle.accessors.dm.LibrariesForVersions
import org.gradle.accessors.dm.RootProjectAccessor
import org.gradle.kotlin.dsl.getByName
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget


plugins {
    alias(versions.plugins.kotlin.multiplatform) apply false
    alias(versions.plugins.kotlin.compose) apply false
    alias(versions.plugins.kotlinx.atomicfu) apply false
    alias(versions.plugins.kotlinx.serialization) apply false
    alias(versions.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform.hotReload) apply false
}

val Project.versions: LibrariesForVersions get() = rootProject.extensions.getByName<LibrariesForVersions>("versions")
val Project.projects: RootProjectAccessor get() = rootProject.extensions.getByName<RootProjectAccessor>("projects")
fun PluginAware.apply(pluginDependency: PluginDependency) = apply(plugin = pluginDependency.pluginId)
fun PluginAware.apply(pluginDependency: Provider<PluginDependency>) = apply(plugin = pluginDependency.get().pluginId)
fun PluginManager.withPlugin(pluginDep: PluginDependency, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDep.pluginId, block)
fun PluginManager.withPlugin(pluginDepProvider: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDepProvider.get().pluginId, block)
fun PluginManager.withPlugins(vararg pluginDeps: PluginDependency, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
fun PluginManager.withPlugins(vararg pluginDeps: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
inline fun <T> Iterable<T>.withEach(action: T.() -> Unit) = forEach { it.action() }

allprojects {
    pluginManager.withPlugin(versions.plugins.kotlinx.atomicfu) {
        configure<AtomicFUPluginExtension> {
            transformJvm = true
            jvmVariant = "VH"
        }
    }
}

stal {
    action {
        "kotlin jvm target" {
            pluginManager.withPlugin(versions.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    jvmToolchain {
                        languageVersion = JavaLanguageVersion.of(project.extra["jvmTargetVersion"] as String)
                        vendor = JvmVendorSpec.matching(project.extra["jvmVendor"] as String)
                    }
                    jvm()
                }
            }
        }
        "kotlin wasm-js target" {
            pluginManager.withPlugin(versions.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    @OptIn(ExperimentalWasmDsl::class)
                    wasmJs {
                        outputModuleName = project.path.substring(startIndex = 1).replace(':', '-')
                        browser()
                    }
                }
            }
        }
//        "kotlin android target" {
//            pluginManager.withPlugin(versions.plugins.kotlin.multiplatform) {
//                configure<KotlinMultiplatformExtension> {
//                    android()
//                }
//            }
//        }
        "kotlin" {
            apply(versions.plugins.kotlin.multiplatform)
            configure<KotlinMultiplatformExtension> {
                compilerOptions {
                    freeCompilerArgs = freeCompilerArgs.get() + listOf(
                        "-Xklib-duplicated-unique-name-strategy=allow-all-with-warning",
                        "-Xexpect-actual-classes",
                        "-Xconsistent-data-class-copy-visibility",
                    )
                }
                sourceSets {
                    all {
                        languageSettings {
                            progressiveMode = true
                            enableLanguageFeature("ContextParameters")
                            enableLanguageFeature("NestedTypeAliases")
                            enableLanguageFeature("ValueClasses")
                            enableLanguageFeature("ContractSyntaxV2")
                            enableLanguageFeature("ExplicitBackingFields")
                            optIn("kotlin.contracts.ExperimentalContracts")
                            optIn("kotlin.ExperimentalStdlibApi")
                            optIn("kotlin.uuid.ExperimentalUuidApi")
                            optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
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
                    testRuns.all {
                        executionTask {
                            useJUnitPlatform()
                        }
                    }
                }
                yarn.lockFileDirectory = rootDir.resolve("gradle")
            }
        }
        "library" {
            pluginManager.withPlugin(versions.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    explicitApi = ExplicitApiMode.Warning
                }
            }
        }
    }
}