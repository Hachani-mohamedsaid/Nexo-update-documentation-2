# 📋 Résumé - Activités "Friends" selon les Matches

## 🎯 Objectif

Les activités avec `visibility: "friends"` doivent être visibles uniquement pour :
1. ✅ **Le créateur de l'activité** (toujours)
2. ✅ **Les personnes qui ont matché avec le créateur** (likes mutuels dans QuickMatch)

## 🔑 Points Clés

### Définition d'un Match

Un **match** est créé quand **deux utilisateurs se likent mutuellement** dans QuickMatch :
- User A like User B → `Like(fromUser: A, toUser: B, isMatch: false)`
- User B like User A en retour → `isMatch: true` pour les deux + création d'un `Match(user1: A, user2: B)`

### Logique de Filtrage

```
GET /activities?visibility=friends
```

**Backend doit :**
1. Récupérer tous les matches de l'utilisateur connecté
2. Extraire les IDs des utilisateurs avec qui il a matché
3. Filtrer les activités pour retourner uniquement :
   - Activités créées par l'utilisateur connecté
   - Activités créées par les utilisateurs matchés
   - Avec `visibility: "friends"`

## 📝 Code Backend Essentiel

### 1. Service d'Activités

```typescript
async getFriendsActivities(userId: string): Promise<Activity[]> {
  // 1. Récupérer les matches
  const matches = await this.matchModel.find({
    $or: [
      { user1: new Types.ObjectId(userId) },
      { user2: new Types.ObjectId(userId) },
    ],
  });

  // 2. Extraire les IDs des utilisateurs matchés
  const matchedUserIds = matches.map((match) => {
    return match.user1.toString() === userId 
      ? match.user2 
      : match.user1;
  });

  // 3. Filtrer les activités
  const allowedUserIds = [new Types.ObjectId(userId), ...matchedUserIds];

  return this.activityModel.find({
    creator: { $in: allowedUserIds },
    visibility: 'friends',
  }).populate('creator').exec();
}
```

### 2. Controller

```typescript
@Get()
async getActivities(
  @Query('visibility') visibility?: string,
  @Request() req?: any,
) {
  const userId = req?.user?.sub || req?.user?.id || null;

  if (visibility === 'friends') {
    if (!userId) {
      throw new UnauthorizedException('Authentication required');
    }
    return this.activitiesService.getFriendsActivities(userId);
  }

  // visibility=public ou pas de paramètre
  return this.activitiesService.getActivities(userId, visibility);
}
```

## ✅ Checklist Backend

- [ ] Modifier `ActivitiesService.getActivities()` pour gérer `visibility=friends`
- [ ] Créer méthode `getFriendsActivities(userId)`
- [ ] Injecter `MatchModel` dans `ActivitiesService`
- [ ] Modifier `ActivitiesController` pour accepter `visibility` query param
- [ ] Tester avec utilisateurs qui ont matché
- [ ] Tester avec utilisateurs qui n'ont pas matché
- [ ] Vérifier l'authentification (401 si pas de token)

## 🧪 Tests à Effectuer

### Test 1 : Utilisateur avec matches
- ✅ User A et User B ont matché
- ✅ User B crée activité `visibility: "friends"`
- ✅ User A doit voir l'activité de User B

### Test 2 : Utilisateur sans matches
- ✅ User C n'a matché avec personne
- ✅ User B crée activité `visibility: "friends"`
- ✅ User C ne doit PAS voir l'activité de User B

### Test 3 : Activité créée par l'utilisateur connecté
- ✅ User A crée activité `visibility: "friends"`
- ✅ User A doit toujours voir sa propre activité

## 📚 Documentation Complète

Voir `BACKEND_ACTIVITIES_FRIENDS_VISIBILITY.md` pour le guide détaillé avec :
- Code complet du service
- Exemples de requêtes/réponses
- Gestion d'erreurs
- Sécurité
- Tests détaillés

## ✅ Frontend Prêt

Le frontend Android est déjà configuré pour :
- ✅ Récupérer les activités `visibility=public`
- ✅ Récupérer les activités `visibility=friends`
- ✅ Combiner les deux listes automatiquement

Une fois le backend implémenté, tout fonctionnera automatiquement ! 🎉

