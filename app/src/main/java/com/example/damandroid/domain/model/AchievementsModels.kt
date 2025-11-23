package com.example.damandroid.domain.model

data class AchievementUserStats(
    val level: Int,
    val xp: Int, // XP total
    val nextLevelXp: Int, // XP nécessaire pour le niveau suivant (dans le niveau actuel)
    val currentLevelXp: Int = 0, // XP dans le niveau actuel
    val totalBadges: Int,
    val currentStreak: Int,
    val longestStreak: Int
)

data class AchievementBadge(
    val id: String,
    val icon: String,
    val iconUrl: String? = null, // URL de l'image du badge depuis l'API
    val title: String,
    val description: String,
    val category: String,
    val unlocked: Boolean,
    val unlockedDate: String? = null,
    val progress: Int? = null,
    val total: Int? = null,
    val rarity: String
)

data class AchievementChallenge(
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

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val points: Int,
    val badge: String
)

data class CurrentUserLeaderboard(
    val rank: Int,
    val username: String,
    val totalXp: Int,
    val isCurrentUser: Boolean
)

data class LeaderboardResponse(
    val currentUser: CurrentUserLeaderboard?,
    val leaderboard: List<LeaderboardEntry>,
    val page: Int,
    val totalPages: Int
)

data class AchievementsOverview(
    val stats: AchievementUserStats,
    val badges: List<AchievementBadge>,
    val challenges: List<AchievementChallenge>,
    val leaderboard: List<LeaderboardEntry>
)

// Notifications Models (domain layer)
enum class AchievementNotificationType {
    BADGE_UNLOCKED,
    LEVEL_UP,
    XP_EARNED,
    CHALLENGE_COMPLETED,
    STREAK_UPDATED
}

data class AchievementNotification(
    val id: String,
    val userId: String,
    val type: AchievementNotificationType,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val metadata: Map<String, Any>? = null,
    val createdAt: String,
    val readAt: String? = null,
    val updatedAt: String
)

data class AchievementNotificationsResponse(
    val notifications: List<AchievementNotification>,
    val total: Int,
    val unreadCount: Int,
    val page: Int,
    val totalPages: Int
)

