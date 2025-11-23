# Guide de Configuration Google Sign-In pour Android

Ce guide vous explique étape par étape comment configurer Google Sign-In pour votre application Android.

## 📋 Prérequis

- Un compte Google
- Accès à Google Cloud Console
- Votre application Android configurée

---

## 🔧 Étape 1 : Créer un Projet dans Google Cloud Console

1. **Allez sur Google Cloud Console** : https://console.cloud.google.com/

2. **Créez un nouveau projet** (ou sélectionnez un projet existant) :
   - Cliquez sur le sélecteur de projet en haut
   - Cliquez sur "Nouveau projet"
   - Donnez un nom à votre projet (ex: "DamAndroid App")
   - Cliquez sur "Créer"

3. **Attendez que le projet soit créé** (peut prendre quelques secondes)

---

## 🔑 Étape 2 : Activer Google Sign-In API

1. **Dans Google Cloud Console**, allez dans le menu latéral
2. Cliquez sur **"APIs & Services"** > **"Library"** (Bibliothèque)
3. Recherchez **"Google Sign-In API"**
4. Cliquez sur **"Google Sign-In API"**
5. Cliquez sur le bouton **"ENABLE"** (Activer)

---

## 🔐 Étape 3 : Créer les Identifiants OAuth 2.0

1. **Dans Google Cloud Console**, allez dans **"APIs & Services"** > **"Credentials"** (Identifiants)

2. **Cliquez sur "CREATE CREDENTIALS"** (Créer des identifiants) > **"OAuth client ID"**

3. **Si c'est la première fois**, vous devrez configurer l'écran de consentement OAuth :
   - Choisissez **"External"** (externe) pour les tests
   - Remplissez les informations requises :
     - **App name** : Nom de votre application (ex: "DamAndroid")
     - **User support email** : Votre email
     - **Developer contact information** : Votre email
   - Cliquez sur **"SAVE AND CONTINUE"** pour chaque étape
   - À la fin, cliquez sur **"BACK TO DASHBOARD"**

4. **Créer l'OAuth Client ID pour Android** :
   - Cliquez sur **"CREATE CREDENTIALS"** > **"OAuth client ID"**
   - Sélectionnez **"Android"** comme type d'application
   - **Name** : Donnez un nom (ex: "DamAndroid Android Client")
   - **Package name** : Entrez le package name de votre app
     - Pour notre projet : `com.example.damandroid`
   - **SHA-1 certificate fingerprint** : Vous obtiendrez cela à l'étape suivante
   - Cliquez sur **"CREATE"**

---

## 📱 Étape 4 : Obtenir votre SHA-1 Certificate Fingerprint

Le SHA-1 est unique pour chaque clé de signature. Vous devez l'obtenir pour deux cas :
- **Debug** : Pour tester l'application pendant le développement
- **Release** : Pour la version de production de l'application

### Pour Debug (Développement) :

#### Sur Windows (PowerShell) :
```powershell
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

#### Sur macOS/Linux :
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### Via Android Studio :
1. Ouvrez Android Studio
2. Allez dans **File** > **Settings** (ou **Preferences** sur Mac) > **Build, Execution, Deployment** > **Gradle**
3. Ouvrez le terminal en bas
4. Exécutez cette commande :
   ```
   ./gradlew signingReport
   ```
5. Copiez le SHA-1 qui s'affiche (commence par quelque chose comme `SHA1: A1:B2:C3:...`)

### Pour Release (Production) :

Si vous avez déjà une clé de release :

```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your-key-alias
```

**Remplacez :**
- `/path/to/your/release.keystore` : Le chemin vers votre fichier keystore
- `your-key-alias` : L'alias de votre clé

Si vous n'avez pas encore de clé de release, vous pouvez la créer plus tard.

---

## 📝 Étape 5 : Ajouter le SHA-1 dans Google Cloud Console

1. **Copiez le SHA-1** que vous avez obtenu à l'étape 4
   - Il ressemble à : `A1:B2:C3:D4:E5:F6:...`
   - **Important** : Copiez seulement la partie après "SHA1: " (sans "SHA1:")

2. **Retournez dans Google Cloud Console** :
   - Allez dans **"APIs & Services"** > **"Credentials"**
   - Trouvez votre **OAuth 2.0 Client ID** pour Android (créé à l'étape 3)
   - Cliquez sur l'icône de crayon (éditer) à droite

3. **Ajoutez le SHA-1** :
   - Dans le champ **"SHA-1 certificate fingerprint"**
   - Collez votre SHA-1 (sans "SHA1:" au début)
   - Cliquez sur **"SAVE"** (Enregistrer)

4. **Pour ajouter plusieurs SHA-1** (debug + release) :
   - Vous pouvez ajouter plusieurs SHA-1 en les séparant par des virgules ou en créant plusieurs OAuth Client IDs

---

## 🔧 Étape 6 : Télécharger le fichier google-services.json (Optionnel mais recommandé)

**Note** : Cette étape est optionnelle si vous utilisez uniquement Google Sign-In sans Firebase.

1. **Allez sur Firebase Console** : https://console.firebase.google.com/

2. **Créez un projet Firebase** (ou utilisez un projet existant)
   - Si vous créez un nouveau projet, associez-le au projet Google Cloud créé précédemment

3. **Ajoutez une application Android** :
   - Cliquez sur l'icône Android
   - Package name : `com.example.damandroid`
   - App nickname : "DamAndroid" (optionnel)
   - Cliquez sur **"Register app"**

4. **Téléchargez le fichier google-services.json**

5. **Placez le fichier** dans votre projet :
   - Copiez `google-services.json` dans le dossier `app/` de votre projet Android
   - Chemin final : `app/google-services.json`

6. **Ajoutez le plugin Google Services** (si vous utilisez Firebase) :
   - Dans `build.gradle.kts` (niveau projet), ajoutez :
     ```kotlin
     plugins {
         id("com.google.gms.google-services") version "4.4.0" apply false
     }
     ```
   - Dans `app/build.gradle.kts`, ajoutez :
     ```kotlin
     plugins {
         id("com.google.gms.google-services")
     }
     ```

---

## ✅ Étape 7 : Vérifier la Configuration

1. **Vérifiez que tout est en place** :
   - ✅ Google Sign-In API activée
   - ✅ OAuth 2.0 Client ID créé pour Android
   - ✅ SHA-1 ajouté dans les identifiants
   - ✅ Package name correct (`com.example.damandroid`)

2. **Testez l'application** :
   - Compilez et lancez l'application
   - Cliquez sur le bouton "Continue with Google"
   - Vous devriez voir le sélecteur de compte Google s'afficher

---

## 🐛 Résolution des Problèmes

### Problème : "DEVELOPER_ERROR" ou "10:"
- **Solution** : Vérifiez que le SHA-1 est correctement ajouté dans Google Cloud Console
- Vérifiez que le package name correspond exactement

### Problème : "12501:" (Sign-in cancelled)
- **Solution** : C'est normal, l'utilisateur a annulé la connexion

### Problème : "7:" (Network error)
- **Solution** : Vérifiez votre connexion internet
- Vérifiez que l'API Google Sign-In est activée

### Le SHA-1 ne fonctionne pas :
1. Assurez-vous d'avoir copié seulement la partie après "SHA1: "
2. Vérifiez qu'il n'y a pas d'espaces avant/après
3. Attendez 5-10 minutes après avoir ajouté le SHA-1 (Google met du temps à propager les changements)
4. Vérifiez que vous utilisez le bon SHA-1 (debug vs release)

---

## 📚 Ressources Utiles

- **Documentation officielle Google Sign-In** : https://developers.google.com/identity/sign-in/android/start-integrating
- **Google Cloud Console** : https://console.cloud.google.com/
- **Firebase Console** : https://console.firebase.google.com/

---

## 🎯 Résumé des Étapes

1. ✅ Créer un projet dans Google Cloud Console
2. ✅ Activer Google Sign-In API
3. ✅ Créer OAuth 2.0 Client ID pour Android
4. ✅ Obtenir le SHA-1 (debug et/ou release)
5. ✅ Ajouter le SHA-1 dans Google Cloud Console
6. ✅ Tester l'application

---

## 💡 Notes Importantes

- **Le SHA-1 debug est différent du SHA-1 release** : Vous devrez ajouter les deux si vous voulez tester en debug ET publier en release
- **Le package name doit correspondre exactement** : Vérifiez qu'il correspond à celui dans `build.gradle.kts` (`applicationId`)
- **Les changements peuvent prendre 5-10 minutes** : Si ça ne marche pas immédiatement, attendez un peu
- **Pour la production** : N'oubliez pas de créer et d'ajouter le SHA-1 de votre keystore de release

---

Si vous avez des questions ou des problèmes, consultez la section "Résolution des Problèmes" ci-dessus.

