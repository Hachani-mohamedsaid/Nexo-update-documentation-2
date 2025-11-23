package com.example.damandroid.presentation.aisuggestions.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DriveEta
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.damandroid.domain.model.SuggestedActivity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.util.BoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Composable pour afficher une carte OpenStreetMap native (100% gratuit)
 * Utilise OSMDroid au lieu de WebView pour une meilleure performance
 */
@Composable
fun OSMMapView(
    activities: List<SuggestedActivity>,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    onActivityClick: ((SuggestedActivity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedActivity by remember { mutableStateOf<SuggestedActivity?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    
    // Configurer OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = "DamAndroid/1.0"
    }
    
    // Wrap map in Box to overlay controls
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
            MapView(ctx).apply {
                mapViewRef = this
                setTileSource(TileSourceFactory.MAPNIK) // OpenStreetMap (100% gratuit)
                setMultiTouchControls(true)
                minZoomLevel = 3.0
                maxZoomLevel = 19.0
                
                // Hide built-in zoom controls by finding and hiding zoom button views
                post {
                    // Find and hide zoom control buttons
                    val zoomInButton = findViewWithTag<android.view.View>("zoom_in")
                    val zoomOutButton = findViewWithTag<android.view.View>("zoom_out")
                    zoomInButton?.visibility = android.view.View.GONE
                    zoomOutButton?.visibility = android.view.View.GONE
                    
                    // Also try to find zoom controls by traversing child views
                    for (i in 0 until childCount) {
                        val child = getChildAt(i)
                        if (child is android.widget.ZoomControls || 
                            child.javaClass.simpleName.contains("Zoom", ignoreCase = true)) {
                            child.visibility = android.view.View.GONE
                        }
                    }
                }
                    
                    // Position par défaut (Los Angeles) ou position de l'utilisateur
                    val defaultLat = userLatitude ?: 34.0522
                    val defaultLng = userLongitude ?: -118.2437
                    val startPoint = GeoPoint(defaultLat, defaultLng)
                    
                    controller.setCenter(startPoint)
                    controller.setZoom(13.0)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapViewRef = mapView
            }
        )
        
        // Zoom controls and location button - positioned at bottom right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom In Button
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { mapViewRef?.controller?.zoomIn() },
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color(0xFF424242),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Zoom Out Button
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { mapViewRef?.controller?.zoomOut() },
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color(0xFF424242),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Location Button
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        if (userLatitude != null && userLongitude != null) {
                            mapViewRef?.controller?.animateTo(GeoPoint(userLatitude, userLongitude))
                        }
                    },
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "My Location",
                        tint = Color(0xFF2196F3), // Blue color like iOS
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    
    // Mettre à jour la carte de manière optimisée avec LaunchedEffect
    // Utiliser kotlinx.coroutines.Dispatchers.Default pour les opérations coûteuses
    LaunchedEffect(activities, userLatitude, userLongitude) {
        mapViewRef?.let { mapView ->
            // Utiliser Dispatchers.Default pour les opérations coûteuses
            withContext(Dispatchers.Default) {
                // Préparer les icônes en arrière-plan
                val userIcon = if (userLatitude != null && userLongitude != null) {
                    createUserLocationIcon(context)
                } else null
                
                val activityIcons = activities.associate { activity ->
                    val (lat, lng) = extractCoordinatesFromActivity(activity)
                    if (lat != null && lng != null) {
                        activity to createActivityIcon(context, activity.sport)
                    } else {
                        activity to null
                    }
                }
                
                // Retourner sur le thread principal pour les opérations UI
                withContext(Dispatchers.Main) {
                    // Supprimer uniquement les marqueurs et overlays de localisation, garder la route
                    val overlaysToRemove = mapView.overlays.filter { 
                        it !is Polyline && it !is MyLocationNewOverlay
                    }
                    overlaysToRemove.forEach { mapView.overlays.remove(it) }
                    
                    // Supprimer aussi MyLocationNewOverlay s'il existe déjà
                    val existingLocationOverlay = mapView.overlays.find { it is MyLocationNewOverlay }
                    if (existingLocationOverlay != null) {
                        mapView.overlays.remove(existingLocationOverlay)
                    }
                    
                    val defaultLat = userLatitude ?: 34.0522
                    val defaultLng = userLongitude ?: -118.2437
                    val startPoint = GeoPoint(defaultLat, defaultLng)
                    mapView.controller.setCenter(startPoint)
                    
                    // Ajouter la localisation de l'utilisateur
                    if (userLatitude != null && userLongitude != null && userIcon != null) {
                        val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView)
                        myLocationOverlay.enableMyLocation()
                        mapView.overlays.add(myLocationOverlay)
                        
                        val userMarker = Marker(mapView).apply {
                            position = GeoPoint(userLatitude, userLongitude)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Votre position"
                            icon = userIcon
                        }
                        mapView.overlays.add(userMarker)
                    }
                    
                    // Ajouter les marqueurs pour les activités
                    activities.forEach { activity ->
                        val (lat, lng) = extractCoordinatesFromActivity(activity)
                        val icon = activityIcons[activity]
                        if (lat != null && lng != null && icon != null) {
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(lat, lng)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = activity.title
                                snippet = "${activity.sport} • ${activity.location.split(",").firstOrNull() ?: activity.location}"
                                this.icon = icon
                                
                                setOnMarkerClickListener { _, _ ->
                                    selectedActivity = activity
                                    onActivityClick?.invoke(activity)
                                    true
                                }
                            }
                            mapView.overlays.add(marker)
                        }
                    }
                    
                    mapView.invalidate()
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            // Nettoyer les ressources si nécessaire
        }
    }
    
    // Dialog pour afficher les informations de l'activité
    selectedActivity?.let { activity ->
        ActivityInfoDialog(
            activity = activity,
            userLatitude = userLatitude,
            userLongitude = userLongitude,
            onDismiss = { 
                selectedActivity = null
            }
        )
    }
}

/**
 * Dialog pour afficher les informations d'une activité - Redesigned to match specification
 */
@Composable
private fun ActivityInfoDialog(
    activity: SuggestedActivity,
    userLatitude: Double?,
    userLongitude: Double?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val (activityLat, activityLng) = extractCoordinatesFromActivity(activity)
    var showDirectionsDialog by remember { mutableStateOf(false) }
    
    // Calculate spots left
    val spotsLeft = activity.capacity - activity.participantsCount
    
    // Format date and time
    val formattedDate = formatDateShort(activity.date)
    val formattedTime = formatTimeShort(activity.time)
    
    // Function to open Google Maps
    fun openGoogleMaps() {
        if (activityLat != null && activityLng != null) {
            val gmmIntentUri = Uri.parse("google.navigation:q=$activityLat,$activityLng")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            
            try {
                context.startActivity(mapIntent)
            } catch (e: Exception) {
                // If Google Maps is not installed, open in browser
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$activityLat,$activityLng")
                )
                context.startActivity(webIntent)
            }
            showDirectionsDialog = false
            onDismiss()
        }
    }
    
    // Dialog with backdrop - Floating/Hovering pop-up
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        // Semi-transparent dark overlay backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center // Centered instead of BottomCenter
        ) {
            // Pop-up Card - 95% width, all corners rounded (hovering style)
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 380.dp), // Max width for larger screens
                shape = RoundedCornerShape(32.dp), // All corners rounded
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                // Close Button - Top right
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                // Header Section - Profile & User Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Profile Picture - 64px circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F0))
                            .border(2.dp, Color(0xFFF0F0F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Placeholder - using organizer initial or default avatar
                        Text(
                            text = activity.organizer.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                    }
                    
                    // User Name and Badge
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Name
                            Text(
                                text = activity.organizer,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            
                            // Individual Badge
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFE3F2FD),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Individual",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2196F3)
                                    )
                                }
                            }
                        }
                        
                        // Sport Type
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = getSportEmoji(activity.sport),
                                fontSize = 20.sp
                            )
                            Text(
                                text = activity.sport,
                                fontSize = 16.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
                
                // Activity Title Section
                Text(
                    text = activity.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 24.dp),
                    lineHeight = 33.6.sp
                )
                
                // Activity Details Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date & Time Item
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "$formattedDate • $formattedTime",
                            fontSize = 16.sp,
                            color = Color(0xFF5F5F5F)
                        )
                    }
                    
                    // Spots Available Item
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "$spotsLeft spots left",
                            fontSize = 16.sp,
                            color = Color(0xFF5F5F5F)
                        )
                    }
                    
                    // Skill Level Item
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Intermediate", // Default or extract from activity if available
                            fontSize = 16.sp,
                            color = Color(0xFF5F5F5F)
                        )
                    }
                }
                
                // Action Buttons Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Directions Button
                    OutlinedButton(
                        onClick = {
                            if (activityLat != null && activityLng != null) {
                                showDirectionsDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        border = BorderStroke(2.dp, Color(0xFF2196F3)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Directions",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2196F3)
                        )
                    }
                    
                    // Chat Now Button
                    OutlinedButton(
                        onClick = {
                            // Handle chat action - navigate to chat
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(27.dp),
                        border = BorderStroke(2.dp, Color(0xFF4CAF50)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chat Now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
                
                // Bottom Padding
                Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    
    // Directions Choice Dialog
    if (showDirectionsDialog) {
        DirectionsChoiceDialog(
            activityTitle = activity.title,
            onGoogleMapsClick = { openGoogleMaps() },
            onCancel = { showDirectionsDialog = false }
        )
    }
}

/**
 * Dialog to choose maps app for directions
 */
@Composable
private fun DirectionsChoiceDialog(
    activityTitle: String,
    onGoogleMapsClick: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .widthIn(max = 320.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title
                Text(
                    text = "Choose Maps App",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // Subtitle
                Text(
                    text = "Choose which maps app to use for directions to $activityTitle",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Google Maps Button
                OutlinedButton(
                    onClick = onGoogleMapsClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = "Google Maps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
                
                // Cancel Button
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
    }
}

/**
 * Trace une route sur la carte entre la position de l'utilisateur et la destination
 * NOTE: This function is kept for reference but route drawing is now disabled
 */
private fun drawRouteOnMap(
    mapView: MapView?,
    userLat: Double?,
    userLng: Double?,
    destinationLat: Double,
    destinationLng: Double
): Polyline? {
    if (mapView == null || userLat == null || userLng == null) {
        return null
    }
    
    // Supprimer toutes les anciennes routes (Polylines) pour éviter les doublons
    val existingRoutes = mapView.overlays.filter { it is Polyline }
    existingRoutes.forEach { mapView.overlays.remove(it) }
    
    // Créer une nouvelle polyline simple pour la route (ligne droite)
    val route = Polyline().apply {
        setPoints(
            listOf(
                GeoPoint(userLat, userLng),
                GeoPoint(destinationLat, destinationLng)
            )
        )
        color = android.graphics.Color.parseColor("#4CAF50") // Vert
        width = 10f
        outlinePaint.strokeWidth = 12f
        outlinePaint.color = android.graphics.Color.parseColor("#2E7D32") // Vert foncé pour le contour
    }
    
    // Ajouter la route au début des overlays pour qu'elle soit en dessous des marqueurs
    mapView.overlays.add(0, route)
    
    // Centrer la carte pour montrer les deux points
    val userPoint = GeoPoint(userLat, userLng)
    val destPoint = GeoPoint(destinationLat, destinationLng)
    
    // Calculer les limites pour inclure les deux points
    val minLat = minOf(userLat, destinationLat)
    val maxLat = maxOf(userLat, destinationLat)
    val minLng = minOf(userLng, destinationLng)
    val maxLng = maxOf(userLng, destinationLng)
    
    // Créer une bounding box avec un peu de padding
    val padding = 0.01 // ~1km de padding
    val boundingBox = BoundingBox(
        maxLat + padding,
        maxLng + padding,
        minLat - padding,
        minLng - padding
    )
    
    // Zoomer pour montrer la route avec un peu de padding
    mapView.zoomToBoundingBox(boundingBox, true, 100)
    
    mapView.invalidate()
    
    return route
}

/**
 * Formate une date pour l'affichage
 */
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) {
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Formate une date pour l'affichage court (ex: "Nov 13")
 */
private fun formatDateShort(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) {
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Formate une heure pour l'affichage
 */
private fun formatTime(timeString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val date = inputFormat.parse(timeString)
        if (date != null) {
            outputFormat.format(date)
        } else {
            timeString
        }
    } catch (e: Exception) {
        timeString
    }
}

/**
 * Formate une heure pour l'affichage court (ex: "1:25 AM")
 */
private fun formatTimeShort(timeString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        val date = inputFormat.parse(timeString)
        if (date != null) {
            outputFormat.format(date)
        } else {
            timeString
        }
    } catch (e: Exception) {
        timeString
    }
}

/**
 * Extrait les coordonnées depuis une activité
 */
private fun extractCoordinatesFromActivity(activity: SuggestedActivity): Pair<Double?, Double?> {
    // Si la location contient des coordonnées (format: "Location, lat, lng")
    val parts = activity.location.split(",")
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
    return Pair(null, null)
}

/**
 * Crée une icône pour la position de l'utilisateur
 */
private fun createUserLocationIcon(context: android.content.Context): android.graphics.drawable.Drawable {
    val size = 40
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    // Cercle vert pour la position de l'utilisateur
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#4CAF50")
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    val borderPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    val centerX = size / 2f
    val centerY = size / 2f
    val radius = size / 2f - 4f
    
    canvas.drawCircle(centerX, centerY, radius, paint)
    canvas.drawCircle(centerX, centerY, radius, borderPaint)
    
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Crée une icône pour une activité selon le sport - iOS style with sparkles
 */
private fun createActivityIcon(context: android.content.Context, sport: String): android.graphics.drawable.Drawable {
    val size = 80 // Bigger size (was 50)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val centerX = size / 2f
    val centerY = size / 2f
    val mainRadius = size / 2f - 8f // Main circle radius
    
    // Main light blue circle for activities
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#42A5F5") // Light blue
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    
    // White border
    val borderPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }
    
    // Draw main circle
    canvas.drawCircle(centerX, centerY, mainRadius, paint)
    canvas.drawCircle(centerX, centerY, mainRadius, borderPaint)
    
    // Add sparkles around the circle (iOS style)
    val sparklePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Draw 3-4 small sparkles around the circle
    val sparkleRadius = 3f
    val sparkleDistance = mainRadius + 6f // Distance from center
    
    // Sparkle positions (top, right, bottom-left)
    val sparklePositions = listOf(
        Pair(centerX, centerY - sparkleDistance), // Top
        Pair(centerX + sparkleDistance * 0.7f, centerY - sparkleDistance * 0.7f), // Top-right
        Pair(centerX + sparkleDistance * 0.7f, centerY + sparkleDistance * 0.7f), // Bottom-right
        Pair(centerX - sparkleDistance * 0.5f, centerY + sparkleDistance * 0.9f) // Bottom-left
    )
    
    sparklePositions.forEach { (x, y) ->
        canvas.drawCircle(x, y, sparkleRadius, sparklePaint)
    }
    
    // Add sport emoji in the center - properly centered
    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = size * 0.32f // Adjusted size for better fit
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        typeface = android.graphics.Typeface.DEFAULT
    }
    
    val sportEmoji = getSportEmoji(sport)
    // Better centering calculation for emoji
    val textBounds = android.graphics.Rect()
    textPaint.getTextBounds(sportEmoji, 0, sportEmoji.length, textBounds)
    val textY = centerY - ((textBounds.top + textBounds.bottom) / 2f)
    canvas.drawText(sportEmoji, centerX, textY, textPaint)
    
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Retourne un emoji selon le sport
 */
private fun getSportEmoji(sport: String): String {
    val sportLower = sport.lowercase()
    return when {
        sportLower.contains("run") -> "🏃"
        sportLower.contains("yoga") -> "🧘"
        sportLower.contains("volley") -> "🏐"
        sportLower.contains("basket") -> "🏀"
        sportLower.contains("swim") -> "🏊"
        sportLower.contains("cycle") -> "🚴"
        sportLower.contains("tennis") -> "🎾"
        sportLower.contains("football") || sportLower.contains("soccer") -> "⚽"
        else -> "🎯"
    }
}

