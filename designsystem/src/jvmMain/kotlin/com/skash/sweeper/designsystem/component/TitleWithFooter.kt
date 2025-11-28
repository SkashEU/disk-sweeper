package com.skash.sweeper.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TitleWithFooter(modifier: Modifier = Modifier, headline: String, footer: String) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = headline, style = MaterialTheme.typography.titleSmall)
        Text(text = footer, style = MaterialTheme.typography.bodySmall)
    }
}