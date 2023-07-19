rootProject.name = "TheTrueHat"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://repo.kotlin.link")
    }
}

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenLocal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("com.lounres.gradle.stal") version "0.1.0"
}

stal {
    structure {
        "api"("library", "server", "desktop", "web", "android")
        "server"("server")
        "client" {
            "common"("library", "desktop", "web", "android")
//            "web"("web")
            "desktop"("desktop")
//            "android"("android")
        }
        "feedback"("kotlin")
    }
    tag {
        "kotlin" since { hasAnyOf("server", "desktop", "web", "android", "library") }
    }
}
