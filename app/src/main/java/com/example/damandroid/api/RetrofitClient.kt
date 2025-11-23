package com.example.damandroid.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // URL de l'API Railway
    private const val BASE_URL = "https://apinest-production.up.railway.app/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val responseBodyInterceptor = ResponseBodyInterceptor()
    private val authInterceptor = AuthInterceptor()
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // Ajouter le token JWT automatiquement
        .addInterceptor(responseBodyInterceptor) // Add before logging to capture body
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(ActivityResponse::class.java, ActivityResponseDeserializer())
        .registerTypeAdapter(Chat::class.java, ChatDeserializer())
        .registerTypeAdapter(ChatMessage::class.java, ChatMessageDeserializer())
        .registerTypeAdapter(ChatListItem::class.java, ChatListItemDeserializer())
        .create()
    
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val userApiService: UserApiService = retrofit.create(UserApiService::class.java)
    val chatApiService: ChatApiService = retrofit.create(ChatApiService::class.java)
    val activityApiService: ActivityApiService = retrofit.create(ActivityApiService::class.java)
    val quickMatchApiService: QuickMatchApiService = retrofit.create(QuickMatchApiService::class.java)
    val aiMatchmakerApiService: AIMatchmakerApiService = retrofit.create(AIMatchmakerApiService::class.java)
    val achievementsApiService: AchievementsApiService = retrofit.create(AchievementsApiService::class.java)
    
    // OpenWeather API client
    private val openWeatherRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val openWeatherService: OpenWeatherService = openWeatherRetrofit.create(OpenWeatherService::class.java)
}

