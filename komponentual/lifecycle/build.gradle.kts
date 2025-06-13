plugins {
//    alias(versions.plugins.android.library)
    alias(versions.plugins.kotlinx.atomicfu)
//    alias(versions.plugins.kotlinx.serialization)
}

atomicfu {
    transformJvm = true
    jvmVariant = "VH"
}

kotlin {
//    js {
//        browser()
//        nodejs()
//    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.kone.automata)
                implementation(projects.kone.atomicFUAtomics)
                api(versions.kone.typeSafeRegistry)
                api(versions.kone.collections)
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