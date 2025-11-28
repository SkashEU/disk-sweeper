package com.skash.sweeper.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppScreen {

    @Serializable
    data object Launch : AppScreen

    @Serializable
    data class Scanner(val path: String) : AppScreen

    @Serializable
    data object DirectorySelection : AppScreen
    @Serializable
    data object Overview : AppScreen
    @Serializable
    data object Settings: AppScreen
}