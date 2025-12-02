import gobley.gradle.GobleyHost
import gobley.gradle.cargo.dsl.jvm

plugins {
    alias(libs.plugins.sweeper.internal.kmp.module)
    alias(libs.plugins.kotlin.serialization)
    id("dev.gobley.cargo") version "0.3.7"
    id("dev.gobley.uniffi") version "0.3.7"
    kotlin("plugin.atomicfu") version libs.versions.kotlin
    alias(libs.plugins.rust)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

cargo {
    packageDirectory = layout.projectDirectory.dir("../disk_sweeper_core")

    builds.jvm {
        embedRustLibrary = (GobleyHost.current.rustTarget == rustTarget)
    }
}

uniffi {
    generateFromLibrary {
        namespace = "disk_sweeper_core"
    }
}