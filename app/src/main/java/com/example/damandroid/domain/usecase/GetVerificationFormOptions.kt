package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.VerificationFormOptions
import com.example.damandroid.domain.repository.VerificationRepository

class GetVerificationFormOptions(
    private val repository: VerificationRepository
) {
    suspend operator fun invoke(): VerificationFormOptions = repository.getFormOptions()
}

