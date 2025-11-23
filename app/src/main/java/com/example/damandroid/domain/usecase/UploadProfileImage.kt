package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.ProfileImageUpload
import com.example.damandroid.domain.model.UserProfile
import com.example.damandroid.domain.repository.ProfileRepository

class UploadProfileImage(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, upload: ProfileImageUpload): UserProfile {
        return repository.uploadProfileImage(userId, upload)
    }
}
