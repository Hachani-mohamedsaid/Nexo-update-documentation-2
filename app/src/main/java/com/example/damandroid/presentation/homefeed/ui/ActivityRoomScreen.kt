package com.example.damandroid.presentation.homefeed.ui

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.HomeActivity
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import com.example.damandroid.presentation.achievements.viewmodel.AchievementsViewModel
import com.example.damandroid.presentation.achievements.ui.components.BadgeUnlockedDialog
import com.example.damandroid.presentation.achievements.ui.components.LevelUpDialog
import com.example.damandroid.presentation.achievements.ui.components.ChallengeCompletedDialog
import com.example.damandroid.presentation.achievements.NotificationDisplayStore
import androidx.compose.ui.platform.LocalContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

@Composable
fun ActivityRoomRoute(
    activity: HomeActivity,
    achievementsViewModel: AchievementsViewModel? = null,
    onBack: () -> Unit,
    onLeave: (() -> Unit)? = null,
    onMarkComplete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val colors = rememberAppThemeColors(themeController.isDarkMode)
    val repository = remember { com.example.damandroid.api.ActivityRoomRepository() }
    val viewModel = remember(activity.id) {
        com.example.damandroid.presentation.homefeed.viewmodel.ActivityRoomViewModel(
            activityId = activity.id,
            repository = repository
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val notificationStore = remember { NotificationDisplayStore(context) }
    
    // Écouter les événements de complétion d'activité pour rafraîchir les achievements
    achievementsViewModel?.let { achievementsVM ->
        val newBadges by achievementsVM.newBadgesUnlocked.collectAsState()
        val levelUp by achievementsVM.levelUpEvent.collectAsState()
        
        // Écouter les activités complétées
        LaunchedEffect(viewModel) {
            viewModel.activityCompleted.collect { activityId ->
                // Rafraîchir les achievements après complétion
                achievementsVM.refreshAllAfterActivityCompletion()
            }
        }
        
        // Collecter les challenges complétés dans un state
        var currentChallengeCompleted by remember { mutableStateOf<com.example.damandroid.presentation.achievements.viewmodel.ChallengeCompletedEvent?>(null) }
        
        // Écouter les challenges complétés
        LaunchedEffect(achievementsVM) {
            achievementsVM.challengeCompletedEvent.collect { event ->
                // Vérifier dans le store si le challenge a déjà été affiché
                if (!notificationStore.isChallengeShown(event.challengeId)) {
                    currentChallengeCompleted = event
                    notificationStore.markChallengeAsShown(event.challengeId)
                }
            }
        }
        
        // Afficher les notifications de badges débloqués (uniquement si pas déjà affichées)
        newBadges.forEach { badge ->
            if (!notificationStore.isBadgeShown(badge.id)) {
                BadgeUnlockedDialog(badge = badge) {
                    achievementsVM.clearNewBadge(badge.id)
                    notificationStore.markBadgeAsShown(badge.id)
                }
            }
        }
        
        // Afficher la notification de montée de niveau (uniquement si pas déjà affichée)
        levelUp?.let { event ->
            if (!notificationStore.isLevelUpShown(event.newLevel)) {
                LevelUpDialog(event = event) {
                    achievementsVM.clearLevelUpEvent()
                    notificationStore.markLevelUpAsShown(event.newLevel)
                }
            }
        }
        
        // Afficher le dialog du challenge complété
        currentChallengeCompleted?.let { event ->
            ChallengeCompletedDialog(event = event) {
                currentChallengeCompleted = null
            }
        }
    }
    
    ActivityRoomScreen(
            activity = activity,
            uiState = uiState,
            colors = colors,
            onBack = onBack,
            onSendMessage = viewModel::sendMessage,
            onSetTyping = viewModel::setTyping,
            onJoinActivity = viewModel::joinActivity,
            onLeave = {
                viewModel.leaveActivity()
                onLeave?.invoke()
            },
            onMarkComplete = if (viewModel.isCurrentUserHost()) {
                {
                    viewModel.completeActivity()
                    onMarkComplete?.invoke()
                }
            } else null,
            onRefresh = viewModel::refresh,
            modifier = modifier
        )
}

private enum class ActivityRoomTab(val label: String) {
    Chat("Chat"),
    People("People"),
    AiTips("AI Tips"),
    Info("Info")
}

// Ces data classes sont maintenant dans ActivityRoomViewModel

private data class ActivityInsight(
    val title: String,
    val description: String,
    val highlight: String? = null,
    val icon: ImageVector,
    val iconTint: Color,
    val chipColor: Color
)

@Composable
private fun ActivityRoomScreen(
    activity: HomeActivity,
    uiState: com.example.damandroid.presentation.homefeed.viewmodel.ActivityRoomUiState,
    colors: AppThemeColors,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSetTyping: (Boolean) -> Unit,
    onJoinActivity: (() -> Unit)? = null,
    onLeave: (() -> Unit)?,
    onMarkComplete: (() -> Unit)?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spotsLeft = (activity.spotsTotal - activity.spotsTaken).coerceAtLeast(0)
    var selectedTab by remember { mutableStateOf(ActivityRoomTab.Chat) }
    var messageInput by remember { mutableStateOf("") }
    
    // Utiliser les données du ViewModel
    val messages = uiState.messages
    val participants = uiState.participants
    val insights = remember(activity.id, colors.isDark) {
        listOf(
            ActivityInsight(
                title = "\"Progress starts with small steps\"",
                description = "Stay consistent and you'll reach your goals!",
                icon = Icons.Filled.AutoAwesome,
                iconTint = colors.accentPurple,
                chipColor = if (colors.isDark) colors.accentPurple.copy(alpha = 0.22f) else Color(0xFFEDE9FE)
            ),
            ActivityInsight(
                title = "Perfect weather conditions",
                description = "72°F, sunny — ideal for outdoor activity.",
                highlight = "Great conditions for ${activity.sportType}. Remember sunscreen!",
                icon = Icons.Filled.WbSunny,
                iconTint = colors.accentBlue,
                chipColor = if (colors.isDark) colors.accentBlue.copy(alpha = 0.18f) else Color(0xFFE0F2FE)
            ),
            ActivityInsight(
                title = "Optimal group size",
                description = "${activity.spotsTaken} participants — perfect for engagement.",
                highlight = "Not too crowded — you'll get personalized attention.",
                icon = Icons.Filled.Group,
                iconTint = colors.success,
                chipColor = if (colors.isDark) colors.success.copy(alpha = 0.24f) else Color(0xFFD1FAE5)
            ),
            ActivityInsight(
                title = "Timing suggestion",
                description = "Arrive 10 minutes early for warm-up.",
                icon = Icons.Filled.Schedule,
                iconTint = colors.accentOrange,
                chipColor = if (colors.isDark) colors.accentOrange.copy(alpha = 0.22f) else Color(0xFFFFEDD5)
            ),
            ActivityInsight(
                title = "Safety reminders",
                description = "Stay hydrated, listen to your body, and inform the host of any concerns.",
                icon = Icons.Filled.Warning,
                iconTint = colors.warning,
                chipColor = if (colors.isDark) colors.warning.copy(alpha = 0.22f) else Color(0xFFFEF3C7)
            ),
            ActivityInsight(
                title = "AI says this is a great match!",
                description = "Based on your profile, this activity matches your skill level and interests.",
                icon = Icons.Filled.ThumbUp,
                iconTint = colors.iconOnAccent,
                chipColor = colors.accentPurple.copy(alpha = if (colors.isDark) 0.38f else 0.5f)
            )
        )
    }

    fun sendMessage() {
        val text = messageInput.trim()
        if (text.isEmpty() || uiState.isSendingMessage) return
        onSendMessage(text)
        messageInput = ""
    }
    
    // Afficher l'erreur si présente
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Vous pouvez afficher un Snackbar ici si nécessaire
        }
    }
    
    // Détecter quand l'utilisateur tape pour l'indicateur de frappe
    LaunchedEffect(messageInput) {
        if (messageInput.isNotEmpty()) {
            onSetTyping(true)
            // Arrêter l'indicateur après 2 secondes d'inactivité
            delay(2000)
            // Vérifier si l'utilisateur a toujours du texte (pas de changement)
            if (messageInput.isNotEmpty()) {
                onSetTyping(false)
            }
        } else {
            onSetTyping(false)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (colors.isDark) {
                    colors.backgroundGradient
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFE8D5F2),
                            Color(0xFFFFE4F1),
                            Color(0xFFE5E5F0)
                        )
                    )
                }
            )
    ) {
        FloatingActivityRoomOrbs(colors)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ActivityRoomHeader(activity = activity, spotsLeft = spotsLeft, colors = colors, onBack = onBack)

            Spacer(modifier = Modifier.height(12.dp))
            
            // Indicateur de connexion WebSocket
            if (!uiState.isWebSocketConnected && !uiState.isLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (colors.isDark) 
                            colors.warning.copy(alpha = 0.2f) 
                        else 
                            Color(0xFFFFF3CD)
                    ),
                    border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colors.warning,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Connexion en cours...",
                            fontSize = 12.sp,
                            color = colors.warning,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            ActivityTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it }, colors = colors)

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.accentPurple)
                        }
                    }
                    else -> {
                        when (selectedTab) {
                            ActivityRoomTab.Chat -> ChatTab(
                                messages = messages,
                                messageInput = messageInput,
                                onMessageChange = { messageInput = it },
                                onSend = ::sendMessage,
                                isSending = uiState.isSendingMessage,
                                typingUsers = uiState.typingUsers,
                                colors = colors
                            )
                            ActivityRoomTab.People -> ParticipantsTab(participants = participants, colors = colors)
                            ActivityRoomTab.AiTips -> InsightsTab(insights = insights, colors = colors)
                            ActivityRoomTab.Info -> InfoTab(activity = activity, colors = colors)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ActionButtons(
                colors = colors,
                onLeave = onLeave ?: onBack,
                onMarkComplete = onMarkComplete,
                isLeaving = uiState.isLeaving,
                isCompleting = uiState.isCompleting
            )
        }
    }
}

@Composable
private fun ActivityRoomHeader(
    activity: HomeActivity, 
    spotsLeft: Int, 
    colors: AppThemeColors, 
    onBack: () -> Unit
) {
    // Calculer le temps restant jusqu'à l'activité
    val timeUntilStart = remember(activity.date, activity.time) {
        try {
            // Parser la date et l'heure de l'activité
            val dateTimeStr = "${activity.date}T${activity.time}"
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val activityDateTime = java.time.LocalDateTime.parse(dateTimeStr, formatter)
            val now = java.time.LocalDateTime.now()
            val duration = Duration.between(now, activityDateTime)
            
            when {
                duration.isNegative -> "Started"
                duration.toHours() < 1 -> "Starts in ${duration.toMinutes()} minutes"
                duration.toHours() < 24 -> "Starts in ${duration.toHours()} hours"
                else -> "Starts in ${duration.toDays()} days"
            }
        } catch (e: Exception) {
            "Starts soon"
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = colors.primaryText)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = activity.title.ifBlank { "Activity" }, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryText)
                    Text(text = "Hosted by ${activity.hostName}", fontSize = 12.sp, color = colors.secondaryText)
                }
                IconButton(onClick = { /* share */ }) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = "Share", tint = colors.primaryText)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(0.dp, Color.Transparent),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(colors.accentPurple, colors.accentPink)))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(imageVector = Icons.Filled.Schedule, contentDescription = null, tint = Color.White)
                                Text(text = timeUntilStart, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                            }
                            Surface(shape = RoundedCornerShape(18.dp), color = if (colors.isDark) colors.cardSurface else Color.White) {
                                Text(
                                    text = "$spotsLeft spots left",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (colors.isDark) colors.primaryText else Color(0xFF1F2933),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Map, contentDescription = null, tint = Color.White)
                            Column {
                                Text(text = activity.location, fontSize = 13.sp, color = Color.White)
                                Text(text = "Get Directions", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityTabRow(selectedTab: ActivityRoomTab, onTabSelected: (ActivityRoomTab) -> Unit, colors: AppThemeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.65f))
            .border(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActivityRoomTab.values().forEach { tab ->
            val selected = tab == selectedTab
            val interaction = remember { MutableInteractionSource() }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(interactionSource = interaction, indication = null) { onTabSelected(tab) },
                shape = RoundedCornerShape(18.dp),
                color = if (selected) {
                    if (colors.isDark) colors.cardSurface else Color.White
                } else {
                    Color.Transparent
                },
                border = BorderStroke(2.dp, if (selected) colors.accentPurple else Color.Transparent)
            ) {
                Text(
                    text = tab.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (selected) colors.accentPurple else colors.secondaryText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ChatTab(
    messages: List<com.example.damandroid.presentation.homefeed.viewmodel.ActivityChatMessage>,
    messageInput: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    typingUsers: Map<String, Boolean>,
    isParticipant: Boolean = true,
    onJoinActivity: (() -> Unit)? = null,
    isJoining: Boolean = false,
    colors: AppThemeColors
) {
    val listState = rememberLazyListState()
    
    // Scroller automatiquement vers le bas quand un nouveau message arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.65f),
                        border = BorderStroke(2.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f))
                    ) {
                        AsyncImage(model = message.avatar, contentDescription = message.sender)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = message.sender, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.primaryText)
                            Text(text = message.time, fontSize = 11.sp, color = colors.mutedText)
                        }
                        Surface(
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp),
                            color = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = message.text,
                                fontSize = 13.sp,
                                color = colors.secondaryText,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
            
            // Afficher les utilisateurs qui tapent
            if (typingUsers.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = colors.mutedText,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "${typingUsers.size} utilisateur(s) en train de taper...",
                            fontSize = 12.sp,
                            color = colors.mutedText,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
            
            // Afficher un message si l'utilisateur n'est pas participant
            if (!isParticipant && messages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 16.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Rejoignez l'activité pour participer au chat",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.primaryText,
                                textAlign = TextAlign.Center
                            )
                            if (onJoinActivity != null) {
                                Button(
                                    onClick = onJoinActivity,
                                    enabled = !isJoining,
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accentPurple
                                    )
                                ) {
                                    if (isJoining) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Rejoindre l'activité",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        text = if (isParticipant) "Type a message..." else "Rejoignez l'activité pour envoyer des messages",
                        fontSize = 13.sp, 
                        color = colors.mutedText
                    ) 
                },
                shape = CircleShape,
                singleLine = true,
                enabled = !isSending && isParticipant,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.65f),
                    unfocusedContainerColor = if (colors.isDark) colors.glassSurface.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.6f),
                    focusedBorderColor = if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.75f),
                    unfocusedBorderColor = if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f),
                    cursorColor = colors.primaryText,
                    focusedTextColor = colors.primaryText,
                    unfocusedTextColor = colors.primaryText
                )
            )
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFF6B35), Color(0xFFF7931E)))),
                contentAlignment = Alignment.Center
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    IconButton(
                        onClick = onSend, 
                        enabled = messageInput.isNotBlank() && !isSending && isParticipant
                    ) {
                        Icon(imageVector = Icons.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantsTab(participants: List<com.example.damandroid.presentation.homefeed.viewmodel.ActivityParticipant>, colors: AppThemeColors) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(participants, key = { it.id }) { participant ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.6f))
                    .border(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.65f),
                    border = BorderStroke(2.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f))
                ) {
                    AsyncImage(model = participant.avatar, contentDescription = participant.name)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = participant.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.primaryText)
                    Text(text = participant.status, fontSize = 12.sp, color = colors.mutedText)
                }
                if (participant.status.equals("Host", ignoreCase = true)) {
                    Surface(shape = RoundedCornerShape(18.dp), color = colors.success.copy(alpha = if (colors.isDark) 0.8f else 1f)) {
                        Text(text = "Host", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                } else {
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f))
                    ) {
                        Text(text = "Message", fontSize = 12.sp, color = colors.accentBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsTab(insights: List<ActivityInsight>, colors: AppThemeColors) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(insights) { insight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.6f)),
                border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = insight.chipColor
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(imageVector = insight.icon, contentDescription = null, tint = insight.iconTint)
                            }
                        }
                        Column {
                            Text(text = insight.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryText)
                            Text(text = insight.description, fontSize = 12.sp, color = colors.secondaryText)
                        }
                    }
                    insight.highlight?.let {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else Color.Transparent)
                        ) {
                            Text(text = it, fontSize = 11.sp, color = colors.secondaryText, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoTab(activity: HomeActivity, colors: AppThemeColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCard(title = "About", colors = colors) {
            Text(
                text = activity.title + " with ${activity.hostName}. We'll do some great practice together!",
                fontSize = 13.sp,
                color = colors.secondaryText
            )
        }
        InfoCard(title = "Details", colors = colors) {
            DetailRow(icon = Icons.Filled.Schedule, label = "Date & Time", value = "${activity.date} at ${activity.time}", colors = colors)
            DetailRow(icon = Icons.Filled.Map, label = "Location", value = activity.location, colors = colors)
            DetailRow(icon = Icons.Filled.Group, label = "Participants", value = "${activity.spotsTaken} / ${activity.spotsTotal}", colors = colors)
        }
        InfoCard(title = "Skill Level", colors = colors) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.Transparent,
                border = BorderStroke(0.dp, Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(colors.accentPurple, colors.accentPink)))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = activity.level, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun InfoCard(title: String, colors: AppThemeColors, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.6f))
            .border(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
            .padding(18.dp)
    ) {
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryText, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String, colors: AppThemeColors) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.6f))
        ) {
            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = colors.accentPurple)
            }
        }
        Column {
            Text(text = label, fontSize = 11.sp, color = colors.mutedText)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.primaryText)
        }
    }
}

@Composable
private fun ActionButtons(
    colors: AppThemeColors, 
    onLeave: () -> Unit, 
    onMarkComplete: (() -> Unit)?,
    isLeaving: Boolean = false,
    isCompleting: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onLeave,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (colors.isDark) colors.glassSurface else Color.White.copy(alpha = 0.6f)),
            border = BorderStroke(1.dp, if (colors.isDark) colors.glassBorder else Color.White.copy(alpha = 0.7f)),
            enabled = !isLeaving && !isCompleting
        ) {
            if (isLeaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.primaryText
                )
            } else {
                Text(text = "Leave", fontSize = 13.sp, color = colors.primaryText)
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFFFF6B35), Color(0xFFF7931E)))),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { onMarkComplete?.invoke() },
                modifier = Modifier.fillMaxSize(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                enabled = !isLeaving && !isCompleting && onMarkComplete != null
            ) {
                if (isCompleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(text = "Mark Complete", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FloatingActivityRoomOrbs(colors: AppThemeColors) {
    val transition = rememberInfiniteTransition(label = "activity-room-orbs")
    val offset1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(animation = tween(4200, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "orb1"
    )
    val offset2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(animation = tween(5000, easing = LinearEasing, delayMillis = 600), repeatMode = RepeatMode.Reverse),
        label = "orb2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-40).dp, y = (-60).dp + offset1.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(colors.accentPurple.copy(alpha = if (colors.isDark) 0.18f else 0.25f))
                .blur(90.dp)
        )
        Box(
            modifier = Modifier
                .offset(x = 220.dp, y = 320.dp - offset2.dp)
                .size(260.dp)
                .clip(CircleShape)
                .background(colors.accentPink.copy(alpha = if (colors.isDark) 0.14f else 0.18f))
                .blur(110.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 40.dp)
                .size(180.dp)
                .clip(CircleShape)
                .background(colors.accentBlue.copy(alpha = if (colors.isDark) 0.12f else 0.16f))
                .blur(90.dp)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-20).dp)
                .size(140.dp)
                .clip(CircleShape)
                .background(colors.accentTeal.copy(alpha = if (colors.isDark) 0.14f else 0.2f))
                .blur(72.dp)
        )
    }
}
