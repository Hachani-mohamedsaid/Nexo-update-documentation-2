package com.example.damandroid.presentation.achievements.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.model.AchievementBadge
import com.example.damandroid.domain.model.AchievementChallenge
import com.example.damandroid.domain.model.AchievementsOverview
import com.example.damandroid.domain.model.LeaderboardResponse
import com.example.damandroid.domain.model.AchievementNotificationsResponse
import com.example.damandroid.domain.repository.AchievementsRepository
import com.example.damandroid.domain.usecase.GetAchievementsOverview
import com.example.damandroid.presentation.achievements.model.AchievementsTab
import com.example.damandroid.presentation.achievements.model.AchievementsUiState
import com.example.damandroid.presentation.achievements.model.UiState
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Événement de montée de niveau
 */
data class LevelUpEvent(
    val oldLevel: Int,
    val newLevel: Int,
    val totalXp: Int
)

data class ChallengeCompletedEvent(
    val challengeId: String,
    val challengeName: String,
    val xpReward: Int
)

class AchievementsViewModel(
    private val getAchievementsOverview: GetAchievementsOverview,
    private val repository: AchievementsRepository
) : ViewModel() {

    // État pour la rétrocompatibilité avec l'ancien système
    private val _uiState = MutableStateFlow(AchievementsUiState(isLoading = true))
    val uiState: StateFlow<AchievementsUiState> = _uiState

    // États séparés avec StateFlow pour chaque endpoint (nouveau système)
    private val _summaryState = MutableStateFlow<UiState<AchievementsOverview>>(UiState.Idle)
    val summaryState: StateFlow<UiState<AchievementsOverview>> = _summaryState.asStateFlow()

    private val _badgesState = MutableStateFlow<UiState<List<AchievementBadge>>>(UiState.Idle)
    val badgesState: StateFlow<UiState<List<AchievementBadge>>> = _badgesState.asStateFlow()

    private val _challengesState = MutableStateFlow<UiState<List<AchievementChallenge>>>(UiState.Idle)
    val challengesState: StateFlow<UiState<List<AchievementChallenge>>> = _challengesState.asStateFlow()

    private val _leaderboardState = MutableStateFlow<UiState<LeaderboardResponse>>(UiState.Idle)
    val leaderboardState: StateFlow<UiState<LeaderboardResponse>> = _leaderboardState.asStateFlow()

    // État pour les notifications
    private val _notificationsState = MutableStateFlow<UiState<AchievementNotificationsResponse>>(UiState.Idle)
    val notificationsState: StateFlow<UiState<AchievementNotificationsResponse>> = _notificationsState.asStateFlow()

    // Événements pour les nouveaux badges débloqués et montée de niveau
    private val _newBadgesUnlocked = MutableStateFlow<List<AchievementBadge>>(emptyList())
    val newBadgesUnlocked: StateFlow<List<AchievementBadge>> = _newBadgesUnlocked.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<LevelUpEvent?>(null)
    val levelUpEvent: StateFlow<LevelUpEvent?> = _levelUpEvent.asStateFlow()

    // Événement pour les challenges complétés
    private val _challengeCompletedEvent = MutableSharedFlow<ChallengeCompletedEvent>(replay = 0)
    val challengeCompletedEvent: SharedFlow<ChallengeCompletedEvent> = _challengeCompletedEvent.asSharedFlow()

    // Stocker les états précédents pour détecter les changements
    private var previousBadgeIds = setOf<String>()
    private var previousLevel = 1
    private var previousChallengeProgress = mapOf<String, Int>() // Map<challengeId, currentProgress>

    init {
        loadAllData()
    }

    /**
     * Charge toutes les données (ancien système pour rétrocompatibilité)
     */
    fun refresh() {
        loadAllData()
    }

    /**
     * Charge toutes les données séparément (nouveau système)
     * et synchronise avec l'ancien uiState pour la rétrocompatibilité
     */
    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Charger toutes les données en parallèle
            val summaryResult = runCatching { repository.getSummary() }
            val badgesResult = runCatching { repository.getBadges() }
            val challengesResult = runCatching { repository.getChallenges() }
            val leaderboardResult = runCatching { repository.getLeaderboard() }
            
            // Combiner les résultats
            when {
                summaryResult.isSuccess -> {
                    val summary = summaryResult.getOrNull()!!
                    val badges = badgesResult.getOrNull() ?: emptyList()
                    val challenges = challengesResult.getOrNull() ?: emptyList()
                    val leaderboardResponse = leaderboardResult.getOrNull()
                    
                    // Extraire la liste des entrées du leaderboard pour la compatibilité
                    val leaderboardEntries = mutableListOf<com.example.damandroid.domain.model.LeaderboardEntry>()
                    leaderboardResponse?.currentUser?.let { user ->
                        leaderboardEntries.add(
                            com.example.damandroid.domain.model.LeaderboardEntry(
                                rank = user.rank,
                                name = "You",
                                points = user.totalXp,
                                badge = when (user.rank) {
                                    1 -> "🥇"
                                    2 -> "🥈"
                                    3 -> "🥉"
                                    else -> ""
                                }
                            )
                        )
                    }
                    leaderboardResponse?.leaderboard?.let {
                        leaderboardEntries.addAll(it)
                    }
                    
                    // Créer un overview combiné
                    val overview = AchievementsOverview(
                        stats = summary.stats,
                        badges = badges,
                        challenges = challenges,
                        leaderboard = leaderboardEntries.sortedBy { it.rank }
                    )
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            overview = overview,
                            error = null
                        )
                    }
                }
                else -> {
                    val error = summaryResult.exceptionOrNull()?.message 
                        ?: badgesResult.exceptionOrNull()?.message
                        ?: challengesResult.exceptionOrNull()?.message
                        ?: leaderboardResult.exceptionOrNull()?.message
                        ?: "Unable to load achievements"
                    
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                }
            }
            
            // Mettre à jour les StateFlow séparés avec les résultats déjà obtenus
            updateSeparateStates(summaryResult, badgesResult, challengesResult, leaderboardResult)
            
            // Initialiser les états précédents après le chargement
            initializePreviousStates()
        }
    }

    /**
     * Met à jour les StateFlow séparés avec les résultats
     */
    private fun updateSeparateStates(
        summaryResult: Result<AchievementsOverview>,
        badgesResult: Result<List<AchievementBadge>>,
        challengesResult: Result<List<AchievementChallenge>>,
        leaderboardResult: Result<LeaderboardResponse>
    ) {
        _summaryState.value = when {
            summaryResult.isSuccess -> UiState.Success(summaryResult.getOrNull()!!)
            else -> UiState.Error(summaryResult.exceptionOrNull()?.message ?: "Erreur inconnue")
        }
        
        _badgesState.value = when {
            badgesResult.isSuccess -> UiState.Success(badgesResult.getOrNull() ?: emptyList())
            else -> UiState.Error(badgesResult.exceptionOrNull()?.message ?: "Erreur inconnue")
        }
        
        _challengesState.value = when {
            challengesResult.isSuccess -> UiState.Success(challengesResult.getOrNull() ?: emptyList())
            else -> UiState.Error(challengesResult.exceptionOrNull()?.message ?: "Erreur inconnue")
        }
        
        _leaderboardState.value = when {
            leaderboardResult.isSuccess -> {
                val leaderboard = leaderboardResult.getOrNull()
                if (leaderboard != null) {
                    UiState.Success(leaderboard)
                } else {
                    UiState.Error("Leaderboard data is null")
                }
            }
            else -> UiState.Error(leaderboardResult.exceptionOrNull()?.message ?: "Erreur inconnue")
        }
    }

    /**
     * Charge le résumé des achievements
     */
    fun fetchSummary() {
        viewModelScope.launch {
            _summaryState.value = UiState.Loading
            try {
                val overview = repository.getSummary()
                _summaryState.value = UiState.Success(overview)
            } catch (e: Exception) {
                _summaryState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    /**
     * Rafraîchit le résumé des achievements
     */
    fun refreshSummary() {
        fetchSummary()
    }

    /**
     * Charge les badges
     */
    fun fetchBadges() {
        viewModelScope.launch {
            _badgesState.value = UiState.Loading
            try {
                val badges = repository.getBadges()
                _badgesState.value = UiState.Success(badges)
            } catch (e: Exception) {
                _badgesState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    /**
     * Rafraîchit les badges
     */
    fun refreshBadges() {
        fetchBadges()
    }

    /**
     * Charge les challenges
     */
    fun fetchChallenges() {
        viewModelScope.launch {
            _challengesState.value = UiState.Loading
            try {
                val challenges = repository.getChallenges()
                _challengesState.value = UiState.Success(challenges)
            } catch (e: Exception) {
                _challengesState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    /**
     * Rafraîchit les challenges
     */
    fun refreshChallenges() {
        fetchChallenges()
    }

    /**
     * Charge le leaderboard avec pagination
     */
    fun fetchLeaderboard(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _leaderboardState.value = UiState.Loading
            try {
                val leaderboard = repository.getLeaderboard(page, limit)
                _leaderboardState.value = UiState.Success(leaderboard)
            } catch (e: Exception) {
                _leaderboardState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    /**
     * Rafraîchit le leaderboard
     */
    fun refreshLeaderboard(page: Int = 1, limit: Int = 20) {
        fetchLeaderboard(page, limit)
    }

    fun onTabSelected(tab: AchievementsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * Initialise les états précédents pour détecter les changements
     */
    private fun initializePreviousStates() {
        _summaryState.value.let { state ->
            if (state is UiState.Success) {
                previousLevel = state.data.stats.level
            }
        }
        _badgesState.value.let { state ->
            if (state is UiState.Success) {
                previousBadgeIds = state.data.filter { it.unlocked }.map { it.id }.toSet()
            }
        }
        _challengesState.value.let { state ->
            if (state is UiState.Success) {
                previousChallengeProgress = state.data.associate { 
                    it.id to it.progress 
                }
            }
        }
    }

    /**
     * Rafraîchit toutes les données après complétion d'activité
     * et vérifie les nouveaux badges et montées de niveau
     */
    fun refreshAllAfterActivityCompletion() {
        viewModelScope.launch {
            // Attendre un peu pour que le backend traite les achievements
            delay(1500)
            
            // Rafraîchir toutes les données en parallèle
            val summaryDeferred = async { 
                try { repository.getSummary() } catch (e: Exception) { null }
            }
            val badgesDeferred = async { 
                try { repository.getBadges() } catch (e: Exception) { null }
            }
            val challengesDeferred = async { 
                try { repository.getChallenges() } catch (e: Exception) { null }
            }
            val leaderboardDeferred = async {
                try { repository.getLeaderboard() } catch (e: Exception) { null }
            }
            // Rafraîchir aussi les notifications pour voir les nouvelles notifications d'XP et badges
            val notificationsDeferred = async {
                try { repository.getNotifications() } catch (e: Exception) { null }
            }
            
            // Attendre les résultats
            val summary = summaryDeferred.await()
            val badges = badgesDeferred.await()
            val challenges = challengesDeferred.await()
            val leaderboardResponse = leaderboardDeferred.await()
            val notifications = notificationsDeferred.await()
            
            // Mettre à jour les états
            summary?.let { 
                _summaryState.value = UiState.Success(it)
                // Vérifier la montée de niveau avant de mettre à jour previousLevel
                val currentLevel = it.stats.level
                if (currentLevel > previousLevel) {
                    _levelUpEvent.value = LevelUpEvent(
                        oldLevel = previousLevel,
                        newLevel = currentLevel,
                        totalXp = it.stats.xp
                    )
                    previousLevel = currentLevel
                }
            }
            
            badges?.let {
                _badgesState.value = UiState.Success(it)
                // Détecter les nouveaux badges débloqués en comparant les IDs
                // Selon le guide : Comparer les IDs avant/après pour détecter les nouveaux
                val currentBadgeIds = it.filter { badge -> badge.unlocked }.map { badge -> badge.id }.toSet()
                val newBadges = it.filter { badge -> badge.unlocked && badge.id !in previousBadgeIds }
                if (newBadges.isNotEmpty()) {
                    // Émettre les nouveaux badges pour affichage dans le dialog
                    _newBadgesUnlocked.value = newBadges
                    // Mettre à jour les IDs précédents pour éviter les doublons
                    previousBadgeIds = currentBadgeIds
                }
            }
            
            challenges?.let {
                _challengesState.value = UiState.Success(it)
                // Vérifier les challenges complétés
                checkForCompletedChallenges(it)
            }
            
            leaderboardResponse?.let {
                _leaderboardState.value = UiState.Success(it)
            }
            
            // Mettre à jour les notifications
            notifications?.let {
                _notificationsState.value = UiState.Success(it)
            }
            
            // Mettre à jour l'état combiné (overview) pour le design legacy
            if (summary != null) {
                val badgesList = badges ?: emptyList()
                val challengesList = challenges ?: emptyList()
                
                // Extraire la liste des entrées du leaderboard pour la compatibilité
                val leaderboardEntries = mutableListOf<com.example.damandroid.domain.model.LeaderboardEntry>()
                leaderboardResponse?.currentUser?.let { currentUser ->
                    leaderboardEntries.add(
                        com.example.damandroid.domain.model.LeaderboardEntry(
                            rank = currentUser.rank,
                            name = "You",
                            points = currentUser.totalXp,
                            badge = when (currentUser.rank) {
                                1 -> "🥇"
                                2 -> "🥈"
                                3 -> "🥉"
                                else -> ""
                            }
                        )
                    )
                }
                leaderboardResponse?.leaderboard?.let {
                    leaderboardEntries.addAll(it)
                }
                
                // Créer un overview combiné
                val overview = AchievementsOverview(
                    stats = summary.stats,
                    badges = badgesList,
                    challenges = challengesList,
                    leaderboard = leaderboardEntries.sortedBy { it.rank }
                )
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        overview = overview,
                        error = null
                    )
                }
            }
            
            // Rafraîchir les challenges une deuxième fois après un délai supplémentaire
            // pour s'assurer que le backend a bien mis à jour la progression
            // (le backend peut mettre du temps à mettre à jour la progression des challenges)
            launch {
                delay(2000) // Délai supplémentaire pour laisser le backend traiter
                try {
                    val refreshedChallenges = repository.getChallenges()
                    _challengesState.value = UiState.Success(refreshedChallenges)
                    
                    // Mettre à jour aussi l'overview si nécessaire
                    val currentSummary = _summaryState.value
                    if (currentSummary is UiState.Success && summary != null) {
                        val currentBadges = (_badgesState.value as? UiState.Success)?.data ?: emptyList()
                        val leaderboardEntries = mutableListOf<com.example.damandroid.domain.model.LeaderboardEntry>()
                        leaderboardResponse?.currentUser?.let { currentUser ->
                            leaderboardEntries.add(
                                com.example.damandroid.domain.model.LeaderboardEntry(
                                    rank = currentUser.rank,
                                    name = "You",
                                    points = currentUser.totalXp,
                                    badge = when (currentUser.rank) {
                                        1 -> "🥇"
                                        2 -> "🥈"
                                        3 -> "🥉"
                                        else -> ""
                                    }
                                )
                            )
                        }
                        leaderboardResponse?.leaderboard?.let {
                            leaderboardEntries.addAll(it)
                        }
                        
                        val updatedOverview = AchievementsOverview(
                            stats = currentSummary.data.stats,
                            badges = currentBadges,
                            challenges = refreshedChallenges,
                            leaderboard = leaderboardEntries.sortedBy { it.rank }
                        )
                        _uiState.update { it.copy(overview = updatedOverview) }
                    }
                } catch (e: Exception) {
                    // Ignorer les erreurs pour le rafraîchissement secondaire
                }
            }
        }
    }

    /**
     * Vérifie les nouveaux badges débloqués
     */
    fun checkForNewBadges() {
        viewModelScope.launch {
            try {
                val currentBadges = repository.getBadges()
                val currentBadgeIds = currentBadges.filter { it.unlocked }.map { it.id }.toSet()
                
                // Trouver les nouveaux badges
                val newBadges = currentBadges.filter { 
                    it.unlocked && it.id !in previousBadgeIds 
                }
                
                if (newBadges.isNotEmpty()) {
                    _newBadgesUnlocked.value = newBadges
                    previousBadgeIds = currentBadgeIds
                }
            } catch (e: Exception) {
                // Erreur silencieuse - on ne veut pas interrompre le flux
            }
        }
    }

    /**
     * Vérifie si le niveau a augmenté
     */
    fun checkForLevelUp() {
        viewModelScope.launch {
            try {
                val summary = repository.getSummary()
                val currentLevel = summary.stats.level
                
                if (currentLevel > previousLevel) {
                    _levelUpEvent.value = LevelUpEvent(
                        oldLevel = previousLevel,
                        newLevel = currentLevel,
                        totalXp = summary.stats.xp
                    )
                    previousLevel = currentLevel
                }
            } catch (e: Exception) {
                // Erreur silencieuse - on ne veut pas interrompre le flux
            }
        }
    }

    /**
     * Retire un badge de la liste des nouveaux badges affichés
     */
    fun clearNewBadge(badgeId: String) {
        _newBadgesUnlocked.value = _newBadgesUnlocked.value.filter { it.id != badgeId }
    }

    /**
     * Efface l'événement de montée de niveau
     */
    fun clearLevelUpEvent() {
        _levelUpEvent.value = null
    }

    /**
     * Vérifie les challenges complétés en comparant la progression actuelle avec la précédente
     */
    private fun checkForCompletedChallenges(currentChallenges: List<AchievementChallenge>) {
        viewModelScope.launch {
            try {
                // Comparer avec la progression précédente
                currentChallenges.forEach { challenge ->
                    val previousProgress = previousChallengeProgress[challenge.id] ?: 0
                    val currentProgress = challenge.progress
                    
                    // Si la progression a atteint le total (challenge complété)
                    if (currentProgress >= challenge.total && previousProgress < challenge.total) {
                        // Extraire la récompense XP depuis le reward (format: "200 XP" ou "+200 XP")
                        val xpReward = challenge.reward.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                        
                        // Émettre l'événement de challenge complété
                        _challengeCompletedEvent.emit(
                            ChallengeCompletedEvent(
                                challengeId = challenge.id,
                                challengeName = challenge.title,
                                xpReward = xpReward
                            )
                        )
                    }
                }
                
                // Mettre à jour la progression précédente
                previousChallengeProgress = currentChallenges.associate { 
                    it.id to it.progress 
                }
            } catch (e: Exception) {
                // Erreur silencieuse
            }
        }
    }

    // Méthodes pour les notifications
    fun fetchNotifications(page: Int = 1, limit: Int = 20, unreadOnly: Boolean = false) {
        viewModelScope.launch {
            _notificationsState.value = UiState.Loading
            runCatching {
                repository.getNotifications(page, limit, unreadOnly)
            }.onSuccess { response ->
                _notificationsState.value = UiState.Success(response)
            }.onFailure { error ->
                _notificationsState.value = UiState.Error(error.message ?: "Erreur inconnue")
            }
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            runCatching {
                repository.markNotificationAsRead(notificationId)
            }.onSuccess {
                // Rafraîchir les notifications pour mettre à jour l'état
                val currentState = _notificationsState.value
                if (currentState is UiState.Success) {
                    val updatedNotifications = currentState.data.notifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true, readAt = System.currentTimeMillis().toString())
                        } else {
                            notification
                        }
                    }
                    val updatedResponse = currentState.data.copy(
                        notifications = updatedNotifications,
                        unreadCount = (currentState.data.unreadCount - 1).coerceAtLeast(0)
                    )
                    _notificationsState.value = UiState.Success(updatedResponse)
                }
            }
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            runCatching {
                repository.markAllNotificationsAsRead()
            }.onSuccess {
                // Rafraîchir les notifications
                val currentState = _notificationsState.value
                if (currentState is UiState.Success) {
                    val page = currentState.data.page
                    val limit = currentState.data.notifications.size
                    fetchNotifications(page, limit, false)
                } else {
                    fetchNotifications()
                }
            }
        }
    }

    fun refreshNotifications() {
        val currentState = _notificationsState.value
        if (currentState is UiState.Success) {
            fetchNotifications(currentState.data.page, currentState.data.notifications.size)
        } else {
            fetchNotifications()
        }
    }
}

