package com.skash.sweeper

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Disk Sweeper",
        alwaysOnTop = true,
        state = rememberWindowState(width = 1300.dp, height = 900.dp)
    ) {
        App()
    }
}