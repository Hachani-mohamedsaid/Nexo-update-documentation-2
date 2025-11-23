package com.example.damandroid.domain.model

data class MatchUserProfile(
    val id: String,
    val name: String,
    val age: Int,
    val avatarUrl: String,
    val coverImageUrl: String,
    val location: String,
    val distance: String,
    val bio: String,
    val sports: List<Sport>,
    val interests: List<String>,
    val rating: Double,
    val activitiesJoined: Int
)

data class Sport(
    val name: String,
    val icon: String,
    val level: String
)

