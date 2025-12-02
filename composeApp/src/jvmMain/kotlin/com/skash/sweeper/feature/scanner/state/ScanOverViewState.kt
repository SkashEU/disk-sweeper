package com.skash.sweeper.feature.scanner.state

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skash.sweeper.designsystem.layout.Page
import com.skash.sweeper.designsystem.layout.Screen
import com.skash.sweeper.designsystem.theme.Spacing
import com.skash.sweeper.domain.model.FileSystemEntry
import com.skash.sweeper.domain.model.StorageType
import com.skash.sweeper.domain.model.parsePathToSegments
import com.skash.sweeper.feature.scanner.ScannerState
import com.skash.sweeper.util.toHumanReadableSize
import io.github.windedge.table.PaginationState
import io.github.windedge.table.m3.PaginatedDataTable
import io.github.windedge.table.m3.Paginator
import io.github.windedge.table.rememberPaginationState

@Composable
internal fun ScanOverViewState(
    state: ScannerState.Scanned,
    onToggleItemDelete: (FileSystemEntry) -> Unit,
    onMarkPageForDeletion: (Int, List<FileSystemEntry>) -> Unit,
    onUnmarkPageForDeletion: (Int, List<FileSystemEntry>) -> Unit,
    onDirectoryClick: (String) -> Unit
) {
    Screen(title = "Scan Overview") {
        Page {
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(Spacing.Large)) {

                Column(Modifier.fillMaxWidth(0.3f), verticalArrangement = Arrangement.spacedBy(Spacing.Medium)) {
                    DiskUsage(
                        modifier = Modifier.weight(1f),
                        usedSpace = state.scanResult.stats.usedBytes,
                        freeSpace = state.scanResult.stats.freeBytes
                    )
                    TopLargestFolders(
                        modifier = Modifier.weight(1f),
                        folders = state.scanResult.files,
                        totalSpace = state.scanResult.stats.totalBytes,
                    )
                }

                FolderTable(
                    modifier = Modifier.weight(1f),
                    rootPath = state.rootPath,
                    currentPath = state.currentPath,
                    items = state.scanResult.files,
                    itemsToDelete = state.itemsToDelete,
                    onDirectoryClick = onDirectoryClick,
                    onToggleItemDelete = onToggleItemDelete,
                    pagesToDelete = state.pagesToDelete,
                    onMarkPageForDeletion = onMarkPageForDeletion,
                    onUnmarkPageForDeletion = onUnmarkPageForDeletion
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DiskUsage(
    modifier: Modifier = Modifier,
    usedSpace: Long,
    freeSpace: Long,
) {

    val totalSpace = usedSpace + freeSpace
    val progressRaw = (usedSpace.toDouble() / totalSpace.toDouble()).coerceIn(0.0, 1.0)
    val percentage = (progressRaw * 100).toInt()

    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.Medium),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Disk Usage", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.weight(1f))

            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {

                Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$percentage%", style = MaterialTheme.typography.displayMedium)
                    Text(text = "Used Space", style = MaterialTheme.typography.labelMedium)
                }

                CircularWavyProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    progress = { progressRaw.toFloat() },
                )
            }

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                Text(text = "Used", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.weight(1f))
                Text(text = usedSpace.toHumanReadableSize(), style = MaterialTheme.typography.bodyMedium)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                Box(
                    modifier = Modifier.size(10.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                )
                Text(text = "Free", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.weight(1f))
                Text(text = freeSpace.toHumanReadableSize(), style = MaterialTheme.typography.bodyMedium)

            }

            Spacer(Modifier.weight(1f))
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopLargestFolders(
    modifier: Modifier = Modifier,
    folders: List<FileSystemEntry>,
    totalSpace: Long,
) {

    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(Spacing.Medium),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "Top Largest Folders", style = MaterialTheme.typography.titleMedium)

            folders.take(3).forEach { folder ->
                val usedSpaceByFolder =
                    (folder.allocatedSizeBytes.toDouble() / totalSpace.toDouble()).coerceIn(0.0, 1.0)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.Small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                        Row {
                            Text(text = folder.name, style = MaterialTheme.typography.labelMedium)
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = folder.allocatedSizeBytes.toHumanReadableSize(),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        LinearProgressIndicator(
                            progress = { usedSpaceByFolder.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(12.dp),
                            drawStopIndicator = {}
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FolderTable(
    modifier: Modifier = Modifier,
    rootPath: String,
    currentPath: String,
    items: List<FileSystemEntry>,
    itemsToDelete: Set<FileSystemEntry>,
    pagesToDelete: Set<Int>,
    onDirectoryClick: (String) -> Unit,
    onToggleItemDelete: (FileSystemEntry) -> Unit,
    onMarkPageForDeletion: (Int, List<FileSystemEntry>) -> Unit,
    onUnmarkPageForDeletion: (Int, List<FileSystemEntry>) -> Unit
) {

    val paginationState = rememberPaginationState(items.size, pageSize = 10)

    Column(
        modifier = Modifier.fillMaxSize()
            .then(modifier)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            BreadcrumbBar(
                rootPath = rootPath,
                currentPath = currentPath,
                onPathClick = onDirectoryClick
            )
        }

        OutlinedCard {
            Column {
                PaginatedDataTable(
                    columns = {
                        column {
                            Checkbox(checked = pagesToDelete.contains(paginationState.pageIndex), onCheckedChange = {
                                if (it) onMarkPageForDeletion(
                                    paginationState.pageIndex,
                                    items.chunked(paginationState.pageSize)[paginationState.pageIndex - 1]
                                ) else onUnmarkPageForDeletion(
                                    paginationState.pageIndex,
                                    items.chunked(paginationState.pageSize)[paginationState.pageIndex - 1]
                                )
                            })
                        }
                        column { Text(text = "NAME") }
                        column { Text(text = "SIZE") }
                        column { Text(text = "LAST MODIFIED") }
                    },
                    paginationState = paginationState,
                    onPageChanged = {
                        items.chunked(it.pageSize)[it.pageIndex - 1]
                    },
                    cellPadding = PaddingValues(0.dp),
                    footer = {
                        TableFooter(
                            selectedCount = itemsToDelete.count(),
                            selectedSize = itemsToDelete.sumOf { it.sizeBytes }.toHumanReadableSize(),
                            paginationState = paginationState,
                            onDelete = { },
                        )
                    }
                ) { item ->
                    row(modifier = if (item.storageType == StorageType.Directory) Modifier.clickable {
                        onDirectoryClick(
                            item.path
                        )
                    } else Modifier) {
                        cell {
                            Checkbox(
                                checked = itemsToDelete.contains(item),
                                onCheckedChange = { onToggleItemDelete(item) }
                            )
                        }
                        cell { Text(text = item.name) }
                        cell { Text(text = item.allocatedSizeBytes.toHumanReadableSize()) }
                        cell { Text(text = "TODO") }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbBar(
    rootPath: String,
    currentPath: String,
    onPathClick: (String) -> Unit
) {
    val segments = remember(currentPath) { parsePathToSegments(currentPath) }
    val listState = rememberLazyListState()

    val normalizedRoot = remember(rootPath) { rootPath.trimEnd('/', '\\') }

    LaunchedEffect(segments.size) {
        if (segments.isNotEmpty()) {
            listState.animateScrollToItem(segments.lastIndex)
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
        LazyRow(
            state = listState,
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(segments) { index, segment ->
                val isLast = index == segments.lastIndex

                val normalizedSegmentPath = segment.path.trimEnd('/', '\\')
                val isSafeToClick = normalizedSegmentPath.length >= normalizedRoot.length &&
                        normalizedSegmentPath.startsWith(normalizedRoot)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .then(
                            if (isSafeToClick) {
                                Modifier.clickable { onPathClick(segment.path) }
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {

                    val contentColor = when {
                        isLast -> MaterialTheme.colorScheme.primary
                        isSafeToClick -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                    if (index == 0) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Root",
                            modifier = Modifier.size(16.dp),
                            tint = contentColor
                        )
                    } else {
                        Text(
                            text = segment.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                if (!isLast) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TableFooter(
    selectedCount: Int,
    selectedSize: String,
    onDelete: () -> Unit,
    paginationState: PaginationState,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth().padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedCount > 0) {
                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = selectedSize,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text("No items selected", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Paginator(paginationState)

                Button(
                    onClick = onDelete,
                    enabled = selectedCount > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        }
    }
}