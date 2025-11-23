package com.example.damandroid.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class LocationRepository {
    private val openWeatherService = RetrofitClient.openWeatherService
    
    suspend fun searchCities(query: String): List<CityLocation> = withContext(Dispatchers.IO) {
        try {
            if (query.length < 2) {
                return@withContext emptyList()
            }
            
            val response = openWeatherService.searchCities(query, limit = 5)
            
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                android.util.Log.e("LocationRepository", "Error searching cities: ${response.code()}")
                emptyList()
            }
        } catch (e: HttpException) {
            android.util.Log.e("LocationRepository", "HttpException: ${e.code()}")
            emptyList()
        } catch (e: IOException) {
            android.util.Log.e("LocationRepository", "IOException: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            android.util.Log.e("LocationRepository", "Exception: ${e.message}")
            emptyList()
        }
    }
}

