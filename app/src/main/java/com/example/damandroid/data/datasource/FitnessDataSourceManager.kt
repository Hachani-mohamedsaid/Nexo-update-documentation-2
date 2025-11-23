package com.example.damandroid.data.datasource

import android.content.Context
import android.util.Log

/**
 * Gestionnaire qui choisit automatiquement la meilleure source de données fitness disponible
 * Priorité :
 * 1. Health Connect (Android 14+, gratuit, compatible partout)
 * 2. Google Fit (si disponible)
 * 3. Autres sources (Strava, Fitbit, etc.)
 */
class FitnessDataSourceManager(private val context: Context) {

    companion object {
        private const val TAG = "FitnessDataSourceManager"
    }

    private val dataSources: List<FitnessDataSource> = listOf(
        HealthConnectDataSource(context)
    )

    /**
     * Trouve la meilleure source de données disponible
     */
    suspend fun getBestAvailableDataSource(): FitnessDataSource? {
        for (dataSource in dataSources) {
            try {
                if (dataSource.isAvailable()) {
                    Log.d(TAG, "Using fitness data source: ${dataSource.getAppName()}")
                    return dataSource
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking ${dataSource.getAppName()}", e)
            }
        }
        Log.w(TAG, "No fitness data source available")
        return null
    }

    /**
     * Récupère les statistiques depuis la meilleure source disponible
     */
    suspend fun getWeeklyStats(): WeeklyStats {
        val dataSource = getBestAvailableDataSource()
        return if (dataSource != null) {
            try {
                dataSource.getWeeklyStats()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting stats from ${dataSource.getAppName()}", e)
                WeeklyStats(0, 0, 0, 0)
            }
        } else {
            WeeklyStats(0, 0, 0, 0)
        }
    }

    /**
     * Vérifie si au moins une source est disponible
     */
    suspend fun isAnySourceAvailable(): Boolean {
        return getBestAvailableDataSource() != null
    }

    /**
     * Vérifie si au moins une source est connectée
     */
    suspend fun isAnySourceConnected(): Boolean {
        for (dataSource in dataSources) {
            try {
                if (dataSource.isAvailable() && dataSource.isConnected()) {
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking connection for ${dataSource.getAppName()}", e)
            }
        }
        return false
    }

    /**
     * Demande les permissions pour la meilleure source disponible
     */
    suspend fun requestPermissions(activity: android.app.Activity): Boolean {
        val dataSource = getBestAvailableDataSource()
        return if (dataSource != null) {
            try {
                dataSource.requestPermissions(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting permissions for ${dataSource.getAppName()}", e)
                false
            }
        } else {
            false
        }
    }

    /**
     * Obtient le nom de l'application utilisée
     */
    suspend fun getCurrentAppName(): String {
        val dataSource = getBestAvailableDataSource()
        return dataSource?.getAppName() ?: "Aucune application"
    }
}


