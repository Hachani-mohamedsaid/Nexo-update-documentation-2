package com.example.damandroid.api

/**
 * DTOs pour les réponses de l'API Achievements
 * Correspondant à la documentation API fournie
 */

// Summary DTOs
data class AchievementSummaryDto(
    val level: LevelInfoDto,
    val stats: StatsInfoDto
)

data class LevelInfoDto(
    val currentLevel: Int,
    val totalXp: Int,
    val xpForNextLevel: Int,
    val currentLevelXp: Int,
    val progressPercentage: Double
)

data class StatsInfoDto(
    val totalBadges: Int,
    val currentStreak: Int,
    val bestStreak: Int
)

// Badges DTOs
data class BadgesResponseDto(
    val earnedBadges: List<EarnedBadgeDto>,
    val inProgress: List<BadgeProgressDto>
)

data class EarnedBadgeDto(
    val _id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val rarity: String,
    val category: String,
    val earnedAt: String
)

data class BadgeProgressDto(
    val badge: BadgeInfoDto,
    val currentProgress: Int,
    val target: Int,
    val percentage: Double
)

data class BadgeInfoDto(
    val _id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val rarity: String,
    val category: String
)

// Challenges DTOs
data class ChallengesResponseDto(
    val activeChallenges: List<ActiveChallengeDto>
)

data class ActiveChallengeDto(
    val _id: String,
    val name: String,
    val description: String,
    val challengeType: String,
    val xpReward: Int,
    val currentProgress: Int,
    val target: Int,
    val daysLeft: Int,
    val expiresAt: String
)

// Leaderboard DTOs
data class LeaderboardResponseDto(
    val currentUser: CurrentUserLeaderboardDto?,
    val leaderboard: List<LeaderboardEntryDto>,
    val page: Int,
    val totalPages: Int
)

data class CurrentUserLeaderboardDto(
    val rank: Int,
    val username: String,
    val totalXp: Int,
    val isCurrentUser: Boolean
)

data class LeaderboardEntryDto(
    val rank: Int,
    val username: String,
    val totalXp: Int,
    val medal: String? = null
)

// Notifications DTOs
data class AchievementNotificationsResponseDto(
    val notifications: List<AchievementNotificationDto>,
    val total: Int,
    val unreadCount: Int,
    val page: Int,
    val totalPages: Int
)

data class AchievementNotificationDto(
    val _id: String,
    val userId: String,
    val type: String, // badge_unlocked, level_up, xp_earned, challenge_completed, streak_updated
    val title: String,
    val message: String,
    val isRead: Boolean,
    val metadata: Map<String, Any>? = null,
    val createdAt: String,
    val readAt: String? = null,
    val updatedAt: String
)

data class SuccessResponseDto(
    val success: Boolean
)

