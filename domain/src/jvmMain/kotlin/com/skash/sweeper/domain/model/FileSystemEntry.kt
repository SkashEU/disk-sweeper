package com.skash.sweeper.domain.model

data class FileSystemEntry(
    val id: Long,
    val name: String,
    val path: String,
    val storageType: StorageType,
    val sizeBytes: Long,
    val allocatedSizeBytes: Long
)