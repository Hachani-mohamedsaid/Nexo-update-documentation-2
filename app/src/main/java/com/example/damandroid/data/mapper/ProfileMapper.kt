package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.AchievementDto
import com.example.damandroid.data.model.ProfileActivityDto
import com.example.damandroid.data.model.ProfileMedalDto
import com.example.damandroid.data.model.UserProfileDto
import com.example.damandroid.data.model.UserStatsDto
import com.example.damandroid.domain.model.Achievement
import com.example.damandroid.domain.model.ActivityStatus
import com.example.damandroid.domain.model.MedalRarity
import com.example.damandroid.domain.model.ProfileActivity
import com.example.damandroid.domain.model.ProfileMedal
import com.example.damandroid.domain.model.UserProfile
import com.example.damandroid.domain.model.UserStatsOverview

fun UserProfileDto.toDomain(): UserProfile {
    android.util.Log.d("ProfileMapper", "Mapping UserProfileDto to domain: activities=${activities.size}, medals=${medals.size}")
    medals.forEach { medal ->
        android.util.Log.d("ProfileMapper", "Medal: id=${medal.id}, title=${medal.title}, rarity=${medal.rarity}")
    }
    return UserProfile(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        bio = bio,
        location = location,
        isVerified = isVerified,
        stats = stats.toDomain(),
        achievements = achievements.map(AchievementDto::toDomain),
        activities = activities.map(ProfileActivityDto::toDomain),
        medals = medals.map(ProfileMedalDto::toDomain)
    )
}

private fun UserStatsDto.toDomain(): UserStatsOverview = UserStatsOverview(
    sessionsJoined = sessionsJoined,
    sessionsHosted = sessionsHosted,
    followers = followers,
    following = following,
    favoriteSports = favoriteSports,
    rating = rating
)

private fun AchievementDto.toDomain(): Achievement = Achievement(
    id = id,
    title = title,
    description = description,
    icon = icon
)

private fun ProfileActivityDto.toDomain(): ProfileActivity = ProfileActivity(
    id = id,
    title = title,
    sportIcon = sportIcon,
    date = date,
    time = time,
    location = location,
    status = ActivityStatus.valueOf(status.uppercase())
)

private fun ProfileMedalDto.toDomain(): ProfileMedal {
    val rarityEnum = try {
        // Mapper "uncommon" vers UNCOMMON (le backend utilise "uncommon" en minuscules)
        val normalizedRarity = when (rarity.lowercase()) {
            "uncommon" -> "UNCOMMON"
            else -> rarity.uppercase()
        }
        MedalRarity.valueOf(normalizedRarity)
    } catch (e: IllegalArgumentException) {
        android.util.Log.e("ProfileMapper", "Invalid rarity: $rarity, defaulting to COMMON")
        MedalRarity.COMMON
    }
    android.util.Log.d("ProfileMapper", "Mapping medal: id=$id, title=$title, rarity=$rarity -> $rarityEnum")
    return ProfileMedal(
        id = id,
        title = title,
        description = description,
        icon = icon,
        rarity = rarityEnum
    )
}

