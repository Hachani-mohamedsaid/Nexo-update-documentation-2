# Guide de Connexion Strava OAuth

## 📋 État Actuel

D'après les logs :
- ✅ **Strava est détecté comme installé** sur votre appareil
- ✅ **Le code est correctement configuré** (credentials, deep links, etc.)
- ⚠️ **Strava n'est pas encore connecté via OAuth** - cela signifie que vous n'avez pas encore lancé le flux d'authentification

## 🚀 Étapes pour Connecter Strava

### 1. Vérifier la Configuration Strava (déjà fait ✅)

Vous avez déjà configuré votre application Strava avec :
- **Nom de l'application**: `nexofitness`
- **Domaine du rappel pour l'autorisation**: `nexofitness` ✅
- **Site Web**: `nexofitness://strava/callback` ✅

✅ **Cette configuration est correcte !**

### 2. Lancer le Flux OAuth

1. **Ouvrez l'application NexoFitness**
2. **Allez dans l'écran AI Coach** (icône coach en bas de l'écran)
3. **Cliquez sur le bouton "Connecter Strava"**

   → Une page web devrait s'ouvrir dans votre navigateur

4. **Sur la page web Strava** :
   - Connectez-vous à votre compte Strava si nécessaire
   - Cliquez sur **"Autoriser"** pour donner accès à vos données d'activité

5. **Après avoir autorisé** :
   - Vous serez automatiquement redirigé vers l'application NexoFitness
   - Le token OAuth sera automatiquement échangé et sauvegardé
   - L'application sera marquée comme synchronisée

### 3. Vérifier la Connexion

Après avoir autorisé Strava, retournez à l'écran AI Coach. Vous devriez voir :
- ✅ Les statistiques de la semaine (workouts, calories, minutes)
- ✅ Le streak (série de jours consécutifs)

## 🔍 Dépannage

### Si le navigateur ne s'ouvre pas

Vérifiez les logs dans Logcat pour voir s'il y a une erreur. Recherchez :
```
StravaOAuthHelper: 🚀 Launching Strava OAuth flow...
```

### Si vous êtes redirigé mais rien ne se passe

1. Vérifiez les logs dans Logcat :
   ```
   MainActivity: ✅ Strava OAuth callback received: ...
   ```

2. Si le callback n'apparaît pas, vérifiez que :
   - L'intent-filter dans `AndroidManifest.xml` est correct (il l'est ✅)
   - Le `launchMode="singleTop"` est configuré (il l'est ✅)

### Si vous voyez une erreur "Invalid callback URI"

Vérifiez que dans votre application Strava, le **"Domaine du rappel pour l'autorisation"** est bien `nexofitness` (sans `/callback`).

## 📝 Logs à Surveiller

Lorsque vous cliquez sur "Connecter Strava", vous devriez voir dans Logcat :

```
StravaOAuthHelper: 🚀 Launching Strava OAuth flow...
StravaOAuthHelper: ✅ Browser opened for Strava OAuth
```

Puis, après avoir autorisé dans le navigateur :

```
MainActivity: ✅ Strava OAuth callback received: nexofitness://strava/callback?code=...
MainActivity: ✅ Authorization code extracted: ...
MainActivity: 🔄 Exchanging authorization code for access token...
MainActivity: ✅✅✅ Strava OAuth success! Token received
MainActivity: ✅ Strava marked as synced
```

## ✅ Test Rapide

Pour tester rapidement si tout fonctionne :

1. Cliquez sur **"Connecter Strava"** dans l'écran AI Coach
2. Autorisez l'accès dans le navigateur
3. Retournez à l'app
4. Cliquez sur **"J'ai accordé les permissions - Vérifier"**
5. Les données Strava devraient maintenant s'afficher !

---

**Note**: Si vous avez déjà autorisé Strava une fois mais que la connexion n'est pas établie, vous pouvez essayer de déconnecter et reconnecter, ou simplement relancer le flux OAuth.

