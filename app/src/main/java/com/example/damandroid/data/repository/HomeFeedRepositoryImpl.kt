package com.example.damandroid.data.repository

import com.example.damandroid.data.datasource.HomeFeedRemoteDataSource
import com.example.damandroid.data.mapper.toDomain
import com.example.damandroid.domain.model.HomeFeed
import com.example.damandroid.domain.repository.HomeFeedRepository

class HomeFeedRepositoryImpl(
    private val remoteDataSource: HomeFeedRemoteDataSource
) : HomeFeedRepository {

    private val savedActivities = mutableSetOf<String>()
    private var cachedFeed: HomeFeed? = null

    override suspend fun getHomeFeed(): HomeFeed = fetchAndCacheFeed()

    override suspend fun getMyActivities(): HomeFeed {
        val dto = remoteDataSource.fetchMyActivities()
        // Persist any server-side saved state into local memory
        dto.activities.filter { it.isSaved }.forEach { savedActivities.add(it.id) }

        val mergedDto = dto.copy(
            activities = dto.activities.map { activity ->
                activity.copy(isSaved = savedActivities.contains(activity.id))
            }
        )
        return mergedDto.toDomain()
    }

    override suspend fun toggleSaved(activityId: String): HomeFeed {
        if (!savedActivities.add(activityId)) {
            savedActivities.remove(activityId)
        }
        return fetchAndCacheFeed()
    }

    private suspend fun fetchAndCacheFeed(): HomeFeed {
        val dto = remoteDataSource.fetchHomeFeed()
        // Persist any server-side saved state into local memory
        dto.activities.filter { it.isSaved }.forEach { savedActivities.add(it.id) }

        val mergedDto = dto.copy(
            activities = dto.activities.map { activity ->
                activity.copy(isSaved = savedActivities.contains(activity.id))
            }
        )
        return mergedDto.toDomain().also { cachedFeed = it }
    }
}

