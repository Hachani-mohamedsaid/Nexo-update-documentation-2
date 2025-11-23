package com.example.damandroid.presentation.notifications.model

import com.example.damandroid.domain.model.NotificationsOverview

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val overview: NotificationsOverview? = null,
    val error: String? = null
)

