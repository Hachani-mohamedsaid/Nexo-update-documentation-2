package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.SettingsRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.UserSettings
import com.example.damandroid.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val remoteDataSource: SettingsRemoteDataSource
) : SettingsRepository {
    override suspend fun getSettings(): UserSettings =
        remoteDataSource.fetchSettings().toDomain()

    override suspend fun updateToggle(itemId: String, enabled: Boolean): UserSettings =
        remoteDataSource.updateToggle(itemId, enabled).toDomain()
}

