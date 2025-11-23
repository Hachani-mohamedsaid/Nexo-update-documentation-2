package com.example.damandroid.data.model

data class DiscoverSportCategoryDto(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String
)

data class FeaturedCoachDto(
    val id: String,
    val name: String,
    val title: String,
    val rating: Double,
    val reviewCount: Int,
    val avatarUrl: String,
    val badge: String
)

data class TrendingActivityDto(
    val id: String,
    val title: String,
    val sportIcon: String,
    val date: String,
    val time: String,
    val participants: Int,
    val maxParticipants: Int,
    val location: String,
    val hostName: String,
    val hostAvatar: String
)

data class DiscoverUserDto(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val sport: String,
    val distance: String
)

data class DiscoverOverviewDto(
    val featuredCoach: FeaturedCoachDto?,
    val sportCategories: List<DiscoverSportCategoryDto>,
    val trendingActivities: List<TrendingActivityDto>,
    val activeUsers: List<DiscoverUserDto>
)

