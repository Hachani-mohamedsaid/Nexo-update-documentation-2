package com.example.damandroid.data.datasource

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * DataSource pour Health Connect (Android 8.0+)
 * Alternative gratuite et compatible partout à Google Fit
 */
class HealthConnectDataSource(private val context: Context) : FitnessDataSource {

    companion object {
        private const val TAG = "HealthConnectDataSource"
    }

    private val healthConnectClient: HealthConnectClient? by lazy {
        try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Health Connect client - Health Connect may not be available", e)
            null
        }
    }

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Vérifier si Health Connect est disponible en essayant de créer le client
            healthConnectClient != null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Health Connect availability", e)
            false
        }
    }

    override suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = healthConnectClient ?: return@withContext false
            val requiredPermissions = getRequiredPermissions()
            
            // Vérifier si les permissions sont accordées
            // getGrantedPermissions() retourne Set<String> (noms de permissions)
            val grantedPermissions: Set<String> = client.permissionController.getGrantedPermissions()
            
            // Vérifier que toutes les permissions requises sont accordées
            // Comparer les noms de permissions (String)
            requiredPermissions.all { required: String ->
                grantedPermissions.contains(required)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Health Connect connection", e)
            false
        }
    }

    override suspend fun requestPermissions(activity: android.app.Activity): Boolean = withContext(Dispatchers.IO) {
        try {
            val client = healthConnectClient ?: return@withContext false
            val requiredPermissions = getRequiredPermissions()
            
            // Vérifier si les permissions sont déjà accordées
            // getGrantedPermissions() retourne Set<String>
            val grantedPermissions: Set<String> = client.permissionController.getGrantedPermissions()
            val allGranted = requiredPermissions.all { required: String ->
                grantedPermissions.contains(required)
            }
            if (allGranted) {
                return@withContext true
            }
            
            // Health Connect nécessite de lancer une activité pour demander les permissions
            // On retourne false et l'UI devra gérer la demande de permissions
            Log.d(TAG, "Permissions need to be requested via Health Connect UI")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting Health Connect permissions", e)
            false
        }
    }

    override suspend fun getWeeklyStats(): WeeklyStats = withContext(Dispatchers.IO) {
        try {
            val client = healthConnectClient ?: return@withContext WeeklyStats(0, 0, 0, 0)
            
            val endTime = Instant.now()
            val startTime = endTime.minus(7, ChronoUnit.DAYS)
            
            // Récupérer les workouts
            val workouts = getWorkoutsCount(client, startTime, endTime)
            
            // Récupérer les calories
            val calories = getCalories(client, startTime, endTime)
            
            // Récupérer les minutes d'activité
            val minutes = getActivityMinutes(client, startTime, endTime)
            
            // Calculer le streak
            val streak = calculateStreak(client)
            
            WeeklyStats(
                workouts = workouts,
                calories = calories,
                minutes = minutes,
                streak = streak
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting weekly stats from Health Connect", e)
            WeeklyStats(0, 0, 0, 0)
        }
    }

    private suspend fun getWorkoutsCount(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): Int {
        return try {
            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            
            val response = client.readRecords(request)
            response.records.size
        } catch (e: Exception) {
            Log.e(TAG, "Error getting workouts count", e)
            0
        }
    }

    private suspend fun getCalories(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): Int {
        return try {
            // Récupérer les calories totales brûlées
            val totalCaloriesRequest = AggregateRequest(
                metrics = setOf(TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            
            val totalCaloriesResult = client.aggregate(totalCaloriesRequest)
            val totalCalories = totalCaloriesResult[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories?.toInt() ?: 0
            
            totalCalories.coerceAtLeast(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting calories", e)
            0
        }
    }

    private suspend fun getActivityMinutes(
        client: HealthConnectClient,
        startTime: Instant,
        endTime: Instant
    ): Int {
        return try {
            // Récupérer les sessions d'exercice pour calculer les minutes
            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            
            val response = client.readRecords(request)
            var totalMinutes = 0L
            
            response.records.forEach { record ->
                val duration = java.time.Duration.between(record.startTime, record.endTime)
                totalMinutes += duration.toMinutes()
            }
            
            totalMinutes.toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activity minutes", e)
            0
        }
    }

    private suspend fun calculateStreak(client: HealthConnectClient): Int {
        return try {
            val endTime = Instant.now()
            var streak = 0
            var currentDate = ZonedDateTime.now()
            
            // Vérifier aujourd'hui
            val todayStart = currentDate.toLocalDate().atStartOfDay(currentDate.zone).toInstant()
            val todayEnd = endTime
            
            val todayRequest = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(todayStart, todayEnd)
            )
            
            val todayResponse = client.readRecords(todayRequest)
            if (todayResponse.records.isEmpty()) {
                return 0
            }
            
            streak = 1
            
            // Vérifier les jours précédents
            for (i in 1..30) {
                currentDate = currentDate.minusDays(1)
                val dayStart = currentDate.toLocalDate().atStartOfDay(currentDate.zone).toInstant()
                val dayEnd = currentDate.toLocalDate().atTime(23, 59, 59).atZone(currentDate.zone).toInstant()
                
                val dayRequest = ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(dayStart, dayEnd)
                )
                
                val dayResponse = client.readRecords(dayRequest)
                if (dayResponse.records.isEmpty()) {
                    break
                }
                streak++
            }
            
            streak
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating streak", e)
            0
        }
    }

    private fun getRequiredPermissions(): Set<String> {
        // getReadPermission() retourne un String (nom de la permission)
        return setOf(
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
        )
    }

    override fun getAppName(): String = "Health Connect"

    override fun getPackageName(): String = "com.google.android.apps.healthdata"
}
