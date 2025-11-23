package com.example.damandroid.presentation.coachprofile.model

import com.example.damandroid.domain.model.CoachProfile

data class CoachProfileUiState(
    val isLoading: Boolean = false,
    val profile: CoachProfile? = null,
    val error: String? = null
)
