package com.skash.sweeper.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FileItem(
    val name: String,
    val path: String,
    @SerialName("is_dir")
    val isDirectory: Boolean,
    @SerialName("size_bytes")
    val sizeBytes: Long
) {
    fun humanReadableSize(): String {
        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        return when {
            sizeBytes >= gb -> String.format("%.2f GB", sizeBytes / gb)
            sizeBytes >= mb -> String.format("%.2f MB", sizeBytes / mb)
            sizeBytes >= kb -> String.format("%.2f KB", sizeBytes / kb)
            else -> "$sizeBytes B"
        }
    }
}