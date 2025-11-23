package com.example.damandroid.presentation.aisuggestions.model

import com.example.damandroid.domain.model.SessionsRecommendation

data class AISuggestionsUiState(
    val isLoading: Boolean = false,
    val recommendation: SessionsRecommendation? = null,
    val error: String? = null
)

