package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.MatchmakerRecommendationDto

interface AIMatchmakerRemoteDataSource {
    suspend fun fetchRecommendations(): MatchmakerRecommendationDto
}

