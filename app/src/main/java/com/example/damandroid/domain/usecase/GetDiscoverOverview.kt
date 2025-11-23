package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.model.DiscoverOverview
import com.example.damandroid.domain.repository.DiscoverRepository

class GetDiscoverOverview(
    private val repository: DiscoverRepository
) {
    suspend operator fun invoke(): DiscoverOverview = repository.getOverview()
}

