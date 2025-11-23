package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.DiscoverOverviewDto
import com.example.damandroid.data.model.DiscoverSportCategoryDto
import com.example.damandroid.data.model.DiscoverUserDto
import com.example.damandroid.data.model.FeaturedCoachDto
import com.example.damandroid.data.model.TrendingActivityDto

class DiscoverRemoteDataSourceImpl : DiscoverRemoteDataSource {
    override suspend fun fetchOverview(): DiscoverOverviewDto = DiscoverOverviewDto(
        featuredCoach = FeaturedCoachDto(
            id = "coach_1",
            name = "Amelia Carter",
            title = "Certified Triathlon Coach",
            rating = 4.9,
            reviewCount = 128,
            avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Amelia",
            badge = "PRO"
        ),
        sportCategories = listOf(
            DiscoverSportCategoryDto("basketball", "Basketball", "🏀", "#FF6B6B"),
            DiscoverSportCategoryDto("running", "Running", "🏃", "#4ECDC4"),
            DiscoverSportCategoryDto("tennis", "Tennis", "🎾", "#FFE66D"),
            DiscoverSportCategoryDto("soccer", "Soccer", "⚽", "#95E1D3"),
            DiscoverSportCategoryDto("swimming", "Swimming", "🏊", "#3498DB"),
            DiscoverSportCategoryDto("cycling", "Cycling", "🚴", "#E74C3C"),
            DiscoverSportCategoryDto("yoga", "Yoga", "🧘", "#9B59B6"),
            DiscoverSportCategoryDto("gym", "Gym", "💪", "#F39C12"),
            DiscoverSportCategoryDto("hiking", "Hiking", "🥾", "#27AE60")
        ),
        trendingActivities = listOf(
            TrendingActivityDto(
                id = "activity_1",
                title = "Sunset Beach Volleyball",
                sportIcon = "🏐",
                date = "Sat, Nov 8",
                time = "5:30 PM",
                participants = 8,
                maxParticipants = 12,
                location = "North Beach",
                hostName = "Mia Johnson",
                hostAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Mia"
            ),
            TrendingActivityDto(
                id = "activity_2",
                title = "Morning Run Club",
                sportIcon = "🏃",
                date = "Sun, Nov 9",
                time = "7:00 AM",
                participants = 14,
                maxParticipants = 20,
                location = "City Park",
                hostName = "Alex Rivera",
                hostAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Alex"
            )
        ),
        activeUsers = listOf(
            DiscoverUserDto(
                id = "user_1",
                name = "Jessica Lee",
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Jessica",
                sport = "Running",
                distance = "0.3 mi"
            ),
            DiscoverUserDto(
                id = "user_2",
                name = "Tom Harris",
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Tom",
                sport = "Cycling",
                distance = "0.7 mi"
            ),
            DiscoverUserDto(
                id = "user_3",
                name = "Nina Patel",
                avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Nina",
                sport = "Yoga",
                distance = "1.2 mi"
            )
        )
    )
}

