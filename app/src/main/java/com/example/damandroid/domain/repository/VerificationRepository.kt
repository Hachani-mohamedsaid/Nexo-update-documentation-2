package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.VerificationApplication
import com.example.damandroid.domain.model.VerificationFormOptions
import com.example.damandroid.domain.model.VerificationSubmissionResult

interface VerificationRepository {
    suspend fun getFormOptions(): VerificationFormOptions
    suspend fun submitApplication(application: VerificationApplication): VerificationSubmissionResult
}

