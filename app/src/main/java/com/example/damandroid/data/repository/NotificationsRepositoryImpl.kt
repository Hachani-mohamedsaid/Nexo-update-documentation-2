package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.NotificationsRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.NotificationsOverview
import com.example.damandroid.domain.repository.NotificationsRepository

class NotificationsRepositoryImpl(
    private val remoteDataSource: NotificationsRemoteDataSource
) : NotificationsRepository {
    override suspend fun getNotifications(): NotificationsOverview =
        remoteDataSource.fetchNotifications().toDomain()

    override suspend fun markAsRead(notificationId: String) {
        remoteDataSource.markAsRead(notificationId)
    }

    override suspend fun markAllAsRead() {
        remoteDataSource.markAllAsRead()
    }

    override suspend fun likeBack(profileId: String): Boolean =
        remoteDataSource.likeBack(profileId)
}

