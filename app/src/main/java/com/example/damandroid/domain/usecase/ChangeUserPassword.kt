package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.PasswordChangeResult
import com.example.damandroid.domain.repository.ProfileRepository

class ChangeUserPassword(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, currentPassword: String, newPassword: String): PasswordChangeResult {
        return repository.changePassword(userId, currentPassword, newPassword)
    }
}
