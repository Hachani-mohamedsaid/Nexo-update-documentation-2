package com.example.damandroid.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit pour les endpoints d'activités
 */
interface ActivityApiService {
    
    /**
     * Récupérer la liste des activités
     * GET /activities?visibility=public
     * 
     * @param visibility Optionnel: "public" ou "friends"
     * Pas d'authentification requise pour visibility=public
     */
    @GET("activities")
    suspend fun getActivities(
        @Query("visibility") visibility: String? = null,
        @Query("sport") sport: String? = null,
        @Query("sportType") sportType: String? = null
    ): Response<List<ActivityResponse>>
    
    /**
     * Récupérer une activité spécifique
     * GET /activities/:id
     * 
     * Pas d'authentification requise (activité publique)
     */
    @GET("activities/{id}")
    suspend fun getActivity(
        @Path("id") activityId: String
    ): Response<ActivityResponse>
    
    /**
     * Récupérer mes activités
     * GET /activities/my-activities
     * 
     * Nécessite authentification - Retourne toutes les activités créées par l'utilisateur connecté
     */
    @GET("activities/my-activities")
    suspend fun getMyActivities(): Response<List<ActivityResponse>>
    
    /**
     * Créer une nouvelle activité
     * POST /activities
     * 
     * Nécessite authentification JWT
     */
    @POST("activities")
    suspend fun createActivity(
        @Body request: CreateActivityRequest
    ): Response<ActivityResponse>
    
    /**
     * Mettre à jour une activité
     * PATCH /activities/:id
     * 
     * Nécessite authentification - Seulement le créateur peut modifier
     */
    @PATCH("activities/{id}")
    suspend fun updateActivity(
        @Path("id") activityId: String,
        @Body request: CreateActivityRequest
    ): Response<ActivityResponse>
    
    /**
     * Supprimer une activité
     * DELETE /activities/:id
     * 
     * Nécessite authentification - Seulement le créateur peut supprimer
     */
    @DELETE("activities/{id}")
    suspend fun deleteActivity(
        @Path("id") activityId: String
    ): Response<Unit>
    
    /**
     * Rejoindre une activité
     * POST /activities/:id/join
     * 
     * Nécessite authentification JWT
     */
    @POST("activities/{id}/join")
    suspend fun joinActivity(
        @Path("id") activityId: String
    ): Response<JoinActivityResponse>
    
    /**
     * Récupérer les messages de chat d'une activité
     * GET /activities/:id/messages
     * 
     * Nécessite authentification JWT
     */
    @GET("activities/{id}/messages")
    suspend fun getActivityMessages(
        @Path("id") activityId: String
    ): Response<ActivityMessagesResponse>
    
    /**
     * Envoyer un message dans le chat d'une activité
     * POST /activities/:id/messages
     * 
     * Nécessite authentification JWT
     */
    @POST("activities/{id}/messages")
    suspend fun sendActivityMessage(
        @Path("id") activityId: String,
        @Body request: ActivitySendMessageRequest
    ): Response<ActivityMessageDto>
    
    /**
     * Récupérer les participants d'une activité
     * GET /activities/:id/participants
     * 
     * Nécessite authentification JWT
     */
    @GET("activities/{id}/participants")
    suspend fun getActivityParticipants(
        @Path("id") activityId: String
    ): Response<ActivityParticipantsResponse>
    
    /**
     * Quitter une activité
     * POST /activities/:id/leave
     * 
     * Nécessite authentification JWT
     */
    @POST("activities/{id}/leave")
    suspend fun leaveActivity(
        @Path("id") activityId: String
    ): Response<MessageResponse>
    
    /**
     * Marquer une activité comme complétée
     * POST /activities/:id/complete
     *
     * Nécessite authentification JWT - Seulement le créateur
     */
    @POST("activities/{id}/complete")
    suspend fun completeActivity(
        @Path("id") activityId: String
    ): Response<MessageResponse>

    /**
     * Créer ou récupérer le chat de groupe d'une activité
     * POST /activities/:id/group-chat
     *
     * Nécessite authentification JWT
     * L'utilisateur doit être participant de l'activité
     */
    @POST("activities/{id}/group-chat")
    suspend fun createOrGetActivityGroupChat(
        @Path("id") activityId: String
    ): Response<ActivityGroupChatResponse>
}

/**
 * Data class pour créer une activité (requête)
 */
data class CreateActivityRequest(
    val sportType: String,           // REQUIRED: "Football", "Basketball", "Running", "Cycling"
    val title: String,               // REQUIRED: min 3 caractères
    val description: String? = null, // OPTIONAL
    val location: String,            // REQUIRED
    val latitude: Double? = null,    // OPTIONAL
    val longitude: Double? = null,   // OPTIONAL
    val date: String,                 // REQUIRED: Format "YYYY-MM-DD"
    val time: String,                 // REQUIRED: Format ISO 8601 (e.g., "2025-11-15T14:30:00Z")
    val participants: Int,           // REQUIRED: Range 1-100
    val level: String,               // REQUIRED: "Beginner", "Intermediate", "Advanced"
    val visibility: String           // REQUIRED: "public" ou "friends"
)

/**
 * Data class pour la réponse de création d'activité
 */
data class ActivityResponse(
    val _id: String,
    val id: String? = null,  // Alias pour compatibilité
    val creator: Any? = null,  // Peut être un objet ou une chaîne (ID), rendu nullable pour gérer les deux cas
    val sportType: String,
    val title: String,
    val description: String?,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val date: String,  // ISO 8601
    val time: String,  // ISO 8601
    val participants: Int,  // Nombre maximum de participants (capacity)
    val participantIds: List<String>? = null,  // Liste des IDs des participants réels
    val level: String,
    val visibility: String,
    val createdAt: String?,
    val updatedAt: String?
) {
    // Helper pour obtenir l'ID (support id/_id)
    fun getActivityId(): String = id ?: _id
    
    // Helper pour obtenir le créateur comme objet si possible
    fun getCreator(): ActivityCreator? {
        return when (creator) {
            is Map<*, *> -> {
                try {
                    ActivityCreator(
                        _id = (creator["_id"] as? String) ?: (creator["id"] as? String) ?: "",
                        id = creator["id"] as? String,
                        name = (creator["name"] as? String) ?: "",
                        email = creator["email"] as? String,
                        profileImageUrl = creator["profileImageUrl"] as? String
                    )
                } catch (e: Exception) {
                    null
                }
            }
            is String -> {
                // Si c'est juste un ID (chaîne), créer un objet minimal
                ActivityCreator(
                    _id = creator,
                    id = null,
                    name = "",
                    email = null,
                    profileImageUrl = null
                )
            }
            else -> null
        }
    }
    
    // Helper pour obtenir l'ID du créateur (peut être dans un objet ou directement une chaîne)
    fun getCreatorId(): String? {
        return when (creator) {
            is Map<*, *> -> (creator["_id"] as? String) ?: (creator["id"] as? String)
            is String -> creator
            else -> null
        }
    }
}

/**
 * Data class pour le créateur d'une activité
 */
data class ActivityCreator(
    val _id: String,
    val id: String? = null,
    val name: String,
    val email: String?,
    val profileImageUrl: String?
) {
    fun getCreatorId(): String = id ?: _id
}

/**
 * Réponse pour rejoindre une activité
 */
data class JoinActivityResponse(
    val message: String,
    val activity: ActivityResponse? = null
)

/**
 * Requête pour envoyer un message dans une activité
 */
data class ActivitySendMessageRequest(
    val content: String
)

/**
 * Message de chat d'activité
 */
data class ActivityMessageDto(
    val _id: String,
    val id: String? = null,
    val activity: String,
    val sender: ActivityMessageSender? = null,
    val content: String,
    val createdAt: String
) {
    fun getMessageId(): String = id ?: _id
}

/**
 * Expéditeur d'un message
 */
data class ActivityMessageSender(
    val _id: String,
    val id: String? = null,
    val name: String,
    val profileImageUrl: String? = null
) {
    fun getSenderId(): String = id ?: _id
}

/**
 * Réponse pour les messages d'activité
 */
data class ActivityMessagesResponse(
    val messages: List<ActivityMessageDto>
)

/**
 * Participant d'une activité
 */
data class ActivityParticipantDto(
    val _id: String,
    val id: String? = null,
    val name: String,
    val profileImageUrl: String? = null,
    val joinedAt: String? = null,
    val isHost: Boolean = false
) {
    fun getParticipantId(): String = id ?: _id
}

/**
 * Réponse pour les participants d'activité
 */
data class ActivityParticipantsResponse(
    val participants: List<ActivityParticipantDto>
)

/**
 * Réponse pour créer/rejoindre un chat de groupe d'activité
 */
data class ActivityGroupChatResponse(
    val chat: ActivityGroupChatDto,
    val message: String
)

/**
 * Chat de groupe d'activité
 */
data class ActivityGroupChatDto(
    val id: String,
    val groupName: String,
    val groupAvatar: String? = null,
    val participants: List<ActivityGroupChatParticipantDto>,
    val isGroup: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Participant d'un chat de groupe d'activité
 */
data class ActivityGroupChatParticipantDto(
    val id: String,
    val name: String,
    val profileImageUrl: String? = null
)

