package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.CreateActivityRequestDto
import com.example.damandroid.data.model.CreateActivityResponseDto
import com.example.damandroid.data.model.SportCategoryDto

interface CreateActivityRemoteDataSource {
    suspend fun fetchSportCategories(): List<SportCategoryDto>
    suspend fun createActivity(request: CreateActivityRequestDto): CreateActivityResponseDto
}

