package com.example.damandroid.data.model

data class UserProfileDto(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val bio: String,
    val location: String,
    val isVerified: Boolean,
    val stats: UserStatsDto,
    val achievements: List<AchievementDto>,
    val activities: List<ProfileActivityDto>,
    val medals: List<ProfileMedalDto>
)

data class UserStatsDto(
    val sessionsJoined: Int,
    val sessionsHosted: Int,
    val followers: Int,
    val following: Int,
    val favoriteSports: List<String>,
    val rating: Double? = null // Rating from backend, null if not available
)

data class AchievementDto(
    val id: String,
    val title: String,
    val description: String,
    val icon: String
)

data class ProfileActivityDto(
    val id: String,
    val title: String,
    val sportIcon: String,
    val date: String,
    val time: String,
    val location: String,
    val status: String
)

data class ProfileMedalDto(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val rarity: String
)

