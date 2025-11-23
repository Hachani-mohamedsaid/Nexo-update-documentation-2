package com.example.damandroid.location

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

/**
 * Service de géocodage utilisant OpenStreetMap Nominatim (100% gratuit)
 * Convertit les adresses en coordonnées latitude/longitude
 */
class GeocodingService {
    
    /**
     * Convertit une adresse en coordonnées latitude/longitude
     * @param address L'adresse à géocoder (ex: "123 Main St, New York, NY")
     * @return Pair<latitude, longitude> ou null si l'adresse n'a pas pu être géocodée
     */
    suspend fun geocode(address: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            // Encoder l'adresse pour l'URL
            val encodedAddress = URLEncoder.encode(address, "UTF-8")
            
            // Appeler l'API Nominatim d'OpenStreetMap (100% gratuit)
            val url = "https://nominatim.openstreetmap.org/search?q=$encodedAddress&format=json&limit=1"
            
            val connection = java.net.URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "DamAndroidApp/1.0")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            
            // Parser la réponse JSON
            val jsonArray = org.json.JSONArray(response)
            if (jsonArray.length() > 0) {
                val firstResult = jsonArray.getJSONObject(0)
                val lat = firstResult.getDouble("lat")
                val lon = firstResult.getDouble("lon")
                return@withContext Pair(lat, lon)
            }
        } catch (e: Exception) {
            android.util.Log.e("GeocodingService", "Error geocoding address '$address': ${e.message}")
        }
        
        null
    }
    
    /**
     * Géocode plusieurs adresses en parallèle
     */
    suspend fun geocodeBatch(addresses: List<String>): Map<String, Pair<Double, Double>> = withContext(Dispatchers.IO) {
        addresses.mapNotNull { address ->
            geocode(address)?.let { coordinates ->
                address to coordinates
            }
        }.toMap()
    }
    
    /**
     * Reverse geocoding: Convertit des coordonnées en adresse (utilise OpenStreetMap Nominatim)
     * @param latitude Latitude du point
     * @param longitude Longitude du point
     * @return L'adresse complète ou null si l'adresse n'a pas pu être trouvée
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        try {
            // Appeler l'API Nominatim d'OpenStreetMap pour le reverse geocoding (100% gratuit)
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$latitude&lon=$longitude&format=json&addressdetails=1"
            
            val connection = java.net.URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "DamAndroidApp/1.0")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            
            // Parser la réponse JSON
            val jsonObject = JSONObject(response)
            val address = jsonObject.optJSONObject("address")
            
            if (address != null) {
                // Construire l'adresse à partir des composants
                val components = mutableListOf<String>()
                
                // Priorité: road > house_number > amenity > building > locality > city
                val road = address.optString("road", "")
                val houseNumber = address.optString("house_number", "")
                val amenity = address.optString("amenity", "")
                val building = address.optString("building", "")
                val locality = address.optString("locality", "")
                val city = address.optString("city", "")
                val town = address.optString("town", "")
                val village = address.optString("village", "")
                val state = address.optString("state", "")
                val country = address.optString("country", "")
                
                // Construire le nom principal
                val name = when {
                    !road.isEmpty() && !houseNumber.isEmpty() -> "$houseNumber $road"
                    !road.isEmpty() -> road
                    !amenity.isEmpty() -> amenity
                    !building.isEmpty() -> building
                    !locality.isEmpty() -> locality
                    !city.isEmpty() -> city
                    !town.isEmpty() -> town
                    !village.isEmpty() -> village
                    else -> jsonObject.optString("display_name", "Unknown Location")
                }
                
                // Ajouter les composants supplémentaires
                val cityName = city.ifEmpty { town.ifEmpty { village } }
                if (!cityName.isEmpty() && cityName != name) {
                    components.add(cityName)
                }
                if (!state.isEmpty()) {
                    components.add(state)
                }
                
                // Retourner l'adresse formatée
                if (components.isNotEmpty()) {
                    return@withContext "$name, ${components.joinToString(", ")}"
                } else {
                    return@withContext name
                }
            } else {
                // Fallback: utiliser display_name si disponible
                val displayName = jsonObject.optString("display_name", null)
                return@withContext displayName ?: "Unknown Location"
            }
        } catch (e: Exception) {
            android.util.Log.e("GeocodingService", "Error reverse geocoding coordinates ($latitude, $longitude): ${e.message}")
        }
        
        null
    }
    
    /**
     * Calcule la distance entre deux points en kilomètres (formule de Haversine)
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val R = 6371.0 // Rayon de la Terre en kilomètres
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return R * c
    }
}

