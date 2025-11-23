package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.MatchmakerRecommendation
import com.example.damandroid.domain.repository.AIMatchmakerRepository

class GetMatchmakerRecommendations(
    private val repository: AIMatchmakerRepository
) {
    suspend operator fun invoke(): MatchmakerRecommendation = repository.getRecommendations()
}

