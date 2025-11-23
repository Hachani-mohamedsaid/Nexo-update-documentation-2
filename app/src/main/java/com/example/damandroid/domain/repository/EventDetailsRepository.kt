package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.EventDetails

interface EventDetailsRepository {
    suspend fun getEventDetails(eventId: String): EventDetails
}

