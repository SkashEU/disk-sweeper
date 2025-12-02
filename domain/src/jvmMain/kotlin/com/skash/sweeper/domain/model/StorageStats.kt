package com.skash.sweeper.domain.model

data class StorageStats(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val scannedFileCount: Long,
    val scannedTotalBytes: Long,
)
