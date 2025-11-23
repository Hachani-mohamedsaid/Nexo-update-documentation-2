package com.example.damandroid.location

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Service pour calculer les itinéraires et estimations de temps
 * Utilise l'API OSRM (Open Source Routing Machine) - 100% gratuit
 */
object RouteService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    
    private const val OSRM_BASE_URL = "https://router.project-osrm.org/route/v1/driving"
    
    /**
     * Calcule la distance et le temps estimé pour un trajet en voiture
     * @param startLat Latitude de départ
     * @param startLng Longitude de départ
     * @param endLat Latitude d'arrivée
     * @param endLng Longitude d'arrivée
     * @return Pair<distance en km, temps en minutes> ou null en cas d'erreur
     */
    suspend fun getRouteEstimate(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): RouteEstimate? = withContext(Dispatchers.IO) {
        try {
            // Format: /route/v1/{profile}/{coordinates}?overview=false
            val url = "$OSRM_BASE_URL/$startLng,$startLat;$endLng,$endLat?overview=false"
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                return@withContext null
            }
            
            val responseBody = response.body?.string() ?: return@withContext null
            val json = JSONObject(responseBody)
            
            // Vérifier si la route existe
            if (json.getString("code") != "Ok") {
                return@withContext null
            }
            
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) {
                return@withContext null
            }
            
            val route = routes.getJSONObject(0)
            val distance = route.getDouble("distance") // en mètres
            val duration = route.getDouble("duration") // en secondes
            
            // Convertir en km et minutes
            val distanceKm = distance / 1000.0
            val durationMinutes = (duration / 60.0).toInt()
            
            RouteEstimate(
                distanceKm = distanceKm,
                durationMinutes = durationMinutes
            )
        } catch (e: Exception) {
            android.util.Log.e("RouteService", "Error getting route estimate: ${e.message}")
            null
        }
    }
}

/**
 * Données d'estimation de route
 */
data class RouteEstimate(
    val distanceKm: Double,
    val durationMinutes: Int
)

