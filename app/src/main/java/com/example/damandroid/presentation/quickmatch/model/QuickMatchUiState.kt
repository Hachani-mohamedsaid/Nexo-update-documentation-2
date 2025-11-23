package com.example.damandroid.presentation.quickmatch.model

import com.example.damandroid.domain.model.MatchUserProfile

data class QuickMatchUiState(
    val isLoading: Boolean = false,
    val profiles: List<MatchUserProfile> = emptyList(),
    val error: String? = null
)

