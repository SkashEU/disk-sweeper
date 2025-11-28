package com.skash.sweeper.feature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skash.sweeper.designsystem.component.ExplainableContent
import com.skash.sweeper.designsystem.component.OutstandingText
import com.skash.sweeper.designsystem.component.SectionGroup
import com.skash.sweeper.designsystem.layout.Page
import com.skash.sweeper.designsystem.layout.Screen
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path

@Composable
fun DirectoryPickerScreen(viewModel: DirectoryPickerViewModel) {

    val state by viewModel.collectStateFlow().collectAsState()

    DirectoryPickerScreenImpl(
        state = state,
        executeIntent = viewModel::executeIntent
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DirectoryPickerScreenImpl(
    state: DirectoryPickerState,
    executeIntent: (DirectoryPickerState.Intent) -> Unit
) {

    val launcher = rememberDirectoryPickerLauncher { directory ->
        executeIntent(DirectoryPickerState.Intent.AddDirectory(directory?.path ?: ""))
    }

    Screen {
        Page {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp)) {
                Text(text = "Scan Settings", style = MaterialTheme.typography.headlineLargeEmphasized)
                Text(
                    text = "Configure parameters to disk scanning.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            SectionGroup(header = {
                Text(text = "Scan Targets", style = MaterialTheme.typography.titleLarge)
                Button(onClick = { launcher.launch() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Folder")
                    Text("Add Folder")
                }
            }) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 50.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.directories) { path ->
                        DirectoryCard(
                            path,
                            onClickRemove = { executeIntent(DirectoryPickerState.Intent.RemoveDirectory(path)) })
                    }
                }
            }

            SectionGroup(title = "File Rules") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(50.dp)) {

                    ExplainableContent(
                        modifier = Modifier.weight(1f),
                        headline = "Excluded File Extensions",
                        footer = "Comma-seperated list of file extensions to exclude from the scan."
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = ".log, .tmp, bak",
                            onValueChange = {},
                            singleLine = true
                        )
                    }

                    ExplainableContent(
                        modifier = Modifier.weight(1f),
                        headline = "Minimum FIle Size to report",
                        footer = "Only shows files larger than this size."
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Slider(modifier = Modifier.weight(1f), value = 5f, onValueChange = {})
                            OutstandingText(text = "5 MB")
                        }
                    }
                }
            }

            SectionGroup(title = "Application Preferences") {
                PreferenceCard(
                    "Follow symbolic links",
                    "Include directories and files linked symbolically in the scan",
                    true
                ) { }
            }
        }
    }

}

@Composable
private fun DirectoryCard(path: String, onClickRemove: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Filled.Folder, contentDescription = "Folder")
            Text(modifier = Modifier.padding(horizontal = 16.dp), text = path)
            Spacer(Modifier.weight(1f))

            IconButton(onClick = onClickRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove Directory")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PreferenceCard(title: String, description: String, enabled: Boolean, onToggleEnable: () -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmallEmphasized)
                Text(text = description)
            }

            Spacer(Modifier.weight(1f))

            Switch(checked = enabled, onCheckedChange = { onToggleEnable() })
        }
    }
}