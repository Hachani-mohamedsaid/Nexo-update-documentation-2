package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.UserSettings
import com.example.damandroid.domain.repository.SettingsRepository

class UpdateSettingsToggle(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(itemId: String, enabled: Boolean): UserSettings =
        repository.updateToggle(itemId, enabled)
}

