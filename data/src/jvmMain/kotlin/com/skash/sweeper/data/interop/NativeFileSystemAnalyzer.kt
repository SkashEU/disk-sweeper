package com.skash.sweeper.data.interop

import com.skash.sweeper.domain.FileSystemAnalyzer
import com.skash.sweeper.domain.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class NativeFileSystemAnalyzer: FileSystemAnalyzer {

    external fun scanDirectory(path: String): String

    companion object Companion {
        init {
            RustLoader.loadLibrary()
        }
    }

    override suspend fun scan(path: String): List<FileItem> = withContext(Dispatchers.IO) {
        val jsonResult = scanDirectory(path)

       Json.decodeFromString<List<FileItem>>(jsonResult)
    }
}