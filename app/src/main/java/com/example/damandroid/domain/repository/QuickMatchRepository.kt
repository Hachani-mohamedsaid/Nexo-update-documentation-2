package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.MatchUserProfile

/**
 * Résultat d'un like (indique si c'est un match)
 */
data class LikeResult(
    val isMatch: Boolean,
    val matchedProfile: MatchUserProfile?
)

interface QuickMatchRepository {
    suspend fun getProfiles(): List<MatchUserProfile>
    suspend fun likeProfile(profileId: String): LikeResult
    suspend fun passProfile(profileId: String)
}

