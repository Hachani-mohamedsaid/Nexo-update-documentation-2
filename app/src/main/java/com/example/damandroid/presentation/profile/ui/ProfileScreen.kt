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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.damandroid.domain.model.MedalRarity
import com.example.damandroid.domain.model.ProfileActivity
import com.example.damandroid.domain.model.ProfileMedal
import com.example.damandroid.domain.model.UserProfile
import com.example.damandroid.domain.model.UserStatsOverview
import com.example.damandroid.presentation.profile.model.ProfileTab
import com.example.damandroid.presentation.profile.model.ProfileUiState
import com.example.damandroid.presentation.profile.viewmodel.ProfileViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    onSettingsClick: () -> Unit,
    onAchievementsClick: (() -> Unit)?,
    onLogoutClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    ProfileScreen(
        state = uiState,
        onSettingsClick = onSettingsClick,
        onAchievementsClick = onAchievementsClick,
        onLogoutClick = onLogoutClick,
        onTabSelected = viewModel::onTabSelected,
        onRefresh = viewModel::refresh,
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onSettingsClick: () -> Unit,
    onAchievementsClick: (() -> Unit)?,
    onLogoutClick: (() -> Unit)?,
    onTabSelected: (ProfileTab) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(message = state.error, onRetry = onRefresh, modifier = modifier)
        state.profile != null -> ContentState(
            state = state,
            onSettingsClick = onSettingsClick,
            onAchievementsClick = onAchievementsClick,
            onLogoutClick = onLogoutClick,
            onTabSelected = onTabSelected,
            modifier = modifier
        )
        else -> ErrorState(
            message = "Profile unavailable",
            onRetry = onRefresh,
            modifier = modifier
        )
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
        Text(text = message)
    }
}

@Composable
private fun ContentState(
    state: ProfileUiState,
    onSettingsClick: () -> Unit,
    onAchievementsClick: (() -> Unit)?,
    onLogoutClick: (() -> Unit)?,
    onTabSelected: (ProfileTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = state.profile ?: return
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
                ProfileHeader(
                    colors = colors,
                    onSettingsClick = onSettingsClick
                )
            }

            item {
                ProfileInfoCard(
                    profile = profile,
                    colors = colors,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (onAchievementsClick != null && profile.achievements.isNotEmpty()) {
                item {
                    AchievementsButton(
                        colors = colors,
                        achievementsCount = profile.achievements.size,
                        onClick = onAchievementsClick,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }


            item {
                ProfileTabs(
                    profile = profile,
                    selectedTab = state.selectedTab,
                    onTabSelected = onTabSelected,
                    colors = colors,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    colors: ProfileColors,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Dashboard",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText,
            letterSpacing = (-0.5).sp
        )
        Button(
            onClick = onSettingsClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.cardBackground
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, colors.cardBorder)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = colors.primaryText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.primaryText
            )
        }
    }
}

@Composable
internal fun FloatingProfileOrbs(colors: ProfileColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "profile-orbs")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )
    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse3"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 80.dp)
                .size(128.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.accentPurple.copy(alpha = pulse1 * colors.orbAlpha),
                            colors.accentPink.copy(alpha = pulse1 * colors.orbAlpha),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
                .blur(48.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-160).dp)
                .size(160.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.accentBlue.copy(alpha = pulse2 * colors.orbAlpha),
                            colors.accentPurple.copy(alpha = pulse2 * colors.orbAlpha),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
                .blur(48.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(96.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.accentPink.copy(alpha = pulse3 * colors.orbAlpha),
                            colors.accentPurple.copy(alpha = pulse3 * colors.orbAlpha),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
                .blur(32.dp)
        )
    }
}

@Composable
internal fun ProfileInfoCard(
    profile: UserProfile,
    colors: ProfileColors,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 2.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colors.accentPurple.copy(alpha = colors.overlayAlpha),
                            colors.accentPink.copy(alpha = colors.overlayAlpha),
                            colors.accentBlue.copy(alpha = colors.overlayAlpha)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .blur(16.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = colors.cardBackground,
            border = BorderStroke(2.dp, colors.cardBorder),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(4.dp, colors.cardBorder, CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        colors.accentPurple.copy(alpha = colors.overlayAlpha),
                                        colors.cardBackground,
                                        colors.accentPink.copy(alpha = colors.overlayAlpha)
                                    )
                                )
                            )
                            .blur(16.dp)
                    )
                    AsyncImage(
                        model = profile.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(4.dp, colors.cardBorder, CircleShape)
                    )
                    if (profile.isVerified) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp),
                            shape = CircleShape,
                            color = colors.accentPurple,
                            border = BorderStroke(2.dp, colors.cardBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Verified",
                                tint = colors.iconOnAccent,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = profile.bio,
                    fontSize = 14.sp,
                    color = colors.secondaryText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.chipBackground,
                    border = BorderStroke(2.dp, colors.chipBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = colors.secondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = profile.location,
                            fontSize = 14.sp,
                            color = colors.secondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                StatsRow(profile.stats, colors)
            }
        }
    }
}

@Composable
private fun StatsRow(stats: UserStatsOverview, colors: ProfileColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatPill(
            value = stats.sessionsJoined.toString(),
            label = "Joined",
            colors = colors,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            value = stats.sessionsHosted.toString(),
            label = "Hosted",
            colors = colors,
            modifier = Modifier.weight(1f)
        )
        // Use rating from backend if available, otherwise calculate from followers
        val rating = stats.rating?.toFloat() ?: when {
            stats.followers > 0 -> (stats.followers.coerceAtMost(50) / 10f + 4f).coerceAtMost(5f)
            else -> 0f
        }
        StatPill(
            value = if (rating > 0) "⭐ ${"%.1f".format(rating)}" else "⭐ 0.0",
            label = "Rating",
            colors = colors,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatPill(
    value: String,
    label: String,
    colors: ProfileColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = colors.chipBackground,
        border = BorderStroke(1.dp, colors.chipBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryText
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = colors.secondaryText,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun AchievementsButton(
    colors: ProfileColors,
    achievementsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 2.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colors.accentGold.copy(alpha = colors.overlayAlpha + 0.1f),
                            colors.accentPink.copy(alpha = colors.overlayAlpha + 0.1f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .blur(16.dp)
        )
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = colors.cardBackground,
            border = BorderStroke(2.dp, colors.cardBorder),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.accentGold,
                    border = BorderStroke(2.dp, colors.cardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = colors.iconOnAccent,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Achievements",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                    Text(
                        text = "View badges & rewards",
                        fontSize = 12.sp,
                        color = colors.secondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun LogoutButton(
    colors: ProfileColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 2.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colors.destructive.copy(alpha = colors.overlayAlpha + 0.1f),
                            colors.destructiveBorder.copy(alpha = colors.overlayAlpha + 0.1f)
                        )
                    ),
                    RoundedCornerShape(24.dp)
                )
                .blur(16.dp)
        )
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = colors.cardBackground,
            border = BorderStroke(2.dp, colors.destructiveBorder),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.destructive,
                    border = BorderStroke(2.dp, colors.cardBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = colors.iconOnAccent,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Logout",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.destructiveText
                    )
                    Text(
                        text = "Sign out of your account",
                        fontSize = 12.sp,
                        color = colors.secondaryText
                    )
                }
            }
        }
    }
}

@Composable
internal fun ProfileTabs(
    profile: UserProfile,
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit,
    colors: ProfileColors,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = colors.cardBackground,
            border = BorderStroke(2.dp, colors.cardBorder),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ProfileTabButton(
                    text = "About",
                    isSelected = selectedTab == ProfileTab.ABOUT,
                    onClick = { onTabSelected(ProfileTab.ABOUT) },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                ProfileTabButton(
                    text = "Activities",
                    isSelected = selectedTab == ProfileTab.ACTIVITIES,
                    onClick = { onTabSelected(ProfileTab.ACTIVITIES) },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                ProfileTabButton(
                    text = "Medals",
                    isSelected = selectedTab == ProfileTab.MEDALS,
                    onClick = { onTabSelected(ProfileTab.MEDALS) },
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            ProfileTab.ABOUT -> AboutTabContent(profile = profile, colors = colors)
            ProfileTab.ACTIVITIES -> ActivitiesTabContent(activities = profile.activities, colors = colors)
            ProfileTab.MEDALS -> MedalsTabContent(medals = profile.medals, colors = colors)
        }
    }
}

@Composable
private fun ProfileTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: ProfileColors,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) colors.tabSelectedBackground else Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isSelected) colors.tabSelectedText else colors.tabUnselectedText,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun AboutTabContent(
    profile: UserProfile,
    colors: ProfileColors
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoCard(
            title = "Interests",
            colors = colors
        ) {
            val interests = buildString {
                if (profile.stats.favoriteSports.isNotEmpty()) {
                    append(profile.stats.favoriteSports.joinToString(" • "))
                }
                if (profile.bio.isNotBlank()) {
                    if (isNotEmpty()) append(" • ")
                    append(profile.bio)
                }
            }.ifBlank { "Outdoor activities • Fitness challenges • Meeting new people" }
            Text(
                text = interests,
                fontSize = 12.sp,
                color = colors.secondaryText,
                lineHeight = 18.sp
            )
        }

        InfoCard(
            title = "Skill Levels",
            colors = colors
        ) {
            val skills = profile.stats.favoriteSports.ifEmpty { listOf("Running", "Swimming", "Hiking") }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                skills.forEachIndexed { index, sport ->
                    val defaultLevel = when (index % 3) {
                        0 -> "Intermediate"
                        1 -> "Advanced"
                        else -> "Beginner"
                    }
                    SkillLevelRow(sport, defaultLevel, colors)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    colors: ProfileColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.cardBackground,
        border = BorderStroke(2.dp, colors.cardBorder),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.primaryText,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SkillLevelRow(
    sport: String,
    level: String,
    colors: ProfileColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sport,
            fontSize = 13.sp,
            color = colors.primaryText
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.chipBackground,
            border = BorderStroke(2.dp, colors.chipBorder)
        ) {
            Text(
                text = level,
                fontSize = 11.sp,
                color = colors.primaryText,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ActivitiesTabContent(
    activities: List<ProfileActivity>,
    colors: ProfileColors
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Recent Activities",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.primaryText,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        if (activities.isEmpty()) {
            Text(
                text = "No activities yet. Join or host a session to see it here.",
                fontSize = 12.sp,
                color = colors.secondaryText
            )
        } else {
            activities.forEach { activity ->
                ActivityCard(activity = activity, colors = colors)
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: ProfileActivity,
    colors: ProfileColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.cardBackground,
        border = BorderStroke(2.dp, colors.cardBorder),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(16.dp),
                color = colors.chipBackground,
                border = BorderStroke(2.dp, colors.chipBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activity.sportIcon,
                        fontSize = 24.sp
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.primaryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = colors.secondaryText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = activity.date,
                        fontSize = 12.sp,
                        color = colors.secondaryText
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = colors.secondaryText
                    )
                    Text(
                        text = activity.time,
                        fontSize = 12.sp,
                        color = colors.secondaryText
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activity.location,
                    fontSize = 12.sp,
                    color = colors.secondaryText
                )
            }
        }
    }
}

@Composable
private fun MedalsTabContent(
    medals: List<ProfileMedal>,
    colors: ProfileColors
) {
    if (medals.isEmpty()) {
        InfoCard(
            title = "Medals",
            colors = colors
        ) {
            Text(
                text = "Keep participating to earn your first medal!",
                fontSize = 12.sp,
                color = colors.secondaryText
            )
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        medals.forEach { medal ->
            MedalCard(medal = medal, colors = colors)
        }
    }
}

@Composable
private fun MedalCard(
    medal: ProfileMedal,
    colors: ProfileColors
) {
    val rarityColor = when (medal.rarity) {
        MedalRarity.COMMON -> colors.accentBlue
        MedalRarity.UNCOMMON -> colors.accentBlue  // Utiliser la même couleur que COMMON ou une couleur intermédiaire
        MedalRarity.RARE -> colors.accentPurple
        MedalRarity.LEGENDARY -> colors.accentGold
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.cardBackground,
        border = BorderStroke(2.dp, colors.cardBorder),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = rarityColor.copy(alpha = if (colors.isDarkMode) 0.35f else 0.3f),
                border = BorderStroke(2.dp, colors.chipBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = medal.icon,
                        fontSize = 28.sp
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medal.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.primaryText
                )
                Text(
                    text = medal.description,
                    fontSize = 12.sp,
                    color = colors.secondaryText
                )
            }
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = rarityColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

internal data class ProfileColors(
    val isDarkMode: Boolean,
    val backgroundBrush: Brush,
    val primaryText: Color,
    val secondaryText: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val chipBackground: Color,
    val chipBorder: Color,
    val accentPurple: Color,
    val accentPink: Color,
    val accentBlue: Color,
    val accentGreen: Color,
    val accentGold: Color,
    val destructive: Color,
    val destructiveBorder: Color,
    val destructiveText: Color,
    val iconOnAccent: Color,
    val tabSelectedBackground: Color,
    val tabSelectedText: Color,
    val tabUnselectedText: Color,
    val overlayAlpha: Float,
    val orbAlpha: Float
)

@Composable
internal fun rememberProfileColors(appTheme: AppThemeColors): ProfileColors {
    return remember(appTheme) {
        val isDarkMode = appTheme.isDark
        ProfileColors(
            isDarkMode = isDarkMode,
            backgroundBrush = appTheme.backgroundGradient,
            primaryText = appTheme.primaryText,
            secondaryText = appTheme.secondaryText,
            cardBackground = appTheme.cardSurface,
            cardBorder = appTheme.cardBorder,
            chipBackground = appTheme.subtleSurface,
            chipBorder = appTheme.subtleBorder,
            accentPurple = appTheme.accentPurple,
            accentPink = appTheme.accentPink,
            accentBlue = appTheme.accentBlue,
            accentGreen = appTheme.accentGreen,
            accentGold = appTheme.accentGold,
            destructive = appTheme.danger,
            destructiveBorder = if (isDarkMode) appTheme.danger.copy(alpha = 0.6f) else appTheme.danger.copy(alpha = 0.4f),
            destructiveText = appTheme.danger,
            iconOnAccent = appTheme.iconOnAccent,
            tabSelectedBackground = appTheme.accentPurple,
            tabSelectedText = appTheme.iconOnAccent,
            tabUnselectedText = appTheme.mutedText,
            overlayAlpha = if (isDarkMode) 0.24f else 0.18f,
            orbAlpha = if (isDarkMode) 0.45f else 0.35f
        )
    }
}

