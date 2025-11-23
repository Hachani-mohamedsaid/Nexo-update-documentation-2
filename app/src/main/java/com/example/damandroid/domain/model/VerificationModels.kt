package com.example.damandroid.domain.model

data class VerificationFormOptions(
    val userTypes: List<String>,
    val countries: List<String>,
    val documentsRequired: List<String>
)

data class VerificationApplication(
    val userType: String,
    val fullName: String,
    val email: String,
    val country: String,
    val documents: List<String>,
    val note: String
)

data class VerificationSubmissionResult(
    val applicationId: String,
    val status: VerificationStatus,
    val message: String
)

enum class VerificationStatus {
    NONE, PENDING, APPROVED, REJECTED
}

