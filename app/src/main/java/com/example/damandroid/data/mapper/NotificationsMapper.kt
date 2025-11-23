package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.NotificationDto
import com.example.damandroid.data.model.NotificationsOverviewDto
import com.example.damandroid.domain.model.NotificationItem
import com.example.damandroid.domain.model.NotificationsOverview
import java.time.Instant
import java.time.ZoneOffset

fun NotificationsOverviewDto.toDomain(): NotificationsOverview = NotificationsOverview(
    unreadCount = unreadCount,
    notifications = notifications.map(NotificationDto::toDomain)
)

private fun NotificationDto.toDomain(): NotificationItem {
    val timestamp = Instant.parse(timestampIso).atOffset(ZoneOffset.UTC)
    return when (type) {
        "session_invite" -> NotificationItem.SessionInvite(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = isRead,
            sessionId = metadata["sessionId"].orEmpty(),
            hostName = metadata["hostName"].orEmpty(),
            sessionTime = metadata["sessionTime"].orEmpty()
        )

        "activity_reminder" -> NotificationItem.ActivityReminder(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = isRead,
            activityId = metadata["activityId"].orEmpty(),
            activityName = metadata["activityName"].orEmpty(),
            activityTime = metadata["activityTime"].orEmpty()
        )

        "achievement" -> NotificationItem.AchievementUnlocked(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = isRead,
            badgeName = metadata["badgeName"].orEmpty(),
            badgeIcon = metadata["badgeIcon"].orEmpty()
        )

        "xp_earned" -> NotificationItem.XpEarned(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = isRead,
            xpAmount = metadata["xpAmount"]?.toIntOrNull() ?: 0
        )

        "level_up" -> NotificationItem.LevelUp(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = isRead,
            oldLevel = metadata["oldLevel"]?.toIntOrNull() ?: 0,
            newLevel = metadata["newLevel"]?.toIntOrNull() ?: 0,
            totalXp = metadata["totalXp"]?.toIntOrNull() ?: 0
        )

        "challenge_completed" -> NotificationItem.ChallengeCompleted(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = isRead,
            challengeName = metadata["challengeName"].orEmpty(),
            xpReward = metadata["xpReward"]?.toIntOrNull() ?: 0
        )

        "streak_updated" -> NotificationItem.SystemMessage(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = isRead
        )

        "like" -> NotificationItem.LikeNotification(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = isRead,
            fromUserId = metadata["fromUserId"].orEmpty(),
            fromUserName = metadata["fromUserName"].orEmpty(),
            fromUserAvatar = metadata["fromUserAvatar"],
            isMatch = metadata["isMatch"]?.toBoolean() ?: false,
            matchId = metadata["matchId"]
        )

        else -> NotificationItem.SystemMessage(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            isRead = isRead
        )
    }
}

