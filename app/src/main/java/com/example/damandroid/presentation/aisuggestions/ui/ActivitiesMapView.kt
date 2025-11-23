package com.example.damandroid.presentation.aisuggestions.ui

import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.damandroid.domain.model.SuggestedActivity
import org.json.JSONArray
import org.json.JSONObject

/**
 * Composable pour afficher une carte OpenStreetMap (100% gratuit)
 * Affiche la localisation de l'utilisateur et les activités proches
 */
@Composable
fun ActivitiesMapView(
    activities: List<SuggestedActivity>,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    onActivityClick: ((SuggestedActivity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isMapReady by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                
                // Important: Permettre le chargement de ressources depuis Internet
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        android.util.Log.d("ActivitiesMapView", "Page started loading: $url")
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isMapReady = true
                        android.util.Log.d("ActivitiesMapView", "Map page loaded successfully")
                        // Attendre un peu pour que Leaflet se charge complètement
                        view?.postDelayed({
                            view?.evaluateJavascript("console.log('WebView ready');", null)
                        }, 500)
                    }
                    
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        android.util.Log.e("ActivitiesMapView", "WebView error: $description at $failingUrl")
                    }
                    
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        android.util.Log.d("ActivitiesMapView", "Loading URL: $url")
                        return false
                    }
                }
                
                // Ajouter l'interface JavaScript pour la communication
                addJavascriptInterface(MapInterface { activityId ->
                    activities.find { it.id == activityId }?.let {
                        onActivityClick?.invoke(it)
                    }
                }, "AndroidInterface")
                
                webView = this
                
                // Charger la carte OpenStreetMap avec Leaflet
                val html = getMapHtml(userLatitude, userLongitude, activities)
                android.util.Log.d("ActivitiesMapView", "Loading map HTML (${html.length} chars)")
                loadDataWithBaseURL(
                    "https://unpkg.com/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = modifier.fillMaxSize()
    )

    // Mettre à jour la carte quand les activités changent
    LaunchedEffect(activities, userLatitude, userLongitude, isMapReady) {
        if (isMapReady && webView != null) {
            val activitiesJson = JSONArray()
            activities.forEach { activity ->
                val marker = activity.toMapMarker()
                activitiesJson.put(JSONObject().apply {
                    put("id", marker["id"])
                    put("title", marker["title"])
                    put("sport", marker["sport"])
                    put("location", marker["location"])
                    put("latitude", marker["latitude"])
                    put("longitude", marker["longitude"])
                    put("organizer", marker["organizer"])
                })
            }
            val jsonString = activitiesJson.toString()
            
            // Utiliser une méthode plus sûre : encoder le JSON en base64 ou utiliser une approche différente
            // Pour éviter les problèmes d'échappement, on va utiliser JSON.stringify côté JavaScript
            // en passant le JSON comme une chaîne JSON valide
            val safeJsonString = jsonString
                .replace("\\", "\\\\")  // Échapper les backslashes
                .replace("'", "\\'")    // Échapper les apostrophes
                .replace("\"", "\\\"")  // Échapper les guillemets
                .replace("\n", "\\n")   // Échapper les nouvelles lignes
                .replace("\r", "\\r")   // Échapper les retours chariot
                .replace("\t", "\\t")   // Échapper les tabulations
            
            // Utiliser evaluateJavascript avec une approche plus sûre
            val userLat = userLatitude ?: 0.0
            val userLng = userLongitude ?: 0.0
            
            // Utiliser une méthode plus sûre : passer le JSON comme argument direct à evaluateJavascript
            // en utilisant JSON.stringify pour éviter les problèmes d'échappement
            val jsCode = StringBuilder()
            jsCode.append("(function() {")
            jsCode.append("try {")
            jsCode.append("if (typeof window.updateMap === 'undefined') {")
            jsCode.append("console.log('updateMap not defined yet, waiting...');")
            jsCode.append("setTimeout(function() {")
            jsCode.append("try {")
            jsCode.append("var activities = ")
            jsCode.append(jsonString)  // Injecter directement le JSON sans échappement
            jsCode.append(";")
            jsCode.append("if (typeof window.updateMap !== 'undefined') {")
            jsCode.append("window.updateMap($userLat, $userLng, activities);")
            jsCode.append("} else {")
            jsCode.append("console.error('updateMap still not defined after retry');")
            jsCode.append("}")
            jsCode.append("} catch(e) {")
            jsCode.append("console.error('Error in retry:', e);")
            jsCode.append("}")
            jsCode.append("}, 1000);")
            jsCode.append("return;")
            jsCode.append("}")
            jsCode.append("var activities = ")
            jsCode.append(jsonString)  // Injecter directement le JSON sans échappement
            jsCode.append(";")
            jsCode.append("window.updateMap($userLat, $userLng, activities);")
            jsCode.append("} catch(e) {")
            jsCode.append("console.error('Error updating map:', e);")
            jsCode.append("}")
            jsCode.append("})();")
            
            webView?.evaluateJavascript(jsCode.toString(), null)
        }
    }
}

/**
 * Interface JavaScript pour la communication entre la carte et Android
 */
class MapInterface(private val onMarkerClick: (String) -> Unit) {
    @JavascriptInterface
    fun onMarkerClick(activityId: String) {
        onMarkerClick(activityId)
    }
}

/**
 * Convertit une SuggestedActivity en marqueur de carte
 */
private fun SuggestedActivity.toMapMarker(): Map<String, Any> {
    // Extraire latitude/longitude depuis location si possible
    // Format attendu: "Location • 1.2 mi" ou "Location, lat, lng"
    val (lat, lng) = extractCoordinatesFromLocation(location)
    
    return mapOf(
        "id" to id,
        "title" to title,
        "sport" to sport,
        "location" to location,
        "latitude" to (lat ?: 0.0),
        "longitude" to (lng ?: 0.0),
        "organizer" to organizer
    )
}

/**
 * Extrait les coordonnées depuis la chaîne de localisation
 * Si pas de coordonnées, génère des coordonnées aléatoires proches
 */
private fun extractCoordinatesFromLocation(location: String): Pair<Double?, Double?> {
    // Si la location contient des coordonnées (format: "Location, lat, lng")
    val parts = location.split(",")
    if (parts.size >= 3) {
        try {
            val lat = parts[parts.size - 2].trim().toDoubleOrNull()
            val lng = parts[parts.size - 1].trim().toDoubleOrNull()
            if (lat != null && lng != null) {
                return Pair(lat, lng)
            }
        } catch (e: Exception) {
            // Ignorer
        }
    }
    
    // Sinon, générer des coordonnées aléatoires (pour la démo)
    // En production, vous devriez utiliser un service de géocodage
    return Pair(null, null)
}

/**
 * Génère le HTML pour la carte OpenStreetMap avec Leaflet
 */
private fun getMapHtml(
    userLat: Double?,
    userLng: Double?,
    activities: List<SuggestedActivity>
): String {
    val defaultLat = userLat ?: 34.0522 // Los Angeles par défaut
    val defaultLng = userLng ?: -118.2437
    
    val activitiesJson = JSONArray()
    activities.forEach { activity ->
        val (lat, lng) = extractCoordinatesFromLocation(activity.location)
        val markerLat = lat ?: (defaultLat + (Math.random() - 0.5) * 0.1)
        val markerLng = lng ?: (defaultLng + (Math.random() - 0.5) * 0.1)
        
        activitiesJson.put(JSONObject().apply {
            put("id", activity.id)
            put("title", activity.title)
            put("sport", activity.sport)
            put("location", activity.location)
            put("latitude", markerLat)
            put("longitude", markerLng)
            put("organizer", activity.organizer)
        })
    }
    
    // Ne pas injecter les activités dans le HTML initial pour éviter les erreurs de syntaxe
    // Elles seront injectées via evaluateJavascript après le chargement de la page
    val activitiesJsonString = "[]"  // Tableau vide pour l'initialisation
    
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            html, body { width: 100%; height: 100%; overflow: hidden; }
            #map { width: 100%; height: 100%; position: absolute; top: 0; left: 0; }
            #loading { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); 
                       background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.2);
                       z-index: 1000; font-family: Arial, sans-serif; }
            #error { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); 
                    background: #ffebee; color: #c62828; padding: 20px; border-radius: 8px; 
                    box-shadow: 0 2px 8px rgba(0,0,0,0.2); z-index: 1000; font-family: Arial, sans-serif;
                    display: none; max-width: 80%; text-align: center; }
        </style>
    </head>
    <body>
        <div id="loading">Chargement de la carte...</div>
        <div id="error"></div>
        <div id="map"></div>
        <script>
            console.log('Initializing map...');
            var map = null;
            var mapInitialized = false;
            var userMarker = null;
            var activityMarkers = [];
            
            function showError(message) {
                var errorDiv = document.getElementById('error');
                errorDiv.textContent = message;
                errorDiv.style.display = 'block';
                document.getElementById('loading').style.display = 'none';
            }
            
            function hideLoading() {
                document.getElementById('loading').style.display = 'none';
            }
            
            // Fonction pour mettre à jour la carte - DOIT être définie AVANT initMap
            window.updateMap = function(userLat, userLng, activities) {
                if (!map || !mapInitialized) {
                    console.log('Map not initialized yet, waiting...');
                    setTimeout(function() { 
                        if (window.updateMap) {
                            window.updateMap(userLat, userLng, activities); 
                        }
                    }, 500);
                    return;
                }
                
                console.log('Updating map with', activities ? activities.length : 0, 'activities');
                
                // Mettre à jour la position de l'utilisateur
                if (userLat && userLng && userLat !== 0 && userLng !== 0) {
                    console.log('Setting user location:', userLat, userLng);
                    if (userMarker) {
                        userMarker.setLatLng([userLat, userLng]);
                    } else {
                        userMarker = L.marker([userLat, userLng], {
                            icon: L.divIcon({
                                className: 'user-marker',
                                html: '<div style="background-color: #4CAF50; width: 20px; height: 20px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>',
                                iconSize: [20, 20],
                                iconAnchor: [10, 10]
                            })
                        }).addTo(map);
                    }
                    map.setView([userLat, userLng], 13);
                }
                
                // Supprimer les anciens marqueurs
                activityMarkers.forEach(function(marker) {
                    map.removeLayer(marker);
                });
                activityMarkers = [];
                
                // Ajouter les nouveaux marqueurs d'activités
                if (activities && Array.isArray(activities) && activities.length > 0) {
                    console.log('Adding', activities.length, 'activity markers');
                    activities.forEach(function(activity) {
                        if (activity.latitude && activity.longitude) {
                            var sportIcon = getSportIcon(activity.sport);
                            var marker = L.marker([activity.latitude, activity.longitude], {
                                icon: L.divIcon({
                                    className: 'activity-marker',
                                    html: '<div style="background-color: #A855F7; width: 40px; height: 40px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 8px rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; font-size: 20px;">' + sportIcon + '</div>',
                                    iconSize: [40, 40],
                                    iconAnchor: [20, 20]
                                })
                            }).addTo(map);
                            
                            var locationText = activity.location || 'Unknown location';
                            if (locationText.includes(',')) {
                                locationText = locationText.split(',')[0];
                            }
                            
                            marker.bindPopup(
                                '<b>' + (activity.title || 'Activity') + '</b><br/>' +
                                sportIcon + ' ' + (activity.sport || 'Sport') + '<br/>' +
                                '📍 ' + locationText + '<br/>' +
                                '👤 ' + (activity.organizer || 'Unknown')
                            );
                            
                            marker.on('click', function() {
                                if (window.AndroidInterface && activity.id) {
                                    window.AndroidInterface.onMarkerClick(activity.id);
                                }
                            });
                            
                            activityMarkers.push(marker);
                        }
                    });
                } else {
                    console.log('No activities to display');
                }
            }
            
            function getSportIcon(sport) {
                if (!sport) return '🎯';
                var sportLower = sport.toLowerCase();
                if (sportLower.includes('run')) return '🏃';
                if (sportLower.includes('yoga')) return '🧘';
                if (sportLower.includes('volley')) return '🏐';
                if (sportLower.includes('basket')) return '🏀';
                if (sportLower.includes('swim')) return '🏊';
                if (sportLower.includes('cycle')) return '🚴';
                if (sportLower.includes('tennis')) return '🎾';
                if (sportLower.includes('football') || sportLower.includes('soccer')) return '⚽';
                return '🎯';
            }
            
            function initMap() {
                if (mapInitialized) return;
                
                try {
                    if (typeof L === 'undefined') {
                        showError('Erreur: Leaflet n\\'a pas pu être chargé. Vérifiez votre connexion Internet.');
                        return;
                    }
                    
                    console.log('Leaflet loaded, creating map...');
                    var defaultLat = $defaultLat;
                    var defaultLng = $defaultLng;
                    map = L.map('map', {
                        zoomControl: true,
                        attributionControl: true
                    }).setView([defaultLat, defaultLng], 13);
                    
                    console.log('Map created, adding tile layer...');
                    
                    // Ajouter la couche OpenStreetMap (100% gratuit)
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors',
                        maxZoom: 19,
                        subdomains: ['a', 'b', 'c']
                    }).addTo(map);
                    
                    console.log('Tile layer added');
                    mapInitialized = true;
                    hideLoading();
                } catch (error) {
                    console.error('Error initializing map:', error);
                    showError('Erreur lors de l\\'initialisation de la carte: ' + error.message);
                    return;
                }
            }
            
            // Attendre que Leaflet soit chargé, puis initialiser
            function waitForLeaflet() {
                if (typeof L !== 'undefined') {
                    console.log('Leaflet is loaded, initializing map...');
                    initMap();
                    
                    // Les activités seront injectées via evaluateJavascript depuis Kotlin
                    // Initialiser la carte vide pour l'instant
                    console.log('Map initialized, waiting for activities from Android...');
                    // Ne pas appeler updateMap ici car la carte vient d'être initialisée
                    // Les activités seront chargées via evaluateJavascript
                } else {
                    console.log('Waiting for Leaflet to load...');
                    setTimeout(waitForLeaflet, 100);
                }
            }
            
            // Démarrer l'attente de Leaflet
            waitForLeaflet();
            
            // Timeout de sécurité après 10 secondes
            setTimeout(function() {
                if (!mapInitialized) {
                    showError('La carte n\\'a pas pu être chargée. Vérifiez votre connexion Internet.');
                }
            }, 10000);
        </script>
    </body>
    </html>
    """.trimIndent()
}

