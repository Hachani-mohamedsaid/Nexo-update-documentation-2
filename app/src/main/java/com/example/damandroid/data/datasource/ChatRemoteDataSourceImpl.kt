package com.example.damandroid.data.datasource

import android.util.Log
import com.example.damandroid.api.Chat
import com.example.damandroid.api.ChatApiService
import com.example.damandroid.api.ChatMessage
import com.example.damandroid.api.ChatParticipant
import com.example.damandroid.api.CreateChatRequest
import com.example.damandroid.api.SendMessageRequest
import com.example.damandroid.api.UserSearchResult
import com.example.damandroid.data.model.ChatPreviewDto
import com.example.damandroid.data.model.toChatPreviewDto
import retrofit2.HttpException
import java.io.IOException

class ChatRemoteDataSourceImpl(
    private val chatApiService: ChatApiService
) : ChatRemoteDataSource {
    
    override suspend fun fetchChats(search: String?): List<ChatPreviewDto> {
        return try {
            val response = chatApiService.getChats(search)
            if (response.isSuccessful) {
                val chatList = response.body() ?: emptyList()
                chatList.map { it.toChatPreviewDto() }
            } else {
                Log.e("ChatRemoteDataSource", "Error fetching chats: ${response.code()} - ${response.message()}")
                throw Exception("Failed to fetch chats: ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e("ChatRemoteDataSource", "HttpException fetching chats: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                throw Exception("Unauthorized: Please login again")
            } else {
                throw Exception("Failed to fetch chats: ${e.message()}")
            }
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException fetching chats: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e("ChatRemoteDataSource", "Exception fetching chats: ${e.message}", e)
            throw e
        }
    }
    
    override suspend fun createChat(request: CreateChatRequest): Chat {
        return try {
            val response = chatApiService.createChat(request)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Chat response body is null")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRemoteDataSource", "Error creating chat: ${response.code()} - $errorBody")
                throw Exception("Failed to create chat: ${response.message()}")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("ChatRemoteDataSource", "HttpException creating chat: ${e.code()} - $errorBody")
            throw Exception("Failed to create chat: ${e.message()}")
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException creating chat: ${e.message}")
            throw e
        }
    }
    
    override suspend fun getChat(chatId: String): Chat {
        return try {
            val response = chatApiService.getChat(chatId)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Chat response body is null")
            } else {
                Log.e("ChatRemoteDataSource", "Error getting chat: ${response.code()} - ${response.message()}")
                throw Exception("Failed to get chat: ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e("ChatRemoteDataSource", "HttpException getting chat: ${e.code()} - ${e.message()}")
            throw Exception("Failed to get chat: ${e.message()}")
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException getting chat: ${e.message}")
            throw e
        }
    }
    
    override suspend fun getMessages(chatId: String): List<ChatMessage> {
        return try {
            val response = chatApiService.getMessages(chatId)
            when {
                response.isSuccessful -> {
                    response.body() ?: emptyList()
                }
                response.code() == 403 -> {
                    // Accès refusé - lancer une exception pour que l'UI puisse l'afficher
                    val errorMessage = "Vous n'avez pas accès à ce chat. Veuillez réessayer."
                    Log.e("ChatRemoteDataSource", "Error getting messages: 403 - $errorMessage")
                    throw Exception(errorMessage)
                }
                response.code() == 401 -> {
                    val errorMessage = "Non autorisé. Veuillez vous reconnecter."
                    Log.e("ChatRemoteDataSource", "Error getting messages: 401 - $errorMessage")
                    throw Exception(errorMessage)
                }
                response.code() == 404 -> {
                    val errorMessage = "Chat non trouvé."
                    Log.e("ChatRemoteDataSource", "Error getting messages: 404 - $errorMessage")
                    throw Exception(errorMessage)
                }
                else -> {
                    Log.e("ChatRemoteDataSource", "Error getting messages: ${response.code()} - ${response.message()}")
                    throw Exception("Erreur lors du chargement des messages: ${response.code()}")
                }
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                403 -> "Vous n'avez pas accès à ce chat. Veuillez réessayer."
                401 -> "Non autorisé. Veuillez vous reconnecter."
                404 -> "Chat non trouvé."
                else -> "Erreur HTTP: ${e.code()}"
            }
            Log.e("ChatRemoteDataSource", "HttpException getting messages: ${e.code()} - $errorMessage")
            throw Exception(errorMessage)
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException getting messages: ${e.message}")
            throw Exception("Erreur de connexion. Vérifiez votre connexion internet.")
        } catch (e: Exception) {
            // Si c'est déjà une Exception avec message, la relancer
            throw e
        }
    }
    
    override suspend fun sendMessage(chatId: String, request: SendMessageRequest): ChatMessage {
        return try {
            val response = chatApiService.sendMessage(chatId, request)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Message response body is null")
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ChatRemoteDataSource", "Error sending message: ${response.code()} - $errorBody")
                throw Exception("Failed to send message: ${response.message()}")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("ChatRemoteDataSource", "HttpException sending message: ${e.code()} - $errorBody")
            throw Exception("Failed to send message: ${e.message()}")
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException sending message: ${e.message}")
            throw e
        }
    }
    
    override suspend fun markChatAsRead(chatId: String): Chat {
        return try {
            val response = chatApiService.markChatAsRead(chatId)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Chat response body is null")
            } else {
                Log.e("ChatRemoteDataSource", "Error marking chat as read: ${response.code()} - ${response.message()}")
                throw Exception("Failed to mark chat as read: ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e("ChatRemoteDataSource", "HttpException marking chat as read: ${e.code()} - ${e.message()}")
            throw Exception("Failed to mark chat as read: ${e.message()}")
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException marking chat as read: ${e.message}")
            throw e
        }
    }
    
    override suspend fun deleteChat(chatId: String) {
        try {
            val response = chatApiService.deleteChat(chatId)
            if (!response.isSuccessful) {
                Log.e("ChatRemoteDataSource", "Error deleting chat: ${response.code()} - ${response.message()}")
                throw Exception("Failed to delete chat: ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e("ChatRemoteDataSource", "HttpException deleting chat: ${e.code()} - ${e.message()}")
            throw Exception("Failed to delete chat: ${e.message()}")
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException deleting chat: ${e.message}")
            throw e
        }
    }
    
    override suspend fun deleteMessage(messageId: String) {
        try {
            val response = chatApiService.deleteMessage(messageId)
            if (!response.isSuccessful) {
                Log.e("ChatRemoteDataSource", "Error deleting message: ${response.code()} - ${response.message()}")
                throw Exception("Failed to delete message: ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e("ChatRemoteDataSource", "HttpException deleting message: ${e.code()} - ${e.message()}")
            throw Exception("Failed to delete message: ${e.message()}")
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException deleting message: ${e.message}")
            throw e
        }
    }
    
    override suspend fun searchUsers(query: String): List<UserSearchResult> {
        return try {
            val response = chatApiService.searchUsers(query)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("ChatRemoteDataSource", "Error searching users: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: HttpException) {
            Log.e("ChatRemoteDataSource", "HttpException searching users: ${e.code()} - ${e.message()}")
            emptyList()
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException searching users: ${e.message}")
            throw e
        }
    }
    
    override suspend fun leaveGroup(chatId: String) {
        try {
            val response = chatApiService.leaveGroup(chatId)
            if (!response.isSuccessful) {
                Log.e("ChatRemoteDataSource", "Error leaving group: ${response.code()} - ${response.message()}")
                throw Exception("Failed to leave group: ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e("ChatRemoteDataSource", "HttpException leaving group: ${e.code()} - ${e.message()}")
            throw Exception("Failed to leave group: ${e.message()}")
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException leaving group: ${e.message}")
            throw e
        }
    }
    
    override suspend fun getParticipants(chatId: String): List<ChatParticipant> {
        return try {
            val response = chatApiService.getParticipants(chatId)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                Log.e("ChatRemoteDataSource", "Error getting participants: ${response.code()} - ${response.message()}")
                emptyList()
            }
        } catch (e: HttpException) {
            Log.e("ChatRemoteDataSource", "HttpException getting participants: ${e.code()} - ${e.message()}")
            emptyList()
        } catch (e: IOException) {
            Log.e("ChatRemoteDataSource", "IOException getting participants: ${e.message}")
            throw e
        }
    }
    
}

