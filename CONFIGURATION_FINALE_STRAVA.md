# ✅ Configuration Finale Strava OAuth

## 🎯 Ce qui a été fait

1. ✅ **Endpoint backend créé** : `https://apinest-production.up.railway.app/strava/callback`
2. ✅ **Code Android modifié** : Utilise maintenant l'URL HTTP au lieu du deep link direct

## 📝 Configuration Strava

### Étape 1 : Aller sur Strava API Settings

1. Allez sur : https://www.strava.com/settings/api
2. Cliquez sur **"Modifier"** (bouton orange)

### Étape 2 : Configurer les champs

Dans le formulaire "Modifier l'application", configurez :

```
Nom de l'application: nexofitness
Catégorie: Bienfaisance (ou autre)
Site Web: https://apinest-production.up.railway.app
Description de l'application: Application fitness pour synchroniser vos activités Strava
Domaine du rappel pour l'autorisation: apinest-production.up.railway.app
```

**⚠️ IMPORTANT :**
- **"Domaine du rappel pour l'autorisation"** doit être : `apinest-production.up.railway.app`
- Pas de `https://`, pas de `/strava/callback`, juste le domaine

### Étape 3 : Enregistrer

1. Cliquez sur **"Enregistrer"** (bouton orange en bas)
2. **Attendez 1-2 minutes** pour que Strava propage les changements

## 🔍 Vérification

### Vérifier que l'endpoint backend fonctionne

Avant de tester dans l'app, testez l'endpoint backend :

1. Ouvrez votre navigateur
2. Allez sur : `https://apinest-production.up.railway.app/strava/callback?code=test123`
3. Vous devriez être redirigé vers l'app Android avec : `nexofitness://strava/callback?code=test123`

Si ça fonctionne, l'endpoint backend est correctement configuré ! ✅

## 🚀 Test Final

Après avoir configuré Strava :

1. **Fermez complètement votre application Android**
2. **Relancez l'application**
3. **Allez dans AI Coach**
4. **Cliquez sur "Connecter Strava"**
5. Le navigateur devrait s'ouvrir
6. **Autorisez l'accès** sur Strava
7. Vous devriez être **automatiquement redirigé vers l'app** ✅

## 📋 Résumé de la Configuration

### Dans le code Android (`StravaOAuthHelper.kt`) :
```kotlin
REDIRECT_URI = "https://apinest-production.up.railway.app/strava/callback"
```

### Dans Strava :
```
Domaine du rappel pour l'autorisation: apinest-production.up.railway.app
Site Web: https://apinest-production.up.railway.app
```

### Dans le backend :
```
Endpoint: GET /strava/callback
Redirige vers: nexofitness://strava/callback?code=...
```

## ✅ Checklist

- [ ] Endpoint backend ajouté et déployé
- [ ] Code Android modifié (déjà fait ✅)
- [ ] Strava configuré avec le nouveau domaine
- [ ] Endpoint backend testé dans le navigateur
- [ ] Application Android testée

## 🐛 Dépannage

### Si l'endpoint backend ne redirige pas :

Vérifiez que l'endpoint est bien déployé :
- Testez : `https://apinest-production.up.railway.app/strava/callback?code=test`
- Devrait rediriger vers `nexofitness://strava/callback?code=test`

### Si Strava affiche encore "invalid redirect_uri" :

1. Vérifiez que le domaine dans Strava est exactement : `apinest-production.up.railway.app`
2. Pas de `https://` au début
3. Pas de `/strava/callback` à la fin
4. Attendez 2-3 minutes après avoir sauvegardé

### Si la redirection vers l'app ne fonctionne pas :

1. Vérifiez que l'intent-filter est bien dans `AndroidManifest.xml` (déjà fait ✅)
2. Testez le deep link manuellement : Ouvrez `nexofitness://strava/callback?code=test` dans un navigateur

---

**Après avoir configuré Strava et déployé le backend, testez et dites-moi si ça fonctionne !** 🚀

