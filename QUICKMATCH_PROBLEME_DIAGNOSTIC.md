# 🔍 Diagnostic du Problème QuickMatch - Un Seul Profil Affiché

## 📋 Problème Identifié

Le QuickMatch affiche **toujours un seul profil** au lieu de plusieurs profils.

## 🔎 Analyse des Logs

D'après les logs de l'application :

```
QuickMatchDataSource: Pagination: total=1, page=1, totalPages=1, limit=50
QuickMatchDataSource: ⚠️ Only ONE profile returned!
```

**Le backend ne retourne qu'un seul profil** (`total=1`), même avec `limit=50`.

## 🎯 Causes Possibles

### 1. **Backend exclut les profils likés/passés**
Le backend NestJS exclut automatiquement les profils qui ont été likés ou passés, même si c'est le seul profil disponible.

**Code backend concerné** (d'après `QUICKMATCH_NESTJS_COMPLETE_GUIDE.md`) :
```typescript
// Le backend exclut les profils likés, passés ou matchés
const excludedUserIds = new Set<string>();
likedProfiles.forEach((like) => excludedUserIds.add(like.toUser.toString()));
passedProfiles.forEach((pass) => excludedUserIds.add(pass.toUser.toString()));
```

### 2. **Filtrage trop strict par sports communs**
Le backend filtre uniquement les utilisateurs qui ont **au moins un sport en commun** avec l'utilisateur connecté. Si peu d'utilisateurs ont des sports communs, il n'y aura qu'un seul profil.

### 3. **Peu d'utilisateurs dans la base de données**
Il n'y a peut-être vraiment qu'un seul utilisateur compatible dans la base de données.

## ✅ Solutions Appliquées (Côté Android)

### Solution Temporaire 1 : Réinitialiser `excludedProfileIds`
Si le backend ne retourne qu'un seul profil et qu'il est exclu, on réinitialise la liste des exclus pour permettre de l'afficher :

```kotlin
if (newProfiles.size == 1 && 
    newProfiles[0].id in excludedProfileIds && 
    currentState.profiles.isEmpty()) {
    excludedProfileIds.clear()
}
```

### Solution Temporaire 2 : Ne pas exclure le seul profil disponible
Si le backend ne retourne qu'un seul profil, on le permet même s'il a été liké précédemment :

```kotlin
if (newProfiles.size == 1 && currentState.profiles.isEmpty()) {
    excludedProfileIds.remove(newProfiles[0].id)
}
```

### Solution Temporaire 3 : Rechargement automatique
Quand la liste devient vide ou ne contient qu'un seul profil, on recharge automatiquement :

```kotlin
if ((displayedProfiles.size == 1 || displayedProfiles.isEmpty()) && !hasTriedAutoReload) {
    viewModel.loadProfiles(append = false)
}
```

## 🔧 Solutions Recommandées (Côté Backend)

### Solution 1 : Ne pas exclure les profils likés si c'est le seul disponible
Modifier le backend pour retourner les profils likés/passés si c'est le seul profil disponible :

```typescript
// Dans quick-match.service.ts
async getCompatibleProfiles(userId: string, page: number = 1, limit: number = 50) {
  // ... code existant ...
  
  // Récupérer les profils compatibles
  const compatibleProfiles = await this.userModel.find(query).limit(limit).exec();
  
  // Si moins de 3 profils, inclure aussi les profils likés/passés
  if (compatibleProfiles.length < 3) {
    const allProfiles = await this.userModel.find({
      _id: { $ne: userId },
      sportsInterests: { $in: allUserSports }
    }).limit(limit).exec();
    
    return allProfiles; // Retourner tous les profils compatibles, même likés
  }
  
  return compatibleProfiles;
}
```

### Solution 2 : Relâcher le filtrage par sports communs
Si peu d'utilisateurs ont des sports communs, retourner plus de profils sans ce filtre strict :

```typescript
// Option 1 : Retourner tous les utilisateurs si moins de 5 avec sports communs
if (compatibleProfiles.length < 5) {
  const allUsers = await this.userModel.find({
    _id: { $ne: userId }
  }).limit(limit).exec();
  return allUsers;
}

// Option 2 : Retourner les utilisateurs avec au moins un sport similaire (pas exact)
// Utiliser une recherche plus flexible
```

### Solution 3 : Augmenter le nombre de profils retournés
Augmenter la limite par défaut ou retourner plus de pages :

```typescript
// Dans le controller
@Get('profiles')
async getProfiles(
  @Request() req,
  @Query('page') page: number = 1,
  @Query('limit') limit: number = 100 // Augmenter de 20 à 100
) {
  // ...
}
```

## 📊 Logs de Diagnostic

Les logs suivants ont été ajoutés pour diagnostiquer le problème :

### Dans `QuickMatchDataSource` :
- Nombre de profils retournés
- Informations de pagination (total, page, totalPages)
- Avertissements si seulement 1 profil
- Causes possibles et solutions

### Dans `QuickMatchViewModel` :
- Profils exclus vs profils retournés
- Logique de filtrage
- Réinitialisation de `excludedProfileIds`
- Avertissements si liste vide ou un seul profil

### Dans `QuickMatchScreen` :
- Nombre de profils affichés dans l'UI
- Rechargement automatique déclenché

## 🧪 Comment Tester

1. **Vérifier les logs** dans Logcat :
   ```
   Filter: QuickMatchDataSource OR QuickMatchViewModel OR QuickMatchScreen
   ```

2. **Vérifier la réponse du backend** :
   - Ouvrir les logs HTTP (OkHttpClient)
   - Vérifier `pagination.total` dans la réponse JSON
   - Si `total=1`, le problème vient du backend

3. **Tester avec plusieurs utilisateurs** :
   - Créer plusieurs comptes avec des sports communs
   - Vérifier si le backend retourne plus de profils

## 🎯 Prochaines Étapes

1. ✅ **Fait** : Améliorer les logs de diagnostic
2. ✅ **Fait** : Ajouter des solutions temporaires côté Android
3. ⏳ **À faire** : Modifier le backend pour retourner plus de profils
4. ⏳ **À faire** : Tester avec plusieurs utilisateurs
5. ⏳ **À faire** : Relâcher les filtres backend si nécessaire

## 📝 Notes

- Les solutions temporaires côté Android permettent d'afficher le profil unique même après l'avoir liké
- Le vrai problème est côté backend : il ne retourne qu'un seul profil
- Il faut modifier le backend pour retourner plus de profils ou relâcher les filtres

