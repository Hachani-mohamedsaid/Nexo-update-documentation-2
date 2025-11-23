package com.example.damandroid.api

import retrofit2.Response
import retrofit2.http.*

/**
 * Interface Retrofit pour les endpoints de chat
 */
interface ChatApiService {
    
    /**
     * Récupérer la liste des chats
     * GET /chats
     */
    @GET("chats")
    suspend fun getChats(
        @Query("search") search: String? = null
    ): Response<List<ChatListItem>>
    
    /**
     * Créer un nouveau chat
     * POST /chats
     */
    @POST("chats")
    suspend fun createChat(
        @Body request: CreateChatRequest
    ): Response<Chat>
    
    /**
     * Récupérer un chat spécifique
     * GET /chats/:id
     */
    @GET("chats/{id}")
    suspend fun getChat(
        @Path("id") chatId: String
    ): Response<Chat>
    
    /**
     * Récupérer les messages d'un chat
     * GET /chats/:id/messages
     */
    @GET("chats/{id}/messages")
    suspend fun getMessages(
        @Path("id") chatId: String
    ): Response<List<ChatMessage>>
    
    /**
     * Envoyer un message
     * POST /chats/:id/messages
     */
    @POST("chats/{id}/messages")
    suspend fun sendMessage(
        @Path("id") chatId: String,
        @Body request: SendMessageRequest
    ): Response<ChatMessage>
    
    /**
     * Marquer un chat comme lu
     * PATCH /chats/:id/read
     */
    @PATCH("chats/{id}/read")
    suspend fun markChatAsRead(
        @Path("id") chatId: String
    ): Response<Chat>
    
    /**
     * Supprimer un chat
     * DELETE /chats/:id
     */
    @DELETE("chats/{id}")
    suspend fun deleteChat(
        @Path("id") chatId: String
    ): Response<MessageResponse>
    
    /**
     * Supprimer un message
     * DELETE /chats/messages/:messageId
     */
    @DELETE("chats/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: String
    ): Response<MessageResponse>
    
    /**
     * Rechercher des utilisateurs
     * GET /users/search?search=query
     */
    @GET("users/search")
    suspend fun searchUsers(
        @Query("search") query: String
    ): Response<List<UserSearchResult>>
    
    /**
     * Quitter un groupe de chat
     * DELETE /chats/:id/leave
     */
    @DELETE("chats/{id}/leave")
    suspend fun leaveGroup(
        @Path("id") chatId: String
    ): Response<MessageResponse>
    
    /**
     * Récupérer les participants d'un chat
     * GET /chats/:id/participants
     */
    @GET("chats/{id}/participants")
    suspend fun getParticipants(
        @Path("id") chatId: String
    ): Response<List<ChatParticipant>>
}

/**
 * Data class pour un élément de liste de chat
 */
data class ChatListItem(
    val id: String,
    val participantNames: String,
    val participantAvatars: List<String>,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val isGroup: Boolean
)

/**
 * Data class pour créer un chat
 */
data class CreateChatRequest(
    val participantIds: List<String>,
    val groupName: String? = null,
    val groupAvatar: String? = null
)

/**
 * Data class pour un chat complet (retourné par l'API)
 */
data class Chat(
    val id: String,
    val participants: List<ChatParticipant>? = null,
    val groupName: String? = null,
    val groupAvatar: String? = null,
    val messages: List<ChatMessage>? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val activityId: String? = null // ID de l'activité associée au chat de groupe
)

/**
 * Data class pour un participant d'un chat
 */
data class ChatParticipant(
    val id: String,
    val name: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val avatar: String? = null
)

/**
 * Data class pour un message de chat
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val sender: String? = null, // ID du sender ou "me"/"other"
    val time: String? = null,
    val senderName: String? = null,
    val avatar: String? = null,
    val createdAt: String,
    // Support pour sender comme objet (depuis l'API)
    val senderObj: MessageSender? = null
) {
    /**
     * Obtenir le nom de l'expéditeur depuis senderObj ou senderName
     */
    fun extractSenderName(): String? {
        return senderObj?.name ?: senderName
    }
    
    /**
     * Obtenir l'avatar de l'expéditeur depuis senderObj ou avatar
     */
    fun extractAvatar(): String? {
        return senderObj?.profileImageUrl ?: senderObj?.avatar ?: avatar
    }
    
    /**
     * Déterminer si le message est de l'utilisateur actuel
     */
    fun isFromMe(currentUserId: String?): Boolean {
        if (sender == "me") return true
        if (currentUserId != null && sender == currentUserId) return true
        if (currentUserId != null && senderObj?.extractId() == currentUserId) return true
        return false
    }
}

/**
 * Data class pour l'objet sender dans un message
 */
data class MessageSender(
    val id: String? = null,
    val _id: String? = null, // Alias pour compatibilité
    val name: String? = null,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val avatar: String? = null
) {
    fun extractId(): String {
        return id ?: _id ?: ""
    }
}

/**
 * Data class pour envoyer un message
 */
data class SendMessageRequest(
    val text: String
)

/**
 * Data class pour un résultat de recherche d'utilisateur
 */
data class UserSearchResult(
    val id: String,
    val _id: String? = null, // Alias pour compatibilité
    val name: String,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val avatar: String? = null, // Alias pour compatibilité
    val profileImageThumbnailUrl: String? = null
)

