package com.example.damandroid.domain.repository

import com.example.damandroid.api.CreateChatRequest
import com.example.damandroid.api.SendMessageRequest
import com.example.damandroid.domain.model.ChatMessage
import com.example.damandroid.domain.model.ChatParticipant
import com.example.damandroid.domain.model.ChatPreview
import com.example.damandroid.domain.model.UserSearchResult

interface ChatRepository {
    suspend fun getChats(): List<ChatPreview>
    suspend fun searchChats(query: String): List<ChatPreview>
    suspend fun getChat(chatId: String): com.example.damandroid.api.Chat // Récupérer un chat complet
    suspend fun getMessages(chatId: String): List<ChatMessage>
    suspend fun sendMessage(chatId: String, text: String): ChatMessage
    suspend fun markChatAsRead(chatId: String)
    suspend fun createChat(request: CreateChatRequest): String // Retourne l'ID du chat créé
    suspend fun searchUsers(query: String): List<UserSearchResult>
    suspend fun deleteChat(chatId: String)
    suspend fun deleteMessage(messageId: String)
    suspend fun leaveGroup(chatId: String)
    suspend fun getParticipants(chatId: String): List<ChatParticipant>
}

