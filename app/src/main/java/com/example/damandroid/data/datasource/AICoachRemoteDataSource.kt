package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.AICoachOverviewDto

interface AICoachRemoteDataSource {
    suspend fun fetchOverview(): AICoachOverviewDto
}

