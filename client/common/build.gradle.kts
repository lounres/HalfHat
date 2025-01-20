plugins {
//    alias(versions.plugins.android.library)
    alias(versions.plugins.kotlin.compose)
    alias(versions.plugins.compose.multiplatform)
    alias(versions.plugins.kotlinx.serialization)
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
                api(versions.logKube.core)

                // Compose
                api(compose.runtime)
                api(compose.ui)
                api(compose.foundation)
                api(compose.material3)
                api(compose.components.resources)
                
                // Decompose & Essenty
                api(versions.decompose)
                api(versions.decompose.extensions.compose.multiplatform)
                api(versions.essenty.lifecycle.coroutines)

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
                implementation(compose.desktop.common)
                implementation(versions.ktor.client.cio)
            }
        }

        wasmJsMain {
            dependencies {
                implementation(versions.ktor.client.js)
            }
        }
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