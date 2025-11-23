# 📋 DTOs NestJS pour QuickMatch

## DTOs à créer dans votre projet NestJS

### 1. LikeProfileDto

**Créer :** `quick-match/dto/like-profile.dto.ts`

```typescript
import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class LikeProfileDto {
  @ApiProperty({
    description: 'ID du profil à liker',
    example: '507f1f77bcf86cd799439011',
  })
  @IsString()
  @IsNotEmpty()
  profileId: string;
}
```

### 2. PassProfileDto

**Créer :** `quick-match/dto/pass-profile.dto.ts`

```typescript
import { IsString, IsNotEmpty } from 'class-validator';
import { ApiProperty } from '@nestjs/swagger';

export class PassProfileDto {
  @ApiProperty({
    description: 'ID du profil à passer',
    example: '507f1f77bcf86cd799439011',
  })
  @IsString()
  @IsNotEmpty()
  profileId: string;
}
```

---

## 📝 Structure complète des fichiers

### Structure de dossiers recommandée :

```
quick-match/
├── dto/
│   ├── like-profile.dto.ts
│   └── pass-profile.dto.ts
├── schemas/
│   ├── like.schema.ts
│   ├── match.schema.ts
│   └── pass.schema.ts
├── quick-match.controller.ts
├── quick-match.service.ts
└── quick-match.module.ts
```

---

## ✅ Vérification de cohérence Android ↔ NestJS

### Endpoint GET /quick-match/profiles

**NestJS retourne :**
```typescript
{
  profiles: QuickMatchProfileResponse[],
  pagination: {
    total: number,
    page: number,
    totalPages: number,
    limit: number
  }
}
```

**Android attend :**
```kotlin
Response<List<QuickMatchProfileResponse>>
```

⚠️ **Problème détecté** : Le backend retourne un objet avec `profiles` et `pagination`, mais Android attend directement une `List<QuickMatchProfileResponse>`.

**Solution :** Modifier l'API Android pour supporter la pagination OU modifier le backend pour retourner directement la liste.

---

## 🔧 Correction nécessaire dans Android

### Option 1 : Modifier l'API Android pour supporter la pagination (Recommandé)

**Modifier :** `app/src/main/java/com/example/damandroid/api/QuickMatchApiService.kt`

```kotlin
/**
 * Réponse paginée des profils QuickMatch
 */
data class QuickMatchProfilesResponse(
    val profiles: List<QuickMatchProfileResponse>,
    val pagination: PaginationInfo?
)

data class PaginationInfo(
    val total: Int,
    val page: Int,
    val totalPages: Int,
    val limit: Int
)

interface QuickMatchApiService {
    @GET("quick-match/profiles")
    suspend fun getProfiles(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<QuickMatchProfilesResponse>  // ← Changer le type de retour
}
```

**Mettre à jour :** `QuickMatchRemoteDataSourceImpl.kt`

```kotlin
override suspend fun fetchProfiles(): List<MatchUserProfileDto> {
    return try {
        val response = quickMatchApiService.getProfiles(page = 1, limit = 20)
        
        if (response.isSuccessful) {
            // Extraire la liste des profils depuis la réponse paginée
            response.body()?.profiles?.map { it.toMatchUserProfileDto() } ?: emptyList()
        } else {
            // ... gestion d'erreurs
        }
    } catch (e: Exception) {
        // ... gestion d'erreurs
    }
}
```

### Option 2 : Modifier le backend pour retourner directement la liste

**Modifier :** `quick-match.controller.ts`

```typescript
async getProfiles(@Request() req, @Query('page') page?: string, @Query('limit') limit?: string) {
  // ... code existant ...
  
  // Retourner directement la liste au lieu d'un objet avec pagination
  return result.profiles.map((profile) => this.mapToResponse(profile));
}
```

---

## 📊 Format de réponse attendu par Android

### GET /quick-match/profiles

**Réponse attendue :**
```json
{
  "profiles": [
    {
      "_id": "507f1f77bcf86cd799439011",
      "id": "507f1f77bcf86cd799439011",
      "name": "John Doe",
      "age": 28,
      "email": "john@example.com",
      "avatarUrl": "https://...",
      "coverImageUrl": "https://...",
      "location": "New York",
      "distance": "2.5 km",
      "bio": "Love sports!",
      "about": "Love sports!",
      "sportsInterests": ["Football", "Basketball"],
      "sports": [
        {
          "name": "Football",
          "icon": "⚽",
          "level": "Intermediate"
        }
      ],
      "interests": ["Football", "Basketball"],
      "rating": 0,
      "activitiesJoined": 15,
      "profileImageUrl": "https://..."
    }
  ],
  "pagination": {
    "total": 50,
    "page": 1,
    "totalPages": 3,
    "limit": 20
  }
}
```

### POST /quick-match/like

**Requête :**
```json
{
  "profileId": "507f1f77bcf86cd799439011"
}
```

**Réponse (si match) :**
```json
{
  "isMatch": true,
  "matchedProfile": {
    "_id": "507f1f77bcf86cd799439011",
    "id": "507f1f77bcf86cd799439011",
    "name": "John Doe",
    // ... autres champs
  }
}
```

**Réponse (si pas de match) :**
```json
{
  "isMatch": false,
  "matchedProfile": null
}
```

### POST /quick-match/pass

**Requête :**
```json
{
  "profileId": "507f1f77bcf86cd799439011"
}
```

**Réponse :**
```json
{
  "success": true
}
```

---

## 🔍 Points à vérifier

1. ✅ **Schémas Mongoose** : Like, Match, Pass sont bien définis
2. ✅ **Service** : Logique de filtrage par sports communs implémentée
3. ✅ **Controller** : Endpoints GET profiles, POST like, POST pass, GET matches
4. ⚠️ **DTOs** : LikeProfileDto et PassProfileDto doivent être créés
5. ⚠️ **Pagination** : Format de réponse doit correspondre entre Android et NestJS
6. ✅ **Filtrage** : Exclut les profils déjà likés, passés ou matchés
7. ✅ **Match** : Détection de match mutuel implémentée

---

## 🚀 Prochaines étapes

1. Créer les DTOs `LikeProfileDto` et `PassProfileDto`
2. Décider si vous voulez la pagination (Option 1) ou juste la liste (Option 2)
3. Mettre à jour l'API Android en conséquence
4. Tester les endpoints avec Postman/Swagger
5. Vérifier que les matches sont bien créés dans la collection Match

