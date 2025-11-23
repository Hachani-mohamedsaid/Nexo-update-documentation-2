package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.repository.NotificationsRepository

class MarkAllNotificationsAsRead(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke() {
        repository.markAllAsRead()
    }
}

