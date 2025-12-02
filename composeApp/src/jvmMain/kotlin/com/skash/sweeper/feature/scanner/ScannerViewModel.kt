package com.skash.sweeper.feature.scanner

import androidx.lifecycle.viewModelScope
import com.skash.sweeper.BaseViewModel
import com.skash.sweeper.domain.model.ScanProgress
import com.skash.sweeper.domain.usecase.GetFileItemsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class ScannerViewModel(
    private val path: String,
    getFileItemsUseCase: GetFileItemsUseCase
) : BaseViewModel<ScannerState, ScannerState.Intent>(initialState = ScannerState.Initial) {

    private val files = getFileItemsUseCase(path)
        .onEach { scanProgress ->
            when (scanProgress) {
                is ScanProgress.Finished -> setState(ScannerState.Scanned(path, scanProgress.result))
                is ScanProgress.Update -> setState(ScannerState.Scanning(scanProgress.update))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    override fun executeIntent(intent: ScannerState.Intent) = when (intent) {
        ScannerState.Scanned.Intent.DeleteSelected -> TODO()
        is ScannerState.Scanned.Intent.ToggleItemDelete -> reduceState<ScannerState.Scanned> {
            val isMarkedToDelete = itemsToDelete.contains(intent.item)
            copy(itemsToDelete = if (isMarkedToDelete) itemsToDelete.minus(intent.item) else itemsToDelete.plus(intent.item))
        }

        is ScannerState.Scanned.Intent.MarkPageForDeletion -> reduceState<ScannerState.Scanned> {
            copy(itemsToDelete = itemsToDelete + intent.items, pagesToDelete = pagesToDelete + intent.page)
        }

        is ScannerState.Scanned.Intent.UnmarkPageForDeletion -> reduceState<ScannerState.Scanned> {
            copy(itemsToDelete = itemsToDelete - intent.items.toSet(), pagesToDelete = pagesToDelete - intent.page)
        }
    }
}