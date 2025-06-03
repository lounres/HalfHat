import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(versions.plugins.kotlin.compose)
    alias(versions.plugins.compose.multiplatform)
//    alias(versions.plugins.ktor)
    alias(versions.plugins.kotlinx.serialization)
//    alias(versions.plugins.kotlinx.atomicfu)
    alias(libs.plugins.sqldelight)
}

kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.client.common)
                
                // Kone
                implementation(versions.kone.maybe)
                implementation(versions.kone.util.misc)
                
                // AppDirs
                implementation(libs.appDirs)

                // Compose
                implementation(compose.desktop.currentOs)

                // Ktor
                implementation(versions.ktor.client.cio)
                runtimeOnly(versions.logback.classic)
                
                // SQLDelight
                implementation(libs.sqldelight.driver.sqlite.jvm)
                implementation(libs.sqldelight.async.extensions)
                implementation(libs.sqldelight.coroutines.extensions)
            }
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName = "dev.lounres.halfhat.client.localStorage.sql"
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