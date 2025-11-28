package com.skash.sweeper.feature.launch

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skash.sweeper.designsystem.component.OutstandingIcon
import com.skash.sweeper.designsystem.component.SectionGroup
import com.skash.sweeper.designsystem.component.TitleWithFooter
import com.skash.sweeper.designsystem.layout.Page
import com.skash.sweeper.designsystem.layout.Screen
import com.skash.sweeper.designsystem.theme.Spacing
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import io.github.vinceglb.filekit.path

@Composable
fun LaunchScreen(viewModel: LaunchViewModel) {
    val state by viewModel.collectStateFlow().collectAsState()

    LaunchScreenImpl(
        state = state,
        executeIntent = viewModel::executeIntent
    )
}

@Composable
private fun LaunchScreenImpl(
    state: LaunchState,
    executeIntent: (LaunchState.Intent) -> Unit
) {

    Screen(
        title = "Disk Space Analyzer",
        subTitle = "Choose a scan option to begin the analysis."
    ) {

        Page {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(Spacing.Large)) {
                QuickScan(
                    state = state,
                    onSelectScanPath = { executeIntent(LaunchState.Intent.SelectScanPath(it)) },
                    onClickStartScan = { executeIntent(LaunchState.Intent.StartScan) }
                )
                AdvancedScan(modifier = Modifier.weight(1f), state = state)
            }
        }

    }
}

@Composable
private fun RowScope.QuickScan(
    state: LaunchState,
    onSelectScanPath: (String) -> Unit,
    onClickStartScan: () -> Unit
) {

    val launcher = rememberDirectoryPickerLauncher { directory ->
        onSelectScanPath(directory?.path ?: "")
    }

    OutlinedCard(modifier = Modifier.fillMaxSize().weight(1f)) {

        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                OutstandingIcon(Modifier.size(60.dp), Icons.Default.Bolt)
                TitleWithFooter(headline = "Quick Scan", footer = "Scan a directory with default settings.")
            }

            SectionGroup(title = "Scan Path") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    ElevatedCard(modifier = Modifier.weight(1f), onClick = { launcher.launch() }) {
                        Row(
                            modifier = Modifier.padding(Spacing.Medium),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = state.selectedScanPath.takeIf { it.isNotBlank() } ?: "Select a directory",
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Icon(Icons.Default.Folder, contentDescription = "Select Directory")
                        }
                    }

                    Button(onClick = onClickStartScan) {
                        Text("Start Scan")
                    }
                }
            }

            if (state.recentScans.isNotEmpty()) {
                SectionGroup(title = "Or select a recent location") {

                    state.recentScans.forEachIndexed { index, path ->
                        DirectoryCard(path = path, onClick = {})
                        if (index != state.recentScans.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvancedScan(modifier: Modifier = Modifier, state: LaunchState) {
    OutlinedCard(modifier = Modifier.fillMaxSize().then(modifier)) {

        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                OutstandingIcon(Modifier.size(60.dp), Icons.Default.Tune)
                TitleWithFooter(
                    headline = "Advanced Scan",
                    footer = "Customize scan parameters for a detailed analysis."
                )
            }

            HorizontalDivider()

            Spacer(Modifier.weight(1f))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.Large)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Text(text = "Specify custom foldersm for a targeted scan.")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    Icon(Icons.Default.FilterAltOff, contentDescription = null)
                    Text(text = "Set exclusion rules to ignore specific file types or directories.")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Text(text = "Configure scan depth, min file size and apply file type filters for precise results")
                }

            }



            Spacer(Modifier.weight(1f))

            Button(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                Text(text = "Go to Advanced Settings")
            }


        }
    }
}

@Composable
private fun DirectoryCard(path: String, onClick: () -> Unit) {

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        OutstandingIcon(Modifier.size(30.dp), Icons.Default.Folder)
        Text(modifier = Modifier.padding(horizontal = 16.dp), text = path)
        Spacer(Modifier.weight(1f))

        IconButton(onClick = onClick) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Scan directory")
        }
    }
}