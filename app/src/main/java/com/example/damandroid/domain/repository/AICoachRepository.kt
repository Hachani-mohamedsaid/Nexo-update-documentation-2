package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.AICoachOverview

interface AICoachRepository {
    suspend fun getOverview(): AICoachOverview
}

