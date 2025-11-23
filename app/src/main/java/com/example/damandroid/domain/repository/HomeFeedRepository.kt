package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.HomeFeed

interface HomeFeedRepository {
    suspend fun getHomeFeed(): HomeFeed
    suspend fun getMyActivities(): HomeFeed
    suspend fun toggleSaved(activityId: String): HomeFeed
}

