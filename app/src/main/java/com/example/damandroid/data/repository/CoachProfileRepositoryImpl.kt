package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.CoachProfileRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.CoachProfile
import com.example.damandroid.domain.repository.CoachProfileRepository

class CoachProfileRepositoryImpl(
    private val remoteDataSource: CoachProfileRemoteDataSource
) : CoachProfileRepository {
    override suspend fun getCoachProfile(coachId: String): CoachProfile =
        remoteDataSource.fetchCoachProfile(coachId).toDomain()
}
