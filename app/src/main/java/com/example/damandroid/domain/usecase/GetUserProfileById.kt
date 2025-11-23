package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.UserProfile
import com.example.damandroid.domain.repository.ProfileRepository

class GetUserProfileById(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String): UserProfile = repository.getUserProfileById(userId)
}

