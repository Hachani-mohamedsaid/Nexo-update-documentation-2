package com.example.damandroid.data.mapper

import com.example.damandroid.api.ActiveChallengeDto
import com.example.damandroid.api.BadgeInfoDto
import com.example.damandroid.api.BadgeProgressDto
import com.example.damandroid.api.BadgesResponseDto
import com.example.damandroid.api.ChallengesResponseDto
import com.example.damandroid.api.CurrentUserLeaderboardDto as ApiCurrentUserLeaderboardDto
import com.example.damandroid.api.EarnedBadgeDto
import com.example.damandroid.api.LeaderboardEntryDto as ApiLeaderboardEntryDto
import com.example.damandroid.api.AchievementSummaryDto
import com.example.damandroid.api.LeaderboardResponseDto as ApiLeaderboardResponseDto
import com.example.damandroid.api.AchievementNotificationsResponseDto as ApiAchievementNotificationsResponseDto
import com.example.damandroid.api.AchievementNotificationDto as ApiAchievementNotificationDto
import com.example.damandroid.api.LevelInfoDto
import com.example.damandroid.api.StatsInfoDto
import com.example.damandroid.data.model.AchievementBadgeDto
import com.example.damandroid.data.model.AchievementChallengeDto
import com.example.damandroid.data.model.AchievementUserStatsDto
import com.example.damandroid.data.model.AchievementsOverviewDto
import com.example.damandroid.data.model.CurrentUserLeaderboardDto
import com.example.damandroid.data.model.LeaderboardEntryDto
import com.example.damandroid.data.model.LeaderboardResponseDto
import com.example.damandroid.data.model.AchievementNotificationDto
import com.example.damandroid.data.model.AchievementNotificationsResponseDto
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Mapper pour convertir les DTOs de l'API vers les DTOs du data layer
 */

fun AchievementSummaryDto.toDataOverview(): AchievementsOverviewDto {
    // Convertir LevelInfoDto vers AchievementUserStatsDto
    val stats = AchievementUserStatsDto(
        level = level.currentLevel,
        xp = level.totalXp,
        nextLevelXp = level.xpForNextLevel,
        currentLevelXp = level.currentLevelXp, // XP dans le niveau actuel
        totalBadges = this.stats.totalBadges,
        currentStreak = this.stats.currentStreak,
        longestStreak = this.stats.bestStreak
    )
    
    // Pour l'instant, retourner un overview vide avec juste les stats
    // Les badges, challenges et leaderboard seront chargés séparément
    return AchievementsOverviewDto(
        stats = stats,
        badges = emptyList(),
        challenges = emptyList(),
        leaderboard = emptyList()
    )
}

fun BadgesResponseDto.toDataBadges(): List<AchievementBadgeDto> {
    val result = mutableListOf<AchievementBadgeDto>()
    
    // Ajouter les badges obtenus
    earnedBadges.forEach { earned ->
        result.add(
            AchievementBadgeDto(
                id = earned._id,
                icon = extractIconFromUrl(earned.iconUrl) ?: "🏆",
                iconUrl = earned.iconUrl, // Garder l'URL de l'image
                title = earned.name,
                description = earned.description,
                category = earned.category,
                unlocked = true,
                unlockedDate = formatDate(earned.earnedAt),
                progress = null,
                total = null,
                rarity = earned.rarity
            )
        )
    }
    
    // Ajouter les badges en cours
    inProgress.forEach { progress ->
        result.add(
            AchievementBadgeDto(
                id = progress.badge._id,
                icon = extractIconFromUrl(progress.badge.iconUrl) ?: "🔒",
                iconUrl = progress.badge.iconUrl, // Garder l'URL de l'image
                title = progress.badge.name,
                description = progress.badge.description,
                category = progress.badge.category,
                unlocked = false,
                unlockedDate = null,
                progress = progress.currentProgress,
                total = progress.target,
                rarity = progress.badge.rarity
            )
        )
    }
    
    return result
}

fun ChallengesResponseDto.toDataChallenges(): List<AchievementChallengeDto> {
    return activeChallenges.map { challenge ->
        AchievementChallengeDto(
            id = challenge._id,
            title = challenge.name,
            description = challenge.description,
            challengeType = challenge.challengeType, // daily, weekly, monthly, event
            progress = challenge.currentProgress,
            total = challenge.target,
            reward = "${challenge.xpReward} XP",
            deadline = formatDate(challenge.expiresAt),
            daysLeft = challenge.daysLeft // Nombre de jours restants
        )
    }
}

fun ApiLeaderboardResponseDto.toDataLeaderboardResponse(): LeaderboardResponseDto {
    // Mapper currentUser si présent
    val currentUserDto = currentUser?.let { user ->
        CurrentUserLeaderboardDto(
            rank = user.rank,
            username = user.username,
            totalXp = user.totalXp,
            isCurrentUser = user.isCurrentUser
        )
    }
    
    // Mapper les entrées du leaderboard
    val leaderboardEntries = leaderboard.map { entry ->
        LeaderboardEntryDto(
            rank = entry.rank,
            name = entry.username,
            points = entry.totalXp,
            badge = entry.medal ?: ""
        )
    }
    
    return LeaderboardResponseDto(
        currentUser = currentUserDto,
        leaderboard = leaderboardEntries,
        page = page,
        totalPages = totalPages
    )
}

// Fonction legacy pour la rétrocompatibilité (utilisée dans fetchOverview)
fun LeaderboardResponseDto.toDataLeaderboard(): List<LeaderboardEntryDto> {
    val result = mutableListOf<LeaderboardEntryDto>()
    
    // Ajouter l'utilisateur actuel s'il existe
    currentUser?.let { user ->
        result.add(
            LeaderboardEntryDto(
                rank = user.rank,
                name = "You",
                points = user.totalXp,
                badge = when (user.rank) {
                    1 -> "🥇"
                    2 -> "🥈"
                    3 -> "🥉"
                    else -> ""
                }
            )
        )
    }
    
    // Ajouter les autres entrées du leaderboard
    result.addAll(leaderboard)
    
    return result.sortedBy { it.rank }
}

private fun extractIconFromUrl(iconUrl: String): String? {
    // Si l'URL contient un emoji ou un nom d'icône, extraire
    // Pour l'instant, on retourne null et on utilisera un emoji par défaut
    // Vous pouvez améliorer cette logique selon vos besoins
    return null
}

private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString // Retourner la date brute en cas d'erreur
    }
}

// Mappers pour les notifications
fun ApiAchievementNotificationsResponseDto.toDataNotificationsResponse(): AchievementNotificationsResponseDto {
    return AchievementNotificationsResponseDto(
        notifications = notifications.map { it.toDataNotification() },
        total = total,
        unreadCount = unreadCount,
        page = page,
        totalPages = totalPages
    )
}

fun ApiAchievementNotificationDto.toDataNotification(): AchievementNotificationDto {
    return AchievementNotificationDto(
        id = _id,
        userId = userId,
        type = type,
        title = title,
        message = message,
        isRead = isRead,
        metadata = metadata,
        createdAt = createdAt,
        readAt = readAt,
        updatedAt = updatedAt
    )
}

