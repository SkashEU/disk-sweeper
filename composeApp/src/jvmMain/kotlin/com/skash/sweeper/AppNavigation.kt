package com.skash.sweeper

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.skash.forge.navigation.NavigationDispatcher
import com.skash.forge.navigation.nav2.CollectNavigationEvents
import com.skash.forge.navigation.nav2.DefaultNavHost
import com.skash.forge.navigation.nav2.composableWithTransition
import com.skash.sweeper.feature.DirectoryPickerScreen
import com.skash.sweeper.feature.launch.LaunchScreen
import com.skash.sweeper.feature.scanner.ScannerScreen
import com.skash.sweeper.navigation.AppScreen
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()
    val dispatcher = koinInject<NavigationDispatcher>()

    rootNavController.CollectNavigationEvents(dispatcher)

    DefaultNavHost(
        navController = rootNavController,
        startDestination = AppScreen.Launch,
        builder = {
            composableWithTransition<AppScreen.Launch> {
                LaunchScreen(koinViewModel())
            }

            composableWithTransition<AppScreen.Scanner> {
                val args = it.toRoute<AppScreen.Scanner>()
                ScannerScreen(
                    koinViewModel(parameters = {
                        parametersOf(args.path)
                    })
                )
            }
        }
    )
}