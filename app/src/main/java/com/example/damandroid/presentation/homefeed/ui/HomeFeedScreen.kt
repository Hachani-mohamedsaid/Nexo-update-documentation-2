package com.example.damandroid.presentation.homefeed.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.damandroid.domain.model.HomeActivity
import com.example.damandroid.presentation.homefeed.model.HomeFeedUiState
import com.example.damandroid.presentation.homefeed.ui.components.HomeFeedContent
import com.example.damandroid.presentation.homefeed.viewmodel.HomeFeedViewModel
import com.example.damandroid.presentation.achievements.viewmodel.AchievementsViewModel
import com.example.damandroid.presentation.achievements.ui.components.BadgeUnlockedDialog
import com.example.damandroid.presentation.achievements.ui.components.LevelUpDialog
import com.example.damandroid.presentation.achievements.ui.components.ChallengeCompletedDialog
import com.example.damandroid.presentation.achievements.NotificationDisplayStore
import kotlinx.coroutines.launch

@Composable
fun HomeFeedRoute(
    viewModel: HomeFeedViewModel,
    achievementsViewModel: AchievementsViewModel? = null,
    onActivityClick: (HomeActivity) -> Unit,
    onSearchClick: (() -> Unit)?,
    onAISuggestionsClick: (() -> Unit)?,
    onQuickMatchClick: (() -> Unit)?,
    onAIMatchmakerClick: (() -> Unit)?,
    onEventDetailsClick: ((String) -> Unit)?,
    onCreateClick: (() -> Unit)?,
    onNotificationsClick: (() -> Unit)?,
    onChatClick: ((String, String, String?, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Store pour persister les notifications déjà affichées
    val notificationStore = remember { NotificationDisplayStore(context) }
    
    // Gestion des notifications de badges, montées de niveau et challenges complétés
    achievementsViewModel?.let { achievementsVM ->
        val newBadges by achievementsVM.newBadgesUnlocked.collectAsState()
        val levelUp by achievementsVM.levelUpEvent.collectAsState()
        
        // Écouter les challenges complétés
        var currentChallengeCompleted by remember { mutableStateOf<com.example.damandroid.presentation.achievements.viewmodel.ChallengeCompletedEvent?>(null) }
        
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

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearSuccessMessage()
            }
        }
    }

    HomeFeedScreen(
        state = uiState,
        snackbarHostState = snackbarHostState,
        achievementsViewModel = achievementsViewModel,
        onRetry = viewModel::loadFeed,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterSportChange = viewModel::onFilterSportChange,
        onFilterDistanceChange = viewModel::onFilterDistanceChange,
        onFilterSheetToggle = viewModel::onFilterSheetToggle,
        onToggleSaved = viewModel::onToggleSaved,
        onActivityClick = onActivityClick,
        onSearchClick = onSearchClick,
        onAISuggestionsClick = onAISuggestionsClick,
        onQuickMatchClick = onQuickMatchClick,
        onAIMatchmakerClick = onAIMatchmakerClick,
        onEventDetailsClick = onEventDetailsClick,
        onCreateClick = onCreateClick,
        onNotificationsClick = onNotificationsClick,
        viewModel = viewModel,
        onChatClick = onChatClick,
        modifier = modifier
    )
}

@Composable
fun HomeFeedScreen(
    state: HomeFeedUiState,
    snackbarHostState: SnackbarHostState,
    achievementsViewModel: AchievementsViewModel? = null,
    onRetry: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterSportChange: (String) -> Unit,
    onFilterDistanceChange: (Float) -> Unit,
    onFilterSheetToggle: (Boolean) -> Unit,
    onToggleSaved: (String) -> Unit,
    onActivityClick: (HomeActivity) -> Unit,
    onSearchClick: (() -> Unit)?,
    onAISuggestionsClick: (() -> Unit)?,
    onQuickMatchClick: (() -> Unit)?,
    onAIMatchmakerClick: (() -> Unit)?,
    onEventDetailsClick: ((String) -> Unit)?,
    onCreateClick: (() -> Unit)?,
    onNotificationsClick: (() -> Unit)?,
    viewModel: HomeFeedViewModel,
    onChatClick: ((String, String, String?, Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var activityForChat by remember { mutableStateOf<HomeActivity?>(null) }
    when {
        state.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error != null && state.activities.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.error)
            }
        }

        else -> {
            HomeFeedContent(
                state = state,
                onSearchQueryChange = onSearchQueryChange,
                onFilterSportChange = onFilterSportChange,
                onFilterDistanceChange = onFilterDistanceChange,
                onFilterSheetToggle = onFilterSheetToggle,
                onToggleSaved = onToggleSaved,
                onActivityClick = onActivityClick,
                onSearchClick = onSearchClick,
                onAISuggestionsClick = onAISuggestionsClick,
                onQuickMatchClick = onQuickMatchClick,
                onAIMatchmakerClick = onAIMatchmakerClick,
                onEventDetailsClick = onEventDetailsClick,
                onCreateClick = onCreateClick,
                onNotificationsClick = onNotificationsClick,
                onActivityTypeFilterChange = { filter ->
                    viewModel.onActivityTypeFilterChange(filter)
                },
                onChatNowClick = { activity ->
                    // Stocker l'activité pour afficher l'alerte
                    activityForChat = activity
                },
                modifier = modifier,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            )
        }
    }

    // Afficher l'alerte si une activité est sélectionnée pour le chat
    activityForChat?.let { activity ->
        JoinGroupAlertHandler(
            activity = activity,
            onConfirm = {
                // Créer/rejoindre le chat de groupe
                createOrJoinGroupChat(activity, onChatClick, viewModel, coroutineScope)
                activityForChat = null
            },
            onDismiss = {
                activityForChat = null
            }
        )
    }
}

/**
 * Handler pour afficher une alerte de confirmation pour rejoindre le groupe
 */
@Composable
private fun JoinGroupAlertHandler(
    activity: HomeActivity,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rejoindre le groupe ?")
        },
        text = {
            Text("Voulez-vous rejoindre le groupe de chat pour \"${activity.title}\" ?")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Oui")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Non")
            }
        }
    )
}

/**
 * Créer ou rejoindre un chat de groupe pour une activité
 * Cette fonction est appelée depuis le callback de confirmation de l'alerte
 * Elle rejoint d'abord l'activité si nécessaire, puis crée/rejoint le chat de groupe
 */
private fun createOrJoinGroupChat(
    activity: HomeActivity,
    onChatClick: ((String, String, String?, Boolean) -> Unit)?,
    viewModel: HomeFeedViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    coroutineScope.launch {
        val repository = com.example.damandroid.api.ActivityRoomRepository()
        
        // Étape 1: Rejoindre l'activité si l'utilisateur n'est pas déjà participant
        val joinResult = repository.joinActivity(activity.id)
        when (joinResult) {
            is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Success -> {
                // L'utilisateur a rejoint l'activité avec succès (ou était déjà participant)
                // Étape 2: Créer/rejoindre le chat de groupe
                when (val chatResult = repository.createOrGetActivityGroupChat(activity.id)) {
                    is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Success -> {
                        val chat = chatResult.data.chat
                        // Vérifier que l'utilisateur actuel est dans les participants
                        // Gérer différents formats : id direct ou objet avec id
                        val currentUserId = com.example.damandroid.auth.UserSession.user?.id
                        val isUserInParticipants = if (currentUserId != null) {
                            chat.participants.any { participant ->
                                // Vérifier si l'ID correspond (gérer différents formats)
                                participant.id == currentUserId ||
                                participant.id.equals(currentUserId, ignoreCase = true)
                            }
                        } else {
                            false
                        }
                        
                        if (!isUserInParticipants) {
                            // L'utilisateur n'est pas dans les participants - problème backend
                            // Attendre plus longtemps et réessayer de créer/rejoindre le chat
                            kotlinx.coroutines.delay(1000)
                            when (val retryResult = repository.createOrGetActivityGroupChat(activity.id)) {
                                is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Success -> {
                                    val retryChat = retryResult.data.chat
                                    kotlinx.coroutines.delay(500)
                                    onChatClick?.invoke(
                                        retryChat.id,
                                        retryChat.groupName,
                                        retryChat.groupAvatar,
                                        retryChat.isGroup
                                    )
                                    viewModel.showSuccessMessage("Chat de groupe rejoint avec succès")
                                }
                                is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Error -> {
                                    viewModel.showSuccessMessage("Erreur: Le chat a été créé mais vous n'êtes pas encore ajouté comme participant. Veuillez réessayer dans quelques instants.")
                                }
                            }
                        } else {
                            // L'utilisateur est dans les participants, on peut naviguer
                            kotlinx.coroutines.delay(500)
                            onChatClick?.invoke(
                                chat.id,
                                chat.groupName,
                                chat.groupAvatar,
                                chat.isGroup
                            )
                            viewModel.showSuccessMessage("Chat de groupe rejoint avec succès")
                        }
                    }
                    is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Error -> {
                        viewModel.showSuccessMessage("Erreur lors de la création du chat: ${chatResult.message}")
                    }
                }
            }
            is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Error -> {
                // Si l'erreur indique que l'utilisateur est déjà participant, on continue quand même
                // (certains backends retournent une erreur dans ce cas)
                if (joinResult.message.contains("déjà participant", ignoreCase = true)) {
                    // L'utilisateur est déjà participant, on peut directement créer/rejoindre le chat
                    when (val chatResult = repository.createOrGetActivityGroupChat(activity.id)) {
                        is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Success -> {
                            val chat = chatResult.data.chat
                            // Vérifier que l'utilisateur actuel est dans les participants
                            val currentUserId = com.example.damandroid.auth.UserSession.user?.id
                            val isUserInParticipants = if (currentUserId != null) {
                                chat.participants.any { it.id == currentUserId }
                            } else {
                                false
                            }
                            
                            if (!isUserInParticipants) {
                                // L'utilisateur n'est pas dans les participants - problème backend
                                // Attendre plus longtemps et réessayer de créer/rejoindre le chat
                                kotlinx.coroutines.delay(1000)
                                when (val retryResult = repository.createOrGetActivityGroupChat(activity.id)) {
                                    is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Success -> {
                                        val retryChat = retryResult.data.chat
                                        kotlinx.coroutines.delay(500)
                                        onChatClick?.invoke(
                                            retryChat.id,
                                            retryChat.groupName,
                                            retryChat.groupAvatar,
                                            retryChat.isGroup
                                        )
                                        viewModel.showSuccessMessage("Chat de groupe rejoint avec succès")
                                    }
                                    is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Error -> {
                                        viewModel.showSuccessMessage("Erreur: Le chat a été créé mais vous n'êtes pas encore ajouté comme participant. Veuillez réessayer dans quelques instants.")
                                    }
                                }
                            } else {
                                // L'utilisateur est dans les participants, on peut naviguer
                                kotlinx.coroutines.delay(500)
                                onChatClick?.invoke(
                                    chat.id,
                                    chat.groupName,
                                    chat.groupAvatar,
                                    chat.isGroup
                                )
                                viewModel.showSuccessMessage("Chat de groupe rejoint avec succès")
                            }
                        }
                        is com.example.damandroid.api.ActivityRoomRepository.ActivityRoomResult.Error -> {
                            viewModel.showSuccessMessage("Erreur lors de la création du chat: ${chatResult.message}")
                        }
                    }
                } else {
                    // Autre erreur lors de la tentative de rejoindre l'activité
                    viewModel.showSuccessMessage("Erreur: ${joinResult.message}")
                }
            }
        }
    }
}

