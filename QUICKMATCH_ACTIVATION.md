# 📱 Guide d'Activation de QuickMatch - Jetpack Compose + NestJS

## 🔄 Flux d'Activation de la Page QuickMatch

### 1. **Point d'Entrée : Page Home Feed**

La page QuickMatch est activée depuis la page **Home Feed** via une carte "Quick Match".

**Fichier :** `app/src/main/java/com/example/damandroid/presentation/homefeed/ui/components/HomeFeedComponents.kt`

```kotlin
// Ligne 291-303
if (onQuickMatchClick != null) {
    QuickMatchCard(
        onClick = onQuickMatchClick,  // ← Clic sur la carte
        appColors = appColors,
        // ... autres paramètres
    )
}
```

### 2. **Navigation dans MainActivity**

**Fichier :** `app/src/main/java/com/example/damandroid/MainActivity.kt`

```kotlin
// Ligne 428
onQuickMatchClick = { overlay = OverlayScreen.QuickMatch }
```

Quand l'utilisateur clique sur la carte QuickMatch :
- `onQuickMatchClick` est appelé
- `overlay` est défini à `OverlayScreen.QuickMatch`

### 3. **Affichage de l'Overlay**

**Fichier :** `app/src/main/java/com/example/damandroid/MainActivity.kt`

```kotlin
// Ligne 357-361
OverlayScreen.QuickMatch -> QuickMatchRoute(
    viewModel = quickMatchViewModel,
    onBack = { overlay = null },
    modifier = Modifier.fillMaxSize()
)
```

L'overlay QuickMatch s'affiche en plein écran.

### 4. **Chargement des Profils**

**Fichier :** `app/src/main/java/com/example/damandroid/presentation/quickmatch/viewmodel/QuickMatchViewModel.kt`

```kotlin
init {
    loadProfiles()  // ← Chargement automatique au démarrage
}

fun loadProfiles() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        runCatching { getQuickMatchProfiles() }
            .onSuccess { profiles ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profiles = profiles,
                        error = null
                    )
                }
            }
            // ...
    }
}
```

---

## 🔌 Connexion avec l'API Backend NestJS

### Étape 1 : Créer l'API Service pour QuickMatch

**Créer :** `app/src/main/java/com/example/damandroid/api/QuickMatchApiService.kt`

```kotlin
package com.example.damandroid.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body

/**
 * Interface Retrofit pour les endpoints QuickMatch
 */
interface QuickMatchApiService {
    
    /**
     * Récupérer les profils pour QuickMatch
     * GET /quick-match/profiles
     * 
     * Nécessite authentification JWT
     * Retourne une liste de profils utilisateurs compatibles
     */
    @GET("quick-match/profiles")
    suspend fun getProfiles(): Response<List<QuickMatchProfileResponse>>
    
    /**
     * Enregistrer un "like" (swipe right)
     * POST /quick-match/like
     * 
     * Nécessite authentification JWT
     */
    @POST("quick-match/like")
    suspend fun likeProfile(
        @Body request: LikeProfileRequest
    ): Response<LikeProfileResponse>
    
    /**
     * Enregistrer un "pass" (swipe left)
     * POST /quick-match/pass
     * 
     * Nécessite authentification JWT
     */
    @POST("quick-match/pass")
    suspend fun passProfile(
        @Body request: PassProfileRequest
    ): Response<Unit>
}

/**
 * Réponse d'un profil QuickMatch depuis l'API
 */
data class QuickMatchProfileResponse(
    val _id: String,
    val id: String? = null,
    val name: String,
    val age: Int,
    val avatarUrl: String?,
    val coverImageUrl: String?,
    val location: String,
    val distance: String?,
    val bio: String?,
    val sports: List<SportResponse>,
    val interests: List<String>?,
    val rating: Double?,
    val activitiesJoined: Int?
) {
    fun getProfileId(): String = id ?: _id
}

data class SportResponse(
    val name: String,
    val icon: String?,
    val level: String
)

data class LikeProfileRequest(
    val profileId: String
)

data class PassProfileRequest(
    val profileId: String
)

data class LikeProfileResponse(
    val isMatch: Boolean,
    val matchedProfile: QuickMatchProfileResponse?
)
```

### Étape 2 : Enregistrer le Service dans RetrofitClient

**Modifier :** `app/src/main/java/com/example/damandroid/api/RetrofitClient.kt`

```kotlin
// Ajouter après les autres services (ligne ~44)
val quickMatchApiService: QuickMatchApiService = retrofit.create(QuickMatchApiService::class.java)
```

### Étape 3 : Créer le Mapper

**Créer :** `app/src/main/java/com/example/damandroid/data/mapper/QuickMatchMapper.kt`

```kotlin
package com.example.damandroid.data.mapper

import com.example.damandroid.api.QuickMatchProfileResponse
import com.example.damandroid.api.SportResponse
import com.example.damandroid.data.model.MatchUserProfileDto
import com.example.damandroid.data.model.SportDto

/**
 * Convertit QuickMatchProfileResponse (API) vers MatchUserProfileDto
 */
fun QuickMatchProfileResponse.toMatchUserProfileDto(): MatchUserProfileDto {
    return MatchUserProfileDto(
        id = getProfileId(),
        name = name,
        age = age,
        avatarUrl = avatarUrl ?: "",
        coverImageUrl = coverImageUrl ?: "",
        location = location,
        distance = distance ?: "Unknown",
        bio = bio ?: "",
        sports = sports.map { it.toSportDto() },
        interests = interests ?: emptyList(),
        rating = rating ?: 0.0,
        activitiesJoined = activitiesJoined ?: 0
    )
}

fun SportResponse.toSportDto(): SportDto {
    return SportDto(
        name = name,
        icon = icon ?: "🏃",
        level = level
    )
}
```

### Étape 4 : Mettre à jour la Data Source

**Modifier :** `app/src/main/java/com/example/damandroid/data/datasource/QuickMatchRemoteDataSourceImpl.kt`

```kotlin
package com.example.damandroid.data.datasource

import com.example.damandroid.api.RetrofitClient
import com.example.damandroid.data.mapper.toMatchUserProfileDto
import com.example.damandroid.data.model.MatchUserProfileDto
import retrofit2.HttpException
import java.io.IOException

class QuickMatchRemoteDataSourceImpl : QuickMatchRemoteDataSource {
    
    private val quickMatchApiService = RetrofitClient.quickMatchApiService

    override suspend fun fetchProfiles(): List<MatchUserProfileDto> {
        return try {
            val response = quickMatchApiService.getProfiles()
            
            if (response.isSuccessful) {
                response.body()?.map { it.toMatchUserProfileDto() } ?: emptyList()
            } else {
                when (response.code()) {
                    401 -> throw Exception("Unauthorized: Please login again")
                    403 -> throw Exception("Forbidden: Access denied")
                    404 -> throw Exception("Profiles not found")
                    500 -> throw Exception("Server error: Please try again later")
                    else -> throw Exception("Failed to fetch profiles: ${response.code()}")
                }
            }
        } catch (e: HttpException) {
            throw Exception("Network error: ${e.message}")
        } catch (e: IOException) {
            throw Exception("Connection error: Please check your internet connection")
        } catch (e: Exception) {
            throw e
        }
    }
}
```

### Étape 5 : Ajouter les méthodes Like/Pass (optionnel)

**Modifier :** `app/src/main/java/com/example/damandroid/data/datasource/QuickMatchRemoteDataSource.kt`

```kotlin
package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.MatchUserProfileDto

interface QuickMatchRemoteDataSource {
    suspend fun fetchProfiles(): List<MatchUserProfileDto>
    suspend fun likeProfile(profileId: String): Boolean  // Retourne true si match
    suspend fun passProfile(profileId: String)
}
```

**Implémenter dans QuickMatchRemoteDataSourceImpl :**

```kotlin
override suspend fun likeProfile(profileId: String): Boolean {
    return try {
        val response = quickMatchApiService.likeProfile(
            LikeProfileRequest(profileId = profileId)
        )
        
        if (response.isSuccessful) {
            response.body()?.isMatch ?: false
        } else {
            throw Exception("Failed to like profile: ${response.code()}")
        }
    } catch (e: Exception) {
        throw Exception("Error liking profile: ${e.message}")
    }
}

override suspend fun passProfile(profileId: String) {
    try {
        val response = quickMatchApiService.passProfile(
            PassProfileRequest(profileId = profileId)
        )
        
        if (!response.isSuccessful) {
            throw Exception("Failed to pass profile: ${response.code()}")
        }
    } catch (e: Exception) {
        throw Exception("Error passing profile: ${e.message}")
    }
}
```

---

## 📋 Structure de l'API Backend NestJS

### Endpoint 1 : GET /quick-match/profiles

**Controller NestJS :**

```typescript
// quick-match.controller.ts
@Get('profiles')
@UseGuards(JwtAuthGuard)
async getProfiles(@Request() req) {
  const userId = req.user.id;
  
  // Récupérer les profils compatibles (exclure ceux déjà likés/passés)
  const profiles = await this.quickMatchService.getCompatibleProfiles(userId);
  
  return profiles.map(profile => ({
    _id: profile._id,
    id: profile._id,
    name: profile.name,
    age: profile.age,
    avatarUrl: profile.avatarUrl,
    coverImageUrl: profile.coverImageUrl,
    location: profile.location,
    distance: this.calculateDistance(userId, profile._id),
    bio: profile.bio,
    sports: profile.sports.map(sport => ({
      name: sport.name,
      icon: sport.icon,
      level: sport.level
    })),
    interests: profile.interests,
    rating: profile.rating,
    activitiesJoined: profile.activitiesJoined
  }));
}
```

### Endpoint 2 : POST /quick-match/like

```typescript
@Post('like')
@UseGuards(JwtAuthGuard)
async likeProfile(@Request() req, @Body() body: { profileId: string }) {
  const userId = req.user.id;
  const { profileId } = body;
  
  // Enregistrer le like
  await this.quickMatchService.likeProfile(userId, profileId);
  
  // Vérifier si c'est un match (l'autre utilisateur a aussi liké)
  const isMatch = await this.quickMatchService.checkMatch(userId, profileId);
  
  if (isMatch) {
    const matchedProfile = await this.userService.findById(profileId);
    return {
      isMatch: true,
      matchedProfile: matchedProfile
    };
  }
  
  return {
    isMatch: false,
    matchedProfile: null
  };
}
```

### Endpoint 3 : POST /quick-match/pass

```typescript
@Post('pass')
@UseGuards(JwtAuthGuard)
async passProfile(@Request() req, @Body() body: { profileId: string }) {
  const userId = req.user.id;
  const { profileId } = body;
  
  await this.quickMatchService.passProfile(userId, profileId);
  
  return { success: true };
}
```

---

## 🎯 Utilisation dans QuickMatchScreen

**Modifier :** `app/src/main/java/com/example/damandroid/presentation/quickmatch/ui/QuickMatchScreen.kt`

Pour utiliser les vraies données au lieu des données mockées :

```kotlin
// Ligne 133 - Remplacer :
val displayedProfiles = if (profiles.isNotEmpty()) profiles else sampleQuickMatchProfiles

// Par :
val displayedProfiles = profiles  // Utiliser uniquement les données de l'API
```

Pour enregistrer les likes/passes :

```kotlin
// Dans SwipeCard, ligne 196-204
SwipeDirection.RIGHT -> {
    // Appeler l'API pour enregistrer le like
    viewModelScope.launch {
        val isMatch = viewModel.likeProfile(currentProfile.id)
        if (isMatch) {
            matches = matches + currentProfile.id
            matchedUser = currentProfile
            showMatch = true
        }
    }
    currentIndex++
}
SwipeDirection.LEFT -> {
    // Appeler l'API pour enregistrer le pass
    viewModelScope.launch {
        viewModel.passProfile(currentProfile.id)
    }
    currentIndex++
}
```

---

## ✅ Checklist d'Implémentation

- [x] Page QuickMatch déjà créée et fonctionnelle
- [x] Navigation depuis Home Feed configurée
- [ ] Créer `QuickMatchApiService.kt`
- [ ] Enregistrer le service dans `RetrofitClient.kt`
- [ ] Créer le mapper `QuickMatchMapper.kt`
- [ ] Mettre à jour `QuickMatchRemoteDataSourceImpl.kt`
- [ ] Implémenter les endpoints dans NestJS
- [ ] Tester la connexion API
- [ ] Ajouter la gestion des likes/passes (optionnel)
- [ ] Gérer les erreurs réseau

---

## 📝 Notes Importantes

1. **Authentification** : Tous les endpoints nécessitent un token JWT (géré automatiquement par `AuthInterceptor`)

2. **Données Mockées** : Actuellement, `QuickMatchRemoteDataSourceImpl` retourne une liste vide. Les données mockées sont dans `QuickMatchScreen.kt` (ligne 954-1045)

3. **Match** : Quand deux utilisateurs se "likent" mutuellement, c'est un match. Le backend doit vérifier cela.

4. **Distance** : Le backend doit calculer la distance entre l'utilisateur et chaque profil basé sur leurs localisations GPS.

5. **Filtres** : Le backend peut filtrer les profils selon :
   - Sports en commun
   - Distance maximale
   - Âge
   - Niveau de compétence

