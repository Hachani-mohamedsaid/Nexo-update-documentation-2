package com.example.damandroid.presentation.ai.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.data.datasource.FitnessSyncManager
import com.example.damandroid.domain.usecase.GetAICoachOverview
import com.example.damandroid.presentation.ai.model.AICoachTab
import com.example.damandroid.presentation.ai.model.AICoachUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AICoachViewModel(
    private val getAICoachOverview: GetAICoachOverview,
    private val context: Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AICoachUiState(isLoading = true))
    val uiState: StateFlow<AICoachUiState> = _uiState
    
    private val syncManager: FitnessSyncManager? = context?.let { FitnessSyncManager(it) }

    init {
        refresh()
    }
    
    /**
     * Vérifie les permissions Strava quand l'utilisateur revient à l'écran
     * Appelé automatiquement lors du refresh ou manuellement après avoir accordé les permissions
     */
    fun checkPermissionsAndRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            if (syncManager != null && context != null) {
                val fitnessDataSourceManager = com.example.damandroid.data.datasource.FitnessDataSourceManager(context!!)
                
                // Vérifier si Strava est disponible et connecté
                val isAvailable = fitnessDataSourceManager.isAnySourceAvailable()
                val isConnected = fitnessDataSourceManager.isAnySourceConnected()
                Log.d("AICoachViewModel", "Strava status check: isAvailable=$isAvailable, isConnected=$isConnected")
                
                if (!isAvailable) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Strava n'est pas installé sur votre appareil.\n\n" +
                                    "Veuillez installer Strava depuis le Play Store:\n" +
                                    "https://play.google.com/store/apps/details?id=com.strava\n\n" +
                                    "Note: Vous pouvez toujours utiliser AI Coach sans Strava.",
                            needsGoogleFitSync = true
                        )
                    }
                    return@launch
                }
                
                if (isConnected) {
                    // Strava est connecté via OAuth, marquer comme synchronisé
                    syncManager.markAsSynced()
                    Log.d("AICoachViewModel", "✅ Strava connecté via OAuth! Marking as synced and refreshing...")
                    refresh()
                } else if (isAvailable) {
                    // Strava est installé mais pas encore connecté via OAuth
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Strava est installé mais n'est pas encore connecté via OAuth.\n\n" +
                                    "Pour récupérer vos données fitness (workouts, calories, minutes), " +
                                    "vous devez connecter votre compte Strava.\n\n" +
                                    "Instructions:\n" +
                                    "1. Cliquez sur 'Connecter Strava' pour lancer le processus d'authentification.\n" +
                                    "2. Autorisez l'accès à votre compte Strava sur la page web qui s'ouvrira.\n" +
                                    "3. Revenez ici et cliquez sur 'J'ai accordé les permissions - Vérifier'.\n\n" +
                                    "Note: Vous pouvez toujours utiliser AI Coach sans Strava.",
                            needsGoogleFitSync = true
                        )
                    }
                } else {
                    // Strava n'est pas installé
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Strava n'est pas installé sur votre appareil.\n\n" +
                                    "Pour utiliser AI Coach avec vos données fitness, " +
                                    "veuillez installer Strava depuis le Play Store:\n" +
                                    "https://play.google.com/store/apps/details?id=com.strava\n\n" +
                                    "Note: Vous pouvez toujours utiliser AI Coach sans Strava.",
                            needsGoogleFitSync = true
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Impossible de vérifier les permissions. Veuillez réessayer.",
                        needsGoogleFitSync = true
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Vérifier l'état de connexion Strava
            if (syncManager != null && context != null) {
                val fitnessDataSourceManager = com.example.damandroid.data.datasource.FitnessDataSourceManager(context!!)
                val isAvailable = fitnessDataSourceManager.isAnySourceAvailable()
                val isConnected = fitnessDataSourceManager.isAnySourceConnected()
                val appName = fitnessDataSourceManager.getCurrentAppName()
                
                Log.d("AICoachViewModel", "=== STRAVA CONNECTION STATUS ===")
                Log.d("AICoachViewModel", "App: $appName")
                Log.d("AICoachViewModel", "Is Available (installed): $isAvailable")
                Log.d("AICoachViewModel", "Is Connected: $isConnected")
                Log.d("AICoachViewModel", "Sync Status (before): ${syncManager.isSynced()}")
                Log.d("AICoachViewModel", "Last Sync Time: ${syncManager.getLastSyncTime()}")
                
                // Si Strava est installé et connecté, marquer automatiquement comme synchronisé
                if (isConnected && !syncManager.isSynced()) {
                    Log.d("AICoachViewModel", "✅ Strava détecté! Marquant comme synchronisé...")
                    syncManager.markAsSynced()
                    Log.d("AICoachViewModel", "Sync Status (after): ${syncManager.isSynced()}")
                }
                
                // Si Strava est disponible mais pas connecté, afficher l'écran de synchronisation
                if (isAvailable && !isConnected) {
                    Log.d("AICoachViewModel", "⚠️ Strava est installé mais pas connecté via OAuth - affichage de l'écran de synchronisation")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Strava est installé mais n'est pas encore connecté via OAuth.\n\n" +
                                    "Pour récupérer vos données fitness (workouts, calories, minutes), " +
                                    "vous devez connecter votre compte Strava.\n\n" +
                                    "Instructions:\n" +
                                    "1. Cliquez sur 'Connecter Strava' pour lancer le processus d'authentification.\n" +
                                    "2. Autorisez l'accès à votre compte Strava sur la page web qui s'ouvrira.\n" +
                                    "3. Revenez ici et cliquez sur 'J'ai accordé les permissions - Vérifier'.\n\n" +
                                    "Note: Vous pouvez toujours utiliser AI Coach sans Strava.",
                            needsGoogleFitSync = true
                        )
                    }
                    return@launch
                }
                
                Log.d("AICoachViewModel", "================================")
            } else {
                Log.d("AICoachViewModel", "⚠️ Cannot check Strava status: syncManager or context is null")
            }
            
            // Permettre l'accès à AI Coach même sans application fitness
            // L'utilisateur peut utiliser AI Coach avec des données par défaut
            Log.d("AICoachViewModel", "Loading AI Coach data (fitness app connection is optional)")
            
            runCatching { getAICoachOverview() }
                .onSuccess { overview ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            overview = overview,
                            error = null,
                            needsGoogleFitSync = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unknown error",
                            needsGoogleFitSync = false
                        )
                    }
                }
        }
    }

    fun onTabSelected(tab: AICoachTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    /**
     * Demande les permissions Strava via OAuth
     * 
     * Quand l'utilisateur clique sur "Connecter Strava" :
     * 1. Vérifie si Strava est disponible
     * 2. Si Strava n'est pas installé, affiche un message pour l'installer
     * 3. Si Strava est installé, lance le flux OAuth qui ouvre le navigateur
     * 4. L'utilisateur autorise l'accès sur la page web Strava
     * 5. Strava redirige vers l'app via le deep link nexofitness://strava/callback
     * 6. MainActivity gère le callback et échange le code contre un token
     * 7. L'app est automatiquement marquée comme synchronisée
     */
    fun requestFitnessSync(activity: android.app.Activity) {
        Log.d("AICoachViewModel", "🔄 requestFitnessSync called")
        viewModelScope.launch {
            Log.d("AICoachViewModel", "🔄 requestFitnessSync: Starting coroutine")
            if (syncManager != null && context != null) {
                Log.d("AICoachViewModel", "🔄 requestFitnessSync: syncManager and context are available")
                val fitnessDataSourceManager = com.example.damandroid.data.datasource.FitnessDataSourceManager(context!!)
                
                // Vérifier si Strava est disponible
                val isAvailable = fitnessDataSourceManager.isAnySourceAvailable()
                Log.d("AICoachViewModel", "🔄 requestFitnessSync: isAvailable=$isAvailable")
                if (!isAvailable) {
                    Log.w("AICoachViewModel", "⚠️ Strava is not available")
                    _uiState.update {
                        it.copy(
                            error = "Strava n'est pas installé sur votre appareil.\n\n" +
                                    "Veuillez installer Strava depuis le Play Store:\n" +
                                    "https://play.google.com/store/apps/details?id=com.strava\n\n" +
                                    "Note: Vous pouvez toujours utiliser AI Coach sans Strava.",
                            needsGoogleFitSync = true
                        )
                    }
                    return@launch
                }
                
                // Lancer le flux OAuth Strava
                // Cela ouvrira le navigateur pour l'authentification
                Log.d("AICoachViewModel", "🔄 requestFitnessSync: Calling requestPermissions...")
                val success = fitnessDataSourceManager.requestPermissions(activity)
                Log.d("AICoachViewModel", "🔄 requestFitnessSync: requestPermissions returned success=$success")
                if (success) {
                    // Ne pas marquer comme synchronisé immédiatement - attendre le callback OAuth
                    // Le callback OAuth dans MainActivity marquera comme synchronisé automatiquement
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Authentification Strava lancée.\n\n" +
                                    "1. Une page web devrait s'ouvrir dans votre navigateur\n" +
                                    "2. Connectez-vous à votre compte Strava si nécessaire\n" +
                                    "3. Autorisez l'accès aux données d'activité\n" +
                                    "4. Vous serez automatiquement redirigé vers l'application\n\n" +
                                    "Si rien ne se passe, vérifiez que les credentials Strava sont configurés.",
                            needsGoogleFitSync = true
                        )
                    }
                    // Vérifier périodiquement si la connexion est établie
                    kotlinx.coroutines.delay(2000)
                    checkPermissionsAndRefresh()
                } else {
                    _uiState.update {
                        it.copy(
                            error = "Impossible de lancer l'authentification Strava.\n\n" +
                                    "Assurez-vous que:\n" +
                                    "1. Strava est installé sur votre appareil\n" +
                                    "2. Les credentials Strava sont configurés dans StravaOAuthHelper.kt\n" +
                                    "3. L'URL de callback est: nexofitness://strava/callback\n" +
                                    "4. Le domaine 'nexofitness' est configuré dans les paramètres Strava\n\n" +
                                    "Voir CORRECTION_FORMULAIRE_STRAVA.md pour plus de détails.",
                            needsGoogleFitSync = true
                        )
                    }
                }
            }
        }
    }
}

