# 🔔 Notifications de Likes - Guide Backend NestJS

## 📋 Vue d'ensemble

Ce guide explique comment implémenter l'endpoint pour récupérer les likes reçus (notifications) dans le backend NestJS. Quand un utilisateur like un profil, l'autre utilisateur doit recevoir une notification.

---

## 🎯 Endpoint à Implémenter

### GET /quick-match/likes-received

**Description** : Récupère tous les likes reçus par l'utilisateur connecté (utilisateurs qui ont liké son profil).

**Authentification** : Requis (JWT)

**Réponse** :
```json
{
  "likes": [
    {
      "likeId": "507f1f77bcf86cd799439011",
      "fromUser": {
        "_id": "507f1f77bcf86cd799439012",
        "id": "507f1f77bcf86cd799439012",
        "name": "Sami",
        "profileImageUrl": "https://...",
        "avatarUrl": "https://..."
      },
      "isMatch": false,
      "matchId": null,
      "createdAt": "2024-01-15T10:30:00.000Z"
    },
    {
      "likeId": "507f1f77bcf86cd799439013",
      "fromUser": {
        "_id": "507f1f77bcf86cd799439014",
        "id": "507f1f77bcf86cd799439014",
        "name": "Ahmed",
        "profileImageUrl": "https://...",
        "avatarUrl": "https://..."
      },
      "isMatch": true,
      "matchId": "507f1f77bcf86cd799439015",
      "createdAt": "2024-01-14T08:20:00.000Z"
    }
  ]
}
```

---

## 🔧 Implémentation dans quick-match.controller.ts

Ajoutez cette méthode dans `QuickMatchController` :

```typescript
@Get('likes-received')
@ApiOperation({ 
  summary: 'Get likes received by the current user',
  description: 'Returns all users who have liked the current user\'s profile. Includes match status.'
})
@ApiResponse({
  status: 200,
  description: 'List of likes received retrieved successfully',
})
@ApiResponse({ status: 401, description: 'Unauthorized' })
async getLikesReceived(@Request() req) {
  const userId = req.user._id.toString();
  const likes = await this.quickMatchService.getLikesReceived(userId);
  
  return {
    likes: likes.map((like) => ({
      likeId: like._id.toString(),
      fromUser: {
        _id: like.fromUser._id.toString(),
        id: like.fromUser._id.toString(),
        name: like.fromUser.name,
        profileImageUrl: like.fromUser.profileImageUrl,
        avatarUrl: like.fromUser.profileImageUrl || like.fromUser.profileImageThumbnailUrl,
      },
      isMatch: like.isMatch,
      matchId: like.isMatch ? this.getMatchId(userId, like.fromUser._id.toString()) : null,
      createdAt: like.createdAt.toISOString(),
    })),
  };
}

/**
 * Récupère l'ID du match entre deux utilisateurs
 */
private async getMatchId(user1Id: string, user2Id: string): Promise<string | null> {
  const match = await this.quickMatchService.getMatchByUsers(user1Id, user2Id);
  return match?._id.toString() || null;
}
```

---

## 🔧 Implémentation dans quick-match.service.ts

Ajoutez cette méthode dans `QuickMatchService` :

```typescript
/**
 * Récupère tous les likes reçus par un utilisateur
 * (utilisateurs qui ont liké son profil)
 */
async getLikesReceived(userId: string): Promise<LikeDocument[]> {
  // Récupérer tous les likes où l'utilisateur connecté est le destinataire (toUser)
  const likes = await this.likeModel
    .find({ toUser: new Types.ObjectId(userId) })
    .populate('fromUser', 'name email profileImageUrl profileImageThumbnailUrl')
    .sort({ createdAt: -1 }) // Plus récents en premier
    .exec();

  return likes;
}

/**
 * Récupère un match entre deux utilisateurs
 */
async getMatchByUsers(user1Id: string, user2Id: string): Promise<MatchDocument | null> {
  // Vérifier dans les deux sens (user1-user2 et user2-user1)
  const match = await this.matchModel
    .findOne({
      $or: [
        { user1: new Types.ObjectId(user1Id), user2: new Types.ObjectId(user2Id) },
        { user1: new Types.ObjectId(user2Id), user2: new Types.ObjectId(user1Id) },
      ],
    })
    .exec();

  return match;
}
```

---

## 📝 Logique de Détection de Match

Quand un utilisateur like un profil :

1. **Créer le like** dans la collection `Like`
2. **Vérifier si c'est un match** :
   - Chercher un like inverse (l'autre utilisateur a déjà liké)
   - Si trouvé → `isMatch = true` et créer un `Match`
3. **Mettre à jour le like inverse** avec `isMatch = true`

**Exemple** :
- Sami like Mohamed → Créer `Like(fromUser: Sami, toUser: Mohamed, isMatch: false)`
- Mohamed like Sami en retour → 
  - Mettre à jour `Like(fromUser: Sami, toUser: Mohamed, isMatch: true)`
  - Créer `Like(fromUser: Mohamed, toUser: Sami, isMatch: true)`
  - Créer `Match(user1: Sami, user2: Mohamed)`

---

## ✅ Format de Réponse Attendu

Le backend DOIT retourner exactement ce format :

```typescript
{
  likes: Array<{
    likeId: string;           // ID du like
    fromUser: {
      _id: string;
      id: string;
      name: string;
      profileImageUrl?: string;
      avatarUrl?: string;
    };
    isMatch: boolean;         // true si l'utilisateur connecté a aussi liké ce profil
    matchId: string | null;   // ID du match si isMatch = true
    createdAt: string;        // ISO 8601 date string
  }>
}
```

---

## 🔍 Points Importants

1. **Ordre de tri** : Les likes les plus récents doivent être en premier (`sort({ createdAt: -1 })`)

2. **Détection de match** : 
   - `isMatch = true` si l'utilisateur connecté a déjà liké ce profil en retour
   - `matchId` doit être fourni uniquement si `isMatch = true`

3. **Populate** : Utiliser `.populate('fromUser', ...)` pour récupérer les informations de l'utilisateur qui a liké

4. **Authentification** : L'endpoint doit être protégé par `@UseGuards(JwtAuthGuard)`

---

## 🧪 Test avec Postman

**Requête** :
```
GET https://apinest-production.up.railway.app/quick-match/likes-received
Headers:
  Authorization: Bearer <JWT_TOKEN>
```

**Réponse attendue** :
```json
{
  "likes": [
    {
      "likeId": "...",
      "fromUser": {
        "_id": "...",
        "id": "...",
        "name": "Sami",
        "profileImageUrl": "https://...",
        "avatarUrl": "https://..."
      },
      "isMatch": false,
      "matchId": null,
      "createdAt": "2024-01-15T10:30:00.000Z"
    }
  ]
}
```

---

## 📚 Intégration avec le Frontend Android

Le frontend Android :
1. Appelle `GET /quick-match/likes-received` pour récupérer les likes reçus
2. Convertit chaque like en `NotificationItem.LikeNotification`
3. Affiche les notifications avec :
   - **Si `isMatch = false`** : Bouton "Like Back"
   - **Si `isMatch = true`** : Boutons "Welcome" et "Chat"

Quand l'utilisateur clique sur "Like Back" :
- Le frontend appelle `POST /quick-match/like` avec le `fromUserId`
- Si c'est un match, le backend retourne `isMatch: true`
- Le frontend rafraîchit les notifications pour afficher "Welcome" et "Chat"

---

## 🎯 Checklist d'Implémentation

- [ ] Ajouter la méthode `getLikesReceived()` dans `QuickMatchService`
- [ ] Ajouter la méthode `getMatchByUsers()` dans `QuickMatchService`
- [ ] Ajouter l'endpoint `GET /quick-match/likes-received` dans `QuickMatchController`
- [ ] Tester avec Postman
- [ ] Vérifier que `isMatch` est correctement détecté
- [ ] Vérifier que `matchId` est fourni uniquement si `isMatch = true`
- [ ] Vérifier l'ordre de tri (plus récents en premier)

---

## ⚠️ Notes Importantes

1. **Performance** : Si vous avez beaucoup de likes, pensez à ajouter une pagination
2. **Cache** : Vous pouvez mettre en cache les likes reçus pour améliorer les performances
3. **Notifications en temps réel** : Pour une meilleure UX, considérez l'utilisation de WebSockets pour les notifications en temps réel

