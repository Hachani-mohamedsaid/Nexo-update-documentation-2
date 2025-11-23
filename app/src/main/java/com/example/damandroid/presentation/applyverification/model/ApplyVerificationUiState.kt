package com.example.damandroid.presentation.applyverification.model

import com.example.damandroid.domain.model.VerificationFormOptions
import com.example.damandroid.domain.model.VerificationStatus
import com.example.damandroid.domain.model.VerificationSubmissionResult

data class ApplyVerificationUiState(
    val isLoading: Boolean = false,
    val formOptions: VerificationFormOptions? = null,
    val selectedUserType: String = "",
    val fullName: String = "",
    val email: String = "",
    val about: String = "",
    val specialization: String = "",
    val yearsOfExperience: String = "",
    val certifications: String = "",
    val location: String = "",
    val website: String = "",
    val clubName: String = "",
    val sportFocus: String = "",
    val country: String = "",
    val documents: List<String> = emptyList(),
    val note: String = "",
    val status: VerificationStatus = VerificationStatus.NONE,
    val submissionResult: VerificationSubmissionResult? = null,
    val error: String? = null
)

