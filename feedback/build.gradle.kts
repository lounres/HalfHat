plugins {
    alias(libs.plugins.ktor)
}

kotlin {
    jvm()
    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.kord.core)
                runtimeOnly(libs.logback.classic)
            }
        }
    }
}

ktor {
    docker {
        localImageName.set("thetruehat-feedback")

        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                7005,
                8080,
                io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))
    }
}