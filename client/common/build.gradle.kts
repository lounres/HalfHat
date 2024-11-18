plugins {
//    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
//    js {
//        browser()
//        nodejs()
//    }

    sourceSets {
        commonMain {
            dependencies {
                // API
                api(projects.api)

                // Logging
                api(libs.logkube.core)

                // Compose
                api(compose.runtime)
                api(compose.ui)
                api(compose.foundation)
                api(compose.material3)
                api(compose.components.resources)
                
                // Decompose & Essenty
                api(libs.decompose)
                api(libs.decompose.extensions.compose.multiplatform)
                api(libs.essenty.lifecycle.coroutines)

                // Ktor
                api(libs.ktor.client.core)
                api(libs.ktor.client.websockets)
                api(libs.ktor.serialization.kotlinx.json)
                api(libs.ktor.serialization.kotlinx.protobuf)

                // Koin
//                api(libs.koin.core)
            }
        }

//        val androidMain by getting {
//            dependencies {
//                api("androidx.activity:activity-compose:1.6.1")
//                api("androidx.appcompat:appcompat:1.6.1")
//                api("androidx.core:core-ktx:1.9.0")
//            }
//        }

        jvmMain {
            dependencies {
                implementation(compose.desktop.common)
                implementation(libs.ktor.client.cio)
            }
        }

//        wasmJsMain {
//            dependencies {
//                implementation(libs.ktor.client.js)
//            }
//        }
    }
}

compose {
    resources {
        packageOfResClass = "dev.lounres.halfhat.client.common.resources"
        generateResClass = always
    }
}

//android {
//    compileSdk = (findProperty("android.compileSdk") as String).toInt()
//    namespace = "com.myapplication.common"
//
//    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
//    sourceSets["main"].res.srcDirs("src/androidMain/res")
//    sourceSets["main"].resources.srcDirs("src/commonMain/resources")
//
//    defaultConfig {
//        minSdk = (findProperty("android.minSdk") as String).toInt()
//        targetSdk = (findProperty("android.targetSdk") as String).toInt()
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_11
//        targetCompatibility = JavaVersion.VERSION_11
//    }
//    kotlin {
//        jvmToolchain(11)
//    }
//}