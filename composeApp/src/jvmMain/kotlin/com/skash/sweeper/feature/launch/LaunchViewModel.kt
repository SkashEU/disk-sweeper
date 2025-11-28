package com.skash.sweeper.feature.launch

import com.skash.forge.navigation.NavigationEvent
import com.skash.sweeper.BaseViewModel
import com.skash.sweeper.navigation.AppScreen

class LaunchViewModel: BaseViewModel<LaunchState, LaunchState.Intent>(LaunchState()) {
    override fun executeIntent(intent: LaunchState.Intent) = when(intent) {
        is LaunchState.Intent.GoToAdvancedSettings -> TODO()
        is LaunchState.Intent.SelectScanPath -> reduceState<LaunchState> { copy(selectedScanPath = intent.path) }
        is LaunchState.Intent.StartScan -> dispatchNavigationEvent(NavigationEvent.NavigateTo(AppScreen.Scanner(currentState.selectedScanPath)))
    }
}