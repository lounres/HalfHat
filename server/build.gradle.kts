import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(versions.plugins.kotlinx.serialization)
    alias(versions.plugins.kotlinx.atomicfu)
//    alias(versions.plugins.ktor)
    alias(versions.plugins.shadow)
}

val mainClassFQN = "dev.lounres.halfhat.server.MainKt"

kotlin {
    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass = mainClassFQN
            }
        }
    }
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.api)
                
                // Utils
                implementation(versions.kone.util.misc)

                // Logging
                implementation(versions.logKube.core)

                // Ktor
                implementation(versions.ktor.server.core)
                implementation(versions.ktor.server.netty)
                implementation(versions.ktor.server.websockets)
                implementation(versions.ktor.serialization.kotlinx.json)
                implementation(versions.ktor.serialization.kotlinx.protobuf)
//                runtimeOnly(versions.logback.classic)
            }
        }
    }
}

tasks.shadowJar {
    archiveBaseName = "halfhat-server"
    archiveClassifier = ""
    archiveVersion = ""
    manifest {
        attributes["Main-Class"] = mainClassFQN
    }
}