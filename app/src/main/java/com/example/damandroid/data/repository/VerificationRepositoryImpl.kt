package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.VerificationRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.data.mapper.toDto
import com.example.damandroid.domain.model.VerificationApplication
import com.example.damandroid.domain.model.VerificationFormOptions
import com.example.damandroid.domain.model.VerificationSubmissionResult
import com.example.damandroid.domain.repository.VerificationRepository

class VerificationRepositoryImpl(
    private val remoteDataSource: VerificationRemoteDataSource
) : VerificationRepository {
    override suspend fun getFormOptions(): VerificationFormOptions =
        remoteDataSource.fetchFormOptions().toDomain()

    override suspend fun submitApplication(application: VerificationApplication): VerificationSubmissionResult =
        remoteDataSource.submitApplication(application.toDto()).toDomain()
}

