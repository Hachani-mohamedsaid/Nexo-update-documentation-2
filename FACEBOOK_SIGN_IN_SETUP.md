# Configuration Facebook Sign-In

Ce guide vous explique comment configurer Facebook Sign-In pour votre application Android.

## 📋 Prérequis

1. Un compte Facebook Developer
2. Une application Facebook créée dans le [Facebook Developers Console](https://developers.facebook.com/)

## 🔧 Étapes de Configuration

### 1. Créer une Application Facebook

1. Allez sur [Facebook Developers Console](https://developers.facebook.com/)
2. Cliquez sur "My Apps" → "Create App"
3. Sélectionnez "Consumer" comme type d'application
4. Remplissez les informations de base (nom de l'app, email de contact)
5. Cliquez sur "Create App"

### 2. Ajouter la Plateforme Android

1. Dans votre application Facebook, allez dans "Settings" → "Basic"
2. Notez votre **App ID** et **App Secret**
3. Cliquez sur "+ Add Platform" → "Android"
4. Remplissez les informations :
   - **Package Name** : `com.example.damandroid` (ou votre package name)
   - **Class Name** : `com.example.damandroid.MainActivity`
   - **Key Hashes** : Vous devez ajouter le SHA-1 de votre keystore

### 3. Obtenir le SHA-1

Pour obtenir le SHA-1 de votre keystore de debug :

**Windows:**
```bash
cd %USERPROFILE%\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Linux/Mac:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Copiez le **SHA-1** (par exemple: `AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD`)

### 4. Configurer dans AndroidManifest.xml

Les métadonnées Facebook sont déjà ajoutées dans `AndroidManifest.xml`. Vous devez simplement remplacer les valeurs dans `strings.xml` :

### 5. Configurer dans strings.xml

Ouvrez `app/src/main/res/values/strings.xml` et remplacez :

```xml
<string name="facebook_app_id">YOUR_FACEBOOK_APP_ID</string>
<string name="facebook_client_token">YOUR_FACEBOOK_CLIENT_TOKEN</string>
```

Par vos valeurs réelles :
- **facebook_app_id** : Votre App ID Facebook
- **facebook_client_token** : Votre Client Token (trouvable dans "Settings" → "Basic" de votre app Facebook)

### 6. Ajouter les Permissions (Optionnel)

Les permissions nécessaires sont déjà ajoutées dans `AndroidManifest.xml` :
- `INTERNET` : Déjà présent

### 7. Tester

1. Compilez et lancez l'application
2. Cliquez sur le bouton "Continue with Facebook" dans la page de connexion ou d'inscription
3. Connectez-vous avec votre compte Facebook
4. L'application devrait créer un compte ou se connecter automatiquement

## 🔍 Résolution de Problèmes

### Erreur : "Invalid key hash"

**Solution :** Assurez-vous que le SHA-1 que vous avez ajouté dans Facebook Developers Console correspond au SHA-1 de votre keystore actuel.

### Erreur : "App ID not found"

**Solution :** Vérifiez que vous avez bien remplié `facebook_app_id` dans `strings.xml`.

### L'application ne se connecte pas au backend

**Solution :** 
1. Vérifiez que votre backend NestJS a l'endpoint `/auth/facebook` ou utilisez la méthode de fallback automatique
2. Vérifiez les logs dans Logcat pour voir les erreurs détaillées

## 📝 Notes Importantes

1. **Keystore de Production** : Pour la production, vous devrez ajouter le SHA-1 de votre keystore de production dans Facebook Developers Console
2. **App Review** : Pour utiliser Facebook Login en production, vous devrez soumettre votre application à l'App Review de Facebook
3. **Backend Integration** : L'application essaie d'abord d'utiliser l'endpoint `/auth/facebook`. Si cet endpoint n'existe pas (404), elle utilise automatiquement une méthode de fallback qui crée l'utilisateur via `/auth/register` puis le connecte via `/auth/login`.

## 🎯 Endpoint Backend Recommandé

Pour une meilleure intégration, créez l'endpoint suivant dans votre backend NestJS :

```typescript
@Post('auth/facebook')
async loginWithFacebook(@Body() facebookLoginDto: FacebookLoginDto) {
  // Vérifier/créer l'utilisateur avec les informations Facebook
  // Retourner un token JWT
  return {
    access_token: 'jwt_token_here',
    user: userObject
  };
}
```

Si cet endpoint n'existe pas, l'application utilisera automatiquement la méthode de fallback.

## ✅ Vérification

Pour vérifier que tout fonctionne :

1. ✅ App ID et Client Token configurés dans `strings.xml`
2. ✅ SHA-1 ajouté dans Facebook Developers Console
3. ✅ Package name correspond dans Facebook Developers Console
4. ✅ L'application compile sans erreur
5. ✅ Le bouton Facebook fonctionne dans l'application

