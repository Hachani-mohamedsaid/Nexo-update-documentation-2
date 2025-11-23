# Guide Complet API Achievements pour Android Kotlin Jetpack Compose

## 🏗️ Vue d'Ensemble de l'Architecture

Le système d'achievements fonctionne en **trois couches principales** :

1. **Couche API** : Reçoit les requêtes HTTP, vérifie l'authentification, valide les données
2. **Couche Business Logic** : Contient toute la logique métier (calcul XP, déblocage badges, progression challenges)
3. **Couche Data** : Stocke toutes les informations dans la base de données MongoDB

---

## 🔐 Authentification

**Tous les endpoints nécessitent une authentification JWT.**

### Processus d'Authentification

1. **Login** : L'utilisateur se connecte avec email/mot de passe
2. **Réception du Token** : Le serveur renvoie un token JWT valide pendant 7 jours (ou 30 jours avec "Remember Me")
3. **Stockage du Token** : L'app stocke le token de manière sécurisée (Android Keystore/SharedPreferences cryptés)
4. **Utilisation** : Ajouter le header `Authorization: Bearer <token>` à chaque requête

### Configuration de Base

**Base URL :**

```
https://apinest-production.up.railway.app/
```

**Headers requis pour toutes les requêtes :**

```
Authorization: Bearer <access_token>
Content-Type: application/json
```

---

## 📋 Endpoints Disponibles

### 1. **Résumé des Achievements**

`GET /achievements/summary`

Retourne un résumé complet des achievements de l'utilisateur : niveau actuel, XP total, progression, badges et séries.

**Réponse (200 OK) :**

```json
{
  "level": {
    "currentLevel": 5,
    "totalXp": 2500,
    "xpForNextLevel": 3000,
    "currentLevelXp": 500,
    "progressPercentage": 50.0
  },
  "stats": {
    "totalBadges": 8,
    "currentStreak": 5,
    "bestStreak": 12
  }
}
```

---

### 2. **Badges**

`GET /achievements/badges`

Retourne les badges obtenus et les badges en cours de progression avec leurs détails.

**Réponse (200 OK) :**

```json
{
  "earnedBadges": [
    {
      "_id": "badge123",
      "name": "Premier Pas",
      "description": "Compléter votre première activité",
      "iconUrl": "https://example.com/badge1.png",
      "rarity": "common",
      "category": "milestone",
      "earnedAt": "2025-01-15T10:30:00Z"
    },
    {
      "_id": "badge456",
      "name": "On Fire",
      "description": "Faire du sport 7 jours consécutifs",
      "iconUrl": "https://example.com/badge-fire.png",
      "rarity": "rare",
      "category": "streak",
      "earnedAt": "2025-01-20T08:15:00Z"
    }
  ],
  "inProgress": [
    {
      "badge": {
        "_id": "badge789",
        "name": "Marathonien",
        "description": "Compléter 10 activités",
        "iconUrl": "https://example.com/badge-marathon.png",
        "rarity": "rare",
        "category": "activity"
      },
      "currentProgress": 7,
      "target": 10,
      "percentage": 70.0
    }
  ]
}
```

**Catégories de badges :**

- `milestone` : Badges pour les grandes étapes (1ère activité, 100ème activité)
- `activity` : Badges liés aux types d'exercices spécifiques
- `social` : Badges liés aux interactions avec d'autres utilisateurs
- `streak` : Badges liés à la régularité (séries de jours)
- `distance` : Badges basés sur la distance totale parcourue

**Raretés de badges :**

- `common` (gris) : Facile à obtenir
- `uncommon` (vert) : Nécessite un peu d'effort
- `rare` (bleu) : Demande de la persévérance
- `epic` (violet) : Vraiment difficile à obtenir
- `legendary` (orange) : Réservé aux champions

---

### 3. **Challenges Actifs**

`GET /achievements/challenges`

Retourne tous les défis actifs de l'utilisateur avec leur progression actuelle.

**Réponse (200 OK) :**

```json
{
  "activeChallenges": [
    {
      "_id": "challenge123",
      "name": "Défi Hebdomadaire",
      "description": "Compléter 5 activités cette semaine",
      "challengeType": "weekly",
      "xpReward": 500,
      "currentProgress": 3,
      "target": 5,
      "daysLeft": 3,
      "expiresAt": "2025-01-25T23:59:59Z"
    },
    {
      "_id": "challenge456",
      "name": "100 km ce mois-ci",
      "description": "Parcours 100 kilomètres avant la fin du mois",
      "challengeType": "monthly",
      "xpReward": 1000,
      "currentProgress": 48,
      "target": 100,
      "daysLeft": 15,
      "expiresAt": "2025-01-31T23:59:59Z"
    }
  ]
}
```

**Types de challenges :**

- `daily` : Objectifs à compléter en 24 heures (se renouvelle chaque jour)
- `weekly` : Objectifs à compléter en 7 jours (se renouvelle chaque lundi)
- `monthly` : Objectifs à compléter en 30 jours (se renouvelle le 1er du mois)
- `event` : Challenges événementiels limités dans le temps (non récurrents)

---

### 4. **Classement (Leaderboard)**

`GET /achievements/leaderboard?page=1&limit=20`

Retourne le classement avec la position de l'utilisateur actuel.

**Query Parameters :**

- `page` (optionnel) : Numéro de page (défaut: 1)
- `limit` (optionnel) : Nombre d'entrées par page (défaut: 20, max recommandé: 50)

**Réponse (200 OK) :**

```json
{
  "currentUser": {
    "rank": 538,
    "username": "john_doe",
    "totalXp": 1250,
    "isCurrentUser": true
  },
  "leaderboard": [
    {
      "rank": 1,
      "username": "champion",
      "totalXp": 15000,
      "medal": "🥇"
    },
    {
      "rank": 2,
      "username": "runner",
      "totalXp": 12000,
      "medal": "🥈"
    },
    {
      "rank": 3,
      "username": "athlete",
      "totalXp": 11000,
      "medal": "🥉"
    },
    {
      "rank": 4,
      "username": "player4",
      "totalXp": 10500
    }
  ],
  "page": 1,
  "totalPages": 63
}
```

**Notes importantes :**

- Le `currentUser` affiche toujours la position de l'utilisateur connecté, même s'il n'est pas dans le top de la page
- Les 3 premiers du podium ont automatiquement une médaille (🥇🥈🥉)
- Utilisez la pagination pour naviguer dans le classement

---

## 🎮 Concepts du Système

### Système de Niveau et d'Expérience (XP)

#### Calcul du Niveau

Le niveau est calculé de manière exponentielle : plus vous montez de niveau, plus il est difficile d'atteindre le niveau suivant.

**Formule (approximative) :**

```
XP nécessaire pour niveau N = Base × (Niveau^Puissance)
```

**Exemples de progression :**

- Niveau 1 → 2 : ~100 XP
- Niveau 2 → 3 : ~283 XP
- Niveau 3 → 4 : ~520 XP
- Niveau 5 → 6 : ~1,500 XP
- Niveau 10 → 11 : ~10,000 XP

**Barre de progression :**

```
Pourcentage = (XP actuel dans le niveau / XP nécessaire pour niveau suivant) × 100
```

#### Comment Gagner de l'XP ?

**1. XP par activité :**

Chaque activité enregistrée donne de l'XP calculé selon :

- **XP de base** : Minimum garanti (ex: 10 XP)
- **Bonus durée** : ~0.5 XP par minute d'exercice
- **Bonus distance** : ~2 XP par kilomètre (pour les activités avec distance)
- **Multiplicateur de type** :
  - Natation : 1.5x (plus difficile)
  - Course : 1.2x
  - Vélo : 1.0x
  - Yoga : 1.0x

**Exemple de calcul :**

```
Activité : 30 min de course, 5 km
- XP de base : 10
- Durée : 30 × 0.5 = 15 XP
- Distance : 5 × 2 = 10 XP
- Total : (10 + 15 + 10) × 1.2 (multiplicateur course) = 42 XP
```

**2. XP bonus :**

- **Débloquer un badge** : 150-500 XP selon la rareté
- **Compléter un challenge** : 100-1000 XP selon le défi

---

### 🔥 Système de Séries (Streaks)

Une série est le nombre de jours consécutifs où l'utilisateur a fait au moins une activité.

**Règles :**

- **Même jour** : Si l'utilisateur fait plusieurs activités le même jour, la série ne change pas
- **Jour consécutif** : Série augmente de +1
- **Interruption** : Si 2+ jours sans activité, la série revient à 1

**Exemples :**

- Activité lundi, activité mardi → Série passe de 5 à 6 jours
- Activité lundi, rien mardi, activité mercredi → Série revient à 1

**Meilleure série :**

- Le record personnel qui ne diminue jamais
- Sert de référence pour les badges de série

**Badges de série courants :**

- "On Fire 🔥" : 7 jours consécutifs
- "Unstoppable ⚡" : 30 jours consécutifs
- "Legend 👑" : 100 jours consécutifs

---

### 🏆 Système de Badges

#### Types de Critères de Déblocage

1. **Nombre d'activités** : "Faire 10 activités"
2. **Distance totale** : "Parcourir 100 km"
3. **Durée totale** : "Faire 500 minutes d'exercice"
4. **Série de jours** : "Faire du sport 7 jours d'affilée"
5. **Activité spécifique** : "Faire 5 séances de natation"
6. **Critères combinés** : "Faire 10 activités ET parcourir 50 km"

#### Processus de Déblocage

Quand un utilisateur enregistre une activité :

1. Le système vérifie TOUS les badges disponibles
2. Pour chaque badge non obtenu, il vérifie les critères
3. Si les critères sont remplis → Badge débloqué !
4. XP bonus attribué automatiquement
5. Notification envoyée à l'utilisateur

---

### 🎯 Système de Challenges

Les challenges sont des objectifs temporaires qui motivent à court terme.

#### Attribution Automatique

- **Quotidiens** : Créés automatiquement chaque jour à minuit
- **Hebdomadaires** : Créés chaque lundi
- **Mensuels** : Créés le 1er de chaque mois
- **Événementiels** : Créés manuellement pour des événements spéciaux

#### Progression

À chaque activité enregistrée, le système met à jour automatiquement :

- Les challenges de type "compléter X activités"
- Les challenges de type "atteindre X kilomètres"
- Les challenges de type "atteindre X minutes"
- Les challenges d'activité spécifique

**Quand un challenge est complété :**

- XP bonus attribué
- Notification de félicitations
- Le challenge disparaît de la liste active (mais reste dans l'historique)

---

## 📝 Structures de Données Kotlin

### Modèles de Données Principaux

```kotlin
// AchievementSummary.kt
data class AchievementSummary(
    val level: LevelInfo,
    val stats: StatsInfo
)

data class LevelInfo(
    val currentLevel: Int,
    val totalXp: Int,
    val xpForNextLevel: Int,
    val currentLevelXp: Int,
    val progressPercentage: Double
)

data class StatsInfo(
    val totalBadges: Int,
    val currentStreak: Int,
    val bestStreak: Int
)

// Badges.kt
data class BadgesResponse(
    val earnedBadges: List<EarnedBadge>,
    val inProgress: List<BadgeProgress>
)

data class EarnedBadge(
    val _id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val rarity: String,
    val category: String,
    val earnedAt: String
)

data class BadgeProgress(
    val badge: BadgeInfo,
    val currentProgress: Int,
    val target: Int,
    val percentage: Double
)

data class BadgeInfo(
    val _id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val rarity: String,
    val category: String
)

// Challenges.kt
data class ChallengesResponse(
    val activeChallenges: List<ActiveChallenge>
)

data class ActiveChallenge(
    val _id: String,
    val name: String,
    val description: String,
    val challengeType: String,
    val xpReward: Int,
    val currentProgress: Int,
    val target: Int,
    val daysLeft: Int,
    val expiresAt: String
)

// Leaderboard.kt
data class LeaderboardResponse(
    val currentUser: CurrentUserLeaderboard?,
    val leaderboard: List<LeaderboardEntry>,
    val page: Int,
    val totalPages: Int
)

data class CurrentUserLeaderboard(
    val rank: Int,
    val username: String,
    val totalXp: Int,
    val isCurrentUser: Boolean
)

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val totalXp: Int,
    val medal: String? = null
)

// UiState.kt
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

---

## 🌐 Configuration Retrofit

### Gestion du Token

```kotlin
// TokenManager.kt
interface TokenManager {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()
}

// TokenManagerImpl.kt
class TokenManagerImpl(
    private val sharedPreferences: SharedPreferences
) : TokenManager {
    
    companion object {
        private const val PREF_TOKEN_KEY = "auth_token"
    }
    
    override fun getToken(): String? {
        return sharedPreferences.getString(PREF_TOKEN_KEY, null)
    }
    
    override fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(PREF_TOKEN_KEY, token)
            .apply()
    }
    
    override fun clearToken() {
        sharedPreferences.edit()
            .remove(PREF_TOKEN_KEY)
            .apply()
    }
}
```

### Configuration Retrofit

```kotlin
// ApiModule.kt
object ApiModule {
    private const val BASE_URL = "https://apinest-production.up.railway.app/"
    
    fun createRetrofit(tokenManager: TokenManager): Retrofit {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val token = tokenManager.getToken()
                
                val request = original.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .apply {
                        token?.let {
                            addHeader("Authorization", "Bearer $it")
                        }
                    }
                    .build()
                
                val response = chain.proceed(request)
                
                // Gérer le token expiré
                if (response.code == 401) {
                    tokenManager.clearToken()
                    // Émettre un événement pour forcer la reconnexion
                }
                
                response
            }
            .build()
        
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

### Interface API

```kotlin
// AchievementsApi.kt
interface AchievementsApi {
    @GET("achievements/summary")
    suspend fun getSummary(): Response<AchievementSummary>
    
    @GET("achievements/badges")
    suspend fun getBadges(): Response<BadgesResponse>
    
    @GET("achievements/challenges")
    suspend fun getChallenges(): Response<ChallengesResponse>
    
    @GET("achievements/leaderboard")
    suspend fun getLeaderboard(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<LeaderboardResponse>
}
```

---

## 🏗️ Repository avec StateFlow

```kotlin
// AchievementsRepository.kt
class AchievementsRepository(
    private val api: AchievementsApi
) {
    private val _summaryState = MutableStateFlow<UiState<AchievementSummary>>(UiState.Idle)
    val summaryState: StateFlow<UiState<AchievementSummary>> = _summaryState.asStateFlow()
    
    private val _badgesState = MutableStateFlow<UiState<BadgesResponse>>(UiState.Idle)
    val badgesState: StateFlow<UiState<BadgesResponse>> = _badgesState.asStateFlow()
    
    private val _challengesState = MutableStateFlow<UiState<ChallengesResponse>>(UiState.Idle)
    val challengesState: StateFlow<UiState<ChallengesResponse>> = _challengesState.asStateFlow()
    
    private val _leaderboardState = MutableStateFlow<UiState<LeaderboardResponse>>(UiState.Idle)
    val leaderboardState: StateFlow<UiState<LeaderboardResponse>> = _leaderboardState.asStateFlow()
    
    suspend fun fetchSummary() {
        _summaryState.value = UiState.Loading
        try {
            val response = api.getSummary()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    _summaryState.value = UiState.Success(it)
                } ?: run {
                    _summaryState.value = UiState.Error("Réponse vide")
                }
            } else {
                _summaryState.value = UiState.Error(handleError(response.code()))
            }
        } catch (e: Exception) {
            _summaryState.value = UiState.Error(ErrorHandler.handleError(e))
        }
    }
    
    suspend fun fetchBadges() {
        _summaryState.value = UiState.Loading
        try {
            val response = api.getBadges()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    _badgesState.value = UiState.Success(it)
                } ?: run {
                    _badgesState.value = UiState.Error("Réponse vide")
                }
            } else {
                _badgesState.value = UiState.Error(handleError(response.code()))
            }
        } catch (e: Exception) {
            _badgesState.value = UiState.Error(ErrorHandler.handleError(e))
        }
    }
    
    suspend fun fetchChallenges() {
        _challengesState.value = UiState.Loading
        try {
            val response = api.getChallenges()
            
            if (response.isSuccessful) {
                response.body()?.let {
                    _challengesState.value = UiState.Success(it)
                } ?: run {
                    _challengesState.value = UiState.Error("Réponse vide")
                }
            } else {
                _challengesState.value = UiState.Error(handleError(response.code()))
            }
        } catch (e: Exception) {
            _challengesState.value = UiState.Error(ErrorHandler.handleError(e))
        }
    }
    
    suspend fun fetchLeaderboard(page: Int = 1, limit: Int = 20) {
        _leaderboardState.value = UiState.Loading
        try {
            val response = api.getLeaderboard(page, limit)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    _leaderboardState.value = UiState.Success(it)
                } ?: run {
                    _leaderboardState.value = UiState.Error("Réponse vide")
                }
            } else {
                _leaderboardState.value = UiState.Error(handleError(response.code()))
            }
        } catch (e: Exception) {
            _leaderboardState.value = UiState.Error(ErrorHandler.handleError(e))
        }
    }
    
    private fun handleError(code: Int): String {
        return when (code) {
            401 -> "Session expirée, veuillez vous reconnecter"
            403 -> "Accès refusé"
            404 -> "Ressource non trouvée"
            500 -> "Erreur serveur, veuillez réessayer plus tard"
            else -> "Erreur: Code $code"
        }
    }
}

// ErrorHandler.kt
object ErrorHandler {
    fun handleError(error: Throwable): String {
        return when (error) {
            is HttpException -> {
                when (error.code()) {
                    401 -> "Session expirée, veuillez vous reconnecter"
                    403 -> "Accès refusé"
                    404 -> "Ressource non trouvée"
                    500 -> "Erreur serveur, veuillez réessayer plus tard"
                    else -> "Erreur: ${error.message()}"
                }
            }
            is SocketTimeoutException -> "Délai d'attente dépassé, vérifiez votre connexion"
            is IOException -> "Problème de connexion réseau"
            else -> error.message ?: "Erreur inconnue"
        }
    }
}
```

---

## 🎨 ViewModels

```kotlin
// AchievementsViewModel.kt
@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val repository: AchievementsRepository
) : ViewModel() {
    
    val summaryState = repository.summaryState
    val badgesState = repository.badgesState
    val challengesState = repository.challengesState
    val leaderboardState = repository.leaderboardState
    
    init {
        loadAllData()
    }
    
    fun loadAllData() {
        viewModelScope.launch {
            launch { repository.fetchSummary() }
            launch { repository.fetchBadges() }
            launch { repository.fetchChallenges() }
            launch { repository.fetchLeaderboard() }
        }
    }
    
    fun refreshSummary() {
        viewModelScope.launch {
            repository.fetchSummary()
        }
    }
    
    fun refreshBadges() {
        viewModelScope.launch {
            repository.fetchBadges()
        }
    }
    
    fun refreshChallenges() {
        viewModelScope.launch {
            repository.fetchChallenges()
        }
    }
    
    fun refreshLeaderboard(page: Int = 1) {
        viewModelScope.launch {
            repository.fetchLeaderboard(page)
        }
    }
}
```

---

## 🖼️ Composables Jetpack Compose

### Écran de Résumé des Achievements

```kotlin
// AchievementsSummaryScreen.kt
@Composable
fun AchievementsSummaryScreen(
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val summaryState by viewModel.summaryState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (summaryState) {
            is UiState.Loading -> {
                CircularProgressIndicator()
            }
            is UiState.Success -> {
                val summary = summaryState.data
                
                // Affichage du niveau
                LevelCard(levelInfo = summary.level)
                
                // Affichage des statistiques
                StatsCard(stats = summary.stats)
            }
            is UiState.Error -> {
                Text(
                    text = "Erreur: ${summaryState.message}",
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { viewModel.refreshSummary() }) {
                    Text("Réessayer")
                }
            }
            is UiState.Idle -> {
                Text("Chargement...")
            }
        }
    }
}

@Composable
fun LevelCard(levelInfo: LevelInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Niveau ${levelInfo.currentLevel}",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${levelInfo.totalXp} XP",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barre de progression
            LinearProgressIndicator(
                progress = { levelInfo.progressPercentage / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${levelInfo.currentLevelXp} / ${levelInfo.xpForNextLevel} XP",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun StatsCard(stats: StatsInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Badges", stats.totalBadges.toString())
            StatItem("Série actuelle", stats.currentStreak.toString())
            StatItem("Meilleure série", stats.bestStreak.toString())
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
```

### Écran des Badges

```kotlin
// BadgesScreen.kt
@Composable
fun BadgesScreen(
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val badgesState by viewModel.badgesState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Badges") },
                actions = {
                    IconButton(onClick = { viewModel.refreshBadges() }) {
                        Icon(Icons.Default.Refresh, "Actualiser")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (badgesState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val badges = badgesState.data
                    
                    // Badges obtenus
                    if (badges.earnedBadges.isNotEmpty()) {
                        Text(
                            text = "Badges obtenus (${badges.earnedBadges.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(badges.earnedBadges) { badge ->
                                EarnedBadgeItem(badge = badge)
                            }
                        }
                    }
                    
                    if (badges.earnedBadges.isNotEmpty() && badges.inProgress.isNotEmpty()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 2.dp
                        )
                    }
                    
                    // Badges en cours
                    if (badges.inProgress.isNotEmpty()) {
                        Text(
                            text = "En cours (${badges.inProgress.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(badges.inProgress) { progress ->
                                BadgeProgressItem(progress = progress)
                            }
                        }
                    }
                    
                    if (badges.earnedBadges.isEmpty() && badges.inProgress.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Aucun badge pour le moment",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Complétez des activités pour débloquer des badges !",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorContent(
                        message = badgesState.message,
                        onRetry = { viewModel.refreshBadges() }
                    )
                }
                is UiState.Idle -> {
                    Text("Chargement...")
                }
            }
        }
    }
}

@Composable
fun EarnedBadgeItem(badge: EarnedBadge) {
    val rarityColor = getRarityColor(badge.rarity)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = rarityColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = badge.iconUrl,
                contentDescription = badge.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit,
                error = {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = badge.name,
                        modifier = Modifier.size(64.dp)
                    )
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Obtenu le ${formatDate(badge.earnedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            BadgeChip(rarity = badge.rarity, rarityColor = rarityColor)
        }
    }
}

@Composable
fun BadgeProgressItem(progress: BadgeProgress) {
    val rarityColor = getRarityColor(progress.badge.rarity)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = progress.badge.iconUrl,
                    contentDescription = progress.badge.name,
                    modifier = Modifier
                        .size(64.dp)
                        .alpha(0.6f),
                    contentScale = ContentScale.Fit,
                    error = {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = progress.badge.name,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = progress.badge.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = progress.badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                BadgeChip(rarity = progress.badge.rarity, rarityColor = rarityColor)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress.percentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = rarityColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${progress.currentProgress} / ${progress.target}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${progress.percentage.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = rarityColor
                )
            }
        }
    }
}

@Composable
fun BadgeChip(rarity: String, rarityColor: Color) {
    val displayName = when (rarity.lowercase()) {
        "common" -> "Commun"
        "uncommon" -> "Peu commun"
        "rare" -> "Rare"
        "epic" -> "Épique"
        "legendary" -> "Légendaire"
        else -> rarity
    }
    
    Surface(
        color = rarityColor.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, rarityColor)
    ) {
        Text(
            text = displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = rarityColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun getRarityColor(rarity: String): Color {
    return when (rarity.lowercase()) {
        "common" -> Color(0xFF808080)
        "uncommon" -> Color(0xFF4CAF50)
        "rare" -> Color(0xFF2196F3)
        "epic" -> Color(0xFF9C27B0)
        "legendary" -> Color(0xFFFF9800)
        else -> Color(0xFF808080)
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString
        outputFormat.format(date)
    } catch (e: Exception) {
        dateString
    }
}
```

### Écran des Challenges

```kotlin
// ChallengesScreen.kt
@Composable
fun ChallengesScreen(
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val challengesState by viewModel.challengesState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Défis") },
                actions = {
                    IconButton(onClick = { viewModel.refreshChallenges() }) {
                        Icon(Icons.Default.Refresh, "Actualiser")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (challengesState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val challenges = challengesState.data
                    
                    if (challenges.activeChallenges.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.StarOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Aucun défi actif",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "De nouveaux défis apparaîtront bientôt !",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(challenges.activeChallenges) { challenge ->
                                ChallengeItem(challenge = challenge)
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorContent(
                        message = challengesState.message,
                        onRetry = { viewModel.refreshChallenges() }
                    )
                }
                is UiState.Idle -> {
                    Text("Chargement...")
                }
            }
        }
    }
}

@Composable
fun ChallengeItem(challenge: ActiveChallenge) {
    val progressPercentage = (challenge.currentProgress.toFloat() / challenge.target)
    val isCompleted = challenge.currentProgress >= challenge.target
    val isUrgent = challenge.daysLeft <= 1
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isUrgent && !isCompleted) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.error)
        } else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = challenge.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        ChallengeTypeChip(type = challenge.challengeType)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = challenge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+${challenge.xpReward} XP",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Complété",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Défi complété !",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                LinearProgressIndicator(
                    progress = { progressPercentage },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isUrgent) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${challenge.currentProgress} / ${challenge.target}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isUrgent) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                        )
                        Text(
                            text = "${challenge.daysLeft} ${if (challenge.daysLeft > 1) "jours" else "jour"} restant${if (challenge.daysLeft > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isUrgent) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                            fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChallengeTypeChip(type: String) {
    val (color, displayName) = when (type.lowercase()) {
        "daily" -> Color(0xFF4CAF50) to "Quotidien"
        "weekly" -> Color(0xFF2196F3) to "Hebdomadaire"
        "monthly" -> Color(0xFF9C27B0) to "Mensuel"
        "event" -> Color(0xFFFF9800) to "Événement"
        else -> Color(0xFF808080) to type
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
```

### Écran du Leaderboard

```kotlin
// LeaderboardScreen.kt
@Composable
fun LeaderboardScreen(
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val leaderboardState by viewModel.leaderboardState.collectAsState()
    var currentPage by remember { mutableIntStateOf(1) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Classement") },
                actions = {
                    IconButton(onClick = { 
                        currentPage = 1
                        viewModel.refreshLeaderboard(currentPage) 
                    }) {
                        Icon(Icons.Default.Refresh, "Actualiser")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (leaderboardState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val leaderboard = leaderboardState.data
                    
                    // Position de l'utilisateur actuel
                    leaderboard.currentUser?.let { currentUser ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${currentUser.rank}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = currentUser.username,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = "${currentUser.totalXp} XP",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                if (currentUser.rank <= 3) {
                                    Text(
                                        text = when (currentUser.rank) {
                                            1 -> "🥇"
                                            2 -> "🥈"
                                            3 -> "🥉"
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.headlineLarge
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Liste du classement
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(leaderboard.leaderboard) { entry ->
                            LeaderboardEntryItem(entry = entry)
                        }
                        
                        // Pagination
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentPage > 1) {
                                            currentPage--
                                            viewModel.refreshLeaderboard(currentPage)
                                        }
                                    },
                                    enabled = currentPage > 1
                                ) {
                                    Icon(Icons.Default.ArrowBack, "Page précédente")
                                }
                                
                                Text(
                                    text = "Page $currentPage / ${leaderboard.totalPages}",
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                
                                IconButton(
                                    onClick = {
                                        if (currentPage < leaderboard.totalPages) {
                                            currentPage++
                                            viewModel.refreshLeaderboard(currentPage)
                                        }
                                    },
                                    enabled = currentPage < leaderboard.totalPages
                                ) {
                                    Icon(Icons.Default.ArrowForward, "Page suivante")
                                }
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    ErrorContent(
                        message = leaderboardState.message,
                        onRetry = { viewModel.refreshLeaderboard(currentPage) }
                    )
                }
                is UiState.Idle -> {
                    Text("Chargement...")
                }
            }
        }
    }
}

@Composable
fun LeaderboardEntryItem(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(48.dp)
                )
                entry.medal?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                Text(
                    text = entry.username,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = "${entry.totalXp} XP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Erreur",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Réessayer")
        }
    }
}
```

**Imports nécessaires :**

```kotlin
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
```

---

## ⚠️ Gestion des Erreurs

### Codes d'Erreur HTTP

| Code | Signification | Action Recommandée |
|------|---------------|-------------------|
| 401 | Non autorisé (token expiré) | Rediriger vers l'écran de connexion |
| 403 | Accès refusé | Afficher un message d'erreur |
| 404 | Ressource non trouvée | Afficher un message informatif |
| 500 | Erreur serveur | Proposer de réessayer plus tard |

### Gestion des Erreurs Réseau

```kotlin
catch (e: SocketTimeoutException) {
    _state.value = UiState.Error("Délai d'attente dépassé")
}
catch (e: IOException) {
    _state.value = UiState.Error("Problème de connexion réseau")
}
catch (e: Exception) {
    _state.value = UiState.Error("Erreur inconnue: ${e.message}")
}
```

---

## 📚 Ressources Supplémentaires

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Jetpack Compose State](https://developer.android.com/jetpack/compose/state)
- [StateFlow Documentation](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/)
- [Material Design 3](https://m3.material.io/)

---

**Dernière mise à jour :** 2025-01-20

