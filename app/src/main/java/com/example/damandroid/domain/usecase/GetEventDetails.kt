package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.EventDetails
import com.example.damandroid.domain.repository.EventDetailsRepository

class GetEventDetails(
    private val repository: EventDetailsRepository
) {
    suspend operator fun invoke(eventId: String): EventDetails =
        repository.getEventDetails(eventId)
}

