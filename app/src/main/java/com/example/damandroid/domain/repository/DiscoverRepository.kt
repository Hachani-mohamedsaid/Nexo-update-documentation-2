package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.DiscoverOverview

interface DiscoverRepository {
    suspend fun getOverview(): DiscoverOverview
}

