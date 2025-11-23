package com.example.damandroid.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Désérialiseur personnalisé pour ChatMessage
 * Extrait le nom et l'avatar depuis l'objet sender si présent
 */
class ChatMessageDeserializer : JsonDeserializer<ChatMessage> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ChatMessage {
        if (json == null || !json.isJsonObject) {
            throw IllegalArgumentException("ChatMessage must be a JSON object")
        }
        
        val jsonObject = json.asJsonObject
        
        // Extraire les champs de base
        val id = jsonObject.get("id")?.asString ?: jsonObject.get("_id")?.asString ?: ""
        val text = jsonObject.get("text")?.asString ?: ""
        val createdAt = jsonObject.get("createdAt")?.asString ?: ""
        
        // Extraire le sender (peut être une string ou un objet)
        val senderElement = jsonObject.get("sender")
        var senderString: String? = null
        var senderObj: MessageSender? = null
        
        when {
            senderElement != null && senderElement.isJsonPrimitive && !senderElement.isJsonNull -> {
                // Sender est une string
                senderString = senderElement.asString
            }
            senderElement != null && senderElement.isJsonObject -> {
                // Sender est un objet - extraire les informations
                val senderJson = senderElement.asJsonObject
                senderObj = MessageSender(
                    id = if (senderJson.has("id") && !senderJson.get("id").isJsonNull) {
                        senderJson.get("id").asString
                    } else if (senderJson.has("_id") && !senderJson.get("_id").isJsonNull) {
                        senderJson.get("_id").asString
                    } else {
                        null
                    },
                    _id = if (senderJson.has("_id") && !senderJson.get("_id").isJsonNull) {
                        senderJson.get("_id").asString
                    } else {
                        null
                    },
                    name = if (senderJson.has("name") && !senderJson.get("name").isJsonNull) {
                        senderJson.get("name").asString
                    } else {
                        null
                    },
                    email = if (senderJson.has("email") && !senderJson.get("email").isJsonNull) {
                        senderJson.get("email").asString
                    } else {
                        null
                    },
                    profileImageUrl = if (senderJson.has("profileImageUrl") && !senderJson.get("profileImageUrl").isJsonNull) {
                        senderJson.get("profileImageUrl").asString
                    } else {
                        null
                    },
                    avatar = if (senderJson.has("avatar") && !senderJson.get("avatar").isJsonNull) {
                        senderJson.get("avatar").asString
                    } else {
                        null
                    }
                )
                // Utiliser l'ID comme sender string
                senderString = senderObj.extractId()
            }
        }
        
        // Extraire senderName et avatar directement (si fournis)
        // Gérer les valeurs null correctement
        val senderName = when {
            jsonObject.has("senderName") && !jsonObject.get("senderName").isJsonNull -> {
                jsonObject.get("senderName").asString
            }
            else -> null
        }
        val avatar = when {
            jsonObject.has("avatar") && !jsonObject.get("avatar").isJsonNull -> {
                jsonObject.get("avatar").asString
            }
            else -> null
        }
        
        // Extraire time (peut être calculé depuis createdAt si non fourni)
        val time = if (jsonObject.has("time") && !jsonObject.get("time").isJsonNull) {
            jsonObject.get("time").asString
        } else {
            null
        }
        
        return ChatMessage(
            id = id,
            text = text,
            sender = senderString,
            time = time,
            senderName = senderName,
            avatar = avatar,
            createdAt = createdAt,
            senderObj = senderObj
        )
    }
}

