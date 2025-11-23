package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.VerificationApplicationDto
import com.example.damandroid.data.model.VerificationFormOptionsDto
import com.example.damandroid.data.model.VerificationSubmissionResultDto
import java.util.UUID

class VerificationRemoteDataSourceImpl : VerificationRemoteDataSource {
    override suspend fun fetchFormOptions(): VerificationFormOptionsDto = VerificationFormOptionsDto(
        userTypes = listOf("Athlete", "Coach", "Organization"),
        countries = listOf("United States", "Canada", "France", "Germany", "United Kingdom"),
        documentsRequired = listOf("Government ID", "Proof of Experience", "Certification")
    )

    override suspend fun submitApplication(application: VerificationApplicationDto): VerificationSubmissionResultDto =
        VerificationSubmissionResultDto(
            applicationId = UUID.randomUUID().toString(),
            status = "pending",
            message = "Your application has been submitted. We will review it within 48 hours."
        )
}

