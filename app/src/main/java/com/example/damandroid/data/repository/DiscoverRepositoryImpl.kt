package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.DiscoverRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.DiscoverOverview
import com.example.damandroid.domain.repository.DiscoverRepository

class DiscoverRepositoryImpl(
    private val remoteDataSource: DiscoverRemoteDataSource
) : DiscoverRepository {
    override suspend fun getOverview(): DiscoverOverview =
        remoteDataSource.fetchOverview().toDomain()
}

