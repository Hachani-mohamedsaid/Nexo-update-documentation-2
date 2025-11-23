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
     * Vérifie les permissions Health Connect quand l'utilisateur revient à l'écran
     * Appelé automatiquement lors du refresh ou manuellement après avoir accordé les permissions
     */
    fun checkPermissionsAndRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            if (syncManager != null && context != null) {
                val fitnessDataSourceManager = com.example.damandroid.data.datasource.FitnessDataSourceManager(context!!)
                val isConnected = fitnessDataSourceManager.isAnySourceConnected()
                
                if (isConnected) {
                    // Les permissions sont accordées, marquer comme synchronisé
                    syncManager.markAsSynced()
                    Log.d("AICoachViewModel", "Health Connect permissions granted! Marking as synced and refreshing...")
                    refresh()
                } else {
                    // Les permissions ne sont pas encore accordées
                    val errorMessage = syncManager.getAccessErrorMessage()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Les permissions n'ont pas encore été accordées.\n\n" +
                                    "Instructions:\n" +
                                    "1. Ouvrez Health Connect\n" +
                                    "2. Allez dans 'Applications et services'\n" +
                                    "3. Trouvez 'DamAndroid' et activez les permissions\n" +
                                    "4. Revenez ici et cliquez sur 'Vérifier'",
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
            
            // Vérifier la synchronisation Health Connect - BLOQUER L'ACCÈS si pas synchronisé
            if (syncManager != null) {
                val canAccess = syncManager.canAccessAICoach()
                if (!canAccess) {
                    val errorMessage = syncManager.getAccessErrorMessage()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage,
                            needsGoogleFitSync = true  // Affiche l'écran de synchronisation
                        )
                    }
                    // NE PAS charger les données - l'utilisateur doit d'abord synchroniser
                    return@launch
                }
            }
            
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
     * Demande les permissions Health Connect
     * 
     * Quand l'utilisateur clique sur "Connecter l'application Fitness" :
     * 1. Vérifie si Health Connect est disponible
     * 2. Si Health Connect n'est pas installé, affiche un message pour l'installer
     * 3. Si Health Connect est installé, ouvre l'application Health Connect
     * 4. L'utilisateur doit accorder les permissions dans Health Connect
     * 5. Quand l'utilisateur revient à l'app, on vérifie si les permissions sont accordées
     * 6. Si oui, on marque comme synchronisé et on rafraîchit les données
     */
    fun requestFitnessSync(activity: android.app.Activity) {
        viewModelScope.launch {
            if (syncManager != null && context != null) {
                val fitnessDataSourceManager = com.example.damandroid.data.datasource.FitnessDataSourceManager(context!!)
                
                // Vérifier si Health Connect est disponible
                val isAvailable = fitnessDataSourceManager.isAnySourceAvailable()
                if (!isAvailable) {
                    _uiState.update {
                        it.copy(
                            error = "Health Connect n'est pas disponible sur votre appareil. " +
                                    "Veuillez installer Health Connect depuis le Play Store (nécessite Android 8.0 ou supérieur).",
                            needsGoogleFitSync = true
                        )
                    }
                    return@launch
                }
                
                // Pour Health Connect, on doit ouvrir l'application Health Connect
                val sourceName = fitnessDataSourceManager.getCurrentAppName()
                if (sourceName == "Health Connect") {
                    // Ouvrir Health Connect pour que l'utilisateur accorde les permissions
                    try {
                        val packageName = "com.google.android.apps.healthdata"
                        
                        // Vérifier si Health Connect est installé
                        val packageManager = activity.packageManager
                        val isInstalled = try {
                            packageManager.getPackageInfo(packageName, 0)
                            true
                        } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                            false
                        }
                        
                        if (!isInstalled) {
                            _uiState.update {
                                it.copy(
                                    error = "Health Connect n'est pas installé sur votre appareil.\n\n" +
                                            "Veuillez installer Health Connect depuis le Play Store:\n" +
                                            "https://play.google.com/store/apps/details?id=$packageName",
                                    needsGoogleFitSync = true
                                )
                            }
                            return@launch
                        }
                        
                        // Essayer d'abord avec getLaunchIntentForPackage
                        var intent = packageManager.getLaunchIntentForPackage(packageName)
                        
                        // Si ça ne fonctionne pas, essayer avec un intent explicite
                        if (intent == null) {
                            try {
                                intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    setPackage(packageName)
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            } catch (e: Exception) {
                                Log.e("AICoachViewModel", "Error creating explicit intent for Health Connect", e)
                            }
                        }
                        
                        if (intent != null) {
                            // Ajouter des flags pour s'assurer que l'intent fonctionne
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            
                            Log.d("AICoachViewModel", "Opening Health Connect with intent: $intent")
                            activity.startActivity(intent)
                            
                            // Afficher un message informatif avec instructions
                            _uiState.update {
                                it.copy(
                                    error = "Instructions:\n\n" +
                                            "1. Health Connect devrait s'ouvrir maintenant\n" +
                                            "2. Dans Health Connect, allez dans 'Applications et services'\n" +
                                            "3. Trouvez 'DamAndroid' (ou 'com.example.damandroid')\n" +
                                            "4. Activez les permissions pour:\n" +
                                            "   - Sessions d'exercice\n" +
                                            "   - Calories brûlées\n" +
                                            "5. Revenez à cette application\n" +
                                            "6. Cliquez sur 'J'ai accordé les permissions - Vérifier'",
                                    needsGoogleFitSync = true
                                )
                            }
                            
                            // Note: On ne marque pas comme synchronisé immédiatement
                            // L'utilisateur doit revenir à l'app et cliquer sur "Vérifier" ou on vérifiera au prochain refresh
                        } else {
                            // Health Connect est installé mais on ne peut pas l'ouvrir
                            _uiState.update {
                                it.copy(
                                    error = "Health Connect est installé mais ne peut pas être ouvert automatiquement.\n\n" +
                                            "Veuillez ouvrir Health Connect manuellement:\n" +
                                            "1. Ouvrez l'application Health Connect\n" +
                                            "2. Allez dans 'Applications et services'\n" +
                                            "3. Trouvez 'DamAndroid' et activez les permissions\n" +
                                            "4. Revenez ici et cliquez sur 'Vérifier'",
                                    needsGoogleFitSync = true
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AICoachViewModel", "Error opening Health Connect", e)
                        _uiState.update {
                            it.copy(
                                error = "Erreur lors de l'ouverture de Health Connect:\n${e.message}\n\n" +
                                        "Veuillez ouvrir Health Connect manuellement depuis le menu des applications.",
                                needsGoogleFitSync = true
                            )
                        }
                    }
                } else {
                    // Pour d'autres sources, utiliser la méthode normale
                    val success = fitnessDataSourceManager.requestPermissions(activity)
                    if (success) {
                        syncManager.markAsSynced()
                        kotlinx.coroutines.delay(1000)
                        refresh()
                    } else {
                        _uiState.update {
                            it.copy(
                                error = "Impossible d'obtenir les permissions. Veuillez réessayer.",
                                needsGoogleFitSync = true
                            )
                        }
                    }
                }
            }
        }
    }
}

