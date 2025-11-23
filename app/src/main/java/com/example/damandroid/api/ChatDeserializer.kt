package com.example.damandroid.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Custom deserializer pour Chat qui gère les participants
 * qui peuvent être soit des strings (IDs) soit des objets
 */
class ChatDeserializer : JsonDeserializer<Chat> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Chat {
        if (json == null || !json.isJsonObject) {
            throw IllegalArgumentException("Expected JSON object for Chat")
        }
        
        val jsonObject = json.asJsonObject
        
        // Extraire l'ID (peut être _id ou id)
        val id = jsonObject.get("_id")?.asString 
            ?: jsonObject.get("id")?.asString 
            ?: throw IllegalArgumentException("Chat must have an id or _id field")
        
        // Gérer les participants qui peuvent être des strings ou des objets
        val participants = mutableListOf<ChatParticipant>()
        val participantsElement = jsonObject.get("participants")
        
        if (participantsElement != null && participantsElement.isJsonArray) {
            val participantsArray = participantsElement.asJsonArray
            for (element in participantsArray) {
                if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
                    // C'est un string (ID)
                    val participantId = element.asString
                    participants.add(
                        ChatParticipant(
                            id = participantId,
                            name = null,
                            email = null,
                            profileImageUrl = null,
                            avatar = null
                        )
                    )
                } else if (element.isJsonObject) {
                    // C'est un objet
                    val participantObj = element.asJsonObject
                    val participantId = participantObj.get("_id")?.asString
                        ?: participantObj.get("id")?.asString
                        ?: continue
                    
                    participants.add(
                        ChatParticipant(
                            id = participantId,
                            name = participantObj.get("name")?.asString,
                            email = participantObj.get("email")?.asString,
                            profileImageUrl = participantObj.get("profileImageUrl")?.asString,
                            avatar = participantObj.get("avatar")?.asString
                        )
                    )
                }
            }
        }
        
        // Extraire activityId si présent
        val activityId = when {
            jsonObject.has("activityId") && !jsonObject.get("activityId").isJsonNull -> {
                val activityIdElement = jsonObject.get("activityId")
                when {
                    activityIdElement.isJsonPrimitive -> activityIdElement.asString
                    activityIdElement.isJsonObject -> {
                        activityIdElement.asJsonObject.get("_id")?.asString
                            ?: activityIdElement.asJsonObject.get("id")?.asString
                    }
                    else -> null
                }
            }
            else -> null
        }
        
        return Chat(
            id = id,
            participants = if (participants.isEmpty()) null else participants,
            groupName = jsonObject.get("groupName")?.asString,
            groupAvatar = jsonObject.get("groupAvatar")?.asString,
            messages = null, // Les messages sont généralement récupérés séparément
            createdAt = jsonObject.get("createdAt")?.asString,
            updatedAt = jsonObject.get("updatedAt")?.asString,
            activityId = activityId
        )
    }
}

