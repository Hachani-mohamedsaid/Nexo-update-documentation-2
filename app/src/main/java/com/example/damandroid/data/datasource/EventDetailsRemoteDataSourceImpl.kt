package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.EventAgendaItemDto
import com.example.damandroid.data.model.EventAttendeeDto
import com.example.damandroid.data.model.EventDetailsDto
import com.example.damandroid.data.model.EventFaqDto
import com.example.damandroid.data.model.EventHostDto

class EventDetailsRemoteDataSourceImpl : EventDetailsRemoteDataSource {
    override suspend fun fetchEventDetails(eventId: String): EventDetailsDto = EventDetailsDto(
        id = eventId,
        title = "Sunset Beach Volleyball",
        description = "Join us for an exciting sunset volleyball session with local athletes.",
        location = "North Beach Court 3",
        date = "Sat, Nov 8",
        time = "5:30 PM",
        price = "\$12.00",
        host = EventHostDto(
            name = "Mia Johnson",
            avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Mia",
            rating = 4.9,
            reviewsCount = 86
        ),
        highlights = listOf(
            "Professional coach on site",
            "All equipment included",
            "Post-game refreshments"
        ),
        agenda = listOf(
            EventAgendaItemDto("Warm-up", "Dynamic stretching and partner drills"),
            EventAgendaItemDto("Skill drills", "Serving, setting, and spiking practice"),
            EventAgendaItemDto("Scrimmage", "Friendly matches with rotating teams"),
            EventAgendaItemDto("Cool down", "Guided cool-down and recovery tips")
        ),
        attendees = listOf(
            EventAttendeeDto("1", "Alex R.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Alex"),
            EventAttendeeDto("2", "Sophie L.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Sophie"),
            EventAttendeeDto("3", "Marcus D.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Marcus")
        ),
        faqs = listOf(
            EventFaqDto(
                question = "What should I bring?",
                answer = "Bring comfortable athletic wear. Balls and nets are provided."
            ),
            EventFaqDto(
                question = "What if I’m a beginner?",
                answer = "All levels are welcome! Coaches will tailor drills to skill levels."
            )
        )
    )
}

