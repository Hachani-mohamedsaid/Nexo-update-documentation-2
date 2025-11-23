package com.example.damandroid.presentation.achievements.model

import com.example.damandroid.domain.model.AchievementsOverview
import com.example.damandroid.domain.model.AchievementBadge
import com.example.damandroid.domain.model.AchievementChallenge
import com.example.damandroid.domain.model.LeaderboardEntry

data class AchievementsUiState(
    val isLoading: Boolean = false,
    val overview: AchievementsOverview? = null,
    val selectedTab: AchievementsTab = AchievementsTab.BADGES,
    val error: String? = null
) {
    val badges: List<AchievementBadge>
        get() = overview?.badges.orEmpty()

    val challenges: List<AchievementChallenge>
        get() = overview?.challenges.orEmpty()

    val leaderboard: List<LeaderboardEntry>
        get() = overview?.leaderboard.orEmpty()
}

enum class AchievementsTab {
    BADGES,
    CHALLENGES,
    LEADERBOARD
}

