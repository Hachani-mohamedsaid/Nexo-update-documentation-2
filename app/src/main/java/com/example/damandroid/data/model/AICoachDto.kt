package com.example.damandroid.data.model

data class WeeklyStatsDto(
    val workouts: Int,
    val goal: Int,
    val calories: Int,
    val minutes: Int,
    val streak: Int
)

data class SuggestionDto(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val time: String,
    val participants: Int,
    val matchScore: Int
)

data class WorkoutTipDto(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String
)

data class ChallengeDto(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val total: Int,
    val reward: String
)

data class AICoachOverviewDto(
    val weeklyStats: WeeklyStatsDto,
    val suggestions: List<SuggestionDto>,
    val workoutTips: List<WorkoutTipDto>,
    val challenges: List<ChallengeDto>
)

