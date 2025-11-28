package com.skash.sweeper.feature.scanner

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.skash.sweeper.designsystem.layout.Screen

@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {

    val state by viewModel.collectStateFlow().collectAsState()

    Screen {
        when(val uiState = state) {
            is ScannerState.Scanned -> {
                LazyColumn {
                    items(uiState.files) {
                        Text(text = it.path, color = Color.White)
                    }
                }
            }
            ScannerState.Scanning -> CircularProgressIndicator()
        }
    }
}