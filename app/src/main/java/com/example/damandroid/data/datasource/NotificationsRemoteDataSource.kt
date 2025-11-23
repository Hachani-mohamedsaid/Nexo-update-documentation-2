package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.NotificationsOverviewDto

interface NotificationsRemoteDataSource {
    suspend fun fetchNotifications(): NotificationsOverviewDto
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead()
    suspend fun likeBack(profileId: String): Boolean // Retourne true si c'est un match
}

