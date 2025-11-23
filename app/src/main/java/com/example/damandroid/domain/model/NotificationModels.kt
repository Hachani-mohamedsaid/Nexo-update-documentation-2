package com.example.damandroid.domain.model

import java.time.OffsetDateTime

sealed class NotificationItem(
    open val id: String,
    open val title: String,
    open val message: String,
    open val timestamp: OffsetDateTime,
    open val isRead: Boolean
) {
    data class SessionInvite(
        override val id: String,
        override val title: String,
        override val message: String,
        override val timestamp: OffsetDateTime,
        override val isRead: Boolean,
        val sessionId: String,
        val hostName: String,
        val sessionTime: String
    ) : NotificationItem(id, title, message, timestamp, isRead)

    data class ActivityReminder(
        override val id: String,
        override val title: String,
        override val message: String,
        override val timestamp: OffsetDateTime,
        override val isRead: Boolean,
        val activityId: String,
        val activityName: String,
        val activityTime: String
    ) : NotificationItem(id, title, message, timestamp, isRead)

    data class AchievementUnlocked(
        override val id: String,
        override val title: String,
        override val message: String,
        override val timestamp: OffsetDateTime,
        override val isRead: Boolean,
        val badgeName: String,
        val badgeIcon: String
    ) : NotificationItem(id, title, message, timestamp, isRead)

    data class XpEarned(
        override val id: String,
        override val title: String,
        override val message: String,
        override val timestamp: OffsetDateTime,
        override val isRead: Boolean,
        val xpAmount: Int
    ) : NotificationItem(id, title, message, timestamp, isRead)

    data class LevelUp(
        override val id: String,
        override val title: String,
        override val message: String,
        override val timestamp: OffsetDateTime,
        override val isRead: Boolean,
        val oldLevel: Int,
        val newLevel: Int,
        val totalXp: Int
    ) : NotificationItem(id, title, message, timestamp, isRead)

    data class ChallengeCompleted(
        override val id: String,
        override val title: String,
        override val message: String,
        override val timestamp: OffsetDateTime,
        override val isRead: Boolean,
        val challengeName: String,
        val xpReward: Int
    ) : NotificationItem(id, title, message, timestamp, isRead)

    data class SystemMessage(
        override val id: String,
        override val title: String,
        override val message: String,
        override val timestamp: OffsetDateTime,
        override val isRead: Boolean
    ) : NotificationItem(id, title, message, timestamp, isRead)

    data class LikeNotification(
        override val id: String,
        override val title: String,
        override val message: String,
        override val timestamp: OffsetDateTime,
        override val isRead: Boolean,
        val fromUserId: String,
        val fromUserName: String,
        val fromUserAvatar: String?,
        val isMatch: Boolean, // true si l'utilisateur a déjà liké ce profil en retour
        val matchId: String? // ID du match si c'est un match
    ) : NotificationItem(id, title, message, timestamp, isRead)
}

data class NotificationsOverview(
    val unreadCount: Int,
    val notifications: List<NotificationItem>
)

