package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.CoachProfileDto

interface CoachProfileRemoteDataSource {
    suspend fun fetchCoachProfile(coachId: String): CoachProfileDto
}
