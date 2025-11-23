package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.AIMatchmakerRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.MatchmakerRecommendation
import com.example.damandroid.domain.repository.AIMatchmakerRepository

class AIMatchmakerRepositoryImpl(
    private val remoteDataSource: AIMatchmakerRemoteDataSource
) : AIMatchmakerRepository {
    override suspend fun getRecommendations(): MatchmakerRecommendation =
        remoteDataSource.fetchRecommendations().toDomain()
}

