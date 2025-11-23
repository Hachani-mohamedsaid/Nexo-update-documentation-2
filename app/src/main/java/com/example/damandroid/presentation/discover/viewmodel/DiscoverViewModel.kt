package com.example.damandroid.presentation.discover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.api.ActivityApiService
import com.example.damandroid.api.RetrofitClient
import com.example.damandroid.domain.usecase.GetDiscoverOverview
import com.example.damandroid.domain.model.DiscoverOverview
import com.example.damandroid.domain.model.TrendingActivity
import com.example.damandroid.presentation.discover.model.DiscoverUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val getDiscoverOverview: GetDiscoverOverview
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState(isLoading = true))
    val uiState: StateFlow<DiscoverUiState> = _uiState

    private val activityApi: ActivityApiService = RetrofitClient.activityApiService

    // Cache de l'overview initial pour pouvoir rétablir les trending par défaut
    private var baseOverview: DiscoverOverview? = null

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getDiscoverOverview() }
                .onSuccess { overview ->
                    baseOverview = overview
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            overview = overview,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load discover data"
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onCategorySelected(categoryName: String?) {
        val toggled =
            if (_uiState.value.selectedCategory == categoryName) null else categoryName
        _uiState.update { it.copy(selectedCategory = toggled) }

        viewModelScope.launch {
            val currentOverview = baseOverview ?: return@launch
            if (toggled.isNullOrBlank()) {
                // Rétablir les trending d'origine
                _uiState.update { it.copy(overview = currentOverview) }
                return@launch
            }

            // Charger depuis le backend les activités par sport
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val resp = activityApi.getActivities(
                    visibility = "public",
                    sport = toggled,
                    sportType = toggled
                )
                val list = resp.body().orEmpty()
                list.map { dto ->
                    TrendingActivity(
                        id = dto.getActivityId(),
                        title = dto.title,
                        sportIcon = sportToIcon(dto.sportType),
                        date = dto.date,
                        time = dto.time,
                        participants = dto.participants,
                        maxParticipants = dto.participants, // backend may not have maxParticipants
                        location = dto.location,
                        hostName = dto.getCreator()?.name ?: "Host",
                        hostAvatar = dto.getCreator()?.profileImageUrl ?: ""
                    )
                }
            }.onSuccess { trending ->
                val newOverview = currentOverview.copy(trendingActivities = trending)
                _uiState.update { it.copy(isLoading = false, overview = newOverview) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load activities") }
            }
        }
    }

    private fun sportToIcon(sportType: String): String = when (sportType.lowercase()) {
        "basketball" -> "🏀"
        "running" -> "🏃"
        "tennis" -> "🎾"
        "soccer", "football" -> "⚽"
        "swimming" -> "🏊"
        "cycling" -> "🚴"
        "yoga" -> "🧘"
        "gym" -> "💪"
        "hiking" -> "🥾"
        else -> "🏅"
    }
}

