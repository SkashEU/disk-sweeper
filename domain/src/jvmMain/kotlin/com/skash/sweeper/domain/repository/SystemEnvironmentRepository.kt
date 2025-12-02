package com.skash.sweeper.domain.repository

import com.skash.sweeper.domain.model.PlatformConfiguration

interface SystemEnvironmentRepository {

    fun getCurrentPlatformConfiguration(): PlatformConfiguration
}