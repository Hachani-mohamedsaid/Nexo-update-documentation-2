package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.AchievementsRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.AchievementBadge
import com.example.damandroid.domain.model.AchievementChallenge
import com.example.damandroid.domain.model.AchievementsOverview
import com.example.damandroid.domain.model.LeaderboardResponse
import com.example.damandroid.domain.model.AchievementNotificationsResponse
import com.example.damandroid.domain.repository.AchievementsRepository
import com.example.damandroid.data.mapper.toDomain

class AchievementsRepositoryImpl(
    private val remoteDataSource: AchievementsRemoteDataSource
) : AchievementsRepository {
    override suspend fun getOverview(): AchievementsOverview =
        remoteDataSource.fetchOverview().toDomain()
    
    override suspend fun getSummary(): AchievementsOverview =
        remoteDataSource.fetchSummary().toDomain()
    
    override suspend fun getBadges(): List<AchievementBadge> =
        remoteDataSource.fetchBadges().map { it.toDomain() }
    
    override suspend fun getChallenges(): List<AchievementChallenge> =
        remoteDataSource.fetchChallenges().map { it.toDomain() }
    
    override suspend fun getLeaderboard(page: Int, limit: Int): LeaderboardResponse =
        remoteDataSource.fetchLeaderboard(page, limit).toDomain()
    
    override suspend fun getNotifications(page: Int, limit: Int, unreadOnly: Boolean): AchievementNotificationsResponse =
        remoteDataSource.fetchNotifications(page, limit, unreadOnly).toDomain()
    
    override suspend fun markNotificationAsRead(notificationId: String): Boolean =
        remoteDataSource.markNotificationAsRead(notificationId)
    
    override suspend fun markAllNotificationsAsRead(): Boolean =
        remoteDataSource.markAllNotificationsAsRead()
}

