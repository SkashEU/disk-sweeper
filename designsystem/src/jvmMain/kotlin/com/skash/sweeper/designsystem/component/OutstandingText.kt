package com.skash.sweeper.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OutstandingText(modifier: Modifier = Modifier, text: String) {
    OutlinedCard(modifier = modifier) {
        Text(modifier = Modifier.padding(8.dp), text = text)
    }
}