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
                api(versions.kone.collections)
                api(versions.kone.state)
                api(versions.kone.typeSafeRegistry)
                
                // Component blocks
                api(versions.komponentual.lifecycle)
                api(versions.komponentual.navigation)
                
                // Logging
                api(versions.logKube.core)
                
                // Coroutines
                api(versions.kotlinx.coroutines.core)
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