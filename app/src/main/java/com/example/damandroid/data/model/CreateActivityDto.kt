package com.example.damandroid.data.model

import com.example.damandroid.api.ActivityResponse
import com.example.damandroid.api.CreateActivityRequest

/**
 * DTO pour créer une activité
 * Utilise CreateActivityRequest de l'API comme source
 */
data class CreateActivityRequestDto(
    val sportType: String,
    val title: String,
    val description: String? = null,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val date: String,
    val time: String,
    val participants: Int,
    val level: String,
    val visibility: String
)

/**
 * Extension pour convertir CreateActivityRequestDto en CreateActivityRequest (API)
 */
fun CreateActivityRequestDto.toApiRequest(): CreateActivityRequest {
    return CreateActivityRequest(
        sportType = sportType,
        title = title,
        description = description,
        location = location,
        latitude = latitude,
        longitude = longitude,
        date = date,
        time = time,
        participants = participants,
        level = level,
        visibility = visibility
    )
}

/**
 * DTO pour la réponse de création d'activité
 * Utilise ActivityResponse de l'API comme source
 */
data class CreateActivityResponseDto(
    val activityId: String,
    val shareLink: String? = null
)

/**
 * Extension pour convertir ActivityResponse (API) en CreateActivityResponseDto
 */
fun ActivityResponse.toCreateActivityResponseDto(): CreateActivityResponseDto {
    return CreateActivityResponseDto(
        activityId = getActivityId(),
        shareLink = null // L'API ne retourne pas de shareLink, on peut le construire si nécessaire
    )
}

/**
 * DTO pour les catégories de sport (pour compatibilité avec l'existant)
 */
data class CreateActivityCategoryDto(
    val id: String,
    val name: String,
    val icon: String
)

