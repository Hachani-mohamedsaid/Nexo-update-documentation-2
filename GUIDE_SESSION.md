# Guide : Gestion des Sessions Utilisateur

## Vue d'ensemble

La gestion des sessions dans DamAndroid utilise un système simple et efficace basé sur un **singleton en mémoire** (`UserSession`) qui stocke le token JWT et les informations de l'utilisateur connecté.

---

## Architecture de la Session

### 1. UserSession (Singleton)

**Fichier :** `app/src/main/java/com/example/damandroid/auth/UserSession.kt`

```kotlin
object UserSession {
    @Volatile
    var token: String? = null
        private set

    @Volatile
    var user: UserDto? = null
        private set

    fun update(token: String?, user: UserDto?) {
        this.token = token
        this.user = user
    }

    fun clear() {
        token = null
        user = null
    }
}
```

**Caractéristiques :**
- ✅ **Singleton** : Accessible partout dans l'app
- ✅ **Thread-safe** : Utilise `@Volatile` pour la sécurité
- ✅ **Simple** : Stockage en mémoire uniquement
- ⚠️ **Non persistant** : Perdu au redémarrage de l'app

---

## Flux de connexion (Login)

### Étape 1 : L'utilisateur se connecte

```kotlin
// Dans LoginScreen ou AuthRepository
val authRepository = AuthRepository(context)
val result = authRepository.login(email, password)
```

### Étape 2 : Le backend retourne le token

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "_id": "6913492bd65af9844d243495",
    "email": "user@example.com",
    "name": "Mohamed",
    "profileImageUrl": "https://..."
  }
}
```

### Étape 3 : Stockage dans UserSession

```kotlin
// Dans AuthRepository.login()
private fun storeSession(token: String?, user: UserDto?) {
    UserSession.update(token, user)  // ← Stockage ici
}
```

### Étape 4 : Utilisation dans l'app

```kotlin
// Accéder au token
val token = UserSession.token

// Accéder aux infos utilisateur
val userId = UserSession.user?.id
val userName = UserSession.user?.name
```

---

## Utilisation dans l'application

### 1. Authentification automatique (AuthInterceptor)

**Fichier :** `app/src/main/java/com/example/damandroid/api/AuthInterceptor.kt`

```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Récupérer le token depuis UserSession
        val token = UserSession.token
        
        if (token != null) {
            // Ajouter le token dans le header Authorization
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            return chain.proceed(newRequest)
        }
        
        return chain.proceed(originalRequest)
    }
}
```

**Résultat :** Toutes les requêtes HTTP incluent automatiquement le token JWT.

### 2. Accès aux informations utilisateur

**Exemple 1 : Obtenir l'ID de l'utilisateur connecté**

```kotlin
// Dans n'importe quel ViewModel ou Composable
val currentUserId = UserSession.user?.id

if (currentUserId != null) {
    // Utiliser l'ID pour filtrer, comparer, etc.
    val isMyMessage = message.senderId == currentUserId
}
```

**Exemple 2 : Afficher le nom de l'utilisateur**

```kotlin
// Dans un Composable
val userName = UserSession.user?.name ?: "Utilisateur"
Text(text = "Bonjour, $userName!")
```

**Exemple 3 : Vérifier si l'utilisateur est connecté**

```kotlin
val isLoggedIn = UserSession.token != null && UserSession.user != null
```

### 3. Déconnexion (Logout)

**Fichier :** `app/src/main/java/com/example/damandroid/MainActivity.kt`

```kotlin
fun onLogout() {
    UserSession.clear()  // ← Efface token et user
    // Rediriger vers l'écran de login
}
```

---

## Méthodes disponibles

### `UserSession.update(token, user)`

**Utilisation :** Stocker ou mettre à jour la session

```kotlin
// Après un login réussi
UserSession.update(
    token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    user = UserDto(
        id = "6913492bd65af9844d243495",
        name = "Mohamed",
        email = "user@example.com",
        // ...
    )
)
```

**Quand l'utiliser :**
- ✅ Après login
- ✅ Après register
- ✅ Après refresh du profil utilisateur
- ✅ Après mise à jour du token

### `UserSession.clear()`

**Utilisation :** Effacer la session (logout)

```kotlin
// Déconnexion
UserSession.clear()
// Maintenant : token = null, user = null
```

**Quand l'utiliser :**
- ✅ Logout utilisateur
- ✅ Token expiré
- ✅ Erreur d'authentification

### `UserSession.token`

**Utilisation :** Accéder au token JWT

```kotlin
val token = UserSession.token
// Retourne : String? (peut être null si non connecté)
```

**Exemples d'utilisation :**
- Envoyer dans les headers HTTP (via AuthInterceptor)
- Vérifier si l'utilisateur est connecté
- Stocker ailleurs si nécessaire

### `UserSession.user`

**Utilisation :** Accéder aux informations utilisateur

```kotlin
val user = UserSession.user
// Retourne : UserDto? (peut être null si non connecté)

// Propriétés disponibles :
user?.id           // ID de l'utilisateur
user?.name         // Nom
user?.email        // Email
user?.location     // Localisation
user?.profileImageUrl  // Photo de profil
// ... etc
```

---

## Exemples pratiques

### Exemple 1 : Filtrer les messages (mes messages vs autres)

```kotlin
// Dans ChatMapper.kt
fun ApiChatMessage.toDomain(): ChatMessage {
    val currentUserId = UserSession.user?.id
    
    return ChatMessage(
        id = this.id,
        text = this.text,
        isFromMe = this.sender == currentUserId,  // ← Comparaison
        senderName = this.extractSenderName(),
        avatar = this.extractAvatar(),
        // ...
    )
}
```

### Exemple 2 : Vérifier si je suis le créateur d'une activité

```kotlin
// Dans HomeFeedScreen.kt
val currentUserId = UserSession.user?.id
val isCreator = activity.creatorId == currentUserId

if (isCreator) {
    // Afficher options spéciales pour le créateur
}
```

### Exemple 3 : Rejoindre une activité (vérifier si déjà participant)

```kotlin
// Dans HomeFeedScreen.kt
val currentUserId = UserSession.user?.id
val isAlreadyParticipant = activity.participantIds.contains(currentUserId)

if (!isAlreadyParticipant) {
    // Afficher bouton "Join"
} else {
    // Afficher "Already joined"
}
```

### Exemple 4 : WebSocket avec authentification

```kotlin
// Dans ActivityRoomWebSocketService.kt
fun connect(activityId: String) {
    val token = UserSession.token  // ← Récupérer le token
    
    if (token.isNullOrEmpty()) {
        Log.e("WebSocket", "No token available")
        return
    }
    
    val options = IO.Options().apply {
        auth = mapOf("token" to token)  // ← Envoyer au serveur
        // ...
    }
    
    socket = IO.socket(wsUrl, options)
    socket?.connect()
}
```

---

## Gestion des erreurs

### Token expiré

**Problème :** Le token JWT peut expirer après un certain temps.

**Solution actuelle :**
- L'utilisateur doit se reconnecter
- `UserSession.clear()` est appelé
- Redirection vers l'écran de login

**Amélioration future :**
```kotlin
// TODO: Implémenter refresh token
if (response.code() == 401) {
    val newToken = refreshToken()
    if (newToken != null) {
        UserSession.update(newToken, UserSession.user)
        // Réessayer la requête
    } else {
        UserSession.clear()
        // Rediriger vers login
    }
}
```

### Session perdue au redémarrage

**Problème :** `UserSession` est en mémoire, donc perdu si l'app est fermée.

**Solution actuelle :**
- L'utilisateur doit se reconnecter à chaque démarrage

**Amélioration future :**
```kotlin
// TODO: Persister dans SharedPreferences
object UserSession {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    
    var token: String?
        get() = prefs.getString("token", null)
        set(value) = prefs.edit().putString("token", value).apply()
    
    // ...
}
```

---

## Bonnes pratiques

### ✅ À FAIRE

1. **Vérifier null avant utilisation**
```kotlin
val userId = UserSession.user?.id
if (userId != null) {
    // Utiliser userId
}
```

2. **Utiliser l'opérateur safe call (?.?)**
```kotlin
val userName = UserSession.user?.name ?: "Utilisateur"
```

3. **Clear la session en cas d'erreur 401**
```kotlin
if (response.code() == 401) {
    UserSession.clear()
    // Rediriger vers login
}
```

### ❌ À ÉVITER

1. **Ne pas stocker de données sensibles**
```kotlin
// ❌ MAUVAIS : Ne pas stocker le mot de passe
UserSession.password = password

// ✅ BON : Seulement token et infos publiques
UserSession.token = token
UserSession.user = user
```

2. **Ne pas modifier directement**
```kotlin
// ❌ MAUVAIS : Propriétés privées
UserSession.token = "new_token"  // Erreur de compilation

// ✅ BON : Utiliser update()
UserSession.update("new_token", user)
```

3. **Ne pas oublier de clear au logout**
```kotlin
// ❌ MAUVAIS : Oublier de clear
fun logout() {
    // Rediriger vers login
    // Mais token toujours en mémoire !
}

// ✅ BON : Toujours clear
fun logout() {
    UserSession.clear()
    // Rediriger vers login
}
```

---

## Structure UserDto

**Fichier :** `app/src/main/java/com/example/damandroid/api/UserDto.kt`

```kotlin
data class UserDto(
    val _id: String,
    val id: String? = null,
    val email: String,
    val name: String,
    val location: String?,
    val profileImageUrl: String?,
    val dateOfBirth: String?,
    val phone: String?,
    val about: String?,
    val sportsInterests: List<String>?,
    val isEmailVerified: Boolean = false,
    // ...
) {
    fun getUserId(): String = id ?: _id
}
```

**Propriétés principales :**
- `id` ou `_id` : Identifiant unique
- `name` : Nom complet
- `email` : Adresse email
- `profileImageUrl` : URL de la photo de profil
- `location` : Localisation
- `sportsInterests` : Liste des sports d'intérêt

---

## Flux complet : Login → Utilisation → Logout

```
1. UTILISATEUR SE CONNECTE
   ↓
   LoginScreen → AuthRepository.login()
   ↓
   Backend retourne { token, user }
   ↓
   AuthRepository.storeSession(token, user)
   ↓
   UserSession.update(token, user)  ← STOCKAGE
   ↓

2. UTILISATION DANS L'APP
   ↓
   AuthInterceptor ajoute token aux requêtes HTTP
   ↓
   ViewModels accèdent à UserSession.user?.id
   ↓
   UI affiche UserSession.user?.name
   ↓

3. UTILISATEUR SE DÉCONNECTE
   ↓
   MainActivity.onLogout()
   ↓
   UserSession.clear()  ← EFFACEMENT
   ↓
   Redirection vers LoginScreen
```

---

## Résumé

### Points clés

1. **UserSession est un singleton** : Accessible partout
2. **Stockage en mémoire** : Simple mais non persistant
3. **Thread-safe** : Utilise `@Volatile`
4. **Automatique** : AuthInterceptor ajoute le token automatiquement

### Méthodes essentielles

- `UserSession.update(token, user)` → Stocker session
- `UserSession.clear()` → Effacer session
- `UserSession.token` → Accéder au token
- `UserSession.user` → Accéder aux infos utilisateur

### Utilisation typique

```kotlin
// Vérifier si connecté
if (UserSession.token != null) {
    val userId = UserSession.user?.id
    // Utiliser userId...
}

// Déconnexion
UserSession.clear()
```

---

**Dernière mise à jour :** Novembre 2025




