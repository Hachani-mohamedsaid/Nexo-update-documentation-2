# 🗺️ Guide d'Implémentation de la Carte avec OpenStreetMap

## ✅ Fonctionnalités Implémentées

### 1. **Carte OpenStreetMap (100% Gratuit)**
- ✅ Utilise OpenStreetMap avec Leaflet.js
- ✅ Aucune clé API requise
- ✅ Aucune limite de requêtes
- ✅ Affichage dans une WebView native

### 2. **Demande de Permissions au Runtime**
- ✅ Demande automatique des permissions de localisation
- ✅ Dialog explicatif si l'utilisateur refuse
- ✅ Gestion des cas où les permissions ne sont pas accordées

### 3. **Géocodage des Adresses**
- ✅ Service de géocodage utilisant OpenStreetMap Nominatim (100% gratuit)
- ✅ Conversion automatique des adresses en coordonnées
- ✅ Fallback si les coordonnées ne sont pas disponibles dans l'API

### 4. **Filtrage par Distance**
- ✅ Calcul de distance avec la formule de Haversine
- ✅ Filtrage des activités dans un rayon de 50 km par défaut
- ✅ Affichage uniquement des activités proches de l'utilisateur

---

## 📁 Fichiers Créés/Modifiés

### Nouveaux Fichiers
- `ActivitiesMapView.kt` : Composable de carte OpenStreetMap
- `LocationService.kt` : Service de localisation GPS/réseau
- `GeocodingService.kt` : Service de géocodage (adresse → coordonnées)

### Fichiers Modifiés
- `AISuggestionsScreen.kt` : Intégration de la carte avec permissions et filtrage
- `AndroidManifest.xml` : Permissions de localisation
- `build.gradle.kts` : Aucune dépendance supplémentaire (WebView natif)

---

## 🔧 Fonctionnement

### 1. Demande de Permissions

Quand l'utilisateur ouvre la page Sessions en mode "Map" :

1. **Vérification des permissions** : L'app vérifie si les permissions sont accordées
2. **Demande automatique** : Si non accordées, demande via `ActivityResultContracts.RequestMultiplePermissions`
3. **Dialog explicatif** : Si l'utilisateur refuse, affiche un dialog pour expliquer l'importance
4. **Récupération de la localisation** : Une fois accordées, récupère la position GPS

### 2. Géocodage des Adresses

Pour chaque activité récupérée depuis l'API :

1. **Vérification des coordonnées** : Vérifie si `latitude` et `longitude` sont présents
2. **Géocodage si nécessaire** : Si absents, utilise `GeocodingService` avec OpenStreetMap Nominatim
3. **Stockage** : Les coordonnées sont stockées dans la chaîne `location` au format : `"Adresse, lat, lng"`

**Exemple** :
```kotlin
// Avant géocodage
location = "123 Main St, New York"

// Après géocodage
location = "123 Main St, New York, 40.7128, -74.0060"
```

### 3. Filtrage par Distance

Une fois la localisation de l'utilisateur obtenue :

1. **Calcul de distance** : Pour chaque activité, calcule la distance avec la formule de Haversine
2. **Filtrage** : Garde uniquement les activités dans un rayon de 50 km (configurable via `maxDistanceKm`)
3. **Affichage** : Affiche uniquement les activités proches sur la carte

**Formule de Haversine** :
```
distance = 2 * R * atan2(√a, √(1-a))
où a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
R = 6371 km (rayon de la Terre)
```

---

## 🎯 Utilisation

### Dans la Page Sessions

1. **Ouvrir la page Sessions**
2. **Cliquer sur "Map"** dans le toggle
3. **Autoriser les permissions** si demandées
4. **Voir votre position** (marqueur vert) et les activités proches (marqueurs violets)

### Marqueurs sur la Carte

- **Marqueur vert** : Votre position actuelle
- **Marqueurs violets** : Activités proches (avec icône du sport)
- **Popup** : Cliquer sur un marqueur pour voir les détails

---

## ⚙️ Configuration

### Distance Maximale

Par défaut, les activités sont filtrées dans un rayon de **50 km**. Pour modifier :

```kotlin
var maxDistanceKm by remember { mutableStateOf(50.0) } // Modifier cette valeur
```

### Géocodage

Le service de géocodage utilise **OpenStreetMap Nominatim** qui est :
- ✅ 100% gratuit
- ✅ Pas de clé API requise
- ⚠️ Limite : 1 requête par seconde (respectée automatiquement)

---

## 🔍 API OpenStreetMap Nominatim

### Endpoint Utilisé

```
GET https://nominatim.openstreetmap.org/search?q={address}&format=json&limit=1
```

### Réponse

```json
[
  {
    "lat": "40.7128",
    "lon": "-74.0060",
    "display_name": "New York, NY, USA"
  }
]
```

### Limites

- **1 requête par seconde** (respectée par le code)
- **Pas de clé API requise**
- **Gratuit et illimité** pour usage raisonnable

---

## 📱 Permissions Android

### Permissions Requises

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### Demande au Runtime

Le code demande automatiquement les permissions au runtime (Android 6.0+) :

```kotlin
val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    // Gérer la réponse
}
```

---

## 🐛 Dépannage

### La carte ne s'affiche pas

1. Vérifier la connexion Internet
2. Vérifier que les permissions sont accordées
3. Vérifier les logs pour les erreurs JavaScript

### La localisation ne fonctionne pas

1. Vérifier que le GPS est activé
2. Vérifier que les permissions sont accordées
3. Essayer en extérieur pour une meilleure réception GPS

### Les activités ne s'affichent pas

1. Vérifier que l'API retourne des activités avec `latitude` et `longitude`
2. Vérifier que le géocodage fonctionne (logs)
3. Vérifier que la distance maximale n'est pas trop restrictive

---

## 🚀 Améliorations Futures (Optionnel)

1. **Cache de géocodage** : Mettre en cache les résultats pour éviter les requêtes répétées
2. **Filtre de distance ajustable** : Permettre à l'utilisateur de changer le rayon
3. **Clustering de marqueurs** : Grouper les marqueurs proches pour améliorer les performances
4. **Directions** : Afficher l'itinéraire vers une activité
5. **Actualisation automatique** : Rafraîchir la position et les activités périodiquement

---

## 📚 Ressources

- **OpenStreetMap** : https://www.openstreetmap.org/
- **Nominatim API** : https://nominatim.org/
- **Leaflet.js** : https://leafletjs.com/
- **Formule de Haversine** : https://en.wikipedia.org/wiki/Haversine_formula

---

## ✅ Checklist d'Implémentation

- [x] Carte OpenStreetMap intégrée
- [x] Permissions de localisation demandées au runtime
- [x] Service de géocodage créé
- [x] Filtrage par distance implémenté
- [x] Marqueurs d'activités affichés
- [x] Marqueur de position utilisateur affiché
- [x] Dialog de permissions ajouté
- [x] Gestion des erreurs implémentée

---

## 🎉 Résultat Final

La page Sessions affiche maintenant :
- ✅ Une vraie carte OpenStreetMap (100% gratuit)
- ✅ Votre position actuelle (marqueur vert)
- ✅ Toutes les activités proches (marqueurs violets avec icônes)
- ✅ Filtrage automatique par distance (50 km par défaut)
- ✅ Géocodage automatique des adresses sans coordonnées

