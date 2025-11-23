package com.example.damandroid.presentation.aimatchmaker.ui

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.MatchmakerProfile
import com.example.damandroid.presentation.aimatchmaker.model.AIMatchmakerUiState
import com.example.damandroid.presentation.aimatchmaker.viewmodel.AIMatchmakerViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import kotlinx.coroutines.delay

data class Message(
    val id: String,
    val type: MessageType,
    val text: String? = null,
    val options: List<String>? = null,
    val results: MessageResults? = null
)

enum class MessageType {
    AI, USER
}

data class MessageResults(
    val type: ResultType,
    val items: List<Any>
)

enum class ResultType {
    PEOPLE, EVENTS
}

data class PersonResult(
    val id: String,
    val name: String,
    val avatar: String,
    val sport: String,
    val distance: String,
    val matchScore: Int,
    val bio: String,
    val availability: String
)

data class EventResult(
    val id: String,
    val title: String,
    val sportIcon: String,
    val date: String,
    val time: String,
    val location: String,
    val distance: String,
    val participants: Int,
    val maxParticipants: Int,
    val matchScore: Int
)

@Composable
fun AIMatchmakerRoute(
    viewModel: AIMatchmakerViewModel,
    onBack: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    onJoinActivity: ((MatchmakerProfile) -> Unit)? = null,
    onViewProfile: ((MatchmakerProfile) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    AIMatchmakerScreen(
        state = uiState,
        onBack = onBack,
        onRefresh = onRefresh ?: viewModel::refresh,
        onSendMessage = viewModel::sendMessage,
        onJoinActivity = onJoinActivity,
        onViewProfile = onViewProfile,
        modifier = modifier
    )
}

@Composable
fun AIMatchmakerScreen(
    state: AIMatchmakerUiState,
    onBack: (() -> Unit)? = null,
    onRefresh: () -> Unit,
    onSendMessage: (String) -> Unit,
    onJoinActivity: ((MatchmakerProfile) -> Unit)? = null,
    onViewProfile: ((MatchmakerProfile) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(message = state.error, onRetry = onRefresh, modifier = modifier)
        else -> LegacyAIMatchmaker(
            modifier = modifier,
            onBack = onBack,
            conversationHistory = state.conversationHistory,
            isSendingMessage = state.isSendingMessage,
            onSendMessage = onSendMessage,
            onJoinActivity = { eventId ->
                state.profiles.firstOrNull { it.id == eventId }?.let { profile ->
                    onJoinActivity?.invoke(profile)
                }
            },
            onViewProfile = { profileId ->
                state.profiles.firstOrNull { it.id == profileId }?.let { profile ->
                    onViewProfile?.invoke(profile)
                }
            }
        )
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = message)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun LegacyAIMatchmaker(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)?,
    conversationHistory: List<com.example.damandroid.presentation.aimatchmaker.model.ChatMessage>,
    isSendingMessage: Boolean,
    onSendMessage: (String) -> Unit,
    onJoinActivity: (String) -> Unit,
    onViewProfile: (String) -> Unit
) {
    var inputValue by remember { mutableStateOf("") }
    
    // Multi-color gradient background - Three colors: Purple top, Pink middle, Cyan bottom-right
    val backgroundGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFFE8D5F5), // Light purple - top area
            Color(0xFFFFE5F0), // Light pink - middle area
            Color(0xFFE0F7F4)  // Light cyan/turquoise - bottom-right corner
        ),
        center = Offset(0.5f, 0.0f), // Top-center
        radius = 2000f
    )
    
    // Convertir l'historique de conversation en messages pour l'affichage
    val messages = remember(conversationHistory) {
        conversationHistory.map { chatMsg ->
            Message(
                id = chatMsg.id,
                type = if (chatMsg.role == com.example.damandroid.presentation.aimatchmaker.model.ChatRole.USER) MessageType.USER else MessageType.AI,
                text = chatMsg.content,
                options = chatMsg.options,
                results = when {
                    chatMsg.suggestedActivities != null && chatMsg.suggestedActivities.isNotEmpty() -> {
                        MessageResults(
                            type = ResultType.EVENTS,
                            items = chatMsg.suggestedActivities.map { activity ->
                                EventResult(
                                    id = activity.id,
                                    title = activity.title,
                                    sportIcon = when (activity.sportType.lowercase()) {
                                        "football", "soccer" -> "⚽"
                                        "basketball" -> "🏀"
                                        "running" -> "🏃"
                                        "cycling" -> "🚴"
                                        "tennis" -> "🎾"
                                        "swimming" -> "🏊"
                                        "yoga" -> "🧘"
                                        "volleyball" -> "🏐"
                                        else -> "🏃"
                                    },
                                    date = activity.date,
                                    time = activity.time,
                                    location = activity.location,
                                    distance = "",
                                    participants = activity.participants,
                                    maxParticipants = activity.maxParticipants,
                                    matchScore = activity.matchScore ?: 85
                                )
                            }
                        )
                    }
                    chatMsg.suggestedUsers != null && chatMsg.suggestedUsers.isNotEmpty() -> {
                        MessageResults(
                            type = ResultType.PEOPLE,
                            items = chatMsg.suggestedUsers.map { user ->
                                PersonResult(
                                    id = user.id,
                                    name = user.name,
                                    avatar = user.profileImageUrl ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${user.name}",
                                    sport = user.sport,
                                    distance = user.distance ?: "",
                                    matchScore = user.matchScore ?: 85,
                                    bio = user.bio ?: "",
                                    availability = user.availability ?: ""
                                )
                            }
                        )
                    }
                    else -> null
                }
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top Header Section - Card with proper styling
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onBack?.invoke() }, modifier = Modifier.size(48.dp), enabled = onBack != null) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(26.dp),
                                tint = Color(0xFF8B5CF6)
                            )
                            Text(
                                text = "AI Matchmaker",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8B5CF6),
                                letterSpacing = (-0.3).sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(48.dp)) // Balance the back button width
                }
            }

            // Chat Content Area - Proper spacing and structure
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp) // Space for header (status bar + card height)
                    .padding(bottom = 88.dp) // Space for bottom input container
            ) {
                // Check if this is the initial greeting message - show if empty or first AI message with options
                val isInitialGreeting = messages.isEmpty() || 
                    (messages.size == 1 && 
                     messages.first().type == MessageType.AI && 
                     (messages.first().options != null && messages.first().options!!.isNotEmpty()))
                
                if (isInitialGreeting || messages.isEmpty()) {
                    // Get the initial message text and options
                    val initialMessage = messages.firstOrNull()?.text ?: "Hi! I'm your AI matchmaker. I can help you find the perfect sport partners or activities. What would you like to do today?"
                    val initialOptions = messages.firstOrNull()?.options ?: listOf(
                        "Find a running partner",
                        "Join a group activity",
                        "Discover new sports"
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp) // Space from top of chat area
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(32.dp) // Increased spacing between elements
                    ) {
                        // Message Bubble with AI Avatar on the left
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp), // More space between avatar and bubble
                            verticalAlignment = Alignment.Top
                        ) {
                            // AI Avatar: 48px circle, white background
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, CircleShape)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Avatar",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color(0xFF8B5CF6)
                                )
                            }
                            
                            // Chat Message Bubble: White background, rounded corners
                            Card(
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                // Padding inside: 24px all around
                                Text(
                                    text = initialMessage,
                                    fontSize = 16.sp,
                                    color = Color(0xFF2C2C2C),
                                    lineHeight = 24.sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.padding(24.dp)
                                )
                            }
                        }
                        
                        // Three Action Buttons: Centered horizontally, increased gap
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp), // Extra space above buttons
                            horizontalArrangement = Arrangement.spacedBy(14.dp) // More space between buttons
                        ) {
                            initialOptions.take(3).forEach { option ->
                                QuickActionButton(
                                    text = option,
                                    onClick = { onSendMessage(option) }
                                )
                            }
                        }
                    }
                } else {
                    // Regular chat messages - start from top with proper padding
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { message ->
                            if (message.type == MessageType.AI) {
                                AIMessage(
                                    message = message,
                                    onOptionClick = { option ->
                                        onSendMessage(option)
                                    },
                                    onJoinActivity = onJoinActivity,
                                    onViewProfile = onViewProfile,
                                    appColors = rememberAppThemeColors(LocalThemeController.current.isDarkMode)
                                )
                            } else {
                                UserMessage(message, appColors = rememberAppThemeColors(LocalThemeController.current.isDarkMode))
                            }
                        }
                        
                        if (isSendingMessage) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color(0xFF8B5CF6)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Input Container (Fixed) - Height: 88px
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(Color.White)
            ) {
                // Upward shadow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = (-3).dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.06f),
                                    Color.Transparent
                                )
                            )
                        )
                        .blur(15.dp)
                )
                
                // Padding: 18px vertical, 20px horizontal
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Input height: 52px
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { newValue -> inputValue = newValue },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        placeholder = {
                            Text(
                                "Ask me anything...",
                                fontSize = 16.sp,
                                color = Color(0xFFB8B8B8),
                                lineHeight = 52.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF3F3F3),
                            focusedContainerColor = Color(0xFFF3F3F3),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFFE8E8E8),
                            cursorColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        shape = RoundedCornerShape(26.dp),
                        singleLine = true
                    )

                    // Send button: 52px diameter
                    Box {
                        // Purple glow shadow
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .offset(x = 0.dp, y = 2.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.45f))
                                .blur(16.dp)
                        )
                        
                        IconButton(
                            onClick = {
                                if (inputValue.isNotBlank() && !isSendingMessage) {
                                    onSendMessage(inputValue)
                                    inputValue = ""
                                }
                            },
                            enabled = !isSendingMessage && inputValue.isNotBlank(),
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF8B5CF6),
                                            Color(0xFFA855F7)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(52f, 52f) // 135° angle
                                    ),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(45f), // Rotate to point up-right
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AIMessage(
    message: Message,
    onOptionClick: (String) -> Unit,
    onJoinActivity: (String) -> Unit,
    onViewProfile: (String) -> Unit,
    appColors: AppThemeColors
) {
    val isDark = appColors.isDark
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.padding(end = 10.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                appColors.accentPurple.copy(alpha = if (isDark) 0.25f else 0.2f),
                                if (isDark) appColors.glassSurface else Color.White.copy(alpha = 0.4f),
                                appColors.accentPink.copy(alpha = if (isDark) 0.25f else 0.2f)
                            )
                        )
                    )
                    .blur(8.dp)
                    .offset(x = 2.dp, y = 2.dp)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDark) appColors.glassSurface else Color.White.copy(alpha = 0.7f))
                    .border(2.dp, if (isDark) appColors.glassBorder else Color.White.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = appColors.accentPurple
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            message.text?.let { body ->
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        appColors.accentPurple.copy(alpha = if (isDark) 0.18f else 0.1f),
                                        appColors.accentPink.copy(alpha = if (isDark) 0.18f else 0.1f),
                                        appColors.accentBlue.copy(alpha = if (isDark) 0.16f else 0.1f)
                                    )
                                )
                            )
                            .blur(8.dp)
                            .offset(x = 2.dp, y = 2.dp)
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) appColors.glassSurface else Color.White.copy(alpha = 0.7f)
                        ),
                        border = BorderStroke(2.dp, if (isDark) appColors.glassBorder else Color.White.copy(alpha = 0.8f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = sanitizeAiText(body),
                                fontSize = 14.sp,
                                color = appColors.primaryText,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            message.options?.let { options ->
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    options.forEach { option ->
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                appColors.accentPurple.copy(alpha = if (isDark) 0.28f else 0.2f),
                                                appColors.accentPink.copy(alpha = if (isDark) 0.26f else 0.2f),
                                                appColors.accentBlue.copy(alpha = if (isDark) 0.24f else 0.2f)
                                            )
                                        )
                                    )
                                    .blur(8.dp)
                                    .offset(x = 2.dp, y = 2.dp)
                            )
                            Button(
                                onClick = { onOptionClick(option) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    if (isDark) appColors.glassSurface.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.7f),
                                                    if (isDark) appColors.glassSurface.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f)
                                                )
                                            ),
                                            RoundedCornerShape(24.dp)
                                        )
                                        .border(2.dp, if (isDark) appColors.glassBorder else Color.White.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = option,
                                        color = appColors.primaryText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            message.results?.let { results ->
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (results.type) {
                        ResultType.PEOPLE -> {
                            (results.items as? List<PersonResult>)?.forEach { person ->
                                PersonResultCard(
                                    person = person,
                                    onViewProfile = { onViewProfile(person.id) },
                                    onConnect = { /* placeholder */ },
                                    appColors = appColors
                                )
                            }
                        }

                        ResultType.EVENTS -> {
                            (results.items as? List<EventResult>)?.forEach { event ->
                                EventResultCard(
                                    event = event,
                                    onJoinActivity = { onJoinActivity(event.id) },
                                    appColors = appColors
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserMessage(message: Message, appColors: AppThemeColors) {
    val isDark = appColors.isDark
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                appColors.accentPurple.copy(alpha = if (isDark) 0.28f else 0.2f),
                                appColors.accentPink.copy(alpha = if (isDark) 0.26f else 0.2f)
                            )
                        )
                    )
                    .blur(8.dp)
                    .offset(x = 2.dp, y = 2.dp)
            )
            Card(
                modifier = Modifier.widthIn(max = 280.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    appColors.accentPurple,
                                    appColors.accentPink
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = message.text.orEmpty(),
                        fontSize = 14.sp,
                        color = appColors.iconOnAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonResultCard(
    person: PersonResult,
    onViewProfile: () -> Unit,
    onConnect: () -> Unit,
    appColors: AppThemeColors
) {
    val isDark = appColors.isDark
    val cardGlow = Brush.linearGradient(
        colors = listOf(
            appColors.accentPurple.copy(alpha = if (isDark) 0.3f else 0.15f),
            appColors.accentPink.copy(alpha = if (isDark) 0.28f else 0.15f),
            appColors.accentBlue.copy(alpha = if (isDark) 0.25f else 0.15f)
        )
    )
    val cardSurface = if (isDark) appColors.glassSurface else Color.White.copy(alpha = 0.7f)
    val cardBorder = if (isDark) appColors.glassBorder else Color.White.copy(alpha = 0.8f)
    val primaryText = appColors.primaryText
    val secondaryText = appColors.secondaryText
    val mutedText = appColors.mutedText
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(cardGlow)
                .blur(12.dp)
                .offset(x = 2.dp, y = 2.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardSurface),
            border = BorderStroke(2.dp, cardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                        .offset(y = (-12).dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = person.avatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .border(2.dp, cardBorder, CircleShape)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = person.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFA855F7),
                                                Color(0xFFEC4899)
                                            )
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${person.matchScore}% match",
                                    fontSize = 10.sp,
                                    color = appColors.iconOnAccent,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = person.sport,
                            fontSize = 12.sp,
                            color = secondaryText,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = person.bio,
                            fontSize = 12.sp,
                            color = mutedText,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = mutedText
                        )
                        Text(
                            text = person.distance,
                            fontSize = 12.sp,
                            color = mutedText
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = appColors.success
                        )
                        Text(
                            text = person.availability,
                            fontSize = 12.sp,
                            color = appColors.success
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onViewProfile,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            if (isDark) appColors.glassSurface.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.6f),
                                            if (isDark) appColors.glassSurface.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.5f)
                                        )
                                    ),
                                    RoundedCornerShape(24.dp)
                                )
                                .border(2.dp, if (isDark) appColors.glassBorder else Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "View Profile",
                                color = primaryText,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = onConnect,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFA855F7),
                                            Color(0xFFEC4899)
                                        )
                                    ),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Connect",
                                color = appColors.iconOnAccent,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventResultCard(
    event: EventResult,
    onJoinActivity: () -> Unit,
    appColors: AppThemeColors
) {
    val isDark = appColors.isDark
    val cardGlow = Brush.linearGradient(
        colors = listOf(
            appColors.success.copy(alpha = if (isDark) 0.26f else 0.2f),
            appColors.accentTeal.copy(alpha = if (isDark) 0.22f else 0.15f)
        )
    )
    val cardSurface = if (isDark) appColors.glassSurface else Color.White.copy(alpha = 0.7f)
    val cardBorder = if (isDark) appColors.glassBorder else Color.White.copy(alpha = 0.8f)
    val primaryText = appColors.primaryText
    val mutedText = appColors.mutedText
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(cardGlow)
                .blur(12.dp)
                .offset(x = 2.dp, y = 2.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardSurface),
            border = BorderStroke(2.dp, cardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) appColors.glassSurface else Color.White.copy(alpha = 0.85f))
                            .border(1.dp, if (isDark) appColors.glassBorder else Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = event.sportIcon, fontSize = 16.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = event.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryText
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFA855F7),
                                                Color(0xFFEC4899)
                                            )
                                        ),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${event.matchScore}% match",
                                    fontSize = 10.sp,
                                    color = appColors.iconOnAccent,
                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 0.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = mutedText
                            )
                            Text(
                                text = "${event.date} • ${event.time}",
                                fontSize = 12.sp,
                                color = mutedText
                            )
                        }

                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = mutedText
                            )
                            Text(
                                text = "${event.location} • ${event.distance}",
                                fontSize = 12.sp,
                                color = mutedText
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = appColors.secondaryText
                        )
                        Text(
                            text = "${event.participants}/${event.maxParticipants} joined",
                            fontSize = 12.sp,
                            color = appColors.secondaryText
                        )
                    }
                }

                Button(
                    onClick = onJoinActivity,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(appColors.success, appColors.accentTeal)
                                ),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Join Activity",
                            color = appColors.iconOnAccent,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun sanitizeAiText(text: String): String {
    // Remove explicit (ID: ...) fragments
    var cleaned = text.replace(Regex("\\(ID:\\s*[^)]+\\)", RegexOption.IGNORE_CASE), "")
    // Remove lines that expose IDs (FR/EN)
    val filtered = cleaned
        .lines()
        .filterNot { line ->
            val l = line.trim()
            l.matches(Regex("(?i)^.*\\b(id|activity id|user id|id de l['’]?activit[eé]|id de l['’]?utilisateur)\\b\\s*[:：].*$"))
        }
    cleaned = filtered.joinToString("\n")
    // Remove markdown bold markers and tidy whitespace
    cleaned = cleaned.replace("**", "")
        .replace(Regex(" +"), " ")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
    return cleaned
}


@Composable
private fun AIMatchmakerOrb(
    size: androidx.compose.ui.unit.Dp,
    color: Color,
    top: androidx.compose.ui.unit.Dp? = null,
    start: androidx.compose.ui.unit.Dp? = null,
    end: androidx.compose.ui.unit.Dp? = null,
    bottom: androidx.compose.ui.unit.Dp? = null,
    center: Boolean = false,
    pulseDurationMs: Int,
    startDelayMs: Int = 0
) {
    val infinite = rememberInfiniteTransition(label = "aimatchmaker-orb")
    val scale by infinite.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = pulseDurationMs, easing = LinearEasing, delayMillis = startDelayMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb-scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        var orbModifier = Modifier
            .size(size * scale)
            .blur(48.dp)
            .background(color = color, shape = CircleShape)

        orbModifier = if (center) {
            orbModifier.align(Alignment.Center)
        } else {
            val alignment = when {
                bottom != null && end != null -> Alignment.BottomEnd
                bottom != null -> Alignment.BottomStart
                end != null -> Alignment.TopEnd
                else -> Alignment.TopStart
            }
            orbModifier.align(alignment)
        }

        top?.let { orbModifier = orbModifier.offset(y = it) }
        start?.let { orbModifier = orbModifier.offset(x = it) }
        end?.let { orbModifier = orbModifier.offset(x = (-it)) }
        bottom?.let { orbModifier = orbModifier.offset(y = (-it)) }

        Box(modifier = orbModifier)
    }
}

@Composable
private fun RowScope.QuickActionButton(
    text: String,
    onClick: () -> Unit
) {
    // Action Button: Rounded rectangle (pill shape), 56px height, 28px border-radius
    Surface(
        shape = RoundedCornerShape(28.dp), // Fully rounded ends
        color = Color(0xFFF8F5FF), // White or very light purple
        border = BorderStroke(1.dp, Color(0xFFE8E0FF)), // Thin light purple border
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF2C2C2C), // Dark gray
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 20.sp,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .wrapContentHeight(Alignment.CenterVertically)
        )
    }
}

