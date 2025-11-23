package com.example.damandroid.presentation.settings.model

import com.example.damandroid.domain.model.UserSettings

data class SettingsUiState(
    val isLoading: Boolean = false,
    val settings: UserSettings? = null,
    val error: String? = null
)

