package com.example.damandroid.data.model

data class NotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val timestampIso: String,
    val isRead: Boolean,
    val metadata: Map<String, String>
)

data class NotificationsOverviewDto(
    val unreadCount: Int,
    val notifications: List<NotificationDto>
)

