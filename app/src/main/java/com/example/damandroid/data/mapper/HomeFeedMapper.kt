package com.example.damandroid.data.mapper

import com.example.damandroid.api.ActivityResponse
import com.example.damandroid.data.model.HomeActivityDto
import com.example.damandroid.data.model.HomeFeedDto
import com.example.damandroid.data.model.SportCategoryDto
import com.example.damandroid.domain.model.HomeActivity
import com.example.damandroid.domain.model.HomeFeed
import com.example.damandroid.domain.model.SportCategory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun HomeFeedDto.toDomain(): HomeFeed = HomeFeed(
    activities = activities.map { it.toDomain() },
    sportCategories = sportCategories.map { it.toDomain() }
)

fun HomeActivityDto.toDomain(): HomeActivity = HomeActivity(
    id = id,
    title = title,
    sportType = sportType,
    sportIcon = sportIcon,
    hostName = hostName,
    hostAvatar = hostAvatar,
    hostId = hostId,
    date = date,
    time = time,
    location = location,
    distance = distance,
    spotsTotal = spotsTotal,
    spotsTaken = spotsTaken,
    level = level,
    isSaved = isSaved
)

fun SportCategoryDto.toDomain(): SportCategory = SportCategory(
    id = id,
    name = name,
    icon = icon
)

/**
 * Convertit ActivityResponse (API) vers HomeActivityDto
 */
fun ActivityResponse.toHomeActivityDto(): HomeActivityDto {
    val creator = getCreator()
    val hostName = creator?.name ?: "Unknown"
    val hostAvatar = creator?.profileImageUrl ?: ""
    
    // Formater la date
    val formattedDate = try {
        if (date.isNotEmpty()) {
            val instant = Instant.parse(date)
            val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            val today = LocalDate.now()
            when {
                localDate == today -> "Today"
                localDate == today.plusDays(1) -> "Tomorrow"
                localDate == today.plusDays(2) -> "Day After Tomorrow"
                else -> localDate.format(DateTimeFormatter.ofPattern("MMM dd"))
            }
        } else {
            "TBD"
        }
    } catch (e: Exception) {
        date
    }
    
    // Formater l'heure
    val formattedTime = try {
        if (time.isNotEmpty()) {
            val instant = Instant.parse(time)
            val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
            localTime.format(DateTimeFormatter.ofPattern("h:mm a"))
        } else {
            "TBD"
        }
    } catch (e: Exception) {
        time
    }
    
    // Obtenir l'icône du sport
    val sportIcon = getSportIcon(sportType)
    
    // Calculer la distance (pour l'instant, on met une valeur par défaut)
    val distance = "0.5 mi" // TODO: Calculer la distance réelle basée sur la localisation
    
    // Formater le niveau
    val formattedLevel = when {
        level.equals("beginner", ignoreCase = true) -> "Beginner"
        level.equals("intermediate", ignoreCase = true) -> "Intermediate"
        level.equals("advanced", ignoreCase = true) -> "Advanced"
        else -> level.replaceFirstChar { it.uppercaseChar() }
    }
    
    return HomeActivityDto(
        id = getActivityId(),
        title = title,
        sportType = sportType,
        sportIcon = sportIcon,
        hostName = hostName,
        hostAvatar = hostAvatar,
        hostId = getCreatorId(), // ID du créateur
        date = formattedDate,
        time = formattedTime,
        location = location,
        distance = distance,
        spotsTotal = participants,
        spotsTaken = participantIds?.size ?: 0, // Nombre réel de participants depuis participantIds
        level = formattedLevel,
        isSaved = false // TODO: Récupérer l'état "saved" depuis l'API si disponible
    )
}

/**
 * Obtient l'icône emoji pour un type de sport
 */
private fun getSportIcon(sportType: String): String {
    return when (sportType.lowercase()) {
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
        "climbing" -> "🧗"
        "boxing" -> "🥊"
        "martial arts" -> "🥋"
        else -> "🏃"
    }
}

