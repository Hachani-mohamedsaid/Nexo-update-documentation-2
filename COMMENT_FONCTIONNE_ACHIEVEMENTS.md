# 📚 Comment Fonctionne le Système d'Achievements

## 🏗️ Architecture Générale

Le système d'Achievements suit une architecture **Clean Architecture** avec 3 couches principales :

```
┌─────────────────────────────────────────────────┐
│  PRESENTATION LAYER (UI/ViewModel)              │
│  - AchievementsScreen.kt                        │
│  - AchievementsViewModel.kt                     │
└─────────────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────────────┐
│  DOMAIN LAYER (Business Logic)                  │
│  - AchievementsRepository (interface)           │
│  - Domain Models                                 │
└─────────────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────────────┐
│  DATA LAYER (API/Database)                      │
│  - AchievementsRepositoryImpl                   │
│  - AchievementsRemoteDataSourceImpl             │
│  - AchievementsApiService                       │
└─────────────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────────────┐
│  API BACKEND                                     │
│  https://apinest-production.up.railway.app/     │
└─────────────────────────────────────────────────┘
```

---

## 🔄 Flux de Données Complet

### 1. **Initialisation**

Quand l'utilisateur ouvre l'écran Achievements :

```
1. AchievementsScreen est créé
2. AchievementsViewModel est initialisé
3. Le ViewModel appelle loadAllData() dans son init {}
```

### 2. **Chargement des Données (ViewModel)**

```kotlin
// Dans AchievementsViewModel.init {}
loadAllData() {
    // 1. Met l'état en Loading
    _uiState.update { it.copy(isLoading = true) }
    
    // 2. Charge TOUTES les données en PARALLÈLE via le Repository
    val summaryResult = repository.getSummary()
    val badgesResult = repository.getBadges()
    val challengesResult = repository.getChallenges()
    val leaderboardResult = repository.getLeaderboard()
    
    // 3. Combine les résultats dans un AchievementsOverview
    val overview = AchievementsOverview(
        stats = summary.stats,
        badges = badges,
        challenges = challenges,
        leaderboard = leaderboard
    )
    
    // 4. Met à jour l'état UI
    _uiState.update { 
        it.copy(
            isLoading = false,
            overview = overview,
            error = null
        )
    }
}
```

### 3. **Repository Pattern**

Le Repository agit comme une couche d'abstraction :

```kotlin
// AchievementsRepositoryImpl
override suspend fun getSummary(): AchievementsOverview {
    // Appelle le DataSource
    val dto = remoteDataSource.fetchSummary()
    // Convertit DTO → Domain Model
    return dto.toDomain()
}
```

### 4. **Data Source (API Calls)**

Le DataSource fait les appels API réels :

```kotlin
// AchievementsRemoteDataSourceImpl
override suspend fun fetchSummary(): AchievementsOverviewDto {
    // 1. Appel API via Retrofit
    val response = apiService.getSummary()
    
    // 2. Vérifie la réponse
    if (response.isSuccessful && response.body() != null) {
        // 3. Convertit API DTO → Data DTO
        return response.body()!!.toDataOverview()
    } else {
        throw Exception("Erreur API")
    }
}
```

### 5. **API Service (Retrofit)**

L'interface Retrofit définit les endpoints :

```kotlin
interface AchievementsApiService {
    @GET("achievements/summary")
    suspend fun getSummary(): Response<AchievementSummaryDto>
    
    @GET("achievements/badges")
    suspend fun getBadges(): Response<BadgesResponseDto>
    
    @GET("achievements/challenges")
    suspend fun getChallenges(): Response<ChallengesResponseDto>
    
    @GET("achievements/leaderboard")
    suspend fun getLeaderboard(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<LeaderboardResponseDto>
}
```

**🔐 Authentification automatique :**
Le token JWT est ajouté automatiquement via `AuthInterceptor` dans `RetrofitClient.kt`

---

## 📊 Les 4 Endpoints API

### 1. **GET /achievements/summary**

**Retourne :**
- `level` : Niveau actuel, XP total, XP pour le prochain niveau, pourcentage de progression
- `stats` : Nombre total de badges, série actuelle, meilleure série

**Exemple de réponse :**
```json
{
  "level": {
    "currentLevel": 1,
    "totalXp": 0,
    "xpForNextLevel": 150,
    "currentLevelXp": 0,
    "progressPercentage": 0
  },
  "stats": {
    "totalBadges": 0,
    "currentStreak": 0,
    "bestStreak": 0
  }
}
```

### 2. **GET /achievements/badges**

**Retourne :**
- `earnedBadges` : Liste des badges déjà obtenus
- `inProgress` : Liste des badges en cours de progression

**Exemple de réponse :**
```json
{
  "earnedBadges": [
    {
      "_id": "badge123",
      "name": "Premier Pas",
      "description": "Compléter votre première activité",
      "iconUrl": "https://example.com/badge1.png",
      "rarity": "common",
      "category": "activity",
      "earnedAt": "2025-01-15T10:30:00Z"
    }
  ],
  "inProgress": [
    {
      "badge": {
        "_id": "badge456",
        "name": "Marathonien",
        "description": "Compléter 10 activités",
        "iconUrl": "https://example.com/badge2.png",
        "rarity": "rare",
        "category": "milestone"
      },
      "currentProgress": 7,
      "target": 10,
      "percentage": 70.0
    }
  ]
}
```

### 3. **GET /achievements/challenges**

**Retourne :**
- `activeChallenges` : Liste des défis actifs avec progression

**Exemple de réponse :**
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
      "expiresAt": "2025-01-20T23:59:59Z"
    }
  ]
}
```

### 4. **GET /achievements/leaderboard**

**Retourne :**
- `currentUser` : Position de l'utilisateur actuel
- `leaderboard` : Liste des utilisateurs classés
- `page` et `totalPages` : Pagination

**Exemple de réponse :**
```json
{
  "currentUser": {
    "rank": 1,
    "username": "Mohamed",
    "totalXp": 0,
    "isCurrentUser": true
  },
  "leaderboard": [
    {
      "rank": 1,
      "username": "Neji Hachani",
      "totalXp": 0,
      "medal": "🥇"
    }
  ],
  "page": 1,
  "totalPages": 1
}
```

---

## 🗺️ Transformation des Données (Mapping)

Le système utilise plusieurs couches de mapping :

### **API DTO → Data DTO**

```kotlin
// Dans AchievementsApiMapper.kt
fun AchievementSummaryDto.toDataOverview(): AchievementsOverviewDto {
    // Convertit les DTOs de l'API vers les DTOs du data layer
    val stats = AchievementUserStatsDto(
        level = level.currentLevel,
        xp = level.totalXp,
        ...
    )
    return AchievementsOverviewDto(stats = stats, ...)
}
```

### **Data DTO → Domain Model**

```kotlin
// Dans AchievementsMapper.kt
fun AchievementsOverviewDto.toDomain(): AchievementsOverview {
    // Convertit les DTOs du data layer vers les domain models
    return AchievementsOverview(
        stats = stats.toDomain(),
        badges = badges.map { it.toDomain() },
        ...
    )
}
```

**Pourquoi 2 couches de mapping ?**
- Séparation claire entre API et données internes
- Flexibilité pour changer l'API sans affecter le domaine
- Respect de Clean Architecture

---

## 🎨 Affichage dans l'UI

### 1. **Collecte de l'État**

```kotlin
// Dans AchievementsRoute (Composable)
@Composable
fun AchievementsRoute(viewModel: AchievementsViewModel) {
    // Collecte l'état du ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    // Affiche l'écran
    AchievementsScreen(
        state = uiState,
        onRefresh = viewModel::refresh,
        ...
    )
}
```

### 2. **Gestion des États**

```kotlin
// Dans AchievementsScreen
when {
    state.isLoading -> LoadingState() // Affiche un loader
    state.error != null -> ErrorState() // Affiche une erreur
    else -> {
        // Affiche les données
        val overview = state.overview ?: sampleAchievementsOverview
        LegacyAchievementsContent(overview = overview, ...)
    }
}
```

### 3. **Affichage par Onglet**

L'écran a 3 onglets :
- **BADGES** : Affiche les badges obtenus et en cours
- **CHALLENGES** : Affiche les défis actifs
- **LEADERBOARD** : Affiche le classement

---

## 🔄 Gestion des États (StateFlow)

Le ViewModel utilise **StateFlow** pour gérer les états :

### **Ancien Système (rétrocompatibilité)**

```kotlin
private val _uiState = MutableStateFlow(AchievementsUiState(isLoading = true))
val uiState: StateFlow<AchievementsUiState> = _uiState
```

### **Nouveau Système (endpoints séparés)**

```kotlin
// StateFlow séparés pour chaque endpoint
val summaryState: StateFlow<UiState<AchievementsOverview>>
val badgesState: StateFlow<UiState<List<AchievementBadge>>>
val challengesState: StateFlow<UiState<List<AchievementChallenge>>>
val leaderboardState: StateFlow<UiState<List<LeaderboardEntry>>>
```

**Avantages du nouveau système :**
- Chargement indépendant de chaque endpoint
- Meilleure gestion des erreurs par endpoint
- Possibilité de rafraîchir seulement un endpoint spécifique

---

## 🔁 Cycle de Vie

```
1. USER Ouvre l'écran Achievements
   ↓
2. AchievementsViewModel.init {}
   ↓
3. loadAllData() appelé automatiquement
   ↓
4. 4 appels API en parallèle :
   - getSummary()
   - getBadges()
   - getChallenges()
   - getLeaderboard()
   ↓
5. Mapping des données (API → Data → Domain)
   ↓
6. Mise à jour des StateFlow
   ↓
7. UI se met à jour automatiquement (recomposition Compose)
   ↓
8. USER voit les données affichées
```

---

## 🚀 Actions Utilisateur

### **Rafraîchir les données**

```kotlin
// L'utilisateur peut tirer vers le bas pour rafraîchir
fun refresh() {
    loadAllData() // Recharge toutes les données
}
```

### **Changer d'onglet**

```kotlin
fun onTabSelected(tab: AchievementsTab) {
    _uiState.update { it.copy(selectedTab = tab) }
    // L'UI change automatiquement grâce à la recomposition
}
```

### **Rafraîchir un endpoint spécifique**

```kotlin
// Pour rafraîchir uniquement les badges
viewModel.fetchBadges()

// Pour rafraîchir uniquement le leaderboard
viewModel.fetchLeaderboard(page = 1, limit = 20)
```

---

## 📝 Résumé en 5 Points

1. **Initialisation** : Le ViewModel charge automatiquement toutes les données au démarrage
2. **Appels API** : 4 endpoints appelés en parallèle pour optimiser les performances
3. **Mapping** : Transformation des données API → Data → Domain
4. **État UI** : StateFlow permet à l'UI de se mettre à jour automatiquement
5. **Affichage** : 3 onglets (Badges, Challenges, Leaderboard) avec gestion d'état

---

## 🔍 Points Clés à Retenir

✅ **Clean Architecture** : Séparation claire des responsabilités  
✅ **Parallélisation** : Les 4 appels API sont faits en même temps  
✅ **Gestion d'erreurs** : Try/catch à chaque niveau  
✅ **Réactivité** : StateFlow + Compose = UI automatiquement mise à jour  
✅ **Authentification** : JWT ajouté automatiquement via interceptor  
✅ **Mapping** : 2 couches (API → Data → Domain) pour la flexibilité  

---

**Dernière mise à jour** : 2025-01-20

