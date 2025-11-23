package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.AchievementBadge
import com.example.damandroid.domain.model.AchievementChallenge
import com.example.damandroid.domain.model.AchievementsOverview
import com.example.damandroid.domain.model.LeaderboardResponse
import com.example.damandroid.domain.model.AchievementNotificationsResponse

interface AchievementsRepository {
    suspend fun getOverview(): AchievementsOverview
    suspend fun getSummary(): AchievementsOverview
    suspend fun getBadges(): List<AchievementBadge>
    suspend fun getChallenges(): List<AchievementChallenge>
    suspend fun getLeaderboard(page: Int = 1, limit: Int = 20): LeaderboardResponse
    suspend fun getNotifications(page: Int = 1, limit: Int = 20, unreadOnly: Boolean = false): AchievementNotificationsResponse
    suspend fun markNotificationAsRead(notificationId: String): Boolean
    suspend fun markAllNotificationsAsRead(): Boolean
}

