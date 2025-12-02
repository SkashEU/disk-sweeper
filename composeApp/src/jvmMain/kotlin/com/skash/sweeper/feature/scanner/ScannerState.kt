package com.skash.sweeper.feature.scanner

import com.skash.sweeper.domain.model.FileSystemEntry
import com.skash.sweeper.domain.model.ScanResult
import com.skash.sweeper.domain.model.ScanUpdate

sealed interface ScannerState {

    sealed interface Intent

    data object Initial : ScannerState

    data class Scanning(
        val update: ScanUpdate
    ) : ScannerState

    data class Scanned(
        val rootPath: String,
        val currentPath: String,
        val scanResult: ScanResult,
        val itemsToDelete: Set<FileSystemEntry> = emptySet(),
        val pagesToDelete: Set<Int> = emptySet()
    ) : ScannerState {

        sealed interface Intent : ScannerState.Intent {
            data object DeleteSelected : Intent
            data class ToggleItemDelete(val item: FileSystemEntry) : Intent
            data class MarkPageForDeletion(val page: Int, val items: List<FileSystemEntry>) : Intent
            data class UnmarkPageForDeletion(val page: Int, val items: List<FileSystemEntry>) : Intent
            data class GoToDirectory(val directory: String) : Intent
        }
    }
}