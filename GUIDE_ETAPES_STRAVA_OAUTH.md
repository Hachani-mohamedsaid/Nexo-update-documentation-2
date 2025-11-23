# 🚴 Guide Étape par Étape - Configuration OAuth Strava

Ce guide vous explique **pas à pas** comment configurer l'intégration OAuth Strava pour récupérer les données fitness réelles dans votre application.

---

## 📋 Table des matières

1. [Étape 1 : Créer une application Strava](#étape-1--créer-une-application-strava)
2. [Étape 2 : Configurer les credentials dans le code](#étape-2--configurer-les-credentials-dans-le-code)
3. [Étape 3 : Vérifier la configuration](#étape-3--vérifier-la-configuration)
4. [Étape 4 : Tester l'intégration](#étape-4--tester-lintégration)
5. [Dépannage](#dépannage)

---

## Étape 1 : Créer une application Strava

### 1.1 Aller sur le site Strava

1. Ouvrez votre navigateur web
2. Allez sur : **https://www.strava.com/settings/api**
3. Connectez-vous à votre compte Strava (ou créez-en un si nécessaire)

### 1.2 Créer une nouvelle application

1. Sur la page des paramètres API, cliquez sur le bouton **"Create App"** ou **"Create New App"**
2. Remplissez le formulaire :

   **Application Name** : `Nexo Fitness`
   - C'est le nom qui apparaîtra lors de l'autorisation

   **Category** : `Training`
   - Sélectionnez la catégorie qui correspond le mieux

   **Club** : (laissez vide)
   - Optionnel, vous pouvez laisser ce champ vide

   **Website** : `http://localhost` ou `https://votre-site.com`
   - Vous pouvez mettre n'importe quelle URL valide

   **Application Description** : (optionnel)
   - Description de votre application

3. **⚠️ IMPORTANT - Authorization Callback Domain** :
   ```
   nexofitness://strava/callback
   ```
   - Ceci est l'URL de callback que Strava utilisera pour rediriger vers votre application
   - **Ne changez pas cette valeur**, elle doit correspondre exactement à celle dans le code

4. Cliquez sur **"Create"**

### 1.3 Récupérer les credentials

Après avoir créé l'application, Strava vous affichera :

- **Client ID** : Un nombre (ex: `12345`)
- **Client Secret** : Une chaîne de caractères longue (ex: `abc123def456ghi789...`)

**⚠️ IMPORTANT :** 
- Notez ces deux valeurs quelque part de sûr
- Le Client Secret ne sera affiché qu'une seule fois
- Si vous le perdez, vous devrez créer une nouvelle application

---

## Étape 2 : Configurer les credentials dans le code

### 2.1 Ouvrir le fichier StravaOAuthHelper.kt

1. Dans Android Studio, ouvrez le fichier :
   ```
   app/src/main/java/com/example/damandroid/auth/StravaOAuthHelper.kt
   ```

2. Cherchez les lignes **28-29** qui contiennent :
   ```kotlin
   private const val STRAVA_CLIENT_ID = "YOUR_STRAVA_CLIENT_ID"
   private const val STRAVA_CLIENT_SECRET = "YOUR_STRAVA_CLIENT_SECRET"
   ```

### 2.2 Remplacer les valeurs

Remplacez ces deux lignes par vos vraies valeurs :

**Exemple :**
```kotlin
private const val STRAVA_CLIENT_ID = "12345"  // Votre Client ID
private const val STRAVA_CLIENT_SECRET = "abc123def456ghi789jkl012mno345pqr678stu901vwx234yz"  // Votre Client Secret
```

**✅ Faites attention à :**
- Garder les guillemets `" "`
- Ne pas mettre d'espaces avant ou après
- Utiliser exactement les valeurs données par Strava

### 2.3 Vérifier l'URL de callback

Vérifiez que la ligne **32** contient bien :
```kotlin
private const val REDIRECT_URI = "nexofitness://strava/callback"
```

Cette URL doit être **exactement la même** que celle configurée dans Strava.

### 2.4 Sauvegarder le fichier

1. Appuyez sur **Ctrl+S** (Windows/Linux) ou **Cmd+S** (Mac) pour sauvegarder
2. Ou cliquez sur **File > Save All**

---

## Étape 3 : Vérifier la configuration

### 3.1 Vérifier AndroidManifest.xml

Assurez-vous que l'intent-filter pour le deep link est présent :

1. Ouvrez : `app/src/main/AndroidManifest.xml`
2. Cherchez la section `<activity android:name=".MainActivity">`
3. Vérifiez qu'il y a un intent-filter comme ceci :
   ```xml
   <intent-filter>
       <action android:name="android.intent.action.VIEW" />
       <category android:name="android.intent.category.DEFAULT" />
       <category android:name="android.intent.category.BROWSABLE" />
       <data
           android:scheme="nexofitness"
           android:host="strava" />
   </intent-filter>
   ```

**✅ Si vous voyez cet intent-filter, c'est correct !**
**❌ Si ce n'est pas là, contactez-nous pour le rajouter.**

### 3.2 Vérifier MainActivity.kt

Vérifiez que le callback OAuth est géré :

1. Ouvrez : `app/src/main/java/com/example/damandroid/MainActivity.kt`
2. Cherchez la fonction `handleStravaOAuthCallback`
3. Si elle existe, c'est correct ✅

**✅ La fonction devrait gérer le callback automatiquement.**

---

## Étape 4 : Tester l'intégration

### 4.1 Rebuild l'application

1. Dans Android Studio, allez dans **Build > Clean Project**
2. Attendez que le nettoyage se termine
3. Ensuite, allez dans **Build > Rebuild Project**
4. Attendez que la compilation se termine

**⚠️ IMPORTANT :** Il est recommandé de rebuild après avoir changé les credentials.

### 4.2 Installer Strava sur votre appareil

1. Sur votre téléphone Android, ouvrez le **Play Store**
2. Cherchez **"Strava"**
3. Installez l'application Strava
4. Ouvrez Strava et connectez-vous à votre compte (ou créez-en un)

**✅ Strava doit être installé et vous devez être connecté.**

### 4.3 Tester l'authentification OAuth

1. **Ouvrez votre application** sur votre téléphone
2. **Connectez-vous** à votre compte dans l'app
3. **Ouvrez AI Coach** (cliquez sur le bouton AI Coach en bas)
4. Vous devriez voir un message demandant de connecter Strava
5. **Cliquez sur "Connecter Strava"**
6. Une **page web** devrait s'ouvrir (dans Chrome ou le navigateur par défaut)
7. **Autorisez l'application** sur la page Strava
8. Vous devriez être **automatiquement redirigé** vers votre application
9. **Revenez dans AI Coach** - les données devraient maintenant apparaître !

### 4.4 Vérifier que ça fonctionne

Après avoir connecté Strava, vérifiez dans AI Coach :

- ✅ **Workouts** : Nombre d'activités de la semaine
- ✅ **Calories** : Total des calories brûlées
- ✅ **Minutes** : Total des minutes d'activité
- ✅ **Streak** : Nombre de jours consécutifs

**Si vous voyez des valeurs différentes de 0, c'est que ça fonctionne ! 🎉**

---

## Dépannage

### ❌ Le bouton "Connecter Strava" ne fait rien

**Problème :** Les credentials ne sont peut-être pas correctement configurés.

**Solution :**
1. Vérifiez que vous avez bien remplacé `YOUR_STRAVA_CLIENT_ID` et `YOUR_STRAVA_CLIENT_SECRET`
2. Rebuild l'application (Build > Rebuild Project)
3. Désinstallez l'application sur votre téléphone
4. Réinstallez-la depuis Android Studio

### ❌ Erreur après avoir autorisé sur Strava

**Problème :** L'URL de callback ne correspond pas.

**Solution :**
1. Vérifiez dans Strava que l'URL de callback est : `nexofitness://strava/callback`
2. Vérifiez dans `StravaOAuthHelper.kt` que `REDIRECT_URI` est : `nexofitness://strava/callback`
3. Les deux doivent être **exactement identiques** (sans espaces, même casse)

### ❌ Les données restent à 0 après connexion

**Problème :** Soit pas d'activités Strava, soit le token n'est pas valide.

**Solution :**
1. Vérifiez dans l'application Strava que vous avez des activités enregistrées
2. Vérifiez que les activités ont été enregistrées cette semaine
3. Si vous avez des activités mais que ça ne fonctionne pas, regardez les logs :
   ```
   adb logcat | grep Strava
   ```

### ❌ Erreur "Token expired" ou "401 Unauthorized"

**Problème :** Le token d'accès a expiré.

**Solution :**
1. L'application devrait rafraîchir automatiquement le token
2. Si le problème persiste, déconnectez-vous et reconnectez-vous :
   - Dans votre application, allez dans les paramètres
   - Trouvez l'option pour déconnecter Strava
   - Reconnectez-vous

### ❌ L'application ne redirige pas après autorisation

**Problème :** Le deep link ne fonctionne pas.

**Solution :**
1. Vérifiez que l'intent-filter est bien dans `AndroidManifest.xml` (voir Étape 3.1)
2. Vérifiez que `launchMode="singleTop"` est dans la MainActivity
3. Essayez de fermer complètement l'application et de la rouvrir
4. Vérifiez les logs :
   ```
   adb logcat | grep "Strava OAuth"
   ```

### 📝 Vérifier les logs

Pour voir ce qui se passe, utilisez :

```bash
adb logcat | grep -E "Strava|OAuth"
```

Cela affichera tous les messages de log liés à Strava et OAuth.

---

## ✅ Checklist finale

Avant de tester, vérifiez que tout est configuré :

- [ ] Application Strava créée sur https://www.strava.com/settings/api
- [ ] Client ID récupéré et configuré dans `StravaOAuthHelper.kt`
- [ ] Client Secret récupéré et configuré dans `StravaOAuthHelper.kt`
- [ ] URL de callback dans Strava : `nexofitness://strava/callback`
- [ ] URL de callback dans le code : `nexofitness://strava/callback`
- [ ] Intent-filter configuré dans `AndroidManifest.xml`
- [ ] Application rebuildée (Build > Rebuild Project)
- [ ] Strava installé sur votre téléphone
- [ ] Compte Strava avec des activités

---

## 🎉 Félicitations !

Si tout fonctionne, vous devriez maintenant voir vos **vraies données Strava** dans AI Coach :
- Workouts de la semaine
- Calories brûlées
- Minutes d'activité
- Streak (jours consécutifs)

**Les données se synchroniseront automatiquement chaque fois que vous ouvrirez AI Coach !** 🚴‍♂️💪

---

## 📞 Besoin d'aide ?

Si vous avez des problèmes :
1. Vérifiez la section [Dépannage](#dépannage)
2. Consultez `STRAVA_OAUTH_IMPLEMENTATION.md` pour plus de détails techniques
3. Vérifiez les logs avec `adb logcat | grep Strava`

