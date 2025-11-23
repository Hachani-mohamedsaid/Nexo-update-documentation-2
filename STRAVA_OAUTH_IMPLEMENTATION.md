# 🚴 Implémentation OAuth Strava - Guide Complet

## ✅ État de l'implémentation

L'intégration OAuth avec Strava est maintenant **complète** ! Voici ce qui a été implémenté :

### 1. ✅ Configuration AndroidManifest
- Deep link configuré : `nexofitness://strava/callback`
- Intent-filter ajouté pour gérer le callback OAuth

### 2. ✅ Gestion du callback OAuth
- `MainActivity` gère automatiquement le callback OAuth
- Échange automatique du code d'autorisation contre un token d'accès
- Marquage automatique de la synchronisation comme réussie

### 3. ✅ Récupération des données réelles
- `getWeeklyStats()` récupère maintenant les **vraies données** depuis l'API Strava :
  - **Workouts** : nombre d'activités de la semaine
  - **Calories** : total des calories brûlées
  - **Minutes** : total des minutes d'activité (basé sur `moving_time`)
  - **Streak** : nombre de jours consécutifs avec au moins une activité

### 4. ✅ Gestion des tokens
- Rafraîchissement automatique du token si expiré
- Stockage sécurisé des tokens dans SharedPreferences
- Vérification de l'expiration avant chaque requête

---

## 🔧 Configuration requise

### Étape 1 : Créer une application Strava

1. Allez sur https://www.strava.com/settings/api
2. Cliquez sur **"Create App"** ou **"Create New App"**
3. Remplissez les informations :
   - **Application Name** : `Nexo Fitness` (ou le nom de votre choix)
   - **Category** : `Training`
   - **Club** : (optionnel)
   - **Website** : (optionnel, vous pouvez mettre votre site ou `http://localhost`)
   - **Authorization Callback Domain** : `nexofitness://strava/callback`
4. Cliquez sur **"Create"**

### Étape 2 : Récupérer les credentials

Après la création, vous obtiendrez :
- **Client ID** : Un nombre (ex: `12345`)
- **Client Secret** : Une chaîne de caractères (ex: `abc123def456...`)

### Étape 3 : Configurer les credentials dans l'application

Ouvrez le fichier `app/src/main/java/com/example/damandroid/auth/StravaOAuthHelper.kt` et remplacez :

```kotlin
private const val STRAVA_CLIENT_ID = "YOUR_STRAVA_CLIENT_ID"
private const val STRAVA_CLIENT_SECRET = "YOUR_STRAVA_CLIENT_SECRET"
```

Par vos vraies valeurs :

```kotlin
private const val STRAVA_CLIENT_ID = "12345"  // Votre Client ID
private const val STRAVA_CLIENT_SECRET = "abc123def456..."  // Votre Client Secret
```

---

## 📱 Utilisation

### Pour l'utilisateur

1. **Ouvrir AI Coach** dans l'application
2. Si Strava n'est pas connecté, l'application affichera un message
3. **Cliquer sur "Connecter Strava"**
4. L'application ouvrira le navigateur pour l'authentification Strava
5. **Autoriser l'application** sur la page Strava
6. L'utilisateur sera **automatiquement redirigé** vers l'app
7. Les **données réelles** seront maintenant synchronisées !

### Flux OAuth

```
1. Utilisateur clique "Connecter Strava"
   ↓
2. Application ouvre: https://www.strava.com/oauth/authorize?...
   ↓
3. Utilisateur autorise l'application sur Strava
   ↓
4. Strava redirige vers: nexofitness://strava/callback?code=...
   ↓
5. Application reçoit le code via onNewIntent()
   ↓
6. Application échange le code contre un token d'accès
   ↓
7. Token sauvegardé, synchronisation marquée comme réussie
   ↓
8. getWeeklyStats() récupère maintenant les vraies données
```

---

## 🔍 Fonctionnalités implémentées

### Récupération des statistiques hebdomadaires

La méthode `getWeeklyStats()` :
- ✅ Récupère toutes les activités de la semaine (depuis lundi 00:00)
- ✅ Calcule le total des calories
- ✅ Calcule le total des minutes d'activité
- ✅ Compte le nombre de workouts
- ✅ Calcule le streak (jours consécutifs avec activité)

### Gestion automatique des tokens

- ✅ Vérification de l'expiration avant chaque requête
- ✅ Rafraîchissement automatique si le token est expiré
- ✅ Gestion des erreurs (401 = token expiré → refresh automatique)

### Calcul du streak

Le streak est calculé en :
1. Groupant les activités par jour
2. Partant d'aujourd'hui et remontant dans le temps
3. Comptant les jours consécutifs avec au moins une activité
4. S'arrêtant au premier jour sans activité

---

## 📊 Structure des données récupérées

### Exemple de réponse Strava API

```json
[
  {
    "id": 123456789,
    "name": "Morning Run",
    "type": "Run",
    "distance": 5000.0,
    "moving_time": 1800,
    "calories": 350.0,
    "start_date_local": "2024-01-15T07:00:00Z"
  },
  ...
]
```

### Conversion en WeeklyStats

- **Workouts** : `activities.size`
- **Calories** : `sum(activity.calories ?? 0)`
- **Minutes** : `sum(activity.moving_time / 60)`
- **Streak** : Calculé à partir des dates des activités

---

## ⚠️ Notes importantes

### Sécurité

⚠️ **IMPORTANT** : Pour la production, l'échange de code contre token devrait être fait côté **backend** plutôt que côté client. L'implémentation actuelle fonctionne mais expose le `Client Secret` dans le code de l'application.

**Recommandation pour production** :
1. Créer un endpoint backend : `POST /api/strava/exchange-token`
2. Le backend échange le code contre le token
3. Le backend renvoie le token à l'app
4. Le `Client Secret` reste sécurisé sur le serveur

### Limitations de l'API Strava

- Les tokens d'accès expirent après **6 heures** d'inactivité
- Le refresh token permet de renouveler le token d'accès
- Rate limiting : 600 requêtes toutes les 15 minutes, 30000 par jour

### Scopes utilisés

- `activity:read_all` : Lire toutes les activités (privées et publiques)
- `profile:read_all` : Lire le profil complet

---

## 🐛 Dépannage

### Le callback OAuth ne fonctionne pas

1. Vérifiez que l'URL de callback dans Strava est exactement : `nexofitness://strava/callback`
2. Vérifiez que l'intent-filter dans AndroidManifest.xml est correct
3. Vérifiez les logs avec `adb logcat | grep StravaOAuth`

### Les données ne s'affichent pas

1. Vérifiez que le token est valide : `adb logcat | grep StravaTokenStore`
2. Vérifiez que l'utilisateur a des activités dans Strava
3. Vérifiez les erreurs API : `adb logcat | grep StravaDataSource`

### Token expiré

Le refresh automatique devrait se déclencher. Si ce n'est pas le cas :
1. Vérifiez que le refresh token est sauvegardé
2. Vérifiez les logs de refresh : `adb logcat | grep "Token refresh"`

---

## 📝 Fichiers modifiés

1. ✅ `AndroidManifest.xml` - Ajout de l'intent-filter pour le deep link
2. ✅ `MainActivity.kt` - Gestion du callback OAuth
3. ✅ `StravaDataSource.kt` - Implémentation de `getWeeklyStats()` avec vraies données
4. ✅ `StravaOAuthHelper.kt` - Ajout de la méthode `refreshAccessTokenIfNeeded()`
5. ✅ `StravaApiService.kt` - Correction de l'endpoint `getAthleteStats()`

---

## 🎉 Prochaines étapes

L'intégration OAuth Strava est maintenant **complète** ! 

Pour utiliser l'application :
1. ✅ Configurez vos credentials Strava (Client ID et Client Secret)
2. ✅ Testez la connexion OAuth
3. ✅ Vérifiez que les données s'affichent correctement

**Les données réelles (workouts, calories, minutes) sont maintenant récupérées depuis Strava !** 🚴‍♂️💪

