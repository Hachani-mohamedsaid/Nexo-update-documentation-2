package com.example.damandroid.data.datasource

import com.example.damandroid.api.Chat
import com.example.damandroid.api.ChatMessage
import com.example.damandroid.api.ChatParticipant
import com.example.damandroid.api.CreateChatRequest
import com.example.damandroid.api.SendMessageRequest
import com.example.damandroid.api.UserSearchResult
import com.example.damandroid.data.model.ChatPreviewDto

interface ChatRemoteDataSource {
    suspend fun fetchChats(search: String? = null): List<ChatPreviewDto>
    suspend fun createChat(request: CreateChatRequest): Chat
    suspend fun getChat(chatId: String): Chat
    suspend fun getMessages(chatId: String): List<ChatMessage>
    suspend fun sendMessage(chatId: String, request: SendMessageRequest): ChatMessage
    suspend fun markChatAsRead(chatId: String): Chat
    suspend fun deleteChat(chatId: String): Unit
    suspend fun deleteMessage(messageId: String): Unit
    suspend fun searchUsers(query: String): List<UserSearchResult>
    suspend fun leaveGroup(chatId: String): Unit
    suspend fun getParticipants(chatId: String): List<ChatParticipant>
}

