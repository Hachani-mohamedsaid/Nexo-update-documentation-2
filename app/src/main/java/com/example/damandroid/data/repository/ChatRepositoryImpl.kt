package com.example.damandroid.data.repository

import com.example.damandroid.api.CreateChatRequest
import com.example.damandroid.api.SendMessageRequest
import com.example.damandroid.data.datasource.ChatRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.ChatMessage
import com.example.damandroid.domain.model.ChatParticipant
import com.example.damandroid.domain.model.ChatPreview
import com.example.damandroid.domain.model.UserSearchResult
import com.example.damandroid.domain.repository.ChatRepository

class ChatRepositoryImpl(
    private val remoteDataSource: ChatRemoteDataSource
) : ChatRepository {
    override suspend fun getChats(): List<ChatPreview> =
        remoteDataSource.fetchChats().map { it.toDomain() }

    override suspend fun searchChats(query: String): List<ChatPreview> =
        // Utiliser la recherche côté serveur si la query n'est pas vide
        if (query.isNotBlank()) {
            remoteDataSource.fetchChats(query).map { it.toDomain() }
        } else {
            getChats()
        }
    
    override suspend fun getChat(chatId: String): com.example.damandroid.api.Chat =
        remoteDataSource.getChat(chatId)
    
    override suspend fun getMessages(chatId: String): List<ChatMessage> =
        remoteDataSource.getMessages(chatId).map { it.toDomain() }
    
    override suspend fun sendMessage(chatId: String, text: String): ChatMessage =
        remoteDataSource.sendMessage(chatId, SendMessageRequest(text)).toDomain()
    
    override suspend fun markChatAsRead(chatId: String) {
        remoteDataSource.markChatAsRead(chatId)
    }
    
    override suspend fun createChat(request: CreateChatRequest): String {
        val chat = remoteDataSource.createChat(request)
        return chat.id
    }
    
    override suspend fun searchUsers(query: String): List<UserSearchResult> {
        return remoteDataSource.searchUsers(query).map { apiResult ->
            UserSearchResult(
                id = apiResult.id,
                name = apiResult.name,
                email = apiResult.email,
                profileImageUrl = apiResult.profileImageUrl ?: apiResult.avatar,
                avatar = apiResult.avatar ?: apiResult.profileImageUrl
            )
        }
    }
    
    override suspend fun deleteChat(chatId: String) {
        remoteDataSource.deleteChat(chatId)
    }
    
    override suspend fun deleteMessage(messageId: String) {
        remoteDataSource.deleteMessage(messageId)
    }
    
    override suspend fun leaveGroup(chatId: String) {
        remoteDataSource.leaveGroup(chatId)
    }
    
    override suspend fun getParticipants(chatId: String): List<ChatParticipant> {
        return remoteDataSource.getParticipants(chatId).map { apiParticipant ->
            ChatParticipant(
                id = apiParticipant.id,
                name = apiParticipant.name ?: "Unknown",
                email = apiParticipant.email,
                profileImageUrl = apiParticipant.profileImageUrl,
                avatar = apiParticipant.avatar ?: apiParticipant.profileImageUrl
            )
        }
    }
}

