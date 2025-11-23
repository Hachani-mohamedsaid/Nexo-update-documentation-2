package com.example.damandroid.domain.model

data class WeeklyStats(
    val workouts: Int,
    val goal: Int,
    val calories: Int,
    val minutes: Int,
    val streak: Int
)

data class Suggestion(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val time: String,
    val participants: Int,
    val matchScore: Int
)

data class WorkoutTip(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val category: String
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val total: Int,
    val reward: String
)

data class AICoachOverview(
    val weeklyStats: WeeklyStats,
    val suggestions: List<Suggestion>,
    val workoutTips: List<WorkoutTip>,
    val challenges: List<Challenge>
)

