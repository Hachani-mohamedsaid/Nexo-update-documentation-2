# 🎯 Guide : Modèle IA pour l'Onglet "Tips" - Vidéos YouTube et Conseils

## 📋 Vue d'Ensemble

Ce guide explique comment utiliser un **modèle IA** pour générer des suggestions personnalisées dans l'onglet **"Tips"** :
1. **Vidéos YouTube de workout** : Recherche intelligente de vidéos basée sur les données utilisateur
2. **Conseils/Tips personnalisés** : Génération de conseils adaptés au profil et aux données Google Fit

---

## 🎯 Architecture avec Modèle IA

```
┌─────────────────────────────────────────────────────────┐
│              Onglet "Tips"                              │
│  - Vidéos YouTube de workout                           │
│  - Conseils personnalisés                              │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│         AICoachViewModel                                │
│  - Collecte données utilisateur                         │
│  - Appelle le service IA                                │
└─────────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ App Data     │ │ Google Fit   │ │ YouTube API  │
│ (Backend)    │ │ Data         │ │ (Vidéos)     │
└──────────────┘ └──────────────┘ └──────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│         AI Service (OpenAI/Claude)                       │
│  - Analyse les données                                  │
│  - Génère des requêtes de recherche YouTube            │
│  - Génère des conseils personnalisés                    │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│         Suggestions "Tips"                               │
│  - Vidéos YouTube recommandées                         │
│  - Conseils par catégorie (Basics, Health, Training)   │
└─────────────────────────────────────────────────────────┘
```

---

## 1️⃣ Données à Collecter pour l'IA

### A. Données Utilisateur

```kotlin
data class TipsContext(
    // Profil utilisateur
    val sportsInterests: List<String>, // ["Running", "Basketball", "Swimming"]
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val favoriteSports: List<String>, // Sports les plus pratiqués
    
    // Données Google Fit
    val weeklyStats: WeeklyStats, // workouts, calories, minutes, streak
    val recentWorkouts: List<WorkoutSession>, // Activités récentes
    val activityTrends: ActivityTrends, // Tendances
    
    // Objectifs et défis
    val activeChallenges: List<Challenge>, // Challenges en cours
    val goals: UserGoals, // Objectifs de l'utilisateur
    val painPoints: List<String> // Points à améliorer
)
```

---

## 2️⃣ Service IA pour Générer les Tips

### AITipsService.kt

```kotlin
package com.example.damandroid.domain.usecase

import android.util.Log
import com.example.damandroid.data.datasource.YouTubeDataSource
import com.example.damandroid.domain.model.WorkoutTip
import com.example.damandroid.domain.model.WorkoutVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AITipsService(
    private val apiKey: String, // OpenAI API Key
    private val youtubeDataSource: YouTubeDataSource
) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    
    /**
     * Génère des suggestions de vidéos YouTube et de conseils personnalisés
     */
    suspend fun generatePersonalizedTips(
        context: TipsContext
    ): TipsRecommendations = withContext(Dispatchers.IO) {
        try {
            // 1. Générer les requêtes de recherche YouTube avec l'IA
            val youtubeQueries = generateYouTubeQueries(context)
            
            // 2. Rechercher les vidéos YouTube
            val workoutVideos = searchYouTubeVideos(youtubeQueries)
            
            // 3. Générer les conseils personnalisés avec l'IA
            val workoutTips = generateWorkoutTips(context)
            
            TipsRecommendations(
                videos = workoutVideos,
                tips = workoutTips
            )
        } catch (e: Exception) {
            Log.e("AITipsService", "Error generating tips", e)
            // Fallback : suggestions basiques
            generateFallbackTips(context)
        }
    }
    
    /**
     * Génère des requêtes de recherche YouTube intelligentes avec l'IA
     */
    private suspend fun generateYouTubeQueries(context: TipsContext): List<YouTubeQuery> {
        val prompt = buildYouTubeQueryPrompt(context)
        val response = callOpenAI(prompt)
        return parseYouTubeQueries(response)
    }
    
    /**
     * Construire le prompt pour générer les requêtes YouTube
     */
    private fun buildYouTubeQueryPrompt(context: TipsContext): String {
        return """
        Tu es un expert en recherche de vidéos de workout sur YouTube.
        
        ## Données de l'utilisateur :
        
        **Profil** :
        - Sports d'intérêt : ${context.sportsInterests.joinToString(", ")}
        - Niveau : ${context.level}
        - Sports favoris : ${context.favoriteSports.joinToString(", ")}
        
        **Activité récente (Google Fit)** :
        - Workouts cette semaine : ${context.weeklyStats.workouts}
        - Calories brûlées : ${context.weeklyStats.calories}
        - Minutes d'activité : ${context.weeklyStats.minutes}
        - Streak : ${context.weeklyStats.streak} jours
        
        **Activités récentes** :
        ${context.recentWorkouts.take(5).joinToString("\n") { 
            "- ${it.sportType} : ${it.duration} min (${it.date})"
        }}
        
        **Challenges actifs** :
        ${context.activeChallenges.take(3).joinToString("\n") { 
            "- ${it.title} : ${it.progress}/${it.total}"
        }}
        
        **Points à améliorer** :
        ${context.painPoints.joinToString(", ")}
        
        ## Tâche :
        
        Génère 8-10 requêtes de recherche YouTube personnalisées pour trouver des vidéos de workout pertinentes.
        
        Les requêtes doivent être :
        - Spécifiques au profil de l'utilisateur
        - Adaptées à son niveau (${context.level})
        - Basées sur ses sports d'intérêt
        - Utiles pour ses challenges en cours
        - Variées (tutoriels, routines, conseils, etc.)
        
        Réponds UNIQUEMENT en JSON :
        ```json
        {
          "queries": [
            {
              "query": "basketball workout beginner tutorial",
              "sportType": "Basketball",
              "category": "Tutorial",
              "reason": "L'utilisateur a fait du basketball récemment et est débutant"
            },
            {
              "query": "running form tips intermediate",
              "sportType": "Running",
              "category": "Tips",
              "reason": "L'utilisateur a un challenge de course et est intermédiaire"
            }
          ]
        }
        ```
        """.trimIndent()
    }
    
    /**
     * Génère des conseils personnalisés avec l'IA
     */
    private suspend fun generateWorkoutTips(context: TipsContext): List<WorkoutTip> {
        val prompt = buildTipsPrompt(context)
        val response = callOpenAI(prompt)
        return parseWorkoutTips(response)
    }
    
    /**
     * Construire le prompt pour générer les conseils
     */
    private fun buildTipsPrompt(context: TipsContext): String {
        return """
        Tu es un coach sportif expert qui donne des conseils personnalisés.
        
        ## Données de l'utilisateur :
        
        **Profil** :
        - Sports : ${context.sportsInterests.joinToString(", ")}
        - Niveau : ${context.level}
        - Sports favoris : ${context.favoriteSports.joinToString(", ")}
        
        **Activité (Google Fit)** :
        - Workouts/semaine : ${context.weeklyStats.workouts}
        - Calories/semaine : ${context.weeklyStats.calories}
        - Minutes/semaine : ${context.weeklyStats.minutes}
        - Streak : ${context.weeklyStats.streak} jours
        
        **Activités récentes** :
        ${context.recentWorkouts.take(5).joinToString("\n") { 
            "- ${it.sportType} : ${it.duration} min"
        }}
        
        **Challenges** :
        ${context.activeChallenges.joinToString("\n") { 
            "- ${it.title} : ${it.progress}/${it.total}"
        }}
        
        **Points à améliorer** :
        ${context.painPoints.joinToString(", ")}
        
        ## Tâche :
        
        Génère 6-8 conseils personnalisés répartis en 3 catégories :
        - **Basics** : Conseils fondamentaux (échauffement, étirements, etc.)
        - **Health** : Santé et bien-être (hydratation, nutrition, récupération)
        - **Training** : Entraînement avancé (progression, techniques, etc.)
        
        Les conseils doivent être :
        - Adaptés au niveau : ${context.level}
        - Basés sur les sports pratiqués : ${context.favoriteSports.joinToString(", ")}
        - Utiles pour les challenges en cours
        - Pertinents selon l'activité récente
        
        Réponds UNIQUEMENT en JSON :
        ```json
        {
          "tips": [
            {
              "title": "Warm-up is essential",
              "description": "Spend 5-10 minutes warming up before your ${context.favoriteSports.firstOrNull() ?: "workout"} sessions to prevent injuries and improve performance. Based on your recent activity, you've been doing ${context.weeklyStats.workouts} workouts per week, so proper warm-up is crucial.",
              "category": "Basics",
              "icon": "🔥"
            },
            {
              "title": "Stay hydrated during workouts",
              "description": "You've been burning ${context.weeklyStats.calories} calories this week. Drink water before, during, and after your workouts, especially for ${context.favoriteSports.firstOrNull() ?: "intense"} activities.",
              "category": "Health",
              "icon": "💧"
            }
          ]
        }
        ```
        """.trimIndent()
    }
    
    /**
     * Appeler l'API OpenAI
     */
    private suspend fun callOpenAI(prompt: String): String {
        val requestBody = JSONObject().apply {
            put("model", "gpt-4") // ou "gpt-3.5-turbo"
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "Tu es un expert en fitness et coaching sportif. Réponds toujours en JSON valide.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("temperature", 0.7)
            put("max_tokens", 2000)
        }
        
        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        
        if (!response.isSuccessful) {
            throw Exception("API Error: ${response.code} - $responseBody")
        }
        
        val jsonResponse = JSONObject(responseBody)
        val choices = jsonResponse.getJSONArray("choices")
        val message = choices.getJSONObject(0).getJSONObject("message")
        return message.getString("content")
    }
    
    /**
     * Parser les requêtes YouTube
     */
    private fun parseYouTubeQueries(aiResponse: String): List<YouTubeQuery> {
        try {
            val jsonStart = aiResponse.indexOf("{")
            val jsonEnd = aiResponse.lastIndexOf("}") + 1
            val jsonString = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                aiResponse.substring(jsonStart, jsonEnd)
            } else {
                aiResponse
            }
            
            val json = JSONObject(jsonString)
            val queriesArray = json.getJSONArray("queries")
            
            return (0 until queriesArray.length()).map { i ->
                val queryJson = queriesArray.getJSONObject(i)
                YouTubeQuery(
                    query = queryJson.getString("query"),
                    sportType = queryJson.optString("sportType", ""),
                    category = queryJson.optString("category", "General"),
                    reason = queryJson.optString("reason", "")
                )
            }
        } catch (e: Exception) {
            Log.e("AITipsService", "Error parsing YouTube queries", e)
            return emptyList()
        }
    }
    
    /**
     * Parser les conseils
     */
    private fun parseWorkoutTips(aiResponse: String): List<WorkoutTip> {
        try {
            val jsonStart = aiResponse.indexOf("{")
            val jsonEnd = aiResponse.lastIndexOf("}") + 1
            val jsonString = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                aiResponse.substring(jsonStart, jsonEnd)
            } else {
                aiResponse
            }
            
            val json = JSONObject(jsonString)
            val tipsArray = json.getJSONArray("tips")
            
            return (0 until tipsArray.length()).map { i ->
                val tipJson = tipsArray.getJSONObject(i)
                WorkoutTip(
                    id = "tip_${i + 1}",
                    title = tipJson.getString("title"),
                    description = tipJson.getString("description"),
                    icon = tipJson.optString("icon", "💪"),
                    category = tipJson.getString("category")
                )
            }
        } catch (e: Exception) {
            Log.e("AITipsService", "Error parsing workout tips", e)
            return emptyList()
        }
    }
    
    /**
     * Rechercher les vidéos YouTube avec les requêtes générées
     */
    private suspend fun searchYouTubeVideos(queries: List<YouTubeQuery>): List<WorkoutVideo> {
        val allVideos = mutableListOf<WorkoutVideo>()
        
        queries.forEach { query ->
            try {
                val videos = youtubeDataSource.searchWorkoutVideos(
                    query = query.query,
                    sportType = query.sportType,
                    maxResults = 2 // 2 vidéos par requête
                )
                allVideos.addAll(videos)
            } catch (e: Exception) {
                Log.e("AITipsService", "Error searching YouTube for: ${query.query}", e)
            }
        }
        
        // Dédupliquer et limiter à 10 vidéos
        return allVideos
            .distinctBy { it.id }
            .take(10)
    }
    
    /**
     * Fallback : suggestions basiques sans IA
     */
    private suspend fun generateFallbackTips(context: TipsContext): TipsRecommendations {
        // Suggestions basiques basées sur les sports d'intérêt
        val basicVideos = context.sportsInterests.take(3).flatMap { sport ->
            youtubeDataSource.searchWorkoutVideos(
                sportType = sport,
                level = context.level,
                maxResults = 2
            )
        }
        
        val basicTips = listOf(
            WorkoutTip(
                id = "tip_1",
                title = "Warm-up is essential",
                description = "Spend 5-10 minutes warming up to prevent injuries.",
                icon = "🔥",
                category = "Basics"
            ),
            WorkoutTip(
                id = "tip_2",
                title = "Stay hydrated",
                description = "Drink water before, during, and after your workout.",
                icon = "💧",
                category = "Health"
            ),
            WorkoutTip(
                id = "tip_3",
                title = "Progressive overload",
                description = "Gradually increase intensity to see improvements.",
                icon = "📈",
                category = "Training"
            )
        )
        
        return TipsRecommendations(
            videos = basicVideos,
            tips = basicTips
        )
    }
}

// Modèles de données
data class YouTubeQuery(
    val query: String,
    val sportType: String,
    val category: String,
    val reason: String
)

data class TipsRecommendations(
    val videos: List<WorkoutVideo>,
    val tips: List<WorkoutTip>
)
```

---

## 3️⃣ Intégration YouTube Data API

### YouTubeDataSource.kt (Complété)

```kotlin
package com.example.damandroid.data.datasource

import android.util.Log
import com.example.damandroid.api.OpenWeatherService // Réutiliser ou créer YouTubeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 10,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse
}

data class YouTubeSearchResponse(
    val items: List<YouTubeVideoItem>
)

data class YouTubeVideoItem(
    val id: YouTubeVideoId,
    val snippet: YouTubeVideoSnippet
)

data class YouTubeVideoId(
    val videoId: String
)

data class YouTubeVideoSnippet(
    val title: String,
    val description: String,
    val thumbnails: YouTubeThumbnails,
    val channelTitle: String
)

data class YouTubeThumbnails(
    val medium: YouTubeThumbnail
)

data class YouTubeThumbnail(
    val url: String
)

class YouTubeDataSource(
    private val apiKey: String
) {
    
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/youtube/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val apiService = retrofit.create(YouTubeApiService::class.java)
    
    suspend fun searchWorkoutVideos(
        query: String,
        sportType: String = "",
        level: String = "beginner",
        maxResults: Int = 10
    ): List<WorkoutVideo> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchVideos(
                query = query,
                maxResults = maxResults,
                apiKey = apiKey
            )
            
            response.items.map { item ->
                WorkoutVideo(
                    id = item.id.videoId,
                    title = item.snippet.title,
                    description = item.snippet.description,
                    thumbnailUrl = item.snippet.thumbnails.medium.url,
                    channelName = item.snippet.channelTitle,
                    sportType = sportType,
                    level = level,
                    videoUrl = "https://www.youtube.com/watch?v=${item.id.videoId}"
                )
            }
        } catch (e: Exception) {
            Log.e("YouTubeDataSource", "Error fetching videos", e)
            emptyList()
        }
    }
}

data class WorkoutVideo(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelName: String,
    val sportType: String,
    val level: String,
    val videoUrl: String
)
```

---

## 4️⃣ Intégration dans le ViewModel

### AICoachViewModel.kt (Modifié)

```kotlin
class AICoachViewModel(
    private val fitnessDataSource: FitnessDataSource,
    private val locationDataSource: LocationDataSource,
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository,
    private val aiTipsService: AITipsService, // Ajouter
    private val youtubeDataSource: YouTubeDataSource // Ajouter
) : ViewModel() {
    
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 1. Charger les données
                val userProfile = userRepository.getCurrentUserProfile()
                val weeklyStats = fitnessDataSource.getWeeklyStats()
                val recentWorkouts = fitnessDataSource.getActivityHistory()
                val activeChallenges = getActiveChallenges()
                
                // 2. Construire le contexte pour l'IA
                val tipsContext = TipsContext(
                    sportsInterests = userProfile.sportsInterests,
                    level = calculateLevel(userProfile),
                    favoriteSports = extractFavoriteSports(recentWorkouts),
                    weeklyStats = weeklyStats,
                    recentWorkouts = recentWorkouts,
                    activityTrends = calculateTrends(recentWorkouts),
                    activeChallenges = activeChallenges,
                    goals = getUserGoals(),
                    painPoints = identifyPainPoints(weeklyStats, activeChallenges)
                )
                
                // 3. Générer les tips avec l'IA
                val tipsRecommendations = aiTipsService.generatePersonalizedTips(tipsContext)
                
                // 4. Mettre à jour l'état
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        overview = AICoachOverview(
                            weeklyStats = weeklyStats,
                            suggestions = getSuggestions(), // Pour l'onglet "For You"
                            workoutTips = tipsRecommendations.tips, // Conseils générés par IA
                            workoutVideos = tipsRecommendations.videos, // Vidéos YouTube
                            challenges = activeChallenges
                        ),
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }
    
    private fun identifyPainPoints(
        stats: WeeklyStats,
        challenges: List<Challenge>
    ): List<String> {
        val points = mutableListOf<String>()
        
        if (stats.workouts < 3) {
            points.add("Need more workouts")
        }
        if (stats.calories < 1000) {
            points.add("Need to burn more calories")
        }
        if (stats.streak < 7) {
            points.add("Need consistency")
        }
        
        challenges.forEach { challenge ->
            if (challenge.progress < challenge.total / 2) {
                points.add("Challenge: ${challenge.title}")
            }
        }
        
        return points
    }
}
```

---

## 5️⃣ Exemple de Réponse IA

### Prompt Envoyé

```
Tu es un expert en recherche de vidéos de workout...
[Données utilisateur]
[Données Google Fit]
[Challenges]
```

### Réponse IA pour Vidéos YouTube

```json
{
  "queries": [
    {
      "query": "basketball shooting form tutorial beginner",
      "sportType": "Basketball",
      "category": "Tutorial",
      "reason": "L'utilisateur a fait 3 workouts de basketball cette semaine et est débutant"
    },
    {
      "query": "running technique tips intermediate",
      "sportType": "Running",
      "category": "Tips",
      "reason": "L'utilisateur a un challenge de course (7/30 jours) et est intermédiaire"
    },
    {
      "query": "recovery yoga after workout",
      "sportType": "Yoga",
      "category": "Recovery",
      "reason": "L'utilisateur a brûlé 1200 calories cette semaine, la récupération est importante"
    }
  ]
}
```

### Réponse IA pour Conseils

```json
{
  "tips": [
    {
      "title": "Warm-up before Basketball",
      "description": "Vous avez fait 3 sessions de basketball cette semaine. Passez 5-10 minutes à vous échauffer avant chaque session pour prévenir les blessures et améliorer vos performances.",
      "category": "Basics",
      "icon": "🔥"
    },
    {
      "title": "Hydration for Active Days",
      "description": "Avec ${weeklyStats.calories} calories brûlées cette semaine et un streak de ${weeklyStats.streak} jours, l'hydratation est cruciale. Buvez de l'eau avant, pendant et après vos workouts.",
      "category": "Health",
      "icon": "💧"
    },
    {
      "title": "Progressive Training for Running",
      "description": "Vous êtes à 7/30 jours de votre challenge de course. Augmentez progressivement la distance pour atteindre votre objectif sans vous blesser.",
      "category": "Training",
      "icon": "📈"
    }
  ]
}
```

---

## 6️⃣ Affichage dans l'UI

### AICoachScreen.kt (Onglet Tips)

```kotlin
AICoachTab.TIPS -> {
    item { VideoLibraryCard(palette) }
    
    // Afficher les vidéos YouTube recommandées par l'IA
    items(overview.workoutVideos, key = { it.id }) { video ->
        WorkoutVideoCard(
            video = video,
            palette = palette,
            onClick = { openYouTubeVideo(video.videoUrl) }
        )
    }
    
    // Afficher les conseils personnalisés par catégorie
    item { SectionTitle("Basics", palette) }
    items(overview.workoutTips.filter { it.category == "Basics" }, key = { it.id }) { tip ->
        TipCard(tip = tip, palette = palette)
    }
    
    item { SectionTitle("Health", palette) }
    items(overview.workoutTips.filter { it.category == "Health" }, key = { it.id }) { tip ->
        TipCard(tip = tip, palette = palette)
    }
    
    item { SectionTitle("Training", palette) }
    items(overview.workoutTips.filter { it.category == "Training" }, key = { it.id }) { tip ->
        TipCard(tip = tip, palette = palette)
    }
}
```

---

## 7️⃣ Modèles de Données

### Modifier AICoachModels.kt

```kotlin
data class AICoachOverview(
    val weeklyStats: WeeklyStats,
    val suggestions: List<Suggestion>,
    val workoutTips: List<WorkoutTip>,
    val workoutVideos: List<WorkoutVideo>, // Ajouter
    val challenges: List<Challenge>
)

data class WorkoutVideo(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val channelName: String,
    val sportType: String,
    val level: String,
    val videoUrl: String
)
```

---

## 8️⃣ Configuration API Keys

### Obtenir YouTube Data API Key

1. Allez sur [Google Cloud Console](https://console.cloud.google.com/)
2. Activez **YouTube Data API v3**
3. Créez une **API Key**
4. Ajoutez dans `local.properties` :

```properties
YOUTUBE_API_KEY=votre_cle_youtube
OPENAI_API_KEY=votre_cle_openai
```

---

## 9️⃣ Résumé

### Flux Complet

1. **Collecter les données** :
   - Profil utilisateur (sports, niveau)
   - Données Google Fit (workouts, calories, streak)
   - Challenges actifs
   - Activités récentes

2. **Générer avec l'IA** :
   - Requêtes YouTube personnalisées
   - Conseils adaptés au profil

3. **Rechercher sur YouTube** :
   - Utiliser les requêtes générées
   - Récupérer les vidéos

4. **Afficher dans Tips** :
   - Vidéos YouTube
   - Conseils par catégorie

---

## 🔗 Ressources

- **YouTube Data API** : https://developers.google.com/youtube/v3
- **OpenAI API** : https://platform.openai.com/docs
- **Obtenir YouTube API Key** : https://console.cloud.google.com/apis/credentials

---

**Date de création** : 22 Novembre 2025

