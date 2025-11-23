# Backend NestJS - Filtrage des Activités "Friends" selon les Matches

## Vue d'ensemble

Les activités avec `visibility: "friends"` doivent être visibles uniquement pour :
1. **Le créateur de l'activité** (toujours)
2. **Les personnes qui ont matché avec le créateur** (likes mutuels dans QuickMatch)

## Logique de Filtrage

### Principe

Quand un utilisateur demande les activités avec `visibility: "friends"`, le backend doit :
1. Récupérer tous les matches de l'utilisateur connecté
2. Filtrer les activités pour ne retourner que celles créées par :
   - L'utilisateur connecté lui-même
   - Les utilisateurs avec qui il a matché

### Schéma de Match

D'après le système QuickMatch, un match est créé quand deux utilisateurs se likent mutuellement :

```typescript
// Collection Match
{
  _id: ObjectId,
  user1: ObjectId,  // ID du premier utilisateur (toujours le plus petit)
  user2: ObjectId,  // ID du second utilisateur (toujours le plus grand)
  hasChatted: Boolean,
  createdAt: Date
}
```

**Important** : L'ordre `user1 < user2` est utilisé pour éviter les doublons.

## Implémentation Backend

### 1. Modifier le Service d'Activités

Dans `src/activities/activities.service.ts` :

```typescript
import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';
import { Activity } from './schemas/activity.schema';
import { Match } from '../quick-match/schemas/match.schema';

@Injectable()
export class ActivitiesService {
  constructor(
    @InjectModel(Activity.name) private activityModel: Model<Activity>,
    @InjectModel(Match.name) private matchModel: Model<Match>,
  ) {}

  /**
   * Récupérer les activités selon la visibilité
   */
  async getActivities(
    userId: string | null,
    visibility?: string,
  ): Promise<Activity[]> {
    if (visibility === 'friends') {
      // Pour "friends", nécessite authentification
      if (!userId) {
        throw new UnauthorizedException('Authentication required for friends visibility');
      }

      return this.getFriendsActivities(userId);
    }

    // Pour "public", retourner toutes les activités publiques
    return this.activityModel
      .find({ visibility: 'public' })
      .populate('creator', 'name email profileImageUrl')
      .sort({ createdAt: -1 })
      .exec();
  }

  /**
   * Récupérer les activités "friends"
   * Retourne uniquement les activités créées par :
   * - L'utilisateur connecté
   * - Les utilisateurs avec qui il a matché
   */
  private async getFriendsActivities(userId: string): Promise<Activity[]> {
    // 1. Récupérer tous les matches de l'utilisateur
    const matches = await this.matchModel
      .find({
        $or: [
          { user1: new Types.ObjectId(userId) },
          { user2: new Types.ObjectId(userId) },
        ],
      })
      .exec();

    // 2. Extraire les IDs des utilisateurs avec qui on a matché
    const matchedUserIds = matches.map((match) => {
      // Déterminer l'autre utilisateur (pas l'utilisateur connecté)
      if (match.user1.toString() === userId) {
        return match.user2;
      } else {
        return match.user1;
      }
    });

    // 3. Ajouter l'utilisateur connecté lui-même (pour voir ses propres activités)
    const allowedUserIds = [
      new Types.ObjectId(userId),
      ...matchedUserIds,
    ];

    // 4. Récupérer les activités créées par ces utilisateurs avec visibility="friends"
    return this.activityModel
      .find({
        creator: { $in: allowedUserIds },
        visibility: 'friends',
      })
      .populate('creator', 'name email profileImageUrl')
      .sort({ createdAt: -1 })
      .exec();
  }
}
```

### 2. Modifier le Controller

Dans `src/activities/activities.controller.ts` :

```typescript
import { Controller, Get, Query, UseGuards, Request } from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { ActivitiesService } from './activities.service';

@Controller('activities')
export class ActivitiesController {
  constructor(private readonly activitiesService: ActivitiesService) {}

  /**
   * GET /activities?visibility=public|friends
   */
  @Get()
  async getActivities(
    @Query('visibility') visibility?: string,
    @Request() req?: any,
  ) {
    // Extraire l'ID utilisateur depuis le token JWT si authentifié
    const userId = req?.user?.sub || req?.user?.id || null;

    // Si visibility="friends" et pas d'authentification, retourner erreur
    if (visibility === 'friends' && !userId) {
      throw new UnauthorizedException('Authentication required for friends visibility');
    }

    return this.activitiesService.getActivities(userId, visibility);
  }
}
```

### 3. Ajouter le Guard JWT (si pas déjà fait)

Pour les requêtes avec `visibility=friends`, utiliser `@UseGuards(JwtAuthGuard)` :

```typescript
@Get()
@UseGuards(JwtAuthGuard) // Optionnel : seulement si visibility=friends
async getActivities(
  @Query('visibility') visibility?: string,
  @Request() req: any,
) {
  const userId = req.user.sub || req.user.id;
  return this.activitiesService.getActivities(userId, visibility);
}
```

### 4. Injecter le Modèle Match dans le Module

Dans `src/activities/activities.module.ts` :

```typescript
import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { ActivitiesController } from './activities.controller';
import { ActivitiesService } from './activities.service';
import { Activity, ActivitySchema } from './schemas/activity.schema';
import { Match, MatchSchema } from '../quick-match/schemas/match.schema';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Activity.name, schema: ActivitySchema },
      { name: Match.name, schema: MatchSchema },
    ]),
  ],
  controllers: [ActivitiesController],
  providers: [ActivitiesService],
})
export class ActivitiesModule {}
```

## Exemple de Requête

### Frontend (Android)

```kotlin
// Récupérer les activités publiques
val publicResponse = activityApiService.getActivities(visibility = "public")

// Récupérer les activités "friends" (nécessite authentification)
val friendsResponse = activityApiService.getActivities(visibility = "friends")
```

### Backend

**Requête 1 : Activités publiques**
```
GET /activities?visibility=public
Authorization: (optionnel)
```

**Réponse :**
```json
[
  {
    "_id": "activity1",
    "creator": {...},
    "title": "Football Match",
    "visibility": "public",
    ...
  }
]
```

**Requête 2 : Activités friends**
```
GET /activities?visibility=friends
Authorization: Bearer <jwt_token>
```

**Réponse :**
```json
[
  {
    "_id": "activity2",
    "creator": {
      "_id": "matched_user_id",
      "name": "John Doe",
      ...
    },
    "title": "Private Basketball",
    "visibility": "friends",
    ...
  }
]
```

## Logique de Filtrage Détaillée

### Étape 1 : Récupérer les Matches

```typescript
const matches = await this.matchModel.find({
  $or: [
    { user1: userId },
    { user2: userId },
  ],
});
```

### Étape 2 : Extraire les IDs des Utilisateurs Matchés

```typescript
const matchedUserIds = matches.map(match => {
  return match.user1.toString() === userId 
    ? match.user2 
    : match.user1;
});
```

### Étape 3 : Filtrer les Activités

```typescript
const allowedUserIds = [userId, ...matchedUserIds];

const activities = await this.activityModel.find({
  creator: { $in: allowedUserIds },
  visibility: 'friends',
});
```

## Cas d'Usage

### Scénario 1 : Utilisateur A crée une activité "friends"

1. **Utilisateur A** crée une activité avec `visibility: "friends"`
2. **Utilisateur B** et **Utilisateur A** ont matché (likes mutuels)
3. **Utilisateur C** n'a pas matché avec **Utilisateur A**

**Résultat :**
- ✅ **Utilisateur A** voit l'activité (créateur)
- ✅ **Utilisateur B** voit l'activité (match avec A)
- ❌ **Utilisateur C** ne voit pas l'activité (pas de match)

### Scénario 2 : Plusieurs matches

**Utilisateur A** a matché avec :
- **Utilisateur B**
- **Utilisateur C**
- **Utilisateur D**

**Utilisateur B** crée une activité "friends"

**Résultat :**
- ✅ **Utilisateur A** voit l'activité (match avec B)
- ✅ **Utilisateur B** voit l'activité (créateur)
- ❌ **Utilisateur C** ne voit pas l'activité (pas de match avec B)
- ❌ **Utilisateur D** ne voit pas l'activité (pas de match avec B)

## Sécurité

### Vérifications Importantes

1. **Authentification requise** : `visibility=friends` nécessite un token JWT valide
2. **Vérification des matches** : Seuls les matches réels (likes mutuels) sont considérés
3. **Isolation des données** : Chaque utilisateur ne voit que ses propres matches

### Protection contre les Accès Non Autorisés

```typescript
// Vérifier que l'utilisateur est authentifié
if (visibility === 'friends' && !userId) {
  throw new UnauthorizedException('Authentication required');
}

// Vérifier que les matches existent vraiment
const matches = await this.matchModel.find({
  $or: [
    { user1: userId },
    { user2: userId },
  ],
});
```

## Tests

### Test 1 : Utilisateur avec matches

```typescript
// Créer des matches
await matchModel.create({ user1: userA, user2: userB });
await matchModel.create({ user1: userA, user2: userC });

// Créer des activités "friends"
await activityModel.create({
  creator: userB,
  visibility: 'friends',
  title: 'Activity from B',
});

// Récupérer les activités "friends" pour userA
const activities = await service.getFriendsActivities(userA._id);

// Vérifier que l'activité de userB est incluse
expect(activities).toHaveLength(1);
expect(activities[0].creator._id.toString()).toBe(userB._id.toString());
```

### Test 2 : Utilisateur sans matches

```typescript
// Créer une activité "friends" par userB
await activityModel.create({
  creator: userB,
  visibility: 'friends',
  title: 'Activity from B',
});

// Récupérer les activités "friends" pour userA (pas de match)
const activities = await service.getFriendsActivities(userA._id);

// Vérifier qu'aucune activité n'est retournée
expect(activities).toHaveLength(0);
```

## Endpoint pour Récupérer les Matches (Optionnel)

Si le frontend a besoin de récupérer la liste des matches :

```typescript
// Dans quick-match.controller.ts
@Get('matches')
@UseGuards(JwtAuthGuard)
async getMatches(@Request() req: any) {
  const userId = req.user.sub || req.user.id;
  return this.quickMatchService.getMatches(userId);
}

// Dans quick-match.service.ts
async getMatches(userId: string) {
  const matches = await this.matchModel
    .find({
      $or: [
        { user1: new Types.ObjectId(userId) },
        { user2: new Types.ObjectId(userId) },
      ],
    })
    .populate('user1', 'name profileImageUrl')
    .populate('user2', 'name profileImageUrl')
    .exec();

  return matches.map((match) => {
    const matchedUser = match.user1._id.toString() === userId 
      ? match.user2 
      : match.user1;

    return {
      matchId: match._id.toString(),
      matchedUser: {
        _id: matchedUser._id.toString(),
        id: matchedUser._id.toString(),
        name: matchedUser.name,
        profileImageUrl: matchedUser.profileImageUrl,
      },
      createdAt: match.createdAt.toISOString(),
    };
  });
}
```

## Résumé

✅ **Backend doit :**
1. Filtrer les activités `visibility: "friends"` selon les matches
2. Retourner uniquement les activités créées par :
   - L'utilisateur connecté
   - Les utilisateurs avec qui il a matché (likes mutuels)
3. Exiger l'authentification pour `visibility=friends`

✅ **Frontend doit :**
1. Appeler `getActivities(visibility = "friends")` en plus de `getActivities(visibility = "public")`
2. Combiner les deux listes pour afficher toutes les activités visibles

