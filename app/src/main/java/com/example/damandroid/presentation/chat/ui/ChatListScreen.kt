package com.example.damandroid.presentation.chat.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.ChatPreview
import com.example.damandroid.presentation.chat.model.ChatListUiState
import com.example.damandroid.presentation.chat.viewmodel.ChatListViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun ChatListRoute(
    viewModel: ChatListViewModel,
    onChatSelected: (ChatPreview) -> Unit,
    onNewChatClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    ChatListScreen(
        state = uiState,
        onQueryChange = viewModel::onQueryChange,
        onChatSelected = onChatSelected,
        onNewChatClick = onNewChatClick,
        modifier = modifier
    )
}

@Composable
fun ChatListScreen(
    state: ChatListUiState,
    onQueryChange: (String) -> Unit,
    onChatSelected: (ChatPreview) -> Unit,
    onNewChatClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(message = state.error, modifier = modifier)
        else -> ChatListContent(
            query = state.query,
            chats = state.filteredChats,
            onQueryChange = onQueryChange,
            onChatSelected = onChatSelected,
            onNewChatClick = onNewChatClick,
            modifier = modifier
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
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message)
    }
}

@Composable
private fun ChatListContent(
    query: String,
    chats: List<ChatPreview>,
    onQueryChange: (String) -> Unit,
    onChatSelected: (ChatPreview) -> Unit,
    onNewChatClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val palette = rememberChatPalette(rememberAppThemeColors(LocalThemeController.current.isDarkMode))
    var searchQuery by remember(query) { mutableStateOf(query) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.backgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderSection(
                searchQuery = searchQuery,
                onSearchChange = {
                    searchQuery = it
                    onQueryChange(it)
                },
                onNewChatClick = onNewChatClick,
                palette = palette
            )

            if (chats.isEmpty()) {
                EmptyState(palette)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(chats, key = { it.id }) { chat ->
                        ChatCard(chat = chat, onClick = { onChatSelected(chat) }, palette = palette)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onNewChatClick: () -> Unit = {},
    palette: ChatPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Messages", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = palette.headerText)
            IconButton(onClick = onNewChatClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = palette.headerText
                )
            }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(text = "Search conversations...", color = palette.searchPlaceholder) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = palette.searchIcon) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = palette.searchText,
                unfocusedTextColor = palette.searchText,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = palette.searchText,
                focusedContainerColor = palette.searchContainer,
                unfocusedContainerColor = palette.searchContainer
            ),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
        )
    }
}

@Composable
private fun ChatCard(chat: ChatPreview, onClick: () -> Unit, palette: ChatPalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (chat.isGroup) {
                palette.groupCardBackground
            } else {
                palette.cardBackground
            }
        ),
        border = BorderStroke(
            width = if (chat.isGroup) 1.5.dp else 1.dp,
            color = if (chat.isGroup) {
                palette.groupCardBorder
            } else {
                palette.cardBorder
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (chat.isGroup) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Afficher les avatars groupés pour les chats de groupe, sinon un seul avatar
            Box {
                if (chat.isGroup && chat.participantAvatars.isNotEmpty()) {
                    GroupAvatars(
                        avatars = chat.participantAvatars,
                        modifier = Modifier.size(52.dp),
                        palette = palette
                    )
                } else {
                    AsyncImage(
                        model = chat.avatarUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(2.dp, palette.avatarBorder, CircleShape)
                    )
                }
                
                // Badge de groupe en bas à droite de l'avatar
                if (chat.isGroup) {
                    Surface(
                        shape = CircleShape,
                        color = palette.groupBadgeBackground,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomEnd)
                            .border(2.dp, palette.cardBackground, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Group",
                                modifier = Modifier.size(10.dp),
                                tint = palette.groupBadgeIcon
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (chat.isGroup) palette.groupChatName else palette.chatName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = chat.timestamp,
                        fontSize = 12.sp,
                        color = palette.chatTime
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.lastMessage,
                        fontSize = 14.sp,
                        color = palette.chatMessage,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chat.unreadCount > 0) {
                            Surface(shape = CircleShape, color = palette.unreadBadge) {
                                Box(
                                    modifier = Modifier.size(22.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.unreadText
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = palette.chevronTint
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupAvatars(
    avatars: List<String>,
    modifier: Modifier = Modifier,
    palette: ChatPalette
) {
    Box(modifier = modifier) {
        when {
            avatars.isEmpty() -> {
                // Avatar par défaut pour groupe
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = palette.groupAvatarPlaceholder
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Group",
                            modifier = Modifier.size(24.dp),
                            tint = palette.groupAvatarIcon
                        )
                    }
                }
            }
            avatars.size == 1 -> {
                AsyncImage(
                    model = avatars[0],
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.5.dp, palette.groupAvatarBorder, CircleShape),
                    onError = {
                        // Fallback si l'image ne charge pas
                    }
                )
            }
            avatars.size == 2 -> {
                // Deux avatars côte à côte avec séparation
                Row(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = avatars[0],
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.5.dp, palette.groupAvatarBorder, CircleShape),
                        onError = {}
                    )
                    Spacer(modifier = Modifier.width((-4).dp)) // Chevauchement léger
                    AsyncImage(
                        model = avatars[1],
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.5.dp, palette.groupAvatarBorder, CircleShape),
                        onError = {}
                    )
                }
            }
            else -> {
                // Trois avatars ou plus : grille 2x2
                Box(modifier = Modifier.fillMaxSize()) {
                    // Premier avatar (haut gauche)
                    AsyncImage(
                        model = avatars[0],
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .border(2.dp, palette.groupAvatarBorder, CircleShape),
                        onError = {}
                    )
                    // Deuxième avatar (haut droite)
                    if (avatars.size >= 2) {
                        AsyncImage(
                            model = avatars[1],
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .border(2.dp, palette.groupAvatarBorder, CircleShape),
                            onError = {}
                        )
                    }
                    // Troisième avatar (bas gauche) ou indicateur "+N" si plus de 3
                    if (avatars.size == 3) {
                        AsyncImage(
                            model = avatars[2],
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomStart)
                                .clip(CircleShape)
                                .border(2.dp, palette.groupAvatarBorder, CircleShape),
                            onError = {}
                        )
                    } else if (avatars.size > 3) {
                        // Afficher le 3ème avatar en bas gauche
                        AsyncImage(
                            model = avatars[2],
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomStart)
                                .clip(CircleShape)
                                .border(2.dp, palette.groupAvatarBorder, CircleShape),
                            onError = {}
                        )
                        // Afficher "+N" en bas droite si plus de 3 participants
                        Surface(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.BottomEnd),
                            shape = CircleShape,
                            color = palette.groupMoreBadgeBackground
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${avatars.size - 3}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.groupMoreBadgeText
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
private fun EmptyState(palette: ChatPalette) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "No conversations yet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = palette.headerText)
            Text(text = "Start a new chat or join an activity to make connections.", fontSize = 13.sp, color = palette.searchPlaceholder, textAlign = TextAlign.Center)
        }
    }
}

// region Palette

data class ChatPalette(
    val backgroundBrush: Brush,
    val headerText: Color,
    val searchText: Color,
    val searchPlaceholder: Color,
    val searchIcon: Color,
    val searchContainer: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val avatarBorder: Color,
    val chatName: Color,
    val chatTime: Color,
    val chatMessage: Color,
    val chevronTint: Color,
    val unreadBadge: Color,
    val unreadText: Color,
    // Couleurs pour les groupes
    val groupCardBackground: Color,
    val groupCardBorder: Color,
    val groupChatName: Color,
    val groupLabelBackground: Color,
    val groupLabelText: Color,
    val groupBadgeBackground: Color,
    val groupBadgeIcon: Color,
    val groupAvatarBorder: Color,
    val groupAvatarPlaceholder: Color,
    val groupAvatarIcon: Color,
    val groupMoreBadgeBackground: Color,
    val groupMoreBadgeText: Color
)

@Composable
private fun rememberChatPalette(colors: AppThemeColors): ChatPalette {
    return ChatPalette(
        backgroundBrush = colors.backgroundGradient,
        headerText = colors.primaryText,
        searchText = colors.primaryText,
        searchPlaceholder = colors.mutedText,
        searchIcon = colors.mutedText,
        searchContainer = colors.glassSurface,
        cardBackground = colors.glassSurface.copy(alpha = 0.95f),
        cardBorder = colors.glassBorder,
        avatarBorder = colors.glassSurface,
        chatName = colors.primaryText,
        chatTime = colors.secondaryText,
        chatMessage = colors.secondaryText,
        chevronTint = colors.mutedText,
        unreadBadge = colors.accentGreen,
        unreadText = colors.iconOnAccent,
        // Couleurs pour les groupes
        groupCardBackground = colors.glassSurface.copy(alpha = 0.98f),
        groupCardBorder = colors.accentPurple.copy(alpha = 0.3f),
        groupChatName = colors.primaryText,
        groupLabelBackground = colors.accentPurple.copy(alpha = 0.15f),
        groupLabelText = colors.accentPurple,
        groupBadgeBackground = colors.accentPurple,
        groupBadgeIcon = colors.iconOnAccent,
        groupAvatarBorder = Color.White,
        groupAvatarPlaceholder = colors.accentPurple.copy(alpha = 0.2f),
        groupAvatarIcon = colors.accentPurple,
        groupMoreBadgeBackground = colors.accentBlue.copy(alpha = 0.8f),
        groupMoreBadgeText = Color.White
    )
}

// endregion

