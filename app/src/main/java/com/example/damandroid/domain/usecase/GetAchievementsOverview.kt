package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.AchievementsOverview
import com.example.damandroid.domain.repository.AchievementsRepository

class GetAchievementsOverview(
    private val repository: AchievementsRepository
) {
    suspend operator fun invoke(): AchievementsOverview = repository.getOverview()
}

