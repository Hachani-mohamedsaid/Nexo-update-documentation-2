package com.example.damandroid.domain.model

data class ChatPreview(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int,
    val avatarUrl: String,
    val isOnline: Boolean,
    val isGroup: Boolean = false,
    val participantAvatars: List<String> = emptyList()
)

data class ChatMessage(
    val id: String,
    val text: String,
    val sender: String, // "me" ou "other"
    val time: String,
    val senderName: String?,
    val avatar: String?,
    val createdAt: String
)

data class UserSearchResult(
    val id: String,
    val name: String,
    val email: String?,
    val profileImageUrl: String?,
    val avatar: String?
)

data class ChatParticipant(
    val id: String,
    val name: String,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val avatar: String? = null
)

