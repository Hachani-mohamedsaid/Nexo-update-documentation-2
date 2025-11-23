package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.NotificationsOverview
import com.example.damandroid.domain.repository.NotificationsRepository

class GetNotifications(
    private val repository: NotificationsRepository
) {
    suspend operator fun invoke(): NotificationsOverview = repository.getNotifications()
}

