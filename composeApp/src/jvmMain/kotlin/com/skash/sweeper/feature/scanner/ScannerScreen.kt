package com.skash.sweeper.feature.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.skash.sweeper.feature.scanner.ScannerState.Scanned.Intent.*
import com.skash.sweeper.feature.scanner.state.InitialState
import com.skash.sweeper.feature.scanner.state.ScanOverViewState
import com.skash.sweeper.feature.scanner.state.ScanningState

@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {

    val state by viewModel.collectStateFlow().collectAsState()

    ScannerScreenImpl(
        state = state,
        executeIntent = viewModel::executeIntent
    )
}

@Composable
private fun ScannerScreenImpl(
    state: ScannerState,
    executeIntent: (ScannerState.Intent) -> Unit
) {
    when (state) {
        is ScannerState.Scanned -> {
            ScanOverViewState(
                state = state,
                onToggleItemDelete = { executeIntent(ToggleItemDelete(it)) },
                onMarkPageForDeletion = { page, items ->
                    executeIntent(MarkPageForDeletion(page, items))
                },
                onUnmarkPageForDeletion = { page, items ->
                    executeIntent(UnmarkPageForDeletion(page, items))
                },
            )
        }

        is ScannerState.Scanning -> ScanningState(state = state)
        ScannerState.Initial -> InitialState()
    }
}