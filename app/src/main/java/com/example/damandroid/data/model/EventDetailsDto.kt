package com.example.damandroid.data.model

data class EventDetailsDto(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val date: String,
    val time: String,
    val price: String,
    val host: EventHostDto,
    val highlights: List<String>,
    val agenda: List<EventAgendaItemDto>,
    val attendees: List<EventAttendeeDto>,
    val faqs: List<EventFaqDto>
)

data class EventHostDto(
    val name: String,
    val avatarUrl: String,
    val rating: Double,
    val reviewsCount: Int
)

data class EventAgendaItemDto(
    val label: String,
    val description: String
)

data class EventAttendeeDto(
    val id: String,
    val name: String,
    val avatarUrl: String
)

data class EventFaqDto(
    val question: String,
    val answer: String
)

