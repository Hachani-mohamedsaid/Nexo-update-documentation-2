package com.example.damandroid.presentation.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.ChatMessage
import com.example.damandroid.presentation.chat.viewmodel.ChatViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import kotlinx.coroutines.launch

@Composable
fun ChatRoute(
    viewModel: ChatViewModel,
    chatName: String,
    chatAvatar: String?,
    chatId: String,
    isGroup: Boolean = false,
    onBack: () -> Unit,
    onViewParticipants: (() -> Unit)? = null,
    onLeaveGroup: (() -> Unit)? = null,
    onViewProfile: ((String) -> Unit)? = null,
    onGetParticipants: suspend (String) -> List<com.example.damandroid.domain.model.ChatParticipant>? = { _ -> null },
    chatRepository: com.example.damandroid.domain.repository.ChatRepository? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Récupérer l'ID de l'autre utilisateur pour les chats individuels
    var otherUserId by remember { mutableStateOf<String?>(null) }
    
    // Récupérer l'activityId et le nombre de participants pour les groupes d'activité
    var activityParticipantsCount by remember { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(chatId, isGroup) {
        if (!isGroup && onViewProfile != null) {
            // Pour un chat individuel, récupérer l'ID de l'autre utilisateur depuis les participants
            try {
                val participants = onGetParticipants(chatId)
                if (participants != null) {
                    val currentUserId = com.example.damandroid.auth.UserSession.user?.id
                    // Trouver l'autre participant (pas l'utilisateur actuel)
                    otherUserId = participants.firstOrNull { it.id != currentUserId }?.id
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatRoute", "Error fetching participants: ${e.message}", e)
            }
        }
        
        // Pour les groupes d'activité, récupérer l'activityId et le nombre de participants
        if (isGroup && chatRepository != null) {
            try {
                val chat = chatRepository.getChat(chatId)
                val activityId = chat.activityId
                if (activityId != null) {
                    // Récupérer l'activité pour obtenir le nombre de participants
                    val activityRepository = com.example.damandroid.api.ActivityRoomRepository()
                    when (val result = activityRepository.getActivity(activityId)) {
                        is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Success -> {
                            val activityResponse = result.data
                            // Utiliser participantIds pour obtenir le nombre réel de participants
                            val participantIds = activityResponse.participantIds
                            activityParticipantsCount = participantIds?.size ?: 0
                        }
                        else -> {
                            activityParticipantsCount = null
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatRoute", "Error fetching activity participants: ${e.message}", e)
                activityParticipantsCount = null
            }
        }
    }
    
    ChatScreen(
        state = uiState,
        chatName = chatName,
        chatAvatar = chatAvatar,
        chatId = chatId,
        isGroup = isGroup,
        activityParticipantsCount = activityParticipantsCount,
        onSendMessage = viewModel::sendMessage,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onViewParticipants = onViewParticipants,
        onLeaveGroup = onLeaveGroup,
        onViewProfile = if (!isGroup && otherUserId != null) {
            { onViewProfile?.invoke(otherUserId!!) }
        } else null,
        modifier = modifier
    )
}

@Composable
fun ChatScreen(
    state: com.example.damandroid.presentation.chat.viewmodel.ChatUiState,
    chatName: String,
    chatAvatar: String?,
    chatId: String,
    isGroup: Boolean = false,
    activityParticipantsCount: Int? = null,
    onSendMessage: (String) -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onViewParticipants: (() -> Unit)? = null,
    onLeaveGroup: (() -> Unit)? = null,
    onViewProfile: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = rememberChatMessagePalette(rememberAppThemeColors(LocalThemeController.current.isDarkMode))
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Scroll to bottom when new messages arrive
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(state.messages.size - 1)
            }
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.backgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            ChatHeader(
                chatName = chatName,
                chatAvatar = chatAvatar,
                isGroup = isGroup,
                activityParticipantsCount = activityParticipantsCount,
                onBack = onBack,
                onViewParticipants = onViewParticipants,
                onLeaveGroup = onLeaveGroup,
                onViewProfile = onViewProfile,
                palette = palette
            )
            
            // Messages
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error ?: "Error",
                                color = palette.errorText
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRefresh) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.messages, key = { it.id }) { message ->
                            MessageBubble(
                                message = message,
                                palette = palette,
                                isGroup = isGroup
                            )
                        }
                    }
                }
            }
            
            // Input field
            MessageInput(
                text = messageText,
                onTextChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText)
                        messageText = ""
                    }
                },
                isSending = state.isSending,
                palette = palette
            )
        }
    }
}

@Composable
private fun ChatHeader(
    chatName: String,
    chatAvatar: String?,
    isGroup: Boolean = false,
    activityParticipantsCount: Int? = null,
    onBack: () -> Unit,
    onViewParticipants: (() -> Unit)? = null,
    onLeaveGroup: (() -> Unit)? = null,
    onViewProfile: (() -> Unit)? = null,
    palette: ChatMessagePalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = palette.headerBackground,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                if (chatAvatar != null && chatAvatar.isNotEmpty()) {
                    var imageLoadError by remember { mutableStateOf(false) }
                    
                    if (!imageLoadError) {
                        AsyncImage(
                            model = chatAvatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            onError = {
                                imageLoadError = true
                            }
                        )
                    }
                    
                    // Afficher un avatar par défaut si l'image n'a pas pu être chargée
                    if (imageLoadError) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            color = palette.avatarPlaceholder
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = chatName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.avatarText
                                )
                            }
                        }
                    }
                }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chatName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.headerText
                )
                if (isGroup) {
                    Text(
                        text = if (activityParticipantsCount != null) {
                            "$activityParticipantsCount participant${if (activityParticipantsCount > 1) "s" else ""}"
                        } else {
                            "membres" // Fallback si on ne peut pas récupérer le nombre
                        },
                        fontSize = 12.sp,
                        color = palette.timeText
                    )
                }
            }
            // Menu trois points pour tous les chats (individuels et groupes)
            if ((!isGroup && onViewProfile != null) || (isGroup && (onViewParticipants != null || onLeaveGroup != null))) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = palette.headerText
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        // Pour les chats individuels : Détails du profil
                        if (!isGroup) {
                            onViewProfile?.let {
                                DropdownMenuItem(
                                    text = { Text("Détails du profil", color = palette.headerText) },
                                    onClick = {
                                        expanded = false
                                        it()
                                    }
                                )
                            }
                        }
                        // Pour les groupes : Voir les participants et Quitter le groupe
                        if (isGroup) {
                            onViewParticipants?.let {
                                DropdownMenuItem(
                                    text = { Text("Voir les participants", color = palette.headerText) },
                                    onClick = {
                                        expanded = false
                                        it()
                                    }
                                )
                            }
                            onLeaveGroup?.let {
                                DropdownMenuItem(
                                    text = { Text("Quitter le groupe", color = Color.Red) },
                                    onClick = {
                                        expanded = false
                                        it()
                                    }
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
private fun MessageBubble(
    message: ChatMessage,
    palette: ChatMessagePalette,
    isGroup: Boolean = false
) {
    val isMe = message.sender == "me"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar et nom pour les messages des autres (ou tous les messages dans un groupe)
        if (!isMe) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // Avatar
                if (message.avatar != null && message.avatar.isNotEmpty()) {
                    var imageLoadError by remember { mutableStateOf(false) }
                    
                    if (!imageLoadError) {
                        AsyncImage(
                            model = message.avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, palette.otherMessageBackground, CircleShape),
                            onError = {
                                imageLoadError = true
                            }
                        )
                    }
                    
                    // Afficher l'avatar par défaut si l'image n'a pas pu être chargée
                    if (imageLoadError) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = palette.avatarPlaceholder
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = message.senderName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.avatarText
                                )
                            }
                        }
                    }
                } else {
                    // Avatar par défaut avec initiale
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = palette.avatarPlaceholder
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = message.senderName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.avatarText
                            )
                        }
                    }
                }
            }
        }
        
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            // Nom de l'expéditeur (pour les groupes ou les messages des autres)
            if ((isGroup || !isMe) && message.senderName != null && message.senderName.isNotEmpty()) {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.senderNameText,
                    modifier = Modifier.padding(
                        bottom = 4.dp,
                        start = if (isMe) 0.dp else 0.dp,
                        end = if (isMe) 0.dp else 0.dp
                    )
                )
            }
            
            // Bulle de message
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                color = if (isMe) palette.myMessageBackground else palette.otherMessageBackground,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = if (isMe) palette.myMessageText else palette.otherMessageText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
            
            // Timestamp
            Text(
                text = message.time,
                fontSize = 11.sp,
                color = palette.timeText,
                modifier = Modifier.padding(
                    top = 4.dp,
                    start = if (isMe) 0.dp else 4.dp,
                    end = if (isMe) 4.dp else 0.dp
                )
            )
        }
        
        // Espace pour aligner avec l'avatar si c'est un message de l'utilisateur
        if (isMe) {
            Spacer(modifier = Modifier.width(44.dp)) // Largeur de l'avatar + espacement
        }
    }
}

@Composable
private fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    palette: ChatMessagePalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = palette.inputBackground,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...", color = palette.inputPlaceholder) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = palette.inputText,
                    unfocusedTextColor = palette.inputText,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = palette.inputText,
                    focusedContainerColor = palette.inputFieldBackground,
                    unfocusedContainerColor = palette.inputFieldBackground
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                enabled = !isSending
            )
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) palette.sendButtonEnabled else palette.sendButtonDisabled
                    )
                }
            }
        }
    }
}

// Palette for chat messages
data class ChatMessagePalette(
    val backgroundBrush: Brush,
    val headerBackground: Color,
    val headerText: Color,
    val myMessageBackground: Color,
    val myMessageText: Color,
    val otherMessageBackground: Color,
    val otherMessageText: Color,
    val senderNameText: Color,
    val timeText: Color,
    val inputBackground: Color,
    val inputFieldBackground: Color,
    val inputText: Color,
    val inputPlaceholder: Color,
    val sendButtonEnabled: Color,
    val sendButtonDisabled: Color,
    val errorText: Color,
    val avatarPlaceholder: Color,
    val avatarText: Color
)

@Composable
private fun rememberChatMessagePalette(colors: AppThemeColors): ChatMessagePalette {
    return ChatMessagePalette(
        backgroundBrush = colors.backgroundGradient,
        headerBackground = colors.glassSurface.copy(alpha = 0.95f),
        headerText = colors.primaryText,
        myMessageBackground = colors.accentGreen,
        myMessageText = colors.iconOnAccent,
        otherMessageBackground = colors.glassSurface,
        otherMessageText = colors.primaryText,
        senderNameText = colors.secondaryText,
        timeText = colors.mutedText,
        inputBackground = colors.glassSurface.copy(alpha = 0.95f),
        inputFieldBackground = colors.glassSurface,
        inputText = colors.primaryText,
        inputPlaceholder = colors.mutedText,
        sendButtonEnabled = colors.accentGreen,
        sendButtonDisabled = colors.mutedText,
        errorText = Color.Red,
        avatarPlaceholder = colors.accentPurple.copy(alpha = 0.3f),
        avatarText = colors.primaryText
    )
}

