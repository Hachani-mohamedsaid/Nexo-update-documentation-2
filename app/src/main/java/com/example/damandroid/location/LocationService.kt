package com.example.damandroid.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service pour récupérer la localisation de l'utilisateur
 * Utilise le GPS ou le réseau (100% gratuit, pas besoin d'API)
 */
class LocationService(private val context: Context) {
    
    /**
     * Récupère la localisation actuelle de l'utilisateur
     * Retourne null si les permissions ne sont pas accordées ou si la localisation n'est pas disponible
     */
    suspend fun getCurrentLocation(): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        // Vérifier les permissions
        if (!hasLocationPermission()) {
            return@withContext null
        }
        
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null
        
        try {
            // Essayer d'obtenir la dernière position connue
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                        bestLocation = location
                    }
                }
            }
            
            bestLocation?.let {
                return@withContext Pair(it.latitude, it.longitude)
            }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationService", "SecurityException: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("LocationService", "Exception: ${e.message}")
        }
        
        null
    }
    
    /**
     * Vérifie si l'application a les permissions de localisation
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Vérifie si le GPS est activé
     */
    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
               locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    }
}

