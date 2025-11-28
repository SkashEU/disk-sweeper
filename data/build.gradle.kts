plugins {
    alias(libs.plugins.sweeper.internal.kmp.module)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain)
            implementation(libs.kotlinx.serialization.json)
        }
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