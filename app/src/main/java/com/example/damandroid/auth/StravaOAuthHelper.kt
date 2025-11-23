package com.example.damandroid.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper pour gérer l'authentification OAuth avec Strava
 * 
 * Pour obtenir les credentials Strava:
 * 1. Allez sur https://www.strava.com/settings/api
 * 2. Créez une application
 * 3. Récupérez le Client ID et Client Secret
 * 4. Configurez l'URL de redirection: nexofitness://strava/callback
 */
class StravaOAuthHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "StravaOAuthHelper"
        
        // ✅ Credentials Strava configurés
        // Obtenus depuis: https://www.strava.com/settings/api
        // ⚠️ IMPORTANT: Si vous générez un nouveau Client Secret, mettez à jour cette valeur !
        private const val STRAVA_CLIENT_ID = "186697"
        private const val STRAVA_CLIENT_SECRET = "cc6c2bed42ca1be1d98b052aec096135412e6c09"
        
        // URL de redirection OAuth (doit correspondre à celle configurée dans Strava)
        // Utilise une URL HTTP qui redirige vers le deep link nexofitness://strava/callback
        private const val REDIRECT_URI = "https://apinest-production.up.railway.app/strava/callback"
        
        // Scopes demandés à Strava
        private const val SCOPES = "activity:read_all,profile:read_all"
        
        // URL d'autorisation Strava
        private const val AUTHORIZATION_URL = "https://www.strava.com/oauth/authorize"
        
        // URL d'échange de token
        private const val TOKEN_URL = "https://www.strava.com/oauth/token"
    }
    
    private val tokenStore = StravaTokenStore(context)
    
    /**
     * Construire l'URL d'autorisation Strava
     */
    fun buildAuthorizationUrl(): String {
        return Uri.parse(AUTHORIZATION_URL)
            .buildUpon()
            .appendQueryParameter("client_id", STRAVA_CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("approval_prompt", "force")
            .build()
            .toString()
    }
    
    /**
     * Lancer le flux OAuth Strava
     * Ouvre le navigateur pour l'authentification
     */
    fun launchOAuthFlow(activity: Activity) {
        try {
            val authUrl = buildAuthorizationUrl()
            Log.d(TAG, "🚀 Launching Strava OAuth flow...")
            Log.d(TAG, "   Authorization URL: $authUrl")
            Log.d(TAG, "   Redirect URI: $REDIRECT_URI")
            Log.d(TAG, "   Activity: ${activity.javaClass.simpleName}")
            Log.d(TAG, "   Activity isFinishing: ${activity.isFinishing}")
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
            
            // Vérifier qu'un navigateur peut gérer cette intent
            val resolveInfo = activity.packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            if (resolveInfo == null) {
                Log.e(TAG, "❌ No activity can handle the intent to open browser")
                return
            }
            
            activity.startActivity(intent)
            Log.d(TAG, "✅ Browser opened for Strava OAuth")
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "❌ No browser found to open Strava OAuth", e)
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to launch Strava OAuth flow", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Traiter la réponse OAuth depuis l'URL de callback
     * @param callbackUri L'URI de callback reçue
     * @return Le code d'autorisation si présent, null sinon
     */
    fun handleCallback(callbackUri: Uri): String? {
        Log.d(TAG, "Handling Strava OAuth callback: $callbackUri")
        
        // Vérifier si c'est notre URL de callback
        if (callbackUri.scheme != "nexofitness" || callbackUri.host != "strava") {
            Log.w(TAG, "Invalid callback URI scheme/host")
            return null
        }
        
        // Extraire le code d'autorisation
        val code = callbackUri.getQueryParameter("code")
        val error = callbackUri.getQueryParameter("error")
        
        if (error != null) {
            Log.e(TAG, "Strava OAuth error: $error")
            return null
        }
        
        if (code != null) {
            Log.d(TAG, "✅ Authorization code received: $code")
            return code
        }
        
        Log.w(TAG, "No authorization code in callback")
        return null
    }
    
    /**
     * Échanger le code d'autorisation contre un token d'accès
     * Cette fonction doit être appelée depuis un coroutine scope
     */
    suspend fun exchangeCodeForToken(authorizationCode: String): StravaTokenResult = suspendCancellableCoroutine { continuation ->
        try {
            // ⚠️ NOTE: Pour des raisons de sécurité, l'échange de token devrait être fait côté backend
            // Ici, on fait une implémentation côté client (moins sécurisé mais fonctionnel)
            // En production, créez un endpoint backend qui échange le code contre le token
            
            val requestBody = mapOf(
                "client_id" to STRAVA_CLIENT_ID,
                "client_secret" to STRAVA_CLIENT_SECRET,
                "code" to authorizationCode,
                "grant_type" to "authorization_code"
            )
            
            // Utiliser OkHttp pour faire la requête POST
            val client = okhttp3.OkHttpClient()
            val formBody = okhttp3.FormBody.Builder().apply {
                requestBody.forEach { (key, value) ->
                    add(key, value)
                }
            }.build()
            
            val request = okhttp3.Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .build()
            
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    Log.e(TAG, "Failed to exchange code for token", e)
                    continuation.resumeWithException(e)
                }
                
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    try {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "Token exchange response: $responseBody")
                        
                        if (response.isSuccessful && responseBody != null) {
                            // Parser la réponse JSON
                            val json = org.json.JSONObject(responseBody)
                            val accessToken = json.getString("access_token")
                            val refreshToken = json.optString("refresh_token", null)
                            val expiresAt = json.optLong("expires_at", 0) * 1000 // Convertir en millisecondes
                            val athlete = json.optJSONObject("athlete")
                            val athleteId = athlete?.optLong("id", -1)?.takeIf { it != -1L }
                            
                            // Sauvegarder le token
                            tokenStore.saveAccessToken(accessToken, refreshToken, expiresAt, athleteId)
                            
                            continuation.resume(StravaTokenResult.Success(accessToken))
                        } else {
                            val error = "Failed to exchange code: ${response.code} - $responseBody"
                            Log.e(TAG, error)
                            continuation.resume(StravaTokenResult.Error(error))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing token response", e)
                        continuation.resumeWithException(e)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error exchanging code for token", e)
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Vérifier si l'utilisateur est connecté à Strava
     */
    fun isConnected(): Boolean {
        return tokenStore.isConnected()
    }
    
    /**
     * Obtenir le token d'accès actuel
     */
    fun getAccessToken(): String? {
        return tokenStore.getAccessToken()
    }
    
    /**
     * Rafraîchir le token d'accès si nécessaire (si expiré)
     * @return true si le token a été rafraîchi avec succès, false sinon
     */
    suspend fun refreshAccessTokenIfNeeded(): Boolean {
        val refreshToken = tokenStore.getRefreshToken()
        if (refreshToken == null) {
            Log.w(TAG, "No refresh token available")
            return false
        }
        
        // Vérifier si le token est vraiment expiré
        val currentToken = tokenStore.getAccessToken()
        if (currentToken != null) {
            Log.d(TAG, "Access token is still valid, no refresh needed")
            return true
        }
        
        Log.d(TAG, "Access token expired, refreshing...")
        
        return suspendCancellableCoroutine { continuation ->
            try {
                val requestBody = mapOf(
                    "client_id" to STRAVA_CLIENT_ID,
                    "client_secret" to STRAVA_CLIENT_SECRET,
                    "refresh_token" to refreshToken,
                    "grant_type" to "refresh_token"
                )
                
                val client = okhttp3.OkHttpClient()
                val formBody = okhttp3.FormBody.Builder().apply {
                    requestBody.forEach { (key, value) ->
                        add(key, value)
                    }
                }.build()
                
                val request = okhttp3.Request.Builder()
                    .url(TOKEN_URL)
                    .post(formBody)
                    .build()
                
                client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                        Log.e(TAG, "Failed to refresh token", e)
                        continuation.resume(false)
                    }
                    
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        try {
                            val responseBody = response.body?.string()
                            Log.d(TAG, "Token refresh response: $responseBody")
                            
                            if (response.isSuccessful && responseBody != null) {
                                val json = org.json.JSONObject(responseBody)
                                val accessToken = json.getString("access_token")
                                val newRefreshToken = json.optString("refresh_token", refreshToken) // Utiliser l'ancien si non fourni
                                val expiresAt = json.optLong("expires_at", 0) * 1000
                                val athlete = json.optJSONObject("athlete")
                                val athleteId = athlete?.optLong("id", -1)?.takeIf { it != -1L } ?: tokenStore.getAthleteId()
                                
                                tokenStore.saveAccessToken(accessToken, newRefreshToken, expiresAt, athleteId)
                                Log.d(TAG, "✅ Token refreshed successfully")
                                continuation.resume(true)
                            } else {
                                Log.e(TAG, "Failed to refresh token: ${response.code} - $responseBody")
                                continuation.resume(false)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing refresh token response", e)
                            continuation.resume(false)
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing token", e)
                continuation.resume(false)
            }
        }
    }
    
    /**
     * Déconnecter l'utilisateur de Strava
     */
    fun disconnect() {
        tokenStore.clearTokens()
        Log.d(TAG, "User disconnected from Strava")
    }
}

/**
 * Résultat de l'échange de code contre token
 */
sealed class StravaTokenResult {
    data class Success(val accessToken: String) : StravaTokenResult()
    data class Error(val message: String) : StravaTokenResult()
}


