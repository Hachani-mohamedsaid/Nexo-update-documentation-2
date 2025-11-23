package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.ProfileImageUpload
import com.example.damandroid.domain.model.ProfileUpdate
import com.example.damandroid.domain.model.UserProfile
import com.example.damandroid.domain.model.PasswordChangeResult

interface ProfileRepository {
    suspend fun getCurrentUserProfile(): UserProfile
    suspend fun getUserProfileById(userId: String): UserProfile
    suspend fun updateProfile(userId: String, update: ProfileUpdate): UserProfile
    suspend fun uploadProfileImage(userId: String, upload: ProfileImageUpload): UserProfile
    suspend fun changePassword(userId: String, currentPassword: String, newPassword: String): PasswordChangeResult
}

