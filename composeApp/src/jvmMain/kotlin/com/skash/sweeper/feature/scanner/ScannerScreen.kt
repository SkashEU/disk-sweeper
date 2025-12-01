package com.skash.sweeper.feature.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skash.sweeper.designsystem.layout.Page
import com.skash.sweeper.designsystem.layout.Screen
import com.skash.sweeper.domain.model.FileItem
import com.skash.sweeper.feature.scanner.state.ScanOverViewState

@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {

    val state by viewModel.collectStateFlow().collectAsState()

    Screen {
        when (val uiState = state) {
            is ScannerState.Scanned -> {
                ScanOverViewState(
                    state = uiState,
                    onToggleItemDelete = { viewModel.executeIntent(ScannerState.Scanned.Intent.ToggleItemDelete(it)) },
                    onMarkPageForDeletion = { page, items ->
                        viewModel.executeIntent(
                            ScannerState.Scanned.Intent.MarkPageForDeletion(
                                page,
                                items
                            )
                        )
                    },
                    onUnmarkPageForDeletion = { page, items ->
                        viewModel.executeIntent(
                            ScannerState.Scanned.Intent.UnmarkPageForDeletion(
                                page,
                                items
                            )
                        )
                    },
                )
            }

            is ScannerState.Scanning -> Scanning(uiState)
        }
    }
}

@Composable
fun FileRow(
    item: FileItem,
    isMarkedForDeletion: Boolean,
    onClick: (FileItem) -> Unit,
    onToggleMarkForDeletion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = { onClick(item) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(isMarkedForDeletion, onCheckedChange = { onToggleMarkForDeletion() })

            Icon(
                if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.bodyLarge)
            }

            // Highlight large files
            val isLarge = item.sizeBytes > 100 * 1024 * 1024 // 100MB
            Text(
                text = formatSize(item.sizeBytes),
                style = MaterialTheme.typography.labelLarge,
                color = if (isLarge) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun Scanning(state: ScannerState.Scanning) {
    Screen {
        Page {
            ProgressDashboard(state)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressDashboard(state: ScannerState.Scanning) {

    val progressRaw = (state.state.scannedBytes.toDouble() / state.state.targetBytes.toDouble()).coerceIn(0.0, 1.0)
    val percentage = (progressRaw * 100).toInt()

    Screen(title = "Scanning...", subTitle = "Please wait while files are scanned.") {

        Page(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {

            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {

                Text(text = percentage.toString(), style = MaterialTheme.typography.displayMedium)
                CircularWavyProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    progress = { progressRaw.toFloat() },
                )
            }

            Spacer(Modifier.height(24.dp))


            LinearProgressIndicator(
                progress = { progressRaw.toFloat() },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                trackColor = MaterialTheme.colorScheme.surface,
            )

            Text(
                text = "Scanning: ${state.state.currentPath}",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            Text(text = "Scan Details", style = MaterialTheme.typography.titleLargeEmphasized)

            Spacer(Modifier.height(24.dp))


            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ScanDetailCard(
                    title = "TIme Remaining",
                    value = formatDuration(state.state.etaSeconds)
                )

                ScanDetailCard(
                    title = "Files processed",
                    value = state.state.scannedCount.toString()
                )

                ScanDetailCard(
                    title = "Data Analyzed",
                    value = formatSize(state.state.scannedBytes)
                )
            }
        }

    }


    /*

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // 1. Top Stats Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Files Scanned", style = MaterialTheme.typography.labelMedium)
                    Text("%,d".format(state.state.scannedCount), style = MaterialTheme.typography.headlineMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Progress", style = MaterialTheme.typography.labelMedium)
                    Text("$percentage%", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. The Progress Bar
            LinearProgressIndicator(
                progress = { progressRaw.toFloat() },
                modifier = Modifier.fillMaxWidth().height(12.dp),
                trackColor = MaterialTheme.colorScheme.surface,
            )

            Spacer(Modifier.height(16.dp))

            // 3. Bottom Stats Row (Size / Speed / ETA)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "${formatSize(state.state.scannedBytes)} / ${formatSize(state.state.targetBytes)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row {
                    Text(
                        text = formatSpeed(state.state.speedBps),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(" • ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "ETA: ${formatDuration(state.state.etaSeconds)}",
                        style = MaterialTheme.typography.bodyMedium,
                        // If ETA is 0 or very large, maybe grey it out
                        color = if (state.state.etaSeconds > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. Current Path (Truncated)
            Text(
                text = "Scanning: ${state.state.currentPath}",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

     */
}

@Composable
private fun ScanDetailCard(title: String, value: String) {
    OutlinedCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

fun formatSize(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        bytes >= gb -> String.format("%.2f GB", bytes / gb)
        bytes >= mb -> String.format("%.1f MB", bytes / mb)
        else -> String.format("%.0f KB", bytes / kb)
    }
}

fun formatSpeed(bps: Long): String {
    val mb = 1024 * 1024.0
    return if (bps > mb) {
        String.format("%.1f MB/s", bps / mb)
    } else {
        String.format("%d KB/s", bps / 1024)
    }
}

fun formatDuration(seconds: Long): String {
    return if (seconds > 3600) {
        "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    } else {
        "${seconds / 60}m ${seconds % 60}s"
    }
}