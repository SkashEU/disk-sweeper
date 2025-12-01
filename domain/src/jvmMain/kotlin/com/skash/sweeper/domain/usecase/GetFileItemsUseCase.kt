package com.skash.sweeper.domain.usecase

import com.skash.forge.usecase.UseCase
import com.skash.sweeper.domain.FileSystemAnalyzer
import com.skash.sweeper.domain.ScanResult
import kotlinx.coroutines.flow.Flow

class GetFileItemsUseCase(
    private val fileSystemAnalyzer: FileSystemAnalyzer
) : UseCase<String, ScanResult>() {

    override fun execute(params: String): Flow<ScanResult> = fileSystemAnalyzer.scan(params)

}