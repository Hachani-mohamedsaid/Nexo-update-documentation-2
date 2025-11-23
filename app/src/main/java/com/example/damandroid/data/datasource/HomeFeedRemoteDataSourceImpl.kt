package com.example.damandroid.data.datasource

import com.example.damandroid.api.RetrofitClient
import com.example.damandroid.data.mapper.toHomeActivityDto
import com.example.damandroid.data.model.HomeFeedDto
import com.example.damandroid.data.model.SportCategoryDto
import retrofit2.HttpException
import java.io.IOException

class HomeFeedRemoteDataSourceImpl : HomeFeedRemoteDataSource {

    private val activityApiService = RetrofitClient.activityApiService

    override suspend fun fetchHomeFeed(): HomeFeedDto {
        return try {
            // Récupérer les activités publiques
            val publicResponse = activityApiService.getActivities(visibility = "public")
            
            val allActivities = mutableListOf<com.example.damandroid.api.ActivityResponse>()
            
            // Ajouter les activités publiques
            if (publicResponse.isSuccessful) {
                publicResponse.body()?.let { allActivities.addAll(it) }
            } else {
                // Si erreur pour les activités publiques, logger mais continuer
                android.util.Log.w("HomeFeedRemoteDataSource", "Failed to fetch public activities: ${publicResponse.code()}")
            }
            
            // Récupérer les activités "friends" (visibles uniquement pour les matches)
            // Note: Si l'utilisateur n'est pas authentifié, cette requête peut échouer (401/403)
            // On ignore l'erreur et on continue avec les activités publiques uniquement
            try {
                val friendsResponse = activityApiService.getActivities(visibility = "friends")
                if (friendsResponse.isSuccessful) {
                    friendsResponse.body()?.let { allActivities.addAll(it) }
                } else {
                    // Si 401/403, l'utilisateur n'est pas authentifié ou n'a pas de matches
                    // C'est normal, on continue sans les activités "friends"
                    android.util.Log.d("HomeFeedRemoteDataSource", "Friends activities not available: ${friendsResponse.code()}")
                }
            } catch (e: Exception) {
                // Ignorer les erreurs pour les activités "friends" (peut être normal si pas authentifié)
                android.util.Log.d("HomeFeedRemoteDataSource", "Could not fetch friends activities: ${e.message}")
            }
            
            // Convertir en DTOs
            val activities = allActivities.map { it.toHomeActivityDto() }
            
            // Récupérer les catégories de sport
            val sportCategories = getSportCategories()
            
            HomeFeedDto(
                activities = activities,
                sportCategories = sportCategories
            )
        } catch (e: HttpException) {
            throw Exception("Network error: ${e.message}")
        } catch (e: IOException) {
            throw Exception("Connection error: Please check your internet connection")
        } catch (e: Exception) {
            throw e
        }
    }
    
    override suspend fun fetchMyActivities(): HomeFeedDto {
        return try {
            // Récupérer les activités créées par l'utilisateur connecté
            val response = activityApiService.getMyActivities()
            
            if (response.isSuccessful) {
                val activityResponses = response.body() ?: emptyList()
                // Utiliser participantIds directement depuis la réponse (pas besoin d'appels API supplémentaires)
                val activities = activityResponses.map { it.toHomeActivityDto() }
                
                // Récupérer les catégories de sport (pour l'instant, on utilise des valeurs hardcodées)
                val sportCategories = getSportCategories()
                
                HomeFeedDto(
                    activities = activities,
                    sportCategories = sportCategories
                )
            } else {
                when (response.code()) {
                    401 -> throw Exception("Unauthorized: Please login again")
                    403 -> throw Exception("Forbidden: Access denied")
                    404 -> throw Exception("No activities found")
                    500 -> throw Exception("Server error: Please try again later")
                    else -> throw Exception("Failed to fetch my activities: ${response.code()}")
                }
            }
        } catch (e: HttpException) {
            throw Exception("Network error: ${e.message}")
        } catch (e: IOException) {
            throw Exception("Connection error: Please check your internet connection")
        } catch (e: Exception) {
            throw e
        }
    }
    
    private fun getSportCategories(): List<SportCategoryDto> {
        return listOf(
            SportCategoryDto(id = "football", name = "Football", icon = "⚽"),
            SportCategoryDto(id = "basketball", name = "Basketball", icon = "🏀"),
            SportCategoryDto(id = "running", name = "Running", icon = "🏃"),
            SportCategoryDto(id = "cycling", name = "Cycling", icon = "🚴"),
            SportCategoryDto(id = "tennis", name = "Tennis", icon = "🎾"),
            SportCategoryDto(id = "swimming", name = "Swimming", icon = "🏊"),
            SportCategoryDto(id = "yoga", name = "Yoga", icon = "🧘"),
            SportCategoryDto(id = "volleyball", name = "Volleyball", icon = "🏐")
        )
    }
}
