package com.example.damandroid.data.model

import com.example.damandroid.api.ChatListItem

/**
 * DTO pour un aperçu de chat
 * Utilise ChatListItem de l'API comme source
 */
data class ChatPreviewDto(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int,
    val avatarUrl: String,
    val isOnline: Boolean = false, // L'API ne fournit pas cette info, on la met à false par défaut
    val isGroup: Boolean = false,
    val participantAvatars: List<String> = emptyList()
)

/**
 * Extension pour convertir ChatListItem (API) en ChatPreviewDto
 */
fun ChatListItem.toChatPreviewDto(): ChatPreviewDto {
    // Pour les chats 1-1, utiliser le premier avatar, pour les groupes utiliser le groupAvatar si disponible
    val avatar = if (participantAvatars.isNotEmpty()) {
        participantAvatars.first()
    } else {
        "" // Pas d'avatar disponible
    }
    
    return ChatPreviewDto(
        id = id,
        name = participantNames,
        lastMessage = lastMessage,
        timestamp = lastMessageTime,
        unreadCount = unreadCount,
        avatarUrl = avatar,
        isOnline = false,
        isGroup = isGroup,
        participantAvatars = participantAvatars
    )
}

