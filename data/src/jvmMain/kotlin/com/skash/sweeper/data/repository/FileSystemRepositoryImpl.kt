package com.skash.sweeper.data.repository

import com.skash.sweeper.data.mapper.toDomain
import com.skash.sweeper.domain.model.FileSystemEntry
import com.skash.sweeper.domain.model.ScanProgress
import com.skash.sweeper.domain.repository.FileSystemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import uniffi.disk_sweeper_core.DiskScanner
import uniffi.disk_sweeper_core.ScanListener

class FileSystemRepositoryImpl : FileSystemRepository {

    private val diskScanner = DiskScanner()

    override fun scan(path: String): Flow<ScanProgress> = callbackFlow {

        val listener = object : ScanListener {
            override fun onEvent(event: uniffi.disk_sweeper_core.ScanEvent) {
                val domainEvent = event.toDomain()
                trySend(domainEvent)
                if (domainEvent is ScanProgress.Finished) {
                    close()
                }
            }
        }

        diskScanner.startScan(path, listener)
        awaitClose {}
    }.flowOn(Dispatchers.IO)

    override suspend fun getFolderContent(path: String): List<FileSystemEntry> = withContext(Dispatchers.IO) {
        diskScanner.getFolderContent(path).map { it.toDomain() }
    }
}