package com.example.damandroid.presentation.notifications.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.damandroid.domain.model.NotificationItem
import com.example.damandroid.presentation.notifications.model.NotificationsUiState
import com.example.damandroid.presentation.notifications.viewmodel.NotificationsViewModel
import com.example.damandroid.presentation.notifications.ui.components.NotificationsContent

@Composable
fun NotificationsRoute(
    viewModel: NotificationsViewModel,
    onBack: (() -> Unit)? = null,
    onStartChat: ((String, String) -> Unit)? = null, // userId, userName
    isVisible: Boolean = true, // Indique si l'écran est visible (onglet actif)
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Rafraîchir automatiquement les notifications à chaque fois que l'écran devient visible
    // LaunchedEffect se déclenche à chaque fois que isVisible devient true
    // Cela garantit que les nouvelles notifications (badges, level up, etc.) sont affichées
    // même après avoir créé une activité ou complété une activité
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Attendre un peu pour laisser le backend traiter les nouvelles notifications
            // après création d'activité ou complétion
            kotlinx.coroutines.delay(800)
            viewModel.refresh()
        }
    }
    
    NotificationsScreen(
        state = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onNotificationRead = viewModel::onNotificationRead,
        onMarkAllRead = viewModel::onMarkAllRead,
        onLikeBack = { profileId ->
            viewModel.likeBack(profileId) {
                // Callback appelé si c'est un match
                // Le refresh mettra à jour l'UI pour afficher "Welcome" et "Chat"
            }
        },
        onStartChat = onStartChat,
        modifier = modifier
    )
}

@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onBack: (() -> Unit)?,
    onRefresh: () -> Unit,
    onNotificationRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onLikeBack: ((String) -> Unit)? = null,
    onStartChat: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(message = state.error, onRetry = onRefresh, modifier = modifier)
        state.overview != null -> NotificationsContent(
            notifications = state.overview.notifications,
            unreadCount = state.overview.unreadCount,
            onBack = onBack,
            onNotificationRead = onNotificationRead,
            onMarkAllRead = onMarkAllRead,
            onLikeBack = onLikeBack,
            onStartChat = onStartChat,
            modifier = modifier
        )
        else -> ErrorState(
            message = "No notifications available",
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

