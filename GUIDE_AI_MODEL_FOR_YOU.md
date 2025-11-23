# 🤖 Guide : Modèle IA pour Suggestions Personnalisées "For You"

## 🎯 Vue d'Ensemble

Ce guide explique comment utiliser un **modèle IA efficace** (OpenAI GPT-4, Claude, ou modèle local) pour générer des suggestions personnalisées dans l'onglet **"For You"**, en combinant :
1. **Données de l'application** (backend) : activités, profil utilisateur, historique
2. **Données Google Fit** : workouts, calories, minutes, streak

---

## 🧠 Modèles IA Recommandés

### Option 1 : OpenAI GPT-4 (Recommandé)
- **Avantages** : Très performant, excellent pour la compréhension contextuelle
- **Coût** : Payant mais raisonnable
- **API** : https://platform.openai.com/

### Option 2 : OpenAI GPT-3.5 Turbo
- **Avantages** : Moins cher que GPT-4, toujours très bon
- **Coût** : Plus économique
- **API** : https://platform.openai.com/

### Option 3 : Anthropic Claude
- **Avantages** : Excellent pour l'analyse de données
- **Coût** : Payant
- **API** : https://www.anthropic.com/

### Option 4 : Modèle Local (Gemini, Llama)
- **Avantages** : Gratuit, privé
- **Inconvénients** : Moins performant, nécessite plus de ressources

---

## 📋 Architecture avec Modèle IA

```
┌─────────────────────────────────────────────────────────┐
│              Onglet "For You"                           │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│         AICoachViewModel                                 │
│  - Collecte toutes les données                           │
│  - Prépare le contexte pour l'IA                        │
└─────────────────────────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ App Data     │ │ Google Fit   │ │ Location     │
│ (Backend)    │ │ Data         │ │ Data         │
└──────────────┘ └──────────────┘ └──────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│         AI Service (OpenAI/Claude)                      │
│  - Analyse les données                                  │
│  - Génère des suggestions personnalisées                 │
└─────────────────────────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────┐
│         Suggestions Personnalisées                       │
│  - Activités recommandées                               │
│  - Score de compatibilité                               │
│  - Raisons de la recommandation                         │
└─────────────────────────────────────────────────────────┘
```

---

## 1️⃣ Préparer les Données pour l'IA

### Données à Collecter

#### A. Données de l'Application (Backend)

```kotlin
data class UserAppData(
    // Profil utilisateur
    val userId: String,
    val name: String,
    val email: String,
    val sportsInterests: List<String>, // ["Running", "Basketball", "Swimming"]
    val level: String, // "Beginner", "Intermediate", "Advanced"
    val location: LocationData?,
    
    // Historique d'activités
    val activitiesJoined: List<Activity>, // Activités auxquelles l'utilisateur a participé
    val activitiesCreated: List<Activity>, // Activités créées
    val favoriteSports: List<String>, // Sports les plus pratiqués
    val preferredTimes: List<Int>, // Heures préférées [7, 18, 20]
    
    // Activités disponibles
    val availableActivities: List<Activity>, // Activités publiques disponibles
    val nearbyActivities: List<Activity> // Activités proches de la localisation
)
```

#### B. Données Google Fit

```kotlin
data class GoogleFitData(
    val weeklyStats: WeeklyStats,
    val dailyStats: DailyStats,
    val activityHistory: List<WorkoutSession>, // Historique des workouts
    val trends: ActivityTrends // Tendances (augmentation/diminution)
)

data class WeeklyStats(
    val workouts: Int,
    val calories: Int,
    val minutes: Int,
    val streak: Int
)

data class WorkoutSession(
    val sportType: String,
    val duration: Int, // minutes
    val calories: Int,
    val date: String
)
```

---

## 2️⃣ Créer le Service IA

### AISuggestionService.kt

```kotlin
package com.example.damandroid.domain.usecase

import android.util.Log
import com.example.damandroid.domain.model.Activity
import com.example.damandroid.domain.model.Suggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AISuggestionService(
    private val apiKey: String // OpenAI API Key
) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val apiUrl = "https://api.openai.com/v1/chat/completions"
    
    /**
     * Génère des suggestions personnalisées en utilisant GPT-4
     */
    suspend fun generatePersonalizedSuggestions(
        userAppData: UserAppData,
        googleFitData: GoogleFitData
    ): List<Suggestion> = withContext(Dispatchers.IO) {
        try {
            // 1. Construire le prompt pour l'IA
            val prompt = buildPrompt(userAppData, googleFitData)
            
            // 2. Appeler l'API OpenAI
            val response = callOpenAI(prompt)
            
            // 3. Parser la réponse et générer les suggestions
            parseAIResponse(response, userAppData.availableActivities)
        } catch (e: Exception) {
            Log.e("AISuggestionService", "Error generating suggestions", e)
            // Fallback : suggestions basiques sans IA
            generateFallbackSuggestions(userAppData)
        }
    }
    
    /**
     * Construire le prompt pour l'IA
     */
    private fun buildPrompt(
        userAppData: UserAppData,
        googleFitData: GoogleFitData
    ): String {
        return """
        Tu es un coach sportif IA qui propose des activités sportives personnalisées.
        
        ## Données de l'utilisateur :
        
        **Profil** :
        - Nom : ${userAppData.name}
        - Sports d'intérêt : ${userAppData.sportsInterests.joinToString(", ")}
        - Niveau : ${userAppData.level}
        - Localisation : ${userAppData.location?.city ?: "Non spécifiée"}
        
        **Historique d'activités** :
        - Activités rejointes : ${userAppData.activitiesJoined.size}
        - Sports favoris : ${userAppData.favoriteSports.joinToString(", ")}
        - Heures préférées : ${userAppData.preferredTimes.joinToString("h, ")}h
        
        **Données Google Fit (semaine actuelle)** :
        - Workouts : ${googleFitData.weeklyStats.workouts}
        - Calories brûlées : ${googleFitData.weeklyStats.calories}
        - Minutes d'activité : ${googleFitData.weeklyStats.minutes}
        - Streak : ${googleFitData.weeklyStats.streak} jours consécutifs
        
        **Activités récentes Google Fit** :
        ${googleFitData.activityHistory.take(5).joinToString("\n") { 
            "- ${it.sportType} : ${it.duration} min, ${it.calories} cal (${it.date})"
        }}
        
        ## Activités disponibles :
        
        ${userAppData.availableActivities.take(10).joinToString("\n\n") { activity ->
            """
            - **${activity.title}** (${activity.sportType})
              - Date/Heure : ${activity.date} à ${activity.time}
              - Lieu : ${activity.location}
              - Participants : ${activity.participants}/${activity.maxParticipants}
              - Niveau : ${activity.level}
              - Description : ${activity.description}
            """
        }}
        
        ## Tâche :
        
        Analyse les données de l'utilisateur et les activités disponibles, puis propose 5-8 suggestions personnalisées.
        
        Pour chaque suggestion, fournis :
        1. **ID de l'activité** (utilise l'ID exact de l'activité)
        2. **Titre de la suggestion** (créatif et engageant)
        3. **Description** (explique pourquoi cette activité est recommandée)
        4. **Score de compatibilité** (0-100)
        5. **Raison de la recommandation** (basée sur les données)
        
        Réponds UNIQUEMENT en JSON au format suivant :
        ```json
        {
          "suggestions": [
            {
              "activityId": "id_de_l_activite",
              "title": "Titre de la suggestion",
              "description": "Description personnalisée",
              "matchScore": 95,
              "reason": "Raison basée sur les données (sport favori, niveau, localisation, etc.)"
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
            put("model", "gpt-4") // ou "gpt-3.5-turbo" pour économiser
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "Tu es un assistant expert en recommandations d'activités sportives. Réponds toujours en JSON valide.")
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
     * Parser la réponse de l'IA
     */
    private fun parseAIResponse(
        aiResponse: String,
        availableActivities: List<Activity>
    ): List<Suggestion> {
        try {
            // Extraire le JSON de la réponse (peut contenir du markdown)
            val jsonStart = aiResponse.indexOf("{")
            val jsonEnd = aiResponse.lastIndexOf("}") + 1
            val jsonString = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                aiResponse.substring(jsonStart, jsonEnd)
            } else {
                aiResponse
            }
            
            val json = JSONObject(jsonString)
            val suggestionsArray = json.getJSONArray("suggestions")
            
            val suggestions = mutableListOf<Suggestion>()
            
            for (i in 0 until suggestionsArray.length()) {
                val suggestionJson = suggestionsArray.getJSONObject(i)
                val activityId = suggestionJson.getString("activityId")
                
                // Trouver l'activité correspondante
                val activity = availableActivities.find { it.id == activityId }
                    ?: continue
                
                suggestions.add(
                    Suggestion(
                        id = activity.id,
                        title = suggestionJson.getString("title"),
                        description = suggestionJson.getString("description"),
                        icon = getSportIcon(activity.sportType),
                        time = formatTime(activity.date, activity.time),
                        participants = activity.participants,
                        matchScore = suggestionJson.getInt("matchScore"),
                        reason = suggestionJson.optString("reason", "")
                    )
                )
            }
            
            return suggestions.sortedByDescending { it.matchScore }
        } catch (e: Exception) {
            Log.e("AISuggestionService", "Error parsing AI response", e)
            return emptyList()
        }
    }
    
    /**
     * Fallback : suggestions basiques sans IA
     */
    private fun generateFallbackSuggestions(userAppData: UserAppData): List<Suggestion> {
        return userAppData.availableActivities
            .filter { activity ->
                activity.sportType in userAppData.sportsInterests ||
                activity.sportType in userAppData.favoriteSports
            }
            .take(5)
            .map { activity ->
                Suggestion(
                    id = activity.id,
                    title = activity.title,
                    description = activity.description,
                    icon = getSportIcon(activity.sportType),
                    time = formatTime(activity.date, activity.time),
                    participants = activity.participants,
                    matchScore = 75, // Score par défaut
                    reason = "Basé sur vos sports d'intérêt"
                )
            }
    }
    
    private fun getSportIcon(sportType: String): String {
        return when (sportType.lowercase()) {
            "running" -> "🏃"
            "swimming" -> "🏊"
            "cycling" -> "🚴"
            "basketball" -> "🏀"
            "football" -> "⚽"
            "yoga" -> "🧘"
            else -> "💪"
        }
    }
    
    private fun formatTime(date: String, time: String): String {
        // Formater la date/heure pour l'affichage
        return "$date à $time"
    }
}
```

---

## 3️⃣ Modèles de Données

### UserAppData.kt

```kotlin
package com.example.damandroid.domain.model

data class UserAppData(
    val userId: String,
    val name: String,
    val email: String,
    val sportsInterests: List<String>,
    val level: String,
    val location: LocationData?,
    val activitiesJoined: List<Activity>,
    val activitiesCreated: List<Activity>,
    val favoriteSports: List<String>,
    val preferredTimes: List<Int>,
    val availableActivities: List<Activity>,
    val nearbyActivities: List<Activity>
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val city: String?,
    val country: String?
)

data class Activity(
    val id: String,
    val title: String,
    val description: String,
    val sportType: String,
    val location: String,
    val date: String,
    val time: String,
    val participants: Int,
    val maxParticipants: Int,
    val level: String
)
```

### GoogleFitData.kt

```kotlin
package com.example.damandroid.domain.model

data class GoogleFitData(
    val weeklyStats: WeeklyStats,
    val dailyStats: DailyStats,
    val activityHistory: List<WorkoutSession>,
    val trends: ActivityTrends
)

data class WorkoutSession(
    val sportType: String,
    val duration: Int, // minutes
    val calories: Int,
    val date: String
)

data class ActivityTrends(
    val workoutsTrend: String, // "increasing", "decreasing", "stable"
    val caloriesTrend: String,
    val minutesTrend: String
)
```

---

## 4️⃣ Intégration dans le ViewModel

### AICoachViewModel.kt (Modifié)

```kotlin
package com.example.damandroid.presentation.ai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.data.datasource.FitnessDataSource
import com.example.damandroid.data.datasource.LocationDataSource
import com.example.damandroid.domain.model.UserAppData
import com.example.damandroid.domain.model.GoogleFitData
import com.example.damandroid.domain.usecase.AISuggestionService
import com.example.damandroid.domain.repository.ActivityRepository
import com.example.damandroid.domain.repository.UserRepository
import com.example.damandroid.presentation.ai.model.AICoachTab
import com.example.damandroid.presentation.ai.model.AICoachUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

class AICoachViewModel(
    private val fitnessDataSource: FitnessDataSource,
    private val locationDataSource: LocationDataSource,
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository,
    private val aiSuggestionService: AISuggestionService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AICoachUiState(isLoading = true))
    val uiState: StateFlow<AICoachUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 1. Charger toutes les données en parallèle
                val userDataDeferred = async { loadUserAppData() }
                val googleFitDataDeferred = async { loadGoogleFitData() }
                val locationDeferred = async { locationDataSource.getCurrentLocation() }
                
                // 2. Attendre que toutes les données soient chargées
                val userAppData = userDataDeferred.await()
                val googleFitData = googleFitDataDeferred.await()
                val location = locationDeferred.await()
                
                // 3. Générer les suggestions avec l'IA
                val suggestions = aiSuggestionService.generatePersonalizedSuggestions(
                    userAppData = userAppData.copy(location = location),
                    googleFitData = googleFitData
                )
                
                // 4. Mettre à jour l'état
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        overview = AICoachOverview(
                            weeklyStats = googleFitData.weeklyStats.toWeeklyStats(),
                            suggestions = suggestions,
                            // ... autres données
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
    
    /**
     * Charger les données de l'application (backend)
     */
    private suspend fun loadUserAppData(): UserAppData {
        // Récupérer le profil utilisateur
        val userProfile = userRepository.getCurrentUserProfile()
        
        // Récupérer l'historique d'activités
        val activitiesJoined = activityRepository.getUserActivities()
        val activitiesCreated = activityRepository.getUserCreatedActivities()
        
        // Récupérer les activités disponibles
        val availableActivities = activityRepository.getPublicActivities()
        
        // Analyser les préférences
        val favoriteSports = extractFavoriteSports(activitiesJoined)
        val preferredTimes = extractPreferredTimes(activitiesJoined)
        
        return UserAppData(
            userId = userProfile.id,
            name = userProfile.name,
            email = userProfile.email,
            sportsInterests = userProfile.sportsInterests,
            level = calculateLevel(activitiesJoined),
            location = null, // Sera rempli avec la localisation actuelle
            activitiesJoined = activitiesJoined,
            activitiesCreated = activitiesCreated,
            favoriteSports = favoriteSports,
            preferredTimes = preferredTimes,
            availableActivities = availableActivities,
            nearbyActivities = availableActivities // Filtrer par localisation si nécessaire
        )
    }
    
    /**
     * Charger les données Google Fit
     */
    private suspend fun loadGoogleFitData(): GoogleFitData {
        val weeklyStats = fitnessDataSource.getWeeklyStats()
        val dailyStats = fitnessDataSource.getTodayStats()
        val activityHistory = fitnessDataSource.getActivityHistory() // À implémenter
        
        return GoogleFitData(
            weeklyStats = weeklyStats,
            dailyStats = dailyStats,
            activityHistory = activityHistory,
            trends = calculateTrends(activityHistory)
        )
    }
    
    private fun extractFavoriteSports(activities: List<Activity>): List<String> {
        return activities
            .groupBy { it.sportType }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
    
    private fun extractPreferredTimes(activities: List<Activity>): List<Int> {
        return activities
            .map { it.time.hour }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
    
    private fun calculateLevel(activities: List<Activity>): String {
        return when {
            activities.size >= 20 -> "Advanced"
            activities.size >= 10 -> "Intermediate"
            else -> "Beginner"
        }
    }
    
    private fun calculateTrends(history: List<WorkoutSession>): ActivityTrends {
        // Analyser les tendances (simplifié)
        return ActivityTrends(
            workoutsTrend = "increasing",
            caloriesTrend = "stable",
            minutesTrend = "increasing"
        )
    }

    fun onTabSelected(tab: AICoachTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
```

---

## 5️⃣ Configuration de l'API Key

### Option 1 : Backend Proxy (Recommandé - Plus Sécurisé)

**Ne pas mettre l'API key dans l'app Android !**

Créer un endpoint backend qui fait l'appel à OpenAI :

**Backend** : `POST /api/ai/suggestions`

```typescript
// Backend NestJS
@Post('suggestions')
async generateSuggestions(@Body() data: SuggestionRequestDto) {
  // Le backend appelle OpenAI avec l'API key
  // Retourne les suggestions
}
```

**Android** : Appeler le backend au lieu d'OpenAI directement

### Option 2 : local.properties (Pour Tests)

**Fichier** : `local.properties`

```properties
OPENAI_API_KEY=sk-...
```

**Fichier** : `app/build.gradle.kts`

```kotlin
android {
    defaultConfig {
        val localProperties = Properties()
        localProperties.load(FileInputStream(rootProject.file("local.properties")))
        buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties.getProperty("OPENAI_API_KEY", "")}\"")
    }
}
```

---

## 6️⃣ Optimisations et Alternatives

### A. Utiliser GPT-3.5 Turbo (Moins Cher)

```kotlin
put("model", "gpt-3.5-turbo") // Au lieu de "gpt-4"
```

### B. Mise en Cache des Suggestions

```kotlin
// Mettre en cache les suggestions pendant 1 heure
private var cachedSuggestions: List<Suggestion>? = null
private var cacheTimestamp: Long = 0

fun getSuggestions(): List<Suggestion> {
    if (cachedSuggestions != null && 
        System.currentTimeMillis() - cacheTimestamp < 3600000) {
        return cachedSuggestions!!
    }
    // Générer de nouvelles suggestions
}
```

### C. Modèle Local (Gemini, Llama)

```kotlin
// Utiliser Gemini API (gratuit jusqu'à certaines limites)
private val geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent"
```

---

## 7️⃣ Exemple de Réponse IA

### Prompt Envoyé

```
Tu es un coach sportif IA...
[Données utilisateur]
[Données Google Fit]
[Activités disponibles]
```

### Réponse IA (JSON)

```json
{
  "suggestions": [
    {
      "activityId": "69207cc2aaa4e7ee5d4045a0",
      "title": "Morning Basketball Session",
      "description": "Perfect for your active streak! You've been doing great with 7 consecutive days. This basketball session matches your favorite sport and is at your preferred time (7AM).",
      "matchScore": 95,
      "reason": "Matches your favorite sport (Basketball), preferred time (7AM), and complements your current activity level (3 workouts this week)."
    },
    {
      "activityId": "69207c7eaaa4e7ee5d404511",
      "title": "Evening Football Match",
      "description": "Great way to reach your weekly goal! You're at 3/5 workouts. This football match will help you reach 4 workouts and burn more calories.",
      "matchScore": 88,
      "reason": "Helps you reach your weekly workout goal, matches your sports interests, and is at a convenient time (6PM)."
    }
  ]
}
```

---

## 8️⃣ Résumé

### Étapes pour Implémenter

1. **Créer `AISuggestionService`** :
   - Construire le prompt avec toutes les données
   - Appeler l'API OpenAI/Claude
   - Parser la réponse JSON

2. **Collecter les Données** :
   - Données application (backend)
   - Données Google Fit
   - Localisation

3. **Intégrer dans ViewModel** :
   - Charger toutes les données
   - Appeler le service IA
   - Mettre à jour l'état

4. **Afficher dans l'UI** :
   - Afficher les suggestions avec score
   - Afficher les raisons de recommandation

---

## 🔗 Ressources

- **OpenAI API** : https://platform.openai.com/docs
- **GPT-4** : https://platform.openai.com/docs/models/gpt-4
- **GPT-3.5 Turbo** : https://platform.openai.com/docs/models/gpt-3-5
- **Anthropic Claude** : https://docs.anthropic.com/

---

## 💡 Notes Importantes

1. **Sécurité** : Ne jamais mettre l'API key dans le code Android (utiliser backend proxy)
2. **Coûts** : GPT-4 est plus cher que GPT-3.5, mais plus performant
3. **Rate Limits** : Respecter les limites de l'API
4. **Fallback** : Toujours prévoir un fallback si l'IA échoue

---

**Date de création** : 22 Novembre 2025

