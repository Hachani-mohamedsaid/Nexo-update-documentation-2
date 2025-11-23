package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.AchievementBadgeDto
import com.example.damandroid.data.model.AchievementChallengeDto
import com.example.damandroid.data.model.AchievementsOverviewDto
import com.example.damandroid.data.model.LeaderboardEntryDto
import com.example.damandroid.data.model.LeaderboardResponseDto
import com.example.damandroid.data.model.AchievementNotificationsResponseDto

interface AchievementsRemoteDataSource {
    suspend fun fetchOverview(): AchievementsOverviewDto
    suspend fun fetchSummary(): AchievementsOverviewDto
    suspend fun fetchBadges(): List<AchievementBadgeDto>
    suspend fun fetchChallenges(): List<AchievementChallengeDto>
    suspend fun fetchLeaderboard(page: Int = 1, limit: Int = 20): LeaderboardResponseDto
    suspend fun fetchNotifications(page: Int = 1, limit: Int = 20, unreadOnly: Boolean = false): AchievementNotificationsResponseDto
    suspend fun markNotificationAsRead(notificationId: String): Boolean
    suspend fun markAllNotificationsAsRead(): Boolean
}

