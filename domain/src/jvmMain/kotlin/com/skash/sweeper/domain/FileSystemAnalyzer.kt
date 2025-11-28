package com.skash.sweeper.domain

import com.skash.sweeper.domain.model.FileItem

interface FileSystemAnalyzer {

    suspend fun scan(path: String): List<FileItem>
    
}