package com.example.damandroid.api

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Désérialiseur personnalisé pour ChatListItem
 * Extrait les avatars des participants depuis le champ chat.participants
 */
class ChatListItemDeserializer : JsonDeserializer<ChatListItem> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ChatListItem {
        if (json == null || !json.isJsonObject) {
            throw IllegalArgumentException("ChatListItem must be a JSON object")
        }
        
        val jsonObject = json.asJsonObject
        
        // Extraire les champs de base
        val id = jsonObject.get("id")?.asString ?: ""
        val participantNames = jsonObject.get("participantNames")?.asString ?: ""
        val lastMessage = jsonObject.get("lastMessage")?.asString ?: ""
        val lastMessageTime = jsonObject.get("lastMessageTime")?.asString ?: ""
        val unreadCount = jsonObject.get("unreadCount")?.asInt ?: 0
        val isGroup = jsonObject.get("isGroup")?.asBoolean ?: false
        
        // Extraire les avatars des participants depuis chat.participants
        val participantAvatars = mutableListOf<String>()
        val chatElement = jsonObject.get("chat")
        
        if (chatElement != null && chatElement.isJsonObject) {
            val chatObject = chatElement.asJsonObject
            val participantsElement = chatObject.get("participants")
            
            if (participantsElement != null && participantsElement.isJsonArray) {
                val participantsArray = participantsElement.asJsonArray
                for (participantElement in participantsArray) {
                    if (participantElement.isJsonObject) {
                        val participantObj = participantElement.asJsonObject
                        // Extraire profileImageUrl ou avatar
                        val profileImageUrl = when {
                            participantObj.has("profileImageUrl") && !participantObj.get("profileImageUrl").isJsonNull -> {
                                participantObj.get("profileImageUrl").asString
                            }
                            else -> null
                        }
                        val avatar = when {
                            participantObj.has("avatar") && !participantObj.get("avatar").isJsonNull -> {
                                participantObj.get("avatar").asString
                            }
                            else -> null
                        }
                        
                        // Ajouter l'avatar s'il existe
                        val avatarUrl = profileImageUrl ?: avatar
                        if (!avatarUrl.isNullOrEmpty()) {
                            participantAvatars.add(avatarUrl)
                        }
                    }
                }
            }
        }
        
        // Si participantAvatars n'est pas fourni dans chat.participants, essayer directement
        if (participantAvatars.isEmpty()) {
            val directAvatars = jsonObject.get("participantAvatars")
            if (directAvatars != null && directAvatars.isJsonArray) {
                directAvatars.asJsonArray.forEach { element ->
                    if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
                        val avatar = element.asString
                        if (avatar.isNotEmpty()) {
                            participantAvatars.add(avatar)
                        }
                    }
                }
            }
        }
        
        return ChatListItem(
            id = id,
            participantNames = participantNames,
            participantAvatars = participantAvatars,
            lastMessage = lastMessage,
            lastMessageTime = lastMessageTime,
            unreadCount = unreadCount,
            isGroup = isGroup
        )
    }
}

