package com.example.damandroid.presentation.achievements

import android.content.Context
import android.content.SharedPreferences

/**
 * Store pour persister les notifications déjà affichées
 * Évite d'afficher les mêmes notifications après redémarrage de l'application
 */
class NotificationDisplayStore(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Vérifie si un badge a déjà été affiché
     */
    fun isBadgeShown(badgeId: String): Boolean {
        return preferences.getBoolean(buildBadgeKey(badgeId), false)
    }

    /**
     * Marque un badge comme déjà affiché
     */
    fun markBadgeAsShown(badgeId: String) {
        preferences.edit()
            .putBoolean(buildBadgeKey(badgeId), true)
            .apply()
    }

    /**
     * Vérifie si une montée de niveau a déjà été affichée
     * Utilise le niveau comme identifiant unique
     */
    fun isLevelUpShown(level: Int): Boolean {
        return preferences.getBoolean(buildLevelUpKey(level), false)
    }

    /**
     * Marque une montée de niveau comme déjà affichée
     */
    fun markLevelUpAsShown(level: Int) {
        preferences.edit()
            .putBoolean(buildLevelUpKey(level), true)
            .apply()
    }

    /**
     * Vérifie si un challenge complété a déjà été affiché
     */
    fun isChallengeShown(challengeId: String): Boolean {
        return preferences.getBoolean(buildChallengeKey(challengeId), false)
    }

    /**
     * Marque un challenge complété comme déjà affiché
     */
    fun markChallengeAsShown(challengeId: String) {
        preferences.edit()
            .putBoolean(buildChallengeKey(challengeId), true)
            .apply()
    }

    /**
     * Nettoie les anciennes notifications affichées (optionnel, pour libérer de l'espace)
     * Garde uniquement les 100 dernières notifications
     */
    fun cleanOldNotifications() {
        // Cette fonction peut être appelée périodiquement pour nettoyer
        // Pour l'instant, on garde tout pour éviter de réafficher des notifications
        // Si nécessaire, on peut implémenter une logique de nettoyage ici
    }

    /**
     * Réinitialise toutes les notifications affichées (pour le débogage)
     */
    fun resetAll() {
        preferences.edit()
            .clear()
            .apply()
    }

    private fun buildBadgeKey(badgeId: String): String {
        return "badge_shown_$badgeId"
    }

    private fun buildLevelUpKey(level: Int): String {
        return "level_up_shown_$level"
    }

    private fun buildChallengeKey(challengeId: String): String {
        return "challenge_shown_$challengeId"
    }

    companion object {
        private const val PREFS_NAME = "achievement_notifications_displayed"
    }
}

