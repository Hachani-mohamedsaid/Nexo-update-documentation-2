package com.example.damandroid.util

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Gestionnaire centralisé pour les erreurs de l'application
 * Conforme au guide des Achievements
 */
object ErrorHandler {
    /**
     * Convertit une exception en message d'erreur lisible par l'utilisateur
     */
    fun handleError(error: Throwable): String {
        return when (error) {
            is HttpException -> {
                when (error.code()) {
                    401 -> "Session expirée, veuillez vous reconnecter"
                    403 -> "Accès refusé"
                    404 -> "Ressource non trouvée"
                    500 -> "Erreur serveur, veuillez réessayer plus tard"
                    else -> "Erreur: ${error.message()}"
                }
            }
            is SocketTimeoutException -> "Délai d'attente dépassé, vérifiez votre connexion"
            is IOException -> "Problème de connexion réseau"
            else -> error.message ?: "Erreur inconnue"
        }
    }
    
    /**
     * Convertit un code HTTP en message d'erreur
     */
    fun handleHttpError(code: Int): String {
        return when (code) {
            401 -> "Session expirée, veuillez vous reconnecter"
            403 -> "Accès refusé"
            404 -> "Ressource non trouvée"
            500 -> "Erreur serveur, veuillez réessayer plus tard"
            else -> "Erreur: Code $code"
        }
    }
}

