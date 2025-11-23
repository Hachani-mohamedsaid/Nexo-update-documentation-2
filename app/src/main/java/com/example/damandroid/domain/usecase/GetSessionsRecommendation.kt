package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.SessionsRecommendation
import com.example.damandroid.domain.repository.AISuggestionsRepository

class GetSessionsRecommendation(
    private val repository: AISuggestionsRepository
) {
    suspend operator fun invoke(): SessionsRecommendation = repository.getSessionsRecommendation()
}

