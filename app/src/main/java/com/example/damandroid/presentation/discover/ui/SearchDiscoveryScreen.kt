package com.example.damandroid.presentation.discover.ui

import android.graphics.Color.parseColor
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.damandroid.domain.model.DiscoverOverview
import com.example.damandroid.domain.model.DiscoverSportCategory
import com.example.damandroid.domain.model.DiscoverUser
import com.example.damandroid.domain.model.FeaturedCoach
import com.example.damandroid.domain.model.TrendingActivity
import com.example.damandroid.presentation.discover.model.DiscoverUiState
import com.example.damandroid.presentation.discover.viewmodel.DiscoverViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

@Composable
fun DiscoverRoute(
    viewModel: DiscoverViewModel,
    onBack: (() -> Unit)? = null,
    onCoachClick: ((String) -> Unit)? = null,
    onChatClick: ((String, String, String?, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    DiscoverScreen(
        state = uiState,
        onBack = onBack,
        onCoachClick = onCoachClick,
        onChatClick = onChatClick,
        onCategorySelected = viewModel::onCategorySelected,
        onRefresh = viewModel::refresh,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        modifier = modifier
    )
}

@Composable
fun DiscoverScreen(
    state: DiscoverUiState,
    onBack: (() -> Unit)?,
    onCoachClick: ((String) -> Unit)?,
    onChatClick: ((String, String, String?, Boolean) -> Unit)?,
    onCategorySelected: (String?) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(message = state.error, onRetry = onRefresh, modifier = modifier)
        state.overview != null -> DiscoverContent(
            overview = state.overview,
            searchQuery = state.searchQuery,
            selectedCategory = state.selectedCategory,
            onCategorySelected = onCategorySelected,
            onChatClick = onChatClick,
            onSearchQueryChange = onSearchQueryChange,
            onBack = onBack,
            onCoachClick = onCoachClick,
            modifier = modifier
        )
        else -> ErrorState(
            message = "No data available",
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = message)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
private fun DiscoverContent(
    overview: DiscoverOverview,
    searchQuery: String,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    onChatClick: ((String, String, String?, Boolean) -> Unit)?,
    onSearchQueryChange: (String) -> Unit,
    onBack: (() -> Unit)?,
    onCoachClick: ((String) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val colors = rememberAppThemeColors(themeController.isDarkMode)

    val filteredCategories = overview.sportCategories.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }
    val selectedIcon = selectedCategory?.let { name ->
        overview.sportCategories.find { it.name.equals(name, ignoreCase = true) }?.icon
    }
    val filteredActivities = overview.trendingActivities.filter { activity ->
        val searchMatch = searchQuery.isBlank() || activity.title.contains(searchQuery, ignoreCase = true)
        val categoryMatch = selectedCategory == null ||
                activity.title.contains(selectedCategory, ignoreCase = true) ||
                (selectedIcon != null && activity.sportIcon == selectedIcon)
        searchMatch && categoryMatch
    }
    val filteredUsers = overview.activeUsers.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Light gray background like the image
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DiscoverHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onBack = onBack,
                colors = colors
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    FeaturedCoachSection(
                        coach = overview.featuredCoach,
                        onCoachClick = onCoachClick,
                        colors = colors
                    )
                }

                item {
                    SportCategoriesSection(
                        categories = filteredCategories,
                        colors = colors,
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected
                    )
                }

                item {
                    TrendingActivitiesSection(
                        activities = filteredActivities,
                        colors = colors,
                        onChatNow = { activityId, title ->
                            launchChatForActivity(activityId, title, onChatClick)
                        }
                    )
                }
                // Removed "Active Now" section per request
            }
        }
    }
}

@Composable
private fun DiscoverHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onBack: (() -> Unit)?,
    colors: AppThemeColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top row: Back button, centered title, notification bell
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }
            
            // Centered title
            Text(
                text = "Discover",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = (-0.5).sp
            )
            
            // Notification bell
            IconButton(
                onClick = { /* Handle notification */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Search bar - Clean white background
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    "Search sports, people, or places...",
                    color = Color(0xFF9E9E9E),
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF9E9E9E),
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                color = Color.Black
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
private fun FeaturedCoachSection(
    coach: FeaturedCoach?,
    onCoachClick: ((String) -> Unit)?,
    colors: AppThemeColors
) {
    if (coach == null) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Verified Coaches", colors)
        FeaturedCoachCard(coach = coach, onClick = onCoachClick, colors = colors)
    }
}

@Composable
private fun FeaturedCoachCard(
    coach: FeaturedCoach,
    onClick: ((String) -> Unit)?,
    colors: AppThemeColors
) {
    // Clean white card with subtle shadow - no glass effects
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick(coach.id) } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clean circular avatar - no border
            AsyncImage(
                model = coach.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = coach.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    // Green verification checkmark
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = coach.title,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "⭐ ${coach.rating} (${coach.reviewCount} reviews)",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                    Text(
                        text = "${coach.reviewCount}+ sessions",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}

@Composable
private fun SportCategoriesSection(
    categories: List<DiscoverSportCategory>,
    colors: AppThemeColors,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Browse by Sport", colors)
        if (categories.isEmpty()) {
            EmptySectionMessage("No categories match your search", colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { category ->
                            SportCategoryCard(
                                category = category,
                                colors = colors,
                                isSelected = selectedCategory?.equals(category.name, ignoreCase = true) == true,
                                onClick = {
                                    onCategorySelected(category.name)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowItems.size < 3) {
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SportCategoryCard(
    category: DiscoverSportCategory,
    colors: AppThemeColors,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Clean white card with subtle shadow - no glass effects
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Clean icon display - no background box
            Text(
                text = category.icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                fontSize = 13.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TrendingActivitiesSection(
    activities: List<TrendingActivity>,
    colors: AppThemeColors,
    onChatNow: (String, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Trending Near You", colors)
        if (activities.isEmpty()) {
            EmptySectionMessage("No activities match your search", colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                activities.forEach { activity ->
                    TrendingActivityCard(
                        activity = activity,
                        colors = colors,
                        onChatNow = { onChatNow(activity.id, activity.title) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendingActivityCard(
    activity: TrendingActivity,
    colors: AppThemeColors,
    onChatNow: (() -> Unit)? = null
) {
    // Clean white card with subtle shadow - no glass effects
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clean icon display - simple square with icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = activity.sportIcon, fontSize = 24.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${activity.location} • ${activity.date}",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Clean circular avatar - no border
                    AsyncImage(
                        model = activity.hostAvatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                    )
                    Text(
                        text = activity.hostName,
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Green spots badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF4CAF50)
                ) {
                    Text(
                        text = "${activity.maxParticipants - activity.participants} spots",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

private fun launchChatForActivity(
    activityId: String,
    activityTitle: String,
    onChatClick: ((String, String, String?, Boolean) -> Unit)?
) {
    // Reuse the ActivityRoom flow (join then create/get group chat)
    kotlinx.coroutines.GlobalScope.launch {
        val repository = com.example.damandroid.api.ActivityRoomRepository()
        val joinResult = repository.joinActivity(activityId)
        when (joinResult) {
            is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Success,
            is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Error -> {
                // Proceed even if already participant
                when (val chatResult = repository.createOrGetActivityGroupChat(activityId)) {
                    is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Success -> {
                        val chat = chatResult.data.chat
                        onChatClick?.invoke(chat.id, chat.groupName, chat.groupAvatar, chat.isGroup)
                    }
                    else -> { /* ignore errors here for brevity */ }
                }
            }
        }
    }
}

@Composable
private fun ActiveUsersSection(users: List<DiscoverUser>, colors: AppThemeColors) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Active Now", colors)
        if (users.isEmpty()) {
            EmptySectionMessage("No active users match your search", colors)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                users.forEach { user ->
                    ActiveUserCard(user = user, colors = colors)
                }
            }
        }
    }
}

@Composable
private fun ActiveUserCard(user: DiscoverUser, colors: AppThemeColors) {
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = 2.dp, y = 2.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.accentTeal.copy(alpha = 0.16f),
                            colors.accentGreen.copy(alpha = 0.12f)
                        )
                    )
                )
                .blur(16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.glassSurface),
            border = BorderStroke(2.dp, colors.glassBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, colors.glassBorder, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .offset(x = 32.dp, y = 32.dp)
                            .clip(CircleShape)
                            .background(colors.success)
                            .border(2.dp, colors.glassSurface, CircleShape)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = user.sport,
                            fontSize = 12.sp,
                            color = colors.mutedText
                        )
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = colors.mutedText
                        )
                        Text(
                            text = "${user.distance} away",
                            fontSize = 12.sp,
                            color = colors.mutedText
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { /* Follow */ },
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colors.primaryText
                        ),
                        border = BorderStroke(2.dp, colors.glassBorder),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Text(text = "Follow", fontSize = 12.sp, color = colors.primaryText)
                    }
                    Button(
                        onClick = { /* Chat */ },
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.success),
                        contentPadding = PaddingValues(horizontal = 14.dp)
    ) {
        Text(
                            text = "Chat",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.iconOnAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, colors: AppThemeColors) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun EmptySectionMessage(message: String, colors: AppThemeColors) {
    Text(
        text = message,
        fontSize = 13.sp,
        color = colors.mutedText
    )
}

@Composable
private fun FloatingDiscoveryOrbs(colors: AppThemeColors) {
    val purple = colors.accentPurple
    val pink = colors.accentPink
    val blue = colors.accentBlue
    val teal = colors.accentTeal
    val glassSurface = colors.glassSurface

    Box(modifier = Modifier.fillMaxSize()) {
        val transition1 = rememberInfiniteTransition(label = "orb1")
        val pulse1 by transition1.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse1"
        )

        Box(
            modifier = Modifier
                .offset(x = 40.dp, y = 80.dp)
                .size(132.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            purple.copy(alpha = pulse1),
                            glassSurface.copy(alpha = pulse1 * 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .blur(52.dp)
        )

        val transition2 = rememberInfiniteTransition(label = "orb2")
        val pulse2 by transition2.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, delayMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse2"
        )

        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-40).dp)
                .size(168.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            pink.copy(alpha = pulse2),
                            glassSurface.copy(alpha = pulse2 * 0.55f),
                            Color.Transparent
                        )
                    )
                )
                .blur(52.dp)
                .align(Alignment.BottomEnd)
        )

        val transition3 = rememberInfiniteTransition(label = "orb3")
        val pulse3 by transition3.animateFloat(
            initialValue = 0.28f,
            targetValue = 0.14f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, delayMillis = 1600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse3"
        )

        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            blue.copy(alpha = pulse3),
                            glassSurface.copy(alpha = pulse3 * 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .blur(36.dp)
                .align(Alignment.Center)
        )

        val transition4 = rememberInfiniteTransition(label = "orb4")
        val pulse4 by transition4.animateFloat(
            initialValue = 0.24f,
            targetValue = 0.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, delayMillis = 2200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse4"
        )

        Box(
            modifier = Modifier
                .offset(x = (-12).dp, y = (-12).dp)
                .size(86.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            teal.copy(alpha = pulse4),
                            glassSurface.copy(alpha = pulse4 * 0.55f),
                            Color.Transparent
                        )
                    )
                )
                .blur(32.dp)
                .align(Alignment.TopEnd)
        )
    }
}

private fun parseHexOrDefault(hex: String, fallback: Color): Color {
    return runCatching { Color(parseColor(hex)) }.getOrElse { fallback }
}

