package com.skash.sweeper.domain.model

data class ScanResult(
    val stats: StorageStats,
    val files: List<FileSystemEntry>
)