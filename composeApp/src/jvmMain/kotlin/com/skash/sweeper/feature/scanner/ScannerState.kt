package com.skash.sweeper.feature.scanner

import com.skash.sweeper.domain.model.FileItem

sealed interface ScannerState {

    sealed interface Intent
    data object Scanning : ScannerState
    data class Scanned(val files: List<FileItem>) : ScannerState
}