package com.skash.sweeper.domain.model

data class ScanUpdate(
    val currentPath: String,
    val scannedCount: Long,
    val scannedBytes: Long,
    val totalBytes: Long,
    val targetBytes: Long,
    val speedBps: Long,
    val etaSeconds: Long
)
