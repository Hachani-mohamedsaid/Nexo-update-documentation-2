package com.example.damandroid.data.datasource

import com.example.damandroid.api.LikeProfileResponse
import com.example.damandroid.data.model.MatchUserProfileDto

interface QuickMatchRemoteDataSource {
    suspend fun fetchProfiles(): List<MatchUserProfileDto>
    suspend fun likeProfile(profileId: String): LikeProfileResponse
    suspend fun passProfile(profileId: String)
}

