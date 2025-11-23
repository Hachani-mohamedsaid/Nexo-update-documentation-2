package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.EventAgendaItemDto
import com.example.damandroid.data.model.EventAttendeeDto
import com.example.damandroid.data.model.EventDetailsDto
import com.example.damandroid.data.model.EventFaqDto
import com.example.damandroid.data.model.EventHostDto
import com.example.damandroid.domain.model.EventAgendaItem
import com.example.damandroid.domain.model.EventAttendee
import com.example.damandroid.domain.model.EventDetails
import com.example.damandroid.domain.model.EventFaq
import com.example.damandroid.domain.model.EventHost

fun EventDetailsDto.toDomain(): EventDetails = EventDetails(
    id = id,
    title = title,
    description = description,
    location = location,
    date = date,
    time = time,
    price = price,
    host = host.toDomain(),
    highlights = highlights,
    agenda = agenda.map(EventAgendaItemDto::toDomain),
    attendees = attendees.map(EventAttendeeDto::toDomain),
    faqs = faqs.map(EventFaqDto::toDomain)
)

private fun EventHostDto.toDomain(): EventHost = EventHost(
    name = name,
    avatarUrl = avatarUrl,
    rating = rating,
    reviewsCount = reviewsCount
)

private fun EventAgendaItemDto.toDomain(): EventAgendaItem = EventAgendaItem(
    label = label,
    description = description
)

private fun EventAttendeeDto.toDomain(): EventAttendee = EventAttendee(
    id = id,
    name = name,
    avatarUrl = avatarUrl
)

private fun EventFaqDto.toDomain(): EventFaq = EventFaq(
    question = question,
    answer = answer
)

