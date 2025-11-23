package com.example.damandroid.presentation.ai.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damandroid.domain.model.AICoachOverview
import com.example.damandroid.domain.model.Challenge
import com.example.damandroid.domain.model.Suggestion
import com.example.damandroid.domain.model.WeeklyStats
import com.example.damandroid.domain.model.WorkoutTip
import com.example.damandroid.presentation.ai.model.AICoachTab
import com.example.damandroid.presentation.ai.model.AICoachUiState
import com.example.damandroid.presentation.ai.viewmodel.AICoachViewModel
import com.example.damandroid.ui.theme.LocalThemeController
import kotlin.math.max

@Composable
fun AICoachRoute(
    viewModel: AICoachViewModel,
    onBack: () -> Unit,
    onStartChallenge: (() -> Unit)? = null,
    onFindPartner: (() -> Unit)? = null,
    onSuggestionJoin: ((Suggestion) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.appcompat.app.AppCompatActivity
    
    AICoachScreen(
        state = uiState,
        onBack = onBack,
        onTabSelected = viewModel::onTabSelected,
        onRefresh = viewModel::refresh,
        onStartChallenge = onStartChallenge,
        onFindPartner = onFindPartner,
        onSuggestionJoin = onSuggestionJoin,
        onRequestGoogleFitSync = {
            activity?.let {
                viewModel.requestFitnessSync(it)
            }
        },
        onCheckPermissions = {
            viewModel.checkPermissionsAndRefresh()
        },
        modifier = modifier
    )
}

@Composable
fun AICoachScreen(
    state: AICoachUiState,
    onBack: () -> Unit,
    onTabSelected: (AICoachTab) -> Unit,
    onRefresh: () -> Unit,
    onStartChallenge: (() -> Unit)? = null,
    onFindPartner: (() -> Unit)? = null,
    onSuggestionJoin: ((Suggestion) -> Unit)? = null,
    onRequestGoogleFitSync: (() -> Unit)? = null,
    onCheckPermissions: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    
    when {
        state.isLoading -> LoadingState(modifier)
        state.needsGoogleFitSync -> GoogleFitSyncRequiredState(
            message = state.error ?: "Veuillez synchroniser Health Connect",
            onRequestSync = onRequestGoogleFitSync ?: {},
            onBack = onBack,
            onCheckPermissions = onCheckPermissions,
            modifier = modifier
        )
        state.error != null -> ErrorState(state.error, onRefresh, modifier)
        else -> {
            val overview = state.overview ?: sampleAICoachOverview
            ContentState(
                overview = overview,
                selectedTab = state.selectedTab,
                onBack = onBack,
                onTabSelected = onTabSelected,
                onStartChallenge = onStartChallenge,
                onFindPartner = onFindPartner,
                onSuggestionJoin = onSuggestionJoin,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
private fun GoogleFitSyncRequiredState(
    message: String,
    onRequestSync: () -> Unit,
    onBack: () -> Unit,
    onCheckPermissions: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF4285F4)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Synchronisation Fitness requise",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRequestSync,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ouvrir Health Connect")
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Bouton pour vérifier les permissions après avoir accordé dans Health Connect
            if (onCheckPermissions != null) {
                OutlinedButton(
                    onClick = onCheckPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("J'ai accordé les permissions - Vérifier")
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retour")
            }
        }
    }
}

@Composable
private fun ContentState(
    overview: AICoachOverview,
    selectedTab: AICoachTab,
    onBack: () -> Unit,
    onTabSelected: (AICoachTab) -> Unit,
    onStartChallenge: (() -> Unit)?,
    onFindPartner: (() -> Unit)?,
    onSuggestionJoin: ((Suggestion) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val palette = rememberCoachPalette(themeController.isDarkMode)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        FloatingCoachOrbs(palette)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CoachHeader(onBack = onBack, palette = palette)
                MotivationCard(palette = palette)
                TabNavigation(selectedTab, onTabSelected, palette)
            }

            when (selectedTab) {
                AICoachTab.OVERVIEW -> {
                    item { WeeklyStatsCard(overview.weeklyStats, palette) }
                    item { WeatherCard(palette) }
                    item { SectionTitle("Active Challenges", palette) }
                    items(overview.challenges, key = { it.id }) { challenge ->
                        ChallengeCard(challenge = challenge, palette = palette)
                    }
                    item {
                        QuickActionsRow(
                            onStartChallenge = onStartChallenge,
                            onFindPartner = onFindPartner,
                            palette = palette
                        )
                    }
                }
                AICoachTab.SUGGESTIONS -> {
                    item { PersonalizedBanner(palette) }
                    items(overview.suggestions, key = { it.id }) { suggestion ->
                        SuggestionCard(
                            suggestion = suggestion,
                            palette = palette,
                            onJoinClick = { onSuggestionJoin?.invoke(suggestion) },
                            enabled = onSuggestionJoin != null
                        )
                    }
                }
                AICoachTab.TIPS -> {
                    item { VideoLibraryCard(palette) }
                    items(overview.workoutTips, key = { it.id }) { tip ->
                        TipCard(tip = tip, palette = palette)
                    }
                }
            }
        }
    }
}

// region Header & Tabs

@Composable
private fun CoachHeader(onBack: () -> Unit, palette: CoachPalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = palette.glassSurface),
        border = BorderStroke(1.dp, palette.glassBorder),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.secondaryText,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = palette.accentPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Coach",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryText,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = palette.secondaryText
            )
        }
    }
}

@Composable
private fun MotivationCard(palette: CoachPalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(palette.accentPurple, palette.accentPink)
                )
            )
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\"Progress starts with small steps\"",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Keep going! You're doing great! 💪",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TabNavigation(
    selectedTab: AICoachTab,
    onTabSelected: (AICoachTab) -> Unit,
    palette: CoachPalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.tabBackground),
        border = BorderStroke(1.dp, palette.tabBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            AICoachTab.values().forEach { tab ->
                TabButton(
                    text = tab.toLabel(),
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    palette: CoachPalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) palette.accentPurple else palette.secondaryText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(if (selected) 1f else 0f)
                .height(2.dp)
                .background(
                    if (selected) palette.accentPurple else Color.Transparent,
                    RoundedCornerShape(1.dp)
                )
        )
    }
}

private fun AICoachTab.toLabel(): String = when (this) {
    AICoachTab.OVERVIEW -> "Overview"
    AICoachTab.SUGGESTIONS -> "For You"
    AICoachTab.TIPS -> "Tips"
}

// endregion

// region Overview Content

@Composable
private fun WeeklyStatsCard(stats: WeeklyStats, palette: CoachPalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.dp, palette.cardBorder),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This Week",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.primaryText
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = palette.accentTeal
                ) {
                    Text(
                        text = "${stats.streak} day streak 🔥",
                        fontSize = 11.sp,
                        color = palette.iconOnAccent,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = stats.workouts.toString(), label = "Workouts", color = palette.accentPurple)
                StatItem(value = stats.calories.toString(), label = "Calories", color = palette.accentOrange)
                StatItem(value = stats.minutes.toString(), label = "Minutes", color = palette.accentBlue)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val goal = max(stats.goal, 1)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Weekly goal", fontSize = 12.sp, color = palette.mutedText)
                    Text(
                        text = "${stats.workouts}/$goal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.primaryText
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { stats.workouts.toFloat() / goal.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = palette.accentPurple,
                    trackColor = palette.progressTrack
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = color.copy(alpha = 0.8f))
    }
}

@Composable
private fun WeatherCard(palette: CoachPalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.weatherCardBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = palette.weatherIconBackground
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = "☀️", fontSize = 24.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Perfect weather today!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.primaryText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "72°F, sunny — ideal for outdoor training",
                    fontSize = 12.sp,
                    color = palette.secondaryText
                )
            }
        }
    }
}

@Composable
private fun ChallengeCard(challenge: Challenge, palette: CoachPalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.dp, palette.cardBorder),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(listOf(palette.accentPurple, palette.accentPink)),
                                RoundedCornerShape(12.dp)
                            )
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = challenge.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.primaryText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = challenge.description, fontSize = 12.sp, color = palette.secondaryText)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Progress", fontSize = 12.sp, color = palette.secondaryText)
                Text(
                    text = "${challenge.progress}/${challenge.total}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.primaryText
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { challenge.progress.toFloat() / max(challenge.total, 1).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = palette.accentPurple,
                trackColor = palette.progressTrack
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Reward: ${challenge.reward}", fontSize = 11.sp, color = palette.mutedText)
        }
    }
}

@Composable
private fun SectionTitle(text: String, palette: CoachPalette) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = palette.primaryText,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    enabled: Boolean,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (enabled) gradient else Brush.linearGradient(listOf(Color.LightGray, Color.LightGray)),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(text = text, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onStartChallenge: (() -> Unit)?,
    onFindPartner: (() -> Unit)?,
    palette: CoachPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Default.EmojiEvents,
            text = "Start Challenge",
            enabled = onStartChallenge != null,
            gradient = Brush.linearGradient(listOf(palette.accentPurple, palette.accentPink)),
            onClick = { onStartChallenge?.invoke() },
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Default.Group,
            text = "Find Partner",
            enabled = onFindPartner != null,
            gradient = Brush.linearGradient(listOf(palette.accentTeal, palette.accentGreen)),
            onClick = { onFindPartner?.invoke() },
            modifier = Modifier.weight(1f)
        )
    }
}

// endregion

// region Suggestions & Tips

@Composable
private fun PersonalizedBanner(palette: CoachPalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(palette.accentPurple, palette.accentPink)))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text(text = "Personalized for you", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Based on your activity history and preferences", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: Suggestion,
    palette: CoachPalette,
    onJoinClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.dp, palette.cardBorder),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = palette.accentTeal.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = suggestion.icon, fontSize = 24.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = suggestion.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
                        Surface(shape = RoundedCornerShape(8.dp), color = Color.Transparent) {
                            Box(
                                modifier = Modifier
                                    .background(Brush.linearGradient(listOf(palette.accentPurple, palette.accentPink)), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "${suggestion.matchScore}% match", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Text(text = suggestion.description, fontSize = 12.sp, color = palette.secondaryText)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = palette.mutedText, modifier = Modifier.size(14.dp))
                            Text(text = suggestion.time, fontSize = 12.sp, color = palette.mutedText)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = palette.mutedText, modifier = Modifier.size(14.dp))
                            Text(text = "${suggestion.participants} interested", fontSize = 12.sp, color = palette.mutedText)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onJoinClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (enabled) palette.accentTeal else palette.mutedText.copy(alpha = 0.3f)),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (enabled) "Join Now" else "Unavailable", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun VideoLibraryCard(palette: CoachPalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.videoCardBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = palette.accentOrange, modifier = Modifier.size(18.dp))
                Text(text = "Workout Video Library", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
            }
            Text(text = "Watch expert-led tutorials and form guides", fontSize = 12.sp, color = palette.secondaryText)
            OutlinedButton(
                onClick = { /* TODO: navigate to videos */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.primaryText),
                border = BorderStroke(1.dp, palette.cardBorder)
            ) {
                Text(text = "Browse Videos", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TipCard(tip: WorkoutTip, palette: CoachPalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
        border = BorderStroke(1.dp, palette.cardBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = palette.tipIconBackground
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = tip.icon, fontSize = 22.sp)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = tip.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = palette.glassSurface.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, palette.glassBorder)
                    ) {
                        Text(text = tip.category, fontSize = 10.sp, color = palette.primaryText, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Text(text = tip.description, fontSize = 12.sp, color = palette.secondaryText)
            }
        }
    }
}

// endregion

private val sampleAICoachOverview = AICoachOverview(
    weeklyStats = WeeklyStats(
        workouts = 3,
        goal = 5,
        calories = 1200,
        minutes = 180,
        streak = 7
    ),
    suggestions = listOf(
        Suggestion(
            id = "suggestion-1",
            title = "Try a morning swim",
            description = "4 swimmers nearby are free tomorrow 7AM",
            icon = "🏊",
            time = "Tomorrow 7AM",
            participants = 4,
            matchScore = 95
        ),
        Suggestion(
            id = "suggestion-2",
            title = "Join evening yoga session",
            description = "Perfect for recovery after your runs",
            icon = "🧘",
            time = "Today 6PM",
            participants = 8,
            matchScore = 88
        ),
        Suggestion(
            id = "suggestion-3",
            title = "Weekend cycling group",
            description = "Explore new routes with local cyclists",
            icon = "🚴",
            time = "Saturday 8AM",
            participants = 12,
            matchScore = 82
        )
    ),
    workoutTips = listOf(
        WorkoutTip(
            id = "tip-1",
            title = "Warm-up is essential",
            description = "Spend 5-10 minutes warming up to prevent injuries and improve performance.",
            icon = "🔥",
            category = "Basics"
        ),
        WorkoutTip(
            id = "tip-2",
            title = "Stay hydrated",
            description = "Drink water before, during, and after your workout for optimal performance.",
            icon = "💧",
            category = "Health"
        ),
        WorkoutTip(
            id = "tip-3",
            title = "Progressive overload",
            description = "Gradually increase intensity to continue seeing improvements.",
            icon = "📈",
            category = "Training"
        )
    ),
    challenges = listOf(
        Challenge(
            id = "challenge-1",
            title = "30-Day Running Streak",
            description = "Run at least 1 mile every day for 30 days",
            progress = 7,
            total = 30,
            reward = "🏆 Marathon Badge"
        ),
        Challenge(
            id = "challenge-2",
            title = "Weekly Variety Challenge",
            description = "Try 3 different sports this week",
            progress = 1,
            total = 3,
            reward = "⭐ Explorer Badge"
        )
    )
)

// region Palette & Background

data class CoachPalette(
    val background: Brush,
    val glassSurface: Color,
    val glassBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val iconOnAccent: Color,
    val accentPurple: Color,
    val accentPink: Color,
    val accentTeal: Color,
    val accentGreen: Color,
    val accentOrange: Color,
    val accentBlue: Color,
    val progressTrack: Color,
    val cardSurface: Color,
    val cardBorder: Color,
    val tabBackground: Color,
    val tabBorder: Color,
    val weatherCardBackground: Color,
    val weatherIconBackground: Color,
    val videoCardBackground: Color,
    val tipIconBackground: Color
)

@Composable
private fun rememberCoachPalette(isDarkMode: Boolean): CoachPalette = remember(isDarkMode) {
    if (!isDarkMode) {
        CoachPalette(
            background = Brush.linearGradient(
                listOf(
                    Color(0xFFE8D5F2),
                    Color(0xFFFFE4F1),
                    Color(0xFFE5E5F0)
                )
            ),
            glassSurface = Color.White.copy(alpha = 0.4f),
            glassBorder = Color.White.copy(alpha = 0.6f),
            primaryText = Color(0xFF1A202C),
            secondaryText = Color(0xFF4B5563),
            mutedText = Color(0xFF6B7280),
            iconOnAccent = Color.White,
            accentPurple = Color(0xFFA855F7),
            accentPink = Color(0xFFEC4899),
            accentTeal = Color(0xFF22D3EE),
            accentGreen = Color(0xFF86EFAC),
            accentOrange = Color(0xFFF97316),
            accentBlue = Color(0xFF2563EB),
            progressTrack = Color(0xFFE5E7EB),
            cardSurface = Color.White.copy(alpha = 0.9f),
            cardBorder = Color.White.copy(alpha = 0.6f),
            tabBackground = Color.White.copy(alpha = 0.4f),
            tabBorder = Color.White.copy(alpha = 0.6f),
            weatherCardBackground = Color(0xFFDBEAFE).copy(alpha = 0.8f),
            weatherIconBackground = Color.White.copy(alpha = 0.6f),
            videoCardBackground = Color(0xFFFFF7ED).copy(alpha = 0.8f),
            tipIconBackground = Color(0xFFDBEAFE)
        )
    } else {
        CoachPalette(
            background = Brush.linearGradient(
                listOf(
                    Color(0xFF0F172A),
                    Color(0xFF1E1B4B),
                    Color(0xFF111827)
                )
            ),
            glassSurface = Color(0xFF1E293B).copy(alpha = 0.75f),
            glassBorder = Color(0xFF334155).copy(alpha = 0.7f),
            primaryText = Color(0xFFF8FAFC),
            secondaryText = Color(0xFFE2E8F0),
            mutedText = Color(0xFFCBD5F5),
            iconOnAccent = Color.White,
            accentPurple = Color(0xFF8B5CF6),
            accentPink = Color(0xFFF472B6),
            accentTeal = Color(0xFF38BDF8),
            accentGreen = Color(0xFF34D399),
            accentOrange = Color(0xFFF59E0B),
            accentBlue = Color(0xFF60A5FA),
            progressTrack = Color(0xFF1E293B),
            cardSurface = Color(0xFF111827).copy(alpha = 0.85f),
            cardBorder = Color(0xFF334155).copy(alpha = 0.6f),
            tabBackground = Color(0xFF1E293B).copy(alpha = 0.75f),
            tabBorder = Color(0xFF475569).copy(alpha = 0.6f),
            weatherCardBackground = Color(0xFF1E3A8A).copy(alpha = 0.6f),
            weatherIconBackground = Color(0xFF1E293B),
            videoCardBackground = Color(0xFF0F172A).copy(alpha = 0.85f),
            tipIconBackground = Color(0xFF312E81)
        )
    }
}

@Composable
private fun FloatingCoachOrbs(palette: CoachPalette) {
    Box(modifier = Modifier.fillMaxSize()) {
        val transition1 = rememberInfiniteTransition(label = "coach-orb1")
        val alpha1 by transition1.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "coach-orb1-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = (-5).dp, y = (-10).dp)
                .size(280.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(palette.accentPurple.copy(alpha = alpha1), Color.Transparent)
                    )
                )
                .blur(48.dp)
                .align(Alignment.TopStart)
        )

        val transition2 = rememberInfiniteTransition(label = "coach-orb2")
        val alpha2 by transition2.animateFloat(
            initialValue = 0.32f,
            targetValue = 0.16f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3400, delayMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "coach-orb2-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = (-12).dp, y = (-20).dp)
                .size(360.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(palette.accentBlue.copy(alpha = alpha2), Color.Transparent)
                    )
                )
                .blur(56.dp)
                .align(Alignment.BottomEnd)
        )

        val transition3 = rememberInfiniteTransition(label = "coach-orb3")
        val alpha3 by transition3.animateFloat(
            initialValue = 0.28f,
            targetValue = 0.14f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3600, delayMillis = 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "coach-orb3-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = 12.dp, y = 140.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(palette.accentPink.copy(alpha = alpha3), Color.Transparent)
                    )
                )
                .blur(44.dp)
                .align(Alignment.TopEnd)
        )
    }
}

// endregion

