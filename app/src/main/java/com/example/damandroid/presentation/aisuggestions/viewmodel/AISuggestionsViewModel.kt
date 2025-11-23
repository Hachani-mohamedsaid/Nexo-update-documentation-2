package com.example.damandroid.presentation.aisuggestions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.usecase.GetSessionsRecommendation
import com.example.damandroid.presentation.aisuggestions.model.AISuggestionsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AISuggestionsViewModel(
    private val getSessionsRecommendation: GetSessionsRecommendation
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISuggestionsUiState(isLoading = true))
    val uiState: StateFlow<AISuggestionsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getSessionsRecommendation() }
                .onSuccess { recommendation ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            recommendation = recommendation,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load sessions"
                        )
                    }
                }
        }
    }
}

