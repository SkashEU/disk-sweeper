package com.skash.sweeper.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.skash.sweeper.designsystem.theme.Spacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SectionGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    SectionGroup(header = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLargeEmphasized,
            color = MaterialTheme.colorScheme.onSurface
        )
    }, content = content)
}

@Composable
fun SectionGroup(
    modifier: Modifier = Modifier,
    header: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().then(modifier)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = header
        )

        content()
    }
}