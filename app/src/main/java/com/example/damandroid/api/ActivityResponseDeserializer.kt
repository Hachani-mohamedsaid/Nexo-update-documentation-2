package com.example.damandroid.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Deserializer personnalisé pour ActivityResponse
 * Gère le cas où creator peut être un objet ou une chaîne (ID)
 */
class ActivityResponseDeserializer : JsonDeserializer<ActivityResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ActivityResponse {
        if (json == null || !json.isJsonObject) {
            throw IllegalArgumentException("Expected JSON object")
        }
        
        val jsonObject = json.asJsonObject
        
        // Gérer creator qui peut être un objet ou une chaîne
        val creatorElement = jsonObject.get("creator")
        val creator: Any? = when {
            creatorElement == null -> null
            creatorElement.isJsonObject -> {
                // Si c'est un objet, le convertir en Map
                val creatorObj = creatorElement.asJsonObject
                mapOf(
                    "_id" to (creatorObj.get("_id")?.asString ?: creatorObj.get("id")?.asString ?: ""),
                    "id" to creatorObj.get("id")?.asString,
                    "name" to (creatorObj.get("name")?.asString ?: ""),
                    "email" to creatorObj.get("email")?.asString,
                    "profileImageUrl" to creatorObj.get("profileImageUrl")?.asString
                )
            }
            creatorElement.isJsonPrimitive && creatorElement.asJsonPrimitive.isString -> {
                // Si c'est une chaîne, retourner juste la chaîne
                creatorElement.asString
            }
            else -> null
        }
        
        // Extraire participantIds (liste des IDs des participants réels)
        val participantIdsElement = jsonObject.get("participantIds")
        val participantIds: List<String>? = when {
            participantIdsElement == null -> null
            participantIdsElement.isJsonArray -> {
                participantIdsElement.asJsonArray.mapNotNull { it.asString }
            }
            else -> null
        }
        
        return ActivityResponse(
            _id = jsonObject.get("_id")?.asString ?: jsonObject.get("id")?.asString ?: "",
            id = jsonObject.get("id")?.asString,
            creator = creator,
            sportType = jsonObject.get("sportType")?.asString ?: "",
            title = jsonObject.get("title")?.asString ?: "",
            description = jsonObject.get("description")?.asString,
            location = jsonObject.get("location")?.asString ?: "",
            latitude = jsonObject.get("latitude")?.asDouble,
            longitude = jsonObject.get("longitude")?.asDouble,
            date = jsonObject.get("date")?.asString ?: "",
            time = jsonObject.get("time")?.asString ?: "",
            participants = jsonObject.get("participants")?.asInt ?: 0,
            participantIds = participantIds,
            level = jsonObject.get("level")?.asString ?: "",
            visibility = jsonObject.get("visibility")?.asString ?: "",
            createdAt = jsonObject.get("createdAt")?.asString,
            updatedAt = jsonObject.get("updatedAt")?.asString
        )
    }
}

