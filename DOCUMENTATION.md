# Documentation Complète - DamAndroid

## Table des matières

1. [Architecture générale](#architecture-générale)
2. [Structure du projet](#structure-du-projet)
3. [Fonctionnalités principales](#fonctionnalités-principales)
4. [Architecture MVVM](#architecture-mvvm)
5. [API et Backend](#api-et-backend)
6. [Gestion des chats](#gestion-des-chats)
7. [Découverte d'activités](#découverte-dactivités)
8. [AI Matchmaker](#ai-matchmaker)
9. [WebSocket vs Polling](#websocket-vs-polling)
10. [Dépendances principales](#dépendances-principales)

---

## Architecture générale

### Pattern architectural : MVVM (Model-View-ViewModel)

```
┌─────────────┐
│   View      │ (Compose UI)
│  (Screen)   │
└──────┬──────┘
       │ observe
       ▼
┌─────────────┐
│  ViewModel  │ (StateFlow)
│  (State)    │
└──────┬──────┘
       │ calls
       ▼
┌─────────────┐
│ Repository  │
│  (Domain)   │
└──────┬──────┘
       │ uses
       ▼
┌─────────────┐
│ DataSource  │
│  (API)      │
└─────────────┘
```

### Couches de l'application

1. **Presentation Layer** : Jetpack Compose UI + ViewModels
2. **Domain Layer** : Use Cases + Domain Models
3. **Data Layer** : Repositories + Data Sources + API Services

---

## Structure du projet

```
app/src/main/java/com/example/damandroid/
│
├── api/                          # Services API et WebSocket
│   ├── ActivityApiService.kt    # Endpoints activités
│   ├── ChatApiService.kt         # Endpoints chats
│   ├── RetrofitClient.kt         # Configuration Retrofit
│   ├── ActivityRoomWebSocketService.kt  # WebSocket pour Activity Rooms
│   └── ...
│
├── auth/                         # Authentification
│   └── UserSession.kt            # Gestion session utilisateur
│
├── data/                         # Couche Data
│   ├── datasource/               # Sources de données (Remote/Local)
│   ├── repository/               # Implémentations repositories
│   ├── model/                    # DTOs (Data Transfer Objects)
│   └── mapper/                   # Mappers DTO → Domain
│
├── domain/                       # Couche Domain
│   ├── model/                    # Modèles métier
│   ├── repository/              # Interfaces repositories
│   └── usecase/                  # Use cases
│
├── presentation/                 # Couche Presentation
│   ├── chat/                     # Module Chat
│   │   ├── ui/                   # Écrans Compose
│   │   ├── viewmodel/            # ViewModels
│   │   └── model/                # UI States
│   │
│   ├── homefeed/                 # Module Home Feed
│   ├── discover/                 # Module Discover
│   ├── createactivity/           # Module Création activité
│   ├── aimatchmaker/             # Module AI Matchmaker
│   └── ...
│
└── ui/                           # Composants UI réutilisables
    └── theme/                    # Thème et couleurs
```

---

## Fonctionnalités principales

### 1. Authentification
- **Login/Register** : Email, Google Sign-In, Facebook Sign-In
- **Session Management** : `UserSession` singleton pour gérer le token JWT
- **Token Refresh** : Gestion automatique via `AuthInterceptor`

### 2. Home Feed
- **Filtres d'activités** : Public, Friends, Mine
- **Rejoindre une activité** : Bouton "Join" avec confirmation
- **Chat de groupe** : Création automatique pour chaque activité
- **Badges** : Coach/Individual sur les cartes d'activité

### 3. Chat
- **Chats individuels** : 1-to-1 entre utilisateurs
- **Chats de groupe** : Pour les activités
- **Polling** : Mise à jour automatique toutes les 3 secondes
- **Participants** : Affichage avec badge "Admin" pour le créateur
- **Quitter un groupe** : Fonctionnalité implémentée

### 4. Discover
- **Verified Coach** : Section statique (Amelia Carter)
- **Browse by Sport** : Filtrage par type de sport
- **Trending Near You** : Activités filtrées par sport sélectionné
- **Chat Now** : Bouton pour rejoindre le chat de groupe

### 5. Create Activity
- **Formulaire complet** : Sport, titre, description, localisation, date, heure
- **Autocomplétion localisation** : Intégration OpenWeather API
- **Validation** : Vérification des champs requis

### 6. AI Matchmaker
- **Chat conversationnel** : Interface de chat avec l'IA
- **Suggestions** : Partenaires et activités suggérées
- **Cartes de résultats** : Affichage professionnel avec badges match %

---

## Architecture MVVM

### Exemple : ChatViewModel

```kotlin
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val chatId: String
) : ViewModel() {
    
    // State observable
    private val _uiState = MutableStateFlow(ChatUiState(isLoading = true))
    val uiState: StateFlow<ChatUiState> = _uiState
    
    // Polling automatique
    private var pollingJob: Job? = null
    private val pollingIntervalMs = 3000L
    
    init {
        loadMessagesWithRetry()
        markChatAsRead()
    }
    
    // Actions
    fun sendMessage(text: String) { ... }
    fun refresh() { ... }
}
```

### Flux de données

1. **User Action** → View appelle ViewModel
2. **ViewModel** → Appelle Repository
3. **Repository** → Appelle DataSource (API)
4. **Response** → Mapper DTO → Domain
5. **Domain** → ViewModel met à jour State
6. **State** → View observe et met à jour UI

---

## API et Backend

### Base URL
```
https://apinest-production.up.railway.app/
```

### Endpoints principaux

#### Activités
- `GET /activities?visibility=public` - Liste activités publiques
- `GET /activities/my-activities` - Activités de l'utilisateur
- `POST /activities` - Créer une activité
- `POST /activities/:id/join` - Rejoindre une activité
- `POST /activities/:id/group-chat` - Créer/rejoindre chat de groupe

#### Chats
- `GET /chats` - Liste des chats
- `GET /chats/:id/messages` - Messages d'un chat
- `POST /chats/:id/messages` - Envoyer un message
- `DELETE /chats/:id/leave` - Quitter un groupe
- `GET /chats/:id/participants` - Participants d'un chat

#### Authentification
- `POST /auth/login` - Connexion
- `POST /auth/register` - Inscription
- Token JWT dans header `Authorization: Bearer <token>`

### Configuration Retrofit

```kotlin
object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)      // Ajoute token JWT
        .addInterceptor(responseBodyInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()
    
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}
```

---

## Gestion des chats

### Chats individuels (1-to-1)

**Architecture :**
- **Polling** : Vérification toutes les 3 secondes
- **ChatViewModel** : Gère l'état et le polling
- **ChatScreen** : Interface Compose avec LazyColumn

**Flux :**
1. Ouverture chat → Chargement initial des messages
2. Polling démarre automatiquement
3. Nouveaux messages détectés → Mise à jour UI
4. Fermeture chat → Arrêt du polling

**Code clé :**
```kotlin
private fun startPolling() {
    pollingJob = viewModelScope.launch {
        while (true) {
            delay(3000L)
            val newMessages = chatRepository.getMessages(chatId)
            // Comparer et mettre à jour si nouveaux messages
        }
    }
}
```

### Chats de groupe (Activity Rooms)

**Architecture :**
- **WebSocket** : Connexion persistante (priorité)
- **Polling** : Fallback si WebSocket échoue
- **ActivityRoomWebSocketService** : Gestion WebSocket

**Flux :**
1. Rejoindre activité → `joinActivity()`
2. Créer/rejoindre chat → `createOrGetActivityGroupChat()`
3. Connexion WebSocket → Messages en temps réel
4. Si WebSocket échoue → Bascule sur polling

**WebSocket Events :**
- `new-message` : Nouveau message reçu
- `user-typing` : Utilisateur en train d'écrire
- `user-joined` : Nouveau participant
- `user-left` : Participant parti

---

## Découverte d'activités

### Discover Screen

**Sections :**
1. **Verified Coach** : Carte statique (Amelia Carter)
2. **Browse by Sport** : Grille de 9 sports cliquables
3. **Trending Near You** : Activités filtrées par sport

**Filtrage par sport :**
- Clic sur un sport → Filtre les activités
- Appel API : `GET /activities?visibility=public&sportType=<sport>`
- Mise à jour automatique de "Trending Near You"

**Chat Now :**
- Bouton sur chaque carte d'activité
- Rejoint l'activité si nécessaire
- Crée/rejoint le chat de groupe
- Navigation vers ChatScreen

### Create Activity

**Fonctionnalités :**
- **Autocomplétion localisation** : OpenWeather Geocoding API
- **Validation** : Champs requis marqués *
- **Date/Time Pickers** : Sélecteurs natifs Material3
- **Slider participants** : 2-20 participants

**Flux de création :**
1. Remplir formulaire
2. Validation côté client
3. Appel API `POST /activities`
4. Succès → Retour Home Feed + Message de confirmation

---

## AI Matchmaker

### Architecture

**Composants :**
- **AIMatchmakerViewModel** : Gère conversation et recommandations
- **AIMatchmakerScreen** : Interface chat avec bulles de message
- **AIMatchmakerRepository** : Appels API backend

**Flux conversationnel :**
1. Message initial de l'IA avec options
2. Utilisateur clique option ou tape message
3. Envoi à l'API avec historique de conversation
4. Réponse avec suggestions (utilisateurs/activités)
5. Affichage cartes de résultats

**Types de messages :**
- **AI Message** : Bulle gauche avec icône sparkle
- **User Message** : Bulle droite avec gradient
- **Options** : Boutons cliquables sous message AI
- **Results** : Cartes PersonResult ou EventResult

**Nettoyage des réponses :**
- Suppression des IDs d'activité/utilisateur
- Suppression markdown (**)
- Formatage propre pour affichage

---

## WebSocket vs Polling

### Implémentation actuelle

| Type de chat | Méthode | Intervalle |
|--------------|---------|------------|
| **Chats individuels** | Polling | 3 secondes |
| **Activity Rooms** | WebSocket + Polling fallback | Temps réel |

### Polling (Chats individuels)

**Avantages :**
- ✅ Simple à implémenter
- ✅ Fonctionne toujours
- ✅ Pas de gestion de reconnexion

**Inconvénients :**
- ❌ Délai jusqu'à 3 secondes
- ❌ Plus de requêtes réseau
- ❌ Consommation batterie

**Code :**
```kotlin
private fun startPolling() {
    pollingJob = viewModelScope.launch {
        while (true) {
            delay(3000L)
            val newMessages = chatRepository.getMessages(chatId)
            // Mise à jour si nouveaux messages
        }
    }
}
```

### WebSocket (Activity Rooms)

**Avantages :**
- ✅ Temps réel instantané
- ✅ Moins de requêtes
- ✅ Meilleure expérience

**Inconvénients :**
- ❌ Plus complexe
- ❌ Peut se déconnecter
- ❌ Nécessite gestion reconnexion

**Code :**
```kotlin
socket?.on("new-message") { args ->
    val messageDto = parseMessage(args[0] as JSONObject)
    _messages.tryEmit(messageDto)
}
```

### Recommandation future

Pour les chats individuels, implémenter :
1. **WebSocket en priorité** (temps réel)
2. **Polling en fallback** (si WebSocket échoue)

---

## Dépendances principales

### Build.gradle (app)

```kotlin
dependencies {
    // Compose
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.material3:material3:$material3_version")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.retrofit2:converter-gson:$retrofit_version")
    
    // WebSocket (Socket.IO)
    implementation("io.socket:socket.io-client:2.1.0")
    
    // Coil (Images)
    implementation("io.coil-kt:coil-compose:$coil_version")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:$nav_version")
}
```

---

## Patterns et bonnes pratiques

### 1. State Management
- **StateFlow** pour état observable
- **MutableStateFlow** privé, StateFlow public
- Mise à jour via `update { }`

### 2. Error Handling
- **runCatching** pour gestion d'erreurs
- Messages d'erreur utilisateur-friendly
- Logging pour debugging

### 3. Repository Pattern
- Interface dans domain layer
- Implémentation dans data layer
- Abstraction de la source de données

### 4. Dependency Injection
- Injection manuelle (pas de Hilt/Dagger)
- ViewModels créés dans MainActivity
- Repositories passés aux ViewModels

### 5. UI Components
- Composables réutilisables
- Palette de couleurs centralisée
- Thème adaptatif (Dark/Light)

---

## Gestion des erreurs

### Erreurs API communes

**403 Forbidden :**
- Chat : Retry avec délai progressif (jusqu'à 5 tentatives)
- Activité : Message explicite + suggestion de réessayer

**401 Unauthorized :**
- Token expiré → Redirection login
- Géré par `AuthInterceptor`

**500 Internal Server Error :**
- Message générique utilisateur
- Log détaillé pour debugging

### Retry Logic

```kotlin
private fun loadMessagesWithRetry(maxRetries: Int = 5) {
    repeat(maxRetries) { attempt ->
        runCatching { ... }
            .onSuccess { return }
            .onFailure { 
                if (is403Error && attempt < maxRetries - 1) {
                    delay(delayMs * (attempt + 1)) // Délai progressif
                }
            }
    }
}
```

---

## Tests et debugging

### Logging
- **OkHttp LoggingInterceptor** : Logs toutes les requêtes HTTP
- **ResponseBodyInterceptor** : Capture bodies de réponse
- **Log.d/Log.e** : Logs personnalisés dans ViewModels

### Points de contrôle
1. **UserSession** : Vérifier token présent
2. **RetrofitClient** : Vérifier BASE_URL correcte
3. **StateFlow** : Observer état dans ViewModel
4. **Polling** : Vérifier job actif

---

## Améliorations futures

### Court terme
- [ ] WebSocket pour chats individuels
- [ ] Notifications push pour nouveaux messages
- [ ] Cache local des messages

### Moyen terme
- [ ] Pagination pour listes longues
- [ ] Recherche dans les chats
- [ ] Partage de fichiers/images

### Long terme
- [ ] Appels vocaux/vidéo
- [ ] Messages vocaux
- [ ] Réactions aux messages

---

## Support et maintenance

### Structure de logs
```
TAG: [Module] - Message
Exemples:
- ChatViewModel: Polling error
- WebSocket: Connection error
- ActivityRoomRepository: Error joining activity
```

### Points d'attention
1. **Token expiration** : Gérer refresh automatique
2. **Connexion réseau** : Gérer offline mode
3. **Performance** : Optimiser polling interval si nécessaire
4. **Batterie** : Surveiller consommation polling

---

## Conclusion

Cette documentation couvre l'architecture complète de l'application DamAndroid. Pour toute question ou amélioration, référez-vous aux sections correspondantes ou consultez le code source directement.

**Dernière mise à jour** : Novembre 2025




