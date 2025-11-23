package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.repository.NotificationsRepository

class MarkNotificationAsRead(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke(notificationId: String) {
        repository.markAsRead(notificationId)
    }
}

