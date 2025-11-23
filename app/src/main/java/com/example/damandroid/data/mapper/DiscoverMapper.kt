package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.DiscoverOverviewDto
import com.example.damandroid.data.model.DiscoverSportCategoryDto
import com.example.damandroid.data.model.DiscoverUserDto
import com.example.damandroid.data.model.FeaturedCoachDto
import com.example.damandroid.data.model.TrendingActivityDto
import com.example.damandroid.domain.model.DiscoverOverview
import com.example.damandroid.domain.model.DiscoverSportCategory
import com.example.damandroid.domain.model.DiscoverUser
import com.example.damandroid.domain.model.FeaturedCoach
import com.example.damandroid.domain.model.TrendingActivity

fun DiscoverOverviewDto.toDomain(): DiscoverOverview = DiscoverOverview(
    featuredCoach = featuredCoach?.toDomain(),
    sportCategories = sportCategories.map(DiscoverSportCategoryDto::toDomain),
    trendingActivities = trendingActivities.map(TrendingActivityDto::toDomain),
    activeUsers = activeUsers.map(DiscoverUserDto::toDomain)
)

private fun FeaturedCoachDto.toDomain(): FeaturedCoach = FeaturedCoach(
    id = id,
    name = name,
    title = title,
    rating = rating,
    reviewCount = reviewCount,
    avatarUrl = avatarUrl,
    badge = badge
)

private fun DiscoverSportCategoryDto.toDomain(): DiscoverSportCategory = DiscoverSportCategory(
    id = id,
    name = name,
    icon = icon,
    colorHex = colorHex
)

private fun TrendingActivityDto.toDomain(): TrendingActivity = TrendingActivity(
    id = id,
    title = title,
    sportIcon = sportIcon,
    date = date,
    time = time,
    participants = participants,
    maxParticipants = maxParticipants,
    location = location,
    hostName = hostName,
    hostAvatar = hostAvatar
)

private fun DiscoverUserDto.toDomain(): DiscoverUser = DiscoverUser(
    id = id,
    name = name,
    avatarUrl = avatarUrl,
    sport = sport,
    distance = distance
)

