# 🔍 Diagnostic - Un Seul Profil Affiché dans QuickMatch

## 📊 Analyse des Logs

D'après les logs fournis :

```
2025-11-22 01:20:39.760 QuickMatchDataSource: Profiles loaded: 1
2025-11-22 01:20:39.761 QuickMatchViewModel: loadProfiles: append=false, newProfiles=1, excluded=0
2025-11-22 01:20:39.761 QuickMatchViewModel: Final profiles count: 1 (existing: 0, uniqueNew: 1, excluded: 0)
```

**Réponse du backend :**
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

### Constat

- ✅ **Backend répond correctement** : Status 200 OK
- ❌ **Un seul profil retourné** : `total: 1` dans la pagination
- ✅ **Frontend affiche correctement** : Le frontend affiche le profil unique retourné par le backend

---

## 🔍 Cause Racine

Le problème est que **le backend ne retourne qu'un seul profil disponible**. C'est un problème **backend**, pas frontend.

Le frontend :
- ✅ Charge correctement les profils depuis le backend
- ✅ Affiche correctement les profils reçus
- ✅ Tente de charger plus de profils automatiquement quand il n'en reste qu'un seul
- ✅ Exclut les profils likés/passés pour éviter qu'ils reviennent

Le backend :
- ❌ Ne retourne qu'un seul profil (`total: 1`)
- ❌ Pagination indique `totalPages: 1`, donc il n'y a vraiment qu'un seul profil disponible

---

## ✅ Solutions Frontend (Déjà Appliquées)

### 1. **Rechargement Automatique**

Un `LaunchedEffect` tente automatiquement de charger plus de profils quand il n'en reste qu'un seul :

```kotlin
LaunchedEffect(displayedProfiles.size) {
    if (displayedProfiles.size == 1 && !hasTriedAutoReload) {
        delay(1500)
        hasTriedAutoReload = true
        viewModel.loadProfiles(append = true)
    }
}
```

### 2. **Liste des Profils Exclus**

Les profils likés/passés sont ajoutés à une liste d'exclusion pour éviter qu'ils reviennent :

```kotlin
private val excludedProfileIds = mutableSetOf<String>()
```

### 3. **Filtrage des Profils Exclus**

Lors du chargement, les profils exclus sont filtrés :

```kotlin
val finalProfiles = newProfiles.filter { it.id !in excludedProfileIds }
```

---

## 🔧 Solutions Backend (À Implémenter)

Le problème principal est **backend**. Le backend doit retourner plus de profils disponibles.

### Problèmes Probables Côté Backend

1. **Filtrage trop strict** : Le backend filtre peut-être trop de profils (likés/passés, sports/intérêts, etc.)
2. **Peu d'utilisateurs disponibles** : Il n'y a peut-être pas beaucoup d'utilisateurs dans la base de données
3. **Problème de pagination** : La pagination ne fonctionne peut-être pas correctement
4. **Filtrage par sports/intérêts** : Le filtre par sports/intérêts communs est peut-être trop strict

### Solutions Recommandées

1. **Réduire le filtrage** : Ne pas exclure définitivement les profils passés (ajouter une expiration)
2. **Augmenter le pool** : Retourner plus de profils même sans sports/intérêts exactement identiques
3. **Vérifier la pagination** : S'assurer que la pagination fonctionne correctement
4. **Vérifier les filtres** : Vérifier que les filtres ne sont pas trop stricts

---

## 📋 Vérifications à Faire

### Backend

1. ✅ Vérifier combien d'utilisateurs sont disponibles dans la base de données
2. ✅ Vérifier le filtre des profils likés/passés (sont-ils exclus définitivement ?)
3. ✅ Vérifier le filtre par sports/intérêts (est-il trop strict ?)
4. ✅ Vérifier la pagination (fonctionne-t-elle correctement ?)
5. ✅ Vérifier les logs backend pour voir combien de profils sont filtrés

### Frontend

1. ✅ Le frontend tente de charger plus de profils automatiquement
2. ✅ Les profils likés/passés sont exclus localement
3. ✅ Le frontend affiche correctement les profils reçus

---

## ✅ Conclusion

**Le problème d'un seul profil affiché est un problème backend**, pas frontend. Le frontend :
- ✅ Affiche correctement le profil unique reçu
- ✅ Tente de charger plus de profils automatiquement
- ✅ Gère correctement l'exclusion des profils likés/passés

**Pour résoudre le problème, le backend doit être modifié pour retourner plus de profils disponibles.**

Voir `PROBLEME_BACKEND_UN_SEUL_PROFIL.md` pour les solutions backend détaillées.

