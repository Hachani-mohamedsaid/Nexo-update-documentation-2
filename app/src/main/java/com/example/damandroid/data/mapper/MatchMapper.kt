package com.example.damandroid.data.mapper

import com.example.damandroid.api.QuickMatchProfileResponse
import com.example.damandroid.api.SportResponse
import com.example.damandroid.data.model.MatchUserProfileDto
import com.example.damandroid.data.model.SportDto
import com.example.damandroid.domain.model.MatchUserProfile
import com.example.damandroid.domain.model.Sport

fun MatchUserProfileDto.toDomain(): MatchUserProfile = MatchUserProfile(
    id = id,
    name = name,
    age = age,
    avatarUrl = avatarUrl,
    coverImageUrl = coverImageUrl,
    location = location,
    distance = distance,
    bio = bio,
    sports = sports.map(SportDto::toDomain),
    interests = interests,
    rating = rating,
    activitiesJoined = activitiesJoined
)

private fun SportDto.toDomain(): Sport = Sport(
    name = name,
    icon = icon,
    level = level
)

/**
 * Convertit QuickMatchProfileResponse (API) vers MatchUserProfileDto
 * 
 * Le backend retourne les utilisateurs avec leurs sportsInterests.
 * On convertit sportsInterests en liste de SportDto pour l'affichage.
 */
fun QuickMatchProfileResponse.toMatchUserProfileDto(): MatchUserProfileDto {
    // Convertir sportsInterests en Sports détaillés
    val sportsList = if (sports != null && sports.isNotEmpty()) {
        // Si le backend retourne des sports détaillés, les utiliser
        sports.map { sport ->
            SportDto(
                name = sport.name,
                icon = sport.icon ?: getSportIcon(sport.name),
                level = sport.level ?: "Intermediate"
            )
        }
    } else if (sportsInterests != null && sportsInterests.isNotEmpty()) {
        // Sinon, convertir sportsInterests en Sports
        sportsInterests.map { sportName ->
            SportDto(
                name = sportName,
                icon = getSportIcon(sportName),
                level = "Intermediate" // Niveau par défaut
            )
        }
    } else {
        emptyList()
    }
    
    // Utiliser bio ou about comme description
    val bioText = bio ?: about ?: ""
    
    // Utiliser avatarUrl ou profileImageUrl
    val avatar = avatarUrl ?: profileImageUrl ?: ""
    
    // Calculer la distance (si non fournie)
    val distanceText = distance ?: "Unknown"
    
    return MatchUserProfileDto(
        id = getProfileId(),
        name = name,
        age = getAge(),
        avatarUrl = avatar,
        coverImageUrl = coverImageUrl ?: avatar, // Utiliser avatar comme cover si non fourni
        location = location ?: "Unknown",
        distance = distanceText,
        bio = bioText,
        sports = sportsList,
        interests = interests ?: emptyList(),
        rating = rating ?: 0.0,
        activitiesJoined = activitiesJoined ?: 0
    )
}

/**
 * Convertit LikeProfileResponse (API) vers LikeResult (domain)
 */
fun com.example.damandroid.api.LikeProfileResponse.toLikeResult(): com.example.damandroid.domain.repository.LikeResult {
    return com.example.damandroid.domain.repository.LikeResult(
        isMatch = isMatch,
        matchedProfile = matchedProfile?.let { it.toMatchUserProfileDto().toDomain() }
    )
}

/**
 * Obtient l'icône emoji pour un sport basé sur son nom
 */
private fun getSportIcon(sportName: String): String {
    return when (sportName.lowercase()) {
        "football", "soccer" -> "⚽"
        "basketball" -> "🏀"
        "running" -> "🏃"
        "cycling" -> "🚴"
        "tennis" -> "🎾"
        "swimming" -> "🏊"
        "yoga" -> "🧘"
        "volleyball" -> "🏐"
        "baseball" -> "⚾"
        "golf" -> "⛳"
        "skiing" -> "⛷️"
        "snowboarding" -> "🏂"
        "surfing" -> "🏄"
        "climbing", "rock climbing" -> "🧗"
        "boxing" -> "🥊"
        "martial arts" -> "🥋"
        "hiking" -> "🥾"
        "dance" -> "💃"
        "pilates" -> "🧘‍♀️"
        "zumba" -> "🎵"
        "crossfit" -> "💪"
        else -> "🏃" // Icône par défaut
    }
}

