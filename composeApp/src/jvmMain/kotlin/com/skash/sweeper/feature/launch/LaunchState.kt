package com.skash.sweeper.feature.launch

data class LaunchState(
    val selectedScanPath: String = "",
    val recentScans: List<String> = listOf("abc", "test", "ye")
) {

    sealed interface Intent {
        data class SelectScanPath(val path: String) : Intent
        data object StartScan : Intent
        data object GoToAdvancedSettings : Intent
    }
}