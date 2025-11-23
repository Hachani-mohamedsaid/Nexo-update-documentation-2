# 🔧 Correction du Formulaire Strava

## ❌ Problème Identifié

Dans le formulaire "Modifier l'application" Strava, j'ai vu :
- **"Site Web"**: `hexofitness://strava/callback` ← **ERREUR !**
- **"Domaine du rappel pour l'autorisation"**: `nexofitness` ← ✅ Correct

## ✅ Solution

### Étape 1 : Corriger le champ "Site Web"

Le champ "Site Web" ne doit **PAS** contenir le deep link `hexofitness://strava/callback`.

**Options pour le champ "Site Web" :**

**Option A : Mettre une URL HTTP valide**
```
https://yourwebsite.com
```
ou
```
https://github.com
```

**Option B : Laisser vide ou mettre n'importe quelle URL valide**
```
https://example.com
```

**⚠️ IMPORTANT :** Le champ "Site Web" est juste informatif pour Strava. Il ne doit **PAS** contenir le deep link `nexofitness://strava/callback`.

### Étape 2 : Vérifier le champ "Domaine du rappel pour l'autorisation"

Le champ **"Domaine du rappel pour l'autorisation"** est correctement configuré :
```
nexofitness
```

✅ **C'est exactement ce qu'il faut !** Pas de `://`, pas de `/strava/callback`, juste `nexofitness`.

### Étape 3 : Configuration Finale

Dans le formulaire "Modifier l'application", configurez :

```
Nom de l'application: nexofitness
Catégorie: Bienfaisance (ou autre)
Site Web: https://example.com  ← Changez ça !
Description de l'application: Application fitness pour synchroniser vos activités Strava
Domaine du rappel pour l'autorisation: nexofitness  ← Laissez tel quel
```

### Étape 4 : Enregistrer

1. Cliquez sur **"Enregistrer"** (bouton orange en bas)
2. **Attendez 1-2 minutes** pour que Strava propage les changements
3. Testez à nouveau dans votre application Android

## 🔍 Pourquoi cette correction ?

- Le champ **"Site Web"** est juste pour donner une URL de site web à Strava (optionnel)
- Le champ **"Domaine du rappel pour l'autorisation"** est celui qui compte pour OAuth
- Le deep link `nexofitness://strava/callback` est envoyé dans l'URL OAuth, mais le domaine configuré (`nexofitness`) doit correspondre au schéma du deep link

## ✅ Vérification

Après avoir corrigé et sauvegardé :

1. Le champ "Site Web" contient une URL HTTP valide (ou est vide)
2. Le champ "Domaine du rappel pour l'autorisation" contient `nexofitness`
3. Vous avez cliqué sur "Enregistrer"
4. Vous avez attendu 1-2 minutes

Ensuite, testez à nouveau dans votre app Android. L'erreur "invalid redirect_uri" devrait disparaître ! 🎉
