package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.VerificationApplicationDto
import com.example.damandroid.data.model.VerificationFormOptionsDto
import com.example.damandroid.data.model.VerificationSubmissionResultDto
import com.example.damandroid.domain.model.VerificationApplication
import com.example.damandroid.domain.model.VerificationFormOptions
import com.example.damandroid.domain.model.VerificationStatus
import com.example.damandroid.domain.model.VerificationSubmissionResult

fun VerificationFormOptionsDto.toDomain(): VerificationFormOptions = VerificationFormOptions(
    userTypes = userTypes,
    countries = countries,
    documentsRequired = documentsRequired
)

fun VerificationApplication.toDto(): VerificationApplicationDto = VerificationApplicationDto(
    userType = userType,
    fullName = fullName,
    email = email,
    country = country,
    documents = documents,
    note = note
)

fun VerificationSubmissionResultDto.toDomain(): VerificationSubmissionResult = VerificationSubmissionResult(
    applicationId = applicationId,
    status = runCatching { VerificationStatus.valueOf(status.uppercase()) }
        .getOrDefault(VerificationStatus.PENDING),
    message = message
)

