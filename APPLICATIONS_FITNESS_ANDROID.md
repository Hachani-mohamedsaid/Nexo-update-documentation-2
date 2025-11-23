# 📱 Applications de Fitness pour Android - Récupération des Données

## 🎯 Applications Principales

### 1. **Google Fit** (Recommandé)
- **Nom de l'application** : `Google Fit`
- **Package name** : `com.google.android.apps.fitness`
- **API** : Google Fit API
- **Données disponibles** :
  - ✅ Workouts (activités)
  - ✅ Calories brûlées
  - ✅ Minutes d'activité
  - ✅ Pas (steps)
  - ✅ Distance parcourue
  - ✅ Fréquence cardiaque
  - ✅ Poids

**Avantages** :
- Gratuit
- Compatible avec la plupart des appareils Android
- API officielle Google
- Synchronise avec d'autres applications (Strava, MyFitnessPal, etc.)

**Documentation** : https://developers.google.com/fit/android/get-started

---

### 2. **Health Connect** (Android 14+)
- **Nom de l'application** : `Health Connect`
- **Package name** : `com.google.android.apps.healthdata`
- **API** : Health Connect API
- **Données disponibles** :
  - ✅ Workouts
  - ✅ Calories
  - ✅ Minutes d'activité
  - ✅ Pas
  - ✅ Distance
  - ✅ Fréquence cardiaque
  - ✅ Sommeil
  - ✅ Nutrition

**Avantages** :
- Standard Android officiel (depuis Android 14)
- Centralise les données de toutes les applications
- Plus sécurisé et privé
- Support multi-applications

**Documentation** : https://developer.android.com/guide/health-and-fitness/health-connect

---

### 3. **Samsung Health** (Samsung uniquement)
- **Nom de l'application** : `Samsung Health`
- **Package name** : `com.sec.android.app.shealth`
- **API** : Samsung Health SDK
- **Données disponibles** :
  - ✅ Workouts
  - ✅ Calories
  - ✅ Minutes
  - ✅ Pas
  - ✅ Fréquence cardiaque
  - ✅ Poids
  - ✅ Sommeil

**Avantages** :
- Intégration native sur appareils Samsung
- Données très précises
- Beaucoup de fonctionnalités

**Inconvénients** :
- Uniquement pour appareils Samsung
- SDK moins documenté

---

## 🔧 Comment Choisir ?

### Pour votre application (DamAndroid) :

**Option 1 : Google Fit** (Recommandé pour compatibilité maximale)
```kotlin
// Utilise Google Fit API
implementation("com.google.android.gms:play-services-fitness:21.1.0")
```

**Option 2 : Health Connect** (Recommandé pour Android 14+)
```kotlin
// Utilise Health Connect
implementation("androidx.health.connect:connect-client:1.1.0-alpha07")
```

**Option 3 : Les deux** (Meilleure compatibilité)
- Utiliser Health Connect si disponible (Android 14+)
- Fallback vers Google Fit pour les anciens appareils

---

## 📊 Comparaison Rapide

| Application | Compatibilité | Facilité d'Intégration | Données Disponibles |
|------------|--------------|----------------------|---------------------|
| **Google Fit** | ✅ Tous Android | ⭐⭐⭐ Facile | ⭐⭐⭐⭐ Complètes |
| **Health Connect** | ⚠️ Android 14+ | ⭐⭐⭐⭐ Très facile | ⭐⭐⭐⭐⭐ Très complètes |
| **Samsung Health** | ❌ Samsung uniquement | ⭐⭐ Moyenne | ⭐⭐⭐⭐ Complètes |

---

## 💡 Recommandation pour votre Projet

### Utiliser **Google Fit** car :
1. ✅ Compatible avec tous les appareils Android
2. ✅ API bien documentée
3. ✅ Facile à intégrer
4. ✅ Beaucoup d'utilisateurs ont déjà Google Fit installé
5. ✅ Synchronise automatiquement avec d'autres apps

### Code d'Exemple - Vérifier si Google Fit est installé :

```kotlin
fun isGoogleFitInstalled(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo(
            "com.google.android.apps.fitness",
            PackageManager.GET_ACTIVITIES
        )
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
```

---

## 🔗 Liens Utiles

- **Google Fit** : https://www.google.com/fit/
- **Health Connect** : https://developer.android.com/guide/health-and-fitness/health-connect
- **Google Fit API** : https://developers.google.com/fit/android/get-started
- **Samsung Health** : https://developer.samsung.com/health

---

## 📝 Résumé

**Pour récupérer workouts, calories, minutes :**

1. **Google Fit** → Application la plus utilisée, compatible partout
2. **Health Connect** → Standard Android moderne (Android 14+)
3. **Samsung Health** → Uniquement Samsung

**Recommandation** : Commencer avec **Google Fit** pour une compatibilité maximale.

