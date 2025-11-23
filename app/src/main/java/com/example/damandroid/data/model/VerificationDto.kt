package com.example.damandroid.data.model

data class VerificationFormOptionsDto(
    val userTypes: List<String>,
    val countries: List<String>,
    val documentsRequired: List<String>
)

data class VerificationApplicationDto(
    val userType: String,
    val fullName: String,
    val email: String,
    val country: String,
    val documents: List<String>,
    val note: String
)

data class VerificationSubmissionResultDto(
    val applicationId: String,
    val status: String,
    val message: String
)

