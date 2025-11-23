package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.HomeFeed
import com.example.damandroid.domain.repository.HomeFeedRepository

class GetMyActivities(
    private val repository: HomeFeedRepository
) {
    suspend operator fun invoke(): HomeFeed = repository.getMyActivities()
}

