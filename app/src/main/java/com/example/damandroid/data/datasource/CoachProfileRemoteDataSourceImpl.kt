package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.CertificationDto
import com.example.damandroid.data.model.CoachProfileDto
import com.example.damandroid.data.model.CoachReviewDto
import com.example.damandroid.data.model.CoachSessionDto

class CoachProfileRemoteDataSourceImpl : CoachProfileRemoteDataSource {
    override suspend fun fetchCoachProfile(coachId: String): CoachProfileDto = CoachProfileDto(
        id = coachId,
        name = "Alex Thompson",
        avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Alex",
        coverImageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=1200",
        isVerified = true,
        rating = 4.8,
        reviewsCount = 124,
        location = "Los Angeles, CA",
        sessionsCount = 450,
        followersCount = 1234,
        experience = "8 years",
        bio = "Certified personal trainer with 8+ years of experience. Specialized in HIIT, strength training, and functional fitness.",
        specializations = listOf("HIIT", "Strength Training", "Yoga", "Running"),
        certifications = listOf(
            CertificationDto("NASM-CPT", "NASM"),
            CertificationDto("ACE Certified", "ACE"),
            CertificationDto("Yoga Alliance RYT-200", "Yoga Alliance")
        ),
        sessions = listOf(
            CoachSessionDto(
                id = "session-1",
                title = "Sunrise HIIT Bootcamp",
                sportIcon = "🌅",
                date = "Tomorrow",
                time = "6:30 AM",
                location = "Downtown Arena",
                price = 25,
                spotsAvailable = 4
            ),
            CoachSessionDto(
                id = "session-2",
                title = "Strength & Conditioning",
                sportIcon = "🏋️",
                date = "Nov 10",
                time = "5:00 PM",
                location = "Peak Performance Gym",
                price = 30,
                spotsAvailable = 2
            )
        ),
        reviews = listOf(
            CoachReviewDto(
                id = "review-1",
                reviewerName = "Sarah M.",
                reviewerAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah",
                rating = 5,
                comment = "Alex is incredibly motivating and keeps sessions challenging!",
                date = "Oct 20, 2025"
            ),
            CoachReviewDto(
                id = "review-2",
                reviewerName = "Michael R.",
                reviewerAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Michael",
                rating = 4,
                comment = "Great attention to form and technique. Highly recommended.",
                date = "Oct 12, 2025"
            )
        )
    )
}
