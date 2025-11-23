package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.AICoachOverviewDto
import com.example.damandroid.data.model.ChallengeDto
import com.example.damandroid.data.model.SuggestionDto
import com.example.damandroid.data.model.WeeklyStatsDto
import com.example.damandroid.data.model.WorkoutTipDto
import com.example.damandroid.domain.model.AICoachOverview
import com.example.damandroid.domain.model.Challenge
import com.example.damandroid.domain.model.Suggestion
import com.example.damandroid.domain.model.WeeklyStats
import com.example.damandroid.domain.model.WorkoutTip

fun AICoachOverviewDto.toDomain(): AICoachOverview = AICoachOverview(
    weeklyStats = weeklyStats.toDomain(),
    suggestions = suggestions.map(SuggestionDto::toDomain),
    workoutTips = workoutTips.map(WorkoutTipDto::toDomain),
    challenges = challenges.map(ChallengeDto::toDomain)
)

private fun WeeklyStatsDto.toDomain(): WeeklyStats = WeeklyStats(
    workouts = workouts,
    goal = goal,
    calories = calories,
    minutes = minutes,
    streak = streak
)

private fun SuggestionDto.toDomain(): Suggestion = Suggestion(
    id = id,
    title = title,
    description = description,
    icon = icon,
    time = time,
    participants = participants,
    matchScore = matchScore
)

private fun WorkoutTipDto.toDomain(): WorkoutTip = WorkoutTip(
    id = id,
    title = title,
    description = description,
    icon = icon,
    category = category
)

private fun ChallengeDto.toDomain(): Challenge = Challenge(
    id = id,
    title = title,
    description = description,
    progress = progress,
    total = total,
    reward = reward
)

