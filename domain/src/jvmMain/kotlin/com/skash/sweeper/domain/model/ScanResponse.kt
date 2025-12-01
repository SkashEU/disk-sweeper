package com.skash.sweeper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ScanResponse(
    val stats: DiskStats,
    val files: List<FileItem>
)