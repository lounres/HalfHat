plugins {
    alias(versions.plugins.kotlinx.serialization)
    alias(versions.plugins.kotlinx.atomicfu)
    alias(versions.plugins.ktor)
}

kotlin {
    jvm { withJava() }
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.api)

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

application {
    mainClass = "dev.lounres.halfhat.server.MainKt"
}