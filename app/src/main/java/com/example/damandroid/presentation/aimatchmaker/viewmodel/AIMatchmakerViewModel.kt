package com.example.damandroid.presentation.aimatchmaker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.api.AIMatchmakerRepository
import com.example.damandroid.api.AIMatchmakerChatResult
import com.example.damandroid.api.ChatMessageDto
import com.example.damandroid.domain.usecase.GetMatchmakerRecommendations
import com.example.damandroid.presentation.aimatchmaker.model.AIMatchmakerUiState
import com.example.damandroid.presentation.aimatchmaker.model.ChatMessage
import com.example.damandroid.presentation.aimatchmaker.model.ChatRole
import com.example.damandroid.presentation.aimatchmaker.model.SuggestedActivity
import com.example.damandroid.presentation.aimatchmaker.model.SuggestedUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AIMatchmakerViewModel(
    private val getRecommendations: GetMatchmakerRecommendations,
    private val aiMatchmakerRepository: AIMatchmakerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AIMatchmakerUiState(
            isLoading = false,
            conversationHistory = listOf(
                ChatMessage(
                    id = "1",
                    role = ChatRole.ASSISTANT,
                    content = "Salut ! Je suis ton AI matchmaker. Je peux t'aider à trouver des partenaires de sport parfaits ou des activités. Que veux-tu faire aujourd'hui ?",
                    options = listOf(
                        "Trouver un partenaire de course",
                        "Rejoindre une activité de groupe",
                        "Découvrir de nouveaux sports"
                    )
                )
            )
        )
    )
    val uiState: StateFlow<AIMatchmakerUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getRecommendations() }
                .onSuccess { recommendation ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profiles = recommendation.profiles,
                            totalMatches = recommendation.totalMatches,
                            newMatchesToday = recommendation.newMatchesToday,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load recommendations"
                        )
                    }
                }
        }
    }

    /**
     * Envoyer un message à l'IA
     */
    fun sendMessage(message: String) {
        if (message.isBlank() || _uiState.value.isSendingMessage) return

        viewModelScope.launch {
            // Ajouter le message de l'utilisateur à l'historique
            val userMessage = ChatMessage(
                id = System.currentTimeMillis().toString(),
                role = ChatRole.USER,
                content = message
            )
            
            _uiState.update { state ->
                state.copy(
                    conversationHistory = state.conversationHistory + userMessage,
                    isSendingMessage = true,
                    error = null
                )
            }

            // Préparer l'historique de conversation pour l'API
            val conversationHistory = _uiState.value.conversationHistory.map { chatMsg ->
                ChatMessageDto(
                    role = if (chatMsg.role == ChatRole.USER) "user" else "assistant",
                    content = chatMsg.content
                )
            }

            // Appeler l'API
            when (val result = aiMatchmakerRepository.sendMessage(message, conversationHistory)) {
                is AIMatchmakerChatResult.Success -> {
                    val response = result.response
                    
                    // Convertir les activités suggérées
                    val suggestedActivities = response.suggestedActivities?.map { activityDto ->
                        SuggestedActivity(
                            id = activityDto.id,
                            title = activityDto.title,
                            sportType = activityDto.sportType,
                            location = activityDto.location,
                            date = activityDto.date,
                            time = activityDto.time,
                            participants = activityDto.participants,
                            maxParticipants = activityDto.maxParticipants,
                            level = activityDto.level,
                            matchScore = activityDto.matchScore
                        )
                    }

                    // Convertir les utilisateurs suggérés
                    val suggestedUsers = response.suggestedUsers?.map { userDto ->
                        SuggestedUser(
                            id = userDto.id,
                            name = userDto.name,
                            profileImageUrl = userDto.profileImageUrl,
                            sport = userDto.sport,
                            distance = userDto.distance,
                            matchScore = userDto.matchScore,
                            bio = userDto.bio,
                            availability = userDto.availability
                        )
                    }

                    // Ajouter la réponse de l'IA à l'historique
                    val aiMessage = ChatMessage(
                        id = (System.currentTimeMillis() + 1).toString(),
                        role = ChatRole.ASSISTANT,
                        content = response.message,
                        suggestedActivities = suggestedActivities,
                        suggestedUsers = suggestedUsers,
                        options = response.options
                    )

                    _uiState.update { state ->
                        state.copy(
                            conversationHistory = state.conversationHistory + aiMessage,
                            isSendingMessage = false
                        )
                    }
                }
                is AIMatchmakerChatResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isSendingMessage = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
}

