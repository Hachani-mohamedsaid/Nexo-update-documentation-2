package com.example.damandroid.presentation.chat.model

import com.example.damandroid.domain.model.ChatPreview

data class ChatListUiState(
    val isLoading: Boolean = false,
    val query: String = "",
    val chats: List<ChatPreview> = emptyList(),
    val filteredChats: List<ChatPreview> = chats,
    val error: String? = null
)

