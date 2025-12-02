package com.skash.sweeper.feature.scanner.state

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.skash.sweeper.designsystem.layout.Page
import com.skash.sweeper.designsystem.layout.Screen
import com.skash.sweeper.feature.scanner.ScannerState
import com.skash.sweeper.util.formatSecondsToDigital
import com.skash.sweeper.util.toHumanReadableSize

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ScanningState(state: ScannerState.Scanning) {
    val progressRaw = (state.update.scannedBytes.toDouble() / state.update.targetBytes.toDouble()).coerceIn(0.0, 1.0)
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
                text = "Scanning: ${state.update.currentPath}",
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
                    title = "Time Remaining",
                    value = state.update.etaSeconds.formatSecondsToDigital()
                )

                ScanDetailCard(
                    title = "Files processed",
                    value = state.update.scannedCount.toString()
                )

                ScanDetailCard(
                    title = "Data Analyzed",
                    value = state.update.scannedBytes.toHumanReadableSize()
                )
            }
        }
    }
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