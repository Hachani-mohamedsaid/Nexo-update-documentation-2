package com.example.damandroid.presentation.discover.model

import com.example.damandroid.domain.model.DiscoverOverview

data class DiscoverUiState(
    val isLoading: Boolean = false,
    val overview: DiscoverOverview? = null,
    val searchQuery: String = "",
    val error: String? = null,
    val selectedCategory: String? = null
)

