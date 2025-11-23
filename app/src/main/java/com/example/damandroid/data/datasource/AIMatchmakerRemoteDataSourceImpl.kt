package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.MatchmakerProfileDto
import com.example.damandroid.data.model.MatchmakerRecommendationDto

class AIMatchmakerRemoteDataSourceImpl : AIMatchmakerRemoteDataSource {
    override suspend fun fetchRecommendations(): MatchmakerRecommendationDto = MatchmakerRecommendationDto(
        profiles = listOf(
            MatchmakerProfileDto(
                id = "1",
                name = "Jessica M.",
                age = 27,
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Jessica",
                coverImageUrl = "https://images.unsplash.com/photo-1542293787938-4d2226c90e88?w=900",
                location = "Downtown",
                distance = "2.3 mi",
                bio = "Outdoor fitness coach and marathon runner",
                sports = listOf("Running", "Yoga", "Cycling"),
                tags = listOf("Morning workouts", "Marathon", "Endurance"),
                rating = 4.9
            ),
            MatchmakerProfileDto(
                id = "2",
                name = "Marcus T.",
                age = 30,
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Marcus",
                coverImageUrl = "https://images.unsplash.com/photo-1508609349937-5ec4ae374ebf?w=900",
                location = "Waterfront",
                distance = "1.8 mi",
                bio = "Competitive swimmer and HIIT trainer",
                sports = listOf("Swimming", "HIIT", "Crossfit"),
                tags = listOf("Evening sessions", "Strength", "Recovery"),
                rating = 4.8
            )
        ),
        totalMatches = 24,
        newMatchesToday = 3
    )
}

