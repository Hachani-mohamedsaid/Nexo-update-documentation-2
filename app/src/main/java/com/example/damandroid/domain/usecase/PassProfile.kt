package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.repository.QuickMatchRepository

class PassProfile(
    private val repository: QuickMatchRepository
) {
    suspend operator fun invoke(profileId: String) = repository.passProfile(profileId)
}

