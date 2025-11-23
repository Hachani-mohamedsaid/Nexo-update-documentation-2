package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.AICoachRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.AICoachOverview
import com.example.damandroid.domain.repository.AICoachRepository

class AICoachRepositoryImpl(
    private val remoteDataSource: AICoachRemoteDataSource
) : AICoachRepository {
    override suspend fun getOverview(): AICoachOverview =
        remoteDataSource.fetchOverview().toDomain()
}

