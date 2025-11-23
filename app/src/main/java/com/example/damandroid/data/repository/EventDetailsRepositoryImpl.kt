package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.EventDetailsRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.EventDetails
import com.example.damandroid.domain.repository.EventDetailsRepository

class EventDetailsRepositoryImpl(
    private val remoteDataSource: EventDetailsRemoteDataSource
) : EventDetailsRepository {
    override suspend fun getEventDetails(eventId: String): EventDetails =
        remoteDataSource.fetchEventDetails(eventId).toDomain()
}

