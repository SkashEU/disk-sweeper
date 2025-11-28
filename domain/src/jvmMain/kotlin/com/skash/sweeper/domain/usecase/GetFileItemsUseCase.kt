package com.skash.sweeper.domain.usecase

import com.skash.forge.outcome.Outcome
import com.skash.forge.usecase.OutcomeUseCase
import com.skash.sweeper.domain.FileSystemAnalyzer
import com.skash.sweeper.domain.model.FileItem
import kotlinx.coroutines.flow.FlowCollector

class GetFileItemsUseCase(
    private val fileSystemAnalyzer: FileSystemAnalyzer
) : OutcomeUseCase<String, List<FileItem>, String>() {

    override suspend fun FlowCollector<Outcome<List<FileItem>, String>>.execute(
        params: String
    ) {
        val files = fileSystemAnalyzer.scan(params)
        emitSuccess(files)
    }
}