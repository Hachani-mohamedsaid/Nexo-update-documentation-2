package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.AICoachOverview
import com.example.damandroid.domain.repository.AICoachRepository

class GetAICoachOverview(
    private val repository: AICoachRepository
) {
    suspend operator fun invoke(): AICoachOverview = repository.getOverview()
}

