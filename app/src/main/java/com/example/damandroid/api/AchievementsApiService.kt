package com.example.damandroid.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AchievementsApiService {
    
    /**
     * GET /achievements/summary
     * Retourne un résumé des achievements de l'utilisateur : niveau, XP, badges et streaks.
     */
    @GET("achievements/summary")
    suspend fun getSummary(): Response<AchievementSummaryDto>
    
    /**
     * GET /achievements/badges
     * Retourne les badges obtenus et les badges en cours de progression.
     */
    @GET("achievements/badges")
    suspend fun getBadges(): Response<BadgesResponseDto>
    
    /**
     * GET /achievements/challenges
     * Retourne les défis actifs de l'utilisateur avec leur progression.
     */
    @GET("achievements/challenges")
    suspend fun getChallenges(): Response<ChallengesResponseDto>
    
    /**
     * GET /achievements/leaderboard?page=1&limit=20
     * Retourne le classement avec la position de l'utilisateur actuel.
     */
    @GET("achievements/leaderboard")
    suspend fun getLeaderboard(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<LeaderboardResponseDto>
    
    /**
     * GET /achievements/notifications?page=1&limit=20&unreadOnly=false
     * Récupère les notifications d'achievements de l'utilisateur.
     */
    @GET("achievements/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("unreadOnly") unreadOnly: Boolean = false
    ): Response<AchievementNotificationsResponseDto>
    
    /**
     * POST /achievements/notifications/:id/read
     * Marque une notification comme lue.
     */
    @POST("achievements/notifications/{id}/read")
    suspend fun markNotificationAsRead(
        @Path("id") notificationId: String
    ): Response<SuccessResponseDto>
    
    /**
     * POST /achievements/notifications/read-all
     * Marque toutes les notifications comme lues.
     */
    @POST("achievements/notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<SuccessResponseDto>
}

