package com.example.damandroid.api

import android.util.Log
import com.example.damandroid.auth.UserSession
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject

/**
 * Service WebSocket pour les messages en temps réel dans les chats individuels
 */
class ChatWebSocketService {
    private var socket: Socket? = null
    private val _messages = MutableSharedFlow<ChatMessage>(replay = 0, extraBufferCapacity = 64)
    val messages: SharedFlow<ChatMessage> = _messages
    
    private val _connectionState = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)
    val connectionState: SharedFlow<Boolean> = _connectionState

    private val _typingUsers = MutableSharedFlow<Map<String, Boolean>>(replay = 1, extraBufferCapacity = 64)
    val typingUsers: SharedFlow<Map<String, Boolean>> = _typingUsers

    private var currentChatId: String? = null

    /**
     * Se connecter au WebSocket pour un chat spécifique
     */
    fun connect(chatId: String) {
        if (socket?.connected() == true && currentChatId == chatId) {
            return
        }

        disconnect() // Déconnecter si on change de chat

        try {
            val token = UserSession.token
            if (token.isNullOrEmpty()) {
                Log.e("ChatWebSocket", "No token available")
                _connectionState.tryEmit(false)
                return
            }

            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                transports = arrayOf("websocket")
            }

            // URL de production pour les chats individuels
            val wsUrl = "https://apinest-production.up.railway.app/chats"
            socket = IO.socket(wsUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("ChatWebSocket", "Connected to chat")
                _connectionState.tryEmit(true)
                currentChatId = chatId
                joinChat(chatId)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("ChatWebSocket", "Disconnected")
                _connectionState.tryEmit(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("ChatWebSocket", "Connection error: ${args.getOrNull(0)}")
                _connectionState.tryEmit(false)
            }

            socket?.on("new-message") { args ->
                try {
                    val messageJson = args[0] as? JSONObject
                    if (messageJson != null) {
                        val message = parseMessage(messageJson)
                        _messages.tryEmit(message)
                    }
                } catch (e: Exception) {
                    Log.e("ChatWebSocket", "Error parsing message: ${e.message}", e)
                }
            }

            socket?.on("user-typing") { args ->
                try {
                    val data = args[0] as? JSONObject
                    val userId = data?.optString("userId")
                    val isTyping = data?.optBoolean("isTyping") ?: false
                    if (userId != null) {
                        val current = _typingUsers.replayCache.firstOrNull()?.toMutableMap() ?: mutableMapOf()
                        if (isTyping) {
                            current[userId] = true
                        } else {
                            current.remove(userId)
                        }
                        _typingUsers.tryEmit(current)
                    }
                } catch (e: Exception) {
                    Log.e("ChatWebSocket", "Error parsing typing event: ${e.message}", e)
                }
            }

            socket?.on("user-joined") { args ->
                Log.d("ChatWebSocket", "User joined: ${args.getOrNull(0)}")
            }

            socket?.on("user-left") { args ->
                Log.d("ChatWebSocket", "User left: ${args.getOrNull(0)}")
            }

            socket?.on("message-read") { args ->
                Log.d("ChatWebSocket", "Message read: ${args.getOrNull(0)}")
                // Optionnel : mettre à jour l'état de lecture des messages
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e("ChatWebSocket", "Failed to connect: ${e.message}", e)
            _connectionState.tryEmit(false)
        }
    }

    /**
     * Se déconnecter du WebSocket
     */
    fun disconnect() {
        currentChatId?.let { leaveChat(it) }
        socket?.disconnect()
        socket = null
        currentChatId = null
        _connectionState.tryEmit(false)
    }

    private fun joinChat(chatId: String) {
        socket?.emit("join-chat", JSONObject().apply {
            put("chatId", chatId)
        })
    }

    private fun leaveChat(chatId: String) {
        socket?.emit("leave-chat", JSONObject().apply {
            put("chatId", chatId)
        })
    }

    /**
     * Envoyer un message via WebSocket
     */
    fun sendMessage(chatId: String, content: String) {
        if (socket?.connected() == true) {
            socket?.emit("send-message", JSONObject().apply {
                put("chatId", chatId)
                put("text", content)
            })
        } else {
            Log.w("ChatWebSocket", "Cannot send message: not connected")
        }
    }

    /**
     * Indiquer que l'utilisateur est en train de taper
     */
    fun setTyping(chatId: String, isTyping: Boolean) {
        if (socket?.connected() == true) {
            socket?.emit("typing", JSONObject().apply {
                put("chatId", chatId)
                put("isTyping", isTyping)
            })
        }
    }

    /**
     * Marquer les messages comme lus
     */
    fun markAsRead(chatId: String) {
        if (socket?.connected() == true) {
            socket?.emit("mark-read", JSONObject().apply {
                put("chatId", chatId)
            })
        }
    }

    /**
     * Parser un message JSON reçu du WebSocket
     */
    private fun parseMessage(json: JSONObject): ChatMessage {
        val senderJson = json.optJSONObject("sender")
        val senderObj = if (senderJson != null) {
            MessageSender(
                id = senderJson.optString("id"),
                _id = senderJson.optString("_id"),
                name = senderJson.optString("name"),
                email = senderJson.optString("email"),
                profileImageUrl = senderJson.optString("profileImageUrl").takeIf { it.isNotEmpty() },
                avatar = senderJson.optString("avatar").takeIf { it.isNotEmpty() }
            )
        } else null

        return ChatMessage(
            id = json.optString("_id") ?: json.optString("id"),
            text = json.optString("text") ?: json.optString("content", ""),
            sender = json.optString("sender"),
            time = json.optString("time"),
            senderName = senderObj?.name ?: json.optString("senderName"),
            avatar = senderObj?.profileImageUrl ?: senderObj?.avatar ?: json.optString("avatar"),
            createdAt = json.optString("createdAt") ?: json.optString("timestamp", ""),
            senderObj = senderObj
        )
    }
}

