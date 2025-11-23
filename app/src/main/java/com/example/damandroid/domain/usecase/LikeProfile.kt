package com.example.damandroid.domain.usecase

import com.example.damandroid.domain.repository.LikeResult
import com.example.damandroid.domain.repository.QuickMatchRepository

class LikeProfile(
    private val repository: QuickMatchRepository
) {
    suspend operator fun invoke(profileId: String): LikeResult = repository.likeProfile(profileId)
}

