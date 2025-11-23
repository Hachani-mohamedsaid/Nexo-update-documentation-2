package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.SessionsRecommendationDto

interface AISuggestionsRemoteDataSource {
    suspend fun fetchSessions(): SessionsRecommendationDto
}

