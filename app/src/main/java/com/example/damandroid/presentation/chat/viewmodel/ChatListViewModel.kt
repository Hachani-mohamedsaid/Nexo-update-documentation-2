package com.example.damandroid.presentation.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.usecase.GetChatPreviews
import com.example.damandroid.domain.usecase.SearchChats
import com.example.damandroid.presentation.chat.model.ChatListUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatListViewModel(
    private val getChatPreviews: GetChatPreviews,
    private val searchChats: SearchChats
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState(isLoading = true))
    val uiState: StateFlow<ChatListUiState> = _uiState

    private var searchJob: Job? = null

    init {
        loadChats()
    }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                getChatPreviews()
            }.onSuccess { chats ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        chats = chats,
                        filteredChats = chats,
                        error = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250) // debounce
            runCatching {
                if (query.isBlank()) {
                    _uiState.value.chats
                } else {
                    searchChats(query)
                }
            }.onSuccess { results ->
                _uiState.update { it.copy(filteredChats = results) }
            }
        }
    }

    fun refresh() {
        loadChats()
    }
}

