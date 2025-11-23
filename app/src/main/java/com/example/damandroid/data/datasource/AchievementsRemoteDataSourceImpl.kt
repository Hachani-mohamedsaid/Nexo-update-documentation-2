package com.example.damandroid.data.datasource

import com.example.damandroid.api.AchievementsApiService
import com.example.damandroid.api.RetrofitClient
import com.example.damandroid.data.mapper.toDataBadges
import com.example.damandroid.data.mapper.toDataChallenges
import com.example.damandroid.data.mapper.toDataLeaderboardResponse
import com.example.damandroid.data.mapper.toDataOverview
import com.example.damandroid.data.model.AchievementBadgeDto
import com.example.damandroid.data.model.AchievementChallengeDto
import com.example.damandroid.data.model.AchievementUserStatsDto
import com.example.damandroid.data.model.AchievementsOverviewDto
import com.example.damandroid.data.model.LeaderboardResponseDto
import com.example.damandroid.data.model.AchievementNotificationsResponseDto
import com.example.damandroid.data.mapper.toDataNotificationsResponse
import com.example.damandroid.util.ErrorHandler
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class AchievementsRemoteDataSourceImpl(
    private val apiService: AchievementsApiService = RetrofitClient.achievementsApiService
) : AchievementsRemoteDataSource {
    
    override suspend fun fetchOverview(): AchievementsOverviewDto {
        // Pour la rétrocompatibilité, on combine toutes les données
        val summary = fetchSummary()
        val badges = fetchBadges()
        val challenges = fetchChallenges()
        val leaderboardResponse = fetchLeaderboard()
        
        // Extraire la liste des entrées du leaderboard pour la compatibilité
        val leaderboardEntries = mutableListOf<com.example.damandroid.data.model.LeaderboardEntryDto>()
        leaderboardResponse.currentUser?.let { user ->
            leaderboardEntries.add(
                com.example.damandroid.data.model.LeaderboardEntryDto(
                    rank = user.rank,
                    name = "You",
                    points = user.totalXp,
                    badge = when (user.rank) {
                        1 -> "🥇"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> ""
                    }
                )
            )
        }
        leaderboardEntries.addAll(leaderboardResponse.leaderboard)
        
        return AchievementsOverviewDto(
            stats = summary.stats,
            badges = badges,
            challenges = challenges,
            leaderboard = leaderboardEntries.sortedBy { it.rank }
        )
    }
    
    override suspend fun fetchSummary(): AchievementsOverviewDto {
        return try {
            val response = apiService.getSummary()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDataOverview()
            } else {
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            throw Exception(ErrorHandler.handleHttpError(e.code()))
        } catch (e: SocketTimeoutException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: IOException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun fetchBadges(): List<AchievementBadgeDto> {
        return try {
            val response = apiService.getBadges()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDataBadges()
            } else {
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            throw Exception(ErrorHandler.handleHttpError(e.code()))
        } catch (e: SocketTimeoutException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: IOException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun fetchChallenges(): List<AchievementChallengeDto> {
        return try {
            val response = apiService.getChallenges()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDataChallenges()
            } else {
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            throw Exception(ErrorHandler.handleHttpError(e.code()))
        } catch (e: SocketTimeoutException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: IOException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun fetchLeaderboard(page: Int, limit: Int): LeaderboardResponseDto {
        return try {
            val response = apiService.getLeaderboard(page, limit)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDataLeaderboardResponse()
            } else {
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            throw Exception(ErrorHandler.handleHttpError(e.code()))
        } catch (e: SocketTimeoutException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: IOException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun fetchNotifications(page: Int, limit: Int, unreadOnly: Boolean): AchievementNotificationsResponseDto {
        return try {
            val response = apiService.getNotifications(page, limit, unreadOnly)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.toDataNotificationsResponse()
            } else {
                throw HttpException(response)
            }
        } catch (e: HttpException) {
            throw Exception(ErrorHandler.handleHttpError(e.code()))
        } catch (e: SocketTimeoutException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: IOException) {
            throw Exception(ErrorHandler.handleError(e))
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            val response = apiService.markNotificationAsRead(notificationId)
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handleError(e))
        }
    }
    
    override suspend fun markAllNotificationsAsRead(): Boolean {
        return try {
            val response = apiService.markAllNotificationsAsRead()
            response.isSuccessful && response.body()?.success == true
        } catch (e: Exception) {
            throw Exception(ErrorHandler.handleError(e))
        }
    }
}

