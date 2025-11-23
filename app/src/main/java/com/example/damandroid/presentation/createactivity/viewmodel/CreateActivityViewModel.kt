package com.example.damandroid.presentation.createactivity.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.model.ActivityVisibility
import com.example.damandroid.domain.model.SkillLevel
import com.example.damandroid.domain.model.SportCategory
import com.example.damandroid.domain.usecase.CreateActivityUseCase
import com.example.damandroid.domain.usecase.GetSportCategories
import com.example.damandroid.presentation.createactivity.model.CreateActivityUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateActivityViewModel(
    private val getSportCategories: GetSportCategories,
    private val createActivity: CreateActivityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateActivityUiState(isLoading = true))
    val uiState: StateFlow<CreateActivityUiState> = _uiState

    // Événement émis après création réussie d'activité pour rafraîchir les achievements
    private val _activityCreated = MutableSharedFlow<String>(replay = 0)
    val activityCreated: SharedFlow<String> = _activityCreated.asSharedFlow()

    init {
        loadSportCategories()
    }

    private fun loadSportCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getSportCategories() }
                .onSuccess { categories ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sportCategories = categories,
                            selectedSport = categories.firstOrNull()
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load categories"
                        )
                    }
                }
        }
    }

    fun onSportSelected(category: SportCategory) {
        _uiState.update { it.copy(selectedSport = category) }
    }

    fun onTitleChanged(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onLocationChanged(location: String) {
        _uiState.update { it.copy(location = location) }
    }

    fun onDateChanged(date: String) {
        _uiState.update { it.copy(date = date) }
    }

    fun onTimeChanged(time: String) {
        _uiState.update { it.copy(time = time) }
    }

    fun onParticipantsChanged(count: Int) {
        _uiState.update { it.copy(participants = count) }
    }

    fun onLevelSelected(level: SkillLevel) {
        _uiState.update { it.copy(level = level) }
    }

    fun onVisibilitySelected(visibility: ActivityVisibility) {
        _uiState.update { it.copy(visibility = visibility) }
    }

    fun onSubmit() {
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                createActivity(currentState.toForm())
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        success = result
                    )
                }
                // Émettre l'événement de création pour rafraîchir les achievements
                _activityCreated.emit(result.activityId)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to create activity"
                    )
                }
            }
        }
    }

    fun onSuccessDialogDismissed() {
        _uiState.update { it.copy(success = null) }
    }

    /**
     * Réinitialise le formulaire pour une nouvelle création
     */
    fun resetForm() {
        _uiState.update { 
            it.copy(
                title = "",
                description = "",
                location = "",
                date = "",
                time = "",
                participants = 5,
                level = null,
                visibility = ActivityVisibility.PUBLIC,
                error = null,
                success = null,
                isLoading = false
            )
        }
    }
}

