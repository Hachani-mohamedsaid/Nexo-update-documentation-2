# Guide de Démarrage Rapide - Achievements Android Kotlin Jetpack Compose

## 🚀 Démarrage Rapide

Ce guide vous permet d'intégrer rapidement le système d'achievements dans votre application Android.

---

## 📦 Configuration Initiale

### 1. Ajoutez les dépendances dans `build.gradle.kts`

```kotlin
dependencies {
    // Réseau
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // ViewModel & Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Coil (Images)
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
}
```

---

## 🔧 Configuration API

### 2. Créez les modèles de données

```kotlin
// data/models/AchievementSummary.kt
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

// data/models/Badge.kt
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
```

### 3. Configurez Retrofit

```kotlin
// network/ApiModule.kt
object ApiModule {
    private const val BASE_URL = "https://apinest-production.up.railway.app/"
    
    fun createRetrofit(tokenManager: TokenManager): Retrofit {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer ${tokenManager.getToken() ?: ""}")
                    .build()
                chain.proceed(request)
            }
            .build()
        
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

// network/AchievementsApi.kt
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

## 🏗️ Architecture

### 4. Créez le Repository

```kotlin
// data/repository/AchievementsRepository.kt
class AchievementsRepository(
    private val api: AchievementsApi
) {
    private val _summaryState = MutableStateFlow<UiState<AchievementSummary>>(UiState.Idle)
    val summaryState: StateFlow<UiState<AchievementSummary>> = _summaryState.asStateFlow()
    
    suspend fun fetchSummary() {
        _summaryState.value = UiState.Loading
        try {
            val response = api.getSummary()
            if (response.isSuccessful) {
                response.body()?.let {
                    _summaryState.value = UiState.Success(it)
                }
            }
        } catch (e: Exception) {
            _summaryState.value = UiState.Error(e.message ?: "Erreur")
        }
    }
}

// utils/UiState.kt
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

### 5. Créez le ViewModel

```kotlin
// ui/viewmodel/AchievementsViewModel.kt
@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val repository: AchievementsRepository
) : ViewModel() {
    
    val summaryState = repository.summaryState
    
    init {
        loadSummary()
    }
    
    fun loadSummary() {
        viewModelScope.launch {
            repository.fetchSummary()
        }
    }
}
```

---

## 🎨 Interface Utilisateur

### 6. Créez l'écran principal

```kotlin
// ui/screens/AchievementsScreen.kt
@Composable
fun AchievementsScreen(
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val summaryState by viewModel.summaryState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (summaryState) {
            is UiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
            is UiState.Success -> {
                val summary = summaryState.data
                
                // Carte de niveau
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Niveau ${summary.level.currentLevel}",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${summary.level.totalXp} XP",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { summary.level.progressPercentage / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Statistiques
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("Badges", summary.stats.totalBadges.toString())
                    StatCard("Série", "${summary.stats.currentStreak} jours")
                }
            }
            is UiState.Error -> {
                Text(
                    text = "Erreur: ${summaryState.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.weight(1f),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
```

---

## 📱 Utilisation dans MainActivity

```kotlin
// MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAppTheme {
                AchievementsScreen()
            }
        }
    }
}
```

---

## 🔐 Gestion du Token

```kotlin
// auth/TokenManager.kt
interface TokenManager {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clearToken()
}

// Implémentation avec SharedPreferences
class TokenManagerImpl(
    private val sharedPreferences: SharedPreferences
) : TokenManager {
    
    override fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }
    
    override fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString("auth_token", token)
            .apply()
    }
    
    override fun clearToken() {
        sharedPreferences.edit()
            .remove("auth_token")
            .apply()
    }
}
```

---

## ✅ Checklist d'Implémentation

- [ ] Ajouter les dépendances Gradle
- [ ] Créer les modèles de données
- [ ] Configurer Retrofit avec l'API
- [ ] Implémenter le Repository avec StateFlow
- [ ] Créer le ViewModel avec Hilt
- [ ] Créer les écrans Compose
- [ ] Gérer le stockage du token JWT
- [ ] Ajouter la gestion d'erreurs
- [ ] Tester les endpoints

---

## 📚 Documentation Complète

Pour une documentation complète avec tous les détails, exemples avancés et bonnes pratiques, consultez :

- **[ANDROID_ACHIEVEMENTS_API_GUIDE.md](./ANDROID_ACHIEVEMENTS_API_GUIDE.md)** - Guide complet et détaillé

---

## 🎯 Endpoints Principaux

| Endpoint | Description |
|----------|-------------|
| `GET /achievements/summary` | Résumé des achievements (niveau, XP, badges, série) |
| `GET /achievements/badges` | Liste des badges obtenus et en progression |
| `GET /achievements/challenges` | Challenges actifs de l'utilisateur |
| `GET /achievements/leaderboard` | Classement avec pagination |

**Tous les endpoints nécessitent l'authentification JWT :**

```
Authorization: Bearer <access_token>
```

---

## 🚨 Erreurs Courantes

### Token expiré (401)

```kotlin
if (response.code == 401) {
    // Rediriger vers l'écran de connexion
    tokenManager.clearToken()
}
```

### Pas de connexion réseau

```kotlin
catch (e: IOException) {
    _state.value = UiState.Error("Vérifiez votre connexion internet")
}
```

---

## 💡 Astuces

1. **Pull-to-Refresh** : Utilisez `SwipeRefresh` pour rafraîchir les données
2. **Cache** : Considérez la mise en cache pour améliorer les performances
3. **Pagination** : Pour le leaderboard, utilisez la pagination
4. **Notifications** : Affichez des notifications quand un badge est débloqué

---

**Bon développement ! 🎉**

Pour plus de détails, consultez le [guide complet](./ANDROID_ACHIEVEMENTS_API_GUIDE.md).

