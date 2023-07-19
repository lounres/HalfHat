plugins {
//    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // API
                api(projects.api)

                // Serialization
                api(libs.kotlinx.serialization.core)

//                // Compose
                api(compose.runtime)
                api(compose.ui)
                api(compose.foundation)
                api(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.components.resources)

                // Ktor
                api(libs.ktor.client.core)
                api(libs.ktor.client.websockets)
                api(libs.ktor.serialization.kotlinx.json)
                api(libs.ktor.serialization.kotlinx.protobuf)
            }
        }
//        val androidMain by getting {
//            dependencies {
//                api("androidx.activity:activity-compose:1.6.1")
//                api("androidx.appcompat:appcompat:1.6.1")
//                api("androidx.core:core-ktx:1.9.0")
//            }
//        }
        desktopMain {
            dependencies {
                implementation(compose.desktop.common)
            }
        }
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