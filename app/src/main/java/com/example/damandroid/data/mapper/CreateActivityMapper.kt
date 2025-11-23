package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.CreateActivityRequestDto
import com.example.damandroid.data.model.CreateActivityResponseDto
import com.example.damandroid.data.model.SportCategoryDto
import com.example.damandroid.domain.model.ActivityVisibility
import com.example.damandroid.domain.model.CreateActivityForm
import com.example.damandroid.domain.model.CreateActivityResult
import com.example.damandroid.domain.model.SkillLevel
import com.example.damandroid.domain.model.SportCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun SportCategoryDto.toCreateActivityDomain(): SportCategory = SportCategory(
    id = id,
    name = name,
    icon = icon
)

/**
 * Convertit une date et une heure en format ISO 8601 pour l'API
 * @param dateString Format attendu: "YYYY-MM-DD" ou autre format parsable
 * @param timeString Format attendu: "HH:mm" ou autre format parsable
 * @return Format ISO 8601 complet: "YYYY-MM-DDTHH:mm:ssZ"
 */
private fun formatDateTimeForApi(dateString: String, timeString: String): String {
    return try {
        // Essayer de parser la date
        val date = try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
        } catch (e: DateTimeParseException) {
            // Essayer d'autres formats communs
            try {
                LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (e2: DateTimeParseException) {
                try {
                    LocalDate.parse(dateString, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                } catch (e3: DateTimeParseException) {
                    // Si aucun format ne fonctionne, utiliser la date d'aujourd'hui
                    LocalDate.now()
                }
            }
        }
        
        // Essayer de parser l'heure
        val time = try {
            LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: DateTimeParseException) {
            try {
                LocalTime.parse(timeString, DateTimeFormatter.ISO_TIME)
            } catch (e2: DateTimeParseException) {
                // Si aucun format ne fonctionne, utiliser l'heure actuelle
                LocalTime.now()
            }
        }
        
        // Combiner date et heure, puis convertir en ISO 8601 avec timezone UTC
        val dateTime = LocalDateTime.of(date, time)
        dateTime.atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneId.of("UTC"))
            .format(DateTimeFormatter.ISO_INSTANT)
    } catch (e: Exception) {
        // En cas d'erreur, retourner un format ISO 8601 avec la date/heure actuelle
        LocalDateTime.now()
            .atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneId.of("UTC"))
            .format(DateTimeFormatter.ISO_INSTANT)
    }
}

/**
 * Formate la date pour l'API (format "YYYY-MM-DD")
 */
private fun formatDateForApi(dateString: String): String {
    return try {
        val date = try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
        } catch (e: DateTimeParseException) {
            try {
                LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            } catch (e2: DateTimeParseException) {
                try {
                    LocalDate.parse(dateString, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                } catch (e3: DateTimeParseException) {
                    LocalDate.now()
                }
            }
        }
        date.format(DateTimeFormatter.ISO_DATE)
    } catch (e: Exception) {
        LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    }
}

fun CreateActivityForm.toDto(
    latitude: Double? = null,
    longitude: Double? = null
): CreateActivityRequestDto = CreateActivityRequestDto(
    sportType = sportType?.name ?: "", // L'API attend le nom du sport (Football, Basketball, etc.)
    title = title,
    description = description.takeIf { it.isNotBlank() }, // Optionnel si vide
    location = location,
    latitude = latitude,
    longitude = longitude,
    date = formatDateForApi(date), // Format: "YYYY-MM-DD"
    time = formatDateTimeForApi(date, time), // Format ISO 8601 complet: "YYYY-MM-DDTHH:mm:ssZ"
    participants = participants,
    level = (level ?: SkillLevel.BEGINNER).name.lowercase().replaceFirstChar { it.uppercase() }, // "Beginner", "Intermediate", "Advanced"
    visibility = visibility.name.lowercase() // "public" ou "friends"
)

fun CreateActivityResponseDto.toDomain(): CreateActivityResult = CreateActivityResult(
    activityId = activityId,
    shareLink = shareLink ?: "" // Valeur par défaut si null
)

