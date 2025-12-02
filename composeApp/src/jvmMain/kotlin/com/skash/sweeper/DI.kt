package com.skash.sweeper

import com.skash.forge.event.DefaultEventBus
import com.skash.forge.event.EventBus
import com.skash.forge.navigation.NavigationDispatcher
import com.skash.forge.navigation.nav2.DefaultNavigationDispatcher
import com.skash.sweeper.data.repository.FileSystemRepositoryImpl
import com.skash.sweeper.domain.repository.FileSystemRepository
import com.skash.sweeper.domain.usecase.GetFileItemsUseCase
import com.skash.sweeper.feature.DirectoryPickerViewModel
import com.skash.sweeper.feature.launch.LaunchViewModel
import com.skash.sweeper.feature.scanner.ScannerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val diModule = module {
    single<FileSystemRepository> { FileSystemRepositoryImpl() }
    single<NavigationDispatcher> { DefaultNavigationDispatcher() }
    single<EventBus<UIEvent>> { DefaultEventBus() }
    factory { GetFileItemsUseCase(get()) }

    viewModelOf(::DirectoryPickerViewModel)
    viewModelOf(::LaunchViewModel)
    viewModelOf(::ScannerViewModel)

}