package com.skash.sweeper.domain

import com.skash.sweeper.domain.model.ScanResponse
import kotlinx.coroutines.flow.Flow

interface FileSystemAnalyzer {

    fun scan(path: String): Flow<ScanResult>

    suspend fun delete(path: String, permanently: Boolean = false): Boolean
}

sealed interface ScanResult {

    data class Done(val response: ScanResponse) : ScanResult

    data class Update(
        val currentPath: String,
        val scannedCount: Long,
        val scannedBytes: Long,
        val totalBytes: Long,
        val targetBytes: Long,
        val speedBps: Long,
        val etaSeconds: Long
    ) : ScanResult

}