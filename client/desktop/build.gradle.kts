import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
//    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlinx.atomicfu)
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

compose {
    resources {
        packageOfResClass = "dev.lounres.halfhat.client.desktop.resources"
        generateResClass = always
        publicResClass = true
    }
    desktop {
        application {
            mainClass = "dev.lounres.halfhat.client.desktop.MainKt"
            
            buildTypes.release.proguard {
//            obfuscate = true
            }

            nativeDistributions {
                packageName = "HalfHat"
                packageVersion = version as String
                description = "HalfHat is a HalfHat" // TODO: Add description
                copyright = "© 2023 Gleb Minaev. All rights reserved."
                vendor = "Gleb Minaev"
//                licenseFile = rootProject.file("LICENSE")
                
                
                targetFormats(
                    // Windows
                    TargetFormat.Exe,
                    TargetFormat.Msi,
                    // Linux
//                TargetFormat.Deb,
//                TargetFormat.Rpm,
                    // maxOS
//                TargetFormat.Dmg,
//                TargetFormat.Pkg
                )
                
                windows {
//                    iconFile = project.file("src/jvmMain/resources/MCCME-logo3.ico")
//                    console = true
//                    perUserInstall = true
//                    upgradeUuid = ""
                }
                
                linux {
//                    iconFile = project.file("src/jvmMain/resources/MCCME-logo3.png")
//                    rpmLicenseType = ""
                }
                
                macOS {
//                    iconFile = project.file("")
                }
            }
        }
    }
}