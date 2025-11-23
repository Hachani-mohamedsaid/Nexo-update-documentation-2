# 🔧 Correction Backend QuickMatch - Retourner Plus de Profils

## ❌ Problème Actuel

Le backend retourne **seulement 1 profil** (`"pagination":{"total":1}`) même avec `limit=50`.

**Réponse actuelle du backend** :
```json
{
  "profiles": [
    {
      "_id": "690e23ebf083f749b2562383",
      "name": "Neji Hachani",
      ...
    }
  ],
  "pagination": {
    "total": 1,
    "page": 1,
    "totalPages": 1,
    "limit": 50
  }
}
```

## 🎯 Causes Identifiées

### 1. **Exclusion des profils likés/passés même si seul disponible**

Le backend exclut automatiquement les profils likés ou passés, même si c'est le seul profil disponible. Cela crée une situation où :
- L'utilisateur like le seul profil disponible
- Le backend l'exclut lors du prochain appel
- Résultat : 0 profils retournés → liste vide dans l'app

**Code problématique** (dans `quick-match.service.ts`) :
```typescript
// ❌ PROBLÈME : Exclut toujours les profils likés/passés
const excludedUserIds = new Set<string>();
likedProfiles.forEach((like) => excludedUserIds.add(like.toUser.toString()));
passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString()));

const query = {
  _id: { $nin: excludedIds }, // ❌ Exclut même si c'est le seul disponible
  // ...
};
```

### 2. **Filtrage trop strict par sports communs**

Le backend filtre uniquement les utilisateurs avec **exactement** les mêmes sports. Si peu d'utilisateurs ont des sports communs, il n'y aura qu'un seul profil.

### 3. **Pas de fallback si peu de résultats**

Le backend ne retourne pas de profils alternatifs si moins de X profils sont trouvés.

## ✅ Solutions à Implémenter

### Solution 1 : Ne pas exclure les profils likés si < 3 profils disponibles

**Modifier `quick-match.service.ts`** :

```typescript
async getCompatibleProfiles(
  userId: string,
  page: number = 1,
  limit: number = 50,
): Promise<{ profiles: any[]; total: number; page: number; totalPages: number }> {
  // ... code existant pour récupérer sports communs ...

  // 1. D'abord, essayer de récupérer les profils SANS exclure les likés/passés
  const queryWithoutExclusions: any = {
    _id: { $ne: new Types.ObjectId(userId) },
  };

  if (allUserSports.length > 0) {
    queryWithoutExclusions.sportsInterests = {
      $in: allUserSports.map((sport) => new RegExp(`^${sport}$`, 'i')),
    };
  }

  // Compter le total SANS exclusions
  const totalWithoutExclusions = await this.userModel
    .countDocuments(queryWithoutExclusions)
    .exec();

  // 2. Si moins de 3 profils disponibles, NE PAS exclure les likés/passés
  if (totalWithoutExclusions < 3) {
    android.util.Log.w("QuickMatchService", 
      `⚠️ Only ${totalWithoutExclusions} compatible profiles found. Including liked/passed profiles.`);
    
    // Retourner tous les profils compatibles, même likés/passés
    const skip = (page - 1) * limit;
    const allProfiles = await this.userModel
      .find(queryWithoutExclusions)
      .skip(skip)
      .limit(limit)
      .exec();

    const totalPages = Math.ceil(totalWithoutExclusions / limit);

    return {
      profiles: allProfiles,
      total: totalWithoutExclusions,
      page,
      totalPages,
    };
  }

  // 3. Si >= 3 profils, exclure les likés/passés (comportement normal)
  const [likedProfiles, passedProfiles, matchedProfiles] = await Promise.all([
    // ... code existant ...
  ]);

  const excludedUserIds = new Set<string>();
  likedProfiles.forEach((like) => excludedUserIds.add(like.toUser.toString()));
  passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString()));
  matchedProfiles.forEach((match) => {
    excludedUserIds.add(
      match.user1.toString() === userId
        ? match.user2.toString()
        : match.user1.toString(),
    );
  });

  const excludedIds = [
    new Types.ObjectId(userId),
    ...Array.from(excludedUserIds).map((id) => new Types.ObjectId(id)),
  ];

  const query: any = {
    _id: { $nin: excludedIds },
  };

  if (allUserSports.length > 0) {
    query.sportsInterests = {
      $in: allUserSports.map((sport) => new RegExp(`^${sport}$`, 'i')),
    };
  }

  const total = await this.userModel.countDocuments(query).exec();
  const skip = (page - 1) * limit;
  const profiles = await this.userModel
    .find(query)
    .skip(skip)
    .limit(limit)
    .exec();

  const totalPages = Math.ceil(total / limit);

  return {
    profiles,
    total,
    page,
    totalPages,
  };
}
```

### Solution 2 : Relâcher le filtrage par sports si peu de résultats

**Ajouter un fallback** :

```typescript
// Si moins de 5 profils avec sports communs, retourner tous les utilisateurs
if (compatibleProfiles.length < 5) {
  android.util.Log.w("QuickMatchService", 
    `⚠️ Only ${compatibleProfiles.length} profiles with common sports. Returning all users.`);
  
  const allUsers = await this.userModel
    .find({
      _id: { $ne: new Types.ObjectId(userId) },
    })
    .limit(limit)
    .exec();

  return {
    profiles: allUsers,
    total: allUsers.length,
    page,
    totalPages: 1,
  };
}
```

### Solution 3 : Recherche plus flexible des sports

**Utiliser une recherche partielle** au lieu d'exacte :

```typescript
// Au lieu de recherche exacte
query.sportsInterests = {
  $in: allUserSports.map((sport) => new RegExp(`^${sport}$`, 'i')),
};

// Utiliser recherche partielle (plus flexible)
query.sportsInterests = {
  $in: allUserSports.map((sport) => new RegExp(sport, 'i')), // Sans ^ et $
};
```

### Solution 4 : Augmenter la limite par défaut

**Dans `quick-match.controller.ts`** :

```typescript
@Get('profiles')
async getProfiles(
  @Request() req,
  @Query('page') page: number = 1,
  @Query('limit') limit: number = 100, // ✅ Augmenter de 20 à 100
) {
  const userId = req.user.id;
  const result = await this.quickMatchService.getCompatibleProfiles(
    userId,
    page,
    limit,
  );
  return result;
}
```

## 🧪 Tests à Effectuer

1. **Test avec 1 seul utilisateur compatible** :
   - Vérifier que le backend retourne ce profil même s'il a été liké
   - Vérifier que `total >= 1` toujours

2. **Test avec plusieurs utilisateurs** :
   - Vérifier que le backend retourne plusieurs profils
   - Vérifier que les profils likés sont exclus seulement si >= 3 profils disponibles

3. **Test avec aucun sport commun** :
   - Vérifier le fallback (retourner tous les utilisateurs)

## 📊 Logs à Ajouter (Backend)

```typescript
console.log(`[QuickMatch] User ${userId} - Compatible profiles: ${total}`);
console.log(`[QuickMatch] User ${userId} - Excluded (liked/passed): ${excludedUserIds.size}`);
console.log(`[QuickMatch] User ${userId} - Final profiles returned: ${profiles.length}`);
```

## ✅ Checklist de Correction

- [ ] Modifier `getCompatibleProfiles` pour ne pas exclure les likés si < 3 profils
- [ ] Ajouter un fallback si peu de profils avec sports communs
- [ ] Augmenter la limite par défaut à 100
- [ ] Ajouter des logs pour diagnostiquer
- [ ] Tester avec différents scénarios
- [ ] Vérifier que `total >= 1` toujours

## 🎯 Résultat Attendu

Après correction, le backend devrait :
- ✅ Retourner **au moins 3-5 profils** par défaut
- ✅ Ne pas exclure les profils likés si c'est le seul disponible
- ✅ Avoir un fallback si peu de profils compatibles
- ✅ Retourner `total >= 1` toujours

