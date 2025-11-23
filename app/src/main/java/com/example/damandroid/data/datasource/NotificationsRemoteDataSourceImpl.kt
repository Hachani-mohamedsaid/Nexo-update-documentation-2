package com.example.damandroid.data.datasource

import com.example.damandroid.api.LikeProfileRequest
import com.example.damandroid.api.LikeProfileResponse
import com.example.damandroid.api.LikesReceivedResponse
import com.example.damandroid.api.QuickMatchApiService
import com.example.damandroid.api.RetrofitClient
import com.example.damandroid.data.model.NotificationDto
import com.example.damandroid.data.model.NotificationsOverviewDto
import com.example.damandroid.data.datasource.AchievementsRemoteDataSource
import com.example.damandroid.data.datasource.AchievementsRemoteDataSourceImpl
import java.time.Instant

class NotificationsRemoteDataSourceImpl(
    private val quickMatchApiService: QuickMatchApiService = RetrofitClient.quickMatchApiService,
    private val achievementsDataSource: AchievementsRemoteDataSource = AchievementsRemoteDataSourceImpl()
) : NotificationsRemoteDataSource {

    override suspend fun fetchNotifications(): NotificationsOverviewDto {
        // Récupérer les likes reçus depuis l'API
        val likesReceived = try {
            val response = quickMatchApiService.getLikesReceived()
            if (response.isSuccessful) {
                response.body()?.likes ?: emptyList()
            } else {
                android.util.Log.e("NotificationsDataSource", "Error fetching likes received: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationsDataSource", "Error fetching likes received: ${e.message}", e)
            emptyList()
        }

        // Convertir les likes reçus en notifications
        val likeNotifications = likesReceived.map { like ->
            NotificationDto(
                id = "like_${like.likeId}",
                type = "like",
                title = if (like.isMatch) "It's a Match! 🎉" else "New Like",
                message = "${like.fromUser.name} ${if (like.isMatch) "matched with you!" else "liked your profile"}",
                timestampIso = like.createdAt,
                isRead = false, // Les likes reçus sont considérés comme non lus
                metadata = mapOf(
                    "fromUserId" to like.fromUser.getUserId(),
                    "fromUserName" to like.fromUser.name,
                    "fromUserAvatar" to (like.fromUser.getAvatar() ?: ""),
                    "isMatch" to like.isMatch.toString(),
                    "matchId" to (like.matchId ?: "")
                )
            )
        }

        // Récupérer les notifications d'achievements depuis l'API Achievements
        val achievementNotifications = try {
            val achievementResponse = achievementsDataSource.fetchNotifications(page = 1, limit = 50, unreadOnly = false)
            achievementResponse.notifications.map { achievementNotif ->
                // Convertir les notifications d'achievements en NotificationDto
                NotificationDto(
                    id = "achievement_${achievementNotif.id}",
                    type = when (achievementNotif.type) {
                        "badge_unlocked" -> "achievement"
                        "xp_earned" -> "xp_earned"
                        "level_up" -> "level_up"
                        "challenge_completed" -> "challenge_completed"
                        "streak_updated" -> "streak_updated"
                        else -> "achievement"
                    },
                    title = achievementNotif.title,
                    message = achievementNotif.message,
                    timestampIso = achievementNotif.createdAt,
                    isRead = achievementNotif.isRead,
                    metadata = when (achievementNotif.type) {
                        "badge_unlocked" -> mapOf(
                            "badgeName" to (achievementNotif.metadata?.get("badgeName")?.toString() ?: ""),
                            "badgeIcon" to (achievementNotif.metadata?.get("badgeIcon")?.toString() ?: "")
                        )
                        "xp_earned" -> mapOf(
                            "xpAmount" to (achievementNotif.metadata?.get("xpAmount")?.toString() ?: "0")
                        )
                        "level_up" -> mapOf(
                            "oldLevel" to (achievementNotif.metadata?.get("oldLevel")?.toString() ?: "0"),
                            "newLevel" to (achievementNotif.metadata?.get("newLevel")?.toString() ?: "0"),
                            "totalXp" to (achievementNotif.metadata?.get("totalXp")?.toString() ?: "0")
                        )
                        "challenge_completed" -> mapOf(
                            "challengeName" to (achievementNotif.metadata?.get("challengeName")?.toString() ?: ""),
                            "xpReward" to (achievementNotif.metadata?.get("xpReward")?.toString() ?: "0")
                        )
                        else -> emptyMap()
                    }
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationsDataSource", "Error fetching achievement notifications: ${e.message}")
            emptyList()
        }

        // Combiner toutes les notifications (likes + achievements)
        val allNotifications = likeNotifications + achievementNotifications

        val unread = allNotifications.count { !it.isRead }
        return NotificationsOverviewDto(
            unreadCount = unread,
            notifications = allNotifications.sortedByDescending { it.timestampIso }
        )
    }

    override suspend fun markAsRead(notificationId: String) {
        // Si c'est une notification d'achievement, appeler l'API Achievements
        if (notificationId.startsWith("achievement_")) {
            try {
                val actualId = notificationId.removePrefix("achievement_")
                achievementsDataSource.markNotificationAsRead(actualId)
            } catch (e: Exception) {
                android.util.Log.e("NotificationsDataSource", "Error marking achievement notification as read: ${e.message}", e)
            }
        }
        // Pour les notifications de likes, on peut les marquer comme lues localement
        // TODO: Implémenter l'appel API si le backend le supporte
        // Pour l'instant, on ne fait rien car les notifications sont rechargées depuis l'API à chaque fois
        android.util.Log.d("NotificationsDataSource", "Marking notification as read: $notificationId")
    }

    override suspend fun markAllAsRead() {
        // Marquer toutes les notifications d'achievements comme lues
        try {
            achievementsDataSource.markAllNotificationsAsRead()
        } catch (e: Exception) {
            android.util.Log.e("NotificationsDataSource", "Error marking all achievement notifications as read: ${e.message}", e)
        }
        // TODO: Marquer toutes les notifications de likes comme lues si le backend le supporte
        // Pour l'instant, on ne fait rien car les notifications sont rechargées depuis l'API à chaque fois
        android.util.Log.d("NotificationsDataSource", "Marking all notifications as read")
    }

    override suspend fun likeBack(profileId: String): Boolean {
        return try {
            val response = quickMatchApiService.likeProfile(LikeProfileRequest(profileId))
            if (response.isSuccessful) {
                val likeResponse = response.body()
                val isMatch = likeResponse?.isMatch ?: false
                android.util.Log.d("NotificationsDataSource", "Like back successful. isMatch: $isMatch")
                isMatch
            } else {
                android.util.Log.e("NotificationsDataSource", "Error liking back: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationsDataSource", "Error liking back: ${e.message}", e)
            false
        }
    }
}
