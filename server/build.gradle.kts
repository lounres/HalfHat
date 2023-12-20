plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm { withJava() }
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.api)

                // kotlinx.datetime
                implementation(libs.kotlinx.datetime)

                // Logging
                implementation(libs.kotlin.logging)

                // Ktor
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.ktor.server.websockets)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.serialization.kotlinx.protobuf)
                runtimeOnly(libs.logback.classic)
            }
        }
    }
}

application {
    mainClass = "dev.lounres.thetruehat.server.MainKt"
}