import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(versions.plugins.kotlin.compose)
    alias(versions.plugins.compose.multiplatform)
//    alias(versions.plugins.ktor)
//    alias(versions.plugins.kotlinx.serialization)
}

kotlin {
    js {
        outputModuleName = "client-web"
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
                        add(rootDir.path + "/client/components")
                        add(rootDir.path + "/client/common")
                        add(rootDir.path + "/client/web/")
                    }
                }
            }
        }
        binaries.executable()
    }
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "client-web"
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
                        add(rootDir.path + "/client/components")
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
        web {
            dependencies {
                implementation(versions.kotlin.wrappers.browser)
            }
        }
        jsMain {
            dependencies {
                implementation(kotlin("stdlib"))
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

tasks.register("publishToProduction") {
    group = "publishing"
    description = "Publish the web application to production server"

    val wasmJsBrowserDistribution by tasks

    dependsOn(wasmJsBrowserDistribution)

    doLast {
        val hostname = project.properties["halfhat.publishing.hostname"] as String
        val username = project.properties["halfhat.publishing.ssh.username"] as String
        val password = project.properties["halfhat.publishing.ssh.password"] as String
        val destinationSite = project.properties["halfhat.publishing.destination.site"] as String
        
        val ssh = SSHClient()
        ssh.addHostKeyVerifier(PromiscuousVerifier())
        ssh.use {
            ssh.connect(hostname)
            ssh.authPassword(username, password)
            ssh.use {
                val session = ssh.startSession()
                val command = session.exec("rm -rf $destinationSite/*")
                println(command.inputStream.bufferedReader().use { it.readText() })
                command.join()
                val scpFileTransfer = ssh.newSCPFileTransfer()
                val sources = wasmJsBrowserDistribution.outputs.files
                val directory = sources.singleFile
                directory.listFiles()!!.forEach { file ->
                    scpFileTransfer.upload(file.absolutePath, destinationSite)
                }
            }
        }
    }
}