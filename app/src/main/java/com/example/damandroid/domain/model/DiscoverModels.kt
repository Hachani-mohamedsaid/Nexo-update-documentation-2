package com.example.damandroid.domain.model

data class DiscoverSportCategory(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String
)

data class FeaturedCoach(
    val id: String,
    val name: String,
    val title: String,
    val rating: Double,
    val reviewCount: Int,
    val avatarUrl: String,
    val badge: String
)

data class TrendingActivity(
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

data class DiscoverUser(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val sport: String,
    val distance: String
)

data class DiscoverOverview(
    val featuredCoach: FeaturedCoach?,
    val sportCategories: List<DiscoverSportCategory>,
    val trendingActivities: List<TrendingActivity>,
    val activeUsers: List<DiscoverUser>
)

