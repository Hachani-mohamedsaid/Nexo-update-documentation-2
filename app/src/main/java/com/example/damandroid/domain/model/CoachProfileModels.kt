package com.example.damandroid.domain.model

data class CoachProfile(
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
    val experienceYears: String,
    val bio: String,
    val specializations: List<String>,
    val certifications: List<Certification>,
    val courses: List<CoachCourse>,
    val reviews: List<CoachReview>
)

data class Certification(
    val name: String,
    val issuer: String
)

data class CoachCourse(
    val id: String,
    val title: String,
    val description: String,
    val sportIcon: String,
    val date: String,
    val time: String,
    val location: String,
    val duration: String,
    val level: String,
    val price: String,
    val format: String
)

data class CoachReview(
    val id: String,
    val reviewerName: String,
    val reviewerAvatar: String,
    val rating: Int,
    val comment: String,
    val date: String
)

