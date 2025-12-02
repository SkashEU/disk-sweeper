package com.skash.sweeper.domain.repository

import com.skash.sweeper.domain.model.ScanProgress
import kotlinx.coroutines.flow.Flow

interface FileSystemRepository {

    fun scan(path: String): Flow<ScanProgress>
}