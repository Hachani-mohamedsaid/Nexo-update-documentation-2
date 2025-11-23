package com.example.damandroid.data.mapper

import com.example.damandroid.api.ChatMessage as ApiChatMessage
import com.example.damandroid.data.model.ChatPreviewDto
import com.example.damandroid.domain.model.ChatMessage
import com.example.damandroid.domain.model.ChatPreview

fun ChatPreviewDto.toDomain(): ChatPreview = ChatPreview(
    id = id,
    name = name,
    lastMessage = lastMessage,
    timestamp = timestamp,
    unreadCount = unreadCount,
    avatarUrl = avatarUrl,
    isOnline = isOnline,
    isGroup = isGroup,
    participantAvatars = participantAvatars
)

fun ApiChatMessage.toDomain(): ChatMessage {
    // Obtenir l'ID de l'utilisateur actuel
    val currentUserId = com.example.damandroid.auth.UserSession.user?.id
    
    // Extraire le nom et l'avatar depuis senderObj ou utiliser les valeurs directes
    val extractedSenderName = extractSenderName()
    val extractedAvatar = extractAvatar()
    
    // Déterminer si le message est de l'utilisateur actuel
    val isFromMe = isFromMe(currentUserId)
    val senderValue = if (isFromMe) "me" else "other"
    
    return ChatMessage(
        id = id,
        text = text,
        sender = senderValue,
        time = time ?: formatTime(createdAt),
        senderName = extractedSenderName,
        avatar = extractedAvatar,
        createdAt = createdAt
    )
}

/**
 * Formater le temps depuis createdAt si time n'est pas fourni
 */
private fun formatTime(createdAt: String): String {
    return try {
        // Essayer de parser la date ISO et formater
        val instant = java.time.Instant.parse(createdAt)
        val localTime = instant.atZone(java.time.ZoneId.systemDefault()).toLocalTime()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("h:mm a")
        localTime.format(formatter)
    } catch (e: Exception) {
        // Si le parsing échoue, retourner une valeur par défaut
        "now"
    }
}

