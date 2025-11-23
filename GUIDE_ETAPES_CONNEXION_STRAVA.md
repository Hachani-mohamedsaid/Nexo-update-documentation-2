# Guide Étape par Étape : Connexion Strava OAuth

## 📱 Instructions Complètes

### Étape 1 : Ouvrir l'écran AI Coach

1. **Lancez l'application NexoFitness** sur votre téléphone
2. **Allez dans l'écran AI Coach** :
   - En bas de l'écran, cliquez sur l'icône du **coach** (🤖)
   - Vous devriez voir l'écran **"Synchronisation Fitness requise"**

### Étape 2 : Lancer l'authentification OAuth

1. **Cliquez sur le bouton "Connecter Strava"** (bouton bleu)
   - Une page web Strava devrait s'ouvrir dans votre navigateur
   - Si le navigateur ne s'ouvre pas, vérifiez les logs dans Logcat

### Étape 3 : Autoriser l'accès sur Strava

Sur la page web Strava qui s'ouvre :

1. **Connectez-vous à votre compte Strava** si vous n'êtes pas déjà connecté
   - Entrez votre email et mot de passe Strava
   - Ou utilisez Google Sign-In si configuré

2. **Autorisez l'accès** :
   - Vous verrez une page demandant l'autorisation pour l'application "nexofitness"
   - Cette page affichera les permissions demandées :
     - ✅ Accès aux données d'activité
     - ✅ Accès au profil
   - **Cliquez sur le bouton "Autoriser"** (ou "Authorize" en anglais)

### Étape 4 : Redirection vers l'application

Après avoir autorisé :

1. **Strava vous redirigera automatiquement** vers l'application NexoFitness
   - Le deep link `nexofitness://strava/callback?code=...` sera déclenché
   - L'application s'ouvrira automatiquement

2. **Le code d'autorisation sera échangé contre un token d'accès**
   - Cela se fait automatiquement en arrière-plan
   - Vous verrez dans les logs : `✅✅✅ Strava OAuth success!`

### Étape 5 : Vérifier la connexion

1. **Retournez à l'écran AI Coach** dans l'application
2. **Cliquez sur "J'ai accordé les permissions - Vérifier"**
   - Ou simplement attendez quelques secondes, la connexion sera vérifiée automatiquement

3. **Vous devriez maintenant voir** :
   - ✅ L'écran AI Coach normal (pas l'écran de synchronisation)
   - ✅ Vos statistiques de la semaine (workouts, calories, minutes)
   - ✅ Votre streak (série de jours consécutifs)

## 🔍 Vérification dans les Logs

Pendant le processus, vous devriez voir dans Logcat :

### Quand vous cliquez sur "Connecter Strava" :
```
StravaOAuthHelper: 🚀 Launching Strava OAuth flow...
StravaOAuthHelper: ✅ Browser opened for Strava OAuth
```

### Quand vous êtes redirigé vers l'app :
```
MainActivity: ✅ Strava OAuth callback received: nexofitness://strava/callback?code=...
MainActivity: ✅ Authorization code extracted: ...
MainActivity: 🔄 Exchanging authorization code for access token...
```

### Si la connexion réussit :
```
MainActivity: ✅✅✅ Strava OAuth success! Token received
MainActivity: ✅ Strava marked as synced
AICoachViewModel: ✅ Strava connecté via OAuth! Marking as synced and refreshing...
```

### Si la connexion échoue :
```
MainActivity: ❌ Strava OAuth error: [message d'erreur]
```

## ❌ Dépannage

### Problème 1 : Le navigateur ne s'ouvre pas

**Symptôme** : Rien ne se passe quand vous cliquez sur "Connecter Strava"

**Solutions** :
1. Vérifiez les logs Logcat pour voir s'il y a une erreur
2. Vérifiez que vous avez une connexion Internet
3. Vérifiez que les credentials Strava sont corrects dans `StravaOAuthHelper.kt`

### Problème 2 : Erreur "Invalid redirect_uri"

**Symptôme** : Strava affiche une erreur lors de l'autorisation

**Solutions** :
1. Vérifiez que dans les paramètres Strava, le **"Domaine du rappel pour l'autorisation"** est bien `nexofitness`
2. Vérifiez que dans le code, `REDIRECT_URI` est `nexofitness://strava/callback`

### Problème 3 : Vous êtes redirigé mais rien ne se passe

**Symptôme** : Vous êtes redirigé vers l'app mais l'écran de synchronisation reste affiché

**Solutions** :
1. Vérifiez les logs Logcat pour voir si le callback est reçu
2. Vérifiez que `MainActivity.onNewIntent()` est bien appelé
3. Essayez de cliquer sur "J'ai accordé les permissions - Vérifier"

### Problème 4 : Erreur "Invalid client_secret"

**Symptôme** : Les logs montrent une erreur lors de l'échange du code

**Solutions** :
1. Vérifiez que le Client Secret dans `StravaOAuthHelper.kt` correspond à celui dans Strava
2. Si vous avez généré un nouveau secret dans Strava, mettez à jour le code

### Problème 5 : Le token expire rapidement

**Symptôme** : La connexion fonctionne au début mais ne fonctionne plus après quelques heures

**Solutions** :
1. C'est normal - les tokens Strava expirent après 6 heures
2. Le code rafraîchit automatiquement le token avec le refresh token
3. Si cela ne fonctionne pas, déconnectez et reconnectez Strava

## 📝 Checklist Complète

Avant de commencer, vérifiez que :

- [ ] Strava est installé sur votre téléphone
- [ ] Le Client ID et Client Secret sont corrects dans le code
- [ ] Le domaine du rappel est configuré dans Strava (`nexofitness`)
- [ ] L'intent-filter est configuré dans `AndroidManifest.xml`
- [ ] `MainActivity` a `launchMode="singleTop"`

## ✅ Après la Connexion Réussie

Une fois connecté, vous devriez voir dans AI Coach :

1. **Statistiques de la Semaine** :
   - Nombre de workouts
   - Calories brûlées
   - Minutes d'activité
   - Streak (série de jours)

2. **Les données sont récupérées depuis Strava** :
   - Toutes vos activités Strava de la semaine
   - Les données de calories et de temps sont calculées depuis vos activités

## 🔄 Reconnexion

Si vous devez vous reconnecter :

1. L'application vérifie automatiquement si le token est valide
2. Si le token est expiré, elle le rafraîchit automatiquement
3. Si le refresh token est invalide, vous devrez vous reconnecter via OAuth

---

**Note** : La connexion Strava est optionnelle. Vous pouvez toujours utiliser AI Coach sans Strava, mais avec des données par défaut.

