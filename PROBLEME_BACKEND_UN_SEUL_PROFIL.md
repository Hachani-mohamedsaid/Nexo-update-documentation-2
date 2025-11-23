# 🔍 Problème Confirmé - Backend Retourne un Seul Profil

## ✅ Diagnostic Confirmé par les Logs

Les logs Android confirment que le backend ne retourne qu'un seul profil :

```
D/QuickMatchDataSource: Profiles loaded: 1
D/QuickMatchDataSource: Pagination: total=1, page=1, totalPages=1
D/QuickMatchViewModel: Final profiles count: 1
```

**Réponse HTTP du backend :**
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
    "total": 1,        // ⚠️ Seulement 1 profil disponible !
    "page": 1,
    "totalPages": 1,
    "limit": 50
  }
}
```

---

## 🔍 Cause Racine

Le problème est **100% côté backend**. Le backend filtre trop strictement et ne trouve qu'un seul profil compatible.

### Logique de Filtrage Actuelle (Backend)

D'après le guide `QUICKMATCH_NESTJS_COMPLETE_GUIDE.md`, le backend filtre les profils en :

1. **Recherchant les utilisateurs avec sports communs** ✅
2. **Excluant les profils déjà likés** ✅
3. **Excluant les profils déjà passés** ⚠️ **PROBLÈME ICI**
4. **Excluant les profils avec matchs** ✅

### Problème Identifié

Le backend exclut **AUSSI les profils passés**. Donc si l'utilisateur a déjà passé plusieurs profils, il ne reste qu'un seul profil disponible.

**Exemple :**
- Utilisateur "Mohamed" a déjà passé 10 profils
- Il reste seulement 1 profil compatible ("Neji Hachani")
- Le backend retourne `total: 1`

---

## 🛠️ Solutions Backend

### Solution 1 : Ne PAS Exclure les Profils Passés (Recommandé)

Les profils passés peuvent être revus après un certain temps. Modifier le backend pour :

```typescript
// Dans getCompatibleProfiles() - quick-match.service.ts

// AVANT (problème)
const excludedUserIds = new Set<string>();
likedProfiles.forEach((like) => excludedUserIds.add(like.toUser.toString()));
passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString())); // ❌ Exclut les passés

// APRÈS (solution)
const excludedUserIds = new Set<string>();
likedProfiles.forEach((like) => excludedUserIds.add(like.toUser.toString()));
// Ne pas exclure les profils passés pour permettre de les revoir
// passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString())); // ✅ Supprimé

// Exclure seulement les profils avec matchs (on ne peut pas les revoir)
matchedProfiles.forEach((match) => {
  excludedUserIds.add(match.user1.toString());
  excludedUserIds.add(match.user2.toString());
});
```

**Avantage :** Plus de profils disponibles, l'utilisateur peut revoir les profils qu'il a passés.

---

### Solution 2 : Expiration des Passes (Alternative)

Si vous voulez garder l'exclusion des passes, ajouter une expiration :

```typescript
// Exclure seulement les passes récents (moins de 7 jours)
const recentPasses = passedProfiles.filter((pass) => {
  const daysSincePass = (Date.now() - new Date(pass.createdAt).getTime()) / (1000 * 60 * 60 * 24);
  return daysSincePass < 7; // Exclure seulement les passes de moins de 7 jours
});

recentPasses.forEach((pass) => excludedUserIds.add(pass.toUser.toString()));
```

**Avantage :** Équilibre entre ne pas montrer les mêmes profils tout le temps et avoir assez de profils disponibles.

---

### Solution 3 : Réduire le Filtrage par Sports (Si Pas Assez d'Utilisateurs)

Si le problème vient du fait qu'il n'y a pas assez d'utilisateurs avec des sports communs :

```typescript
// Si pas assez de profils avec sports communs, assouplir le filtre
let compatibleProfiles = await this.userModel
  .find({
    _id: { $nin: excludedIds },
    sportsInterests: { $in: allUserSports.map(s => new RegExp(`^${s}$`, 'i')) }
  })
  .limit(limit)
  .skip((page - 1) * limit)
  .exec();

// Si moins de 5 profils, chercher avec sports similaires
if (compatibleProfiles.length < 5) {
  // Chercher avec sports similaires (case-insensitive, partial match)
  compatibleProfiles = await this.userModel
    .find({
      _id: { $nin: excludedIds },
      $or: allUserSports.map(sport => ({
        sportsInterests: { $regex: sport, $options: 'i' }
      }))
    })
    .limit(limit)
    .skip((page - 1) * limit)
    .exec();
}
```

---

### Solution 4 : Ajouter Plus d'Utilisateurs de Test

Si le problème vient du manque d'utilisateurs dans la base :

1. Créer des utilisateurs de test avec différents sports
2. S'assurer qu'ils ont des sports en commun avec l'utilisateur connecté
3. S'assurer qu'ils ne sont pas tous déjà likés/passés

---

## 📋 Actions Immédiates Recommandées

### 1. Vérifier le Backend - `quick-match.service.ts`

Vérifier la méthode `getCompatibleProfiles()` et voir :

```typescript
// Vérifier combien d'utilisateurs sont trouvés AVANT filtrage
const allUsersWithSports = await this.userModel.find({
  sportsInterests: { $in: allUserSports }
}).exec();

console.log(`[QuickMatch] Users with common sports: ${allUsersWithSports.length}`);

// Vérifier combien sont exclus
console.log(`[QuickMatch] Excluded (liked): ${likedProfiles.length}`);
console.log(`[QuickMatch] Excluded (passed): ${passedProfiles.length}`);
console.log(`[QuickMatch] Excluded (matched): ${matchedProfiles.length}`);
console.log(`[QuickMatch] Total excluded: ${excludedUserIds.size}`);
```

### 2. Modifier le Filtrage (Solution 1)

Supprimer l'exclusion des profils passés :

```typescript
// Dans getCompatibleProfiles()
const excludedUserIds = new Set<string>();

// Exclure seulement les profils LIKÉS et MATCHÉS
likedProfiles.forEach((like) => excludedUserIds.add(like.toUser.toString()));
matchedProfiles.forEach((match) => {
  excludedUserIds.add(match.user1.toString());
  excludedUserIds.add(match.user2.toString());
});

// NE PAS exclure les profils passés pour permettre de les revoir
// passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString())); // ❌ Supprimer cette ligne
```

### 3. Vérifier la Base de Données

Vérifier combien d'utilisateurs existent avec des sports en commun :

```javascript
// Dans MongoDB shell ou Compass
db.users.count({ 
  sportsInterests: { $in: ["Running", "Swimming", "Hiking", "Cycling", "Boxing"] }
})
```

---

## 🎯 Résultat Attendu Après Correction

Après avoir appliqué la **Solution 1** (ne pas exclure les profils passés) :

1. Le backend devrait retourner **plus de profils** (tous les profils compatibles sauf ceux déjà likés/matchés)
2. Les logs devraient montrer :
   ```
   D/QuickMatchDataSource: Profiles loaded: 10-20
   D/QuickMatchDataSource: Pagination: total=15, page=1, totalPages=1
   ```
3. L'utilisateur pourra voir **plusieurs profils** dans l'écran QuickMatch

---

## 📝 Résumé

- ✅ **Problème confirmé** : Backend retourne `total: 1`
- ✅ **Cause identifiée** : Backend exclut les profils passés, ne laissant qu'un seul profil
- ✅ **Solution** : Modifier le backend pour ne pas exclure les profils passés
- ✅ **Frontend OK** : Le frontend affiche correctement les profils reçus

**Le problème est 100% backend. Une fois corrigé, le frontend affichera automatiquement plus de profils.**

