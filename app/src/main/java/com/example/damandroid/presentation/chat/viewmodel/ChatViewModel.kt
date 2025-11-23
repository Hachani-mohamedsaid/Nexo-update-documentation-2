package com.example.damandroid.presentation.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.api.ChatMessage as ApiChatMessage
import com.example.damandroid.api.ChatWebSocketService
import com.example.damandroid.data.mapper.toDomain as apiMessageToDomain
import com.example.damandroid.domain.model.ChatMessage
import com.example.damandroid.domain.repository.ChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null,
    val isSending: Boolean = false,
    val isWebSocketConnected: Boolean = false,
    val typingUsers: Map<String, Boolean> = emptyMap()
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val chatId: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState(isLoading = true))
    val uiState: StateFlow<ChatUiState> = _uiState
    
    private val webSocketService = ChatWebSocketService()
    
    init {
        loadMessagesWithRetry()
        markChatAsRead()
        connectWebSocket()
    }
    
    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
    
    /**
     * Se connecter au WebSocket pour recevoir les messages en temps réel
     */
    private fun connectWebSocket() {
        // Écouter l'état de connexion WebSocket
        viewModelScope.launch {
            try {
                webSocketService.connectionState.collect { isConnected ->
                    _uiState.update { it.copy(isWebSocketConnected = isConnected) }
                }
            } catch (e: CancellationException) {
                // Annulation normale - ne pas logger
                throw e
            }
        }
        
        // Écouter les nouveaux messages du WebSocket
        viewModelScope.launch {
            webSocketService.messages.collect { apiMessage ->
                val chatMessage = apiMessage.apiMessageToDomain()
                _uiState.update { state ->
                    // Éviter les doublons
                    val messageExists = state.messages.any { it.id == chatMessage.id }
                    if (!messageExists) {
                        // Ajouter le nouveau message et trier
                        val updatedMessages = (state.messages + chatMessage)
                            .distinctBy { it.id }
                            .sortedBy { parseDateTimestamp(it.createdAt) }
                        state.copy(messages = updatedMessages)
                    } else {
                        state
                    }
                }
            }
        }
        
        // Écouter les indicateurs de frappe
        viewModelScope.launch {
            webSocketService.typingUsers.collect { typingMap ->
                _uiState.update { it.copy(typingUsers = typingMap) }
            }
        }
        
        // Connecter au WebSocket
        webSocketService.connect(chatId)
    }
    
    /**
     * Parser un timestamp ISO en Long pour le tri
     */
    private fun parseDateTimestamp(createdAt: String): Long {
        return try {
            java.time.Instant.parse(createdAt).toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun loadMessagesWithRetry(maxRetries: Int = 5, delayMs: Long = 1000) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            var lastError: Throwable? = null
            repeat(maxRetries) { attempt ->
                runCatching {
                    chatRepository.getMessages(chatId)
                }.onSuccess { messages ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = messages,
                            error = null
                        )
                    }
                    // Le WebSocket est déjà connecté dans init, pas besoin de polling
                    return@launch // Succès, on sort
                }.onFailure { throwable ->
                    lastError = throwable
                    // Si c'est une erreur 403 et qu'on n'est pas au dernier essai, on retry
                    val is403Error = throwable.message?.contains("403", ignoreCase = true) == true ||
                                    throwable.message?.contains("accès", ignoreCase = true) == true ||
                                    throwable.message?.contains("access", ignoreCase = true) == true
                    
                    if (is403Error && attempt < maxRetries - 1) {
                        // Attendre avant de réessayer avec délai progressif
                        kotlinx.coroutines.delay(delayMs * (attempt + 1)) // 1s, 2s, 3s, 4s, 5s
                    } else {
                        // Dernier essai ou erreur non-403, on affiche l'erreur
                        val errorMessage = if (is403Error && attempt == maxRetries - 1) {
                            // Message d'erreur plus explicite pour 403 après tous les essais
                            "Vous n'avez pas accès à ce chat. Le backend n'a pas encore synchronisé vos permissions. Veuillez réessayer dans quelques instants ou contactez le support si le problème persiste."
                        } else {
                            throwable.message ?: "Erreur lors du chargement des messages"
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                        return@launch
                    }
                }
            }
            
            // Si on arrive ici, tous les essais ont échoué
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = lastError?.message ?: "Échec du chargement des messages après $maxRetries tentatives"
                )
            }
        }
    }
    
    fun loadMessages() {
        loadMessagesWithRetry()
    }
    
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        // Si WebSocket est connecté, utiliser WebSocket
        if (_uiState.value.isWebSocketConnected) {
            _uiState.update { it.copy(isSending = true, error = null) }
            webSocketService.sendMessage(chatId, text)
            // Le message sera reçu via le flux WebSocket
            viewModelScope.launch {
                delay(500)
                _uiState.update { it.copy(isSending = false) }
            }
        } else {
            // Fallback vers l'API REST si WebSocket n'est pas connecté
            viewModelScope.launch {
                _uiState.update { it.copy(isSending = true, error = null) }
                runCatching {
                    chatRepository.sendMessage(chatId, text)
                }.onSuccess { message ->
                    // message est déjà un ChatMessage (domain model), pas besoin de mapper
                    _uiState.update { state ->
                        state.copy(
                            isSending = false,
                            messages = state.messages + message
                        )
                    }
                }.onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            error = throwable.message ?: "Failed to send message"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Indiquer que l'utilisateur est en train de taper
     */
    fun setTyping(isTyping: Boolean) {
        if (_uiState.value.isWebSocketConnected) {
            webSocketService.setTyping(chatId, isTyping)
        }
    }
    
    private fun markChatAsRead() {
        viewModelScope.launch {
            runCatching {
                chatRepository.markChatAsRead(chatId)
            }.onSuccess {
                // Si WebSocket est connecté, notifier via WebSocket aussi
                if (_uiState.value.isWebSocketConnected) {
                    webSocketService.markAsRead(chatId)
                }
            }.onFailure {
                // Log error but don't show to user
                android.util.Log.e("ChatViewModel", "Failed to mark chat as read: ${it.message}")
            }
        }
    }
    
    fun refresh() {
        loadMessages()
    }
}

