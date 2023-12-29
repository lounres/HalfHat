import java.time.LocalDateTime

plugins {
    alias(libs.plugins.ktor.old)
}

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        jvmMain {
            dependencies {
                implementation(libs.ktor.old.server.core)
                implementation(libs.ktor.old.server.netty)
                implementation(libs.kord.core)
                runtimeOnly(libs.logback.classic)
            }
        }
    }
}

application {
    mainClass = "dev.lounres.thetruehat.feedback.MainKt"
}

jib {
    container {
        mainClass = "dev.lounres.thetruehat.feedback.MainKt"
    }
}

ktor {
    docker {
        localImageName = "thetruehat-feedback"
        imageTag = LocalDateTime.now().toString().replace(':', '-')

        portMappings.set(listOf(
            io.ktor.plugin.features.DockerPortMapping(
                outsideDocker = 2000,
                insideDocker = 8080,
                protocol = io.ktor.plugin.features.DockerPortMappingProtocol.TCP
            )
        ))

//        environmentVariable("token", TODO())
//        environmentVariable("channelId", TODO())

//        externalRegistry = null // TODO
    }
}