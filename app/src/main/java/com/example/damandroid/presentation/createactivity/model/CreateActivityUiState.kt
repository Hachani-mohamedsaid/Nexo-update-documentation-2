package com.example.damandroid.presentation.createactivity.model

import com.example.damandroid.domain.model.ActivityVisibility
import com.example.damandroid.domain.model.CreateActivityForm
import com.example.damandroid.domain.model.CreateActivityResult
import com.example.damandroid.domain.model.SkillLevel
import com.example.damandroid.domain.model.SportCategory

data class CreateActivityUiState(
    val isLoading: Boolean = false,
    val sportCategories: List<SportCategory> = emptyList(),
    val selectedSport: SportCategory? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val date: String = "",
    val time: String = "",
    val participants: Int = 5,
    val level: SkillLevel? = null,
    val visibility: ActivityVisibility = ActivityVisibility.PUBLIC,
    val error: String? = null,
    val success: CreateActivityResult? = null
) {
    fun toForm(): CreateActivityForm = CreateActivityForm(
        sportType = selectedSport,
        title = title,
        description = description,
        location = location,
        date = date,
        time = time,
        participants = participants,
        level = level,
        visibility = visibility
    )
}

