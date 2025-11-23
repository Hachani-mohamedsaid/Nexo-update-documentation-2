package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.UserProfileDto
import com.example.damandroid.api.UpdateProfileRequestDto
import com.example.damandroid.api.ChangePasswordRequestDto
import com.example.damandroid.api.MessageResponse

interface ProfileRemoteDataSource {
    suspend fun fetchCurrentUser(): UserProfileDto
    suspend fun getUserProfileById(userId: String): UserProfileDto
    suspend fun updateProfile(userId: String, body: UpdateProfileRequestDto): UserProfileDto
    suspend fun uploadProfileImage(userId: String, fileName: String, mimeType: String, bytes: ByteArray): UserProfileDto
    suspend fun changePassword(userId: String, request: ChangePasswordRequestDto): MessageResponse
}

