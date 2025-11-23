package com.example.damandroid.domain.model

data class MatchmakerProfile(
    val id: String,
    val name: String,
    val age: Int,
    val avatarUrl: String,
    val coverImageUrl: String,
    val location: String,
    val distance: String,
    val bio: String,
    val sports: List<String>,
    val tags: List<String>,
    val rating: Double
)

data class MatchmakerRecommendation(
    val profiles: List<MatchmakerProfile>,
    val totalMatches: Int,
    val newMatchesToday: Int
)

