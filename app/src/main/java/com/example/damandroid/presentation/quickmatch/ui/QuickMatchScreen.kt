package com.example.damandroid.presentation.quickmatch.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.MatchUserProfile
import com.example.damandroid.domain.model.Sport
import com.example.damandroid.presentation.quickmatch.model.QuickMatchUiState
import com.example.damandroid.presentation.quickmatch.viewmodel.QuickMatchViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun QuickMatchRoute(
    viewModel: QuickMatchViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    QuickMatchScreen(
        state = uiState,
        viewModel = viewModel,
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun QuickMatchScreen(
    state: QuickMatchUiState,
    viewModel: QuickMatchViewModel,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(state.error, modifier)
        else -> QuickMatchContent(
            profiles = state.profiles,
            viewModel = viewModel,
            onBack = onBack,
            modifier = modifier
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF8B5CF6))
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, fontWeight = FontWeight.Medium, color = Color(0xFF2D3748))
    }
}

@Composable
private fun QuickMatchContent(
    profiles: List<MatchUserProfile>,
    viewModel: QuickMatchViewModel,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    // Log pour déboguer le nombre de profils affichés
    LaunchedEffect(profiles.size) {
        android.util.Log.d("QuickMatchScreen", "=== UI: profiles.size = ${profiles.size} ===")
        if (profiles.size == 1) {
            android.util.Log.w("QuickMatchScreen", "⚠️ WARNING: Only ONE profile displayed in UI!")
            profiles.forEachIndexed { index, profile ->
                android.util.Log.d("QuickMatchScreen", "Profile[$index]: id=${profile.id}, name=${profile.name}")
            }
        } else if (profiles.size > 1) {
            android.util.Log.d("QuickMatchScreen", "✅ Multiple profiles displayed: ${profiles.size}")
        }
    }
    
    // Ne pas réinitialiser currentIndex quand displayedProfiles change
    // On veut garder l'index même si la liste change (profil retiré)
    var currentIndex by remember { mutableStateOf(0) }
    var showMatch by remember { mutableStateOf(false) }
    var matchedUser by remember { mutableStateOf<MatchUserProfile?>(null) }
    var likesCount by remember { mutableStateOf(0) }

    LaunchedEffect(showMatch) {
        if (showMatch) {
            delay(2500) // Match iOS timing
            showMatch = false
            matchedUser = null
        }
    }

    // Réinitialiser seulement si la liste change complètement (nouveau chargement)
    var previousProfilesSize by remember { mutableStateOf(profiles.size) }
    
    LaunchedEffect(profiles.size) {
        // Si la taille a augmenté (nouveau chargement), réinitialiser
        if (profiles.size > previousProfilesSize) {
            currentIndex = 0
            showMatch = false
            matchedUser = null
            likesCount = 0
            previousProfilesSize = profiles.size
            android.util.Log.d("QuickMatchScreen", "Nouveau chargement: size=${profiles.size}, reset currentIndex à 0")
        } else if (profiles.size < previousProfilesSize) {
            // Si un profil a été retiré, s'assurer que currentIndex reste dans les limites
            if (currentIndex >= profiles.size && profiles.isNotEmpty()) {
                currentIndex = profiles.size - 1
                android.util.Log.d("QuickMatchScreen", "currentIndex ajusté après retrait: ${currentIndex}")
            } else if (profiles.isEmpty()) {
                currentIndex = 0
            }
            previousProfilesSize = profiles.size
        }
    }
    
    // Recharger automatiquement les profils quand il n'en reste qu'un seul ou aucun
    // MAIS limiter à 2 tentatives pour éviter les boucles infinies
    var autoReloadAttempts by remember { mutableStateOf(0) }
    val maxAutoReloadAttempts = 2
    
    LaunchedEffect(profiles.size) {
        if ((profiles.size == 1 || profiles.isEmpty()) && autoReloadAttempts < maxAutoReloadAttempts) {
            // Attendre un peu avant de recharger pour éviter les appels multiples
            delay(1500)
            autoReloadAttempts++
            viewModel.loadProfiles(append = false) // Recharger complètement si la liste est vide
            android.util.Log.d("QuickMatchScreen", "Auto-reload déclenché: ${profiles.size} profil(s) affiché(s), tentative $autoReloadAttempts/$maxAutoReloadAttempts")
        } else if (profiles.size > 1) {
            // Si on a maintenant plus d'un profil, réinitialiser le compteur
            autoReloadAttempts = 0
        } else if (profiles.isEmpty() && autoReloadAttempts >= maxAutoReloadAttempts) {
            android.util.Log.w("QuickMatchScreen", "⚠️ Arrêt du rechargement automatique après $maxAutoReloadAttempts tentatives. Backend ne retourne que des profils déjà likés.")
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                // iOS-matching gradient
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8ECF4), // Very light pink
                        Color(0xFFF3E8F0), // Soft lavender
                        Color(0xFFEFE4ED)  // Slightly deeper lavender
                    )
                )
            )
    ) {
        // Background orbs matching iOS
        BackgroundOrbs()

        // S'assurer que currentIndex est toujours valide pour éviter les crashes
        val safeIndex = if (profiles.isNotEmpty()) {
            currentIndex.coerceIn(0, profiles.size - 1)
        } else {
            0
        }
        val currentProfile = profiles.getOrNull(safeIndex)
        
        // Log pour déboguer
        LaunchedEffect(currentIndex, profiles.size, currentProfile?.id) {
            android.util.Log.d("QuickMatchScreen", "=== CURRENT PROFILE DEBUG ===")
            android.util.Log.d("QuickMatchScreen", "currentIndex: $currentIndex")
            android.util.Log.d("QuickMatchScreen", "safeIndex: $safeIndex")
            android.util.Log.d("QuickMatchScreen", "profiles.size: ${profiles.size}")
            android.util.Log.d("QuickMatchScreen", "currentProfile: ${currentProfile?.name} (${currentProfile?.id})")
            profiles.forEachIndexed { idx, profile ->
                android.util.Log.d("QuickMatchScreen", "  Profile[$idx]: ${profile.name} (${profile.id})")
            }
        }

        when {
            profiles.isEmpty() || currentIndex >= profiles.size ->
                AllCaughtUpScreen(
                    onBack = { onBack?.invoke() },
                    onReload = { 
                        viewModel.loadProfiles()
                        currentIndex = 0
                    }
                )

            currentProfile != null -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    MatchHeader(onBack = { onBack?.invoke() })

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Likes counter positioned at top-right, outside card
                        LikesCounter(
                            count = likesCount,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 28.dp)
                                .zIndex(1001f) // Above card
                        )

                        // Card container - centered vertically
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Preview stack
                            val previewProfiles = profiles.drop(safeIndex + 1).take(2)
                            previewProfiles.forEachIndexed { index, _ ->
                                NextCardPreview(
                                    index = index,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Main card (without likes counter inside)
                            // CRITIQUE: Utiliser key() pour forcer la recréation du composable quand le profil change
                            // Cela garantit que le callback utilise toujours le bon profil
                            if (currentProfile != null) {
                                key(currentProfile.id) {
                                    ProfileCard(
                                        profile = currentProfile,
                                        showLikesCounter = false, // Don't show inside card anymore
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .zIndex(1000f)
                                    ) { direction ->
                                        // Utiliser directement currentProfile.id qui est toujours à jour grâce à key()
                                        val profileId = currentProfile.id
                                        val profileName = currentProfile.name
                                        
                                        android.util.Log.d("QuickMatchScreen", "=== SWIPE ACTION ===")
                                        android.util.Log.d("QuickMatchScreen", "Profile ID: $profileId, Name: $profileName")
                                        android.util.Log.d("QuickMatchScreen", "Current Index: $currentIndex, List Size: ${profiles.size}")
                                        
                                        when (direction) {
                                            SwipeDirection.LEFT -> {
                                                android.util.Log.d("QuickMatchScreen", "PASS action for profile: $profileId ($profileName)")
                                                // Appeler le ViewModel qui retirera le profil de la liste
                                                viewModel.passProfile(profileId)
                                                // Incrémenter l'index pour passer au profil suivant
                                                currentIndex++
                                            }
                                            SwipeDirection.RIGHT -> {
                                                android.util.Log.d("QuickMatchScreen", "LIKE action for profile: $profileId ($profileName)")
                                                // Incrémenter le compteur de likes
                                                likesCount++
                                                // Appeler le ViewModel qui retirera le profil de la liste
                                                viewModel.likeProfile(
                                                    profileId = profileId,
                                                    onMatch = { matchedProfile ->
                                                        matchedUser = matchedProfile
                                                        showMatch = true
                                                    }
                                                )
                                                // Incrémenter l'index pour passer au profil suivant
                                                currentIndex++
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showMatch && matchedUser != null) {
            MatchModal(user = matchedUser!!, modifier = Modifier.zIndex(2000f))
        }
    }
}

@Composable
private fun BackgroundOrbs() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Orb 1 - Purple/lavender (top-left area)
        Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = (-180).dp)
                .size(130.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE9D5FF).copy(alpha = 0.4f),
                            Color(0xFFDDD6FE).copy(alpha = 0.3f)
                        )
                    )
                )
                .blur(50.dp)
        )

        // Orb 2 - Pink (bottom-right area)
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = 220.dp)
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFCE7F3).copy(alpha = 0.5f),
                            Color(0xFFFBCFE8).copy(alpha = 0.3f)
                        )
                    )
                )
                .blur(60.dp)
        )

        // Orb 3 - Blue (center area)
        Box(
            modifier = Modifier
                .offset(x = 0.dp, y = 140.dp)
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0E7FF).copy(alpha = 0.4f),
                            Color(0xFFC7D2FE).copy(alpha = 0.3f)
                        )
                    )
                )
                .blur(40.dp)
        )

        // Orb 4 - Yellow (top-right area)
        Box(
            modifier = Modifier
                .offset(x = 120.dp, y = (-80).dp)
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFEF3C7).copy(alpha = 0.4f),
                            Color(0xFFFDE68A).copy(alpha = 0.3f)
                        )
                    )
                )
                .blur(30.dp)
        )
    }
}

private enum class SwipeDirection { LEFT, RIGHT }

@Composable
private fun MatchHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF2D3748),
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Text(
            text = "Quick Match",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8B5CF6), // iOS purple
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun LikesCounter(
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Glow effect
        Box(
            modifier = Modifier
                .size(width = 70.dp, height = 40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFEC4899).copy(alpha = 0.45f),
                            Color(0xFFA855F7).copy(alpha = 0.45f)
                        )
                    )
                )
                .blur(10.dp)
        )

        // Main badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            modifier = Modifier
                .size(width = 64.dp, height = 40.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFEC4899), Color(0xFFA855F7))
                    ),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
                Text(
                    text = "$count",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun NextCardPreview(
    index: Int,
    modifier: Modifier = Modifier
) {
    val scale = 1f - (index + 1) * 0.05f
    val offsetY = -(index + 1) * 10f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .offset(y = offsetY.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Transparent)
            .border(0.dp, Color.Transparent, RoundedCornerShape(24.dp))
    )
}

@Composable
private fun ProfileCard(
    profile: MatchUserProfile,
    showLikesCounter: Boolean = true, // Control whether to show counter inside card
    modifier: Modifier = Modifier,
    onSwipe: (SwipeDirection) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val displayedSports = remember(profile.sports) {
        if (profile.sports.isNotEmpty()) profile.sports.take(6) else DEFAULT_SPORTS.take(6)
    }
    val displayedInterests = remember(profile.interests) {
        if (profile.interests.isNotEmpty()) profile.interests else DEFAULT_INTERESTS
    }

    val rotation = offsetX / 20f
    val opacity = when {
        offsetX < -100 -> 0f
        offsetX > 100 -> 0f
        else -> 1f
    }

    val likeOpacity by animateFloatAsState(
        targetValue = if (offsetX > 30) (offsetX / 100f).coerceIn(0f, 1f) else 0f,
        label = "like-opacity"
    )
    val nopeOpacity by animateFloatAsState(
        targetValue = if (offsetX < -30) ((-offsetX) / 100f).coerceIn(0f, 1f) else 0f,
        label = "nope-opacity"
    )

    Box(
        modifier = modifier
            .offset(x = offsetX.dp)
            .rotate(rotation)
            .alpha(opacity)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        when {
                            abs(offsetX) > 100 -> {
                                onSwipe(if (offsetX > 0) SwipeDirection.RIGHT else SwipeDirection.LEFT)
                                offsetX = 0f
                            }
                            else -> offsetX = 0f
                        }
                    },
                    onDrag = { change, dragAmount ->
                        offsetX += dragAmount.x
                        change.consume()
                    }
                )
            }
    ) {
        // Glow background matching iOS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = (-4).dp, y = (-4).dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE9D5FF).copy(alpha = 0.12f),
                            Color(0xFFFCE7F3).copy(alpha = 0.10f),
                            Color(0xFFE0E7FF).copy(alpha = 0.12f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .blur(12.dp)
        )

        // Main card - now wraps content dynamically
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFFE5E7EB)),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Cover Image Section - 210dp to match iOS better
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    AsyncImage(
                        model = profile.coverImageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlays matching iOS
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.6f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Name and location - 10dp padding like iOS
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "${profile.name}, ${profile.age}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "${profile.location} • ${profile.distance}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Swipe indicators
                    if (offsetX > 30) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 12.dp, top = 12.dp)
                                .alpha(likeOpacity)
                                .rotate(-20f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF86EFAC))
                                .border(2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "LIKE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (offsetX < -30) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 12.dp, top = 12.dp)
                                .alpha(nopeOpacity)
                                .rotate(20f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF87171))
                                .border(2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "NOPE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Content Section - wraps content with no height constraints
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 12.dp), // Bottom padding for the card
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stats Row - 8dp from top
                    Spacer(modifier = Modifier.height(0.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatBox(
                            title = "Rating",
                            value = String.format("%.1f", profile.rating),
                            icon = Icons.Default.Star,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "Activities",
                            value = "${profile.activitiesJoined}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Bio
                    if (profile.bio.isNotBlank()) {
                        Text(
                            text = profile.bio,
                            fontSize = 13.sp,
                            color = Color(0xFF2D3748),
                            lineHeight = 15.sp
                        )
                    }

                    // Favorite Sports
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Favorite Sports",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3748)
                        )
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            displayedSports.chunked(3).forEach { rowSports ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowSports.forEach { sport ->
                                        SportItem(sport = sport, modifier = Modifier.weight(1f))
                                    }
                                    repeat(3 - rowSports.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // Interests
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Interests",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3748)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            displayedInterests.forEach { interest ->
                                InterestTag(interest = interest)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp) // Slightly taller for better balance
                .offset(y = 2.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.05f))
                .blur(4.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = BorderStroke(2.dp, Color(0xFFE5E7EB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                if (icon != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Text(
                            text = value,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        )
                    }
                } else {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
                }
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = Color(0xFF718096)
                )
            }
        }
    }
}

@Composable
private fun SportItem(
    sport: Sport,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Shadow
        Box(
            modifier = Modifier
                .size(58.dp) // Slightly smaller to match iOS
                .offset(y = 2.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.05f))
                .blur(4.dp)
        )

        Surface(
            modifier = Modifier.size(58.dp),
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(2.dp, Color(0xFFE5E7EB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = sport.icon,
                    fontSize = 18.sp
                )
                Text(
                    text = sport.name,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3748),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun InterestTag(interest: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(2.dp, Color(0xFFE5E7EB))
    ) {
        Text(
            text = interest,
            fontSize = 10.sp,
            color = Color(0xFF2D3748),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun AllCaughtUpScreen(onBack: () -> Unit, onReload: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon matching iOS
        Box(
            modifier = Modifier
                .size(96.dp)
                .padding(bottom = 16.dp)
        ) {
            // Glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFE9D5FF).copy(alpha = 0.4f),
                                Color(0xFFFCE7F3).copy(alpha = 0.4f)
                            )
                        )
                    )
                    .blur(20.dp)
            )

            // Main icon box
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(2.dp, Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Top gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                            )
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFFA855F7)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "All caught up!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3748)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You've seen all available profiles. Check back later for more sport buddies!",
            fontSize = 15.sp,
            color = Color(0xFF718096),
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Reload button if onReload is provided
        onReload?.let { reload ->
            Box {
                // Glow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF8B5CF6).copy(alpha = 0.3f),
                                    Color(0xFFEC4899).copy(alpha = 0.3f)
                                )
                            )
                        )
                        .blur(12.dp)
                )

                Button(
                    onClick = reload,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6)
                    )
                ) {
                    Text(
                        text = "Voir Plus de Profils",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Back button matching iOS
        Box {
            // Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFE9D5FF).copy(alpha = 0.3f),
                                Color(0xFFFCE7F3).copy(alpha = 0.3f),
                                Color(0xFFE0E7FF).copy(alpha = 0.3f)
                            )
                        )
                    )
                    .blur(12.dp)
            )

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(2.dp, Color(0xFFE5E7EB))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to Home",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3748)
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchModal(user: MatchUserProfile, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "match-modal-scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFEC4899), Color(0xFFA855F7))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .scale(scale)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "It's a Match!",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "You and ${user.name} both like each other",
                fontSize = 17.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                shape = CircleShape,
                modifier = Modifier.size(96.dp),
                border = BorderStroke(4.dp, Color.White)
            ) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Starting a conversation...",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

private val DEFAULT_SPORTS = listOf(
    Sport("Running", "🏃", "All levels"),
    Sport("Swimming", "🏊", "All levels"),
    Sport("Hiking", "🥾", "All levels"),
    Sport("Cycling", "🚴", "All levels"),
    Sport("Yoga", "🧘", "All levels"),
    Sport("Tennis", "🎾", "All levels")
)

private val DEFAULT_INTERESTS = listOf(
    "Running",
    "Swimming",
    "Hiking",
    "Cycling",
    "Yoga",
    "Pilates"
)