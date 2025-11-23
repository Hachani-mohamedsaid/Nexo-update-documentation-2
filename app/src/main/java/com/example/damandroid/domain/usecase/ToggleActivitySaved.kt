package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.HomeFeed
import com.example.damandroid.domain.repository.HomeFeedRepository

class ToggleActivitySaved(
    private val repository: HomeFeedRepository
) {
    suspend operator fun invoke(activityId: String): HomeFeed =
        repository.toggleSaved(activityId)
}

