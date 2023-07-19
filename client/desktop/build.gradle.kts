import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.compose)
//    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvm("desktop") { withJava() }
    sourceSets {
        desktopMain {
            dependencies {
                implementation(projects.client.common)
                implementation(libs.ktor.client.cio)
                runtimeOnly(libs.logback.classic)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

//application {
//    mainClass = "dev.lounres.thetruehat.client.desktop.MainKt"
//}

compose.desktop {
    application {
        mainClass = "dev.lounres.thetruehat.client.desktop.MainKt"

        nativeDistributions {
            targetFormats(/*TargetFormat.Dmg,*/ TargetFormat.Msi, TargetFormat.Deb, )
            packageName = "KotlinMultiplatformComposeDesktopApplication"
            packageVersion = version as String
        }
    }
}