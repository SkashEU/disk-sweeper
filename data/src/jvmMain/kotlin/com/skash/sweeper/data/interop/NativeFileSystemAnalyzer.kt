package com.skash.sweeper.data.interop

import com.skash.sweeper.domain.FileSystemAnalyzer
import com.skash.sweeper.domain.ScanResult
import com.skash.sweeper.domain.model.DiskStats
import com.skash.sweeper.domain.model.ScanResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class NativeFileSystemAnalyzer : FileSystemAnalyzer {

    external fun scanDirectory(path: String, listener: ScanListener): String
    private external fun deletePath(path: String, permanently: Boolean): String

    companion object Companion {
        init {
            RustLoader.loadLibrary()
        }
    }

    override fun scan(path: String): Flow<ScanResult> = callbackFlow {
        val listener = object : ScanListener {
            override fun onProgress(
                currentPath: String,
                scannedCount: Long,
                scannedBytes: Long,
                totalBytes: Long,
                targetBytes: Long,
                speedBps: Long,
                etaSeconds: Long
            ) {
                trySend(
                    ScanResult.Update(
                        currentPath, scannedCount, scannedBytes, totalBytes, targetBytes, speedBps, etaSeconds
                    )
                )
            }
        }

        val jsonString = scanDirectory(path, listener)

        try {
            val items = Json.decodeFromString<ScanResponse>(jsonString)
            send(ScanResult.Done(items))
        } catch (e: Exception) {
            send(ScanResult.Done(ScanResponse(DiskStats(), emptyList())))
        }
        close()

        awaitClose {
        }
    }.flowOn(Dispatchers.IO)

   override suspend fun delete(path: String, permanently: Boolean): Boolean = withContext(Dispatchers.IO) {
        val error = deletePath(path, permanently)
        if (error.isNotEmpty()) {
            println("DELETE FAILED: $error")
            false
        } else {
            true
        }
    }
}


interface ScanListener {
    fun onProgress(
        currentPath: String,
        scannedCount: Long,
        scannedBytes: Long,
        totalBytes: Long,
        targetBytes: Long,
        speedBps: Long,
        etaSeconds: Long
    )
}