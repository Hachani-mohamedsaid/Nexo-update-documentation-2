package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.MatchmakerRecommendation

interface AIMatchmakerRepository {
    suspend fun getRecommendations(): MatchmakerRecommendation
}

