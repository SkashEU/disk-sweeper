package com.skash.sweeper.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExplainableContent(modifier: Modifier, headline: String, footer: String, content: @Composable () -> Unit) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = headline, style = MaterialTheme.typography.titleSmall)
        content()
        Text(text = footer, style = MaterialTheme.typography.bodySmall)
    }
}