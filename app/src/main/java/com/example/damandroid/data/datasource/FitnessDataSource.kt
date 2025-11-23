package com.example.damandroid.data.datasource

import android.content.Context
import android.util.Log

/**
 * Interface pour les sources de données fitness
 * Permet d'utiliser différentes applications (Google Fit, Health Connect, Strava, etc.)
 */
interface FitnessDataSource {
    /**
     * Vérifie si l'application est disponible et installée
     */
    suspend fun isAvailable(): Boolean
    
    /**
     * Vérifie si l'utilisateur est connecté
     */
    suspend fun isConnected(): Boolean
    
    /**
     * Demande les permissions
     */
    suspend fun requestPermissions(activity: android.app.Activity): Boolean
    
    /**
     * Récupère les statistiques hebdomadaires
     */
    suspend fun getWeeklyStats(): WeeklyStats
    
    /**
     * Nom de l'application
     */
    fun getAppName(): String
    
    /**
     * Package name de l'application
     */
    fun getPackageName(): String
}

/**
 * Statistiques hebdomadaires
 */
data class WeeklyStats(
    val workouts: Int,
    val calories: Int,
    val minutes: Int,
    val streak: Int
)
