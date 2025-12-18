plugins {
//    alias(versions.plugins.android.library)
    alias(versions.plugins.kotlin.compose)
    alias(versions.plugins.compose.multiplatform)
    alias(versions.plugins.kotlinx.atomicfu)
    alias(versions.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(versions.kone.util.misc)
                
                // API
                api(projects.api)
                
                // Components
                api(projects.client.components)
                api(versions.kone.util.stateForCompose)
                implementation(versions.kone.util.atomics)

                // Compose
                api(versions.compose.multiplatform.runtime)
                api(versions.compose.multiplatform.ui)
                api(versions.compose.multiplatform.foundation)
                api(versions.compose.multiplatform.material3)
                api(versions.compose.multiplatform.material3.windowSizeClass)
                api(versions.compose.multiplatform.components.resources)

                // Ktor
                api(versions.ktor.client.core)
                api(versions.ktor.client.websockets)
                api(versions.ktor.serialization.kotlinx.json)
                api(versions.ktor.serialization.kotlinx.protobuf)

                // Koin
//                api(versions.koin.core)
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
                implementation(versions.compose.multiplatform.desktop.common)
                implementation(versions.ktor.client.cio)
                
                // mp3spi
                runtimeOnly("com.googlecode.soundlibs:tritonus-share:0.3.7-2")
                runtimeOnly("com.googlecode.soundlibs:mp3spi:1.9.5-1")
            }
        }
        
        webMain {
            dependencies {
                implementation(versions.ktor.client.js)
                implementation(versions.kotlin.wrappers.browser)
            }
        }
    }
}

compose {
    resources {
        packageOfResClass = "dev.lounres.halfhat.client.common.resources"
        generateResClass = always
        publicResClass = true
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