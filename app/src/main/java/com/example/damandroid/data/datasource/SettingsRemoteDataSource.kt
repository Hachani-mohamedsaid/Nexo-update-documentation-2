package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.UserSettingsDto

interface SettingsRemoteDataSource {
    suspend fun fetchSettings(): UserSettingsDto
    suspend fun updateToggle(itemId: String, enabled: Boolean): UserSettingsDto
}

