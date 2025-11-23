package com.example.damandroid.presentation.coachprofile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.usecase.GetCoachProfile
import com.example.damandroid.presentation.coachprofile.model.CoachProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CoachProfileViewModel(
    private val getCoachProfile: GetCoachProfile
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoachProfileUiState(isLoading = true))
    val uiState: StateFlow<CoachProfileUiState> = _uiState

    fun load(coachId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getCoachProfile(coachId) }
                .onSuccess { profile ->
                    _uiState.update { it.copy(isLoading = false, profile = profile) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load coach profile"
                        )
                    }
                }
        }
    }
}
