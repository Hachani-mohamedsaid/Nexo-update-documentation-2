package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.SessionsRecommendationDto
import com.example.damandroid.data.model.SuggestedActivityDto

class AISuggestionsRemoteDataSourceImpl : AISuggestionsRemoteDataSource {
    override suspend fun fetchSessions(): SessionsRecommendationDto = SessionsRecommendationDto(
        upcoming = listOf(
            SuggestedActivityDto(
                id = "upcoming_1",
                title = "Morning Run Club",
                sport = "Running",
                description = "5k run with local runners",
                date = "Tomorrow",
                time = "7:00 AM",
                location = "City Park",
                organizer = "Alex Thompson",
                participantsCount = 12,
                capacity = 20,
                isRecommended = true
            )
        ),
        recommended = listOf(
            SuggestedActivityDto(
                id = "recommended_1",
                title = "Sunset Yoga Session",
                sport = "Yoga",
                description = "Relaxing yoga with panoramic views",
                date = "Fri, Nov 8",
                time = "6:00 PM",
                location = "Skyline Rooftop",
                organizer = "Jessica Lee",
                participantsCount = 8,
                capacity = 15,
                isRecommended = true
            )
        ),
        mapActivities = listOf(
            SuggestedActivityDto(
                id = "map_1",
                title = "Beach Volleyball Meetup",
                sport = "Volleyball",
                description = "Friendly beach volleyball games",
                date = "Sat, Nov 9",
                time = "4:00 PM",
                location = "North Beach",
                organizer = "Mia Johnson",
                participantsCount = 10,
                capacity = 16,
                isRecommended = false
            )
        )
    )
}

