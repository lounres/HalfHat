plugins {
    alias(versions.plugins.kotlinx.serialization)
    alias(versions.plugins.kotlinx.atomicfu)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                // Kone
                api(versions.kone.algebraic)
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