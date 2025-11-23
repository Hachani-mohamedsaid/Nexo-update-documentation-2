package com.example.damandroid.data.model

data class MatchUserProfileDto(
    val id: String,
    val name: String,
    val age: Int,
    val avatarUrl: String,
    val coverImageUrl: String,
    val location: String,
    val distance: String,
    val bio: String,
    val sports: List<SportDto>,
    val interests: List<String>,
    val rating: Double,
    val activitiesJoined: Int
)

data class SportDto(
    val name: String,
    val icon: String,
    val level: String
)

