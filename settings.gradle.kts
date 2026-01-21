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
        "api"("library", "server", "desktop", "web", "android library")
        "server"("server")
        "client"("desktop", "web", "android library") {
            "components"("library", "desktop", "web", "android library")
            "proxy"("kotlin jvm target")
//            "android"("android application")
        }
    }
    tag {
        "kotlin jvm target" since { hasAnyOf("server", "desktop") }
        "kotlin web targets" since { hasAnyOf("web") }
//        "kotlin android library target" since { hasAnyOf("android library") }
//        "kotlin android application target" since { hasAnyOf("android application") }
        "kotlin android target" since { hasAnyOf("kotlin android library target", "kotlin android application target") }
        "kotlin" since { hasAnyOf("kotlin jvm target", "kotlin web targets", "kotlin android library target") }
    }
    action {
        gradle.allprojects {
            extra["jvmTargetVersion"] = settings.extra["jvmTargetVersion"]
            extra["jvmVendor"] = settings.extra["jvmVendor"]
        }
    }
}
