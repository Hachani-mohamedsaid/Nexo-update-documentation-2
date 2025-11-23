package com.example.damandroid.presentation.aisuggestions.ui

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.SessionsRecommendation
import com.example.damandroid.domain.model.SuggestedActivity
import com.example.damandroid.presentation.aisuggestions.model.AISuggestionsUiState
import com.example.damandroid.presentation.aisuggestions.viewmodel.AISuggestionsViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.damandroid.location.LocationService
import com.example.damandroid.location.GeocodingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun AISuggestionsRoute(
    viewModel: AISuggestionsViewModel,
    onBack: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    onActivityClick: ((SuggestedActivity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    AISuggestionsScreen(
        state = uiState,
        onBack = onBack,
        onRefresh = onRefresh ?: viewModel::refresh,
        onActivityClick = onActivityClick,
        modifier = modifier
    )
}

@Composable
fun AISuggestionsScreen(
    state: AISuggestionsUiState,
    onBack: (() -> Unit)?,
    onRefresh: () -> Unit,
    onActivityClick: ((SuggestedActivity) -> Unit)?,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(state.error, onRefresh, modifier)
        state.recommendation != null -> LegacySessionsContent(
            recommendation = state.recommendation,
            onBack = onBack,
            onRefresh = onRefresh,
            onActivityClick = onActivityClick,
            modifier = modifier
        )
        else -> ErrorState("No sessions available", onRefresh, modifier)
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) { Text(text = "Retry") }
        }
    }
}

@Composable
private fun LegacySessionsContent(
    recommendation: SessionsRecommendation,
    onBack: (() -> Unit)?,
    onRefresh: () -> Unit,
    onActivityClick: ((SuggestedActivity) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val appTheme = rememberAppThemeColors(LocalThemeController.current.isDarkMode)
    var savedActivities by remember { mutableStateOf(setOf<String>()) }
    var viewMode by remember { mutableStateOf("map") }
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    val geocodingService = remember { GeocodingService() }
    val coroutineScope = rememberCoroutineScope()
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var activitiesWithLocation by remember { mutableStateOf<List<SuggestedActivity>>(emptyList()) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var maxDistanceKm by remember { mutableStateOf(50.0) }

    val toggleSave = { id: String ->
        savedActivities = if (savedActivities.contains(id)) savedActivities - id else savedActivities + id
    }

    // Launcher pour demander les permissions de localisation
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            coroutineScope.launch {
                userLocation = withContext(Dispatchers.IO) {
                    locationService.getCurrentLocation()
                }
            }
        } else {
            showPermissionDialog = true
        }
    }
    
    // Vérifier les permissions au démarrage
    LaunchedEffect(Unit) {
        if (!locationService.hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            userLocation = withContext(Dispatchers.IO) {
                locationService.getCurrentLocation()
            }
        }
    }
    
    // Fonction helper pour extraire les coordonnées depuis la location string
    fun extractCoordinatesFromLocation(location: String): Pair<Double?, Double?> {
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
        return Pair(null, null)
    }
    
    // Récupérer les activités depuis l'API avec latitude/longitude
    LaunchedEffect(Unit) {
        activitiesWithLocation = withContext(Dispatchers.IO) {
            try {
                val response = com.example.damandroid.api.RetrofitClient.activityApiService.getActivities(visibility = "public")
                if (response.isSuccessful) {
                    val apiActivities = response.body() ?: emptyList()
                    
                    // Convertir ActivityResponse en SuggestedActivity avec coordonnées
                    apiActivities.mapNotNull { activityResponse ->
                        var lat = activityResponse.latitude
                        var lng = activityResponse.longitude
                        
                        // Si pas de coordonnées, essayer de géocoder l'adresse
                        if (lat == null || lng == null) {
                            val geocoded = geocodingService.geocode(activityResponse.location)
                            if (geocoded != null) {
                                lat = geocoded.first
                                lng = geocoded.second
                            }
                        }
                        
                        if (lat != null && lng != null) {
                            SuggestedActivity(
                                id = activityResponse.getActivityId(),
                                title = activityResponse.title,
                                sport = activityResponse.sportType,
                                description = activityResponse.description ?: "",
                                date = activityResponse.date,
                                time = activityResponse.time,
                                location = "${activityResponse.location}, $lat, $lng",
                                organizer = activityResponse.getCreator()?.name ?: "Unknown",
                                participantsCount = 0,
                                capacity = activityResponse.participants,
                                isRecommended = false
                            )
                        } else null
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("LegacySessionsContent", "Error fetching activities: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Filtrer par distance quand la localisation de l'utilisateur est disponible
    val filteredActivities = remember(activitiesWithLocation, userLocation, maxDistanceKm) {
        if (userLocation != null && activitiesWithLocation.isNotEmpty()) {
            val (userLat, userLng) = userLocation!!
            activitiesWithLocation.filter { activity ->
                val (activityLat, activityLng) = extractCoordinatesFromLocation(activity.location)
                if (activityLat != null && activityLng != null) {
                    val distance = geocodingService.calculateDistance(
                        userLat, userLng,
                        activityLat, activityLng
                    )
                    distance <= maxDistanceKm
                } else false
            }
        } else {
            activitiesWithLocation
        }
    }
    
    // Utiliser les activités de l'API si disponibles, sinon utiliser celles de la recommandation
    val displayedActivities = remember(filteredActivities, recommendation) {
        when {
            filteredActivities.isNotEmpty() -> filteredActivities
            activitiesWithLocation.isNotEmpty() -> activitiesWithLocation
            recommendation.recommended.isNotEmpty() -> recommendation.recommended
            else -> sampleActivities
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appTheme.backgroundGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Combined header and toggle in a single card container
            CombinedHeaderAndToggle(
                onBack = onBack,
                currentMode = viewMode,
                onModeChange = { viewMode = it },
                theme = appTheme
            )

            if (viewMode == "list") {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { PersonalizedCard(theme = appTheme) }
                    item { WhyThisCardLegacy(theme = appTheme) }
                    items(displayedActivities, key = { it.id }) { activity ->
                        ActivitySuggestionCard(
                            activity = activity,
                            isSaved = savedActivities.contains(activity.id),
                            onSaveClick = { toggleSave(activity.id) },
                            onJoinClick = { onActivityClick?.invoke(activity) },
                            theme = appTheme
                        )
                    }
                }
            } else {
                // Map view wrapped in a container with rounded corners and proper spacing
                // Similar to iOS interface - map in a card with soft corners
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp) // Space below header card
                        .padding(bottom = 100.dp) // Space for bottom navigation
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        RealMapView(
                            activities = displayedActivities,
                            onActivityClick = { onActivityClick?.invoke(it) },
                            theme = appTheme,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                        )
                    }
                }
            }
        }
    }
    
    // Dialog pour demander les permissions
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission de localisation requise") },
            text = { Text("Pour afficher votre position et les activités proches, nous avons besoin de votre permission de localisation.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("Autoriser")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

private fun Box(modifier: Modifier, content: () -> Unit) {}

// region Legacy UI

@Composable
private fun CombinedHeaderAndToggle(
    onBack: (() -> Unit)?,
    currentMode: String,
    onModeChange: (String) -> Unit,
    theme: AppThemeColors
) {
    // Single card/cell container for header and toggle - like iOS interface
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (theme.isDark) theme.subtleSurface else Color(0xFFE8D5F2).copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Closer spacing between header and toggle
        ) {
            // Header section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Sessions", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = theme.primaryText)
                    Text(
                        text = "AI-powered recommendations",
                        fontSize = 14.sp,
                        color = theme.secondaryText,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                IconButton(
                    onClick = { onBack?.invoke() },
                    modifier = Modifier.size(48.dp),
                    enabled = onBack != null
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFEC4899), // Dark pink
                                        Color(0xFFA855F7)  // Violet
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(48f, 48f)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            
            // Toggle section - inside the same card, closer to header
            ModeToggleLegacy(
                currentMode = currentMode,
                onModeChange = onModeChange,
                theme = theme,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ModeToggleLegacy(
    currentMode: String,
    onModeChange: (String) -> Unit,
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    // iOS-like toggle background - white/light gray with subtle border
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ToggleButtonLegacy(
                label = "Map",
                icon = Icons.Default.LocationOn,
                selected = currentMode == "map",
                onClick = { onModeChange("map") },
                theme = theme
            )
            ToggleButtonLegacy(
                label = "List",
                icon = Icons.Default.List,
                selected = currentMode == "list",
                onClick = { onModeChange("list") },
                theme = theme
            )
        
        }
    }
}

@Composable
private fun RowScope.ToggleButtonLegacy(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    theme: AppThemeColors
) {
    // iOS-like toggle button - green when selected, transparent when not
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Color(0xFF4CAF50) else Color.Transparent, // Green when selected
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else Color(0xFF757575), // White when selected, gray when not
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) Color.White else Color(0xFF757575) // White when selected, gray when not
            )
        }
    }
}

@Composable
private fun PersonalizedCard(theme: AppThemeColors) {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFEC4899),
                        Color(0xFFA855F7)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Personalized For You",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
        Text(
                    text = "Based on your activity & preferences",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactChip(text = "Cardio", background = Color.White.copy(alpha = 0.2f))
                    CompactChip(text = "Morning sessions", background = Color.White.copy(alpha = 0.2f))
                    CompactChip(text = "Outdoor", background = Color.White.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
private fun CompactChip(text: String, background: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = background
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun WhyThisCardLegacy(theme: AppThemeColors) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardSurface),
        border = BorderStroke(1.dp, theme.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(theme.accentPurple.copy(alpha = if (theme.isDark) 0.25f else 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = theme.accentPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Why these activities?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = theme.primaryText
                )
            }
            Text(
                text = "We've selected activities matching your skill level, preferred sports, and typical schedule. These are nearby and have availability.",
                fontSize = 12.sp,
                color = theme.mutedText,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ActivitySuggestionCard(
    activity: SuggestedActivity,
    isSaved: Boolean,
    onSaveClick: () -> Unit,
    onJoinClick: () -> Unit,
    theme: AppThemeColors
) {
    val spotsLeft = activity.capacity - activity.participantsCount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onJoinClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.cardSurface),
        border = BorderStroke(1.dp, theme.cardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.align(Alignment.TopEnd),
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFEC4899),
                                    Color(0xFFA855F7)
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(text = "AI Pick", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            Column(modifier = Modifier.padding(end = 70.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = avatarUrlFor(activity.organizer),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(2.dp, theme.cardBorder.copy(alpha = 0.35f), CircleShape)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = activity.organizer, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = theme.primaryText)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = sportIconFor(activity.sport), fontSize = 16.sp)
                            Text(text = activity.sport, fontSize = 13.sp, color = theme.secondaryText)
                        }
                    }
                }

                Text(
                    text = activity.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.primaryText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = theme.mutedText, modifier = Modifier.size(14.dp))
                        Text(text = "${activity.date} • ${activity.time}", fontSize = 13.sp, color = theme.mutedText)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = theme.mutedText, modifier = Modifier.size(14.dp))
                        Text(text = activity.location, fontSize = 13.sp, color = theme.mutedText)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = theme.mutedText, modifier = Modifier.size(14.dp))
                        Text(text = "$spotsLeft of ${activity.capacity} spots remaining", fontSize = 13.sp, color = theme.mutedText)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.accentPurple.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, theme.accentPurple.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (activity.isRecommended) "AI Recommended" else "Popular nearby",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.accentPurple,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onSaveClick, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Save",
                                tint = if (isSaved) Color(0xFFEF4444) else theme.mutedText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Button(
                            onClick = onJoinClick,
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentGreen),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Join",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.iconOnAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealMapView(
    activities: List<SuggestedActivity>,
    onActivityClick: (SuggestedActivity) -> Unit,
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    val geocodingService = remember { GeocodingService() }
    val coroutineScope = rememberCoroutineScope()
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var activitiesWithLocation by remember { mutableStateOf<List<SuggestedActivity>>(emptyList()) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var maxDistanceKm by remember { mutableStateOf(50.0) } // Distance maximale en km
    
    // Launcher pour demander les permissions de localisation
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            // Permissions accordées, récupérer la localisation
            coroutineScope.launch {
                userLocation = withContext(Dispatchers.IO) {
                    locationService.getCurrentLocation()
                }
            }
        } else {
            showPermissionDialog = true
        }
    }
    
    // Vérifier les permissions au démarrage
    LaunchedEffect(Unit) {
        if (!locationService.hasLocationPermission()) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            userLocation = withContext(Dispatchers.IO) {
                locationService.getCurrentLocation()
            }
        }
    }
    
    // Fonction helper pour extraire les coordonnées depuis la location string
    fun extractCoordinatesFromLocation(location: String): Pair<Double?, Double?> {
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
        return Pair(null, null)
    }
    
    // Récupérer les activités depuis l'API avec latitude/longitude
    LaunchedEffect(Unit) {
        activitiesWithLocation = withContext(Dispatchers.IO) {
            try {
                val response = com.example.damandroid.api.RetrofitClient.activityApiService.getActivities(visibility = "public")
                if (response.isSuccessful) {
                    val apiActivities = response.body() ?: emptyList()
                    
                    // Convertir ActivityResponse en SuggestedActivity avec coordonnées
                    apiActivities.mapNotNull { activityResponse ->
                        var lat = activityResponse.latitude
                        var lng = activityResponse.longitude
                        
                        // Si pas de coordonnées, essayer de géocoder l'adresse
                        if (lat == null || lng == null) {
                            val geocoded = geocodingService.geocode(activityResponse.location)
                            if (geocoded != null) {
                                lat = geocoded.first
                                lng = geocoded.second
                            }
                        }
                        
                        if (lat != null && lng != null) {
                            SuggestedActivity(
                                id = activityResponse.getActivityId(),
                                title = activityResponse.title,
                                sport = activityResponse.sportType,
                                description = activityResponse.description ?: "",
                                date = activityResponse.date,
                                time = activityResponse.time,
                                location = "${activityResponse.location}, $lat, $lng",
                                organizer = activityResponse.getCreator()?.name ?: "Unknown",
                                participantsCount = 0,
                                capacity = activityResponse.participants,
                                isRecommended = false
                            )
                        } else null
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("RealMapView", "Error fetching activities: ${e.message}")
                emptyList()
            }
        }
    }
    
    // Filtrer par distance quand la localisation de l'utilisateur est disponible
    val filteredActivities = remember(activitiesWithLocation, userLocation, maxDistanceKm) {
        if (userLocation != null && activitiesWithLocation.isNotEmpty()) {
            val (userLat, userLng) = userLocation!!
            activitiesWithLocation.filter { activity ->
                val (activityLat, activityLng) = extractCoordinatesFromLocation(activity.location)
                if (activityLat != null && activityLng != null) {
                    val distance = geocodingService.calculateDistance(
                        userLat, userLng,
                        activityLat, activityLng
                    )
                    distance <= maxDistanceKm
                } else false
            }
        } else {
            activitiesWithLocation
        }
    }
    
    // Utiliser les activités filtrées de l'API si disponibles, sinon utiliser celles passées en paramètre
    val activitiesToShow = when {
        filteredActivities.isNotEmpty() -> filteredActivities
        activitiesWithLocation.isNotEmpty() -> activitiesWithLocation
        else -> activities
    }
    
    // Dialog pour demander les permissions
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { androidx.compose.material3.Text("Permission de localisation requise") },
            text = { androidx.compose.material3.Text("Pour afficher votre position et les activités proches, nous avons besoin de votre permission de localisation.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    androidx.compose.material3.Text("Autoriser")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    androidx.compose.material3.Text("Annuler")
                }
            }
        )
    }
    
    OSMMapView(
        activities = activitiesToShow,
        userLatitude = userLocation?.first,
        userLongitude = userLocation?.second,
        onActivityClick = onActivityClick,
        modifier = modifier
    )
}

@Composable
private fun MapViewLegacy(
    activities: List<SuggestedActivity>,
    onActivityClick: (SuggestedActivity) -> Unit,
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    val parkColor = Color(0xFFB3F0D0).copy(alpha = 0.35f)
    val buildingColor = Color(0xFFE6D9C5).copy(alpha = 0.45f)

    Box(
        modifier = modifier
            .fillMaxSize()
                        .background(theme.altBackgroundGradient)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF7EFFD),
                            Color(0xFFF2E5FF),
                            Color(0xFFF7EFFD)
                        )
                    )
                )
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            val columns = 6
            val rows = 8
            val colSpacing = size.width / columns
            val rowSpacing = size.height / rows
            val gridColor = Color(0xFFE5D7FF)

            repeat(columns + 1) { index ->
                val x = index * colSpacing
                drawLine(
                    color = gridColor,
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                    strokeWidth = 2f
                )
            }

            repeat(rows + 1) { index ->
                val y = index * rowSpacing
                drawLine(
                    color = gridColor,
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 2f
                )
            }
        }

        listOf(
            Triple(40.dp, 80.dp, 120.dp to 80.dp),
            Triple(200.dp, 200.dp, 100.dp to 60.dp),
            Triple(60.dp, 350.dp, 140.dp to 90.dp)
        ).forEach { (x, y, sizePair) ->
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(width = sizePair.first, height = sizePair.second)
                    .clip(RoundedCornerShape(8.dp))
                    .background(parkColor)
            )
        }

        listOf(
            Triple(160.dp, 120.dp, 80.dp to 50.dp),
            Triple(280.dp, 280.dp, 70.dp to 45.dp),
            Triple(120.dp, 400.dp, 90.dp to 55.dp)
        ).forEach { (x, y, sizePair) ->
            Box(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(width = sizePair.first, height = sizePair.second)
                    .clip(RoundedCornerShape(12.dp))
                    .background(buildingColor)
            )
        }

        activities.take(4).forEachIndexed { index, activity ->
            val position = when (index) {
                0 -> 100.dp to 140.dp
                1 -> 280.dp to 220.dp
                2 -> 80.dp to 380.dp
                else -> 320.dp to 420.dp
            }
            ActivityMarkerLegacy(
                x = position.first,
                y = position.second,
                icon = sportIconFor(activity.sport),
                onClick = { onActivityClick(activity) }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(Icons.Default.Add, Icons.Default.Remove, Icons.Default.Send, Icons.Default.FilterList).forEach { icon ->
                val backgroundColor = if (icon == Icons.Default.FilterList) theme.accentPurple else Color.White
                val tint = if (icon == Icons.Default.FilterList) Color.White else theme.primaryText
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = backgroundColor,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(20.dp).let { if (icon == Icons.Default.Send) it.rotate(45f) else it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityMarkerLegacy(
    x: androidx.compose.ui.unit.Dp,
    y: androidx.compose.ui.unit.Dp,
    icon: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(width = 56.dp, height = 40.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFA855F7),
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = icon, fontSize = 20.sp)
            }
        }

        Surface(
            modifier = Modifier
                .offset(x = 32.dp, y = (-4).dp)
                .size(24.dp),
            shape = CircleShape,
            color = Color(0xFFEC4899),
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Pick", tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}


// endregion

// region sample data mapping

private val sampleActivities = listOf(
    SuggestedActivity(
        id = "ai-1",
        title = "Morning Beach Volleyball Match",
        sport = "Volleyball",
        description = "Friendly match with mixed skill levels at Santa Monica Beach.",
        date = "Today",
        time = "8:00 AM",
        location = "Santa Monica Beach • 1.2 mi",
        organizer = "Emma Wilson",
        participantsCount = 8,
        capacity = 12,
        isRecommended = true
    ),
    SuggestedActivity(
        id = "ai-2",
        title = "Evening Running Group",
        sport = "Running",
        description = "Paced group run with multiple pace leaders for all levels.",
        date = "Today",
        time = "6:30 PM",
        location = "Central Park • 0.8 mi",
        organizer = "Michael Chen",
        participantsCount = 10,
        capacity = 15,
        isRecommended = true
    ),
    SuggestedActivity(
        id = "ai-3",
        title = "Yoga & Meditation Session",
        sport = "Yoga",
        description = "Gentle flow and guided meditation for beginners.",
        date = "Tomorrow",
        time = "7:00 AM",
        location = "Zen Studio • 1.5 mi",
        organizer = "Sarah Johnson",
        participantsCount = 15,
        capacity = 20,
        isRecommended = true
    ),
    SuggestedActivity(
        id = "ai-4",
        title = "Pickup Basketball Game",
        sport = "Basketball",
        description = "Full-court game with rotating squads, bring your A-game.",
        date = "Tomorrow",
        time = "5:00 PM",
        location = "Downtown Court • 2.1 mi",
        organizer = "James Rodriguez",
        participantsCount = 7,
        capacity = 10,
        isRecommended = true
    )
)

private fun avatarUrlFor(name: String): String {
    val seed = name.replace("\\s+".toRegex(), "_")
    return "https://api.dicebear.com/7.x/avataaars/svg?seed=$seed"
}

private fun sportIconFor(sport: String): String = when {
    sport.contains("run", true) -> "🏃"
    sport.contains("yoga", true) -> "🧘"
    sport.contains("volley", true) -> "🏐"
    sport.contains("basket", true) -> "🏀"
    sport.contains("swim", true) -> "🏊"
    sport.contains("cycle", true) -> "🚴"
    sport.contains("tennis", true) -> "🎾"
    else -> "🎯"
}

// endregion

