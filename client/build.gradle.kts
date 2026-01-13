import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
//    alias(versions.plugins.android.library)
    alias(versions.plugins.kotlin.compose)
    alias(versions.plugins.compose.multiplatform)
    alias(versions.plugins.kotlinx.atomicfu)
    alias(versions.plugins.kotlinx.serialization)
//    alias(libs.plugins.sqldelight)
}

val webpackPort = (rootProject.extra["halfhat.client.web.devServer.webpackPort"] as String).toInt()

kotlin {
    js {
        outputModuleName = "client"
        browser {
            commonWebpackConfig {
                outputFileName = "HalfHat.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    open = false
                    
                    port = webpackPort
                    
                    // Serve sources to debug inside browser
                    static(rootDir.path + "/api")
                    static(rootDir.path + "/client/components")
                    static(rootDir.path + "/client")
                }
            }
        }
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "client"
        compilerOptions {
            freeCompilerArgs.add("-Xwasm-debugger-custom-formatters")
        }
        browser {
            commonWebpackConfig {
                outputFileName = "HalfHat.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    open = false
                    
                    port = webpackPort
                    
                    // Serve sources to debug inside browser
                    static(rootDir.path + "/api")
                    static(rootDir.path + "/client/components")
                    static(rootDir.path + "/client")
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val buildDirectory = project.layout.buildDirectory.get().asFile!!
        val constsDirectory = buildDirectory.resolve("generated/halfhat/client/consts")
        val resourcesDirectory = buildDirectory.resolve("generated/halfhat/client/resources")
        
        commonMain {
            val constsSrcDirectory = constsDirectory.resolve("commonMain")
            kotlin.srcDir(constsSrcDirectory)
            dependencies {
                implementation(versions.kone.util.misc)
                
                // API
                api(projects.api)
                
                // Components
                api(projects.client.components)
                api(versions.kone.util.stateForCompose)
                implementation(versions.kone.util.atomics)

                // Compose
                api(versions.compose.multiplatform.runtime)
                api(versions.compose.multiplatform.ui)
                api(versions.compose.multiplatform.foundation)
                api(versions.compose.multiplatform.material3)
                api(versions.compose.multiplatform.material3.adaptive)
                api(versions.compose.multiplatform.components.resources)

                // Ktor
                api(versions.ktor.client.core)
                api(versions.ktor.client.websockets)
                api(versions.ktor.serialization.kotlinx.json)
                api(versions.ktor.serialization.kotlinx.protobuf)

                // Koin
//                api(versions.koin.core)
                
                // Utils
                implementation("net.thauvin.erik.urlencoder:urlencoder-lib:1.6.0")
            }
        }

        jvmMain {
            dependencies {
                // mp3spi
                runtimeOnly("com.googlecode.soundlibs:tritonus-share:0.3.7-2")
                runtimeOnly("com.googlecode.soundlibs:mp3spi:1.9.5-1")
                
                // Kone
                implementation(versions.kone.maybe)
                implementation(versions.kone.util.misc)
                
                // AppDirs
                implementation(libs.appDirs)
                
                // Compose
                implementation(versions.compose.multiplatform.desktop.common)
                implementation(compose.desktop.currentOs)
                
                // Ktor
                implementation(versions.ktor.client.cio)
                runtimeOnly(versions.logback.classic)
                
                // Serialization
                implementation(versions.kotlinx.serialization.json)
                
                // SQLDelight
                implementation(libs.sqldelight.driver.sqlite.jvm)
                implementation(libs.sqldelight.async.extensions)
                implementation(libs.sqldelight.coroutines.extensions)
            }
        }
        
        webMain {
            val constsSrcDirectory = constsDirectory.resolve("webMain")
            kotlin.srcDir(constsSrcDirectory)
            dependencies {
                implementation(versions.ktor.client.js)
                implementation(versions.kotlin.wrappers.browser)
            }
        }
        
        jsMain {
            val constsSrcDirectory = constsDirectory.resolve("jsMain")
            kotlin.srcDir(constsSrcDirectory)
            val resourcesSrcDirectory = resourcesDirectory.resolve("jsMain")
            resources.srcDir(resourcesSrcDirectory)
        }
        
        wasmJsMain {
            val constsSrcDirectory = constsDirectory.resolve("wasmJsMain")
            kotlin.srcDir(constsSrcDirectory)
            val resourcesSrcDirectory = resourcesDirectory.resolve("wasmJsMain")
            resources.srcDir(resourcesSrcDirectory)
        }

//        val androidMain by getting {
//            dependencies {
//                api("androidx.activity:activity-compose:1.6.1")
//                api("androidx.appcompat:appcompat:1.6.1")
//                api("androidx.core:core-ktx:1.9.0")
//            }
//        }
    }
}

//sqldelight {
//    databases {
//        create("AppDatabase") {
//            packageName = "dev.lounres.halfhat.client.localStorage.sql"
//        }
//    }
//}

composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}

compose {
    resources {
        packageOfResClass = "dev.lounres.halfhat.client.resources"
        generateResClass = always
        publicResClass = true
    }
    desktop {
        application {
            mainClass = "dev.lounres.halfhat.client.MainKt"
            
            buildTypes.release.proguard {
//            obfuscate = true
            }
            
            nativeDistributions {
                packageName = "HalfHat"
                packageVersion = version as String
                description = "HalfHat is a HalfHat" // TODO: Add description
                copyright = "© 2025 Gleb Minaev. All rights reserved."
                vendor = "Gleb Minaev"
//                licenseFile = rootProject.file("LICENSE")
                
                
                targetFormats(
                    // Windows
                    TargetFormat.Exe,
                    TargetFormat.Msi,
                    // Linux
//                TargetFormat.Deb,
//                TargetFormat.Rpm,
                    // maxOS
//                TargetFormat.Dmg,
//                TargetFormat.Pkg
                )
                
                windows {
//                    iconFile = project.file("src/jvmMain/resources/MCCME-logo3.ico")
//                    console = true
//                    perUserInstall = true
//                    upgradeUuid = ""
                }
                
                linux {
//                    iconFile = project.file("src/jvmMain/resources/MCCME-logo3.png")
//                    rpmLicenseType = ""
                }
                
                macOS {
//                    iconFile = project.file("")
                }
            }
        }
    }
}

//android {
//    compileSdk = (findProperty("android.compileSdk") as String).toInt()
//    namespace = "com.myapplication.common"
//
//    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
//    sourceSets["main"].res.srcDirs("src/androidMain/res")
//    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
//
//    defaultConfig {
//        minSdk = (findProperty("android.minSdk") as String).toInt()
//        targetSdk = (findProperty("android.targetSdk") as String).toInt()
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlin {
//        jvmToolchain(11)
//    }
//}

fun ExtraPropertiesExtension.getOrNull(name: String): Any? = if (has(name)) get(name) else null

val generateClientConsts by tasks.registering {
    group = "build"
    description = "Generates either dev or prod consts for the client"
    
    doLast {
        val debug = when (val debugFlag = rootProject.extra.getOrNull("halfhat.client.debug")) {
            "true" -> true
            "false", null -> false
            else -> error("Undefined value for 'halfhat.client.debug' project property: $debugFlag")
        }
        
        val local = when (val debugFlag = rootProject.extra.getOrNull("halfhat.client.local")) {
            "true" -> true
            "false", null -> false
            else -> error("Undefined value for 'halfhat.client.local' project property: $debugFlag")
        }
        
        val buildDirectory = project.layout.buildDirectory.get().asFile!!
        val constsDirectory = buildDirectory.resolve("generated/halfhat/client/consts").apply { mkdirs() }
        val resourcesDirectory = buildDirectory.resolve("generated/halfhat/client/resources").apply { mkdirs() }
        run {
            val constsSrcDirectory = constsDirectory.resolve("commonMain").apply { mkdirs() }
            val constsSrcPackageDirectory = constsSrcDirectory.resolve("dev/lounres/halfhat/client/consts").apply { mkdirs() }
            constsSrcPackageDirectory.resolve("OnlineGameSettings.kt").bufferedWriter().use {
                it.append(
                    """
                    package dev.lounres.halfhat.client.consts
                    
                    
                    @Suppress("RedundantNullableReturnType")
                    data object OnlineGameSettings {
                        val host: String? = ${if (local) rootProject.extra["halfhat.client.consts.dev.onlineGame.host"] else rootProject.extra["halfhat.client.consts.prod.onlineGame.host"]}
                        val port: Int? = ${if (local) rootProject.extra["halfhat.client.consts.dev.onlineGame.port"] else rootProject.extra["halfhat.client.consts.prod.onlineGame.port"]}
                        val path: String? = ${if (local) rootProject.extra["halfhat.client.consts.dev.onlineGame.path"] else rootProject.extra["halfhat.client.consts.prod.onlineGame.path"]}
                        val isSecure: Boolean = ${if (local) rootProject.extra["halfhat.client.consts.dev.onlineGame.isSecure"] else rootProject.extra["halfhat.client.consts.prod.onlineGame.isSecure"]}
                        val linkBase: String = ${if (debug) rootProject.extra["halfhat.client.consts.dev.onlineGame.linkBase"] else rootProject.extra["halfhat.client.consts.prod.onlineGame.linkBase"]}
                    }
                    """.trimIndent()
                )
            }
        }
        run {
            val constsSrcDirectory = constsDirectory.resolve("webMain").apply { mkdirs() }
            val constsSrcPackageDirectory = constsSrcDirectory.resolve("dev/lounres/halfhat/client/consts").apply { mkdirs() }
            constsSrcPackageDirectory.resolve("WebPageSettings.kt").bufferedWriter().use {
                it.append(
                    """
                    package dev.lounres.halfhat.client.consts
                    
                    
                    @Suppress("RedundantNullableReturnType")
                    expect object WebPageSettings {
                        val base: String
                    }
                    """.trimIndent()
                )
            }
        }
        run {
            val base = if (debug) rootProject.extra["halfhat.client.consts.dev.webPage.base.js"] else rootProject.extra["halfhat.client.consts.prod.webPage.base.js"]
            
            val constsSrcDirectory = constsDirectory.resolve("jsMain").apply { mkdirs() }
            val constsSrcPackageDirectory = constsSrcDirectory.resolve("dev/lounres/halfhat/client/consts").apply { mkdirs() }
            constsSrcPackageDirectory.resolve("WebPageSettings.kt").bufferedWriter().use {
                it.append(
                    """
                    package dev.lounres.halfhat.client.consts
                    
                    
                    @Suppress("RedundantNullableReturnType")
                    actual data object WebPageSettings {
                        actual val base: String = "$base"
                    }
                    """.trimIndent()
                )
            }
            val resourcesSrcDirectory = resourcesDirectory.resolve("jsMain").apply { mkdirs() }
            resourcesSrcDirectory.resolve("index.html").bufferedWriter().use {
                it.append(
                    """
                        <!DOCTYPE html>
                        <html lang="en">
                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">

                                <title>HalfHat</title>

                                <style>
                                    html, body {
                                        width: 100%;
                                        height: 100%;
                                        margin: 0;
                                        padding: 0;
                                        overflow: hidden;
                                    }
                                </style>
                                <script src="skiko.js"> </script>
                            </head>
                            <body></body>
                            <script type="application/javascript" src="${base}HalfHat.js"></script>
                        </html>
                    """.trimIndent()
                )
            }
        }
        run {
            val base = if (debug) rootProject.extra["halfhat.client.consts.dev.webPage.base.wasm"] else rootProject.extra["halfhat.client.consts.prod.webPage.base.wasm"]
            
            val constsSrcDirectory = constsDirectory.resolve("wasmJsMain").apply { mkdirs() }
            val constsSrcPackageDirectory = constsSrcDirectory.resolve("dev/lounres/halfhat/client/consts").apply { mkdirs() }
            constsSrcPackageDirectory.resolve("WebPageSettings.kt").bufferedWriter().use {
                it.append(
                    """
                    package dev.lounres.halfhat.client.consts
                    
                    
                    @Suppress("RedundantNullableReturnType")
                    actual data object WebPageSettings {
                        actual val base: String = "$base"
                    }
                    """.trimIndent()
                )
            }
            val resourcesSrcDirectory = resourcesDirectory.resolve("wasmJsMain").apply { mkdirs() }
            resourcesSrcDirectory.resolve("index.html").bufferedWriter().use {
                it.append(
                    """
                        <!DOCTYPE html>
                        <html lang="en">
                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">

                                <title>HalfHat</title>

                                <style>
                                    html, body {
                                        width: 100%;
                                        height: 100%;
                                        margin: 0;
                                        padding: 0;
                                        overflow: hidden;
                                    }
                                </style>
                            </head>
                            <body></body>
                            <script type="application/javascript" src="${base}HalfHat.js"></script>
                        </html>
                    """.trimIndent()
                )
            }
        }
    }
}

tasks.withType<KotlinCompilationTask<*>> {
    dependsOn(generateClientConsts)
}

listOf(
    tasks.jsAggregateResources,
    tasks.wasmJsProcessResources,
).forEach {
    it.configure {
        dependsOn(generateClientConsts)
    }
}

tasks.register("publishToProduction") {
    group = "publishing"
    description = "Publish the web application to production server"
    
    val jsBrowserDistribution by tasks
    val wasmJsBrowserDistribution by tasks
    
    dependsOn(jsBrowserDistribution, wasmJsBrowserDistribution)
    
    doLast {
        val hostname = project.properties["halfhat.publishing.hostname"] as String
        val port = (project.properties["halfhat.publishing.ssh.port"] as String).toInt()
        val username = project.properties["halfhat.publishing.ssh.username"] as String
        val password = project.properties["halfhat.publishing.ssh.password"] as String
        val destinationSite = project.properties["halfhat.publishing.destination.site"] as String
        
        val ssh = SSHClient()
        ssh.addHostKeyVerifier(PromiscuousVerifier())
        ssh.use {
            ssh.connect(hostname, port)
            ssh.authPassword(username, password)
            ssh.use {
                run {
                    val session = ssh.startSession()
                    val command = session.exec("rm -rf $destinationSite/js/* $destinationSite/wasm/*")
                    command.join()
                }
                
                val scpFileTransfer = ssh.newSCPFileTransfer()
                
                run {
                    val sources = jsBrowserDistribution.outputs.files
                    val directory = sources.singleFile
                    directory.listFiles()!!.forEach { file ->
                        scpFileTransfer.upload(file.absolutePath, "$destinationSite/js")
                    }
                }
                
                run {
                    val sources = wasmJsBrowserDistribution.outputs.files
                    val directory = sources.singleFile
                    directory.listFiles()!!.forEach { file ->
                        scpFileTransfer.upload(file.absolutePath, "$destinationSite/wasm")
                    }
                }
            }
        }
    }
}