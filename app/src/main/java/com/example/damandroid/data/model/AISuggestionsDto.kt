package com.example.damandroid.data.model

data class SuggestedActivityDto(
    val id: String,
    val title: String,
    val sport: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val organizer: String,
    val participantsCount: Int,
    val capacity: Int,
    val isRecommended: Boolean
)

data class SessionsRecommendationDto(
    val upcoming: List<SuggestedActivityDto>,
    val recommended: List<SuggestedActivityDto>,
    val mapActivities: List<SuggestedActivityDto>
)

