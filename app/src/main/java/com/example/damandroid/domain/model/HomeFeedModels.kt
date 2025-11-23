package com.example.damandroid.domain.model

data class HomeFeed(
    val activities: List<HomeActivity>,
    val sportCategories: List<SportCategory>
)

data class HomeActivity(
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

data class SportCategory(
    val id: String,
    val name: String,
    val icon: String
)

