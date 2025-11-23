package com.example.damandroid.domain.model

data class CreateActivityForm(
    val sportType: SportCategory? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val participants: Int = 5,
    val level: SkillLevel? = null,
    val visibility: ActivityVisibility = ActivityVisibility.PUBLIC
)

enum class SkillLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class ActivityVisibility {
    PUBLIC,
    FRIENDS
}

data class CreateActivityResult(
    val activityId: String,
    val shareLink: String
)

