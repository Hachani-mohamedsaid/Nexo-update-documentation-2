package com.example.damandroid.presentation.quickmatch.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Store pour persister les IDs des profils likés/passés dans QuickMatch
 * Garantit que ces profils ne réapparaissent pas après un refresh ou un redémarrage de l'app
 */
class ExcludedProfilesStore(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Ajoute un profil ID à la liste des exclus
     */
    fun addExcludedProfile(profileId: String) {
        val excludedSet = getExcludedProfiles().toMutableSet()
        excludedSet.add(profileId)
        saveExcludedProfiles(excludedSet)
    }

    /**
     * Ajoute plusieurs profils IDs à la liste des exclus
     */
    fun addExcludedProfiles(profileIds: Set<String>) {
        val excludedSet = getExcludedProfiles().toMutableSet()
        excludedSet.addAll(profileIds)
        saveExcludedProfiles(excludedSet)
    }

    /**
     * Récupère tous les profils exclus
     */
    fun getExcludedProfiles(): Set<String> {
        return preferences.getStringSet(KEY_EXCLUDED_PROFILES, emptySet()) ?: emptySet()
    }

    /**
     * Vérifie si un profil est exclu
     */
    fun isExcluded(profileId: String): Boolean {
        return getExcludedProfiles().contains(profileId)
    }

    /**
     * Sauvegarde la liste complète des profils exclus
     */
    private fun saveExcludedProfiles(profileIds: Set<String>) {
        preferences.edit()
            .putStringSet(KEY_EXCLUDED_PROFILES, profileIds)
            .apply()
    }

    /**
     * Supprime un profil de la liste des exclus (utile pour les tests ou reset)
     */
    fun removeExcludedProfile(profileId: String) {
        val excludedSet = getExcludedProfiles().toMutableSet()
        excludedSet.remove(profileId)
        saveExcludedProfiles(excludedSet)
    }

    /**
     * Vide complètement la liste des exclus (utile pour les tests ou reset)
     */
    fun clearExcludedProfiles() {
        preferences.edit()
            .remove(KEY_EXCLUDED_PROFILES)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "quickmatch_excluded_profiles"
        private const val KEY_EXCLUDED_PROFILES = "excluded_profile_ids"
    }
}

