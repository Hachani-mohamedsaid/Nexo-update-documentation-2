package com.example.damandroid.presentation.profile.model

import com.example.damandroid.domain.model.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val selectedTab: ProfileTab = ProfileTab.ABOUT,
    val error: String? = null,
    val isChangingPassword: Boolean = false,
    val changePasswordMessage: String? = null,
    val changePasswordError: String? = null
)

enum class ProfileTab {
    ABOUT,
    ACTIVITIES,
    MEDALS
}

