package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.CoachProfile

interface CoachProfileRepository {
    suspend fun getCoachProfile(coachId: String): CoachProfile
}

