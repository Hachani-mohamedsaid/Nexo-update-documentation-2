package com.example.damandroid.presentation.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import com.example.damandroid.domain.model.UserSearchResult
import com.example.damandroid.presentation.chat.viewmodel.UserSearchViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

@Composable
fun UserSearchRoute(
    viewModel: UserSearchViewModel,
    onBack: () -> Unit,
    onChatCreated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    UserSearchScreen(
        state = uiState,
        onQueryChange = viewModel::onQueryChange,
        onUserSelected = { userId ->
            viewModel.createChat(userId, onChatCreated)
        },
        onBack = onBack,
        modifier = modifier
    )
}

@Composable
fun UserSearchScreen(
    state: com.example.damandroid.presentation.chat.viewmodel.UserSearchUiState,
    onQueryChange: (String) -> Unit,
    onUserSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = rememberUserSearchPalette(rememberAppThemeColors(LocalThemeController.current.isDarkMode))
    var searchQuery by remember(state.query) { mutableStateOf(state.query) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.backgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            UserSearchHeader(
                query = searchQuery,
                onQueryChange = {
                    searchQuery = it
                    onQueryChange(it)
                },
                onBack = onBack,
                palette = palette
            )
            
            // Content
            when {
                state.isCreatingChat -> {
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
                        Text(
                            text = state.error ?: "Error",
                            color = palette.errorText
                        )
                    }
                }
                state.query.length < 2 -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Type at least 2 characters to search",
                            color = palette.hintText
                        )
                    }
                }
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.users.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No users found",
                            color = palette.hintText
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.users, key = { it.id }) { user ->
                            UserItem(
                                user = user,
                                onClick = { onUserSelected(user.id) },
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
private fun UserSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    palette: UserSearchPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = palette.headerBackground,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "New Chat",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.headerText,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search users...", color = palette.searchPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = palette.searchIcon
                    )
                },
                modifier = Modifier.fillMaxWidth(),
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
                singleLine = true
            )
        }
    }
}

@Composable
private fun UserItem(
    user: UserSearchResult,
    onClick: () -> Unit,
    palette: UserSearchPalette
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = palette.cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatar ?: user.profileImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.userNameText
                )
                if (user.email != null) {
                    Text(
                        text = user.email,
                        fontSize = 14.sp,
                        color = palette.userEmailText
                    )
                }
            }
        }
    }
}

// Palette for user search
data class UserSearchPalette(
    val backgroundBrush: Brush,
    val headerBackground: Color,
    val headerText: Color,
    val searchText: Color,
    val searchPlaceholder: Color,
    val searchIcon: Color,
    val searchContainer: Color,
    val cardBackground: Color,
    val userNameText: Color,
    val userEmailText: Color,
    val hintText: Color,
    val errorText: Color
)

@Composable
private fun rememberUserSearchPalette(colors: AppThemeColors): UserSearchPalette {
    return UserSearchPalette(
        backgroundBrush = colors.backgroundGradient,
        headerBackground = colors.glassSurface.copy(alpha = 0.95f),
        headerText = colors.primaryText,
        searchText = colors.primaryText,
        searchPlaceholder = colors.mutedText,
        searchIcon = colors.mutedText,
        searchContainer = colors.glassSurface,
        cardBackground = colors.glassSurface.copy(alpha = 0.95f),
        userNameText = colors.primaryText,
        userEmailText = colors.secondaryText,
        hintText = colors.mutedText,
        errorText = Color.Red
    )
}

