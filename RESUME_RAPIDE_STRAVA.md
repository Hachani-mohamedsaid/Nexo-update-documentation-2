# 🚴 Résumé Rapide - Configuration Strava OAuth

## ✅ En 4 étapes simples

### 1️⃣ Créer l'application Strava
- Allez sur : https://www.strava.com/settings/api
- Cliquez "Create App"
- **Important :** URL de callback = `nexofitness://strava/callback`
- Notez votre **Client ID** et **Client Secret**

### 2️⃣ Configurer les credentials
- Ouvrez : `app/src/main/java/com/example/damandroid/auth/StravaOAuthHelper.kt`
- Lignes 28-29, remplacez :
  ```kotlin
  private const val STRAVA_CLIENT_ID = "VOTRE_CLIENT_ID_ICI"
  private const val STRAVA_CLIENT_SECRET = "VOTRE_CLIENT_SECRET_ICI"
  ```

### 3️⃣ Rebuild l'application
- Build > Rebuild Project dans Android Studio
- Réinstallez l'app sur votre téléphone

### 4️⃣ Tester
- Installez Strava sur votre téléphone
- Ouvrez votre app > AI Coach > "Connecter Strava"
- Autorisez sur la page web
- Les données apparaîtront automatiquement ! 🎉

---

## ⚠️ Important
- L'URL de callback doit être **exactement** : `nexofitness://strava/callback`
- Le Client Secret n'est affiché qu'une seule fois
- Strava doit être installé sur votre téléphone

---

Pour le guide détaillé, voir : `GUIDE_ETAPES_STRAVA_OAUTH.md`

