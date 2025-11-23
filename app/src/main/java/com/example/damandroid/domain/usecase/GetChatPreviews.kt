package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.ChatPreview
import com.example.damandroid.domain.repository.ChatRepository

class GetChatPreviews(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): List<ChatPreview> = repository.getChats()
}

