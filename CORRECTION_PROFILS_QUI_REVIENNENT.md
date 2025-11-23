# ✅ Correction - Profils qui Reviennent après Like/Pass

## 🔍 Problème Identifié

Après avoir liké ou passé un profil, celui-ci revient dans la liste lorsque les profils sont rechargés depuis le backend.

### Cause Racine

Le backend retourne toujours les mêmes profils (y compris ceux déjà likés/passés) lors des appels à `/quick-match/profiles`. Lors d'un rechargement, ces profils étaient réajoutés à la liste même s'ils avaient déjà été traités.

**Flux du problème :**
1. L'utilisateur like/passe un profil → le profil est retiré de la liste localement ✅
2. Les profils sont rechargés depuis le backend (appel manuel ou automatique)
3. Le backend retourne les mêmes profils (y compris celui qui vient d'être liké/passé) ❌
4. Le profil revient dans la liste → **PROBLÈME** ❌

---

## ✅ Solution Appliquée

### 1. **Liste des Profils Exclus**

Ajout d'une liste persistante dans le ViewModel pour garder en mémoire les IDs des profils qui ont été likés ou passés :

```kotlin
class QuickMatchViewModel(...) : ViewModel() {
    // Liste des IDs de profils qui ont été likés ou passés pour éviter de les réafficher
    private val excludedProfileIds = mutableSetOf<String>()
    
    // ...
}
```

### 2. **Ajouter le Profil à la Liste Exclue lors du Like/Pass**

Quand un profil est liké ou passé, son ID est ajouté à la liste des exclus **AVANT** de le retirer de la liste :

```kotlin
fun likeProfile(profileId: String, onMatch: (MatchUserProfile) -> Unit) {
    viewModelScope.launch {
        // ...
        
        // Ajouter le profil à la liste des exclus pour éviter de le réafficher après un rechargement
        excludedProfileIds.add(profileId)
        
        // Retirer immédiatement le profil de la liste pour éviter les likes multiples
        _uiState.update { state ->
            state.copy(
                profiles = state.profiles.filter { it.id != profileId },
                error = null
            )
        }
        
        // ...
    }
}

fun passProfile(profileId: String) {
    viewModelScope.launch {
        // ...
        
        // Ajouter le profil à la liste des exclus pour éviter de le réafficher après un rechargement
        excludedProfileIds.add(profileId)
        
        // Retirer immédiatement le profil de la liste pour éviter les passes multiples
        _uiState.update { state ->
            state.copy(
                profiles = state.profiles.filter { it.id != profileId }
            )
        }
        
        // ...
    }
}
```

### 3. **Filtrer les Profils Exclus lors du Chargement**

Lors du chargement des profils depuis le backend, on filtre ceux qui sont dans la liste des exclus :

```kotlin
fun loadProfiles(append: Boolean = false) {
    viewModelScope.launch {
        // ...
        runCatching { getQuickMatchProfiles() }
            .onSuccess { newProfiles ->
                _uiState.update { currentState ->
                    val existingProfileIds = currentState.profiles.map { it.id }.toSet()
                    // Filtrer les profils déjà présents ET les profils exclus (likés/passés)
                    val uniqueNewProfiles = newProfiles.filter { 
                        it.id !in existingProfileIds && it.id !in excludedProfileIds 
                    }
                    
                    val finalProfiles = if (append && uniqueNewProfiles.isNotEmpty()) {
                        // Ajouter les nouveaux profils à la liste existante (en excluant déjà les profils exclus)
                        currentState.profiles + uniqueNewProfiles
                    } else {
                        // Remplacer la liste (comportement par défaut) mais exclure les profils exclus
                        newProfiles.filter { it.id !in excludedProfileIds }
                    }
                    
                    // ...
                }
            }
    }
}
```

---

## 🔄 Flux Corrigé

### Avant (Problème) :

```
1. L'utilisateur like un profil → profil retiré de la liste localement
2. Rechargement des profils → backend retourne le même profil ❌
3. Le profil revient dans la liste → **PROBLÈME** ❌
```

### Après (Solution) :

```
1. L'utilisateur like un profil → profil ajouté à `excludedProfileIds` ✅
2. Profil retiré de la liste localement ✅
3. Rechargement des profils → backend retourne le même profil
4. Le profil est filtré (car dans `excludedProfileIds`) ✅
5. Le profil ne revient pas dans la liste → **CORRIGÉ** ✅
```

---

## 📋 Modifications Appliquées

### `QuickMatchViewModel.kt`

1. ✅ **Liste des exclus** : `private val excludedProfileIds = mutableSetOf<String>()`
2. ✅ **Ajout lors du like** : `excludedProfileIds.add(profileId)` dans `likeProfile()`
3. ✅ **Ajout lors du pass** : `excludedProfileIds.add(profileId)` dans `passProfile()`
4. ✅ **Filtrage lors du chargement** : `newProfiles.filter { it.id !in excludedProfileIds }` dans `loadProfiles()`
5. ✅ **Logs de diagnostic** : Logs pour suivre le nombre de profils exclus

---

## ✅ Avantages

1. ✅ **Plus de profils qui reviennent** : Les profils likés/passés sont exclus de manière persistante
2. ✅ **Gestion robuste** : Même si le backend retourne les mêmes profils, ils sont filtrés
3. ✅ **Performance** : Filtrage efficace avec un `Set` pour les vérifications O(1)
4. ✅ **UX améliorée** : L'utilisateur ne voit plus les profils qu'il a déjà traités

---

## 🧪 Test

### Scénario 1 : Like un Profil

1. Charger QuickMatch → 12 profils affichés
2. Liker le premier profil → ✅ Profil retiré de la liste
3. Les profils sont rechargés → ✅ Le profil liké ne revient pas

### Scénario 2 : Pass un Profil

1. Charger QuickMatch → 12 profils affichés
2. Passer le premier profil → ✅ Profil retiré de la liste
3. Les profils sont rechargés → ✅ Le profil passé ne revient pas

### Scénario 3 : Mixte Like/Pass

1. Charger QuickMatch → 12 profils affichés
2. Liker le premier profil → ✅ Retiré
3. Passer le deuxième profil → ✅ Retiré
4. Les profils sont rechargés → ✅ Aucun des deux profils ne revient

### Scénario 4 : Rechargement Automatique

1. Liker plusieurs profils → ✅ Tous retirés et ajoutés à `excludedProfileIds`
2. Rechargement automatique (quand il reste peu de profils) → ✅ Les profils likés ne reviennent pas

---

## 📊 Résultat

- ✅ **Plus de profils qui reviennent** après like/pass
- ✅ **Gestion persistante** : Les profils exclus restent exclus même après rechargement
- ✅ **Comportement cohérent** : L'utilisateur ne voit plus les profils déjà traités
- ✅ **Performance optimisée** : Filtrage efficace avec un `Set`

Le problème des profils qui reviennent est maintenant résolu ! 🎉

