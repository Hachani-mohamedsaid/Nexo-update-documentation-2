package com.example.damandroid.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Service API pour Strava
 * Documentation: https://developers.strava.com/docs/reference/
 */
interface StravaApiService {
    
    /**
     * Récupérer les activités de l'athlète
     * @param accessToken Token d'accès OAuth
     * @param before Timestamp Unix (optionnel) - activités avant cette date
     * @param after Timestamp Unix (optionnel) - activités après cette date
     * @param page Numéro de page (défaut: 1)
     * @param perPage Nombre d'activités par page (défaut: 30, max: 200)
     */
    @GET("athlete/activities")
    suspend fun getAthleteActivities(
        @Header("Authorization") accessToken: String,
        @Query("before") before: Long? = null,
        @Query("after") after: Long? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 200
    ): Response<List<StravaActivityDto>>
    
    /**
     * Récupérer les statistiques de l'athlète
     */
    @GET("athletes/{id}/stats")
    suspend fun getAthleteStats(
        @Header("Authorization") accessToken: String,
        @Path("id") athleteId: Long
    ): Response<StravaStatsDto>
}

/**
 * DTO pour une activité Strava
 */
data class StravaActivityDto(
    val id: Long,
    val name: String,
    val type: String, // Run, Ride, Swim, etc.
    val distance: Float, // en mètres
    val moving_time: Int, // en secondes
    val elapsed_time: Int, // en secondes
    val total_elevation_gain: Float, // en mètres
    val calories: Float?,
    val start_date: String, // ISO 8601
    val start_date_local: String,
    val timezone: String,
    val achievement_count: Int,
    val kudos_count: Int,
    val comment_count: Int,
    val athlete_count: Int,
    val photo_count: Int
)

/**
 * DTO pour les statistiques de l'athlète
 */
data class StravaStatsDto(
    val biggest_ride_distance: Float?,
    val biggest_climb_elevation_gain: Float?,
    val recent_ride_totals: StravaTotalsDto?,
    val recent_run_totals: StravaTotalsDto?,
    val recent_swim_totals: StravaTotalsDto?,
    val ytd_ride_totals: StravaTotalsDto?,
    val ytd_run_totals: StravaTotalsDto?,
    val ytd_swim_totals: StravaTotalsDto?,
    val all_ride_totals: StravaTotalsDto?,
    val all_run_totals: StravaTotalsDto?,
    val all_swim_totals: StravaTotalsDto?
)

data class StravaTotalsDto(
    val count: Int,
    val distance: Float, // en mètres
    val moving_time: Int, // en secondes
    val elapsed_time: Int, // en secondes
    val elevation_gain: Float // en mètres
)


