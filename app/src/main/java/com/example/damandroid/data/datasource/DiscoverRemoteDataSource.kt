package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.DiscoverOverviewDto

interface DiscoverRemoteDataSource {
    suspend fun fetchOverview(): DiscoverOverviewDto
}

