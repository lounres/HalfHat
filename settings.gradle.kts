rootProject.name = "HalfHat"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

val projectProperties = java.util.Properties()
file("gradle.properties").inputStream().use {
    projectProperties.load(it)
}

val versions: String by projectProperties

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        mavenLocal()
    }
    
    versionCatalogs {
        create("versions").from("dev.lounres:versions:$versions")
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

plugins {
    id("dev.lounres.gradle.stal") version "0.4.0"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

stal {
    structure {
        "api"("library", "server", "desktop", "web", "android")
        "server"("server")
        "client" {
            "components"("library", "desktop", "web", /*"android"*/)
            "common"("library", "desktop", "web", /*"android"*/)
            "desktop"("desktop")
            "web"("web")
//            "android"("android")
        }
    }
    tag {
        "kotlin" since { hasAnyOf("server", "desktop", "web", "android", "library") }
        "kotlin jvm target" since { hasAnyOf("server", "desktop") }
        "kotlin wasm-js target" since { hasAnyOf("web") }
        "kotlin android target" since { hasAnyOf("android") }
    }
    action {
        gradle.allprojects {
            extra["jvmTargetVersion"] = settings.extra["jvmTargetVersion"]
            extra["jvmVendor"] = settings.extra["jvmVendor"]
        }
    }
}
