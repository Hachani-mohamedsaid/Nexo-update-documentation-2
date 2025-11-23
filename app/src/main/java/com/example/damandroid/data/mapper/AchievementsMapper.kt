package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.AchievementBadgeDto
import com.example.damandroid.data.model.AchievementChallengeDto
import com.example.damandroid.data.model.AchievementUserStatsDto
import com.example.damandroid.data.model.AchievementsOverviewDto
import com.example.damandroid.data.model.CurrentUserLeaderboardDto
import com.example.damandroid.data.model.LeaderboardEntryDto
import com.example.damandroid.data.model.LeaderboardResponseDto
import com.example.damandroid.domain.model.AchievementBadge
import com.example.damandroid.domain.model.AchievementChallenge
import com.example.damandroid.domain.model.AchievementUserStats
import com.example.damandroid.domain.model.AchievementsOverview
import com.example.damandroid.domain.model.CurrentUserLeaderboard
import com.example.damandroid.domain.model.LeaderboardEntry
import com.example.damandroid.domain.model.LeaderboardResponse
import com.example.damandroid.data.model.AchievementNotificationDto
import com.example.damandroid.data.model.AchievementNotificationsResponseDto
import com.example.damandroid.domain.model.AchievementNotification
import com.example.damandroid.domain.model.AchievementNotificationType
import com.example.damandroid.domain.model.AchievementNotificationsResponse

fun AchievementsOverviewDto.toDomain(): AchievementsOverview = AchievementsOverview(
    stats = stats.toDomain(),
    badges = badges.map(AchievementBadgeDto::toDomain),
    challenges = challenges.map(AchievementChallengeDto::toDomain),
    leaderboard = leaderboard.map(LeaderboardEntryDto::toDomain)
)

private fun AchievementUserStatsDto.toDomain(): AchievementUserStats = AchievementUserStats(
    level = level,
    xp = xp,
    nextLevelXp = nextLevelXp,
    currentLevelXp = currentLevelXp,
    totalBadges = totalBadges,
    currentStreak = currentStreak,
    longestStreak = longestStreak
)

// Fonctions d'extension publiques pour les nouvelles méthodes
fun AchievementBadgeDto.toDomain(): AchievementBadge = AchievementBadge(
    id = id,
    icon = icon,
    iconUrl = iconUrl, // Transmettre l'URL de l'image
    title = title,
    description = description,
    category = category,
    unlocked = unlocked,
    unlockedDate = unlockedDate,
    progress = progress,
    total = total,
    rarity = rarity
)

fun AchievementChallengeDto.toDomain(): AchievementChallenge = AchievementChallenge(
    id = id,
    title = title,
    description = description,
    challengeType = challengeType, // daily, weekly, monthly, event
    progress = progress,
    total = total,
    reward = reward,
    deadline = deadline,
    daysLeft = daysLeft // Nombre de jours restants
)

fun LeaderboardEntryDto.toDomain(): LeaderboardEntry = LeaderboardEntry(
    rank = rank,
    name = name,
    points = points,
    badge = badge
)

fun CurrentUserLeaderboardDto.toDomain(): CurrentUserLeaderboard = CurrentUserLeaderboard(
    rank = rank,
    username = username,
    totalXp = totalXp,
    isCurrentUser = isCurrentUser
)

fun LeaderboardResponseDto.toDomain(): LeaderboardResponse = LeaderboardResponse(
    currentUser = currentUser?.toDomain(),
    leaderboard = leaderboard.map { it.toDomain() },
    page = page,
    totalPages = totalPages
)

// Mappers pour les notifications
fun AchievementNotificationsResponseDto.toDomain(): AchievementNotificationsResponse = AchievementNotificationsResponse(
    notifications = notifications.map { it.toDomain() },
    total = total,
    unreadCount = unreadCount,
    page = page,
    totalPages = totalPages
)

fun AchievementNotificationDto.toDomain(): AchievementNotification = AchievementNotification(
    id = id,
    userId = userId,
    type = parseNotificationType(type),
    title = title,
    message = message,
    isRead = isRead,
    metadata = metadata,
    createdAt = createdAt,
    readAt = readAt,
    updatedAt = updatedAt
)

private fun parseNotificationType(type: String): AchievementNotificationType {
    return when (type.lowercase()) {
        "badge_unlocked" -> AchievementNotificationType.BADGE_UNLOCKED
        "level_up" -> AchievementNotificationType.LEVEL_UP
        "xp_earned" -> AchievementNotificationType.XP_EARNED
        "challenge_completed" -> AchievementNotificationType.CHALLENGE_COMPLETED
        "streak_updated" -> AchievementNotificationType.STREAK_UPDATED
        else -> AchievementNotificationType.XP_EARNED // Par défaut
    }
}

