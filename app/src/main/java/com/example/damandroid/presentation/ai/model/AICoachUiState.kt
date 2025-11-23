package com.example.damandroid.presentation.ai.model

import com.example.damandroid.domain.model.AICoachOverview

data class AICoachUiState(
    val isLoading: Boolean = false,
    val overview: AICoachOverview? = null,
    val error: String? = null,
    val selectedTab: AICoachTab = AICoachTab.OVERVIEW,
    val needsGoogleFitSync: Boolean = false
)

enum class AICoachTab {
    OVERVIEW,
    SUGGESTIONS,
    TIPS
}

