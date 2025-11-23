package com.example.damandroid.data.datasource

import com.example.damandroid.api.LikeProfileRequest
import com.example.damandroid.api.LikeProfileResponse
import com.example.damandroid.api.PassProfileRequest
import com.example.damandroid.api.RetrofitClient
import com.example.damandroid.data.mapper.toMatchUserProfileDto
import com.example.damandroid.data.model.MatchUserProfileDto
import retrofit2.HttpException
import java.io.IOException

/**
 * Implémentation de la data source pour QuickMatch
 * 
 * Récupère les profils utilisateurs depuis l'API backend NestJS.
 * Le backend filtre automatiquement pour retourner uniquement les utilisateurs
 * qui ont au moins un sport/intérêt commun (sportsInterests) avec l'utilisateur connecté.
 */
class QuickMatchRemoteDataSourceImpl : QuickMatchRemoteDataSource {
    
    private val quickMatchApiService = RetrofitClient.quickMatchApiService

    override suspend fun fetchProfiles(): List<MatchUserProfileDto> {
        return try {
            // Récupérer la première page avec 50 résultats par défaut pour avoir plus de profils
            // Si le backend ne retourne qu'un seul profil, augmenter la limite peut aider
            android.util.Log.d("QuickMatchDataSource", "Fetching profiles: page=1, limit=50")
            val response = quickMatchApiService.getProfiles(page = 1, limit = 50)
            
            android.util.Log.d("QuickMatchDataSource", "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                android.util.Log.d("QuickMatchDataSource", "Response body: ${responseBody != null}")
                
                if (responseBody == null) {
                    android.util.Log.e("QuickMatchDataSource", "❌ Response body is NULL!")
                    return emptyList()
                }
                
                // Extraire la liste des profils depuis la réponse paginée
                val rawProfiles = responseBody.profiles ?: emptyList()
                android.util.Log.d("QuickMatchDataSource", "Raw profiles from API: ${rawProfiles.size}")
                
                // Vérifier si la réponse contient directement une liste au lieu d'un objet avec "profiles"
                if (rawProfiles.isEmpty() && responseBody is List<*>) {
                    android.util.Log.w("QuickMatchDataSource", "⚠️ Response might be a direct list, not an object with 'profiles' field")
                }
                
                val profiles = rawProfiles.map { 
                    android.util.Log.d("QuickMatchDataSource", "Mapping profile: ${it.name} (id: ${it.getProfileId()})")
                    it.toMatchUserProfileDto() 
                }
                val pagination = responseBody?.pagination
                
                // Log détaillé pour debug
                android.util.Log.d("QuickMatchDataSource", "✅ Profiles loaded: ${profiles.size}")
                android.util.Log.d("QuickMatchDataSource", "Pagination: total=${pagination?.total}, page=${pagination?.page}, totalPages=${pagination?.totalPages}, limit=${pagination?.limit}")
                
                if (profiles.isEmpty()) {
                    android.util.Log.e("QuickMatchDataSource", "❌ No profiles returned from backend!")
                    android.util.Log.e("QuickMatchDataSource", "❌ CAUSE POSSIBLE:")
                    android.util.Log.e("QuickMatchDataSource", "   1. Aucun utilisateur avec sports communs")
                    android.util.Log.e("QuickMatchDataSource", "   2. Tous les profils sont déjà likés/passés (backend les exclut)")
                    android.util.Log.e("QuickMatchDataSource", "   3. Problème de filtrage dans le backend")
                } else if (profiles.size == 1) {
                    android.util.Log.w("QuickMatchDataSource", "⚠️ PROBLÈME: Only ONE profile returned!")
                    android.util.Log.w("QuickMatchDataSource", "   Profile: ${profiles[0].name} (${profiles[0].id})")
                    android.util.Log.w("QuickMatchDataSource", "   Pagination total: ${pagination?.total}")
                    android.util.Log.w("QuickMatchDataSource", "   Total pages: ${pagination?.totalPages}")
                    android.util.Log.w("QuickMatchDataSource", "⚠️ CAUSE POSSIBLE:")
                    android.util.Log.w("QuickMatchDataSource", "   1. Seulement 1 utilisateur compatible dans la base")
                    android.util.Log.w("QuickMatchDataSource", "   2. Backend exclut les profils likés/passés (même si c'est le seul disponible)")
                    android.util.Log.w("QuickMatchDataSource", "   3. Filtrage trop strict (sports communs)")
                    android.util.Log.w("QuickMatchDataSource", "⚠️ SOLUTION BACKEND:")
                    android.util.Log.w("QuickMatchDataSource", "   - Retourner plus de profils (relâcher les filtres)")
                    android.util.Log.w("QuickMatchDataSource", "   - Ne pas exclure les profils likés si c'est le seul disponible")
                    android.util.Log.w("QuickMatchDataSource", "   - Vérifier la logique de filtrage par sports communs")
                } else {
                    android.util.Log.d("QuickMatchDataSource", "✅ Multiple profiles returned: ${profiles.size}")
                }
                
                profiles
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("QuickMatchDataSource", "❌ API Error: code=${response.code()}, message=${response.message()}, body=$errorBody")
                
                when (response.code()) {
                    401 -> throw Exception("Unauthorized: Please login again")
                    403 -> throw Exception("Forbidden: Access denied")
                    404 -> throw Exception("No profiles found")
                    500 -> throw Exception("Server error: Please try again later")
                    else -> throw Exception("Failed to fetch profiles: ${response.code()} - ${errorBody ?: response.message()}")
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

    override suspend fun likeProfile(profileId: String): LikeProfileResponse {
        return try {
            val response = quickMatchApiService.likeProfile(
                LikeProfileRequest(profileId = profileId)
            )
            
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Empty response")
            } else {
                when (response.code()) {
                    401 -> throw Exception("Unauthorized: Please login again")
                    403 -> throw Exception("Forbidden: Access denied")
                    404 -> throw Exception("Profile not found")
                    409 -> throw Exception("Profile already liked or passed")
                    500 -> throw Exception("Server error: Please try again later")
                    else -> throw Exception("Failed to like profile: ${response.code()}")
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

    override suspend fun passProfile(profileId: String) {
        try {
            val response = quickMatchApiService.passProfile(
                PassProfileRequest(profileId = profileId)
            )
            
            if (!response.isSuccessful) {
                when (response.code()) {
                    401 -> throw Exception("Unauthorized: Please login again")
                    403 -> throw Exception("Forbidden: Access denied")
                    404 -> throw Exception("Profile not found")
                    409 -> throw Exception("Profile already passed or liked")
                    500 -> throw Exception("Server error: Please try again later")
                    else -> throw Exception("Failed to pass profile: ${response.code()}")
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
}

