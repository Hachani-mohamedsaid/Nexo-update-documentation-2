package com.example.damandroid.presentation.coachprofile.ui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.CoachCourse
import com.example.damandroid.domain.model.CoachProfile
import com.example.damandroid.domain.model.CoachReview
import com.example.damandroid.presentation.coachprofile.model.CoachProfileUiState
import com.example.damandroid.presentation.coachprofile.viewmodel.CoachProfileViewModel
import com.example.damandroid.ui.theme.LocalThemeController
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.RowScope

private data class CoachProfilePalette(
    val isDark: Boolean,
    val background: Color,
    val headerBackground: Color,
    val headerIconTint: Color,
    val headerText: Color,
    val heroGradient: Brush,
    val heroImageBorder: Color,
    val heroVerificationBackground: Color,
    val heroVerificationBorder: Color,
    val heroVerificationIcon: Color,
    val heroPrimaryText: Color,
    val heroSecondaryText: Color,
    val heroMutedText: Color,
    val heroIconTint: Color,
    val heroBadgeBackground: Color,
    val heroBadgeBorder: Color,
    val heroBadgeText: Color,
    val heroFollowSelectedContainer: Color,
    val heroFollowSelectedContent: Color,
    val heroFollowUnselectedContainer: Color,
    val heroFollowUnselectedContent: Color,
    val heroFollowBorder: Color,
    val heroMessageContainer: Color,
    val heroMessageContent: Color,
    val heroMessageIcon: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val tabCardBackground: Color,
    val tabSelectedBackground: Color,
    val tabSelectedText: Color,
    val tabUnselectedText: Color,
    val tabBorder: Color,
    val statValueText: Color,
    val statLabelText: Color,
    val ratingBackground: Color,
    val chipBackground: Color,
    val chipBorder: Color,
    val chipText: Color,
    val sessionIconTint: Color,
    val sessionButtonBackground: Color,
    val sessionButtonText: Color,
    val reviewStarFilled: Color,
    val reviewStarEmpty: Color,
    val emptyStateSecondaryText: Color
)

@Composable
private fun rememberCoachProfilePalette(isDarkMode: Boolean): CoachProfilePalette = remember(isDarkMode) {
    if (!isDarkMode) {
        CoachProfilePalette(
            isDark = false,
            background = Color(0xFFF5F7FA),
            headerBackground = Color.White,
            headerIconTint = Color(0xFF1F2933),
            headerText = Color(0xFF1F2933),
            heroGradient = Brush.linearGradient(listOf(Color(0xFF3498DB), Color(0xFF2ECC71))),
            heroImageBorder = Color.White,
            heroVerificationBackground = Color.White,
            heroVerificationBorder = Color(0xFF2ECC71),
            heroVerificationIcon = Color(0xFF2ECC71),
            heroPrimaryText = Color.White,
            heroSecondaryText = Color.White.copy(alpha = 0.9f),
            heroMutedText = Color.White.copy(alpha = 0.8f),
            heroIconTint = Color.White,
            heroBadgeBackground = Color.White.copy(alpha = 0.2f),
            heroBadgeBorder = Color.White.copy(alpha = 0.3f),
            heroBadgeText = Color.White,
            heroFollowSelectedContainer = Color.White.copy(alpha = 0.2f),
            heroFollowSelectedContent = Color.White,
            heroFollowUnselectedContainer = Color.White,
            heroFollowUnselectedContent = Color(0xFF3498DB),
            heroFollowBorder = Color.White,
            heroMessageContainer = Color.White,
            heroMessageContent = Color(0xFF3498DB),
            heroMessageIcon = Color(0xFF3498DB),
            cardBackground = Color.White,
            cardBorder = Color(0xFFE2E8F0),
            primaryText = Color(0xFF1F2933),
            secondaryText = Color(0xFF4B5563),
            mutedText = Color(0xFF7B8794),
            tabCardBackground = Color.White,
            tabSelectedBackground = Color(0xFF8B5CF6),
            tabSelectedText = Color.White,
            tabUnselectedText = Color(0xFF4A5568),
            tabBorder = Color(0xFFE2E8F0),
            statValueText = Color(0xFF1F2933),
            statLabelText = Color(0xFF7B8794),
            ratingBackground = Color(0xFFFFF8E7),
            chipBackground = Color(0xFF2ECC71).copy(alpha = 0.12f),
            chipBorder = Color.Transparent,
            chipText = Color(0xFF2ECC71),
            sessionIconTint = Color(0xFF718096),
            sessionButtonBackground = Color(0xFF2ECC71),
            sessionButtonText = Color.White,
            reviewStarFilled = Color(0xFFF59E0B),
            reviewStarEmpty = Color(0xFFE2E8F0),
            emptyStateSecondaryText = Color(0xFF7B8794)
        )
    } else {
        CoachProfilePalette(
            isDark = true,
            background = Color(0xFF0B1120),
            headerBackground = Color(0xFF111827),
            headerIconTint = Color(0xFFE2E8F0),
            headerText = Color(0xFFE2E8F0),
            heroGradient = Brush.linearGradient(listOf(Color(0xFF1D4ED8), Color(0xFF059669))),
            heroImageBorder = Color(0xFF93C5FD),
            heroVerificationBackground = Color(0xFF0B1120),
            heroVerificationBorder = Color(0xFF34D399),
            heroVerificationIcon = Color(0xFF34D399),
            heroPrimaryText = Color(0xFFF8FAFC),
            heroSecondaryText = Color(0xFFCBD5F5),
            heroMutedText = Color(0xFF94A3B8),
            heroIconTint = Color(0xFFF8FAFC),
            heroBadgeBackground = Color.White.copy(alpha = 0.12f),
            heroBadgeBorder = Color(0xFF60A5FA).copy(alpha = 0.5f),
            heroBadgeText = Color(0xFFF8FAFC),
            heroFollowSelectedContainer = Color.White.copy(alpha = 0.12f),
            heroFollowSelectedContent = Color(0xFFF8FAFC),
            heroFollowUnselectedContainer = Color(0xFFF8FAFC),
            heroFollowUnselectedContent = Color(0xFF1D4ED8),
            heroFollowBorder = Color.White.copy(alpha = 0.35f),
            heroMessageContainer = Color(0xFFF8FAFC),
            heroMessageContent = Color(0xFF1D4ED8),
            heroMessageIcon = Color(0xFF1D4ED8),
            cardBackground = Color(0xFF111827),
            cardBorder = Color(0xFF1F2937),
            primaryText = Color(0xFFE2E8F0),
            secondaryText = Color(0xFFCBD5F5),
            mutedText = Color(0xFF94A3B8),
            tabCardBackground = Color(0xFF111827),
            tabSelectedBackground = Color(0xFF6366F1),
            tabSelectedText = Color.White,
            tabUnselectedText = Color(0xFF94A3B8),
            tabBorder = Color(0xFF1F2937),
            statValueText = Color(0xFFF8FAFC),
            statLabelText = Color(0xFF94A3B8),
            ratingBackground = Color(0xFF1E293B),
            chipBackground = Color(0xFF047857).copy(alpha = 0.35f),
            chipBorder = Color(0xFF10B981).copy(alpha = 0.5f),
            chipText = Color(0xFF34D399),
            sessionIconTint = Color(0xFF93C5FD),
            sessionButtonBackground = Color(0xFF10B981),
            sessionButtonText = Color.White,
            reviewStarFilled = Color(0xFFF59E0B),
            reviewStarEmpty = Color(0xFF334155),
            emptyStateSecondaryText = Color(0xFF94A3B8)
        )
    }
}

@Composable
fun CoachProfileRoute(
    coachId: String,
    viewModel: CoachProfileViewModel,
    onBack: (() -> Unit)? = null,
    onMessage: (() -> Unit)? = null,
    onBookSession: ((CoachCourse) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(coachId) {
        viewModel.load(coachId)
    }
    val uiState by viewModel.uiState.collectAsState()
    CoachProfileScreen(
        state = uiState,
        onBack = onBack,
        onMessage = onMessage,
        onBookSession = onBookSession,
        modifier = modifier
    )
}

@Composable
fun CoachProfileScreen(
    state: CoachProfileUiState,
    onBack: (() -> Unit)? = null,
    onMessage: (() -> Unit)? = null,
    onBookSession: ((CoachCourse) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(state.error, modifier)
        state.profile != null -> CoachProfileContent(
            profile = state.profile,
            onBack = onBack,
            onMessage = onMessage,
            onBookSession = onBookSession,
            modifier = modifier
        )
        else -> ErrorState("Coach profile unavailable", modifier)
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message)
    }
}

private enum class CoachTab(val label: String) { ABOUT("About"), SESSIONS("Sessions"), REVIEWS("Reviews") }

@Composable
private fun CoachProfileContent(
    profile: CoachProfile,
    onBack: (() -> Unit)? = null,
    onMessage: (() -> Unit)? = null,
    onBookSession: ((CoachCourse) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(CoachTab.ABOUT) }
    var isFollowing by remember { mutableStateOf(false) }
    val themeController = LocalThemeController.current
    val palette = rememberCoachProfilePalette(themeController.isDarkMode)

    Box(modifier = modifier.fillMaxSize().background(palette.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                CoachHeader(onBack, palette)
            }
            item {
                CoachHeroSection(
                    profile = profile,
                    isFollowing = isFollowing,
                    onToggleFollow = { isFollowing = !isFollowing },
                    onMessage = onMessage,
                    palette = palette
                )
            }
            item {
                CoachStatsRow(profile, palette)
            }
            item {
                CoachTabBar(selectedTab = selectedTab, onSelect = { selectedTab = it }, palette = palette)
            }
            when (selectedTab) {
                CoachTab.ABOUT -> {
                    item { AboutSection(profile, palette) }
                    item { SpecializationSection(profile.specializations, palette) }
                    item { CertificationSection(profile, palette) }
                }
                CoachTab.SESSIONS -> {
                    if (profile.courses.isEmpty()) {
                        item { EmptyStateCard("No upcoming sessions", palette) }
                    } else {
                        items(profile.courses, key = { it.id }) { course ->
                            SessionCard(course = course, onBook = onBookSession, palette = palette)
                        }
                    }
                }
                CoachTab.REVIEWS -> {
                    item { RatingSummary(profile.rating, profile.reviewsCount, palette) }
                    if (profile.reviews.isEmpty()) {
                        item { EmptyStateCard("No reviews yet", palette) }
                    } else {
                        items(profile.reviews, key = { it.id }) { review ->
                            ReviewCard(review, palette)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachHeader(onBack: (() -> Unit)?, palette: CoachProfilePalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.headerBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onBack?.invoke() }, enabled = onBack != null) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = palette.headerIconTint)
        }
        Text(text = "Coach Profile", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = palette.headerText)
        IconButton(onClick = { /* TODO share */ }) {
            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = palette.headerIconTint)
        }
    }
}

@Composable
private fun CoachHeroSection(
    profile: CoachProfile,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    onMessage: (() -> Unit)?,
    palette: CoachProfilePalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.heroGradient)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = profile.name,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(4.dp, palette.heroImageBorder, CircleShape)
            )
            if (profile.isVerified) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = palette.heroVerificationBackground,
                    border = BorderStroke(2.dp, palette.heroVerificationBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = palette.heroVerificationIcon,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = palette.heroPrimaryText)
            if (profile.isVerified) {
                Surface(shape = RoundedCornerShape(20.dp), color = palette.heroBadgeBackground, border = BorderStroke(1.dp, palette.heroBadgeBorder)) {
                    Text(
                        text = "✓ Verified Coach",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.heroBadgeText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = palette.heroIconTint)
                Text(text = "${profile.rating}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.heroPrimaryText)
                Text(text = "(${profile.reviewsCount} reviews)", fontSize = 12.sp, color = palette.heroMutedText)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = palette.heroIconTint, modifier = Modifier.size(16.dp))
                Text(text = profile.location, fontSize = 13.sp, color = palette.heroSecondaryText)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onToggleFollow,
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isFollowing) palette.heroFollowSelectedContainer else palette.heroFollowUnselectedContainer,
                    contentColor = if (isFollowing) palette.heroFollowSelectedContent else palette.heroFollowUnselectedContent
                ),
                border = BorderStroke(1.5.dp, palette.heroFollowBorder)
            ) {
                Icon(
                    imageVector = if (isFollowing) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFollowing) palette.heroFollowSelectedContent else palette.heroFollowUnselectedContent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = if (isFollowing) "Following" else "Follow", fontSize = 14.sp)
            }
            Button(
                onClick = { onMessage?.invoke() },
                modifier = Modifier.weight(1f).height(42.dp),
                enabled = onMessage != null,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.heroMessageContainer, contentColor = palette.heroMessageContent)
            ) {
                Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = palette.heroMessageIcon)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Message", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun CoachStatsRow(profile: CoachProfile, palette: CoachProfilePalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RowScopeStatCard(value = profile.sessionsCount.toString(), label = "Sessions", palette = palette)
        RowScopeStatCard(value = profile.followersCount.toString(), label = "Followers", palette = palette)
        RowScopeStatCard(value = profile.experienceYears, label = "Experience", palette = palette)
    }
}

@Composable
private fun RowScope.RowScopeStatCard(value: String, label: String, palette: CoachProfilePalette) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.statValueText)
            Text(text = label, fontSize = 11.sp, color = palette.statLabelText)
        }
    }
}

@Composable
private fun CoachTabBar(selectedTab: CoachTab, onSelect: (CoachTab) -> Unit, palette: CoachProfilePalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.tabCardBackground),
        border = BorderStroke(1.dp, palette.tabBorder),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CoachTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(tab) },
                    color = if (isSelected) palette.tabSelectedBackground else Color.Transparent,
                    border = BorderStroke(1.dp, if (isSelected) palette.tabSelectedBackground else Color.Transparent)
                ) {
                    Text(
                        text = tab.label,
                        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) palette.tabSelectedText else palette.tabUnselectedText
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSection(profile: CoachProfile, palette: CoachProfilePalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "About", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
            Text(text = profile.bio, fontSize = 13.sp, color = palette.secondaryText)
        }
    }
}

@Composable
private fun SpecializationSection(specializations: List<String>, palette: CoachProfilePalette) {
    if (specializations.isEmpty()) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Specializations", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                specializations.forEach { spec ->
                    Surface(shape = RoundedCornerShape(50), color = palette.chipBackground, border = BorderStroke(1.dp, palette.chipBorder)) {
                        Text(
                            text = spec,
                            fontSize = 12.sp,
                            color = palette.chipText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificationSection(profile: CoachProfile, palette: CoachProfilePalette) {
    if (profile.certifications.isEmpty()) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = "Certifications", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
            profile.certifications.forEach { certification ->
                Text(text = "• ${certification.name}", fontSize = 13.sp, color = palette.secondaryText)
            }
        }
    }
}

@Composable
private fun SessionCard(course: CoachCourse, onBook: ((CoachCourse) -> Unit)?, palette: CoachProfilePalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = course.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = palette.sessionIconTint, modifier = Modifier.size(16.dp))
                Text(text = "${course.date} • ${course.time}", fontSize = 12.sp, color = palette.secondaryText)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = palette.sessionIconTint, modifier = Modifier.size(16.dp))
                Text(text = course.location, fontSize = 12.sp, color = palette.secondaryText)
            }
            Text(text = course.description, fontSize = 12.sp, color = palette.mutedText, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Button(
                onClick = { onBook?.invoke(course) },
                enabled = onBook != null,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.sessionButtonBackground, contentColor = palette.sessionButtonText)
            ) {
                Text(text = "Book Session", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RatingSummary(rating: Double, totalReviews: Int, palette: CoachProfilePalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.ratingBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = String.format("%.1f", rating), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = palette.primaryText)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(5) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = if (it < rating) palette.reviewStarFilled else palette.reviewStarEmpty)
                    }
                }
                Text(text = "$totalReviews reviews", fontSize = 11.sp, color = palette.mutedText)
            }
        }
    }
}

@Composable
private fun ReviewCard(review: CoachReview, palette: CoachProfilePalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = review.reviewerName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.primaryText)
                    Text(text = review.date, fontSize = 11.sp, color = palette.mutedText)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = palette.reviewStarFilled)
                    Text(text = review.rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
                }
            }
            Text(text = review.comment, fontSize = 13.sp, color = palette.secondaryText)
        }
    }
}

@Composable
private fun EmptyStateCard(message: String, palette: CoachProfilePalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground),
        border = BorderStroke(1.dp, palette.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = message, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.primaryText)
            Text(text = "Check back later for updates.", fontSize = 12.sp, color = palette.emptyStateSecondaryText)
        }
    }
}
