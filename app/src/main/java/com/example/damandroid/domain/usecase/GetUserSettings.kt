package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.UserSettings
import com.example.damandroid.domain.repository.SettingsRepository

class GetUserSettings(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): UserSettings = repository.getSettings()
}

