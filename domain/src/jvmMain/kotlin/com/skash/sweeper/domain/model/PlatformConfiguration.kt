package com.skash.sweeper.domain.model

sealed class PlatformConfiguration(
    val fileSizeBase: Int
) {

    companion object {
        fun getPlatformConfiguration(): PlatformConfiguration = when {
            System.getProperty("os.name").lowercase().contains("win") -> Windows
            System.getProperty("os.name").lowercase().contains("mac") -> Mac
            System.getProperty("os.name").lowercase().contains("nix") || System.getProperty("os.name").lowercase().contains("nux") -> Linux
            else -> Unknown
        }
    }

    object Windows : PlatformConfiguration(1024)
    object Mac : PlatformConfiguration(1000)
    object Linux : PlatformConfiguration(1024)
    object Unknown : PlatformConfiguration(1024)
}