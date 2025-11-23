package com.example.damandroid.data.datasource

import android.util.Log
import com.example.damandroid.api.ActivityApiService
import com.example.damandroid.data.model.CreateActivityCategoryDto
import com.example.damandroid.data.model.CreateActivityRequestDto
import com.example.damandroid.data.model.CreateActivityResponseDto
import com.example.damandroid.data.model.SportCategoryDto
import com.example.damandroid.data.model.toApiRequest
import com.example.damandroid.data.model.toCreateActivityResponseDto
import retrofit2.HttpException
import java.io.IOException

class CreateActivityRemoteDataSourceImpl(
    private val activityApiService: ActivityApiService
) : CreateActivityRemoteDataSource {
    
    override suspend fun fetchSportCategories(): List<SportCategoryDto> {
        // Les catégories de sport sont hardcodées selon l'API
        // L'API accepte: "Football", "Basketball", "Running", "Cycling"
        return listOf(
            SportCategoryDto(id = "Football", name = "Football", icon = "⚽"),
            SportCategoryDto(id = "Basketball", name = "Basketball", icon = "🏀"),
            SportCategoryDto(id = "Running", name = "Running", icon = "🏃"),
            SportCategoryDto(id = "Cycling", name = "Cycling", icon = "🚴")
        )
    }

    override suspend fun createActivity(request: CreateActivityRequestDto): CreateActivityResponseDto {
        return try {
            val apiRequest = request.toApiRequest()
            val response = activityApiService.createActivity(apiRequest)
            
            if (response.isSuccessful) {
                val activityResponse = response.body()
                if (activityResponse != null) {
                    activityResponse.toCreateActivityResponseDto()
                } else {
                    Log.e("CreateActivityDataSource", "Response body is null")
                    throw Exception("Failed to create activity: Response body is null")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CreateActivityDataSource", "Error creating activity: ${response.code()} - $errorBody")
                throw Exception("Failed to create activity: ${response.message()}")
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("CreateActivityDataSource", "HttpException creating activity: ${e.code()} - $errorBody")
            when (e.code()) {
                400 -> throw Exception("Invalid data: ${errorBody ?: e.message()}")
                401 -> throw Exception("Unauthorized: Please login again")
                500 -> throw Exception("Server error: Please try again later")
                else -> throw Exception("Failed to create activity: ${e.message()}")
            }
        } catch (e: IOException) {
            Log.e("CreateActivityDataSource", "IOException creating activity: ${e.message}")
            throw Exception("Network error: ${e.message}")
        } catch (e: Exception) {
            Log.e("CreateActivityDataSource", "Exception creating activity: ${e.message}", e)
            throw e
        }
    }
}

