plugins {
    alias(libs.plugins.ktor)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.netty)
                implementation(libs.kord.core)
                runtimeOnly(libs.logback.classic)
            }
        }
    }
}

application {
    mainClass = "site.m20sch57.thetruehat.feedback.MainKt"
}

jib {
    container {
        mainClass = "site.m20sch57.thetruehat.feedback.MainKt"
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