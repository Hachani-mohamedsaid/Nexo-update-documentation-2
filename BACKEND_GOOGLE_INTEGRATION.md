# 🔐 Intégration Google Sign-In avec Backend

## ✅ Modification Effectuée

Le code Android envoie maintenant les informations Google au backend NestJS !

### Ce qui a été ajouté :

1. **Nouvel endpoint API** : `POST /auth/google`
2. **Fonction dans AuthRepository** : `loginWithGoogle()`
3. **Intégration dans LoginScreen et SignUpPage** : Envoi des données au backend

---

## 📋 Données Envoyées au Backend

Quand l'utilisateur se connecte avec Google, l'app envoie :
```json
{
  "email": "utilisateur@gmail.com",
  "name": "Nom de l'utilisateur",
  "idToken": null,  // Sera disponible si vous configurez un Client ID Web
  "photoUrl": "https://..."  // URL de la photo de profil
}
```

---

## ⚠️ Important : Créer l'Endpoint Backend

**Vous devez créer l'endpoint `/auth/google` dans votre backend NestJS !**

### Exemple d'implémentation Backend (NestJS) :

```typescript
// auth.controller.ts
@Post('auth/google')
async googleLogin(@Body() body: GoogleLoginDto) {
  const { email, name, photoUrl } = body;
  
  // Vérifier si l'utilisateur existe
  let user = await this.usersService.findByEmail(email);
  
  if (!user) {
    // Créer nouvel utilisateur
    user = await this.usersService.create({
      email: email,
      name: name,
      location: '', // Vous pouvez demander la location plus tard ou utiliser une valeur par défaut
      password: '', // Pas de mot de passe pour les comptes Google
      photoUrl: photoUrl,
      authProvider: 'google' // Pour distinguer les comptes Google
    });
  }
  
  // Générer un token JWT
  const token = this.jwtService.sign({ 
    email: user.email, 
    sub: user._id 
  });
  
  return {
    access_token: token,
    user: {
      id: user._id,
      email: user.email,
      name: user.name,
      location: user.location
    }
  };
}
```

```typescript
// auth.dto.ts
export class GoogleLoginDto {
  @IsEmail()
  email: string;
  
  @IsString()
  name: string;
  
  @IsOptional()
  @IsString()
  idToken?: string;
  
  @IsOptional()
  @IsString()
  photoUrl?: string;
}
```

---

## 🔧 Option Avancée : Utiliser l'ID Token

Pour une sécurité maximale, vous pouvez utiliser l'ID Token de Google :

### 1. Créer un Client ID Web dans Google Cloud Console

1. Allez dans Google Cloud Console
2. **APIs et services** > **Identifiants**
3. **+ CRÉER DES IDENTIFIANTS** > **ID client OAuth**
4. Choisissez **"Application Web"**
5. Notez le **Client ID Web** (différent du Client ID Android)

### 2. Modifier GoogleSignInHelper.kt

```kotlin
private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestEmail()
    .requestProfile()
    .requestIdToken("VOTRE_CLIENT_ID_WEB_ICI.apps.googleusercontent.com") // Client ID Web
    .build()
```

### 3. Vérifier l'ID Token dans le Backend

```typescript
import { OAuth2Client } from 'google-auth-library';

const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID_WEB);

@Post('auth/google')
async googleLogin(@Body() body: GoogleLoginDto) {
  const { idToken, email, name } = body;
  
  // Vérifier le token avec Google
  if (idToken) {
    const ticket = await client.verifyIdToken({
      idToken: idToken,
      audience: process.env.GOOGLE_CLIENT_ID_WEB
    });
    
    const payload = ticket.getPayload();
    // Utiliser les données vérifiées de Google
    const verifiedEmail = payload.email;
    const verifiedName = payload.name;
    // ...
  }
  
  // Reste du code...
}
```

---

## ✅ Test

1. **Créez l'endpoint `/auth/google` dans votre backend NestJS**
2. **Testez la connexion Google depuis l'app**
3. **Vérifiez que l'utilisateur est créé dans la base de données**

---

## 📝 Résumé

- ✅ **Code Android prêt** - Envoie les données au backend
- ⚠️ **Backend à créer** - Créez l'endpoint `/auth/google`
- 🔒 **Option ID Token** - Pour plus de sécurité (optionnel)

**L'application envoie maintenant les données au backend ! Il faut juste créer l'endpoint backend.** 🚀

