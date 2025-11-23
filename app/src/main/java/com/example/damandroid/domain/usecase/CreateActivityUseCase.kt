package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.CreateActivityForm
import com.example.damandroid.domain.model.CreateActivityResult
import com.example.damandroid.domain.repository.CreateActivityRepository

class CreateActivityUseCase(
    private val repository: CreateActivityRepository
) {
    suspend operator fun invoke(form: CreateActivityForm): CreateActivityResult =
        repository.createActivity(form)
}

