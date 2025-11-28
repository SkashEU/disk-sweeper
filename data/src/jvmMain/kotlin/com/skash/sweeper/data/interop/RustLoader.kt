package com.skash.sweeper.data.interop

import java.nio.file.Files

object RustLoader {
    private const val LIB_NAME = "disk_sweeper_core"

    fun loadLibrary() {
        val osName = System.getProperty("os.name").lowercase()

        val (pathPrefix, extension) = when {
            osName.contains("win") -> "native/windows" to ".dll"
            osName.contains("mac") -> "native/macos" to ".dylib"
            else -> "native/linux" to ".so"
        }
        val resourceName = if (osName.contains("win")) {
            "$LIB_NAME$extension"
        } else {
            "lib$LIB_NAME$extension"
        }

        val resourcePath = "/$pathPrefix/$resourceName"

        val inputStream = RustLoader::class.java.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Could not find native library at: $resourcePath")

        val tempFile = Files.createTempFile(LIB_NAME, extension).toFile()
        tempFile.deleteOnExit()

        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        System.load(tempFile.absolutePath)
    }
}