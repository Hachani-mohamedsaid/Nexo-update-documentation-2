package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.repository.CreateActivityRepository
import com.example.damandroid.domain.model.SportCategory

class GetSportCategories(
    private val repository: CreateActivityRepository
) {
    suspend operator fun invoke(): List<SportCategory> = repository.getSportCategories()
}

