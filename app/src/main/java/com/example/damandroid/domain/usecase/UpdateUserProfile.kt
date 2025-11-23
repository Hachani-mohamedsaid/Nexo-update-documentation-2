package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.ProfileUpdate
import com.example.damandroid.domain.model.UserProfile
import com.example.damandroid.domain.repository.ProfileRepository

class UpdateUserProfile(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, update: ProfileUpdate): UserProfile {
        return repository.updateProfile(userId, update)
    }
}
