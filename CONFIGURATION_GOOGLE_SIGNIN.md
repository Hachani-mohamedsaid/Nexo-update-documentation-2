# 📱 Guide Simple : Configuration Google Sign-In

## 🎯 Ce que vous devez faire en 5 étapes simples

### ✅ Étape 1 : Obtenir votre SHA-1 (5 minutes)

**C'est votre "empreinte digitale" d'application Android**

#### Option A : Méthode la plus simple (via Android Studio)

1. **Ouvrez Android Studio**
2. **Ouvrez le terminal** en bas (onglet "Terminal")
3. **Tapez cette commande** :

   **Sur Windows :**
   ```powershell
   .\gradlew signingReport
   ```

   **Sur Mac/Linux :**
   ```bash
   ./gradlew signingReport
   ```

4. **Cherchez dans le résultat** quelque chose qui ressemble à :
   ```
   SHA1: A1:B2:C3:D4:E5:F6:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE
   ```

5. **Copiez tout ce qui est après "SHA1:"** (les lettres et chiffres séparés par des deux-points)
   - Exemple : `A1:B2:C3:D4:E5:F6:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE`

---

#### Option B : Via ligne de commande (si Option A ne marche pas)

**Sur Windows (PowerShell) :**
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```

**Sur Mac/Linux :**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Cherchez** la ligne qui dit `SHA1:` et copiez la valeur.

---

### ✅ Étape 2 : Aller sur Google Cloud Console (2 minutes)

1. **Ouvrez votre navigateur**
2. **Allez sur** : https://console.cloud.google.com/
3. **Connectez-vous** avec votre compte Google

---

### ✅ Étape 3 : Créer un Projet (3 minutes)

1. **En haut à gauche**, cliquez sur le nom du projet actuel
2. **Cliquez sur "Nouveau projet"**
3. **Nommez votre projet** (ex: "DamAndroid App")
4. **Cliquez sur "Créer"**
5. **Attendez quelques secondes** que le projet soit créé

---

### ✅ Étape 4 : Activer Google Sign-In (2 minutes)

1. **Dans le menu de gauche**, cliquez sur **"APIs et services"** (ou "APIs & Services")
2. **Cliquez sur "Bibliothèque"** (ou "Library")
3. **Dans la barre de recherche**, tapez : `Google Sign-In API`
4. **Cliquez sur "Google Sign-In API"**
5. **Cliquez sur le bouton bleu "ACTIVER"** (ou "ENABLE")

---

### ✅ Étape 5 : Créer les Identifiants OAuth (5 minutes)

#### 5.1. Configurer l'écran de consentement (première fois uniquement)

1. **Dans le menu de gauche**, allez dans **"APIs et services"** > **"Écran de consentement OAuth"** (ou "OAuth consent screen")
2. **Choisissez "Externe"** (ou "External")
3. **Cliquez sur "Créer"**
4. **Remplissez les informations** :
   - **Nom de l'application** : `DamAndroid` (ou ce que vous voulez)
   - **Email de support utilisateur** : Votre email
   - **Email de contact du développeur** : Votre email
5. **Cliquez sur "Enregistrer et continuer"** pour chaque étape
6. **À la fin**, cliquez sur "Retour au tableau de bord"

#### 5.2. Créer l'OAuth Client ID

1. **Dans le menu de gauche**, allez dans **"APIs et services"** > **"Identifiants"** (ou "Credentials")
2. **En haut**, cliquez sur **"+ CRÉER DES IDENTIFIANTS"** (ou "+ CREATE CREDENTIALS")
3. **Sélectionnez "ID client OAuth"** (ou "OAuth client ID")
4. **Sélectionnez "Application Android"**
5. **Remplissez** :
   - **Nom** : `DamAndroid Android Client` (ou ce que vous voulez)
   - **Nom du package** : `com.example.damandroid`
   - **Empreinte du certificat SHA-1** : Collez le SHA-1 que vous avez copié à l'Étape 1
6. **Cliquez sur "Créer"** (ou "CREATE")

---

## 🎉 C'est Terminé !

### Maintenant, testez votre application :

1. **Recompilez votre application** dans Android Studio
2. **Lancez l'application** sur un appareil ou un émulateur
3. **Cliquez sur le bouton "Continue with Google"**
4. **Vous devriez voir** le sélecteur de compte Google s'afficher !

---

## ⚠️ Important à savoir

1. **Attendez 5-10 minutes** après avoir ajouté le SHA-1. Google a besoin de temps pour activer les changements.

2. **Vérifiez que le package name correspond** :
   - Dans Google Cloud Console : `com.example.damandroid`
   - Dans votre `build.gradle.kts` : `applicationId = "com.example.damandroid"`
   - Ils doivent être **exactement identiques**

3. **Si ça ne marche pas** :
   - Vérifiez que vous avez bien copié le SHA-1 (sans "SHA1:" au début)
   - Vérifiez qu'il n'y a pas d'espaces avant ou après
   - Attendez encore 5-10 minutes
   - Vérifiez que "Google Sign-In API" est bien activée

---

## 🆘 Besoin d'aide ?

Si vous avez des problèmes, consultez le fichier `GOOGLE_SIGN_IN_SETUP.md` pour un guide plus détaillé avec des captures d'écran et la résolution des problèmes.

---

## 📋 Checklist de Vérification

Avant de tester, vérifiez que vous avez :

- [ ] ✅ Obtenu votre SHA-1 (Étape 1)
- [ ] ✅ Créé un projet dans Google Cloud Console (Étape 3)
- [ ] ✅ Activé Google Sign-In API (Étape 4)
- [ ] ✅ Configuré l'écran de consentement OAuth (Étape 5.1)
- [ ] ✅ Créé l'OAuth Client ID avec le SHA-1 (Étape 5.2)
- [ ] ✅ Vérifié que le package name correspond
- [ ] ✅ Attendu 5-10 minutes après avoir ajouté le SHA-1

---

**Bon courage ! 🚀**

