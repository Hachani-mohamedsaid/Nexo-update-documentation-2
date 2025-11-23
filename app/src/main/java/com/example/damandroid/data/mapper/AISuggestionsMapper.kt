package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.SessionsRecommendationDto
import com.example.damandroid.data.model.SuggestedActivityDto
import com.example.damandroid.domain.model.SessionsRecommendation
import com.example.damandroid.domain.model.SuggestedActivity

fun SessionsRecommendationDto.toDomain(): SessionsRecommendation = SessionsRecommendation(
    upcoming = upcoming.map(SuggestedActivityDto::toDomain),
    recommended = recommended.map(SuggestedActivityDto::toDomain),
    mapActivities = mapActivities.map(SuggestedActivityDto::toDomain)
)

private fun SuggestedActivityDto.toDomain(): SuggestedActivity = SuggestedActivity(
    id = id,
    title = title,
    sport = sport,
    description = description,
    date = date,
    time = time,
    location = location,
    organizer = organizer,
    participantsCount = participantsCount,
    capacity = capacity,
    isRecommended = isRecommended
)

