package com.skash.sweeper.feature.scanner

import com.skash.sweeper.domain.ScanResult
import com.skash.sweeper.domain.model.FileItem
import com.skash.sweeper.domain.model.ScanResponse

sealed interface ScannerState {

    sealed interface Intent
    //TODO: Move this into a better structure its just poc
    data class Scanning(
        val state: ScanResult.Update = ScanResult.Update("", 0, 0, 0, 0, 0, 0)
    ) : ScannerState

    data class Scanned(
        val path: String, val scanResponse: ScanResponse,
        val itemsToDelete: Set<FileItem> = emptySet(),
        val pagesToDelete: Set<Int> = emptySet()
    ) : ScannerState {
        val sizeOfItemsToDelete get() = itemsToDelete.sumOf { it.sizeBytes }

        sealed interface Intent: ScannerState.Intent {
            data object DeleteSelected : Intent
            data class ToggleItemDelete(val item: FileItem) : Intent
            data class MarkPageForDeletion(val page: Int, val items: List<FileItem>) : Intent
            data class UnmarkPageForDeletion(val page: Int, val items: List<FileItem>) : Intent
        }
    }
}