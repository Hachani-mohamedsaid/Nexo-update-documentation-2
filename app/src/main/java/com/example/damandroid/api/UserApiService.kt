package com.example.damandroid.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApiService {
    @GET("users/profile")
    suspend fun getProfile(
        @Header("Authorization") bearerToken: String
    ): Response<UserResponseDto>

    @GET("users/{id}/profile")
    suspend fun getUserProfileById(
        @Header("Authorization") bearerToken: String,
        @Path("id") userId: String
    ): Response<UserResponseDto>

    @PATCH("users/{id}")
    suspend fun updateProfile(
        @Header("Authorization") bearerToken: String,
        @Path("id") userId: String,
        @Body body: UpdateProfileRequestDto
    ): Response<UserResponseDto>

    @Multipart
    @PATCH("users/{id}/profile-image")
    suspend fun uploadProfileImage(
        @Header("Authorization") bearerToken: String,
        @Path("id") userId: String,
        @Part image: MultipartBody.Part
    ): Response<UserResponseDto>

    @PATCH("users/{id}/change-password")
    suspend fun changePassword(
        @Header("Authorization") bearerToken: String,
        @Path("id") userId: String,
        @Body body: ChangePasswordRequestDto
    ): Response<MessageResponse>
}

data class UpdateProfileRequestDto(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val location: String? = null,
    val about: String? = null,
    val sportsInterests: List<String>? = null,
    val profileImageUrl: String? = null
)

data class UserResponseDto(
    val id: String? = null,
    val _id: String? = null,
    val email: String? = null,
    val name: String? = null,
    val location: String? = null,
    val isEmailVerified: Boolean? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val about: String? = null,
    val sportsInterests: List<String>? = null,
    val profileImageUrl: String? = null,
    val profileImageThumbnailUrl: String? = null,
    val profileImageDeleteUrl: String? = null,
    // Statistics from backend
    val sessionsJoined: Int? = null,
    val sessionsHosted: Int? = null,
    val followers: Int? = null,
    val following: Int? = null,
    val rating: Double? = null
)

data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)
