package com.example.damandroid.data.datasource

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.example.damandroid.auth.StravaOAuthHelper
import com.example.damandroid.api.StravaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * DataSource pour Strava (Application fitness populaire)
 * Strava est gratuit, compatible avec tous les appareils Android
 * et synchronise avec de nombreuses applications et montres connectées
 */
class StravaDataSource(private val context: Context) : FitnessDataSource {

    companion object {
        private const val TAG = "StravaDataSource"
        private const val STRAVA_PACKAGE = "com.strava"
        private const val STRAVA_APP_NAME = "Strava"
        private const val STRAVA_API_BASE_URL = "https://www.strava.com/api/v3/"
        
        // Variantes possibles du package name
        private val POSSIBLE_PACKAGE_NAMES = listOf(
            "com.strava",
            "com.strava.app",
            "com.strava.android"
        )
    }
    
    private val oAuthHelper = StravaOAuthHelper(context)
    
    // Instance Retrofit pour l'API Strava
    private val stravaApiService: StravaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(STRAVA_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StravaApiService::class.java)
    }

    override suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val packageManager = context.packageManager
            
            // Méthode 1: Vérifier avec getPackageInfo (nécessite API 33+ pour PackageManager.PackageInfoFlags)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    val packageInfo = packageManager.getPackageInfo(STRAVA_PACKAGE, PackageManager.PackageInfoFlags.of(0))
                    if (packageInfo != null) {
                        Log.d(TAG, "Strava is installed (method 1: getPackageInfo API 33+)")
                        return@withContext true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val packageInfo = packageManager.getPackageInfo(STRAVA_PACKAGE, 0)
                    if (packageInfo != null) {
                        Log.d(TAG, "Strava is installed (method 1: getPackageInfo legacy)")
                        return@withContext true
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.d(TAG, "Strava not found with getPackageInfo: ${e.message}")
            } catch (e: Exception) {
                Log.d(TAG, "Error with getPackageInfo: ${e.message}")
            }
            
            // Méthode 2: Vérifier avec getLaunchIntentForPackage (plus fiable)
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(STRAVA_PACKAGE)
                if (launchIntent != null) {
                    Log.d(TAG, "Strava is installed (method 2: getLaunchIntentForPackage)")
                    return@withContext true
                } else {
                    Log.d(TAG, "Strava launch intent is null")
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error with getLaunchIntentForPackage: ${e.message}")
            }
            
            // Méthode 3: Chercher dans tous les packages installés (plus lent mais plus fiable)
            try {
                val installedPackages = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getInstalledPackages(0)
                }
                
                Log.d(TAG, "Total installed packages: ${installedPackages.size}")
                
                // Chercher Strava avec le package name exact
                val stravaInstalled = installedPackages.any { it.packageName == STRAVA_PACKAGE }
                if (stravaInstalled) {
                    Log.d(TAG, "✅ Strava is installed (method 3: getInstalledPackages - exact match)")
                    return@withContext true
                }
                
                // Chercher avec les variantes possibles
                for (packageName in POSSIBLE_PACKAGE_NAMES) {
                    if (installedPackages.any { it.packageName == packageName }) {
                        Log.d(TAG, "✅ Strava found with variant package name: $packageName")
                        return@withContext true
                    }
                }
                
                // Chercher par nom d'application (label) - méthode améliorée
                val stravaByLabel = installedPackages.firstOrNull { pkg ->
                    try {
                        val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            packageManager.getApplicationInfo(pkg.packageName, PackageManager.ApplicationInfoFlags.of(0))
                        } else {
                            @Suppress("DEPRECATION")
                            packageManager.getApplicationInfo(pkg.packageName, 0)
                        }
                        val label = packageManager.getApplicationLabel(appInfo).toString().lowercase()
                        val containsStrava = label.contains("strava", ignoreCase = true)
                        if (containsStrava) {
                            Log.d(TAG, "Found app with 'strava' in label: ${pkg.packageName} -> '$label'")
                        }
                        containsStrava
                    } catch (e: PackageManager.NameNotFoundException) {
                        false
                    } catch (e: Exception) {
                        false
                    }
                }
                
                if (stravaByLabel != null) {
                    Log.d(TAG, "✅ Strava found by application label: ${stravaByLabel.packageName}")
                    return@withContext true
                }
                
                // Log tous les packages contenant "strava" dans le package name
                val stravaRelatedPackages = installedPackages.filter { 
                    it.packageName.lowercase().contains("strava") 
                }
                if (stravaRelatedPackages.isNotEmpty()) {
                    Log.d(TAG, "Found packages with 'strava' in package name: ${stravaRelatedPackages.map { it.packageName }}")
                    return@withContext true
                }
                
                // Log tous les packages contenant "strava" dans le label
                val allAppsWithStrava = installedPackages.filter { pkg ->
                    try {
                        val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            packageManager.getApplicationInfo(pkg.packageName, PackageManager.ApplicationInfoFlags.of(0))
                        } else {
                            @Suppress("DEPRECATION")
                            packageManager.getApplicationInfo(pkg.packageName, 0)
                        }
                        val label = packageManager.getApplicationLabel(appInfo).toString().lowercase()
                        label.contains("strava", ignoreCase = true)
                    } catch (e: Exception) {
                        false
                    }
                }
                
                if (allAppsWithStrava.isNotEmpty()) {
                    val appDetails = allAppsWithStrava.map { pkg ->
                        try {
                            val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                packageManager.getApplicationInfo(pkg.packageName, PackageManager.ApplicationInfoFlags.of(0))
                            } else {
                                @Suppress("DEPRECATION")
                                packageManager.getApplicationInfo(pkg.packageName, 0)
                            }
                            val label = packageManager.getApplicationLabel(appInfo)
                            "${pkg.packageName} -> '$label'"
                        } catch (e: Exception) {
                            pkg.packageName
                        }
                    }
                    Log.d(TAG, "✅ Found apps with 'strava' in label: $appDetails")
                    return@withContext true
                }
                
                Log.d(TAG, "❌ Strava not found in installed packages list (total: ${installedPackages.size})")
                
                // Recherche exhaustive dans TOUS les packages (pas seulement les 50 premiers)
                Log.d(TAG, "🔍 Searching through ALL ${installedPackages.size} packages for Strava...")
                var foundStrava = false
                var stravaPackageName: String? = null
                
                for (pkg in installedPackages) {
                    try {
                        // Vérifier le package name
                        if (pkg.packageName.lowercase().contains("strava")) {
                            Log.d(TAG, "✅ FOUND Strava by package name: ${pkg.packageName}")
                            stravaPackageName = pkg.packageName
                            foundStrava = true
                            break
                        }
                        
                        // Vérifier le label de l'application
                        val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            packageManager.getApplicationInfo(pkg.packageName, PackageManager.ApplicationInfoFlags.of(0))
                        } else {
                            @Suppress("DEPRECATION")
                            packageManager.getApplicationInfo(pkg.packageName, 0)
                        }
                        val label = packageManager.getApplicationLabel(appInfo).toString().lowercase()
                        
                        if (label.contains("strava", ignoreCase = true)) {
                            Log.d(TAG, "✅ FOUND Strava by label: ${pkg.packageName} -> '$label'")
                            stravaPackageName = pkg.packageName
                            foundStrava = true
                            break
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        // Ignorer les packages non trouvés
                    } catch (e: Exception) {
                        // Ignorer les erreurs pour continuer la recherche
                    }
                }
                
                if (foundStrava && stravaPackageName != null) {
                    Log.d(TAG, "✅✅✅ Strava DETECTED! Package: $stravaPackageName")
                    return@withContext true
                }
                
                // Log tous les packages installés pour debug (premiers 50 pour voir plus)
                val samplePackages = installedPackages.take(50).map { pkg ->
                    try {
                        val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            packageManager.getApplicationInfo(pkg.packageName, PackageManager.ApplicationInfoFlags.of(0))
                        } else {
                            @Suppress("DEPRECATION")
                            packageManager.getApplicationInfo(pkg.packageName, 0)
                        }
                        val label = packageManager.getApplicationLabel(appInfo)
                        "${pkg.packageName} ($label)"
                    } catch (e: Exception) {
                        pkg.packageName
                    }
                }
                Log.d(TAG, "Sample installed packages (first 50): $samplePackages")
                
                // Log les packages 51-90 aussi
                if (installedPackages.size > 50) {
                    val remainingPackages = installedPackages.drop(50).take(40).map { pkg ->
                        try {
                            val appInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                packageManager.getApplicationInfo(pkg.packageName, PackageManager.ApplicationInfoFlags.of(0))
                            } else {
                                @Suppress("DEPRECATION")
                                packageManager.getApplicationInfo(pkg.packageName, 0)
                            }
                            val label = packageManager.getApplicationLabel(appInfo)
                            "${pkg.packageName} ($label)"
                        } catch (e: Exception) {
                            pkg.packageName
                        }
                    }
                    Log.d(TAG, "Remaining packages (51-90): $remainingPackages")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error with getInstalledPackages: ${e.message}", e)
            }
            
            // Méthode 4: Vérifier avec queryIntentActivities (intent MAIN)
            try {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage(STRAVA_PACKAGE)
                }
                val activities = packageManager.queryIntentActivities(intent, 0)
                if (activities.isNotEmpty()) {
                    Log.d(TAG, "Strava is installed (method 4: queryIntentActivities)")
                    return@withContext true
                } else {
                    Log.d(TAG, "Strava not found with queryIntentActivities")
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error with queryIntentActivities: ${e.message}")
            }
            
            Log.d(TAG, "Strava is NOT installed (all methods failed)")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Strava availability: ${e.message}", e)
            false
        }
    }

    override suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        // Vérifier d'abord si Strava est installé
        val available = isAvailable()
        if (!available) {
            Log.d(TAG, "❌ Strava n'est pas installé - pas connecté")
            return@withContext false
        }
        
        // Vérifier si l'utilisateur est authentifié via OAuth
        val isOAuthConnected = oAuthHelper.isConnected()
        Log.d(TAG, "Strava connection check: available=$available, oAuthConnected=$isOAuthConnected")
        
        if (isOAuthConnected) {
            Log.d(TAG, "✅ Strava est connecté via OAuth")
        } else {
            Log.d(TAG, "⚠️ Strava est installé mais pas connecté via OAuth")
        }
        
        isOAuthConnected
    }

    override suspend fun requestPermissions(activity: android.app.Activity): Boolean = withContext(Dispatchers.Main) {
        try {
            Log.d(TAG, "🔄 requestPermissions called")
            val available = isAvailable()
            Log.d(TAG, "🔄 requestPermissions: isAvailable=$available")
            
            if (!available) {
                Log.w(TAG, "⚠️ Strava not available, opening Play Store")
                // Ouvrir le Play Store pour installer Strava
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$STRAVA_PACKAGE")
                    setPackage("com.android.vending")
                }
                activity.startActivity(intent)
                return@withContext false
            }

            // Si déjà connecté via OAuth, ne rien faire
            val connected = oAuthHelper.isConnected()
            Log.d(TAG, "🔄 requestPermissions: isConnected=$connected")
            if (connected) {
                Log.d(TAG, "✅ Already connected to Strava via OAuth")
                return@withContext true
            }

            // Lancer le flux OAuth Strava
            Log.d(TAG, "🚀 Launching Strava OAuth flow...")
            oAuthHelper.launchOAuthFlow(activity)
            Log.d(TAG, "✅ launchOAuthFlow called successfully")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error requesting Strava permissions", e)
            e.printStackTrace()
            false
        }
    }

    override suspend fun getWeeklyStats(): WeeklyStats = withContext(Dispatchers.IO) {
        try {
            // Vérifier si l'utilisateur est connecté via OAuth
            if (!oAuthHelper.isConnected()) {
                Log.w(TAG, "Strava not connected via OAuth, returning empty stats")
                return@withContext WeeklyStats(0, 0, 0, 0)
            }
            
            val accessToken = oAuthHelper.getAccessToken()
            if (accessToken == null) {
                Log.w(TAG, "No valid access token available")
                return@withContext WeeklyStats(0, 0, 0, 0)
            }
            
            // Calculer la date de début de la semaine (lundi à 00:00)
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // Aller au lundi de la semaine actuelle
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
            calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
            
            val weekStartTimestamp = calendar.timeInMillis / 1000 // Convertir en secondes Unix
            
            Log.d(TAG, "Fetching Strava activities from ${calendar.time} (timestamp: $weekStartTimestamp)")
            
            // Récupérer les activités de la semaine
            val response = stravaApiService.getAthleteActivities(
                accessToken = "Bearer $accessToken",
                after = weekStartTimestamp,
                before = null, // Pas de limite supérieure, toutes les activités jusqu'à maintenant
                page = 1,
                perPage = 200
            )
            
            if (response.isSuccessful) {
                val activities = response.body() ?: emptyList()
                Log.d(TAG, "✅ Fetched ${activities.size} activities from Strava")
                
                // Calculer les statistiques
                var totalCalories = 0
                var totalMinutes = 0
                val workouts = activities.size
                
                activities.forEach { activity ->
                    // Calories (peuvent être null, utiliser 0 si null)
                    totalCalories += activity.calories?.toInt() ?: 0
                    
                    // Temps en minutes (moving_time est en secondes)
                    totalMinutes += activity.moving_time / 60
                }
                
                // Calculer le streak (nombre de jours consécutifs avec au moins une activité)
                val streak = calculateStreak(activities)
                
                Log.d(TAG, "Strava weekly stats: workouts=$workouts, calories=$totalCalories, minutes=$totalMinutes, streak=$streak")
                
                return@withContext WeeklyStats(
                    workouts = workouts,
                    calories = totalCalories,
                    minutes = totalMinutes,
                    streak = streak
                )
            } else {
                Log.e(TAG, "Failed to fetch Strava activities: ${response.code()} - ${response.message()}")
                // Si le token est expiré, essayer de le rafraîchir
                if (response.code() == 401) {
                    Log.d(TAG, "Token might be expired, attempting refresh...")
                    val refreshed = oAuthHelper.refreshAccessTokenIfNeeded()
                    if (refreshed) {
                        // Réessayer avec le nouveau token
                        val newToken = oAuthHelper.getAccessToken()
                        if (newToken != null) {
                            val retryResponse = stravaApiService.getAthleteActivities(
                                accessToken = "Bearer $newToken",
                                after = weekStartTimestamp,
                                before = null,
                                page = 1,
                                perPage = 200
                            )
                            if (retryResponse.isSuccessful) {
                                val activities = retryResponse.body() ?: emptyList()
                                val stats = calculateStatsFromActivities(activities)
                                return@withContext stats
                            }
                        }
                    }
                }
                return@withContext WeeklyStats(0, 0, 0, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting weekly stats from Strava", e)
            WeeklyStats(0, 0, 0, 0)
        }
    }
    
    /**
     * Calcule les statistiques à partir des activités Strava
     */
    private fun calculateStatsFromActivities(activities: List<com.example.damandroid.api.StravaActivityDto>): WeeklyStats {
        var totalCalories = 0
        var totalMinutes = 0
        val workouts = activities.size
        
        activities.forEach { activity ->
            totalCalories += activity.calories?.toInt() ?: 0
            totalMinutes += activity.moving_time / 60
        }
        
        val streak = calculateStreak(activities)
        
        return WeeklyStats(
            workouts = workouts,
            calories = totalCalories,
            minutes = totalMinutes,
            streak = streak
        )
    }
    
    /**
     * Calcule le streak (nombre de jours consécutifs avec au moins une activité)
     * Le streak commence à partir d'aujourd'hui et remonte dans le temps
     */
    private fun calculateStreak(activities: List<com.example.damandroid.api.StravaActivityDto>): Int {
        if (activities.isEmpty()) return 0
        
        // Grouper les activités par jour
        val activitiesByDay = mutableMapOf<String, List<com.example.damandroid.api.StravaActivityDto>>()
        activities.forEach { activity ->
            try {
                // Parser la date ISO 8601
                val dateStr = activity.start_date_local.substring(0, 10) // YYYY-MM-DD
                activitiesByDay[dateStr] = activitiesByDay.getOrDefault(dateStr, emptyList()) + activity
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing date for activity ${activity.id}", e)
            }
        }
        
        // Trier les dates (du plus récent au plus ancien)
        val sortedDates = activitiesByDay.keys.sortedDescending()
        
        // Calculer le streak en remontant depuis aujourd'hui
        val calendar = Calendar.getInstance()
        var streak = 0
        var currentDate = calendar.clone() as Calendar
        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)
        
        while (true) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentDate.time)
            
            if (activitiesByDay.containsKey(dateStr)) {
                streak++
                // Passer au jour précédent
                currentDate.add(Calendar.DAY_OF_MONTH, -1)
            } else {
                // Pas d'activité ce jour, le streak est cassé
                break
            }
            
            // Limiter à 30 jours pour éviter une boucle infinie
            if (streak >= 30) break
        }
        
        return streak
    }

    override fun getAppName(): String = STRAVA_APP_NAME

    override fun getPackageName(): String = STRAVA_PACKAGE
}

