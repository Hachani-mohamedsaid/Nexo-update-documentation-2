package com.example.damandroid.api

import android.util.Log
import com.example.damandroid.auth.UserSession
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject

/**
 * Service WebSocket pour les messages en temps réel dans les Activity Rooms
 */
class ActivityRoomWebSocketService {
    private var socket: Socket? = null
    private val _messages = MutableSharedFlow<ActivityMessageDto>(replay = 0, extraBufferCapacity = 64)
    val messages: SharedFlow<ActivityMessageDto> = _messages
    
    private val _connectionState = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)
    val connectionState: SharedFlow<Boolean> = _connectionState

    private val _typingUsers = MutableSharedFlow<Map<String, Boolean>>(replay = 1, extraBufferCapacity = 64)
    val typingUsers: SharedFlow<Map<String, Boolean>> = _typingUsers

    private var currentActivityId: String? = null

    /**
     * Se connecter au WebSocket pour une activité spécifique
     */
    fun connect(activityId: String) {
        if (socket?.connected() == true && currentActivityId == activityId) {
            return
        }

        disconnect() // Déconnecter si on change d'activité

        try {
            val token = UserSession.token
            if (token.isNullOrEmpty()) {
                Log.e("WebSocket", "No token available")
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

            // URL de production
            val wsUrl = "https://apinest-production.up.railway.app/activity-room"
            socket = IO.socket(wsUrl, options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("WebSocket", "Connected to activity room")
                _connectionState.tryEmit(true)
                currentActivityId = activityId
                joinActivity(activityId)
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("WebSocket", "Disconnected")
                _connectionState.tryEmit(false)
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("WebSocket", "Connection error: ${args.getOrNull(0)}")
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
                    Log.e("WebSocket", "Error parsing message: ${e.message}", e)
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
                    Log.e("WebSocket", "Error parsing typing event: ${e.message}", e)
                }
            }

            socket?.on("user-joined") { args ->
                Log.d("WebSocket", "User joined: ${args.getOrNull(0)}")
                // Optionnel : mettre à jour la liste des participants
            }

            socket?.on("user-left") { args ->
                Log.d("WebSocket", "User left: ${args.getOrNull(0)}")
                // Optionnel : mettre à jour la liste des participants
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e("WebSocket", "Failed to connect: ${e.message}", e)
            _connectionState.tryEmit(false)
        }
    }

    /**
     * Se déconnecter du WebSocket
     */
    fun disconnect() {
        currentActivityId?.let { leaveActivity(it) }
        socket?.disconnect()
        socket = null
        currentActivityId = null
        _connectionState.tryEmit(false)
    }

    private fun joinActivity(activityId: String) {
        socket?.emit("join-activity", JSONObject().apply {
            put("activityId", activityId)
        })
    }

    private fun leaveActivity(activityId: String) {
        socket?.emit("leave-activity", JSONObject().apply {
            put("activityId", activityId)
        })
    }

    /**
     * Envoyer un message via WebSocket
     */
    fun sendMessage(activityId: String, content: String) {
        if (socket?.connected() == true) {
            socket?.emit("send-message", JSONObject().apply {
                put("activityId", activityId)
                put("content", content)
            })
        } else {
            Log.w("WebSocket", "Cannot send message: not connected")
        }
    }

    /**
     * Indiquer que l'utilisateur est en train de taper
     */
    fun setTyping(activityId: String, isTyping: Boolean) {
        if (socket?.connected() == true) {
            socket?.emit("typing", JSONObject().apply {
                put("activityId", activityId)
                put("isTyping", isTyping)
            })
        }
    }

    /**
     * Parser un message JSON reçu du WebSocket
     */
    private fun parseMessage(json: JSONObject): ActivityMessageDto {
        val senderJson = json.optJSONObject("sender")
        val sender = if (senderJson != null) {
            ActivityMessageSender(
                _id = senderJson.optString("_id"),
                id = senderJson.optString("id"),
                name = senderJson.optString("name"),
                profileImageUrl = senderJson.optString("profileImageUrl").takeIf { it.isNotEmpty() }
            )
        } else null

        return ActivityMessageDto(
            _id = json.optString("_id"),
            id = json.optString("id"),
            activity = json.optString("activity"),
            sender = sender,
            content = json.optString("content"),
            createdAt = json.optString("createdAt")
        )
    }
}

