package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.EventDetailsDto

interface EventDetailsRemoteDataSource {
    suspend fun fetchEventDetails(eventId: String): EventDetailsDto
}

