# 🔐 Intégration Google Sign-In avec Backend NestJS

## 📋 Informations de Configuration

**Client ID (Android)** : `316994227231-7c392ucjvvovmi64dqglk7b4rnbcluht.apps.googleusercontent.com`  
**Project ID** : `damandroid-app`

**Fichier JSON** (pour référence) :
```json
{
  "installed": {
    "client_id": "316994227231-7c392ucjvvovmi64dqglk7b4rnbcluht.apps.googleusercontent.com",
    "project_id": "damandroid-app",
    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
    "token_uri": "https://oauth2.googleapis.com/token",
    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs"
  }
}
```

---

## 🎯 Deux Options d'Utilisation

### Option 1 : Connexion Locale (Simple) ✅ Actuel

**Ce que vous avez maintenant :**
- Connexion Google fonctionne localement sur l'app
- L'utilisateur peut se connecter avec son compte Google
- Pas d'intégration avec le backend

**Utilisation :** 
- Connexion rapide pour l'utilisateur
- Données stockées localement
- Pas besoin du fichier JSON

---

### Option 2 : Intégration Backend (Recommandé pour production) 🔄

**Ce qu'il faut ajouter :**
1. Obtenir un **ID Token** Google (pas juste l'email)
2. Envoyer ce token à votre backend NestJS
3. Backend vérifie le token avec Google
4. Backend crée/connecte l'utilisateur dans votre base de données

**Avantages :**
- Authentification sécurisée côté serveur
- Synchronisation entre appareils
- Vérification du token par le backend
- Stockage des données utilisateur dans votre DB

---

## 🔧 Configuration pour Option 2 (Backend)

### Étape 1 : Créer un OAuth Client ID Web (pour le backend)

1. **Allez dans Google Cloud Console** : https://console.cloud.google.com/
2. **APIs et services** > **Identifiants**
3. **+ CRÉER DES IDENTIFIANTS** > **ID client OAuth**
4. **Choisissez "Application Web"** (pas Android cette fois)
5. **Nom** : `DamAndroid Web Client`
6. **URIs de redirection autorisés** : 
   - `http://localhost:3000/auth/google/callback` (pour dev)
   - `https://apinest-production.up.railway.app/auth/google/callback` (pour production)
7. **Créer**

**Note :** Vous obtiendrez un **nouveau Client ID Web** (différent du Client ID Android).

---

### Étape 2 : Modifier le Code Android pour obtenir l'ID Token

**Modifier `GoogleSignInHelper.kt` :**

```kotlin
// Ajoutez le Client ID Web pour obtenir l'ID Token
private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestEmail()
    .requestProfile()
    .requestIdToken("VOTRE_CLIENT_ID_WEB_ICI.apps.googleusercontent.com") // Client ID Web (pas Android)
    .build()
```

**Important :** 
- Utilisez le **Client ID Web** (pas Android)
- Ce Client ID permet d'obtenir un ID Token que le backend peut vérifier

---

### Étape 3 : Envoyer l'ID Token au Backend

**Modifier `LoginScreen.kt` et `SignUpPage.kt` :**

```kotlin
onGoogleSignInRequest { account ->
    if (account != null) {
        coroutineScope.launch {
            val idToken = account.idToken // Obtenir l'ID Token
            
            if (idToken != null) {
                // Envoyer le token au backend
                val result = authRepository.loginWithGoogle(idToken)
                
                when (result) {
                    is AuthRepository.AuthResult.Success -> {
                        // Sauvegarder le token du backend
                        onLogin()
                    }
                    is AuthRepository.AuthResult.Error -> {
                        errorMessage = result.message
                    }
                }
            }
        }
    }
}
```

---

### Étape 4 : Créer l'Endpoint Backend NestJS

**Dans votre backend NestJS, créez :**

```typescript
// auth.controller.ts
@Post('auth/google')
async googleLogin(@Body() body: { idToken: string }) {
  // Vérifier le token avec Google
  const ticket = await client.verifyIdToken({
    idToken: body.idToken,
    audience: 'VOTRE_CLIENT_ID_WEB.apps.googleusercontent.com'
  });
  
  const payload = ticket.getPayload();
  const email = payload.email;
  const name = payload.name;
  
  // Trouver ou créer l'utilisateur
  let user = await this.usersService.findByEmail(email);
  
  if (!user) {
    // Créer nouvel utilisateur
    user = await this.usersService.create({
      email: email,
      name: name,
      // ...
    });
  }
  
  // Générer votre propre token JWT
  const token = this.jwtService.sign({ email: user.email, sub: user._id });
  
  return {
    access_token: token,
    user: user
  };
}
```

---

## 📝 Résumé

### Pour l'Instant (Option 1 - Simple) :
✅ Votre code fonctionne  
✅ Connexion Google locale  
✅ Pas besoin du fichier JSON dans l'app  
✅ Vous pouvez tester maintenant

### Pour Plus Tard (Option 2 - Backend) :
1. Créer un Client ID Web dans Google Cloud Console
2. Modifier `GoogleSignInHelper.kt` pour utiliser `requestIdToken()` avec le Client ID Web
3. Créer l'endpoint `/auth/google` dans NestJS
4. Envoyer l'ID Token au backend
5. Backend vérifie le token et crée/connecte l'utilisateur

---

## 🚀 Action Immédiate

**Vous pouvez tester l'application maintenant !**

1. **Attendez 5-10 minutes** (si vous venez de créer les identifiants)
2. **Recompilez l'application**
3. **Lancez l'application**
4. **Cliquez sur "Continue with Google"**
5. **Ça devrait fonctionner !** 🎉

L'intégration backend peut être ajoutée plus tard si nécessaire.

