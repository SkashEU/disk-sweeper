package com.skash.sweeper.domain.usecase

import com.skash.forge.usecase.UseCase
import com.skash.sweeper.domain.model.FileSystemEntry
import com.skash.sweeper.domain.repository.FileSystemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetFileItemsUseCase(
    private val fileSystemRepository: FileSystemRepository
) : UseCase<String, List<FileSystemEntry>>() {

    override fun execute(params: String): Flow<List<FileSystemEntry>> = flow {
        emit(fileSystemRepository.getFolderContent(params))
    }
}