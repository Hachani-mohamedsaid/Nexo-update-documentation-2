package com.example.damandroid.presentation.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.api.CreateChatRequest
import com.example.damandroid.domain.model.UserSearchResult
import com.example.damandroid.domain.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val users: List<UserSearchResult> = emptyList(),
    val error: String? = null,
    val isCreatingChat: Boolean = false
)

class UserSearchViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserSearchUiState())
    val uiState: StateFlow<UserSearchUiState> = _uiState
    
    private var searchJob: Job? = null
    
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        
        if (query.length < 2) {
            _uiState.update { it.copy(users = emptyList()) }
            return
        }
        
        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                chatRepository.searchUsers(query)
            }.onSuccess { users ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        users = users,
                        error = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to search users"
                    )
                }
            }
        }
    }
    
    fun createChat(userId: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingChat = true, error = null) }
            runCatching {
                chatRepository.createChat(
                    CreateChatRequest(participantIds = listOf(userId))
                )
            }.onSuccess { chatId ->
                _uiState.update { it.copy(isCreatingChat = false) }
                onSuccess(chatId)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isCreatingChat = false,
                        error = throwable.message ?: "Failed to create chat"
                    )
                }
            }
        }
    }
}

