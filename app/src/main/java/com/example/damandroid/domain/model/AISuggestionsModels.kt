package com.example.damandroid.domain.model

data class SuggestedActivity(
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

data class SessionsRecommendation(
    val upcoming: List<SuggestedActivity>,
    val recommended: List<SuggestedActivity>,
    val mapActivities: List<SuggestedActivity>
)

