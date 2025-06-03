plugins {
//    alias(versions.plugins.android.library)
//    alias(versions.plugins.kotlinx.serialization)
}

kotlin {
//    js {
//        browser()
//        nodejs()
//    }

    sourceSets {
        commonMain {
            dependencies {
                api(versions.kone.maybe)
            }
        }

//        val androidMain by getting {
//            dependencies {
//                api("androidx.activity:activity-compose:1.6.1")
//                api("androidx.appcompat:appcompat:1.6.1")
//                api("androidx.core:core-ktx:1.9.0")
//            }
//        }
    }
}