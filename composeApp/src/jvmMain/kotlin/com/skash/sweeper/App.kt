package com.skash.sweeper

import androidx.compose.runtime.Composable
import com.skash.sweeper.designsystem.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
    KoinMultiplatformApplication(
        config = koinConfiguration {
            modules(diModule)
        }
    ) {

        AppTheme {
            AppNavigation()
        }
    }
}