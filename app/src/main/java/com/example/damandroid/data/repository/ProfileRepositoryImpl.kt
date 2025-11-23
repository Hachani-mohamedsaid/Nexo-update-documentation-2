package com.example.damandroid.data.repository

import com.example.damandroid.api.ChangePasswordRequestDto
import com.example.damandroid.api.UpdateProfileRequestDto
import com.example.damandroid.api.UserDto
import com.example.damandroid.auth.UserSession
import com.example.damandroid.data.datasource.ProfileRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.ProfileImageUpload
import com.example.damandroid.domain.model.ProfileUpdate
import com.example.damandroid.domain.model.UserProfile
import com.example.damandroid.domain.repository.ProfileRepository
import com.example.damandroid.api.MessageResponse
import com.example.damandroid.domain.model.PasswordChangeResult

class ProfileRepositoryImpl(
    private val remoteDataSource: ProfileRemoteDataSource
) : ProfileRepository {
    override suspend fun getCurrentUserProfile(): UserProfile =
        remoteDataSource.fetchCurrentUser().toDomain()

    override suspend fun getUserProfileById(userId: String): UserProfile =
        remoteDataSource.getUserProfileById(userId).toDomain()

    override suspend fun updateProfile(userId: String, update: ProfileUpdate): UserProfile {
        val request = UpdateProfileRequestDto(
            name = update.name,
            email = update.email,
            phone = update.phone,
            dateOfBirth = update.dateOfBirth,
            location = update.location,
            about = update.about,
            sportsInterests = update.sportsInterests,
            profileImageUrl = update.profileImageUrl
        )

        val updatedProfile = remoteDataSource.updateProfile(userId, request).toDomain()

        val currentToken = UserSession.token
        val previousUser = UserSession.user

        val updatedUser = UserDto(
            id = userId,
            email = update.email ?: previousUser?.email ?: "",
            name = update.name ?: updatedProfile.name,
            location = update.location ?: updatedProfile.location
        )

        UserSession.update(currentToken, updatedUser)

        return updatedProfile
    }

    override suspend fun uploadProfileImage(userId: String, upload: ProfileImageUpload): UserProfile {
        val updatedProfile = remoteDataSource
            .uploadProfileImage(
                userId = userId,
                fileName = upload.fileName,
                mimeType = upload.mimeType,
                bytes = upload.bytes
            )
            .toDomain()

        val currentToken = UserSession.token
        val previousUser = UserSession.user

        val updatedUser = UserDto(
            id = userId,
            email = previousUser?.email ?: "",
            name = updatedProfile.name,
            location = updatedProfile.location
        )

        UserSession.update(currentToken, updatedUser)

        return updatedProfile
    }

    override suspend fun changePassword(userId: String, currentPassword: String, newPassword: String): PasswordChangeResult {
        val request = ChangePasswordRequestDto(
            currentPassword = currentPassword,
            newPassword = newPassword
        )

        val response: MessageResponse = remoteDataSource.changePassword(userId, request)
        return PasswordChangeResult(message = response.message)
    }
}

