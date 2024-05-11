import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.accessors.dm.RootProjectAccessor
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
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

val Project.libs: LibrariesForLibs get() = rootProject.extensions.getByName<LibrariesForLibs>("libs")
val Project.projects: RootProjectAccessor get() = rootProject.extensions.getByName<RootProjectAccessor>("projects")
fun PluginAware.apply(pluginDependency: PluginDependency) = apply(plugin = pluginDependency.pluginId)
fun PluginAware.apply(pluginDependency: Provider<PluginDependency>) = apply(plugin = pluginDependency.get().pluginId)
fun PluginManager.withPlugin(pluginDep: PluginDependency, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDep.pluginId, block)
fun PluginManager.withPlugin(pluginDepProvider: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = withPlugin(pluginDepProvider.get().pluginId, block)
fun PluginManager.withPlugins(vararg pluginDeps: PluginDependency, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
fun PluginManager.withPlugins(vararg pluginDeps: Provider<PluginDependency>, block: AppliedPlugin.() -> Unit) = pluginDeps.forEach { withPlugin(it, block) }
inline fun <T> Iterable<T>.withEach(action: T.() -> Unit) = forEach { it.action() }

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
            apply(libs.plugins.kotlin.multiplatform)
            configure<KotlinMultiplatformExtension> {
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