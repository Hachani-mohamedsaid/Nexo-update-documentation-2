# 📋 Endpoint Backend Requis : GET /users/{id}/profile

## ❌ Problème Actuel

L'endpoint `GET /users/{id}/profile` n'existe pas dans le backend NestJS, ce qui cause une erreur HTTP 404 lorsque l'utilisateur clique sur "Détails du profil" dans le chat.

## ✅ Solution : Créer l'Endpoint dans le Backend

### 📍 Endpoint à Créer

```
GET /users/{id}/profile
```

### 🔐 Authentification

- **Requis** : JWT Bearer Token dans le header `Authorization`
- **Format** : `Authorization: Bearer <access_token>`

### 📝 Implémentation Backend NestJS

#### 1. Controller (`users.controller.ts`)

```typescript
import { Controller, Get, Param, UseGuards, Request } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { UsersService } from './users.service';

@Controller('users')
@UseGuards(JwtAuthGuard)
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get(':id/profile')
  async getUserProfileById(
    @Param('id') userId: string,
    @Request() req
  ) {
    return this.usersService.getUserProfileById(userId);
  }
}
```

#### 2. Service (`users.service.ts`)

```typescript
import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { User } from './schemas/user.schema';

@Injectable()
export class UsersService {
  constructor(
    @InjectModel(User.name) private userModel: Model<User>
  ) {}

  async getUserProfileById(userId: string): Promise<any> {
    const user = await this.userModel.findById(userId).exec();

    if (!user) {
      throw new NotFoundException('User not found');
    }

    // Récupérer les statistiques de l'utilisateur
    const activitiesJoined = await this.activityModel.countDocuments({
      participants: userId
    }).exec();

    const activitiesHosted = await this.activityModel.countDocuments({
      creator: userId
    }).exec();

    // Récupérer les achievements/badges de l'utilisateur
    const userAchievements = await this.achievementsModel
      .findOne({ userId })
      .exec();

    return {
      id: user._id.toString(),
      _id: user._id.toString(),
      email: user.email,
      name: user.name,
      location: user.location || "Non spécifiée",
      isEmailVerified: user.isEmailVerified || false,
      phone: user.phone || null,
      dateOfBirth: user.dateOfBirth || null,
      about: user.about || "Aucune description",
      sportsInterests: user.sportsInterests || [],
      profileImageUrl: user.profileImageUrl || null,
      profileImageThumbnailUrl: user.profileImageThumbnailUrl || null,
      profileImageDeleteUrl: user.profileImageDeleteUrl || null,
      // Données enrichies
      stats: {
        sessionsJoined: activitiesJoined || 0,
        sessionsHosted: activitiesHosted || 0,
        followers: user.followersCount || 0,
        following: user.followingCount || 0,
        favoriteSports: user.sportsInterests || []
      }
    };
  }
}
```

#### 3. Schéma User (si nécessaire)

Assurez-vous que le schéma User contient tous les champs nécessaires :

```typescript
@Schema()
export class User {
  @Prop({ required: true })
  email: string;

  @Prop({ required: true })
  name: string;

  @Prop()
  location?: string;

  @Prop({ default: false })
  isEmailVerified: boolean;

  @Prop()
  phone?: string;

  @Prop()
  dateOfBirth?: string;

  @Prop()
  about?: string;

  @Prop({ type: [String], default: [] })
  sportsInterests: string[];

  @Prop()
  profileImageUrl?: string;

  @Prop()
  profileImageThumbnailUrl?: string;

  @Prop()
  profileImageDeleteUrl?: string;
}
```

### 📤 Réponse Attendue (200 OK)

```json
{
  "id": "6913492bd65af9d44d243495",
  "_id": "6913492bd65af9d44d243495",
  "email": "user@example.com",
  "name": "John Doe",
  "location": "Paris, France",
  "isEmailVerified": true,
  "phone": "+33612345678",
  "dateOfBirth": "1990-01-01",
  "about": "Passionné de sport et d'activités en plein air",
  "sportsInterests": ["Running", "Cycling", "Swimming"],
  "profileImageUrl": "https://example.com/profile.jpg",
  "profileImageThumbnailUrl": "https://example.com/profile-thumb.jpg",
  "profileImageDeleteUrl": null
}
```

### 🔒 Gestion des Erreurs

- **404 Not Found** : L'utilisateur avec cet ID n'existe pas
- **401 Unauthorized** : Token JWT invalide ou manquant
- **403 Forbidden** : L'utilisateur n'a pas les droits pour accéder à ce profil

### ✅ Validation

Une fois l'endpoint créé dans le backend, l'application Android pourra :
1. Récupérer le profil complet de l'autre utilisateur
2. Afficher toutes les informations (nom, avatar, bio, statistiques, achievements)
3. Naviguer vers la page de détails du profil depuis le chat

---

## 📱 Frontend (Déjà Implémenté)

Le frontend Android est déjà prêt et attend cet endpoint :

- ✅ API Service : `UserApiService.getUserProfileById()`
- ✅ Data Source : `ProfileRemoteDataSource.getUserProfileById()`
- ✅ Repository : `ProfileRepository.getUserProfileById()`
- ✅ Use Case : `GetUserProfileById`
- ✅ UI Screen : `UserProfileDetailsScreen`

Une fois l'endpoint créé dans le backend, tout fonctionnera automatiquement sans modification du frontend.

---

## 🚀 Test de l'Endpoint

### Test avec cURL

```bash
curl -X GET \
  https://apinest-production.up.railway.app/users/{userId}/profile \
  -H "Authorization: Bearer <your_jwt_token>"
```

### Test avec Postman

1. **Method** : GET
2. **URL** : `https://apinest-production.up.railway.app/users/{userId}/profile`
3. **Headers** :
   - `Authorization: Bearer <your_jwt_token>`
   - `Content-Type: application/json`

Remplacer `{userId}` par l'ID d'un utilisateur existant.

---

## 📌 Note Importante

Cet endpoint est nécessaire pour que la fonctionnalité "Détails du profil" fonctionne correctement depuis le chat. Sans cet endpoint, l'utilisateur verra une erreur HTTP 404.

