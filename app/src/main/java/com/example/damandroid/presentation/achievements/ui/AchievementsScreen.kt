package com.example.damandroid.presentation.achievements.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import androidx.compose.ui.layout.ContentScale
import com.example.damandroid.domain.model.AchievementBadge
import com.example.damandroid.domain.model.AchievementChallenge
import com.example.damandroid.domain.model.AchievementUserStats
import com.example.damandroid.domain.model.AchievementsOverview
import com.example.damandroid.domain.model.CurrentUserLeaderboard
import com.example.damandroid.domain.model.LeaderboardEntry
import com.example.damandroid.domain.model.LeaderboardResponse
import com.example.damandroid.domain.model.AchievementNotification
import com.example.damandroid.domain.model.AchievementNotificationType
import com.example.damandroid.presentation.achievements.model.UiState
import com.example.damandroid.presentation.achievements.model.AchievementsTab
import com.example.damandroid.presentation.achievements.model.AchievementsUiState
import com.example.damandroid.presentation.achievements.viewmodel.AchievementsViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

@Composable
fun AchievementsRoute(
    viewModel: AchievementsViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val leaderboardState by viewModel.leaderboardState.collectAsState()
    val currentPageState = remember { mutableStateOf(1) }
    var currentPage by currentPageState
    
    val badgesState by viewModel.badgesState.collectAsState()
    val challengesState by viewModel.challengesState.collectAsState()
    
    // Rafraîchir automatiquement les données quand l'écran devient visible
    LaunchedEffect(Unit) {
        // Rafraîchir toutes les données des achievements
        viewModel.refresh()
    }
    
    AchievementsScreen(
        state = uiState,
        badgesState = badgesState,
        challengesState = challengesState,
        leaderboardState = leaderboardState,
        currentPage = currentPage,
        onCurrentPageChange = { currentPageState.value = it },
        onBack = onBack,
        onTabSelected = viewModel::onTabSelected,
        onRefresh = viewModel::refresh,
        onRefreshBadges = viewModel::refreshBadges,
        onRefreshChallenges = viewModel::refreshChallenges,
        onRefreshLeaderboard = { page -> viewModel.refreshLeaderboard(page) },
        modifier = modifier
    )
}

@Composable
fun AchievementsScreen(
    state: AchievementsUiState,
    badgesState: UiState<List<AchievementBadge>>,
    challengesState: UiState<List<AchievementChallenge>>,
    leaderboardState: UiState<LeaderboardResponse>,
    currentPage: Int,
    onCurrentPageChange: (Int) -> Unit,
    onBack: (() -> Unit)?,
    onTabSelected: (tab: AchievementsTab) -> Unit,
    onRefresh: () -> Unit,
    onRefreshBadges: () -> Unit,
    onRefreshChallenges: () -> Unit,
    onRefreshLeaderboard: (page: Int) -> Unit,
    notificationsState: com.example.damandroid.presentation.achievements.model.UiState<com.example.damandroid.domain.model.AchievementNotificationsResponse>? = null,
    onRefreshNotifications: () -> Unit = {},
    onMarkNotificationAsRead: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Note: badgesState, challengesState, leaderboardState sont gardés pour compatibilité
    // mais on utilise le design legacy avec state.overview
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(message = state.error, onRetry = onRefresh, modifier = modifier)
        else -> {
            // Utiliser LegacyAchievementsContent pour le design original
            val overview = state.overview ?: return
            LegacyAchievementsContent(
                overview = overview,
                selectedTab = state.selectedTab,
                onBack = onBack,
                onTabSelected = onTabSelected,
                notificationsState = notificationsState,
                onRefreshNotifications = onRefreshNotifications,
                onMarkNotificationAsRead = onMarkNotificationAsRead,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LegacyAchievementsContent(
    overview: com.example.damandroid.domain.model.AchievementsOverview,
    selectedTab: AchievementsTab,
    onBack: (() -> Unit)?,
    onTabSelected: (AchievementsTab) -> Unit,
    notificationsState: com.example.damandroid.presentation.achievements.model.UiState<com.example.damandroid.domain.model.AchievementNotificationsResponse>? = null,
    onRefreshNotifications: () -> Unit = {},
    onMarkNotificationAsRead: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backgroundBrush = remember {
        Brush.linearGradient(
            listOf(
                Color(0xFFE8D5F2),
                Color(0xFFFFE4F1),
                Color(0xFFE5E5F0)
            )
        )
    }
    val headerSurface = Color.White.copy(alpha = 0.4f)
    val headerBorder = Color.White.copy(alpha = 0.6f)
    val primaryText = Color(0xFF1A202C)
    val secondaryText = Color(0xFF4B5563)
    val mutedText = Color(0xFF6B7280)
    val accentPurple = Color(0xFFA855F7)
    val accentPink = Color(0xFFEC4899)
    val accentBlue = Color(0xFF2563EB)
    val accentGold = Color(0xFFFBBF24)
    val accentTeal = Color(0xFF86EFAC)
    val progressTrack = Color(0xFFE5E7EB)
    val progressActive = accentPurple
    val iconOnAccent = Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        LegacyFloatingAchievementsOrbs(
            primary = accentPurple,
            secondary = accentPink,
            tertiary = accentBlue
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                color = headerSurface,
                border = BorderStroke(1.dp, headerBorder)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color(0xFF2D3748),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🏆", fontSize = 24.sp)
                                Text(
                                    text = "Achievements",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(40.dp))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        LegacyLevelCard(
                            stats = overview.stats,
                            primaryText = primaryText,
                            labelColor = Color(0xFF92400E),
                            valueColor = Color(0xFF78350F),
                            progressActive = progressActive,
                            progressTrack = progressTrack
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LegacyQuickStatCard(
                                icon = Icons.Default.Star,
                                value = overview.stats.totalBadges.toString(),
                                label = "Badges",
                                iconColor = accentGold,
                                primaryText = primaryText,
                                mutedText = mutedText,
                                modifier = Modifier.weight(1f)
                            )
                            LegacyQuickStatCard(
                                icon = Icons.Default.FlashOn,
                                value = overview.stats.currentStreak.toString(),
                                label = "Day Streak",
                                iconColor = accentPink,
                                primaryText = primaryText,
                                mutedText = mutedText,
                                modifier = Modifier.weight(1f)
                            )
                            LegacyQuickStatCard(
                                icon = Icons.Default.TrendingUp,
                                value = overview.stats.longestStreak.toString(),
                                label = "Best Streak",
                                iconColor = accentBlue,
                                primaryText = primaryText,
                                mutedText = mutedText,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LegacyTabChip(
                            text = "Badges",
                            selected = selectedTab == AchievementsTab.BADGES,
                            onClick = { onTabSelected(AchievementsTab.BADGES) },
                            selectedBrush = Brush.linearGradient(listOf(accentPurple, accentPink)),
                            selectedTextColor = iconOnAccent,
                            unselectedTextColor = mutedText,
                            modifier = Modifier.weight(1f)
                        )
                        LegacyTabChip(
                            text = "Challenges",
                            selected = selectedTab == AchievementsTab.CHALLENGES,
                            onClick = { onTabSelected(AchievementsTab.CHALLENGES) },
                            selectedBrush = Brush.linearGradient(listOf(accentPurple, accentPink)),
                            selectedTextColor = iconOnAccent,
                            unselectedTextColor = mutedText,
                            modifier = Modifier.weight(1f)
                        )
                        LegacyTabChip(
                            text = "Leaderboard",
                            selected = selectedTab == AchievementsTab.LEADERBOARD,
                            onClick = { onTabSelected(AchievementsTab.LEADERBOARD) },
                            selectedBrush = Brush.linearGradient(listOf(accentPurple, accentPink)),
                            selectedTextColor = iconOnAccent,
                            unselectedTextColor = mutedText,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = 14.dp,
                            top = 12.dp,
                            end = 14.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (selectedTab) {
                            AchievementsTab.BADGES -> {
                                val chunks = overview.badges.chunked(2)
                                items(chunks) { rowBadges ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowBadges.forEach { badge ->
                                            LegacyBadgeCard(
                                                badge = badge,
                                                primaryText = primaryText,
                                                secondaryText = secondaryText,
                                                mutedText = mutedText,
                                                progressActive = progressActive,
                                                progressTrack = progressTrack,
                                                iconOnAccent = iconOnAccent,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowBadges.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            AchievementsTab.CHALLENGES -> {
                                items(overview.challenges) { challenge ->
                                    LegacyChallengeCard(
                                        challenge = challenge,
                                        primaryText = primaryText,
                                        secondaryText = secondaryText,
                                        mutedText = mutedText,
                                        progressActive = progressActive,
                                        progressTrack = progressTrack,
                                        iconOnAccent = iconOnAccent,
                                        gradient = Brush.linearGradient(listOf(accentPurple, accentPink))
                                    )
                                }
                            }

                            AchievementsTab.LEADERBOARD -> {
                                // Utiliser la nouvelle structure avec currentUser séparé
                                // Pour le legacy content, on affiche toujours la liste combinée depuis overview
                                item {
                                    LegacyLeaderboardHeader(
                                        primaryText = primaryText,
                                        iconOnAccent = iconOnAccent,
                                        gradient = Brush.linearGradient(
                                            listOf(
                                                Color(0xFFFDE68A).copy(alpha = 0.6f),
                                                Color(0xFFF97316).copy(alpha = 0.5f)
                                            )
                                        )
                                    )
                                }
                                // Filtrer les doublons et utiliser une clé unique pour éviter les erreurs
                                val leaderboardEntries = overview.leaderboard.distinctBy { "${it.rank}-${it.name}-${it.points}" }
                                leaderboardEntries.forEachIndexed { index, entry ->
                                    item(key = "legacy-leaderboard-${entry.rank}-${entry.name}-${entry.points}-$index") {
                                        LegacyLeaderboardRow(
                                            entry = entry,
                                            primaryText = primaryText,
                                            secondaryText = secondaryText,
                                            mutedText = mutedText,
                                            accentGold = accentGold,
                                            chipSurface = Color.White.copy(alpha = 0.8f),
                                            chipBorder = Color.White.copy(alpha = 0.6f),
                                            currentUserBackground = Color(0xFFFDF4FF).copy(alpha = 0.7f),
                                            iconOnAccent = iconOnAccent
                                        )
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

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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
private fun LegacyLevelCard(
    stats: AchievementUserStats,
    primaryText: Color,
    labelColor: Color,
    valueColor: Color,
    progressActive: Color,
    progressTrack: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFA855F7),
                        Color(0xFFEC4899)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Your Level", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    Text(text = "Level ${stats.level}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "XP Progress", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
                    Text(
                        text = "${stats.currentLevelXp} / ${stats.nextLevelXp}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (stats.currentLevelXp.toFloat() / stats.nextLevelXp.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressActive,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
private fun LegacyQuickStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    primaryText: Color,
    mutedText: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryText)
            Text(text = label, fontSize = 11.sp, color = mutedText)
        }
    }
}

@Composable
private fun LegacyTabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedBrush: Brush,
    selectedTextColor: Color,
    unselectedTextColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        val shape = RoundedCornerShape(12.dp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (selected) {
                        Modifier.background(selectedBrush, shape)
                    } else {
                        Modifier.background(Color.Transparent, shape)
                    }
                )
                .border(
                    BorderStroke(1.dp, if (selected) Color.Transparent else Color.White.copy(alpha = 0.5f)),
                    shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = if (selected) selectedTextColor else unselectedTextColor
            )
        }
    }
}

@Composable
private fun LegacyBadgeCard(
    badge: AchievementBadge,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    progressActive: Color,
    progressTrack: Color,
    iconOnAccent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.unlocked) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Afficher l'image depuis l'URL si disponible, sinon l'emoji
            badge.iconUrl?.takeIf { it.isNotBlank() }?.let { url ->
                SubcomposeAsyncImage(
                    model = url,
                    contentDescription = badge.title,
                    modifier = Modifier
                        .size(64.dp)
                        .alpha(if (badge.unlocked) 1f else 0.6f),
                    contentScale = ContentScale.Fit,
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                    },
                    error = {
                        Text(
                            text = badge.icon,
                            fontSize = 42.sp,
                            modifier = Modifier.alpha(if (badge.unlocked) 1f else 0.6f)
                        )
                    },
                    success = { SubcomposeAsyncImageContent() }
                )
            } ?: run {
                // Pas d'URL, utiliser l'emoji
                Text(
                    text = badge.icon,
                    fontSize = 42.sp,
                    modifier = Modifier.alpha(if (badge.unlocked) 1f else 0.6f)
                )
            }
            Text(text = badge.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = primaryText, textAlign = TextAlign.Center)
            Text(text = badge.description, fontSize = 12.sp, color = mutedText, textAlign = TextAlign.Center)
            Text(text = "Category: ${badge.category}", fontSize = 11.sp, color = secondaryText)

            if (badge.unlocked) {
                badge.unlockedDate?.let {
                    Text(text = "Unlocked on $it", fontSize = 11.sp, color = mutedText)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = {
                            val progress = badge.progress ?: 0
                            val total = badge.total ?: 1
                            progress.toFloat() / total.toFloat()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressActive,
                        trackColor = progressTrack
                    )
                    Text(
                        text = "${badge.progress ?: 0}/${badge.total ?: 0} progress",
                        fontSize = 11.sp,
                        color = mutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun LegacyChallengeCard(
    challenge: AchievementChallenge,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    progressActive: Color,
    progressTrack: Color,
    iconOnAccent: Color,
    gradient: Brush
) {
    val isCompleted = challenge.progress >= challenge.total
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF10B981).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.9f)
        ),
        border = BorderStroke(
            if (isCompleted) 2.dp else 1.dp,
            if (isCompleted) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = if (isCompleted) Brush.linearGradient(
                                listOf(
                                    Color(0xFF10B981).copy(alpha = 0.2f),
                                    Color(0xFF10B981).copy(alpha = 0.3f)
                                )
                            ) else gradient,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = iconOnAccent, modifier = Modifier.size(20.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = challenge.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) Color(0xFF10B981) else primaryText
                            )
                            if (isCompleted) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = "✓",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        // Chip pour le type de challenge
                        ChallengeTypeChip(challenge.challengeType, mutedText)
                    }
                    Text(text = challenge.description, fontSize = 12.sp, color = secondaryText)
                    // Afficher les jours restants avec icône d'alerte si urgent
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (challenge.daysLeft <= 1) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFDC2626) // Rouge pour urgent
                            )
                        }
                        Text(
                            text = "${challenge.daysLeft} ${if (challenge.daysLeft > 1) "jours" else "jour"} restant${if (challenge.daysLeft > 1) "s" else ""}",
                            fontSize = 11.sp,
                            color = if (challenge.daysLeft <= 1) Color(0xFFDC2626) else mutedText,
                            fontWeight = if (challenge.daysLeft <= 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Progress", fontSize = 12.sp, color = mutedText)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isCompleted = challenge.progress >= challenge.total
                        if (isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981), // Vert pour terminé
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.White
                                    )
                                    Text(
                                        text = "Terminé",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(text = "${challenge.progress}/${challenge.total}", fontSize = 12.sp, color = primaryText, fontWeight = FontWeight.Medium)
                    }
                }
                LinearProgressIndicator(
                    progress = { (challenge.progress.toFloat() / challenge.total.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (challenge.progress >= challenge.total) Color(0xFF10B981) else progressActive,
                    trackColor = progressTrack
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🏆", fontSize = 14.sp)
                Text(text = "Reward: ${challenge.reward}", fontSize = 11.sp, color = secondaryText)
            }
        }
    }
}

@Composable
private fun LegacyLeaderboardHeader(
    primaryText: Color,
    iconOnAccent: Color,
    gradient: Brush
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .background(gradient, RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, tint = iconOnAccent, modifier = Modifier.size(18.dp))
                    Text(text = "Weekly Leaderboard", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = iconOnAccent)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Top performers this week. Keep going to reach the top!",
                    fontSize = 12.sp,
                    color = iconOnAccent.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun LegacyLeaderboardRow(
    entry: LeaderboardEntry,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    accentGold: Color,
    chipSurface: Color,
    chipBorder: Color,
    currentUserBackground: Color,
    iconOnAccent: Color
) {
    val isCurrentUser = entry.name.equals("You", ignoreCase = true)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCurrentUser) currentUserBackground else Color.White.copy(alpha = 0.9f)),
        border = BorderStroke(if (isCurrentUser) 2.dp else 1.dp, if (isCurrentUser) accentGold else Color.White.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = if (entry.badge.isNotEmpty()) accentGold.copy(alpha = 0.2f) else chipSurface,
                border = BorderStroke(1.dp, if (entry.badge.isNotEmpty()) accentGold else chipBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (entry.badge.isNotEmpty()) {
                        Text(text = entry.badge, fontSize = 16.sp)
                    } else {
                        Text(text = entry.rank.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryText)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entry.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = primaryText)
                Text(text = "${entry.points} XP", fontSize = 12.sp, color = mutedText)
            }
            if (isCurrentUser) {
                Surface(shape = RoundedCornerShape(8.dp), color = accentGold) {
                    Text(text = "You", fontSize = 11.sp, color = iconOnAccent, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun LegacyNotificationCard(
    notification: AchievementNotification,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    accentPurple: Color,
    accentGold: Color,
    onMarkAsRead: () -> Unit
) {
    val icon = when (notification.type) {
        AchievementNotificationType.XP_EARNED -> Icons.Default.TrendingUp
        AchievementNotificationType.BADGE_UNLOCKED -> Icons.Default.EmojiEvents
        AchievementNotificationType.LEVEL_UP -> Icons.Default.Star
        AchievementNotificationType.CHALLENGE_COMPLETED -> Icons.Default.CheckCircle
        AchievementNotificationType.STREAK_UPDATED -> Icons.Default.Whatshot
    }
    
    val iconColor = when (notification.type) {
        AchievementNotificationType.XP_EARNED -> accentGold
        AchievementNotificationType.BADGE_UNLOCKED -> accentGold
        AchievementNotificationType.LEVEL_UP -> accentPurple
        AchievementNotificationType.CHALLENGE_COMPLETED -> Color(0xFF4CAF50)
        AchievementNotificationType.STREAK_UPDATED -> Color(0xFFFF5722)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                Color.White.copy(alpha = 0.6f)
            } else {
                Color.White.copy(alpha = 0.9f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (notification.isRead) {
                Color.White.copy(alpha = 0.4f)
            } else {
                iconColor.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône de notification
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.2f),
                border = BorderStroke(2.dp, iconColor.copy(alpha = 0.4f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Contenu de la notification
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = primaryText
                )
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = if (notification.isRead) mutedText else secondaryText
                )
                
                // Extraire le montant XP si c'est une notification XP_EARNED
                if (notification.type == AchievementNotificationType.XP_EARNED) {
                    val xpAmount = notification.metadata?.get("xpAmount") as? Number
                    if (xpAmount != null) {
                        Surface(
                            color = accentGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+${xpAmount} XP",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentGold
                            )
                        }
                    }
                }
            }
            
            // Indicateur de non-lu
            if (!notification.isRead) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = CircleShape,
                    color = accentPurple
                ) {}
            }
        }
    }
    
    // Marquer comme lu automatiquement après un délai
    LaunchedEffect(notification.id) {
        if (!notification.isRead) {
            kotlinx.coroutines.delay(3000) // 3 secondes
            onMarkAsRead()
        }
    }
}

@Composable
private fun LegacyFloatingAchievementsOrbs(
    primary: Color,
    secondary: Color,
    tertiary: Color
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val transition1 = rememberInfiniteTransition(label = "legacy-orb-1")
        val alpha1 by transition1.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "legacy-orb-1-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = (-8).dp, y = (-12).dp)
                .size(288.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(primary.copy(alpha = alpha1), Color.Transparent)))
                .blur(48.dp)
                .align(Alignment.TopStart)
        )

        val transition2 = rememberInfiniteTransition(label = "legacy-orb-2")
        val alpha2 by transition2.animateFloat(
            initialValue = 0.28f,
            targetValue = 0.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, delayMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "legacy-orb-2-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = (-12).dp, y = (-20).dp)
                .size(384.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(secondary.copy(alpha = alpha2), Color.Transparent)))
                .blur(48.dp)
                .align(Alignment.BottomEnd)
        )

        val transition3 = rememberInfiniteTransition(label = "legacy-orb-3")
        val alpha3 by transition3.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, delayMillis = 1500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "legacy-orb-3-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = 24.dp, y = 120.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(tertiary.copy(alpha = alpha3), Color.Transparent)))
                .blur(44.dp)
                .align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun ContentState(
    state: AchievementsUiState,
    badgesState: UiState<List<AchievementBadge>>,
    challengesState: UiState<List<AchievementChallenge>>,
    leaderboardState: UiState<LeaderboardResponse>,
    currentPage: Int,
    onCurrentPageChange: (Int) -> Unit,
    onBack: (() -> Unit)?,
    onTabSelected: (tab: AchievementsTab) -> Unit,
    onRefreshBadges: () -> Unit,
    onRefreshChallenges: () -> Unit,
    onRefreshLeaderboard: (page: Int) -> Unit,
    notificationsState: UiState<com.example.damandroid.domain.model.AchievementNotificationsResponse>? = null,
    onRefreshNotifications: () -> Unit = {},
    onMarkNotificationAsRead: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val overview = state.overview ?: return
    val themeController = LocalThemeController.current
    val appColors = rememberAppThemeColors(themeController.isDarkMode)
    val palette = rememberAchievementsPalette(appColors)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        FloatingAchievementsOrbs(palette)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { AchievementsHeader(onBack = onBack, palette = palette) }
            item { StatsSection(stats = overview.stats, palette = palette) }
            item {
                AchievementsTabs(
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected,
                    palette = palette
                )
            }

            when (state.selectedTab) {
                AchievementsTab.BADGES -> {
                    BadgesContent(
                        badgesState = badgesState,
                        onRefresh = onRefreshBadges,
                        palette = palette,
                        scope = this@LazyColumn
                    )
                }
                AchievementsTab.CHALLENGES -> {
                    ChallengesContent(
                        challengesState = challengesState,
                        onRefresh = onRefreshChallenges,
                        palette = palette,
                        scope = this@LazyColumn
                    )
                }
                AchievementsTab.LEADERBOARD -> {
                    LeaderboardContentNew(
                        leaderboardState = leaderboardState,
                        currentPage = currentPage,
                        onCurrentPageChange = onCurrentPageChange,
                        onRefreshLeaderboard = onRefreshLeaderboard,
                        palette = palette,
                        scope = this@LazyColumn
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

// region Header & Stats

@Composable
private fun AchievementsHeader(
    onBack: (() -> Unit)?,
    palette: AchievementsPalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = palette.glassSurface),
        border = BorderStroke(1.dp, palette.glassBorder),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.secondaryText,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Text(
                    text = "🏆 Achievements",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primaryText,
                    letterSpacing = (-0.5).sp
                )
            }
            Icon(
                imageVector = Icons.Default.Leaderboard,
                contentDescription = null,
                tint = palette.secondaryText
            )
        }
    }
}

@Composable
private fun StatsSection(
    stats: AchievementUserStats,
    palette: AchievementsPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LevelCard(stats = stats, palette = palette)
        QuickStatsRow(stats = stats, palette = palette)
    }
}

@Composable
private fun LevelCard(
    stats: AchievementUserStats,
    palette: AchievementsPalette
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = palette.levelCardBackground),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, palette.levelCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Your Level",
                        fontSize = 12.sp,
                        color = palette.levelLabel
                    )
                    Text(
                        text = "Level ${stats.level}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.levelValue
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "XP Progress",
                        fontSize = 12.sp,
                        color = palette.levelLabel
                    )
                    Text(
                        text = "${stats.currentLevelXp} / ${stats.nextLevelXp}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.levelValue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (stats.currentLevelXp.toFloat() / stats.nextLevelXp.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = palette.progressActive,
                trackColor = palette.progressTrack
            )
        }
    }
}

@Composable
private fun QuickStatsRow(
    stats: AchievementUserStats,
    palette: AchievementsPalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = Icons.Default.EmojiEvents,
            label = "Badges",
            value = stats.totalBadges.toString(),
            palette = palette,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Default.Whatshot,
            label = "Day Streak",
            value = stats.currentStreak.toString(),
            palette = palette,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Default.TrendingUp,
            label = "Best Streak",
            value = stats.longestStreak.toString(),
            palette = palette,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    palette: AchievementsPalette,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = palette.statCardBackground),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, palette.statCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = palette.primaryText,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.primaryText
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = palette.secondaryText
            )
        }
    }
}

@Composable
private fun AchievementsTabs(
    selectedTab: AchievementsTab,
    onTabSelected: (AchievementsTab) -> Unit,
    palette: AchievementsPalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.tabBackground),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, palette.tabBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabButton(
                text = "Badges",
                selected = selectedTab == AchievementsTab.BADGES,
                onClick = { onTabSelected(AchievementsTab.BADGES) },
                palette = palette,
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Challenges",
                selected = selectedTab == AchievementsTab.CHALLENGES,
                onClick = { onTabSelected(AchievementsTab.CHALLENGES) },
                palette = palette,
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Leaderboard",
                selected = selectedTab == AchievementsTab.LEADERBOARD,
                onClick = { onTabSelected(AchievementsTab.LEADERBOARD) },
                palette = palette,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    palette: AchievementsPalette,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) palette.tabSelected else Color.Transparent,
            contentColor = if (selected) palette.tabSelectedText else palette.secondaryText
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun BadgeCard(
    badge: AchievementBadge,
    palette: AchievementsPalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.badgeCardBackground),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, palette.badgeCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = rarityColor(badge.rarity, palette).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, rarityColor(badge.rarity, palette))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Afficher l'image depuis l'URL si disponible, sinon l'emoji
                        badge.iconUrl?.takeIf { it.isNotBlank() }?.let { url ->
                            SubcomposeAsyncImage(
                                model = url,
                                contentDescription = badge.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .alpha(if (badge.unlocked) 1f else 0.6f),
                                contentScale = ContentScale.Fit,
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                },
                                error = {
                                    Text(text = badge.icon, fontSize = 26.sp)
                                },
                                success = { SubcomposeAsyncImageContent() }
                            )
                        } ?: run {
                            // Pas d'URL, utiliser l'emoji
                            Text(text = badge.icon, fontSize = 26.sp)
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = badge.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.primaryText
                    )
                    Text(
                        text = badge.description,
                        fontSize = 13.sp,
                        color = palette.secondaryText
                    )
                }

                BadgeStatusChip(unlocked = badge.unlocked, palette = palette)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = badge.category,
                    fontSize = 12.sp,
                    color = palette.secondaryText
                )
                badge.unlockedDate?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = palette.secondaryText
                    )
                }
            }

            if (!badge.unlocked && badge.progress != null && badge.total != null) {
                LinearProgressIndicator(
                    progress = { badge.progress.toFloat() / badge.total.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = palette.progressActive,
                    trackColor = palette.progressTrack
                )
                Text(
                    text = "${badge.progress} / ${badge.total}",
                    fontSize = 12.sp,
                    color = palette.secondaryText
                )
            }
        }
    }
}

@Composable
private fun BadgeStatusChip(
    unlocked: Boolean,
    palette: AchievementsPalette
) {
    val label = if (unlocked) "Unlocked" else "In progress"
    val color = if (unlocked) palette.success else palette.warning
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.18f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ChallengeTypeChip(type: String, mutedText: Color) {
    val (color, displayName) = when (type.lowercase()) {
        "daily" -> Color(0xFF4CAF50) to "Quotidien"
        "weekly" -> Color(0xFF2196F3) to "Hebdomadaire"
        "monthly" -> Color(0xFF9C27B0) to "Mensuel"
        "event" -> Color(0xFFFF9800) to "Événement"
        else -> Color(0xFF808080) to type
    }
    
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    palette: AchievementsPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primaryText
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.accentPurple.copy(alpha = 0.15f)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.accentPurple,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    challenge: AchievementChallenge,
    palette: AchievementsPalette
) {
    val isCompleted = challenge.progress >= challenge.total
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) palette.success.copy(alpha = 0.1f) else palette.challengeCardBackground
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            if (isCompleted) 2.dp else 1.dp,
            if (isCompleted) palette.success else palette.challengeCardBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 5.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Insights,
                    contentDescription = null,
                    tint = if (isCompleted) palette.success else palette.primaryText,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = challenge.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) palette.success else palette.primaryText
                            )
                            if (isCompleted) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = palette.success,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    Text(
                                        text = "✓",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        // Chip pour le type de challenge
                        ChallengeTypeChip(challenge.challengeType, palette.secondaryText)
                    }
                    Text(
                        text = challenge.description,
                        fontSize = 13.sp,
                        color = palette.secondaryText
                    )
                    // Afficher les jours restants avec icône d'alerte si urgent
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (challenge.daysLeft <= 1) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFDC2626) // Rouge pour urgent
                            )
                        }
                        Text(
                            text = "${challenge.daysLeft} ${if (challenge.daysLeft > 1) "jours" else "jour"} restant${if (challenge.daysLeft > 1) "s" else ""}",
                            fontSize = 11.sp,
                            color = if (challenge.daysLeft <= 1) Color(0xFFDC2626) else palette.secondaryText,
                            fontWeight = if (challenge.daysLeft <= 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { (challenge.progress.toFloat() / challenge.total.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (challenge.progress >= challenge.total) palette.success else palette.progressActive,
                trackColor = palette.progressTrack
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isCompleted = challenge.progress >= challenge.total
                    if (isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = palette.success.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, palette.success)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = palette.success
                                )
                                Text(
                                    text = "Terminé",
                                    fontSize = 12.sp,
                                    color = palette.success,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    // Afficher la progression avec pourcentage
                    val progressPercentage = if (challenge.total > 0) {
                        ((challenge.progress.toFloat() / challenge.total.toFloat()) * 100).toInt()
                    } else 0
                    Text(
                        text = "${challenge.progress} / ${challenge.total} (${progressPercentage}%)",
                        fontSize = 12.sp,
                        color = palette.secondaryText,
                        fontWeight = if (progressPercentage >= 80) FontWeight.Bold else FontWeight.Normal
                    )
                }
                Text(
                    text = challenge.deadline,
                    fontSize = 12.sp,
                    color = palette.warning
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = palette.glassSurface.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, palette.glassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = palette.success,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = challenge.reward,
                        fontSize = 12.sp,
                        color = palette.primaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardHeader(palette: AchievementsPalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Leaderboard",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.primaryText
        )
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            tint = palette.secondaryText
        )
    }
}

private fun BadgesContent(
    badgesState: UiState<List<AchievementBadge>>,
    onRefresh: () -> Unit,
    palette: AchievementsPalette,
    scope: LazyListScope
) {
    when (badgesState) {
        is UiState.Loading -> {
            scope.item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        is UiState.Success -> {
            val badges = badgesState.data
            val earnedBadges = badges.filter { it.unlocked }
            val inProgressBadges = badges.filter { !it.unlocked }
            
            // Section "Badges obtenus"
            if (earnedBadges.isNotEmpty()) {
                scope.item {
                    Text(
                        text = "Badges obtenus (${earnedBadges.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                scope.items(
                    items = earnedBadges,
                    key = { it.id }
                ) { badge ->
                    BadgeCard(badge = badge, palette = palette)
                }
            }
            
            // Séparateur
            if (earnedBadges.isNotEmpty() && inProgressBadges.isNotEmpty()) {
                scope.item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                        thickness = 2.dp,
                        color = palette.glassBorder
                    )
                }
            }
            
            // Section "En cours"
            if (inProgressBadges.isNotEmpty()) {
                scope.item {
                    Text(
                        text = "En cours (${inProgressBadges.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                scope.items(
                    items = inProgressBadges,
                    key = { it.id }
                ) { badge ->
                    BadgeCard(badge = badge, palette = palette)
                }
            }
            
            // Message si aucun badge
            if (earnedBadges.isEmpty() && inProgressBadges.isEmpty()) {
                scope.item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = palette.secondaryText
                            )
                            Text(
                                text = "Aucun badge pour le moment",
                                fontSize = 16.sp,
                                color = palette.secondaryText
                            )
                            Text(
                                text = "Continue à faire du sport pour débloquer des badges !",
                                fontSize = 12.sp,
                                color = palette.mutedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        is UiState.Error -> {
            scope.item {
                ErrorContentBadgesComposable(
                    message = badgesState.message,
                    onRetry = onRefresh,
                    palette = palette
                )
            }
        }
        is UiState.Idle -> {
            scope.item {
                Text(
                    text = "Chargement...",
                    modifier = Modifier.padding(16.dp),
                    color = palette.primaryText
                )
            }
        }
    }
}

private fun ChallengesContent(
    challengesState: UiState<List<AchievementChallenge>>,
    onRefresh: () -> Unit,
    palette: AchievementsPalette,
    scope: LazyListScope
) {
    when (challengesState) {
        is UiState.Loading -> {
            scope.item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        is UiState.Success -> {
            val challenges = challengesState.data
            
            if (challenges.isEmpty()) {
                scope.item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.StarOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = palette.secondaryText
                            )
                            Text(
                                text = "Aucun défi actif",
                                fontSize = 16.sp,
                                color = palette.primaryText
                            )
                            Text(
                                text = "De nouveaux défis apparaîtront bientôt !",
                                fontSize = 12.sp,
                                color = palette.secondaryText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Trier et grouper les challenges par type
                // Trier: complétés en bas, presque complétés en haut, puis par type
                val sortedChallenges = challenges.sortedWith(
                    compareBy<AchievementChallenge> { it.progress >= it.total } // Complétés en bas (true vient après false)
                        .thenByDescending { it.progress.toFloat() / it.total.toFloat() } // Presque complétés en premier dans chaque groupe
                        .thenBy { it.challengeType } // Puis par type (daily, weekly, monthly)
                        .thenBy { it.title } // Enfin par titre pour cohérence
                )
                
                // Grouper par type
                val dailyChallenges = sortedChallenges.filter { it.challengeType.lowercase() == "daily" }
                val weeklyChallenges = sortedChallenges.filter { it.challengeType.lowercase() == "weekly" }
                val monthlyChallenges = sortedChallenges.filter { it.challengeType.lowercase() == "monthly" }
                val otherChallenges = sortedChallenges.filter { 
                    val type = it.challengeType.lowercase()
                    type != "daily" && type != "weekly" && type != "monthly"
                }
                
                // Afficher par sections groupées
                if (dailyChallenges.isNotEmpty()) {
                    scope.item {
                        SectionHeader(
                            title = "Défis Quotidiens",
                            count = dailyChallenges.size,
                            palette = palette
                        )
                    }
                    scope.items(
                        items = dailyChallenges,
                        key = { it.id }
                    ) { challenge ->
                        ChallengeCard(challenge = challenge, palette = palette)
                    }
                }
                
                if (weeklyChallenges.isNotEmpty()) {
                    if (dailyChallenges.isNotEmpty()) {
                        scope.item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    scope.item {
                        SectionHeader(
                            title = "Défis Hebdomadaires",
                            count = weeklyChallenges.size,
                            palette = palette
                        )
                    }
                    scope.items(
                        items = weeklyChallenges,
                        key = { it.id }
                    ) { challenge ->
                        ChallengeCard(challenge = challenge, palette = palette)
                    }
                }
                
                if (monthlyChallenges.isNotEmpty()) {
                    if (dailyChallenges.isNotEmpty() || weeklyChallenges.isNotEmpty()) {
                        scope.item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    scope.item {
                        SectionHeader(
                            title = "Défis Mensuels",
                            count = monthlyChallenges.size,
                            palette = palette
                        )
                    }
                    scope.items(
                        items = monthlyChallenges,
                        key = { it.id }
                    ) { challenge ->
                        ChallengeCard(challenge = challenge, palette = palette)
                    }
                }
                
                if (otherChallenges.isNotEmpty()) {
                    if (dailyChallenges.isNotEmpty() || weeklyChallenges.isNotEmpty() || monthlyChallenges.isNotEmpty()) {
                        scope.item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    scope.item {
                        SectionHeader(
                            title = "Autres Défis",
                            count = otherChallenges.size,
                            palette = palette
                        )
                    }
                    scope.items(
                        items = otherChallenges,
                        key = { it.id }
                    ) { challenge ->
                        ChallengeCard(challenge = challenge, palette = palette)
                    }
                }
            }
        }
        is UiState.Error -> {
            scope.item {
                ErrorContentChallengesComposable(
                    message = challengesState.message,
                    onRetry = onRefresh,
                    palette = palette
                )
            }
        }
        is UiState.Idle -> {
            scope.item {
                Text(
                    text = "Chargement...",
                    modifier = Modifier.padding(16.dp),
                    color = palette.primaryText
                )
            }
        }
    }
}

@Composable
private fun ErrorContentBadgesComposable(
    message: String,
    onRetry: () -> Unit,
    palette: AchievementsPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Erreur",
            modifier = Modifier.size(64.dp),
            tint = palette.warning
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = palette.warning,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Réessayer")
        }
    }
}

@Composable
private fun ErrorContentChallengesComposable(
    message: String,
    onRetry: () -> Unit,
    palette: AchievementsPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Erreur",
            modifier = Modifier.size(64.dp),
            tint = palette.warning
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = palette.warning,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Réessayer")
        }
    }
}

private fun LeaderboardContentNew(
    leaderboardState: UiState<LeaderboardResponse>,
    currentPage: Int,
    onCurrentPageChange: (Int) -> Unit,
    onRefreshLeaderboard: (page: Int) -> Unit,
    palette: AchievementsPalette,
    scope: LazyListScope
) {
    when (leaderboardState) {
        is UiState.Loading -> {
            scope.item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        is UiState.Success -> {
            val leaderboard = leaderboardState.data
            
            // Afficher currentUser séparément s'il existe
            leaderboard.currentUser?.let { currentUser ->
                scope.item {
                    CurrentUserPositionCard(
                        currentUser = currentUser,
                        palette = palette
                    )
                }
            }
            
            // Afficher le header du leaderboard
            scope.item {
                LeaderboardHeader(palette = palette)
            }
            
            // Afficher les entrées du leaderboard
            // Filtrer les doublons et exclure currentUser de la liste s'il est déjà dedans
            val leaderboardEntries = leaderboard.leaderboard
                .filter { entry ->
                    // Exclure l'utilisateur actuel s'il est déjà dans la liste du leaderboard
                    leaderboard.currentUser?.let { currentUser ->
                        !(entry.rank == currentUser.rank && 
                          entry.name == currentUser.username && 
                          entry.points == currentUser.totalXp)
                    } ?: true
                }
                .distinctBy { "${it.rank}-${it.name}-${it.points}" }
            
            // Utiliser une clé unique basée sur rank, name, points et l'index pour éviter les doublons
            leaderboardEntries.forEachIndexed { index, entry ->
                scope.item(key = "leaderboard-${entry.rank}-${entry.name}-${entry.points}-$index") {
                    LeaderboardRow(entry = entry, palette = palette)
                }
            }
            
            // Afficher les contrôles de pagination si nécessaire
            if (leaderboard.totalPages > 1) {
                scope.item {
                    PaginationControls(
                        currentPage = currentPage,
                        totalPages = leaderboard.totalPages,
                        onPreviousPage = {
                            if (currentPage > 1) {
                                val newPage = currentPage - 1
                                onCurrentPageChange(newPage)
                                onRefreshLeaderboard(newPage)
                            }
                        },
                        onNextPage = {
                            if (currentPage < leaderboard.totalPages) {
                                val newPage = currentPage + 1
                                onCurrentPageChange(newPage)
                                onRefreshLeaderboard(newPage)
                            }
                        },
                        palette = palette
                    )
                }
            }
        }
        is UiState.Error -> {
            scope.item {
                ErrorContentLeaderboard(
                    message = leaderboardState.message,
                    onRetry = { onRefreshLeaderboard(currentPage) },
                    palette = palette
                )
            }
        }
        is UiState.Idle -> {
            scope.item {
                Text(
                    text = "Chargement...",
                    modifier = Modifier.padding(16.dp),
                    color = palette.primaryText
                )
            }
        }
    }
}

@Composable
private fun CurrentUserPositionCard(
    currentUser: CurrentUserLeaderboard,
    palette: AchievementsPalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = palette.accentPurple.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, palette.accentPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Ta position",
                    fontSize = 12.sp,
                    color = palette.secondaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#${currentUser.rank}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.primaryText
                    )
                    Text(
                        text = currentUser.username,
                        fontSize = 18.sp,
                        color = palette.primaryText
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${currentUser.totalXp}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.primaryText
                )
                Text(
                    text = "XP",
                    fontSize = 12.sp,
                    color = palette.secondaryText
                )
            }
        }
    }
}

@Composable
private fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    palette: AchievementsPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousPage,
            enabled = currentPage > 1
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Page précédente",
                tint = if (currentPage > 1) palette.primaryText else palette.secondaryText.copy(alpha = 0.5f)
            )
        }
        
        Text(
            text = "Page $currentPage / $totalPages",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 14.sp,
            color = palette.primaryText
        )
        
        IconButton(
            onClick = onNextPage,
            enabled = currentPage < totalPages
        ) {
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Page suivante",
                tint = if (currentPage < totalPages) palette.primaryText else palette.secondaryText.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ErrorContentLeaderboard(
    message: String,
    onRetry: () -> Unit,
    palette: AchievementsPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Erreur",
            modifier = Modifier.size(64.dp),
            tint = palette.warning
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = palette.warning,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Réessayer")
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    palette: AchievementsPalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.leaderboardCardBackground),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, palette.leaderboardCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${entry.rank}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = palette.primaryText,
                modifier = Modifier.width(48.dp)
            )
            
            // Afficher la médaille si présente
            if (entry.badge.isNotBlank()) {
                Text(
                    text = entry.badge,
                    fontSize = 20.sp
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.primaryText
                )
                Text(
                    text = "${entry.points} XP",
                    fontSize = 12.sp,
                    color = palette.secondaryText
                )
            }
            
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = palette.accentGold.copy(alpha = 0.8f)
            )
        }
    }
}

private fun NotificationsContent(
    notificationsState: UiState<com.example.damandroid.domain.model.AchievementNotificationsResponse>?,
    onRefresh: () -> Unit,
    onMarkNotificationAsRead: (String) -> Unit,
    palette: AchievementsPalette,
    scope: LazyListScope
) {
    when (notificationsState) {
        is UiState.Loading -> {
            scope.item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        is UiState.Success -> {
            val notifications = notificationsState.data.notifications
            if (notifications.isEmpty()) {
                scope.item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = palette.secondaryText
                            )
                            Text(
                                text = "Aucune notification",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.secondaryText
                            )
                            Text(
                                text = "Vous recevrez des notifications lorsque vous gagnerez de l'XP ou débloquerez des badges",
                                fontSize = 14.sp,
                                color = palette.mutedText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                scope.items(notifications, key = { it.id }) { notification ->
                    NotificationCardNew(
                        notification = notification,
                        palette = palette,
                        onMarkAsRead = { onMarkNotificationAsRead(notification.id) }
                    )
                }
            }
        }
        is UiState.Error -> {
            scope.item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = palette.warning
                    )
                    Text(
                        text = "Erreur lors du chargement",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.warning
                    )
                    Button(onClick = onRefresh) {
                        Text("Réessayer")
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun NotificationCardNew(
    notification: AchievementNotification,
    palette: AchievementsPalette,
    onMarkAsRead: () -> Unit
) {
    val icon = when (notification.type) {
        AchievementNotificationType.XP_EARNED -> Icons.Default.TrendingUp
        AchievementNotificationType.BADGE_UNLOCKED -> Icons.Default.EmojiEvents
        AchievementNotificationType.LEVEL_UP -> Icons.Default.Star
        AchievementNotificationType.CHALLENGE_COMPLETED -> Icons.Default.CheckCircle
        AchievementNotificationType.STREAK_UPDATED -> Icons.Default.Whatshot
    }
    
    val iconColor = when (notification.type) {
        AchievementNotificationType.XP_EARNED -> palette.accentGold
        AchievementNotificationType.BADGE_UNLOCKED -> palette.accentGold
        AchievementNotificationType.LEVEL_UP -> palette.accentPurple
        AchievementNotificationType.CHALLENGE_COMPLETED -> Color(0xFF4CAF50)
        AchievementNotificationType.STREAK_UPDATED -> Color(0xFFFF5722)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                palette.glassSurface.copy(alpha = 0.6f)
            } else {
                palette.glassSurface
            }
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            if (notification.isRead) {
                palette.glassBorder.copy(alpha = 0.4f)
            } else {
                iconColor.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icône de notification
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.2f),
                border = BorderStroke(2.dp, iconColor.copy(alpha = 0.4f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Contenu de la notification
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = palette.primaryText
                )
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = if (notification.isRead) palette.mutedText else palette.secondaryText
                )
                
                // Extraire le montant XP si c'est une notification XP_EARNED
                if (notification.type == AchievementNotificationType.XP_EARNED) {
                    val xpAmount = notification.metadata?.get("xpAmount") as? Number
                    if (xpAmount != null) {
                        Surface(
                            color = palette.accentGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "+${xpAmount} XP",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.accentGold
                            )
                        }
                    }
                }
            }
            
            // Indicateur de non-lu
            if (!notification.isRead) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = CircleShape,
                    color = palette.accentPurple
                ) {}
            }
        }
    }
    
    // Marquer comme lu automatiquement après un délai
    LaunchedEffect(notification.id) {
        if (!notification.isRead) {
            kotlinx.coroutines.delay(3000) // 3 secondes
            onMarkAsRead()
        }
    }
}

// region Palette & Background

private data class AchievementsPalette(
    val background: Brush,
    val glassSurface: Color,
    val glassBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val success: Color,
    val warning: Color,
    val accentGold: Color,
    val accentPurple: Color,
    val accentPink: Color,
    val accentBlue: Color,
    val accentTeal: Color,
    val progressActive: Color,
    val progressTrack: Color,
    val levelLabel: Color,
    val levelValue: Color,
    val levelCardBackground: Color,
    val levelCardBorder: Color,
    val statCardBackground: Color,
    val statCardBorder: Color,
    val tabBackground: Color,
    val tabBorder: Color,
    val tabSelected: Color,
    val tabSelectedText: Color,
    val badgeCardBackground: Color,
    val badgeCardBorder: Color,
    val challengeCardBackground: Color,
    val challengeCardBorder: Color,
    val leaderboardCardBackground: Color,
    val leaderboardCardBorder: Color
)

@Composable
private fun rememberAchievementsPalette(appColors: AppThemeColors): AchievementsPalette {
    return AchievementsPalette(
        background = appColors.backgroundGradient,
        glassSurface = appColors.glassSurface,
        glassBorder = appColors.glassBorder,
        primaryText = appColors.primaryText,
        secondaryText = appColors.secondaryText,
        mutedText = appColors.mutedText,
        success = appColors.accentGreen,
        warning = appColors.accentOrange,
        accentGold = appColors.accentGold,
        accentPurple = appColors.accentPurple,
        accentPink = appColors.accentPink,
        accentBlue = appColors.accentBlue,
        accentTeal = appColors.accentTeal,
        progressActive = appColors.accentPurple,
        progressTrack = appColors.outline.copy(alpha = 0.4f),
        levelLabel = appColors.secondaryText,
        levelValue = appColors.primaryText,
        levelCardBackground = appColors.glassSurface.copy(alpha = 0.9f),
        levelCardBorder = appColors.accentPurple.copy(alpha = 0.35f),
        statCardBackground = appColors.glassSurface.copy(alpha = 0.85f),
        statCardBorder = appColors.glassBorder.copy(alpha = 0.6f),
        tabBackground = appColors.glassSurface.copy(alpha = 0.8f),
        tabBorder = appColors.glassBorder.copy(alpha = 0.6f),
        tabSelected = appColors.accentPurple,
        tabSelectedText = appColors.iconOnAccent,
        badgeCardBackground = appColors.glassSurface.copy(alpha = 0.9f),
        badgeCardBorder = appColors.glassBorder,
        challengeCardBackground = appColors.glassSurface.copy(alpha = 0.92f),
        challengeCardBorder = appColors.glassBorder,
        leaderboardCardBackground = appColors.glassSurface.copy(alpha = 0.88f),
        leaderboardCardBorder = appColors.glassBorder
    )
}

@Composable
private fun FloatingAchievementsOrbs(palette: AchievementsPalette) {
    Box(modifier = Modifier.fillMaxSize()) {
        val transition1 = rememberInfiniteTransition(label = "achievements-orb1")
        val alpha1 by transition1.animateFloat(
            initialValue = 0.32f,
            targetValue = 0.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "achievements-orb1-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = 48.dp, y = 120.dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            palette.accentPurple.copy(alpha = alpha1),
                            palette.glassSurface.copy(alpha = alpha1 * 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .blur(56.dp)
        )

        val transition2 = rememberInfiniteTransition(label = "achievements-orb2")
        val alpha2 by transition2.animateFloat(
            initialValue = 0.28f,
            targetValue = 0.14f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3400, delayMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "achievements-orb2-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = (-40).dp, y = (-60).dp)
                .size(176.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            palette.accentPink.copy(alpha = alpha2),
                            palette.glassSurface.copy(alpha = alpha2 * 0.55f),
                            Color.Transparent
                        )
                    )
                )
                .blur(60.dp)
                .align(Alignment.BottomEnd)
        )

        val transition3 = rememberInfiniteTransition(label = "achievements-orb3")
        val alpha3 by transition3.animateFloat(
            initialValue = 0.24f,
            targetValue = 0.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3600, delayMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "achievements-orb3-alpha"
        )

        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            palette.accentBlue.copy(alpha = alpha3),
                            palette.glassSurface.copy(alpha = alpha3 * 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .blur(40.dp)
                .align(Alignment.Center)
        )

        val transition4 = rememberInfiniteTransition(label = "achievements-orb4")
        val alpha4 by transition4.animateFloat(
            initialValue = 0.22f,
            targetValue = 0.11f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3600, delayMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "achievements-orb4-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = (-8).dp, y = 140.dp)
                .size(92.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            palette.accentTeal.copy(alpha = alpha4),
                            palette.glassSurface.copy(alpha = alpha4 * 0.55f),
                            Color.Transparent
                        )
                    )
                )
                .blur(32.dp)
                .align(Alignment.TopEnd)
        )
    }
}

/**
 * Fonction pour obtenir la couleur d'un badge selon sa rareté
 * Utilise les couleurs de la palette personnalisée si disponibles,
 * sinon utilise les couleurs standard du guide
 */
private fun rarityColor(rarity: String, palette: AchievementsPalette): Color {
    return when (rarity.lowercase()) {
        "common" -> palette.accentTeal.takeIf { 
            it != Color.Unspecified 
        } ?: Color(0xFF4CAF50) // Vert selon le guide
        "uncommon" -> palette.accentBlue.takeIf { 
            it != Color.Unspecified 
        } ?: Color(0xFF2196F3) // Bleu selon le guide
        "rare" -> palette.accentPurple.takeIf { 
            it != Color.Unspecified 
        } ?: Color(0xFF9C27B0) // Violet selon le guide
        "epic" -> palette.accentGold.takeIf { 
            it != Color.Unspecified 
        } ?: Color(0xFFFF9800) // Orange selon le guide
        "legendary" -> palette.accentGold.takeIf { 
            it != Color.Unspecified 
        } ?: Color(0xFFFFD700) // Or selon le guide
        else -> palette.secondaryText
    }
}

private val sampleAchievementsOverview = AchievementsOverview(
    stats = AchievementUserStats(
        level = 12,
        xp = 2350,
        nextLevelXp = 3000,
        totalBadges = 18,
        currentStreak = 7,
        longestStreak = 21
    ),
    badges = listOf(
        AchievementBadge(
            id = "badge-1",
            icon = "🏃",
            title = "Marathon Runner",
            description = "Completed 5+ running events",
            category = "Running",
            unlocked = true,
            unlockedDate = "Oct 28, 2025",
            rarity = "rare"
        ),
        AchievementBadge(
            id = "badge-2",
            icon = "🏊",
            title = "Water Warrior",
            description = "Joined 10+ swimming sessions",
            category = "Swimming",
            unlocked = true,
            unlockedDate = "Oct 15, 2025",
            rarity = "common"
        ),
        AchievementBadge(
            id = "badge-3",
            icon = "👥",
            title = "Social Butterfly",
            description = "Connected with 25+ athletes",
            category = "Social",
            unlocked = true,
            unlockedDate = "Oct 10, 2025",
            rarity = "uncommon"
        ),
        AchievementBadge(
            id = "badge-4",
            icon = "⭐",
            title = "Top Host",
            description = "Hosted 10+ successful events",
            category = "Hosting",
            unlocked = true,
            unlockedDate = "Oct 5, 2025",
            rarity = "rare"
        ),
        AchievementBadge(
            id = "badge-5",
            icon = "🔥",
            title = "Consistency King",
            description = "Maintain a 30-day streak",
            category = "Consistency",
            unlocked = false,
            progress = 7,
            total = 30,
            rarity = "epic"
        ),
        AchievementBadge(
            id = "badge-6",
            icon = "🌟",
            title = "Early Bird",
            description = "Join 20 morning sessions",
            category = "Participation",
            unlocked = false,
            progress = 12,
            total = 20,
            rarity = "uncommon"
        )
    ),
    challenges = listOf(
        AchievementChallenge(
            id = "challenge-1",
            title = "Weekend Warrior",
            description = "Complete 4 activities this weekend",
            challengeType = "weekly",
            progress = 2,
            total = 4,
            reward = "100 XP + Weekend Badge",
            deadline = "2 days left",
            daysLeft = 2
        ),
        AchievementChallenge(
            id = "challenge-2",
            title = "Variety Seeker",
            description = "Try 3 different sports this week",
            challengeType = "weekly",
            progress = 1,
            total = 3,
            reward = "150 XP + Explorer Badge",
            deadline = "5 days left",
            daysLeft = 5
        ),
        AchievementChallenge(
            id = "challenge-3",
            title = "Social Sprint",
            description = "Connect with 5 new sport buddies",
            challengeType = "monthly",
            progress = 3,
            total = 5,
            reward = "75 XP",
            deadline = "7 days left",
            daysLeft = 7
        )
    ),
    leaderboard = listOf(
        LeaderboardEntry(rank = 1, name = "You", points = 2350, badge = "🥇"),
        LeaderboardEntry(rank = 2, name = "Sarah M.", points = 2280, badge = "🥈"),
        LeaderboardEntry(rank = 3, name = "Mike R.", points = 2150, badge = "🥉"),
        LeaderboardEntry(rank = 4, name = "Emma L.", points = 2020, badge = ""),
        LeaderboardEntry(rank = 5, name = "Alex T.", points = 1980, badge = "")
    )
)
