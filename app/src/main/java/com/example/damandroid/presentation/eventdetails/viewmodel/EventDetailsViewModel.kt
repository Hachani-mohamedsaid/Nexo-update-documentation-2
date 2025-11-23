package com.example.damandroid.presentation.eventdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.usecase.GetEventDetails
import com.example.damandroid.presentation.eventdetails.model.EventDetailsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EventDetailsViewModel(
    private val getEventDetails: GetEventDetails
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventDetailsUiState(isLoading = true))
    val uiState: StateFlow<EventDetailsUiState> = _uiState

    fun load(eventId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getEventDetails(eventId) }
                .onSuccess { details ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            event = details,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load event details"
                        )
                    }
                }
        }
    }
}

