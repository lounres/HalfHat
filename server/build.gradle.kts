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