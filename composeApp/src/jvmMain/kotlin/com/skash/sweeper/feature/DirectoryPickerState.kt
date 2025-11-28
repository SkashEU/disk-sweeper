package com.skash.sweeper.feature

data class DirectoryPickerState(
    val directories: List<String> = emptyList(),
    val isScanning: Boolean = false,
    val progress: Int = 0
) {

    sealed interface Intent {
        data class AddDirectory(val directory: String) : Intent
        data class RemoveDirectory(val directory: String) : Intent
        data object ScanDirectory : Intent
    }
}