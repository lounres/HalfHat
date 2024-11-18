rootProject.name = "HalfHat"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val projectProperties = java.util.Properties()
file("gradle.properties").inputStream().use {
    projectProperties.load(it)
}

val koneVersion : String by projectProperties

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
    
    versionCatalogs {
        create("kone").from("dev.lounres:kone.versionCatalog:$koneVersion")
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

stal {
    structure {
        "api"("library", "server", "desktop", "web", "android")
        "server"("server")
        "client" {
            "common"("library", "desktop", /*"web",*/ /*"android"*/)
//            "web"("web")
            "desktop"("desktop")
//            "android"("android")
        }
    }
    tag {
        "kotlin" since { hasAnyOf("server", "desktop", "web", "android", "library") }
        "kotlin jvm target" since { hasAnyOf("server", "desktop") }
        "kotlin wasm-js target" since { hasAnyOf("web") }
        "kotlin android target" since { hasAnyOf("android") }
    }
}
