package com.skash.sweeper.data.repository

import com.skash.sweeper.data.mapper.toDomain
import com.skash.sweeper.domain.model.ScanProgress
import com.skash.sweeper.domain.repository.FileSystemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import uniffi.disk_sweeper_core.ScanListener
import uniffi.disk_sweeper_core.scanDirectory

class FileSystemRepositoryImpl : FileSystemRepository {

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

        scanDirectory(path, listener)

        awaitClose {}
    }.flowOn(Dispatchers.IO)
}