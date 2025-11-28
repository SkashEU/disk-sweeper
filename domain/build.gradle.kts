plugins {
    alias(libs.plugins.sweeper.internal.kmp.module)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.forge.usecase)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}