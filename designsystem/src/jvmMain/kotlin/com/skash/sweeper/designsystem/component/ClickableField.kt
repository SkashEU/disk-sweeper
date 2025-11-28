package com.skash.sweeper.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ClickableField(
    modifier: Modifier = Modifier,
    value: String,
    icon: ImageVector? = null,
    placeholder: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .then(modifier),
            placeholder = { Text(text = placeholder) },
            label = { Text(text = placeholder) },
            enabled = false,
            readOnly = true,
            trailingIcon = {
                icon?.let { Icon(imageVector = icon, contentDescription = null) }
            },
        )
    }
}