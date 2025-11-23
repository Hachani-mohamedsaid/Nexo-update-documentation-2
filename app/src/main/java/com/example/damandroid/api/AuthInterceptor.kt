package com.example.damandroid.api

import com.example.damandroid.auth.UserSession
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor pour ajouter automatiquement le token JWT dans les headers
 * de toutes les requêtes vers l'API
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Récupérer le token depuis UserSession
        val token = UserSession.token
        
        // Si un token existe, l'ajouter au header Authorization
        val newRequest = if (token != null && token.isNotBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}

