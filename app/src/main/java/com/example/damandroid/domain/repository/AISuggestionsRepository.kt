package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.SessionsRecommendation

interface AISuggestionsRepository {
    suspend fun getSessionsRecommendation(): SessionsRecommendation
}

