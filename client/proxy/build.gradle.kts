import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

kotlin {
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass = "dev.lounres.halfhat.client.proxy.MainKt"
            }.configure {
                setArgs(
                    listOf(
                        rootProject.extra["halfhat.client.web.devServer.proxyPrefix"] as String,
                        rootProject.extra["halfhat.client.web.devServer.proxyPort"] as String,
                        rootProject.extra["halfhat.client.web.devServer.webpackPort"] as String,
                    )
                )
            }
        }
    }
    sourceSets {
        jvmMain {
            dependencies {
                implementation(versions.ktor.server.core)
                implementation(versions.ktor.server.netty)
                implementation(versions.ktor.client.core)
                implementation(versions.ktor.client.cio)
            }
        }
    }
}