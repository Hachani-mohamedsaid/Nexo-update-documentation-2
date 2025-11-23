# Guide : Concept de Carte (Map) dans DamAndroid

## Vue d'ensemble

La fonctionnalité **Map** (carte) permet d'afficher visuellement les activités sportives sur une carte géographique interactive. Les utilisateurs peuvent voir leur position et les activités à proximité, puis cliquer sur les marqueurs pour voir les détails.

---

## Architecture de la Carte

### Technologie utilisée

**OpenStreetMap + Leaflet.js** (100% gratuit, pas besoin de clé API)

- ✅ **OpenStreetMap** : Service de cartes gratuit et open-source
- ✅ **Leaflet.js** : Bibliothèque JavaScript pour cartes interactives
- ✅ **WebView** : Affichage de la carte HTML/JavaScript dans Android

### Fichiers principaux

1. **`ActivitiesMapView.kt`** : Composable principal pour la carte
2. **`OSMMapView.kt`** : Alternative (si différente)
3. **`AISuggestionsScreen.kt`** : Intégration de la carte dans l'écran Sessions

---

## Fonctionnement

### 1. Structure de la carte

```
┌─────────────────────────────┐
│   Carte OpenStreetMap       │
│                             │
│   🟢 [Position utilisateur] │
│                             │
│   🟣 [Activité 1]           │
│   🟣 [Activité 2]           │
│   🟣 [Activité 3]           │
│                             │
│   (Zoom, Pan, Click)        │
└─────────────────────────────┘
```

### 2. Flux de données

```
1. UTILISATEUR OUVRE LA CARTE
   ↓
   AISuggestionsScreen charge les activités
   ↓
   Récupération de la position GPS de l'utilisateur
   ↓
   Filtrage des activités par distance
   ↓
   ActivitiesMapView affiche la carte
   ↓

2. AFFICHAGE SUR LA CARTE
   ↓
   WebView charge HTML avec Leaflet.js
   ↓
   Marqueur vert = Position utilisateur
   ↓
   Marqueurs violets = Activités sportives
   ↓
   Popup au clic = Détails de l'activité
   ↓

3. INTERACTION UTILISATEUR
   ↓
   Clic sur marqueur → Popup avec infos
   ↓
   Clic sur popup → Navigation vers détails
   ↓
   Zoom/Pan → Navigation sur la carte
```

---

## Composants principaux

### ActivitiesMapView

**Fichier :** `app/src/main/java/com/example/damandroid/presentation/aisuggestions/ui/ActivitiesMapView.kt`

```kotlin
@Composable
fun ActivitiesMapView(
    activities: List<SuggestedActivity>,      // Liste des activités
    userLatitude: Double? = null,            // Latitude utilisateur
    userLongitude: Double? = null,           // Longitude utilisateur
    onActivityClick: ((SuggestedActivity) -> Unit)? = null,  // Callback clic
    modifier: Modifier = Modifier
)
```

**Paramètres :**
- `activities` : Liste des activités à afficher sur la carte
- `userLatitude/Longitude` : Position GPS de l'utilisateur
- `onActivityClick` : Callback quand l'utilisateur clique sur une activité

### Marqueurs

**Types de marqueurs :**

1. **Marqueur utilisateur** (vert)
   - Position actuelle de l'utilisateur
   - Cercle vert avec bordure blanche
   - Centré automatiquement sur la carte

2. **Marqueurs d'activités** (violet)
   - Chaque activité = 1 marqueur
   - Icône selon le sport (🏃 🧘 🏀 etc.)
   - Popup au clic avec détails

---

## Intégration dans l'application

### Accès à la carte

**Navigation :**
```
MainActivity
  ↓
  BottomNav → Tab "Sessions" (icône CalendarToday)
  ↓
  AISuggestionsRoute
  ↓
  AISuggestionsScreen
  ↓
  Toggle "List" ↔ "Map"
  ↓
  ActivitiesMapView (si mode Map)
```

### Code d'intégration

**Dans `AISuggestionsScreen.kt` :**

```kotlin
var viewMode by remember { mutableStateOf("list") }  // "list" ou "map"

// Toggle entre liste et carte
Row {
    IconButton(onClick = { viewMode = "list" }) {
        Icon(Icons.Default.List, "List")
    }
    IconButton(onClick = { viewMode = "map" }) {
        Icon(Icons.Default.Map, "Map")
    }
}

// Afficher selon le mode
when (viewMode) {
    "list" -> ActivitiesListView(...)
    "map" -> ActivitiesMapView(
        activities = filteredActivities,
        userLatitude = userLocation?.first,
        userLongitude = userLocation?.second,
        onActivityClick = onActivityClick
    )
}
```

---

## Gestion de la localisation

### 1. Permissions

**Permissions requises :**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

**Demande de permissions :**
```kotlin
val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
        // Récupérer la position
        userLocation = locationService.getCurrentLocation()
    }
}
```

### 2. Récupération de la position

**Service utilisé :** `LocationService`

```kotlin
val locationService = remember { LocationService(context) }
var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

LaunchedEffect(Unit) {
    if (locationService.hasLocationPermission()) {
        userLocation = withContext(Dispatchers.IO) {
            locationService.getCurrentLocation()
        }
    }
}
```

### 3. Calcul de distance

**Service utilisé :** `GeocodingService`

```kotlin
val geocodingService = remember { GeocodingService() }

// Calculer la distance entre deux points
val distance = geocodingService.calculateDistance(
    userLat, userLng,        // Position utilisateur
    activityLat, activityLng // Position activité
)
```

---

## Filtrage par distance

### Fonctionnalité

Les activités peuvent être filtrées par distance maximale depuis la position de l'utilisateur.

**Code :**
```kotlin
var maxDistanceKm by remember { mutableStateOf(50.0) }  // 50 km par défaut

val filteredActivities = remember(activities, userLocation, maxDistanceKm) {
    if (userLocation != null) {
        activities.filter { activity ->
            val (activityLat, activityLng) = extractCoordinates(activity.location)
            if (activityLat != null && activityLng != null) {
                val distance = geocodingService.calculateDistance(
                    userLocation!!.first, userLocation!!.second,
                    activityLat, activityLng
                )
                distance <= maxDistanceKm
            } else false
        }
    } else {
        activities  // Afficher toutes si pas de position
    }
}
```

**Slider de distance :**
```kotlin
Slider(
    value = maxDistanceKm,
    onValueChange = { maxDistanceKm = it },
    valueRange = 1.0..100.0,
    steps = 9
)
Text("Rayon: ${maxDistanceKm.toInt()} km")
```

---

## Format des coordonnées

### Extraction depuis location string

**Format attendu :**
```
"Location Name, latitude, longitude"
Exemple: "Paris, 48.8566, 2.3522"
```

**Fonction d'extraction :**
```kotlin
fun extractCoordinatesFromLocation(location: String): Pair<Double?, Double?> {
    val parts = location.split(",")
    if (parts.size >= 3) {
        val lat = parts[parts.size - 2].trim().toDoubleOrNull()
        val lng = parts[parts.size - 1].trim().toDoubleOrNull()
        return Pair(lat, lng)
    }
    return Pair(null, null)
}
```

### Géocodage (si pas de coordonnées)

**Si une activité n'a pas de coordonnées :**
```kotlin
if (lat == null || lng == null) {
    val geocoded = geocodingService.geocode(activity.location)
    if (geocoded != null) {
        lat = geocoded.first
        lng = geocoded.second
    }
}
```

---

## Communication WebView ↔ Android

### Interface JavaScript

**Bridge Android ↔ JavaScript :**

```kotlin
class MapInterface(private val onMarkerClick: (String) -> Unit) {
    @JavascriptInterface
    fun onMarkerClick(activityId: String) {
        onMarkerClick(activityId)
    }
}

// Dans WebView
webView.addJavascriptInterface(
    MapInterface { activityId ->
        activities.find { it.id == activityId }?.let {
            onActivityClick?.invoke(it)
        }
    },
    "AndroidInterface"
)
```

**Côté JavaScript (dans HTML) :**
```javascript
marker.on('click', function() {
    if (window.AndroidInterface && activity.id) {
        window.AndroidInterface.onMarkerClick(activity.id);
    }
});
```

### Mise à jour dynamique

**Mise à jour des marqueurs :**
```kotlin
LaunchedEffect(activities, userLatitude, userLongitude, isMapReady) {
    if (isMapReady && webView != null) {
        val activitiesJson = JSONArray()
        activities.forEach { activity ->
            activitiesJson.put(JSONObject().apply {
                put("id", activity.id)
                put("title", activity.title)
                put("sport", activity.sport)
                put("latitude", lat)
                put("longitude", lng)
            })
        }
        
        // Injecter dans JavaScript
        webView?.evaluateJavascript(
            "window.updateMap($userLat, $userLng, ${activitiesJson.toString()});",
            null
        )
    }
}
```

---

## Icônes de sports

### Mapping sport → emoji

**Fonction JavaScript :**
```javascript
function getSportIcon(sport) {
    var sportLower = sport.toLowerCase();
    if (sportLower.includes('run')) return '🏃';
    if (sportLower.includes('yoga')) return '🧘';
    if (sportLower.includes('basket')) return '🏀';
    if (sportLower.includes('swim')) return '🏊';
    if (sportLower.includes('tennis')) return '🎾';
    if (sportLower.includes('football')) return '⚽';
    return '🎯';  // Par défaut
}
```

**Affichage :**
- Chaque marqueur d'activité affiche l'emoji correspondant au sport
- Cercle violet avec emoji au centre

---

## Popup d'information

### Contenu du popup

Quand l'utilisateur clique sur un marqueur, un popup s'affiche avec :

```
┌─────────────────────┐
│  [Titre activité]   │
│  🏃 Running         │
│  📍 Location        │
│  👤 Organizer       │
└─────────────────────┘
```

**Code JavaScript :**
```javascript
marker.bindPopup(
    '<b>' + activity.title + '</b><br/>' +
    sportIcon + ' ' + activity.sport + '<br/>' +
    '📍 ' + locationText + '<br/>' +
    '👤 ' + activity.organizer
);
```

### Action au clic

**Navigation vers détails :**
```kotlin
onActivityClick: ((SuggestedActivity) -> Unit)? = null

// Dans le popup JavaScript
marker.on('click', function() {
    window.AndroidInterface.onMarkerClick(activity.id);
})

// Dans Android
MapInterface { activityId ->
    activities.find { it.id == activityId }?.let { activity ->
        onActivityClick?.invoke(activity)  // Navigation
    }
}
```

---

## Exemple d'utilisation complète

### Dans AISuggestionsScreen

```kotlin
@Composable
fun LegacySessionsContent(...) {
    // 1. Récupérer position utilisateur
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    val locationService = remember { LocationService(context) }
    
    LaunchedEffect(Unit) {
        if (locationService.hasLocationPermission()) {
            userLocation = locationService.getCurrentLocation()
        }
    }
    
    // 2. Charger activités depuis API
    var activitiesWithLocation by remember { mutableStateOf<List<SuggestedActivity>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        activitiesWithLocation = fetchActivitiesFromAPI()
    }
    
    // 3. Filtrer par distance
    val filteredActivities = remember(activitiesWithLocation, userLocation) {
        filterByDistance(activitiesWithLocation, userLocation, maxDistanceKm = 50.0)
    }
    
    // 4. Toggle List/Map
    var viewMode by remember { mutableStateOf("list") }
    
    // 5. Afficher carte
    if (viewMode == "map") {
        ActivitiesMapView(
            activities = filteredActivities,
            userLatitude = userLocation?.first,
            userLongitude = userLocation?.second,
            onActivityClick = { activity ->
                // Navigation vers détails
                navController.navigate("activity/${activity.id}")
            }
        )
    }
}
```

---

## Avantages de cette approche

### ✅ Avantages

1. **Gratuit** : OpenStreetMap est 100% gratuit (pas de clé API)
2. **Léger** : Pas besoin d'installer Google Play Services
3. **Flexible** : Contrôle total sur l'affichage
4. **Offline possible** : Peut fonctionner avec cache de tuiles

### ⚠️ Limitations

1. **Dépendance Internet** : Nécessite connexion pour charger les tuiles
2. **Performance** : WebView peut être plus lent que native
3. **Complexité** : Communication JavaScript ↔ Android

---

## Améliorations futures

### Court terme
- [ ] Cache des tuiles pour mode offline
- [ ] Cluster de marqueurs (regrouper quand zoom out)
- [ ] Filtres par sport sur la carte

### Moyen terme
- [ ] Itinéraire vers l'activité (navigation)
- [ ] Rayon de recherche visuel (cercle sur la carte)
- [ ] Mode satellite/hybride

### Long terme
- [ ] Carte native avec Google Maps (si budget)
- [ ] Heatmap des zones populaires
- [ ] Prédiction de trafic pour se rendre à l'activité

---

## Résumé

### Concept

**Map = Carte géographique interactive** qui affiche :
- 🟢 Position de l'utilisateur
- 🟣 Activités sportives à proximité
- 📍 Popups avec détails au clic

### Technologies

- **OpenStreetMap** : Service de cartes
- **Leaflet.js** : Bibliothèque JavaScript
- **WebView** : Affichage dans Android
- **LocationService** : GPS de l'utilisateur

### Flux

```
Utilisateur → Ouvre Sessions → Toggle Map
  ↓
Récupération position GPS
  ↓
Chargement activités depuis API
  ↓
Filtrage par distance
  ↓
Affichage sur carte OpenStreetMap
  ↓
Clic marqueur → Détails activité
```

---

**Dernière mise à jour :** Novembre 2025




