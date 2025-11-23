package com.example.damandroid.presentation.quickmatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.model.MatchUserProfile
import com.example.damandroid.domain.usecase.GetQuickMatchProfiles
import com.example.damandroid.domain.usecase.LikeProfile
import com.example.damandroid.domain.usecase.PassProfile
import com.example.damandroid.presentation.quickmatch.data.ExcludedProfilesStore
import com.example.damandroid.presentation.quickmatch.model.QuickMatchUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuickMatchViewModel(
    private val getQuickMatchProfiles: GetQuickMatchProfiles,
    private val likeProfileUseCase: LikeProfile,
    private val passProfileUseCase: PassProfile,
    private val excludedProfilesStore: ExcludedProfilesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickMatchUiState(isLoading = true))
    val uiState: StateFlow<QuickMatchUiState> = _uiState
    
    // Liste des IDs de profils qui ont été likés ou passés pour éviter de les réafficher
    // Chargée depuis le store persistant au démarrage
    private val excludedProfileIds = excludedProfilesStore.getExcludedProfiles().toMutableSet()
    
    // Flag pour savoir si c'est le premier chargement
    private var isFirstLoad = true

    init {
        android.util.Log.d("QuickMatchViewModel", "=== INIT: Loaded ${excludedProfileIds.size} excluded profiles from store ===")
        excludedProfileIds.forEach { profileId ->
            android.util.Log.d("QuickMatchViewModel", "Excluded profile ID: $profileId")
        }
        loadProfiles()
    }

    /**
     * Charge les profils depuis l'API
     * Si append est true, ajoute les nouveaux profils à la liste existante au lieu de les remplacer
     */
    fun loadProfiles(append: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !append, error = null) }
            runCatching { getQuickMatchProfiles() }
                .onSuccess { newProfiles ->
                    android.util.Log.d("QuickMatchViewModel", "=== loadProfiles START ===")
                    android.util.Log.d("QuickMatchViewModel", "append=$append, newProfiles=${newProfiles.size}, excluded=${excludedProfileIds.size}")
                    android.util.Log.d("QuickMatchViewModel", "Excluded IDs: $excludedProfileIds")
                    
                    // Log tous les nouveaux profils
                    newProfiles.forEachIndexed { index, profile ->
                        android.util.Log.d("QuickMatchViewModel", "New profile[$index]: id=${profile.id}, name=${profile.name}")
                    }
                    
                    _uiState.update { currentState ->
                        val existingProfileIds = currentState.profiles.map { it.id }.toSet()
                        android.util.Log.d("QuickMatchViewModel", "Existing profile IDs: $existingProfileIds")
                        
                        // IMPORTANT : Ne JAMAIS réafficher les profils likés/passés
                        // Même si le backend retourne le même profil, on doit le filtrer
                        // Le backend devrait ne pas retourner les profils likés/passés
                        if (newProfiles.isNotEmpty()) {
                            val likedProfilesInResponse = newProfiles.filter { it.id in excludedProfileIds }
                            if (likedProfilesInResponse.isNotEmpty()) {
                                android.util.Log.w("QuickMatchViewModel", "⚠️ PROBLÈME BACKEND: Backend returned ${likedProfilesInResponse.size} profile(s) that were already liked/passed:")
                                likedProfilesInResponse.forEach { profile ->
                                    android.util.Log.w("QuickMatchViewModel", "   - ${profile.name} (${profile.id})")
                                }
                                android.util.Log.w("QuickMatchViewModel", "⚠️ These profiles will be filtered out. Backend should not return them.")
                            }
                        }
                        
                        // Filtrer les profils déjà présents ET les profils exclus (likés/passés) pour éviter les doublons et les réafficher
                        val uniqueNewProfiles = newProfiles.filter { 
                            val isUnique = it.id !in existingProfileIds && it.id !in excludedProfileIds
                            if (!isUnique) {
                                android.util.Log.d("QuickMatchViewModel", "Profile ${it.id} filtered out: existing=${it.id in existingProfileIds}, excluded=${it.id in excludedProfileIds}")
                            }
                            isUnique
                        }
                        
                        android.util.Log.d("QuickMatchViewModel", "Unique new profiles: ${uniqueNewProfiles.size}")
                        
                        val finalProfiles = if (append && uniqueNewProfiles.isNotEmpty()) {
                            // Ajouter les nouveaux profils à la liste existante (en excluant déjà les profils exclus)
                            val appended = currentState.profiles + uniqueNewProfiles
                            android.util.Log.d("QuickMatchViewModel", "Appending: ${currentState.profiles.size} + ${uniqueNewProfiles.size} = ${appended.size}")
                            appended
                        } else {
                            // Remplacer la liste (comportement par défaut) mais TOUJOURS exclure les profils exclus
                            // Même au premier chargement, on filtre les profils likés/passés
                            val filtered = newProfiles.filter { it.id !in excludedProfileIds }
                            android.util.Log.d("QuickMatchViewModel", "Replacing: ${newProfiles.size} -> ${filtered.size} (excluded: ${excludedProfileIds.size}, isFirstLoad: $isFirstLoad)")
                            if (filtered.size < newProfiles.size) {
                                android.util.Log.w("QuickMatchViewModel", "⚠️ Filtered out ${newProfiles.size - filtered.size} profile(s) that were already liked/passed")
                            }
                            filtered
                        }
                        
                        // Marquer que le premier chargement est terminé
                        if (isFirstLoad) {
                            isFirstLoad = false
                        }
                        
                        android.util.Log.d("QuickMatchViewModel", "=== Final profiles count: ${finalProfiles.size} ===")
                        finalProfiles.forEachIndexed { index, profile ->
                            android.util.Log.d("QuickMatchViewModel", "Final[$index]: id=${profile.id}, name=${profile.name}")
                        }
                        
                        if (finalProfiles.size == 1) {
                            android.util.Log.w("QuickMatchViewModel", "⚠️ WARNING: Only ONE profile in final list!")
                            android.util.Log.w("QuickMatchViewModel", "⚠️ CAUSE POSSIBLE: Backend ne retourne qu'un seul profil compatible")
                            android.util.Log.w("QuickMatchViewModel", "⚠️ SOLUTION: Vérifier le backend - peut-être trop de filtres (sports communs, profils likés/passés)")
                        } else if (finalProfiles.isEmpty() && newProfiles.isNotEmpty()) {
                            android.util.Log.e("QuickMatchViewModel", "❌ ERREUR: All profiles filtered out! Backend returned ${newProfiles.size} but all are excluded.")
                            android.util.Log.e("QuickMatchViewModel", "❌ Profils retournés: ${newProfiles.map { "${it.name} (${it.id})" }}")
                            android.util.Log.e("QuickMatchViewModel", "❌ Profils exclus: $excludedProfileIds")
                        } else if (finalProfiles.isEmpty() && newProfiles.isEmpty()) {
                            android.util.Log.e("QuickMatchViewModel", "❌ ERREUR: Backend returned NO profiles!")
                            android.util.Log.e("QuickMatchViewModel", "❌ CAUSE: Aucun utilisateur compatible trouvé (sports communs, filtres backend)")
                        }
                        
                        currentState.copy(
                            isLoading = false,
                            profiles = finalProfiles,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unknown error"
                        )
                    }
                }
        }
    }

    /**
     * Enregistre un like pour un profil
     * Retire immédiatement le profil de la liste pour éviter les likes multiples
     * Vérifie si c'est un match et traite l'erreur 409 (profil déjà liké) comme un succès silencieux
     */
    fun likeProfile(profileId: String, onMatch: (MatchUserProfile) -> Unit) {
        viewModelScope.launch {
            // Vérifier si le profil existe encore dans la liste
            val profileExists = _uiState.value.profiles.any { it.id == profileId }
            if (!profileExists) {
                // Profil déjà retiré (déjà traité), ignorer
                return@launch
            }
            
            // Ajouter le profil à la liste des exclus pour éviter de le réafficher après un rechargement
            excludedProfileIds.add(profileId)
            // Persister dans le store pour que le profil ne réapparaisse pas après un refresh
            excludedProfilesStore.addExcludedProfile(profileId)
            android.util.Log.d("QuickMatchViewModel", "✅ Profile ${profileId} added to excludedProfileIds and persisted. Total excluded: ${excludedProfileIds.size}")
            
            // Retirer immédiatement le profil de la liste pour éviter les likes multiples
            _uiState.update { state ->
                state.copy(
                    profiles = state.profiles.filter { it.id != profileId },
                    error = null
                )
            }
            
            runCatching { likeProfileUseCase(profileId) }
                .onSuccess { result ->
                    // Si c'est un match, appeler le callback
                    result.matchedProfile?.let { matchedProfile ->
                        onMatch(matchedProfile)
                    }
                }
                .onFailure { throwable ->
                    val errorMessage = throwable.message ?: "Failed to like profile"
                    
                    // Si c'est une erreur 409 (profil déjà liké), c'est OK - on a déjà retiré le profil
                    // Pour les autres erreurs (réseau, serveur), afficher le message mais ne pas remettre le profil
                    if (!errorMessage.contains("already liked", ignoreCase = true) && 
                        !errorMessage.contains("409", ignoreCase = true) &&
                        !errorMessage.contains("Conflict", ignoreCase = true)) {
                        // Pour les autres erreurs (réseau, serveur, etc.), afficher le message
                        _uiState.update {
                            it.copy(error = errorMessage)
                        }
                    }
                }
        }
    }

    /**
     * Enregistre un pass pour un profil
     * Retire immédiatement le profil de la liste pour éviter les passes multiples
     */
    fun passProfile(profileId: String) {
        viewModelScope.launch {
            // Vérifier si le profil existe encore dans la liste
            val profileExists = _uiState.value.profiles.any { it.id == profileId }
            if (!profileExists) {
                // Profil déjà retiré (déjà traité), ignorer
                return@launch
            }
            
            // Ajouter le profil à la liste des exclus pour éviter de le réafficher après un rechargement
            excludedProfileIds.add(profileId)
            // Persister dans le store pour que le profil ne réapparaisse pas après un refresh
            excludedProfilesStore.addExcludedProfile(profileId)
            android.util.Log.d("QuickMatchViewModel", "✅ Profile ${profileId} added to excludedProfileIds (pass) and persisted. Total excluded: ${excludedProfileIds.size}")
            
            // Retirer immédiatement le profil de la liste pour éviter les passes multiples
            _uiState.update { state ->
                state.copy(
                    profiles = state.profiles.filter { it.id != profileId }
                )
            }
            
            runCatching { passProfileUseCase(profileId) }
                .onSuccess {
                    // Profil déjà retiré de la liste
                }
                .onFailure { throwable ->
                    val errorMessage = throwable.message ?: "Failed to pass profile"
                    // Si c'est une erreur 409 (profil déjà passé), c'est OK - on a déjà retiré le profil
                    // Pour les autres erreurs, afficher le message mais ne pas remettre le profil
                    if (!errorMessage.contains("already passed", ignoreCase = true) && 
                        !errorMessage.contains("409", ignoreCase = true) &&
                        !errorMessage.contains("Conflict", ignoreCase = true)) {
                        _uiState.update {
                            it.copy(error = errorMessage)
                        }
                    }
                }
        }
    }
}

