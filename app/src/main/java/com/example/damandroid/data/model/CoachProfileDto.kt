package com.example.damandroid.data.model

data class CoachProfileDto(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val coverImageUrl: String,
    val isVerified: Boolean,
    val rating: Double,
    val reviewsCount: Int,
    val location: String,
    val sessionsCount: Int,
    val followersCount: Int,
    val experience: String,
    val bio: String,
    val specializations: List<String>,
    val certifications: List<CertificationDto>,
    val sessions: List<CoachSessionDto>,
    val reviews: List<CoachReviewDto>
)

data class CertificationDto(
    val name: String,
    val issuer: String
)

data class CoachSessionDto(
    val id: String,
    val title: String,
    val sportIcon: String,
    val date: String,
    val time: String,
    val location: String,
    val price: Int,
    val spotsAvailable: Int
)

data class CoachReviewDto(
    val id: String,
    val reviewerName: String,
    val reviewerAvatar: String,
    val rating: Int,
    val comment: String,
    val date: String
)
