package com.example.damandroid

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import com.example.damandroid.auth.GoogleSignInHelper
import com.example.damandroid.auth.FacebookSignInHelper
import com.example.damandroid.auth.UserSession
import com.example.damandroid.ui.theme.DamAndroidTheme
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.ThemeController
import com.example.damandroid.ui.theme.ThemePreferences
import com.example.damandroid.api.RetrofitClient
import com.example.damandroid.data.datasource.*
import com.example.damandroid.data.repository.*
import com.example.damandroid.domain.usecase.*
import com.example.damandroid.domain.usecase.GetUserProfileById
import com.example.damandroid.presentation.discover.ui.DiscoverRoute
import com.example.damandroid.presentation.discover.viewmodel.DiscoverViewModel
import com.example.damandroid.presentation.notifications.ui.NotificationsRoute
import com.example.damandroid.presentation.notifications.viewmodel.NotificationsViewModel
import com.example.damandroid.presentation.profile.ui.ProfileRoute
import com.example.damandroid.presentation.profile.ui.EditProfileRoute
import com.example.damandroid.presentation.profile.ui.UserProfileDetailsRoute
import com.example.damandroid.presentation.profile.viewmodel.ProfileViewModel
import com.example.damandroid.presentation.settings.ui.SettingsRoute
import com.example.damandroid.presentation.settings.viewmodel.SettingsViewModel
import com.example.damandroid.presentation.chat.ui.ChatListRoute
import com.example.damandroid.presentation.chat.ui.ChatRoute
import com.example.damandroid.presentation.chat.ui.ParticipantsRoute
import com.example.damandroid.presentation.chat.ui.UserSearchRoute
import com.example.damandroid.presentation.chat.viewmodel.ChatListViewModel
import com.example.damandroid.presentation.chat.viewmodel.ChatViewModel
import com.example.damandroid.presentation.chat.viewmodel.UserSearchViewModel
import com.example.damandroid.presentation.ai.ui.AICoachRoute
import com.example.damandroid.presentation.ai.viewmodel.AICoachViewModel
import com.example.damandroid.presentation.quickmatch.ui.QuickMatchRoute
import com.example.damandroid.presentation.quickmatch.viewmodel.QuickMatchViewModel
import com.example.damandroid.presentation.homefeed.ui.HomeFeedRoute
import com.example.damandroid.presentation.homefeed.viewmodel.HomeFeedViewModel
import com.example.damandroid.presentation.createactivity.ui.CreateActivityRoute
import com.example.damandroid.presentation.createactivity.viewmodel.CreateActivityViewModel
import com.example.damandroid.presentation.applyverification.ui.ApplyVerificationRoute
import com.example.damandroid.presentation.applyverification.viewmodel.ApplyVerificationViewModel
import com.example.damandroid.presentation.achievements.ui.AchievementsRoute
import com.example.damandroid.presentation.achievements.viewmodel.AchievementsViewModel
import com.example.damandroid.presentation.eventdetails.ui.EventDetailsRoute
import com.example.damandroid.presentation.eventdetails.viewmodel.EventDetailsViewModel
import com.example.damandroid.presentation.aimatchmaker.ui.AIMatchmakerRoute
import com.example.damandroid.presentation.aimatchmaker.viewmodel.AIMatchmakerViewModel
import com.example.damandroid.presentation.aisuggestions.ui.AISuggestionsRoute
import com.example.damandroid.presentation.aisuggestions.viewmodel.AISuggestionsViewModel
import com.example.damandroid.presentation.coachprofile.ui.CoachProfileRoute
import com.example.damandroid.presentation.coachprofile.viewmodel.CoachProfileViewModel
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch
import com.example.damandroid.presentation.settings.ui.ChangePasswordRoute
import com.example.damandroid.domain.model.HomeActivity
import com.example.damandroid.presentation.homefeed.ui.ActivityRoomRoute
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private lateinit var facebookSignInHelper: FacebookSignInHelper
    private var onGoogleSignInResult: ((GoogleSignInAccount?) -> Unit)? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        googleSignInHelper = GoogleSignInHelper(this)
        facebookSignInHelper = FacebookSignInHelper(this)
        
        // Gérer le callback OAuth Strava si l'app est lancée depuis un deep link
        handleStravaOAuthCallback(intent)

        setContent {
            val themePreferences = remember { ThemePreferences(applicationContext) }
            val systemDark = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(themePreferences.getSavedDarkMode() ?: systemDark) }

            LaunchedEffect(isDarkMode) {
                themePreferences.saveDarkMode(isDarkMode)
            }

            CompositionLocalProvider(
                LocalThemeController provides ThemeController(
                    isDarkMode = isDarkMode,
                    setDarkMode = { isDarkMode = it }
                )
            ) {
                DamAndroidTheme(darkTheme = isDarkMode) {
                    MainApp(
                        onGoogleSignInRequest = { callback ->
                            onGoogleSignInResult = callback
                            val signInIntent = googleSignInHelper.getSignInClient().signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                        facebookSignInHelper = facebookSignInHelper
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookSignInHelper.getCallbackManager().onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Gérer le callback OAuth Strava si l'app est relancée depuis un deep link
        intent?.let { handleStravaOAuthCallback(it) }
    }
    
    /**
     * Gère le callback OAuth Strava depuis le deep link
     */
    private fun handleStravaOAuthCallback(intent: Intent) {
        val data = intent.data
        if (data != null && data.scheme == "nexofitness" && data.host == "strava") {
            android.util.Log.d("MainActivity", "✅ Strava OAuth callback received: $data")
            android.util.Log.d("MainActivity", "   Scheme: ${data.scheme}, Host: ${data.host}, Path: ${data.path}")
            
            val oAuthHelper = com.example.damandroid.auth.StravaOAuthHelper(this)
            val code = oAuthHelper.handleCallback(data)
            
            if (code != null) {
                android.util.Log.d("MainActivity", "✅ Authorization code extracted: $code")
                // Échanger le code contre un token d'accès
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    try {
                        android.util.Log.d("MainActivity", "🔄 Exchanging authorization code for access token...")
                        val result = oAuthHelper.exchangeCodeForToken(code)
                        when (result) {
                            is com.example.damandroid.auth.StravaTokenResult.Success -> {
                                android.util.Log.d("MainActivity", "✅✅✅ Strava OAuth success! Token received (length: ${result.accessToken.length})")
                                // La synchronisation est maintenant réussie
                                val syncManager = com.example.damandroid.data.datasource.FitnessSyncManager(this@MainActivity)
                                syncManager.markAsSynced()
                                android.util.Log.d("MainActivity", "✅ Strava marked as synced. Last sync time: ${syncManager.getLastSyncTime()}")
                            }
                            is com.example.damandroid.auth.StravaTokenResult.Error -> {
                                android.util.Log.e("MainActivity", "❌ Strava OAuth error: ${result.message}")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "❌ Error exchanging Strava OAuth code", e)
                    }
                }
            } else {
                android.util.Log.w("MainActivity", "⚠️ No authorization code found in callback URI")
            }
        } else {
            if (data != null) {
                android.util.Log.d("MainActivity", "Ignoring non-Strava deep link: $data")
            }
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        val callback = onGoogleSignInResult
        onGoogleSignInResult = null

        if (callback != null) {
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                callback(account)
            } catch (e: com.google.android.gms.common.api.ApiException) {
                val errorMessage = when (e.statusCode) {
                    10 -> {
                        "DEVELOPER_ERROR: Please check Google Sign-In configuration:\n" +
                        "1. Verify SHA-1 fingerprint is added in Google Cloud Console\n" +
                        "2. Verify package name matches: com.example.damandroid\n" +
                        "3. Wait 5-10 minutes after adding SHA-1 for changes to propagate"
                    }
                    12501 -> "Sign-in cancelled by user"
                    7 -> "Network error. Please check your internet connection"
                    8 -> "Internal error. Please try again"
                    else -> "Google Sign-In failed (Error ${e.statusCode})"
                }
                android.util.Log.e("MainActivity", "Google Sign-In failed: ${e.statusCode} - $errorMessage", e)
                callback(null)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainApp(
    onGoogleSignInRequest: ((GoogleSignInAccount?) -> Unit) -> Unit = {},
    facebookSignInHelper: FacebookSignInHelper? = null
) {
    val context = LocalContext.current
    var showSplash by remember { mutableStateOf(true) }
    var showOnboarding by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var showSignUp by remember { mutableStateOf(false) }
    var showResetPassword by remember { mutableStateOf(false) }

    val googleSignInHelper = remember { GoogleSignInHelper(context) }

    when {
        showSplash -> SplashScreen(
            onComplete = {
                showSplash = false
                showOnboarding = true
            },
            logoResId = R.drawable.nexo_logo
        )
        showOnboarding -> OnboardingScreens(onComplete = { showOnboarding = false })
        showResetPassword -> ResetPasswordPage(onBackToLogin = { showResetPassword = false })
        showSignUp -> SignUpPage(
            onSignUp = {
                showSignUp = false
                isLoggedIn = true
            },
            onLoginClick = { showSignUp = false },
            onGoogleSignInRequest = onGoogleSignInRequest,
            googleSignInHelper = googleSignInHelper,
            facebookSignInHelper = facebookSignInHelper
        )
        !isLoggedIn -> LoginScreen(
            onLogin = { isLoggedIn = true },
            onSignUpClick = { showSignUp = true },
            onForgotPasswordClick = { showResetPassword = true },
            logoResId = R.drawable.nexo_logo,
            onGoogleSignInRequest = onGoogleSignInRequest,
            googleSignInHelper = googleSignInHelper,
            facebookSignInHelper = facebookSignInHelper
        )
        else -> MainHomeScreen(onLogout = {
            UserSession.clear()
            isLoggedIn = false
        })
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainHomeScreen(
    onLogout: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf("home") }
    var overlay by remember { mutableStateOf<OverlayScreen?>(null) }

    val discoverViewModel = remember {
        DiscoverViewModel(GetDiscoverOverview(DiscoverRepositoryImpl(DiscoverRemoteDataSourceImpl())))
    }
    val notificationsRepository = remember { NotificationsRepositoryImpl(NotificationsRemoteDataSourceImpl()) }
    val notificationsViewModel = remember {
        NotificationsViewModel(
            getNotifications = GetNotifications(notificationsRepository),
            markNotificationAsRead = MarkNotificationAsRead(notificationsRepository),
            markAllNotificationsAsRead = MarkAllNotificationsAsRead(notificationsRepository),
            notificationsRepository = notificationsRepository
        )
    }
    val profileViewModel = remember {
        val profileRepository = ProfileRepositoryImpl(ProfileRemoteDataSourceImpl())
        ProfileViewModel(
            getCurrentUserProfile = GetCurrentUserProfile(profileRepository),
            updateUserProfile = UpdateUserProfile(profileRepository),
            uploadProfileImage = UploadProfileImage(profileRepository),
            changeUserPassword = ChangeUserPassword(profileRepository)
        )
    }
    val settingsRepository = remember { SettingsRepositoryImpl(SettingsRemoteDataSourceImpl()) }
    val settingsViewModel = remember {
        SettingsViewModel(
            getUserSettings = GetUserSettings(settingsRepository),
            updateSettingsToggle = UpdateSettingsToggle(settingsRepository)
        )
    }
    val homeFeedViewModel = remember {
        val remoteDataSource = HomeFeedRemoteDataSourceImpl()
        val repository = HomeFeedRepositoryImpl(remoteDataSource)
        HomeFeedViewModel(
            getHomeFeed = GetHomeFeed(repository),
            getMyActivities = GetMyActivities(repository),
            toggleActivitySaved = ToggleActivitySaved(repository)
        )
    }
    val chatRepository = remember { 
        ChatRepositoryImpl(
            ChatRemoteDataSourceImpl(RetrofitClient.chatApiService)
        ) 
    }
    val userSearchViewModel = remember { UserSearchViewModel(chatRepository) }
    val chatListViewModel = remember {
        ChatListViewModel(
            getChatPreviews = GetChatPreviews(chatRepository),
            searchChats = SearchChats(chatRepository)
        )
    }
    val aiCoachContext = LocalContext.current
    val aiCoachViewModel = remember(aiCoachContext) {
        AICoachViewModel(
            getAICoachOverview = GetAICoachOverview(
                AICoachRepositoryImpl(
                    AICoachRemoteDataSourceImpl(context = aiCoachContext)
                )
            ),
            context = aiCoachContext
        )
    }
    val quickMatchRepository = remember { QuickMatchRepositoryImpl(QuickMatchRemoteDataSourceImpl()) }
    val quickMatchContext = LocalContext.current
    val excludedProfilesStore = remember { 
        com.example.damandroid.presentation.quickmatch.data.ExcludedProfilesStore(quickMatchContext)
    }
    val quickMatchViewModel = remember {
        QuickMatchViewModel(
            getQuickMatchProfiles = GetQuickMatchProfiles(quickMatchRepository),
            likeProfileUseCase = LikeProfile(quickMatchRepository),
            passProfileUseCase = PassProfile(quickMatchRepository),
            excludedProfilesStore = excludedProfilesStore
        )
    }
    val createActivityRepository = remember { 
        CreateActivityRepositoryImpl(
            CreateActivityRemoteDataSourceImpl(RetrofitClient.activityApiService)
        ) 
    }
    val createActivityViewModel = remember {
        CreateActivityViewModel(
            getSportCategories = GetSportCategories(createActivityRepository),
            createActivity = CreateActivityUseCase(createActivityRepository)
        )
    }
    val verificationRepository = remember { VerificationRepositoryImpl(VerificationRemoteDataSourceImpl()) }
    val applyVerificationViewModel = remember {
        ApplyVerificationViewModel(
            getVerificationFormOptions = GetVerificationFormOptions(verificationRepository),
            submitVerificationApplication = SubmitVerificationApplication(verificationRepository)
        )
    }
    val achievementsViewModel = remember {
        val achievementsRepository = AchievementsRepositoryImpl(AchievementsRemoteDataSourceImpl())
        AchievementsViewModel(
            getAchievementsOverview = GetAchievementsOverview(achievementsRepository),
            repository = achievementsRepository
        )
    }
    val eventDetailsViewModel = remember {
        EventDetailsViewModel(
            getEventDetails = GetEventDetails(EventDetailsRepositoryImpl(EventDetailsRemoteDataSourceImpl()))
        )
    }
    val aiMatchmakerRepository = remember { com.example.damandroid.api.AIMatchmakerRepository() }
    val matchmakerViewModel = remember {
        AIMatchmakerViewModel(
            getRecommendations = GetMatchmakerRecommendations(AIMatchmakerRepositoryImpl(AIMatchmakerRemoteDataSourceImpl())),
            aiMatchmakerRepository = aiMatchmakerRepository
        )
    }
    val sessionsViewModel = remember {
        AISuggestionsViewModel(
            getSessionsRecommendation = GetSessionsRecommendation(AISuggestionsRepositoryImpl(AISuggestionsRemoteDataSourceImpl()))
        )
    }
    val coachProfileViewModel = remember {
        CoachProfileViewModel(
            getCoachProfile = GetCoachProfile(CoachProfileRepositoryImpl(CoachProfileRemoteDataSourceImpl()))
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val currentOverlay = overlay) {
            OverlayScreen.CreateActivity -> CreateActivityRoute(
                viewModel = createActivityViewModel,
                achievementsViewModel = achievementsViewModel,
                onBack = { overlay = null },
                onSuccess = {
                    overlay = null
                    activeTab = "home"
                    // Recharger le feed pour afficher la nouvelle activité
                    homeFeedViewModel.loadFeed()
                    // Afficher le message de succès sur la page home
                    homeFeedViewModel.showSuccessMessage("Activity created successfully!")
                },
                modifier = Modifier.fillMaxSize()
            )
            OverlayScreen.Discover -> DiscoverRoute(
                viewModel = discoverViewModel,
                onBack = { overlay = null },
                onCoachClick = { coachId -> overlay = OverlayScreen.CoachProfile(coachId) },
                onChatClick = { chatId, chatName, chatAvatar, isGroup ->
                    overlay = OverlayScreen.Chat(chatId, chatName, chatAvatar, isGroup)
                },
                modifier = Modifier.fillMaxSize()
            )
            is OverlayScreen.CoachProfile -> CoachProfileRoute(
                coachId = currentOverlay.coachId,
                viewModel = coachProfileViewModel,
                onBack = { overlay = null },
                onMessage = { /* TODO */ },
                onBookSession = { overlay = OverlayScreen.CreateActivity },
                modifier = Modifier.fillMaxSize()
            )
            is OverlayScreen.EventDetails -> EventDetailsRoute(
                eventId = currentOverlay.eventId,
                viewModel = eventDetailsViewModel,
                onBack = { overlay = null },
                onCoachProfile = { coachId ->
                    overlay = OverlayScreen.CoachProfile(coachId)
                },
                onMessageCoach = { /* TODO message coach */ },
                modifier = Modifier.fillMaxSize()
            )
            OverlayScreen.Achievements -> AchievementsRoute(
                viewModel = achievementsViewModel,
                onBack = { overlay = null },
                modifier = Modifier.fillMaxSize()
            )
            OverlayScreen.AICoach -> AICoachRoute(
                viewModel = aiCoachViewModel,
                onBack = { overlay = null },
                modifier = Modifier.fillMaxSize()
            )
            OverlayScreen.AIMatchmaker -> AIMatchmakerRoute(
                viewModel = matchmakerViewModel,
                onBack = { overlay = null },
                modifier = Modifier.fillMaxSize()
            )
            OverlayScreen.QuickMatch -> QuickMatchRoute(
                viewModel = quickMatchViewModel,
                onBack = { overlay = null },
                modifier = Modifier.fillMaxSize()
            )
            is OverlayScreen.ActivityRoom -> ActivityRoomRoute(
                activity = currentOverlay.activity,
                achievementsViewModel = achievementsViewModel,
                onBack = { overlay = null },
                onLeave = { overlay = null },
                onMarkComplete = { overlay = null },
                modifier = Modifier.fillMaxSize()
            )
            OverlayScreen.EditProfile -> EditProfileRoute(
                viewModel = profileViewModel,
                onBack = { overlay = null },
                onSave = { overlay = null },
                modifier = Modifier.fillMaxSize()
            )
            OverlayScreen.ChangePassword -> ChangePasswordRoute(
                viewModel = profileViewModel,
                onBack = { overlay = null },
                modifier = Modifier.fillMaxSize()
            )
            OverlayScreen.Settings -> SettingsRoute(
                viewModel = settingsViewModel,
                onBack = { overlay = null },
                onApplyVerification = { overlay = OverlayScreen.ApplyVerification },
                onEditProfile = { overlay = OverlayScreen.EditProfile },
                onChangePassword = { overlay = OverlayScreen.ChangePassword },
                onLogout = { onLogout() },
                modifier = Modifier.fillMaxSize()
            )
            OverlayScreen.ApplyVerification -> ApplyVerificationRoute(
                viewModel = applyVerificationViewModel,
                onBack = { overlay = null },
                modifier = Modifier.fillMaxSize()
            )
            is OverlayScreen.Chat -> {
                val chatViewModel = remember(currentOverlay.chatId) {
                    ChatViewModel(chatRepository, currentOverlay.chatId)
                }
                ChatRoute(
                    viewModel = chatViewModel,
                    chatName = currentOverlay.chatName,
                    chatAvatar = currentOverlay.chatAvatar,
                    chatId = currentOverlay.chatId,
                    isGroup = currentOverlay.isGroup,
                    onBack = { overlay = null },
                    onViewParticipants = {
                        overlay = OverlayScreen.ChatParticipants(
                            chatId = currentOverlay.chatId,
                            chatName = currentOverlay.chatName
                        )
                    },
                    onLeaveGroup = {
                        // Quitter le groupe et revenir à la liste des chats
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            try {
                                chatRepository.leaveGroup(currentOverlay.chatId)
                                // Rafraîchir la liste des chats
                                chatListViewModel.refresh()
                                // Revenir à la liste des chats
                                overlay = null
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Error leaving group: ${e.message}", e)
                                // Afficher une erreur ou un message à l'utilisateur
                            }
                        }
                    },
                    onViewProfile = { userId ->
                        overlay = OverlayScreen.UserProfileDetails(userId)
                    },
                    onGetParticipants = { chatId ->
                        try {
                            chatRepository.getParticipants(chatId)
                        } catch (e: Exception) {
                            null
                        }
                    },
                    chatRepository = chatRepository,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is OverlayScreen.ChatParticipants -> {
                var activityId by remember { mutableStateOf<String?>(null) }
                
                // Récupérer l'activityId depuis le chat
                LaunchedEffect(currentOverlay.chatId) {
                    try {
                        val chat = chatRepository.getChat(currentOverlay.chatId)
                        activityId = chat.activityId
                    } catch (e: Exception) {
                        // En cas d'erreur, on continue sans activityId
                        activityId = null
                    }
                }
                
                ParticipantsRoute(
                    chatId = currentOverlay.chatId,
                    chatName = currentOverlay.chatName,
                    onGetParticipants = chatRepository::getParticipants,
                    activityId = activityId,
                    onBack = { overlay = null },
                    modifier = Modifier.fillMaxSize()
                )
            }
            is OverlayScreen.UserProfileDetails -> {
                val profileRepository = remember { ProfileRepositoryImpl(ProfileRemoteDataSourceImpl()) }
                val getUserProfileById = remember {
                    GetUserProfileById(profileRepository)
                }
                UserProfileDetailsRoute(
                    userId = currentOverlay.userId,
                    getUserProfileById = getUserProfileById::invoke,
                    onBack = { overlay = null },
                    modifier = Modifier.fillMaxSize()
                )
            }
            OverlayScreen.UserSearch -> {
                val userSearchViewModel = remember {
                    UserSearchViewModel(chatRepository)
                }
                UserSearchRoute(
                    viewModel = userSearchViewModel,
                    onBack = { overlay = null },
                    onChatCreated = { chatId ->
                        // Find the chat to get its details
                        overlay = null
                        activeTab = "chat"
                        // Optionally navigate to the chat
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            null -> when (activeTab) {
                "home" -> HomeFeedRoute(
                    viewModel = homeFeedViewModel,
                    achievementsViewModel = achievementsViewModel,
                    onActivityClick = { activity -> overlay = OverlayScreen.ActivityRoom(activity) },
                    onSearchClick = { overlay = OverlayScreen.Discover },
                    onAISuggestionsClick = { activeTab = "map" },
                    onQuickMatchClick = { overlay = OverlayScreen.QuickMatch },
                    onAIMatchmakerClick = { overlay = OverlayScreen.AIMatchmaker },
                    onEventDetailsClick = { eventId -> overlay = OverlayScreen.EventDetails(eventId) },
                    onCreateClick = { overlay = OverlayScreen.CreateActivity },
                    onNotificationsClick = { activeTab = "notifications" },
                    onChatClick = { chatId, chatName, chatAvatar, isGroup ->
                        // Naviguer directement vers le chat de groupe avec les informations fournies
                        overlay = OverlayScreen.Chat(
                            chatId = chatId,
                            chatName = chatName,
                            chatAvatar = chatAvatar,
                            isGroup = isGroup
                        )
                        // Rafraîchir la liste des chats en arrière-plan pour qu'elle soit à jour
                        chatListViewModel.refresh()
                    },
                    modifier = Modifier.fillMaxSize()
                )
                "map" -> AISuggestionsRoute(
                    viewModel = sessionsViewModel,
                    onBack = { activeTab = "home" },
                    modifier = Modifier.fillMaxSize()
                )
                "chat" -> ChatListRoute(
                    viewModel = chatListViewModel,
                    onChatSelected = { chat ->
                        overlay = OverlayScreen.Chat(
                            chatId = chat.id,
                            chatName = chat.name,
                            chatAvatar = chat.avatarUrl,
                            isGroup = chat.isGroup
                        )
                    },
                    onNewChatClick = {
                        overlay = OverlayScreen.UserSearch
                    },
                    modifier = Modifier.fillMaxSize()
                )
                "notifications" -> NotificationsRoute(
                    viewModel = notificationsViewModel,
                    onBack = { activeTab = "home" },
                    isVisible = activeTab == "notifications", // L'écran est visible quand l'onglet est actif
                    onStartChat = { userId, userName ->
                        // Créer un chat avec l'utilisateur et naviguer vers l'écran de chat
                        // Utiliser LaunchedEffect pour gérer la création du chat de manière asynchrone
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            try {
                                val chatId = chatRepository.createChat(
                                    com.example.damandroid.api.CreateChatRequest(
                                        participantIds = listOf(userId)
                                    )
                                )
                                overlay = OverlayScreen.Chat(
                                    chatId = chatId,
                                    chatName = userName,
                                    chatAvatar = null,
                                    isGroup = false
                                )
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Error creating chat: ${e.message}", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                "profile" -> ProfileRoute(
                    viewModel = profileViewModel,
                    onSettingsClick = { overlay = OverlayScreen.Settings },
                    onAchievementsClick = { overlay = OverlayScreen.Achievements },
                    onLogoutClick = { onLogout() },
                    modifier = Modifier.fillMaxSize()
                )
                else -> Unit
            }
        }

        if (overlay == null) {
            BottomNav(
                activeTab = activeTab,
                onTabChange = { activeTab = it },
                onAICoachClick = {
                    // Ouvrir AI Coach - la vérification de synchronisation Strava se fera dans le ViewModel
                    // Si Strava n'est pas synchronisé, l'écran de synchronisation s'affichera automatiquement
                    overlay = OverlayScreen.AICoach
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

private sealed interface OverlayScreen {
    data object QuickMatch : OverlayScreen
    data object AIMatchmaker : OverlayScreen
    data object AICoach : OverlayScreen
    data object Achievements : OverlayScreen
    data class CoachProfile(val coachId: String) : OverlayScreen
    data class EventDetails(val eventId: String) : OverlayScreen
    data object Discover : OverlayScreen
    data object CreateActivity : OverlayScreen
    data object Settings : OverlayScreen
    data object ApplyVerification : OverlayScreen
    data object EditProfile : OverlayScreen
    data object ChangePassword : OverlayScreen
    data class ActivityRoom(val activity: HomeActivity) : OverlayScreen
    data class Chat(
        val chatId: String,
        val chatName: String,
        val chatAvatar: String?,
        val isGroup: Boolean = false
    ) : OverlayScreen
    data class ChatParticipants(
        val chatId: String,
        val chatName: String
    ) : OverlayScreen
    data object UserSearch : OverlayScreen
    data class UserProfileDetails(val userId: String) : OverlayScreen
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DamAndroidTheme {
        Greeting("Android")
    }
}

