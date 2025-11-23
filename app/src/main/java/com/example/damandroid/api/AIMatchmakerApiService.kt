package com.example.damandroid.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface Retrofit pour les endpoints AI Matchmaker
 */
interface AIMatchmakerApiService {
    
    /**
     * Envoyer un message à l'IA et recevoir une réponse
     * POST /ai-matchmaker/chat
     * 
     * Nécessite authentification JWT
     */
    @POST("ai-matchmaker/chat")
    suspend fun sendMessage(
        @Body request: AIMatchmakerChatRequest
    ): Response<AIMatchmakerChatResponse>
}

/**
 * Requête pour envoyer un message à l'IA
 */
data class AIMatchmakerChatRequest(
    val message: String,
    val conversationHistory: List<ChatMessageDto>? = null
)

/**
 * Message dans l'historique de conversation
 */
data class ChatMessageDto(
    val role: String, // "user" ou "assistant"
    val content: String
)

/**
 * Réponse de l'IA
 */
data class AIMatchmakerChatResponse(
    val message: String = "",
    val suggestedActivities: List<SuggestedActivityDto>? = null,
    val suggestedUsers: List<SuggestedUserDto>? = null,
    val options: List<String>? = null
)

/**
 * Activité suggérée par l'IA
 */
data class SuggestedActivityDto(
    val id: String,
    val title: String,
    val sportType: String,
    val location: String,
    val date: String,
    val time: String,
    val participants: Int,
    val maxParticipants: Int,
    val level: String,
    val matchScore: Int? = null
)

/**
 * Utilisateur suggéré par l'IA
 */
data class SuggestedUserDto(
    val id: String,
    val name: String,
    val profileImageUrl: String? = null,
    val sport: String,
    val distance: String? = null,
    val matchScore: Int? = null,
    val bio: String? = null,
    val availability: String? = null
)

