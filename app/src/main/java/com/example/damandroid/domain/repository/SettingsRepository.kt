package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.UserSettings

interface SettingsRepository {
    suspend fun getSettings(): UserSettings
    suspend fun updateToggle(itemId: String, enabled: Boolean): UserSettings
}

