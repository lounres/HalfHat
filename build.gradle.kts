@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import kotlinx.atomicfu.plugin.gradle.AtomicFUPluginExtension
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.accessors.dm.RootProjectAccessor
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.yarn.yarn
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget


plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlinx.atomicfu) apply false
}

val jvmTargetVersion: String by project

val Project.libs: LibrariesForLibs get() = rootProject.extensions.getByName<LibrariesForLibs>("libs")
val Project.projects: RootProjectAccessor get() = rootProject.extensions.getByName<RootProjectAccessor>("projects")
fun PluginAware.apply(pluginDependency: PluginDependency) = apply(plugin = pluginDependency.pluginId)
fun PluginAware.apply(pluginDependency: Provider<PluginDependency>) = apply(plugin = pluginDependency.get().pluginId)
fun PluginManager.withPlugin(pluginDep: PluginDependency, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDep.pluginId, block)
fun PluginManager.withPlugin(pluginDepProvider: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDepProvider.get().pluginId, block)
fun PluginManager.withPlugins(vararg pluginDeps: PluginDependency, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
fun PluginManager.withPlugins(vararg pluginDeps: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
inline fun <T> Iterable<T>.withEach(action: T.() -> Unit) = forEach { it.action() }

allprojects {
    pluginManager.withPlugin(rootProject.libs.plugins.kotlinx.atomicfu) {
        configure<AtomicFUPluginExtension> {
            transformJvm = true
            jvmVariant = "VH"
            transformJs = true
        }
    }
}

stal {
    action {
        "kotlin jvm target" {
            pluginManager.withPlugin(rootProject.libs.plugins.kotlin.multiplatform) {
                configure<KotlinMultiplatformExtension> {
                    jvmToolchain(jvmTargetVersion.toInt())
                    jvm()
                }
            }
        }
        "kotlin wasm-js target" {
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
//        "kotlin android target" {
//            pluginManager.withPlugin(rootProject.libs.plugins.kotlin.multiplatform) {
//                configure<KotlinMultiplatformExtension> {
//                    android()
//                }
//            }
//        }
        "kotlin" {
            apply(libs.plugins.kotlin.multiplatform)
            configure<KotlinMultiplatformExtension> {
                // TODO: Добавить JVM toolchain
                compilerOptions {
                    freeCompilerArgs = freeCompilerArgs.get() + listOf(
                        "-Xlambdas=indy",
                        "-Xexpect-actual-classes",
                        "-Xconsistent-data-class-copy-visibility",
                    )
                }
                sourceSets {
                    all {
                        languageSettings {
                            progressiveMode = true
                            enableLanguageFeature("ContextReceivers")
                            enableLanguageFeature("ValueClasses")
                            enableLanguageFeature("ContractSyntaxV2")
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
                    @OptIn(ExperimentalKotlinGradlePluginApi::class)
                    compilerOptions {
                        jvmTarget = JvmTarget.fromTarget(jvmTargetVersion)
                        freeCompilerArgs = freeCompilerArgs.get() + listOf("-Xlambdas=indy")
                    }
                    testRuns.all {
                        executionTask {
                            useJUnitPlatform()
                        }
                    }
                }
                yarn.lockFileDirectory = rootDir.resolve("gradle")
            }
            pluginManager.withPlugin("org.gradle.java") {
                configure<JavaPluginExtension> {
                    sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion)
                    targetCompatibility = JavaVersion.toVersion(jvmTargetVersion)
                }
                tasks.withType<Test> {
                    useJUnitPlatform()
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