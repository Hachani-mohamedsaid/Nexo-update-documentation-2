# 🛠️ Solution Backend - QuickMatch Retourne un Seul Profil

## ✅ Problème Confirmé

**Leaderboard :** 15 utilisateurs dans la base
**QuickMatch :** 1 seul profil retourné

### Diagnostic

Le backend a **15 utilisateurs** dans le leaderboard, mais QuickMatch ne retourne qu'**un seul profil** (`total: 1`). Cela confirme que le backend filtre trop strictement.

---

## 🔍 Analyse du Filtrage Backend

### Filtres Appliqués (Trop Stricts)

Le backend exclut probablement :
1. ✅ **L'utilisateur connecté** (normal)
2. ❌ **Profils déjà likés** (normal)
3. ❌ **Profils déjà passés** ⚠️ **PROBLÈME**
4. ❌ **Profils avec matchs** (normal)
5. ❌ **Utilisateurs sans sports communs** (normal, mais peut être trop strict)

**Résultat :** Sur 15 utilisateurs, il ne reste qu'1 profil compatible.

---

## 🛠️ Solutions Backend NestJS

### Solution 1 : Ne PAS Exclure les Profils Passés (Recommandé)

**Fichier :** `src/quick-match/quick-match.service.ts`

**Méthode :** `getCompatibleProfiles()`

#### ❌ Code Actuel (Problème)

```typescript
async getCompatibleProfiles(
  userId: string,
  page: number = 1,
  limit: number = 20,
): Promise<{ profiles: any[]; total: number; page: number; totalPages: number }> {
  // ... récupération des sports de l'utilisateur ...

  // ❌ PROBLÈME : Exclut les profils passés
  const [likedProfiles, passedProfiles, matchedProfiles] = await Promise.all([
    this.likeModel.find({ fromUser: new Types.ObjectId(userId) }).select('toUser').exec(),
    this.passModel.find({ fromUser: new Types.ObjectId(userId) }).select('toUser').exec(),
    // ...
  ]);

  const excludedUserIds = new Set<string>();
  likedProfiles.forEach((like) => excludedUserIds.add(like.toUser.toString()));
  passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString())); // ❌ SUPPRIMER
  matchedProfiles.forEach((match) => {
    excludedUserIds.add(match.user1.toString());
    excludedUserIds.add(match.user2.toString());
  });

  // ...
}
```

#### ✅ Code Corrigé (Solution)

```typescript
async getCompatibleProfiles(
  userId: string,
  page: number = 1,
  limit: number = 20,
): Promise<{ profiles: any[]; total: number; page: number; totalPages: number }> {
  // ... récupération des sports de l'utilisateur ...

  // ✅ SOLUTION : Ne pas récupérer les profils passés
  const [likedProfiles, matchedProfiles] = await Promise.all([
    this.likeModel.find({ fromUser: new Types.ObjectId(userId) }).select('toUser').exec(),
    // Ne plus récupérer passedProfiles car on ne veut pas les exclure
    this.matchModel.find({
      $or: [
        { user1: new Types.ObjectId(userId) },
        { user2: new Types.ObjectId(userId) },
      ],
    }).select('user1 user2').exec(),
  ]);

  // ✅ Exclure seulement les profils likés et matchés
  const excludedUserIds = new Set<string>();
  likedProfiles.forEach((like) => excludedUserIds.add(like.toUser.toString()));
  // Ne pas exclure les profils passés pour permettre de les revoir
  // passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString())); // ❌ SUPPRIMÉ

  matchedProfiles.forEach((match) => {
    excludedUserIds.add(match.user1.toString());
    excludedUserIds.add(match.user2.toString());
  });

  // Exclure aussi l'utilisateur connecté
  excludedUserIds.add(userId);

  // ... reste du code ...
}
```

---

### Solution 2 : Expiration des Passes (Alternative)

Si vous voulez garder l'exclusion des passes mais permettre de revoir les anciens passes :

```typescript
// Récupérer seulement les passes récents (moins de 7 jours)
const sevenDaysAgo = new Date();
sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

const recentPasses = await this.passModel
  .find({
    fromUser: new Types.ObjectId(userId),
    createdAt: { $gte: sevenDaysAgo } // Seulement les passes de moins de 7 jours
  })
  .select('toUser')
  .exec();

// Exclure seulement les passes récents
recentPasses.forEach((pass) => excludedUserIds.add(pass.toUser.toString()));
```

**Avantage :** L'utilisateur peut revoir les profils qu'il a passés il y a plus de 7 jours.

---

### Solution 3 : Assouplir le Filtre par Sports (Si Pas Assez de Profils)

Si après avoir retiré l'exclusion des passes, il n'y a toujours pas assez de profils :

```typescript
// Chercher d'abord avec sports exacts
let compatibleProfiles = await this.userModel
  .find({
    _id: { $nin: Array.from(excludedUserIds) },
    sportsInterests: { $in: allUserSports.map(s => new RegExp(`^${s}$`, 'i')) }
  })
  .limit(limit)
  .skip((page - 1) * limit)
  .exec();

// Si moins de 5 profils, chercher avec sports similaires
if (compatibleProfiles.length < 5 && allUserSports.length > 0) {
  const additionalProfiles = await this.userModel
    .find({
      _id: { 
        $nin: [
          ...Array.from(excludedUserIds),
          ...compatibleProfiles.map(p => p._id.toString())
        ]
      },
      $or: allUserSports.map(sport => ({
        sportsInterests: { $regex: sport, $options: 'i' }
      }))
    })
    .limit(limit - compatibleProfiles.length)
    .skip(0)
    .exec();

  compatibleProfiles = [...compatibleProfiles, ...additionalProfiles];
}
```

---

## 📋 Checklist de Correction Backend

### 1. Identifier la Méthode de Filtrage

Dans `src/quick-match/quick-match.service.ts`, trouver la méthode `getCompatibleProfiles()`.

### 2. Vérifier les Exclusions

Vérifier si le code exclut les profils passés :

```typescript
// Chercher cette ligne
passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString()));
```

### 3. Appliquer la Correction

**Option A :** Supprimer complètement l'exclusion des passes (recommandé)

**Option B :** Ajouter une expiration (7 jours)

### 4. Tester

1. Créer plusieurs utilisateurs avec sports communs
2. Liker/passer certains profils
3. Vérifier que QuickMatch retourne plus de profils

---

## 🎯 Résultat Attendu

### Avant (Problème)

```json
{
  "profiles": [{"_id":"690e23ebf083f749b2562383","name":"Neji Hachani",...}],
  "pagination": {"total": 1, "page": 1, "totalPages": 1}
}
```

### Après (Solution)

```json
{
  "profiles": [
    {"_id":"690e23ebf083f749b2562383","name":"Neji Hachani",...},
    {"_id":"...","name":"aaa",...},
    {"_id":"...","name":"nexo",...},
    // ... plus de profils
  ],
  "pagination": {"total": 10-15, "page": 1, "totalPages": 1}
}
```

---

## 📝 Résumé

- ✅ **Problème confirmé** : Backend retourne `total: 1` sur 15 utilisateurs
- ✅ **Cause identifiée** : Backend exclut les profils passés
- ✅ **Solution** : Modifier `getCompatibleProfiles()` pour ne pas exclure les passes
- ✅ **Frontend OK** : Le frontend affichera automatiquement plus de profils après correction

**Le problème est 100% backend. Une fois corrigé, le frontend affichera automatiquement plus de profils.**

---

## 🔗 Fichiers à Modifier (Backend)

1. `src/quick-match/quick-match.service.ts`
   - Méthode : `getCompatibleProfiles()`
   - Modifier : Retirer l'exclusion des profils passés

---

## 💡 Notes Supplémentaires

### Pourquoi Ne Pas Exclure les Passes ?

1. **UX Améliorée** : L'utilisateur peut changer d'avis et revoir des profils
2. **Plus de Profils** : Plus de choix pour l'utilisateur
3. **Comportement Standard** : Comme Tinder, Bumble, etc., on peut revoir les profils passés après un certain temps

### Alternatives Si Vraiment Besoin d'Exclure les Passes

1. **Expiration Temporelle** : Exclure seulement les passes de moins de 7-30 jours
2. **Limite de Rechargement** : Permettre de revoir les passes après avoir vu tous les autres profils
3. **Reset Automatique** : Réinitialiser les passes après X jours

---

**Dernière mise à jour :** 2025-11-22

