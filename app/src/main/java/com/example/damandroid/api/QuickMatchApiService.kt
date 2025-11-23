package com.example.damandroid.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Query

/**
 * Interface Retrofit pour les endpoints QuickMatch
 * 
 * QuickMatch récupère les utilisateurs qui ont au moins un sport/intérêt commun
 * basé sur les sportsInterests de l'utilisateur connecté et les activités créées/jointe
 */
interface QuickMatchApiService {
    
    /**
     * Récupérer les profils pour QuickMatch
     * GET /quick-match/profiles?page=1&limit=20
     * 
     * Le backend filtre automatiquement pour retourner uniquement les utilisateurs qui ont :
     * - Au moins un sport/intérêt commun (sportsInterests)
     * - Basé sur les activités créées/jointe par l'utilisateur
     * 
     * Nécessite authentification JWT (géré par AuthInterceptor)
     * 
     * @param page Numéro de page (optionnel, défaut: 1)
     * @param limit Nombre de résultats par page (optionnel, défaut: 20)
     * @return Réponse paginée avec liste de profils et informations de pagination
     */
    @GET("quick-match/profiles")
    suspend fun getProfiles(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<QuickMatchProfilesResponse>
    
    /**
     * Enregistrer un "like" (swipe right)
     * POST /quick-match/like
     * 
     * Nécessite authentification JWT
     */
    @POST("quick-match/like")
    suspend fun likeProfile(
        @Body request: LikeProfileRequest
    ): Response<LikeProfileResponse>
    
    /**
     * Enregistrer un "pass" (swipe left)
     * POST /quick-match/pass
     * 
     * Nécessite authentification JWT
     */
    @POST("quick-match/pass")
    suspend fun passProfile(
        @Body request: PassProfileRequest
    ): Response<Unit>
    
    /**
     * Récupérer les likes reçus (notifications)
     * GET /quick-match/likes-received
     * 
     * Retourne la liste des utilisateurs qui ont liké le profil de l'utilisateur connecté
     * Nécessite authentification JWT
     */
    @GET("quick-match/likes-received")
    suspend fun getLikesReceived(): Response<LikesReceivedResponse>
    
    /**
     * Récupérer les matches de l'utilisateur connecté
     * GET /quick-match/matches
     * 
     * Retourne la liste des utilisateurs avec qui l'utilisateur connecté a matché
     * (les deux se sont likés mutuellement)
     * Nécessite authentification JWT
     */
    @GET("quick-match/matches")
    suspend fun getMatches(): Response<MatchesResponse>
}

/**
 * Réponse d'un profil QuickMatch depuis l'API
 * 
 * Le backend doit retourner les utilisateurs avec :
 * - Leurs informations de base (nom, âge, avatar, etc.)
 * - Leurs sports/intérêts (sportsInterests)
 * - Le nombre d'activités créées/jointe (activitiesJoined)
 * - La distance calculée depuis l'utilisateur connecté
 */
data class QuickMatchProfileResponse(
    val _id: String,
    val id: String? = null,
    val name: String,
    val age: Int?,
    val email: String?,
    val avatarUrl: String?,
    val coverImageUrl: String?,
    val location: String?,
    val distance: String?,
    val bio: String?,
    val about: String?,
    val sportsInterests: List<String>?,  // Sports/intérêts de l'utilisateur
    val sports: List<SportResponse>?,    // Sports détaillés (nom, icon, level)
    val interests: List<String>?,
    val rating: Double?,
    val activitiesJoined: Int?,          // Nombre d'activités créées/jointe
    val profileImageUrl: String?
) {
    fun getProfileId(): String = id ?: _id
    
    /**
     * Calculer l'âge depuis dateOfBirth si nécessaire
     */
    fun getAge(): Int {
        return age ?: 0
    }
}

/**
 * Sport avec détails (nom, icône, niveau)
 */
data class SportResponse(
    val name: String,
    val icon: String?,
    val level: String?
)

/**
 * Requête pour liker un profil
 */
data class LikeProfileRequest(
    val profileId: String
)

/**
 * Requête pour passer un profil
 */
data class PassProfileRequest(
    val profileId: String
)

/**
 * Réponse paginée des profils QuickMatch
 */
data class QuickMatchProfilesResponse(
    val profiles: List<QuickMatchProfileResponse>,
    val pagination: PaginationInfo?
)

/**
 * Informations de pagination
 */
data class PaginationInfo(
    val total: Int,
    val page: Int,
    val totalPages: Int,
    val limit: Int
)

/**
 * Réponse après un like (indique si c'est un match)
 */
data class LikeProfileResponse(
    val isMatch: Boolean,
    val matchedProfile: QuickMatchProfileResponse?
)

/**
 * Réponse pour les likes reçus
 */
data class LikesReceivedResponse(
    val likes: List<LikeReceivedItem>
)

/**
 * Item d'un like reçu
 */
data class LikeReceivedItem(
    val likeId: String,
    val fromUser: LikeUserInfo,
    val isMatch: Boolean, // true si l'utilisateur connecté a déjà liké ce profil en retour
    val matchId: String?, // ID du match si c'est un match
    val createdAt: String
)

/**
 * Informations sur l'utilisateur qui a liké
 */
data class LikeUserInfo(
    val _id: String,
    val id: String? = null,
    val name: String,
    val profileImageUrl: String?,
    val avatarUrl: String?
) {
    fun getUserId(): String = id ?: _id
    fun getAvatar(): String? = avatarUrl ?: profileImageUrl
}

/**
 * Réponse pour les matches
 */
data class MatchesResponse(
    val matches: List<MatchItem>
)

/**
 * Item d'un match
 */
data class MatchItem(
    val matchId: String,
    val matchedUser: MatchedUserInfo,
    val createdAt: String
)

/**
 * Informations sur l'utilisateur avec qui on a matché
 */
data class MatchedUserInfo(
    val _id: String,
    val id: String? = null,
    val name: String,
    val profileImageUrl: String?,
    val avatarUrl: String?
) {
    fun getUserId(): String = id ?: _id
    fun getAvatar(): String? = avatarUrl ?: profileImageUrl
}

