package com.example.damandroid.data.model

data class MatchmakerProfileDto(
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

data class MatchmakerRecommendationDto(
    val profiles: List<MatchmakerProfileDto>,
    val totalMatches: Int,
    val newMatchesToday: Int
)

