package com.example.damandroid.presentation.eventdetails.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.EventDetails
import com.example.damandroid.presentation.eventdetails.model.EventDetailsUiState
import com.example.damandroid.presentation.eventdetails.viewmodel.EventDetailsViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

@Composable
fun EventDetailsRoute(
    eventId: String,
    viewModel: EventDetailsViewModel,
    onBack: (() -> Unit)? = null,
    onJoin: (() -> Unit)? = null,
    onViewCoach: (() -> Unit)? = null,
    onMessageCoach: (() -> Unit)? = null,
    onCoachProfile: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(eventId) { viewModel.load(eventId) }
    val uiState by viewModel.uiState.collectAsState()
    EventDetailsScreen(
        state = uiState,
        onBack = onBack,
        onJoin = onJoin,
        onViewCoach = onViewCoach,
        onMessageCoach = onMessageCoach,
        onCoachProfile = onCoachProfile,
        modifier = modifier
    )
}

@Composable
fun EventDetailsScreen(
    state: EventDetailsUiState,
    onBack: (() -> Unit)? = null,
    onJoin: (() -> Unit)? = null,
    onViewCoach: (() -> Unit)? = null,
    onMessageCoach: (() -> Unit)? = null,
    onCoachProfile: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(state.error, modifier)
        else -> {
            val event = state.event?.toLegacyEvent() ?: sampleLegacyEvent
            EventDetailsContent(
                event = event,
                onBack = onBack,
                onJoin = onJoin,
                onViewCoach = onViewCoach,
                onMessageCoach = onMessageCoach,
                onCoachProfile = onCoachProfile,
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
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, textAlign = TextAlign.Center)
    }
}

@Composable
private fun EventDetailsContent(
    event: LegacyEventDetails,
    onBack: (() -> Unit)?,
    onJoin: (() -> Unit)?,
    onViewCoach: (() -> Unit)?,
    onMessageCoach: (() -> Unit)?,
    onCoachProfile: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val theme = rememberAppThemeColors(themeController.isDarkMode)
    val background = if (theme.isDark) {
        theme.backgroundGradient
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF5F3FF),
                Color(0xFFFDF4FF),
                Color(0xFFF0F4F8)
            )
        )
    }

    var isSaved by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("details") }

    val participants = remember(event.participants) {
        if (event.participants.isNotEmpty()) event.participants else sampleParticipants
    }
    val reviews = remember(event.reviews) {
        if (event.reviews.isNotEmpty()) event.reviews else sampleReviews
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            EventHeader(
                event = event,
                isSaved = isSaved,
                onBack = onBack,
                onToggleSaved = { isSaved = !isSaved },
                theme = theme
            )

            EventSummaryCard(event = event, theme = theme)

            Spacer(modifier = Modifier.height(12.dp))

            CoachCard(
                coach = event.coach,
                theme = theme,
                onViewCoach = onViewCoach,
                onMessage = onMessageCoach,
                onCoachProfile = onCoachProfile
            )

            Spacer(modifier = Modifier.height(12.dp))

            TabRow(
                selected = selectedTab,
                onTabSelected = { selectedTab = it },
                theme = theme
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == "details") {
                    item { AvailabilityCard(event = event, theme = theme) }

                    item {
                        SectionCard(title = "About this session", theme = theme) {
                            Text(
                                text = event.description,
                                fontSize = 13.sp,
                                color = theme.primaryText,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    if (event.requirements.isNotEmpty()) {
                        item {
                            SectionCard(title = "What to bring", theme = theme) {
                                RequirementList(requirements = event.requirements, theme = theme)
                            }
                        }
                    }
                }

                if (selectedTab == "participants") {
                    item {
                        SectionCard(title = "Participants", theme = theme) {
                            ParticipantList(participants = participants, theme = theme)
                        }
                    }
                }

                if (selectedTab == "reviews") {
                    item {
                        SectionCard(title = "Reviews", theme = theme) {
                            ReviewList(reviews = reviews, theme = theme)
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = theme.glassSurface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, theme.glassBorder),
                    modifier = Modifier
                        .width(80.dp)
                        .height(56.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total", fontSize = 11.sp, color = theme.mutedText)
                        val priceLabel = if (event.price > 0) "$${event.price}" else "Free"
                        Text(text = priceLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = theme.accentPurple)
                    }
                }

                Button(
                    onClick = { onJoin?.invoke() },
                    enabled = onJoin != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6A38)
                    )
                ) {
                    Text("Book Now", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EventHeader(
    event: LegacyEventDetails,
    isSaved: Boolean,
    onBack: (() -> Unit)?,
    onToggleSaved: () -> Unit,
    theme: AppThemeColors
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = theme.glassSurface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = theme.primaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = event.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryText
                    )
                    Text(
                        text = event.sportType,
                        fontSize = 13.sp,
                        color = theme.mutedText
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onToggleSaved, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) theme.danger else theme.mutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { /* Share */ }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = theme.primaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { /* More */ }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = theme.primaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabRow(
    selected: String,
    onTabSelected: (String) -> Unit,
    theme: AppThemeColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("details", "participants", "reviews").forEach { tab ->
            TabChip(
                text = tab.replaceFirstChar { it.uppercase() },
                selected = selected == tab,
                onClick = { onTabSelected(tab) },
                theme = theme
            )
        }
    }
}

@Composable
private fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    theme: AppThemeColors
) {
    val background = if (selected) theme.accentPurple else theme.glassSurface
    val borderColor = if (selected) theme.accentPurple else theme.glassBorder
    val textColor = if (selected) theme.iconOnAccent else theme.primaryText

    Surface(
        modifier = Modifier
            .width(110.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = background,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
private fun EventSummaryCard(event: LegacyEventDetails, theme: AppThemeColors) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            theme.accentPurple,
                            theme.accentPink
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text(text = event.sportIcon, fontSize = 28.sp) }
                    }
                    Column {
                        Text(
                            text = event.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = event.sportType,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Hosted by Verified Coach",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientInfoPill(
                            label = "Date",
                            value = event.date,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                        GradientInfoPill(
                            label = "Time",
                            value = event.time,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GradientInfoPill(
                            label = "Location",
                            value = event.distance,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                        GradientInfoPill(
                            label = "Price",
                            value = if (event.price <= 0) "Free" else "$${event.price}",
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GradientInfoPill(
    label: String,
    value: String,
    theme: AppThemeColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
private fun AvailabilityCard(event: LegacyEventDetails, theme: AppThemeColors) {
    val spotsLeft = event.maxParticipants - event.currentParticipants
    val fill = if (event.maxParticipants == 0) 0f else event.currentParticipants.toFloat() / event.maxParticipants.toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Availability",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.primaryText,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = theme.glassSurface,
            border = BorderStroke(2.dp, theme.glassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${event.currentParticipants}/${event.maxParticipants} joined",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.primaryText
                    )
                    Text(
                        text = "$spotsLeft spots left",
                        fontSize = 12.sp,
                        color = theme.mutedText
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.subtleSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fill)
                            .height(10.dp)
                            .background(theme.accentGreen)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    theme: AppThemeColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.primaryText,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = theme.glassSurface,
            border = BorderStroke(2.dp, theme.glassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
private fun RequirementList(
    requirements: List<String>,
    theme: AppThemeColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        requirements.forEach { requirement ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(theme.subtleSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "•", fontSize = 18.sp, color = theme.accentPurple)
                }
                Text(text = requirement, fontSize = 13.sp, color = theme.primaryText)
            }
        }
    }
}

@Composable
private fun CoachCard(
    coach: LegacyCoach,
    theme: AppThemeColors,
    onMessage: (() -> Unit)? = null,
    onViewCoach: (() -> Unit)? = null,
    onCoachProfile: ((String) -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, theme.glassBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        AsyncImage(
            model = coach.avatar,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.dp, theme.glassBorder, CircleShape)
        )

            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = coach.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.primaryText
                    )
                    if (coach.isVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = theme.success,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = coach.bio,
                    fontSize = 12.sp,
                    color = theme.mutedText,
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "⭐ ${coach.rating} (${coach.totalReviews} reviews)",
                        fontSize = 12.sp,
                        color = theme.mutedText
                    )
                    if (coach.certifications.isNotEmpty()) {
                        Text(
                            text = coach.certifications.joinToString(", "),
                            fontSize = 12.sp,
                            color = theme.mutedText
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onCoachProfile?.invoke(coach.id) ?: onViewCoach?.invoke()
                    },
                    enabled = onCoachProfile != null || onViewCoach != null,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = theme.primaryText),
                    border = BorderStroke(2.dp, theme.glassBorder)
                ) {
                    Text("View Profile", fontSize = 12.sp)
                }
                Button(
                    onClick = { onMessage?.invoke() },
                    enabled = onMessage != null,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentPurple)
                ) {
                    Text("Message", fontSize = 12.sp, color = theme.iconOnAccent)
                }
            }
        }
    }
}

@Composable
private fun ParticipantList(
    participants: List<LegacyParticipant>,
    theme: AppThemeColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        participants.forEach { participant ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = participant.avatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, theme.glassBorder, CircleShape)
                    )
                    Text(text = participant.name, fontSize = 13.sp, color = theme.primaryText)
                }
                Button(
                    onClick = { /* message participant */ },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.accentPurple)
                ) {
                    Text("Message", fontSize = 12.sp, color = theme.iconOnAccent)
                }
            }
        }
    }
}

@Composable
private fun ReviewList(
    reviews: List<LegacyReview>,
    theme: AppThemeColors
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        reviews.forEach { review ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = review.userAvatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(2.dp, theme.glassBorder, CircleShape)
                    )
                    Column {
                        Text(
                            text = review.userName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.primaryText
                        )
                        Text(text = review.date, fontSize = 11.sp, color = theme.mutedText)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(review.rating) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = theme.accentGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(text = review.comment, fontSize = 12.sp, color = theme.primaryText)
            }
        }
    }
}

// region data conversion & samples

private data class LegacyEventDetails(
    val id: String,
    val title: String,
    val sportIcon: String,
    val sportType: String,
    val date: String,
    val time: String,
    val duration: String,
    val location: String,
    val distance: String,
    val price: Int,
    val type: EventType,
    val level: String,
    val maxParticipants: Int,
    val currentParticipants: Int,
    val description: String,
    val requirements: List<String>,
    val coach: LegacyCoach,
    val participants: List<LegacyParticipant>,
    val reviews: List<LegacyReview>
)

private data class LegacyCoach(
    val id: String,
    val name: String,
    val avatar: String,
    val isVerified: Boolean,
    val rating: Double,
    val totalReviews: Int,
    val bio: String,
    val certifications: List<String>
)

private data class LegacyParticipant(
    val id: String,
    val name: String,
    val avatar: String
)

private data class LegacyReview(
    val id: String,
    val userName: String,
    val userAvatar: String,
    val rating: Int,
    val comment: String,
    val date: String
)

private enum class EventType(val label: String) { FREE("Free"), PAID("Paid"), MEMBERS("Members") }

private val sampleLegacyEvent = LegacyEventDetails(
    id = "legacy-event-1",
    title = "Morning HIIT Bootcamp",
    sportIcon = "🏃",
    sportType = "HIIT Training",
    date = "Nov 5, 2025",
    time = "7:00 AM",
    duration = "60 min",
    location = "Central Park - Main Field",
    distance = "2.1 mi away",
    price = 25,
    type = EventType.PAID,
    level = "Intermediate",
    maxParticipants = 12,
    currentParticipants = 8,
    description = "High-intensity interval training session focused on building strength and endurance. Perfect for all fitness levels with modifications available. Bring water and a workout mat!",
    requirements = listOf("Yoga mat", "Water bottle", "Athletic shoes"),
    coach = LegacyCoach(
        id = "coach_1",
        name = "Mia Johnson",
        avatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Mia",
        isVerified = true,
        rating = 4.8,
        totalReviews = 124,
        bio = "Certified personal trainer with 8+ years of experience",
        certifications = listOf("NASM-CPT", "ACE")
    ),
    participants = listOf(
        LegacyParticipant("1", "Sarah M.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah"),
        LegacyParticipant("2", "Mike R.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Mike"),
        LegacyParticipant("3", "Emma L.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Emma"),
        LegacyParticipant("4", "John D.", "https://api.dicebear.com/7.x/avataaars/svg?seed=John"),
        LegacyParticipant("5", "Lisa K.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Lisa")
    ),
    reviews = listOf(
        LegacyReview(
            id = "review-1",
            userName = "Sarah M.",
            userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah",
            rating = 5,
            comment = "Excellent workout! Alex is very motivating and adjusts exercises for different levels.",
            date = "Oct 28, 2025"
        ),
        LegacyReview(
            id = "review-2",
            userName = "Mike R.",
            userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Mike",
            rating = 5,
            comment = "Great session, challenging but fun. Highly recommend!",
            date = "Oct 25, 2025"
        )
    )
)

private val sampleParticipants = listOf(
    

    LegacyParticipant("1", "Sarah M.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah"),
    LegacyParticipant("2", "Mike R.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Mike"),
    LegacyParticipant("3", "Emma L.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Emma"),
    LegacyParticipant("4", "John D.", "https://api.dicebear.com/7.x/avataaars/svg?seed=John"),
    LegacyParticipant("5", "Lisa K.", "https://api.dicebear.com/7.x/avataaars/svg?seed=Lisa")
)

private val sampleReviews = listOf(
    LegacyReview(
        id = "review-1",
        userName = "Sarah M.",
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Sarah",
        rating = 5,
        comment = "Excellent workout! Alex is very motivating and adjusts exercises for different levels.",
        date = "Oct 28, 2025"
    ),
    LegacyReview(
        id = "review-2",
        userName = "Mike R.",
        userAvatar = "https://api.dicebear.com/7.x/avataaars/svg?seed=Mike",
        rating = 5,
        comment = "Great session, challenging but fun. Highly recommend!",
        date = "Oct 25, 2025"
    )
)

private fun EventDetails.toLegacyEvent(): LegacyEventDetails {
    return LegacyEventDetails(
        id = id,
        title = title,
        sportIcon = "🏃",
        sportType = "Group Session",
        date = date,
        time = time,
        duration = "60 min",
        location = location,
        distance = "Nearby",
        price = price.removePrefix("$").toIntOrNull() ?: 0,
        type = when {
            price.contains("free", ignoreCase = true) || price == "$0" -> EventType.FREE
            price.contains("member", ignoreCase = true) -> EventType.MEMBERS
            else -> EventType.PAID
        },
        level = "All Levels",
        maxParticipants = 12,
        currentParticipants = attendees.size.coerceAtMost(12),
        description = description,
        requirements = listOf("Water bottle", "Comfortable shoes"),
        coach = LegacyCoach(
          id = sampleLegacyEvent.coach.id,
            name = host.name,
            avatar = host.avatarUrl,
            isVerified = true,
            rating = host.rating,
            totalReviews = host.reviewsCount,
            bio = "Professional coach",
            certifications = emptyList()
        ),
        participants = attendees.map {
            LegacyParticipant(
                id = it.id,
                name = it.name,
                avatar = it.avatarUrl
            )
        },
        reviews = emptyList()
    )
}

