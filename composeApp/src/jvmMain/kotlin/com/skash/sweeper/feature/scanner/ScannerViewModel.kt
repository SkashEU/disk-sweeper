package com.skash.sweeper.feature.scanner

import androidx.lifecycle.viewModelScope
import com.skash.sweeper.BaseViewModel
import com.skash.sweeper.domain.ScanResult
import com.skash.sweeper.domain.usecase.GetFileItemsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class ScannerViewModel(
    private val path: String,
    getFileItemsUseCase: GetFileItemsUseCase
) : BaseViewModel<ScannerState, ScannerState.Intent>(initialState = ScannerState.Scanning()) {

    private val files = getFileItemsUseCase(path)
        .onEach {
            when (it) {
                is ScanResult.Done -> setState(ScannerState.Scanned(path, it.response))
                is ScanResult.Update -> setState(ScannerState.Scanning(it))
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