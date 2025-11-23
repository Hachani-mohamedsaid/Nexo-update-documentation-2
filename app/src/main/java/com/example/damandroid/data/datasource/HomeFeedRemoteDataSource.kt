package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.HomeFeedDto

interface HomeFeedRemoteDataSource {
    suspend fun fetchHomeFeed(): HomeFeedDto
    suspend fun fetchMyActivities(): HomeFeedDto
}

