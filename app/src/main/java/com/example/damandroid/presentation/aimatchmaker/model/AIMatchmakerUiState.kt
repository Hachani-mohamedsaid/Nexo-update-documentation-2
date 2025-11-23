package com.example.damandroid.presentation.aimatchmaker.model

import com.example.damandroid.domain.model.MatchmakerProfile

data class AIMatchmakerUiState(
    val isLoading: Boolean = false,
    val profiles: List<MatchmakerProfile> = emptyList(),
    val totalMatches: Int = 0,
    val newMatchesToday: Int = 0,
    val error: String? = null,
    val isSendingMessage: Boolean = false,
    val conversationHistory: List<ChatMessage> = emptyList()
)

data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val suggestedActivities: List<SuggestedActivity>? = null,
    val suggestedUsers: List<SuggestedUser>? = null,
    val options: List<String>? = null
)

enum class ChatRole {
    USER, ASSISTANT
}

data class SuggestedActivity(
    val id: String,
    val title: String,
    val sportType: String,
    val location: String,
    val date: String,
    val time: String,
    val participants: Int,
    val maxParticipants: Int,
    val level: String,
    val matchScore: Int? = null
)

data class SuggestedUser(
    val id: String,
    val name: String,
    val profileImageUrl: String? = null,
    val sport: String,
    val distance: String? = null,
    val matchScore: Int? = null,
    val bio: String? = null,
    val availability: String? = null
)

