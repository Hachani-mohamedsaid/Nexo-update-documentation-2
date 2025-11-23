package com.example.damandroid.data.datasource

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Gestionnaire de synchronisation Fitness (Strava ou autres applications)
 * Vérifie si une source de données fitness est connectée et synchronisée
 */
class FitnessSyncManager(private val context: Context) {

    companion object {
        private const val TAG = "FitnessSyncManager"
        private const val PREFS_NAME = "fitness_sync_prefs"
        private const val KEY_IS_SYNCED = "is_synced"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_SOURCE_NAME = "source_name"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val fitnessDataSourceManager = FitnessDataSourceManager(context)

    /**
     * Vérifie si une source de données fitness est disponible et connectée
     */
    suspend fun isFitnessSourceConnected(): Boolean {
        return try {
            fitnessDataSourceManager.isAnySourceConnected()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking fitness source connection", e)
            false
        }
    }
    

    /**
     * Vérifie si les données sont synchronisées
     */
    fun isSynced(): Boolean {
        return prefs.getBoolean(KEY_IS_SYNCED, false)
    }

    /**
     * Marque la synchronisation comme réussie
     */
    fun markAsSynced() {
        prefs.edit()
            .putBoolean(KEY_IS_SYNCED, true)
            .putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
            .apply()
        Log.d(TAG, "Google Fit marked as synced")
    }

    /**
     * Réinitialise la synchronisation (déconnexion)
     */
    fun resetSync() {
        prefs.edit()
            .putBoolean(KEY_IS_SYNCED, false)
            .putLong(KEY_LAST_SYNC_TIME, 0)
            .apply()
        Log.d(TAG, "Google Fit sync reset")
    }

    /**
     * Obtient le temps de la dernière synchronisation
     */
    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0)
    }

    /**
     * Vérifie si l'accès à AI Coach est autorisé
     * Permet l'accès même sans application fitness (l'utilisateur peut utiliser AI Coach avec des données par défaut)
     */
    suspend fun canAccessAICoach(): Boolean {
        // Permettre l'accès même si pas d'application fitness connectée
        // L'utilisateur peut toujours utiliser AI Coach
        val isConnected = isFitnessSourceConnected()
        val isSynced = isSynced()
        Log.d(TAG, "AI Coach access check: isConnected=$isConnected, isSynced=$isSynced - allowing access anyway")
        return true // Toujours permettre l'accès
    }

    /**
     * Obtient le message d'erreur si l'accès n'est pas autorisé
     */
    suspend fun getAccessErrorMessage(): String {
        val sourceName = fitnessDataSourceManager.getCurrentAppName()
        val isAvailable = fitnessDataSourceManager.isAnySourceAvailable()
        val isConnected = fitnessDataSourceManager.isAnySourceConnected()
        
        return when {
            !isAvailable -> {
                "Aucune application fitness n'est disponible sur votre appareil. " +
                "Veuillez installer Strava depuis le Play Store."
            }
            !isConnected -> {
                "Veuillez connecter Strava pour accéder à AI Coach. " +
                "Les données de workout, calories et minutes seront synchronisées depuis Strava."
            }
            !isSynced() -> {
                "Veuillez synchroniser vos données Strava pour accéder à AI Coach."
            }
            else -> {
                "Erreur inconnue lors de la vérification de l'application fitness."
            }
        }
    }
}

