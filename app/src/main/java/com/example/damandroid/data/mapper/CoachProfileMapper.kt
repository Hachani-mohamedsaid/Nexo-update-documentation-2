package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.CoachProfileDto
import com.example.damandroid.data.model.CertificationDto
import com.example.damandroid.data.model.CoachReviewDto
import com.example.damandroid.data.model.CoachSessionDto
import com.example.damandroid.domain.model.CoachProfile
import com.example.damandroid.domain.model.Certification
import com.example.damandroid.domain.model.CoachReview
import com.example.damandroid.domain.model.CoachCourse

fun CoachProfileDto.toDomain(): CoachProfile = CoachProfile(
    id = id,
    name = name,
    avatarUrl = avatarUrl,
    coverImageUrl = coverImageUrl,
    isVerified = isVerified,
    rating = rating,
    reviewsCount = reviewsCount,
    location = location,
    sessionsCount = sessionsCount,
    followersCount = followersCount,
    experience = experience,
    experienceYears = experience,
    bio = bio,
    specializations = specializations,
    certifications = certifications.map(CertificationDto::toDomain),
    courses = sessions.map(CoachSessionDto::toDomain),
    reviews = reviews.map(CoachReviewDto::toDomain)
)

private fun CertificationDto.toDomain(): Certification = Certification(
    name = name,
    issuer = issuer
)

private fun CoachSessionDto.toDomain(): CoachCourse = CoachCourse(
    id = id,
    title = title,
    description = "$date at $time • $location",
    sportIcon = sportIcon,
    date = date,
    time = time,
    location = location,
    duration = "$spotsAvailable spots",
    level = "All Levels",
    price = "$${price}",
    format = "In-person"
)

private fun CoachReviewDto.toDomain(): CoachReview = CoachReview(
    id = id,
    reviewerName = reviewerName,
    reviewerAvatar = reviewerAvatar,
    rating = rating,
    comment = comment,
    date = date
)

