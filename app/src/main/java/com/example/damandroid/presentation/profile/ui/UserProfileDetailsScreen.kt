package com.example.damandroid.presentation.profile.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.*
import com.example.damandroid.presentation.profile.model.ProfileTab
import com.example.damandroid.presentation.profile.model.ProfileUiState
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

@Composable
fun UserProfileDetailsRoute(
    userId: String,
    getUserProfileById: suspend (String) -> UserProfile,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(ProfileTab.ABOUT) }

    LaunchedEffect(userId) {
        isLoading = true
        error = null
        try {
            profile = getUserProfileById(userId)
            isLoading = false
        } catch (e: Exception) {
            error = e.message ?: "Failed to load profile"
            isLoading = false
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Erreur HTTP 404",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "L'endpoint pour récupérer le profil utilisateur n'existe pas encore dans le backend.\n\nEndpoint requis: GET /users/{id}/profile",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Button(onClick = onBack) {
                        Text("Retour")
                    }
                }
            }
        }
        profile != null -> {
            UserProfileDetailsScreen(
                profile = profile!!,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onBack = onBack,
                modifier = modifier
            )
        }
    }
}

@Composable
fun UserProfileDetailsScreen(
    profile: UserProfile,
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val appTheme = rememberAppThemeColors(themeController.isDarkMode)
    val colors = rememberProfileColors(appTheme)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundBrush)
    ) {
        FloatingProfileOrbs(colors)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                UserProfileHeader(
                    colors = colors,
                    onBack = onBack
                )
            }

            item {
                ProfileInfoCard(
                    profile = profile,
                    colors = colors,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                ProfileTabs(
                    profile = profile,
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    colors = colors,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun UserProfileHeader(
    colors: ProfileColors,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = colors.primaryText
            )
        }
        Text(
            text = "Profil",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText,
            letterSpacing = (-0.5).sp,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.width(48.dp)) // Équilibrer avec le bouton retour
    }
}

// Réutiliser les composants existants de ProfileScreen
// ProfileInfoCard, FloatingProfileOrbs, ProfileTabs sont déjà définis dans ProfileScreen.kt

