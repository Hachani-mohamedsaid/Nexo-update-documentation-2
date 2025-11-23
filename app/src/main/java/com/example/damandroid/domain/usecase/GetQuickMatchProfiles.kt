package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.MatchUserProfile
import com.example.damandroid.domain.repository.QuickMatchRepository

class GetQuickMatchProfiles(
    private val repository: QuickMatchRepository
) {
    suspend operator fun invoke(): List<MatchUserProfile> = repository.getProfiles()
}

