package com.skash.sweeper.domain.usecase

import com.skash.forge.usecase.UseCase
import com.skash.sweeper.domain.model.ScanProgress
import com.skash.sweeper.domain.repository.FileSystemRepository
import kotlinx.coroutines.flow.Flow

class GetFileItemsUseCase(
    private val fileSystemRepository: FileSystemRepository
) : UseCase<String, ScanProgress>() {

    override fun execute(params: String): Flow<ScanProgress> = fileSystemRepository.scan(params)

}