# 🔍 Pourquoi Un Seul Profil Est Affiché ?

## ✅ Diagnostic Complet

### 1. **Vérification Frontend** ✅

Le code frontend est **correct** :
- ✅ Affiche **tous** les profils reçus : `val displayedProfiles = profiles`
- ✅ Affiche le profil à l'index actuel : `val currentProfile = displayedProfiles.getOrNull(currentIndex)`
- ✅ Pas de filtre qui limite à 1 profil
- ✅ Pas de limite artificielle

**Conclusion Frontend :** Le frontend affiche correctement **tous les profils** qu'il reçoit du backend.

---

### 2. **Vérification Backend** ❌

Les logs confirment que le backend ne retourne qu'un seul profil :

```
D/QuickMatchDataSource: Profiles loaded: 1
D/QuickMatchDataSource: Pagination: total=1, page=1, totalPages=1
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

**Conclusion Backend :** Le backend ne trouve qu'un seul profil compatible et le retourne.

---

## 🔍 Pourquoi le Backend Retourne Qu'un Seul Profil ?

### Analyse

Le leaderboard contient **15 utilisateurs** dans la base, mais QuickMatch ne retourne qu'**1 profil**.

### Raisons Possibles

#### 1. **Filtrage Trop Strict** (Probable)

Le backend exclut probablement :
- ✅ L'utilisateur connecté (normal)
- ✅ Les profils déjà **likés** (normal)
- ❌ Les profils déjà **passés** (⚠️ **PROBLÈME - Trop strict**)
- ✅ Les profils avec **matchs** (normal)
- ✅ Les utilisateurs sans **sports communs** (normal)

**Exemple concret :**
- Base de données : 15 utilisateurs
- Utilisateur connecté : 1 (exclu) → 14 restants
- Profils déjà likés : 5 (exclus) → 9 restants
- Profils déjà passés : 8 (exclus) → **1 restant** ⚠️
- Profils avec matchs : 0 → 1 restant
- Sports communs : Tous compatibles → **1 restant**

**Résultat :** Le backend retourne `total: 1`

---

#### 2. **Pas Assez d'Utilisateurs avec Sports Communs**

Si les autres utilisateurs n'ont pas de sports en commun avec l'utilisateur connecté :
- Sur 15 utilisateurs, peut-être que seul 1 a des sports communs
- Le backend filtre correctement, mais il n'y a vraiment qu'un seul profil compatible

---

#### 3. **Combinaison des Deux**

Les deux problèmes combinés :
- Peu d'utilisateurs avec sports communs
- + Filtrage strict des passes
- = Un seul profil disponible

---

## 🛠️ Solution

### Solution Immédiate (Backend)

**Modifier le backend pour ne PAS exclure les profils passés :**

Dans `src/quick-match/quick-match.service.ts`, méthode `getCompatibleProfiles()` :

```typescript
// ❌ SUPPRIMER cette ligne :
passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString()));

// ✅ Résultat : Ne plus exclure les profils passés
// Les utilisateurs pourront revoir les profils qu'ils ont passés
```

**Impact attendu :**
- Avant : 1 profil disponible (tous les autres passés)
- Après : 8-10 profils disponibles (profils passés inclus)

---

### Solution Alternative (Backend)

**Ajouter une expiration des passes :**

Exclure seulement les passes récents (moins de 7 jours) :

```typescript
const sevenDaysAgo = new Date();
sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);

const recentPasses = await this.passModel
  .find({
    fromUser: new Types.ObjectId(userId),
    createdAt: { $gte: sevenDaysAgo }
  })
  .select('toUser')
  .exec();

// Exclure seulement les passes récents
recentPasses.forEach((pass) => excludedUserIds.add(pass.toUser.toString()));
```

**Impact :**
- Les passes de plus de 7 jours ne sont plus exclus
- L'utilisateur peut revoir les anciens profils passés

---

## 📊 Résumé Simple

| Élément | État | Action |
|---------|------|--------|
| **Frontend** | ✅ OK | Rien à faire |
| **Backend - Retourne** | ❌ `total: 1` | **À corriger** |
| **Cause** | ❌ Filtrage trop strict | **Retirer exclusion des passes** |
| **Solution** | ✅ Modifier backend | Voir `SOLUTION_BACKEND_QUICKMATCH.md` |

---

## 🎯 Réponse Directe

**Pourquoi un seul profil est affiché ?**

**Parce que le backend ne retourne qu'un seul profil (`total: 1`).**

Le frontend affiche correctement tous les profils qu'il reçoit. Le problème vient du backend qui filtre trop strictement (exclut les profils passés), laissant seulement 1 profil disponible.

**Solution :** Modifier le backend pour ne pas exclure les profils passés. Une fois corrigé, le frontend affichera automatiquement plus de profils.

---

**Documents de référence :**
- `SOLUTION_BACKEND_QUICKMATCH.md` - Guide complet pour corriger le backend
- `PROBLEME_BACKEND_UN_SEUL_PROFIL.md` - Diagnostic détaillé

