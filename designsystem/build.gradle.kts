plugins {
    alias(libs.plugins.sweeper.internal.compose.module)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
        }
    }
}