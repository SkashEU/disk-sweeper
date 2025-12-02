package com.skash.sweeper.feature.scanner.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.skash.sweeper.designsystem.layout.Page
import com.skash.sweeper.designsystem.layout.Screen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun InitialState() {
    Screen(title = "Setting everything up...") {
        Page(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            CircularWavyProgressIndicator()
        }
    }
}