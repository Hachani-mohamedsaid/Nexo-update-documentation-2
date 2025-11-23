package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.ChatPreview
import com.example.damandroid.domain.repository.ChatRepository

class SearchChats(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(query: String): List<ChatPreview> =
        repository.searchChats(query)
}

