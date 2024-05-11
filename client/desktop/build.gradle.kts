import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    java
    alias(libs.plugins.compose)
    alias(libs.plugins.jetbrains.compose)
//    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm { withJava() }
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.client.common)

                // Compose
                implementation(compose.desktop.currentOs)
                implementation(compose.components.resources)

                // Ktor
                implementation(libs.ktor.client.cio)
                runtimeOnly(libs.logback.classic)
            }
        }
    }
}

task<JavaExec>("runUi") {
    group = "run"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "dev.lounres.thetruehat.client.desktop.ui.MainKt"
}

compose {
    resources {
        packageOfResClass = "dev.lounres.thetruehat.client.desktop.resources"
    }
    desktop {
        application {
            mainClass = "dev.lounres.thetruehat.client.desktop.MainKt"

            nativeDistributions {
                targetFormats(/*TargetFormat.Dmg,*/ TargetFormat.Msi, TargetFormat.Deb)
                packageName = "KotlinMultiplatformComposeDesktopApplication"
                packageVersion = version as String
            }
        }
    }
}