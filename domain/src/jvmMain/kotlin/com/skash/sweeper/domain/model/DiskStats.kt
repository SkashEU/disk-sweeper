package com.skash.sweeper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DiskStats(
    val driveTotalBytes: Long = 0,
    val driveFreeBytes: Long = 0,
    val driveUsedBytes: Long = 0,
    val scannedFileCount: Long = 0,
    val scannedTotalBytes: Long = 0
)