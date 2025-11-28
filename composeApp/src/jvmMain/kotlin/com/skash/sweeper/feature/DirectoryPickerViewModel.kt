package com.skash.sweeper.feature

import com.skash.sweeper.BaseViewModel

class DirectoryPickerViewModel :
    BaseViewModel<DirectoryPickerState, DirectoryPickerState.Intent>(DirectoryPickerState()) {

    override fun executeIntent(intent: DirectoryPickerState.Intent) = when (intent) {
        is DirectoryPickerState.Intent.ScanDirectory -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleScanDirectory
        )

        is DirectoryPickerState.Intent.AddDirectory -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleAddDirectory
        )

        is DirectoryPickerState.Intent.RemoveDirectory -> handleIntent<_, _>(
            intent = intent,
            handler = ::handleRemoveDirectory
        )
    }

    private fun handleScanDirectory(
        state: DirectoryPickerState,
        intent: DirectoryPickerState.Intent.ScanDirectory
    ) {

    }

    private fun handleAddDirectory(
        state: DirectoryPickerState,
        intent: DirectoryPickerState.Intent.AddDirectory
    ) {
        setState(state.copy(directories = state.directories + intent.directory))
    }

    private fun handleRemoveDirectory(
        state: DirectoryPickerState,
        intent: DirectoryPickerState.Intent.RemoveDirectory
    ) {
        setState(state.copy(directories = state.directories - intent.directory))
    }
}