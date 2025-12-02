package com.skash.sweeper.domain.model

sealed interface ScanProgress {
    data class Finished(val result: ScanResult) : ScanProgress
    data class Update(val update: ScanUpdate) : ScanProgress
}