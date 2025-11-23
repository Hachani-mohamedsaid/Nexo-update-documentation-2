package com.example.damandroid.data.datasource

import com.example.damandroid.api.RetrofitClient
import com.example.damandroid.api.UpdateProfileRequestDto
import com.example.damandroid.api.UserApiService
import com.example.damandroid.api.UserResponseDto
import com.example.damandroid.api.ChangePasswordRequestDto
import com.example.damandroid.api.MessageResponse
import com.example.damandroid.api.ActivityApiService
import com.example.damandroid.api.AchievementsApiService
import com.example.damandroid.api.EarnedBadgeDto
import com.example.damandroid.auth.UserSession
import com.example.damandroid.data.model.AchievementDto
import com.example.damandroid.data.model.ProfileActivityDto
import com.example.damandroid.data.model.ProfileMedalDto
import com.example.damandroid.data.model.UserProfileDto
import com.example.damandroid.data.model.UserStatsDto
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ProfileRemoteDataSourceImpl(
    private val userApiService: UserApiService = RetrofitClient.userApiService,
    private val activityApiService: ActivityApiService = RetrofitClient.activityApiService,
    private val achievementsApiService: AchievementsApiService = RetrofitClient.achievementsApiService
) : ProfileRemoteDataSource {

    override suspend fun fetchCurrentUser(): UserProfileDto {
        android.util.Log.d("ProfileRemoteDataSource", "=== fetchCurrentUser() called ===")
        val token = UserSession.token
        val currentUserId = UserSession.user?.id
        android.util.Log.d("ProfileRemoteDataSource", "Token available: ${!token.isNullOrBlank()}, UserId: $currentUserId")
        
        val networkProfile = if (!token.isNullOrBlank()) {
            runCatching {
                val response = userApiService.getProfile("Bearer $token")
                if (response.isSuccessful) {
                    android.util.Log.d("ProfileRemoteDataSource", "Profile response successful: ${response.body()?._id ?: response.body()?.id}")
                    response.body()
                } else {
                    android.util.Log.e("ProfileRemoteDataSource", "Profile response failed: ${response.code()}")
                    null
                }
            }.getOrNull().also {
                android.util.Log.d("ProfileRemoteDataSource", "networkProfile is null: ${it == null}")
            }
        } else {
            null
        }

        // Obtenir l'ID utilisateur depuis networkProfile si UserSession.user?.id est null
        val resolvedUserId = currentUserId ?: networkProfile?._id ?: networkProfile?.id
        android.util.Log.d("ProfileRemoteDataSource", "Resolved UserId: $resolvedUserId (from session: $currentUserId, from profile: ${networkProfile?._id ?: networkProfile?.id})")
        
        // Si resolvedUserId est toujours null, essayer d'extraire depuis le token JWT
        val finalUserId = resolvedUserId ?: run {
            try {
                val jwtPayload = token?.split(".")?.get(1) ?: return@run null
                val decoded = String(android.util.Base64.decode(jwtPayload, android.util.Base64.URL_SAFE), Charsets.UTF_8)
                val json = org.json.JSONObject(decoded)
                json.optString("sub", null).takeIf { it.isNotEmpty() }
            } catch (e: Exception) {
                android.util.Log.e("ProfileRemoteDataSource", "Error extracting userId from JWT: ${e.message}")
                null
            }
        }
        android.util.Log.d("ProfileRemoteDataSource", "Final UserId: $finalUserId")

        // Calculer les statistiques depuis les activités si non disponibles dans le backend
        var calculatedSessionsHosted: Int? = null
        var calculatedSessionsJoined: Int? = null
        var realActivities: List<ProfileActivityDto>? = null
        var realMedals: List<ProfileMedalDto>? = null

        // Récupérer les badges obtenus (médailles) - ne nécessite pas userId, utilise le token JWT
        if (token != null) {
            runCatching {
                android.util.Log.d("ProfileRemoteDataSource", "Fetching badges...")
                val badgesResponse = achievementsApiService.getBadges()
                if (badgesResponse.isSuccessful) {
                    val badges = badgesResponse.body()
                    android.util.Log.d("ProfileRemoteDataSource", "Badges response received: ${badges?.earnedBadges?.size} earned badges")
                    realMedals = badges?.earnedBadges?.map { 
                        android.util.Log.d("ProfileRemoteDataSource", "Mapping badge: ${it.name} with iconUrl: ${it.iconUrl}")
                        it.toProfileMedalDto() 
                    } ?: emptyList()
                    android.util.Log.d("ProfileRemoteDataSource", "Mapped medals: ${realMedals?.size}")
                } else {
                    android.util.Log.e("ProfileRemoteDataSource", "Badges response not successful: ${badgesResponse.code()}")
                }
            }.onFailure { e ->
                android.util.Log.e("ProfileRemoteDataSource", "Error fetching badges: ${e.message}", e)
            }
        }

        // Récupérer les activités créées - nécessite userId
        if (finalUserId != null && token != null) {
            runCatching {
                // Calculer sessionsHosted et récupérer les activités créées
                android.util.Log.d("ProfileRemoteDataSource", "Fetching my activities...")
                val myActivitiesResponse = activityApiService.getMyActivities()
                android.util.Log.d("ProfileRemoteDataSource", "My activities response: isSuccessful=${myActivitiesResponse.isSuccessful}, code=${myActivitiesResponse.code()}")
                if (myActivitiesResponse.isSuccessful) {
                    val myActivities = myActivitiesResponse.body() ?: emptyList()
                    android.util.Log.d("ProfileRemoteDataSource", "My activities count: ${myActivities.size}")
                    calculatedSessionsHosted = myActivities.size
                    // Mapper les activités vers ProfileActivityDto
                    realActivities = myActivities.map { it.toProfileActivityDto() }
                    android.util.Log.d("ProfileRemoteDataSource", "Mapped activities count: ${realActivities?.size}")
                } else {
                    android.util.Log.e("ProfileRemoteDataSource", "Failed to fetch my activities: ${myActivitiesResponse.code()}")
                }

                // Calculer sessionsJoined (activités où l'utilisateur est participant)
                val allActivitiesResponse = activityApiService.getActivities(visibility = "public")
                if (allActivitiesResponse.isSuccessful) {
                    val allActivities = allActivitiesResponse.body() ?: emptyList()
                    calculatedSessionsJoined = allActivities.count { activity ->
                        activity.participantIds?.contains(finalUserId) == true
                    }
                }
            }.onFailure { e ->
                android.util.Log.e("ProfileRemoteDataSource", "Error fetching profile data: ${e.message}", e)
                e.printStackTrace()
            }
        }

        android.util.Log.d("ProfileRemoteDataSource", "Creating UserProfileDto with: activities=${realActivities?.size}, medals=${realMedals?.size}")
        
        return networkProfile.toDomainProfile(
            calculatedSessionsHosted = calculatedSessionsHosted,
            calculatedSessionsJoined = calculatedSessionsJoined,
            realActivities = realActivities,
            realMedals = realMedals
        )
    }

    override suspend fun getUserProfileById(userId: String): UserProfileDto {
        val token = UserSession.token
            ?: throw IllegalStateException("Cannot fetch user profile without authentication token")

        val response = userApiService.getUserProfileById(
            bearerToken = "Bearer $token",
            userId = userId
        )

        if (!response.isSuccessful) {
            when (response.code()) {
                404 -> throw Exception("Endpoint /users/{id}/profile not found. This endpoint needs to be created in the backend.")
                else -> throw HttpException(response)
            }
        }

        val userProfile = response.body()
            ?: throw Exception("Empty response from server")
        
        // Vérifier si c'est l'utilisateur connecté
        val currentUserId = UserSession.user?.id
        val profileId = userProfile.id ?: userProfile._id
        
        // Comparer les IDs de différentes manières pour être sûr
        val isCurrentUser = when {
            currentUserId == null -> false
            currentUserId == userId -> true
            currentUserId == profileId -> true
            userId == profileId && profileId != null -> {
                // Si userId et profileId correspondent, c'est probablement le même utilisateur
                // On vérifie aussi si currentUserId correspond
                currentUserId == userId || currentUserId == profileId
            }
            else -> false
        }
        
        android.util.Log.d("ProfileRemoteDataSource", "User check: userId=$userId, currentUserId=$currentUserId, profileId=$profileId, isCurrentUser=$isCurrentUser")
        
        // Récupérer les activités et calculer les statistiques pour cet utilisateur
        var realActivities: List<ProfileActivityDto>? = null
        var realMedals: List<ProfileMedalDto>? = null
        var calculatedSessionsHosted: Int? = null
        var calculatedSessionsJoined: Int? = null
        
        runCatching {
            android.util.Log.d("ProfileRemoteDataSource", "Fetching activities for user $userId (isCurrentUser: $isCurrentUser)...")
            
            // Récupérer toutes les activités publiques pour trouver celles de cet utilisateur
            val allActivitiesResponse = activityApiService.getActivities(visibility = "public")
            if (allActivitiesResponse.isSuccessful) {
                val allActivities = allActivitiesResponse.body() ?: emptyList()
                android.util.Log.d("ProfileRemoteDataSource", "Total public activities: ${allActivities.size}")
                
                // Filtrer les activités créées par cet utilisateur
                val userCreatedActivities = allActivities.filter { activity ->
                    activity.getCreatorId() == userId
                }
                android.util.Log.d("ProfileRemoteDataSource", "Activities created by user $userId: ${userCreatedActivities.size}")
                
                // Calculer sessionsHosted (nombre d'activités créées)
                calculatedSessionsHosted = userCreatedActivities.size
                
                // Mapper les activités vers ProfileActivityDto
                realActivities = userCreatedActivities.map { it.toProfileActivityDto() }
                
                // Calculer sessionsJoined (nombre d'activités où l'utilisateur est participant)
                calculatedSessionsJoined = allActivities.count { activity ->
                    activity.participantIds?.contains(userId) == true
                }
                android.util.Log.d("ProfileRemoteDataSource", "Activities joined by user $userId: $calculatedSessionsJoined")
            } else {
                android.util.Log.e("ProfileRemoteDataSource", "Failed to fetch activities: ${allActivitiesResponse.code()}")
            }
            
            // Récupérer les badges (médailles) - toujours essayer si on a un token
            // L'API getBadges() retourne les badges de l'utilisateur connecté
            // Si c'est un autre utilisateur, on ne pourra pas récupérer ses badges (limitation API)
            if (token != null) {
                android.util.Log.d("ProfileRemoteDataSource", "Fetching badges (isCurrentUser: $isCurrentUser)...")
                try {
                    val badgesResponse = achievementsApiService.getBadges()
                    if (badgesResponse.isSuccessful) {
                        val badges = badgesResponse.body()
                        android.util.Log.d("ProfileRemoteDataSource", "Badges response received: ${badges?.earnedBadges?.size} earned badges")
                        
                        // Si c'est l'utilisateur connecté, utiliser les badges
                        // Sinon, on ne peut pas récupérer les badges d'un autre utilisateur
                        if (isCurrentUser) {
                            realMedals = badges?.earnedBadges?.map { 
                                android.util.Log.d("ProfileRemoteDataSource", "Mapping badge: ${it.name} with iconUrl: ${it.iconUrl}, rarity: ${it.rarity}")
                                it.toProfileMedalDto() 
                            } ?: emptyList()
                            android.util.Log.d("ProfileRemoteDataSource", "Mapped medals: ${realMedals?.size}")
                        } else {
                            android.util.Log.d("ProfileRemoteDataSource", "Not current user, skipping badges (API limitation)")
                            realMedals = emptyList()
                        }
                    } else {
                        android.util.Log.e("ProfileRemoteDataSource", "Badges response not successful: ${badgesResponse.code()}, message: ${badgesResponse.message()}")
                        realMedals = emptyList()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ProfileRemoteDataSource", "Error fetching badges: ${e.message}", e)
                    realMedals = emptyList()
                }
            } else {
                android.util.Log.d("ProfileRemoteDataSource", "No token available, cannot fetch badges")
                realMedals = emptyList()
            }
        }.onFailure { e ->
            android.util.Log.e("ProfileRemoteDataSource", "Error fetching activities/stats for user $userId: ${e.message}", e)
        }
        
        return userProfile.toDomainProfile(
            userId = userId,
            calculatedSessionsHosted = calculatedSessionsHosted,
            calculatedSessionsJoined = calculatedSessionsJoined,
            realActivities = realActivities,
            realMedals = realMedals
        )
    }

    override suspend fun updateProfile(
        userId: String,
        body: UpdateProfileRequestDto
    ): UserProfileDto {
        val token = UserSession.token
            ?: throw IllegalStateException("Cannot update profile without authentication token")

        val response = userApiService.updateProfile(
            bearerToken = "Bearer $token",
            userId = userId,
            body = body
        )

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val updated = response.body()
        return updated.toDomainProfile()
    }

    override suspend fun uploadProfileImage(
        userId: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): UserProfileDto {
        val token = UserSession.token
            ?: throw IllegalStateException("Cannot upload profile image without authentication token")

        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val multipart = MultipartBody.Part.createFormData("image", fileName, requestBody)

        val response = userApiService.uploadProfileImage(
            bearerToken = "Bearer $token",
            userId = userId,
            image = multipart
        )

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val updated = response.body()
        return updated.toDomainProfile()
    }

    override suspend fun changePassword(
        userId: String,
        request: ChangePasswordRequestDto
    ): MessageResponse {
        val token = UserSession.token
            ?: throw IllegalStateException("Cannot change password without authentication token")

        val response = userApiService.changePassword(
            bearerToken = "Bearer $token",
            userId = userId,
            body = request
        )

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        return response.body() ?: MessageResponse(message = "Password updated successfully")
    }

    private fun UserResponseDto?.toDomainProfile(
        userId: String? = null,
        calculatedSessionsHosted: Int? = null,
        calculatedSessionsJoined: Int? = null,
        realActivities: List<ProfileActivityDto>? = null,
        realMedals: List<ProfileMedalDto>? = null
    ): UserProfileDto {
        val sessionUser = UserSession.user
        // Utiliser userId si fourni, sinon utiliser les valeurs du profil, sinon sessionUser
        val resolvedId = this?.id ?: this?._id ?: userId ?: sessionUser?.id ?: "user_123"
        val resolvedName = this?.name ?: sessionUser?.name ?: "Alex Thompson"
        val resolvedLocation = this?.location ?: sessionUser?.location ?: "Los Angeles, CA"
        val resolvedBio = this?.about ?: "Fitness enthusiast and outdoor adventurer"
        val resolvedSports = this?.sportsInterests.takeUnless { it.isNullOrEmpty() }
            ?: listOf("Running", "Swimming", "Hiking", "Cycling")

        // Utiliser les valeurs du backend si disponibles, sinon utiliser les valeurs calculées, sinon 0
        val sessionsHosted = this?.sessionsHosted ?: calculatedSessionsHosted ?: 0
        val sessionsJoined = this?.sessionsJoined ?: calculatedSessionsJoined ?: 0

        return UserProfileDto(
            id = resolvedId,
            name = resolvedName,
            avatarUrl = this?.profileImageUrl
                ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${resolvedName.replace(" ", "")}",
            bio = resolvedBio,
            location = resolvedLocation,
            isVerified = this?.isEmailVerified ?: (sessionUser != null),
            stats = UserStatsDto(
                sessionsJoined = sessionsJoined,
                sessionsHosted = sessionsHosted,
                followers = this?.followers ?: 0,
                following = this?.following ?: 0,
                favoriteSports = resolvedSports,
                rating = this?.rating
            ),
            achievements = listOf(
                AchievementDto(
                    id = "achv_1",
                    title = "Consistency Champion",
                    description = "Completed 10 sessions in a row",
                    icon = "🔥"
                ),
                AchievementDto(
                    id = "achv_2",
                    title = "Trail Explorer",
                    description = "Hosted 5 outdoor activities",
                    icon = "🥾"
                )
            ),
            activities = realActivities ?: emptyList(),
            medals = realMedals ?: emptyList()
        ).also {
            android.util.Log.d("ProfileRemoteDataSource", "=== UserProfileDto created: activities=${it.activities.size}, medals=${it.medals.size} ===")
            it.activities.forEach { activity ->
                android.util.Log.d("ProfileRemoteDataSource", "Activity: ${activity.title} - ${activity.date} ${activity.time}")
            }
            it.medals.forEach { medal ->
                android.util.Log.d("ProfileRemoteDataSource", "Medal: ${medal.title} - ${medal.rarity}")
            }
        }
    }

    /**
     * Convertit ActivityResponse vers ProfileActivityDto
     */
    private fun com.example.damandroid.api.ActivityResponse.toProfileActivityDto(): ProfileActivityDto {
        // Formater la date
        val formattedDate = try {
            if (date.isNotEmpty()) {
                val instant = Instant.parse(date)
                val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                val today = LocalDate.now()
                when {
                    localDate == today -> "Today"
                    localDate == today.plusDays(1) -> "Tomorrow"
                    localDate.isBefore(today) -> localDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                    else -> localDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                }
            } else {
                "TBD"
            }
        } catch (e: Exception) {
            date
        }

        // Formater l'heure
        val formattedTime = try {
            if (time.isNotEmpty()) {
                val instant = Instant.parse(time)
                val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
                localTime.format(DateTimeFormatter.ofPattern("h:mm a"))
            } else {
                "TBD"
            }
        } catch (e: Exception) {
            time
        }

        // Obtenir l'icône du sport
        val sportIcon = getSportIconForProfile(sportType)

        // Déterminer le statut (upcoming, completed, cancelled)
        // Pour l'instant, on considère que si la date est passée, c'est completed
        val status = try {
            if (date.isNotEmpty()) {
                val instant = Instant.parse(date)
                val activityDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                val today = LocalDate.now()
                if (activityDate.isBefore(today)) {
                    "completed"
                } else {
                    "upcoming"
                }
            } else {
                "upcoming"
            }
        } catch (e: Exception) {
            "upcoming"
        }

        return ProfileActivityDto(
            id = getActivityId(),
            title = title,
            sportIcon = sportIcon,
            date = formattedDate,
            time = formattedTime,
            location = location,
            status = status
        )
    }

    /**
     * Convertit EarnedBadgeDto vers ProfileMedalDto
     */
    private fun EarnedBadgeDto.toProfileMedalDto(): ProfileMedalDto {
        // Utiliser iconUrl si c'est une URL, sinon utiliser l'emoji par défaut
        // Note: iconUrl peut être une URL ou un emoji selon le backend
        val icon = when {
            iconUrl.isNotEmpty() && (iconUrl.startsWith("http://") || iconUrl.startsWith("https://")) -> {
                // C'est une URL, l'utiliser directement
                iconUrl
            }
            iconUrl.isNotEmpty() -> {
                // C'est probablement un emoji, l'utiliser directement
                iconUrl
            }
            else -> {
                // Fallback vers un emoji par défaut
                "🏅"
            }
        }

        android.util.Log.d("ProfileRemoteDataSource", "Mapping badge: _id=$_id, name=$name, iconUrl=$iconUrl, mapped icon=$icon, rarity=$rarity")

        return ProfileMedalDto(
            id = _id,
            title = name,
            description = description,
            icon = icon,
            rarity = rarity.lowercase()
        )
    }

    /**
     * Fonction utilitaire pour obtenir l'icône du sport
     */
    private fun getSportIconForProfile(sportType: String): String {
        return when (sportType.lowercase()) {
            "football", "soccer" -> "⚽"
            "basketball" -> "🏀"
            "running" -> "🏃"
            "cycling" -> "🚴"
            "swimming" -> "🏊"
            "tennis" -> "🎾"
            "volleyball" -> "🏐"
            "badminton" -> "🏸"
            "table tennis", "ping pong" -> "🏓"
            "golf" -> "⛳"
            "baseball" -> "⚾"
            "hiking", "trekking" -> "🥾"
            "yoga" -> "🧘"
            "gym", "fitness" -> "💪"
            "dancing" -> "💃"
            "boxing" -> "🥊"
            "martial arts" -> "🥋"
            "skating" -> "⛸️"
            "skiing" -> "⛷️"
            "surfing" -> "🏄"
            "climbing", "rock climbing" -> "🧗"
            else -> "🎯"
        }
    }
}

