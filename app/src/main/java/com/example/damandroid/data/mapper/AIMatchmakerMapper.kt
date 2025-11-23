package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.MatchmakerProfileDto
import com.example.damandroid.data.model.MatchmakerRecommendationDto
import com.example.damandroid.domain.model.MatchmakerProfile
import com.example.damandroid.domain.model.MatchmakerRecommendation

fun MatchmakerRecommendationDto.toDomain(): MatchmakerRecommendation = MatchmakerRecommendation(
    profiles = profiles.map(MatchmakerProfileDto::toDomain),
    totalMatches = totalMatches,
    newMatchesToday = newMatchesToday
)

private fun MatchmakerProfileDto.toDomain(): MatchmakerProfile = MatchmakerProfile(
    id = id,
    name = name,
    age = age,
    avatarUrl = avatarUrl,
    coverImageUrl = coverImageUrl,
    location = location,
    distance = distance,
    bio = bio,
    sports = sports,
    tags = tags,
    rating = rating
)

