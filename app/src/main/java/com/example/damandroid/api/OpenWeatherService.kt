package com.example.damandroid.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherService {
    @GET("geo/1.0/direct")
    suspend fun searchCities(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String = "c9a2402841b01ace6e45f2b61194c78a"
    ): Response<List<CityLocation>>
}

data class CityLocation(
    val name: String,
    val local_names: Map<String, String>? = null,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
) {
    fun getDisplayName(): String {
        return if (state != null) {
            "$name, $state, $country"
        } else {
            "$name, $country"
        }
    }
}

