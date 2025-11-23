package com.example.damandroid.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body loginDto: LoginDto): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body registerDto: RegisterDto): Response<RegisterResponse>
    
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body forgotPasswordDto: ForgotPasswordDto): Response<MessageResponse>

    @POST("auth/send-verification-email")
    suspend fun sendVerificationEmail(@Body verificationEmailDto: VerificationEmailDto): Response<MessageResponse>
    
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body resetPasswordDto: ResetPasswordDto): Response<MessageResponse>
    
    @POST("auth/google")
    suspend fun loginWithGoogle(@Body googleLoginDto: GoogleLoginDto): Response<AuthResponse>
    
    @POST("auth/facebook")
    suspend fun loginWithFacebook(@Body facebookLoginDto: FacebookLoginDto): Response<AuthResponse>
}

data class GoogleLoginDto(
    val email: String,
    val name: String,
    val idToken: String? = null,
    val photoUrl: String? = null
)

data class FacebookLoginDto(
    val email: String,
    val name: String,
    val userId: String? = null,
    val accessToken: String? = null,
    val photoUrl: String? = null
)

data class LoginDto(
    val email: String,
    val password: String,
    val rememberMe: Boolean? = null
)

data class RegisterDto(
    val email: String,
    val password: String,
    val name: String,
    val location: String
)

data class ForgotPasswordDto(
    val email: String
)

data class ResetPasswordDto(
    val token: String,
    val password: String
)

data class MessageResponse(
    val message: String
)

data class VerificationEmailDto(
    val email: String
)

data class AuthResponse(
    val access_token: String? = null,
    val token: String? = null, // Alternative field name
    val user: UserDto? = null,
    val message: String? = null
)

// Response from register endpoint (returns user object, no token)
data class RegisterResponse(
    val email: String,
    val name: String,
    val location: String,
    val isEmailVerified: Boolean? = null,
    val _id: String? = null,
    val id: String? = null, // Alternative field name
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
)

data class UserDto(
    val id: String,
    val email: String,
    val name: String,
    val location: String
)

data class ErrorResponse(
    val message: String? = null,
    val error: String? = null,
    val statusCode: Int? = null
)

