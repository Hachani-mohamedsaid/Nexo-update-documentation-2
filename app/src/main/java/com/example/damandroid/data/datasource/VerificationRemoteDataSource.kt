package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.VerificationApplicationDto
import com.example.damandroid.data.model.VerificationFormOptionsDto
import com.example.damandroid.data.model.VerificationSubmissionResultDto

interface VerificationRemoteDataSource {
    suspend fun fetchFormOptions(): VerificationFormOptionsDto
    suspend fun submitApplication(application: VerificationApplicationDto): VerificationSubmissionResultDto
}

