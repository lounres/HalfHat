rootProject.name = "TheTrueHat"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
//        mavenLocal()
    }
}

plugins {
    id("dev.lounres.gradle.stal") version "0.3.1"
}

stal {
    structure {
        "api"("library", "server", "desktop", "web", "android")
        "server"("server")
        "client" {
            "common"("library", "desktop", "web", "android")
            "web"("web")
            "desktop"("desktop")
//            "android"("android")
        }
        "feedback"("kotlin")
    }
    tag {
        "kotlin" since { hasAnyOf("server", "desktop", "web", "android", "library") }
    }
}
