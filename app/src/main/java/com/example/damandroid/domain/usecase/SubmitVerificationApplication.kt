package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.VerificationApplication
import com.example.damandroid.domain.model.VerificationSubmissionResult
import com.example.damandroid.domain.repository.VerificationRepository

class SubmitVerificationApplication(
    private val repository: VerificationRepository
) {
    suspend operator fun invoke(application: VerificationApplication): VerificationSubmissionResult =
        repository.submitApplication(application)
}

