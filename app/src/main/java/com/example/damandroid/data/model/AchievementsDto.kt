package com.example.damandroid.data.model

data class AchievementUserStatsDto(
    val level: Int,
    val xp: Int, // XP total
    val nextLevelXp: Int, // XP nécessaire pour le niveau suivant (dans le niveau actuel)
    val currentLevelXp: Int = 0, // XP dans le niveau actuel
    val totalBadges: Int,
    val currentStreak: Int,
    val longestStreak: Int
)

data class AchievementBadgeDto(
    val id: String,
    val icon: String,
    val iconUrl: String? = null, // URL de l'image du badge depuis l'API
    val title: String,
    val description: String,
    val category: String,
    val unlocked: Boolean,
    val unlockedDate: String?,
    val progress: Int?,
    val total: Int?,
    val rarity: String
)

data class AchievementChallengeDto(
    val id: String,
    val title: String,
    val description: String,
    val challengeType: String, // daily, weekly, monthly, event
    val progress: Int,
    val total: Int,
    val reward: String,
    val deadline: String,
    val daysLeft: Int // Nombre de jours restants
)

data class LeaderboardEntryDto(
    val rank: Int,
    val name: String,
    val points: Int,
    val badge: String
)

data class CurrentUserLeaderboardDto(
    val rank: Int,
    val username: String,
    val totalXp: Int,
    val isCurrentUser: Boolean
)

data class LeaderboardResponseDto(
    val currentUser: CurrentUserLeaderboardDto?,
    val leaderboard: List<LeaderboardEntryDto>,
    val page: Int,
    val totalPages: Int
)

data class AchievementsOverviewDto(
    val stats: AchievementUserStatsDto,
    val badges: List<AchievementBadgeDto>,
    val challenges: List<AchievementChallengeDto>,
    val leaderboard: List<LeaderboardEntryDto>
)

// Notifications DTOs (data layer)
data class AchievementNotificationDto(
    val id: String,
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

data class AchievementNotificationsResponseDto(
    val notifications: List<AchievementNotificationDto>,
    val total: Int,
    val unreadCount: Int,
    val page: Int,
    val totalPages: Int
)

