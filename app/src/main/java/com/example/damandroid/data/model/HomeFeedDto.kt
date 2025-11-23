package com.example.damandroid.data.model

data class HomeFeedDto(
    val activities: List<HomeActivityDto>,
    val sportCategories: List<SportCategoryDto>
)

data class HomeActivityDto(
    val id: String,
    val title: String,
    val sportType: String,
    val sportIcon: String,
    val hostName: String,
    val hostAvatar: String,
    val hostId: String? = null, // ID du créateur de l'activité
    val date: String,
    val time: String,
    val location: String,
    val distance: String,
    val spotsTotal: Int,
    val spotsTaken: Int,
    val level: String,
    val isSaved: Boolean
)

data class SportCategoryDto(
    val id: String,
    val name: String,
    val icon: String
)

