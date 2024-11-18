plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlinx.atomicfu)
}

kotlin {
    jvm { withJava() }
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.api)

                // Logging
                implementation(libs.logkube.core)

                // Ktor
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.websockets)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.serialization.kotlinx.protobuf)
//                runtimeOnly(libs.logback.classic)
            }
        }
    }
}

application {
    mainClass = "dev.lounres.halfhat.server.MainKt"
}