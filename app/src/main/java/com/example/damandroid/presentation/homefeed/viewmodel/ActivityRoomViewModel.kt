package com.example.damandroid.presentation.homefeed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.api.ActivityMessageDto
import com.example.damandroid.api.ActivityParticipantDto
import com.example.damandroid.api.ActivityRoomRepository
import com.example.damandroid.api.ActivityRoomWebSocketService
import com.example.damandroid.auth.UserSession
import com.example.damandroid.domain.model.HomeActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActivityRoomUiState(
    val isLoading: Boolean = false,
    val messages: List<ActivityChatMessage> = emptyList(),
    val participants: List<ActivityParticipant> = emptyList(),
    val error: String? = null,
    val isSendingMessage: Boolean = false,
    val isJoining: Boolean = false,
    val isLeaving: Boolean = false,
    val isCompleting: Boolean = false,
    val isWebSocketConnected: Boolean = false,
    val typingUsers: Map<String, Boolean> = emptyMap(),
    val creatorId: String? = null,
    val currentUserId: String? = null,
    val isCurrentUserParticipant: Boolean = false
)

data class ActivityChatMessage(
    val id: String,
    val sender: String,
    val avatar: String,
    val text: String,
    val time: String
)

data class ActivityParticipant(
    val id: String,
    val name: String,
    val avatar: String,
    val status: String
)

class ActivityRoomViewModel(
    private val activityId: String,
    private val repository: ActivityRoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ActivityRoomUiState(
            isLoading = true,
            currentUserId = UserSession.user?.id
        )
    )
    val uiState: StateFlow<ActivityRoomUiState> = _uiState

    // Événement émis après complétion d'activité pour rafraîchir les achievements
    private val _activityCompleted = MutableSharedFlow<String>(replay = 0)
    val activityCompleted: SharedFlow<String> = _activityCompleted.asSharedFlow()

    private val webSocketService = ActivityRoomWebSocketService()

    init {
        loadData()
        connectWebSocket()
    }
    
    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
    }
    
    /**
     * Se connecter au WebSocket
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
            webSocketService.messages.collect { messageDto ->
                val chatMessage = convertToChatMessage(messageDto)
                _uiState.update { state ->
                    // Éviter les doublons
                    val messageExists = state.messages.any { it.id == chatMessage.id }
                    if (!messageExists) {
                        // Ajouter le nouveau message et trier
                        val updatedMessages = (state.messages + chatMessage)
                            .distinctBy { it.id }
                            .sortedBy { msg ->
                                // Trier par timestamp si disponible
                                parseDateTimestamp(
                                    messageDto.createdAt
                                )
                            }
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
        webSocketService.connect(activityId)
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Charger les messages, participants et détails de l'activité en parallèle
            val messagesResult = repository.getMessages(activityId)
            val participantsResult = repository.getParticipants(activityId)
            val activityResult = repository.getActivity(activityId)

            val currentUserId = UserSession.user?.id
            var creatorId: String? = null
            var isCurrentUserParticipant = false

            // Récupérer le creatorId depuis les détails de l'activité
            if (activityResult is ActivityRoomRepository.ActivityRoomResult.Success) {
                creatorId = activityResult.data.getCreatorId()
            }

            // Vérifier si l'utilisateur actuel est participant
            if (participantsResult is ActivityRoomRepository.ActivityRoomResult.Success && currentUserId != null) {
                isCurrentUserParticipant = participantsResult.data.any { participant ->
                    participant.getParticipantId() == currentUserId
                }
            }

            when {
                messagesResult is ActivityRoomRepository.ActivityRoomResult.Success &&
                participantsResult is ActivityRoomRepository.ActivityRoomResult.Success -> {
                    // Trier les messages par date avant conversion
                    val sortedDtos = messagesResult.data.sortedBy { dto ->
                        parseDateTimestamp(dto.createdAt)
                    }
                    val loadedMessages = sortedDtos.map { convertToChatMessage(it) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            messages = loadedMessages,
                            participants = participantsResult.data.map { dto ->
                                convertToParticipant(dto)
                            },
                            creatorId = creatorId,
                            currentUserId = currentUserId,
                            isCurrentUserParticipant = isCurrentUserParticipant,
                            error = null
                        )
                    }
                }
                messagesResult is ActivityRoomRepository.ActivityRoomResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = messagesResult.message
                        )
                    }
                }
                participantsResult is ActivityRoomRepository.ActivityRoomResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = participantsResult.message
                        )
                    }
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _uiState.value.isSendingMessage) return

        // Vérifier si l'utilisateur est participant avant d'envoyer un message
        if (!_uiState.value.isCurrentUserParticipant) {
            _uiState.update { 
                it.copy(error = "Vous devez rejoindre l'activité pour envoyer des messages")
            }
            return
        }

        // Si WebSocket est connecté, utiliser WebSocket
        if (_uiState.value.isWebSocketConnected) {
            _uiState.update { it.copy(isSendingMessage = true, error = null) }
            webSocketService.sendMessage(activityId, content)
            // Le message sera reçu via le flux WebSocket
            // Réinitialiser isSendingMessage après un court délai
            viewModelScope.launch {
                delay(500)
                _uiState.update { it.copy(isSendingMessage = false) }
            }
        } else {
            // Fallback vers REST API
            viewModelScope.launch {
                _uiState.update { it.copy(isSendingMessage = true, error = null) }
                
                when (val result = repository.sendMessage(activityId, content)) {
                    is ActivityRoomRepository.ActivityRoomResult.Success -> {
                        val newMessage = convertToChatMessage(result.data)
                        _uiState.update { state ->
                            // Vérifier que le message n'existe pas déjà (éviter les doublons)
                            val messageExists = state.messages.any { it.id == newMessage.id }
                            val updatedMessages = if (messageExists) {
                                state.messages
                            } else {
                                state.messages + newMessage
                            }
                            
                            state.copy(
                                messages = updatedMessages,
                                isSendingMessage = false
                            )
                        }
                    }
                    is ActivityRoomRepository.ActivityRoomResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isSendingMessage = false,
                                error = result.message
                            )
                        }
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
            webSocketService.setTyping(activityId, isTyping)
        }
    }

    fun joinActivity() {
        if (_uiState.value.isJoining) return

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, error = null) }
            
            when (val result = repository.joinActivity(activityId)) {
                is ActivityRoomRepository.ActivityRoomResult.Success -> {
                    // Recharger les données après avoir rejoint
                    loadData()
                    _uiState.update { it.copy(isJoining = false) }
                }
                is ActivityRoomRepository.ActivityRoomResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isJoining = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Vérifier si l'utilisateur actuel est l'hôte de l'activité
     */
    fun isCurrentUserHost(): Boolean {
        val state = _uiState.value
        return state.currentUserId != null && 
               state.creatorId != null && 
               state.currentUserId == state.creatorId
    }

    fun leaveActivity() {
        if (_uiState.value.isLeaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLeaving = true, error = null) }
            
            when (val result = repository.leaveActivity(activityId)) {
                is ActivityRoomRepository.ActivityRoomResult.Success -> {
                    _uiState.update { it.copy(isLeaving = false) }
                }
                is ActivityRoomRepository.ActivityRoomResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLeaving = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun completeActivity() {
        if (_uiState.value.isCompleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCompleting = true, error = null) }
            
            when (val result = repository.completeActivity(activityId)) {
                is ActivityRoomRepository.ActivityRoomResult.Success -> {
                    _uiState.update { it.copy(isCompleting = false) }
                    
                    // Émettre l'événement de complétion pour rafraîchir les achievements
                    _activityCompleted.emit(activityId)
                }
                is ActivityRoomRepository.ActivityRoomResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCompleting = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        loadData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    /**
     * Parser la date pour obtenir un timestamp pour le tri
     */
    private fun parseDateTimestamp(dateString: String): Long {
        return try {
            when {
                dateString.contains("T") && dateString.contains("Z") -> {
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        inputFormat.parse(dateString)?.time ?: 0L
                    } catch (e: Exception) {
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                            inputFormat.parse(dateString)?.time ?: 0L
                        } catch (e2: Exception) {
                            0L
                        }
                    }
                }
                dateString.contains("T") -> {
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        inputFormat.parse(dateString)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
                else -> 0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun convertToChatMessage(dto: ActivityMessageDto): ActivityChatMessage {
        val senderName = dto.sender?.name ?: "Unknown"
        val senderAvatar = dto.sender?.profileImageUrl 
            ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${senderName}"
        
        // Formater la date - essayer plusieurs formats
        val time = try {
            val date = when {
                // Format ISO avec millisecondes
                dto.createdAt.contains("T") && dto.createdAt.contains("Z") -> {
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        inputFormat.parse(dto.createdAt)
                    } catch (e: Exception) {
                        try {
                            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                            inputFormat.parse(dto.createdAt)
                        } catch (e2: Exception) {
                            null
                        }
                    }
                }
                // Format ISO sans Z
                dto.createdAt.contains("T") -> {
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                        inputFormat.parse(dto.createdAt)
                    } catch (e: Exception) {
                        null
                    }
                }
                else -> null
            }
            
            if (date != null) {
                val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                outputFormat.format(date)
            } else {
                dto.createdAt
            }
        } catch (e: Exception) {
            dto.createdAt
        }
        
        return ActivityChatMessage(
            id = dto.getMessageId(),
            sender = senderName,
            avatar = senderAvatar,
            text = dto.content,
            time = time
        )
    }

    private fun convertToParticipant(dto: ActivityParticipantDto): ActivityParticipant {
        val avatar = dto.profileImageUrl 
            ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${dto.name}"
        val status = if (dto.isHost) "Host" else "Joined"
        
        return ActivityParticipant(
            id = dto.getParticipantId(),
            name = dto.name,
            avatar = avatar,
            status = status
        )
    }
}

