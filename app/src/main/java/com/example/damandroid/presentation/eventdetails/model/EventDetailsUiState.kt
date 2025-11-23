package com.example.damandroid.presentation.eventdetails.model

import com.example.damandroid.domain.model.EventDetails

data class EventDetailsUiState(
    val isLoading: Boolean = false,
    val event: EventDetails? = null,
    val error: String? = null
)

