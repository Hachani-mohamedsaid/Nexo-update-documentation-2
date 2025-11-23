package com.example.damandroid.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Store pour gérer le token d'accès Strava OAuth
 */
class StravaTokenStore(private val context: Context) {
    
    companion object {
        private const val TAG = "StravaTokenStore"
        private const val PREFS_NAME = "strava_oauth_prefs"
        private const val KEY_ACCESS_TOKEN = "strava_access_token"
        private const val KEY_REFRESH_TOKEN = "strava_refresh_token"
        private const val KEY_EXPIRES_AT = "strava_expires_at"
        private const val KEY_ATHLETE_ID = "strava_athlete_id"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Sauvegarder le token d'accès Strava
     */
    fun saveAccessToken(accessToken: String, refreshToken: String?, expiresAt: Long, athleteId: Long?) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            putLong(KEY_EXPIRES_AT, expiresAt)
            athleteId?.let { putLong(KEY_ATHLETE_ID, it) }
            apply()
        }
        Log.d(TAG, "✅ Strava access token saved (expires at: $expiresAt)")
    }
    
    /**
     * Récupérer le token d'accès Strava
     */
    fun getAccessToken(): String? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        if (token != null) {
            // Vérifier si le token est expiré
            val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0)
            if (expiresAt > 0 && System.currentTimeMillis() >= expiresAt) {
                Log.d(TAG, "⚠️ Strava access token expired")
                return null
            }
            Log.d(TAG, "✅ Strava access token retrieved (valid)")
        }
        return token
    }
    
    /**
     * Récupérer le refresh token
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Vérifier si un token est valide (non expiré)
     */
    fun isTokenValid(): Boolean {
        val token = getAccessToken()
        return token != null
    }
    
    /**
     * Récupérer l'ID de l'athlète
     */
    fun getAthleteId(): Long? {
        val id = prefs.getLong(KEY_ATHLETE_ID, -1)
        return if (id != -1L) id else null
    }
    
    /**
     * Supprimer tous les tokens (déconnexion)
     */
    fun clearTokens() {
        prefs.edit().clear().apply()
        Log.d(TAG, "🗑️ Strava tokens cleared")
    }
    
    /**
     * Vérifier si l'utilisateur est connecté à Strava
     */
    fun isConnected(): Boolean {
        return isTokenValid()
    }
}


