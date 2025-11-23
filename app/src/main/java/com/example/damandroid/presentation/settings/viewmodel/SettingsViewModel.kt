package com.example.damandroid.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.usecase.GetUserSettings
import com.example.damandroid.domain.usecase.UpdateSettingsToggle
import com.example.damandroid.presentation.settings.model.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getUserSettings: GetUserSettings,
    private val updateSettingsToggle: UpdateSettingsToggle
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getUserSettings() }
                .onSuccess { settings ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            settings = settings,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load settings"
                        )
                    }
                }
        }
    }

    fun onToggleChanged(itemId: String, enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { updateSettingsToggle(itemId, enabled) }
                .onSuccess { settings ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            settings = settings,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to update setting"
                        )
                    }
                }
        }
    }
}

