package com.example.damandroid.presentation.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.damandroid.domain.model.ChatParticipant
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

@Composable
fun ParticipantsRoute(
    chatId: String,
    chatName: String,
    onGetParticipants: suspend (String) -> List<ChatParticipant>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    activityId: String? = null // ID de l'activité pour identifier le créateur
) {
    var participants by remember { mutableStateOf<List<ChatParticipant>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }
    var creatorId by remember { mutableStateOf<String?>(null) }
    
    // Récupérer l'ID du créateur de l'activité si activityId est fourni
    LaunchedEffect(activityId) {
        if (activityId != null) {
            try {
                val repository = com.example.damandroid.api.ActivityRoomRepository()
                when (val result = repository.getActivity(activityId)) {
                    is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Success -> {
                        creatorId = result.data.getCreatorId()
                    }
                    else -> {
                        // En cas d'erreur, on continue sans badge admin
                        creatorId = null
                    }
                }
            } catch (e: Exception) {
                // En cas d'erreur, on continue sans badge admin
                creatorId = null
            }
        }
    }
    
    LaunchedEffect(chatId, retryTrigger) {
        isLoading = true
        error = null
        try {
            participants = onGetParticipants(chatId)
            isLoading = false
        } catch (e: Exception) {
            error = e.message ?: "Failed to load participants"
            isLoading = false
        }
    }
    
    ParticipantsScreen(
        chatName = chatName,
        participants = participants,
        isLoading = isLoading,
        error = error,
        creatorId = creatorId,
        onBack = onBack,
        onRetry = {
            retryTrigger++
        },
        modifier = modifier
    )
}

@Composable
fun ParticipantsScreen(
    chatName: String,
    participants: List<ChatParticipant>,
    isLoading: Boolean,
    error: String?,
    creatorId: String? = null, // ID du créateur de l'activité
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = rememberParticipantsPalette(rememberAppThemeColors(LocalThemeController.current.isDarkMode))
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.backgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            ParticipantsHeader(
                chatName = chatName,
                participantCount = participants.size,
                onBack = onBack,
                palette = palette
            )
            
            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = error,
                                color = palette.errorText
                            )
                            Button(onClick = onRetry) {
                                Text("Retry")
                            }
                        }
                    }
                }
                participants.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No participants found",
                            color = palette.secondaryText
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(participants, key = { it.id }) { participant ->
                            ParticipantItem(
                                participant = participant,
                                isAdmin = creatorId != null && participant.id == creatorId,
                                palette = palette
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantsHeader(
    chatName: String,
    participantCount: Int,
    onBack: () -> Unit,
    palette: ParticipantsPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = palette.headerBackground,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = palette.headerText
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Participants",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.headerText
                )
                Text(
                    text = "$participantCount members",
                    fontSize = 14.sp,
                    color = palette.secondaryText
                )
            }
        }
    }
}

@Composable
private fun ParticipantItem(
    participant: ChatParticipant,
    isAdmin: Boolean = false,
    palette: ParticipantsPalette
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = palette.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            val avatarUrl = participant.profileImageUrl ?: participant.avatar
            if (!avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = palette.avatarPlaceholder
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = participant.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.avatarText
                        )
                    }
                }
            }
            
            // Name, email and admin badge
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = participant.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.primaryText
                    )
                    // Badge Admin
                    if (isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = palette.adminBadgeBackground,
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text(
                                text = "Admin",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.adminBadgeText,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                if (!participant.email.isNullOrEmpty()) {
                    Text(
                        text = participant.email,
                        fontSize = 14.sp,
                        color = palette.secondaryText
                    )
                }
            }
        }
    }
}

// Palette for participants screen
data class ParticipantsPalette(
    val backgroundBrush: Brush,
    val headerBackground: Color,
    val headerText: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val cardBackground: Color,
    val avatarPlaceholder: Color,
    val avatarText: Color,
    val errorText: Color,
    val adminBadgeBackground: Color,
    val adminBadgeText: Color
)

@Composable
private fun rememberParticipantsPalette(colors: AppThemeColors): ParticipantsPalette {
    return ParticipantsPalette(
        backgroundBrush = colors.backgroundGradient,
        headerBackground = colors.glassSurface.copy(alpha = 0.95f),
        headerText = colors.primaryText,
        primaryText = colors.primaryText,
        secondaryText = colors.secondaryText,
        cardBackground = colors.glassSurface.copy(alpha = 0.95f),
        avatarPlaceholder = colors.accentPurple.copy(alpha = 0.3f),
        avatarText = colors.primaryText,
        errorText = Color.Red,
        adminBadgeBackground = colors.accentPurple,
        adminBadgeText = colors.iconOnAccent
    )
}

