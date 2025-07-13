plugins {
//    alias(versions.android.library)
    alias(versions.plugins.kotlinx.serialization)
    alias(versions.plugins.kotlinx.atomicfu)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // Kone
                api(versions.kone.collections)
                api(versions.kone.automata)
                implementation(versions.kone.util.misc)
                
                // Coroutines
                api(versions.kotlinx.coroutines.core)
                
                // Serialization
                api(versions.kotlinx.serialization.core)
                
                // Datetime
                api(versions.kotlinx.datetime)
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