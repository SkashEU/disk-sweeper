package com.skash.sweeper.data.mapper

import com.skash.sweeper.domain.model.*
import uniffi.disk_sweeper_core.NativeFileSystemEntry
import uniffi.disk_sweeper_core.ScanEvent

fun ScanEvent.toDomain(): ScanProgress = when(this) {
    is ScanEvent.Update -> ScanProgress.Update(
        update = this.update.toDomain()
    )
    is ScanEvent.Finished -> ScanProgress.Finished(
    result = this.result.toDomain()
    )
}

fun uniffi.disk_sweeper_core.ScanResult.toDomain() = ScanResult(
    stats = this.stats.toDomain(),
    files = this.files.map { it.toDomain() }
)

fun uniffi.disk_sweeper_core.ScanUpdate.toDomain() = ScanUpdate(
    currentPath = path,
    scannedCount = scannedCount.toLong(),
    scannedBytes = scannedBytes.toLong(),
    totalBytes = totalBytes.toLong(),
    targetBytes = targetBytes.toLong(),
    speedBps = avgSpeed.toLong(),
    etaSeconds = etaSeconds
)

fun uniffi.disk_sweeper_core.StorageStats.toDomain() = StorageStats(
    totalBytes = driveTotalBytes.toLong(),
    freeBytes = driveFreeBytes.toLong(),
    usedBytes = driveUsedBytes.toLong(),
    scannedFileCount = scannedFileCount.toLong(),
    scannedTotalBytes = scannedTotalBytes.toLong()
)

fun NativeFileSystemEntry.toDomain() = FileSystemEntry(
    id = id.toLong(),
    name = name,
    path = path,
    storageType = if (isDir) StorageType.Directory else StorageType.File,
    sizeBytes = sizeBytes.toLong(),
    allocatedSizeBytes = allocatedSizeBytes.toLong()
)