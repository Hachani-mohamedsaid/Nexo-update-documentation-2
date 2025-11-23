package com.example.damandroid.domain.repository

import com.example.damandroid.domain.model.CreateActivityForm
import com.example.damandroid.domain.model.CreateActivityResult
import com.example.damandroid.domain.model.SportCategory

interface CreateActivityRepository {
    suspend fun getSportCategories(): List<SportCategory>
    suspend fun createActivity(form: CreateActivityForm): CreateActivityResult
}

