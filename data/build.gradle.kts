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

val rustProjectDir = file("../disk_sweeper_core")
val rustBuildDir = file("../disk_sweeper_core/target/release")
val nativeResourceDir = file("src/jvmMain/resources/native")

val osName = System.getProperty("os.name").lowercase()
val targetFolder = when {
    osName.contains("win") -> "windows"
    osName.contains("mac") -> "macos"
    else -> "linux"
}

val libFileName = when {
    osName.contains("win") -> "disk_sweeper_core.dll"
    osName.contains("mac") -> "libdisk_sweeper_core.dylib"
    else -> "libdisk_sweeper_core.so"
}

tasks.register<Exec>("cargoBuild") {
    group = "rust"
    description = "Builds the Rust core library"
    workingDir = rustProjectDir

    commandLine("cargo", "build", "--release")

    inputs.files(fileTree(rustProjectDir) { include("src/**/*.rs", "Cargo.toml") })
    outputs.dir(rustBuildDir)
}

tasks.register<Copy>("copyRustLib") {
    group = "rust"
    description = "Copies the compiled Rust binary to the resources folder"
    dependsOn("cargoBuild")

    from(File(rustBuildDir, libFileName))

    into(File(nativeResourceDir, targetFolder))
}

tasks.named("jvmProcessResources") {
    dependsOn("copyRustLib")
}