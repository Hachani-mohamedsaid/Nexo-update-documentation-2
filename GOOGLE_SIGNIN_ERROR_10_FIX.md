# Résolution de l'erreur Google Sign-In : DEVELOPER_ERROR (10)

## 🔴 Problème

L'erreur `ApiException: 10` (DEVELOPER_ERROR) se produit lors de la tentative de connexion avec Google Sign-In. Cette erreur indique un problème de configuration dans Google Cloud Console.

## 🔍 Cause

L'erreur `10` (DEVELOPER_ERROR) signifie généralement que :
1. **Le SHA-1 fingerprint n'est pas configuré** dans Google Cloud Console
2. **Le package name ne correspond pas** exactement
3. **Les changements n'ont pas encore été propagés** (attendre 5-10 minutes)

## ✅ Solution

### Étape 1 : Obtenir votre SHA-1 Fingerprint

#### Option A : Via Terminal (macOS/Linux)
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### Option B : Via Gradle (Recommandé)
```bash
cd /Users/mohamedsaidhachani/Downloads/Nexo-update-documentation-2
./gradlew signingReport
```

Cherchez la ligne qui commence par `SHA1:` et copiez la valeur (sans le préfixe "SHA1:").

Exemple :
```
SHA1: A1:B2:C3:D4:E5:F6:...
```
Copiez seulement : `A1:B2:C3:D4:E5:F6:...`

### Étape 2 : Configurer dans Google Cloud Console

1. **Allez sur Google Cloud Console** : https://console.cloud.google.com/
2. **Sélectionnez votre projet**
3. **Allez dans "APIs & Services" > "Credentials"**
4. **Trouvez votre OAuth 2.0 Client ID pour Android** (ou créez-en un si nécessaire)
5. **Cliquez sur l'icône de crayon (éditer)**
6. **Vérifiez/Configurez** :
   - **Package name** : `com.example.damandroid` (doit correspondre exactement)
   - **SHA-1 certificate fingerprint** : Collez votre SHA-1 (sans "SHA1:")
7. **Cliquez sur "SAVE"**

### Étape 3 : Attendre la propagation

⚠️ **Important** : Après avoir ajouté le SHA-1, attendez **5-10 minutes** pour que Google propage les changements.

### Étape 4 : Vérifier la configuration

1. **Vérifiez que Google Sign-In API est activée** :
   - "APIs & Services" > "Library"
   - Recherchez "Google Sign-In API"
   - Assurez-vous qu'elle est activée

2. **Vérifiez le package name** :
   - Dans `app/build.gradle.kts`, vérifiez que `applicationId = "com.example.damandroid"`
   - Il doit correspondre exactement à celui dans Google Cloud Console

## 📝 Améliorations apportées au code

J'ai amélioré la gestion d'erreur pour afficher des messages plus clairs :

1. **MainActivity.kt** : Messages d'erreur détaillés selon le code d'erreur
2. **GoogleSignInHelper.kt** : Logs plus informatifs
3. **LoginScreen.kt** : Affichage d'un message d'erreur à l'utilisateur

## 🧪 Tester après configuration

1. **Attendez 5-10 minutes** après avoir ajouté le SHA-1
2. **Redémarrez l'application** complètement (fermez-la et relancez-la)
3. **Essayez de vous connecter avec Google**
4. **Vérifiez les logs** pour voir si l'erreur persiste

## 🔧 Commandes utiles

### Obtenir le SHA-1 (Debug)
```bash
./gradlew signingReport
```

### Obtenir le SHA-1 (Release) - Si vous avez une clé de release
```bash
keytool -list -v -keystore /path/to/your/release.keystore -alias your-key-alias
```

## 📚 Documentation

Pour plus de détails, consultez :
- `GOOGLE_SIGN_IN_SETUP.md` : Guide complet de configuration
- Documentation Google : https://developers.google.com/identity/sign-in/android/start-integrating

## ⚠️ Notes importantes

- **Le SHA-1 debug est différent du SHA-1 release** : Si vous publiez l'app, vous devrez ajouter les deux
- **Le package name doit correspondre exactement** : Vérifiez qu'il n'y a pas d'espaces ou de différences
- **Les changements peuvent prendre du temps** : Attendez 5-10 minutes après avoir ajouté le SHA-1
- **Pour la production** : N'oubliez pas d'ajouter le SHA-1 de votre keystore de release

## 🐛 Si le problème persiste

1. **Vérifiez les logs** pour voir le message d'erreur exact
2. **Vérifiez que le SHA-1 est correct** : Comparez avec celui obtenu via `./gradlew signingReport`
3. **Vérifiez le package name** : Il doit être exactement `com.example.damandroid`
4. **Vérifiez que Google Sign-In API est activée**
5. **Attendez encore 5-10 minutes** et réessayez
6. **Vérifiez que vous utilisez le bon compte Google** dans Google Cloud Console

