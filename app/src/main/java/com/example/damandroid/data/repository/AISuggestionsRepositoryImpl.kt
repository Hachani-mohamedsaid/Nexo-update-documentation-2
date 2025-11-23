package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.AISuggestionsRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.SessionsRecommendation
import com.example.damandroid.domain.repository.AISuggestionsRepository

class AISuggestionsRepositoryImpl(
    private val remoteDataSource: AISuggestionsRemoteDataSource
) : AISuggestionsRepository {
    override suspend fun getSessionsRecommendation(): SessionsRecommendation =
        remoteDataSource.fetchSessions().toDomain()
}

