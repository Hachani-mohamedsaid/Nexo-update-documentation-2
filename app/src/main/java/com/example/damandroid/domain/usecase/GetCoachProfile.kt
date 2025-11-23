package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.CoachProfile
import com.example.damandroid.domain.repository.CoachProfileRepository

class GetCoachProfile(
    private val repository: CoachProfileRepository
) {
    suspend operator fun invoke(coachId: String): CoachProfile =
        repository.getCoachProfile(coachId)
}

