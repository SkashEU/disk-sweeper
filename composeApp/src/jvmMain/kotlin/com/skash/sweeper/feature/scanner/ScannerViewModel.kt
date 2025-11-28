package com.skash.sweeper.feature.scanner

import androidx.lifecycle.viewModelScope
import com.skash.forge.outcome.onEachOutcome
import com.skash.sweeper.BaseViewModel
import com.skash.sweeper.domain.usecase.GetFileItemsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ScannerViewModel(
    private val path: String,
    getFileItemsUseCase: GetFileItemsUseCase
): BaseViewModel<ScannerState, ScannerState.Intent>(initialState = ScannerState.Scanning) {

    private val files = getFileItemsUseCase(path)
        .onEachOutcome(
            onSuccess = {
                setState(ScannerState.Scanned(it))
            }
        )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    override fun executeIntent(intent: ScannerState.Intent) {
        TODO("Not yet implemented")
    }
}