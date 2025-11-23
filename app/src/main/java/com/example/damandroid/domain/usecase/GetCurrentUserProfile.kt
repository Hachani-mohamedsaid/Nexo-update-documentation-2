package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.UserProfile
import com.example.damandroid.domain.repository.ProfileRepository

class GetCurrentUserProfile(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): UserProfile = repository.getCurrentUserProfile()
}

