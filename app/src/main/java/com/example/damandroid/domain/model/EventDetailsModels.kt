package com.example.damandroid.domain.model

data class EventDetails(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val date: String,
    val time: String,
    val price: String,
    val host: EventHost,
    val highlights: List<String>,
    val agenda: List<EventAgendaItem>,
    val attendees: List<EventAttendee>,
    val faqs: List<EventFaq>
)

data class EventHost(
    val name: String,
    val avatarUrl: String,
    val rating: Double,
    val reviewsCount: Int
)

data class EventAgendaItem(
    val label: String,
    val description: String
)

data class EventAttendee(
    val id: String,
    val name: String,
    val avatarUrl: String
)

data class EventFaq(
    val question: String,
    val answer: String
)

