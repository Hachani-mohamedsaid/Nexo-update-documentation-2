package com.example.damandroid.presentation.applyverification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.model.VerificationApplication
import com.example.damandroid.domain.model.VerificationStatus
import com.example.damandroid.domain.usecase.GetVerificationFormOptions
import com.example.damandroid.domain.usecase.SubmitVerificationApplication
import com.example.damandroid.presentation.applyverification.model.ApplyVerificationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ApplyVerificationViewModel(
    private val getVerificationFormOptions: GetVerificationFormOptions,
    private val submitVerificationApplication: SubmitVerificationApplication
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApplyVerificationUiState(isLoading = true))
    val uiState: StateFlow<ApplyVerificationUiState> = _uiState

    init {
        loadForm()
    }

    fun loadForm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getVerificationFormOptions() }
                .onSuccess { options ->
                    val defaultUserType = options.userTypes.firstOrNull().orEmpty()
                    val defaultCountry = options.countries.firstOrNull().orEmpty()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            formOptions = options,
                            selectedUserType = defaultUserType,
                            country = defaultCountry
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load verification form"
                        )
                    }
                }
        }
    }

    fun onUserTypeSelected(type: String) {
        _uiState.update { it.copy(selectedUserType = type) }
    }

    fun onFullNameChanged(name: String) {
        _uiState.update { it.copy(fullName = name) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onCountrySelected(country: String) {
        _uiState.update { it.copy(country = country) }
    }

    fun onDocumentsChanged(documents: List<String>) {
        _uiState.update { it.copy(documents = documents) }
    }

    fun onAboutChanged(text: String) {
        _uiState.update { it.copy(about = text) }
    }

    fun onSpecializationChanged(value: String) {
        _uiState.update { it.copy(specialization = value) }
    }

    fun onClubNameChanged(value: String) {
        _uiState.update { it.copy(clubName = value) }
    }

    fun onSportFocusChanged(value: String) {
        _uiState.update { it.copy(sportFocus = value) }
    }

    fun onYearsOfExperienceSelected(value: String) {
        _uiState.update { it.copy(yearsOfExperience = value) }
    }

    fun onCertificationsChanged(value: String) {
        _uiState.update { it.copy(certifications = value) }
    }

    fun onLocationChanged(value: String) {
        _uiState.update { it.copy(location = value) }
    }

    fun onWebsiteChanged(value: String) {
        _uiState.update { it.copy(website = value) }
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(note = value) }
    }

    fun submit() {
        val state = _uiState.value
        val options = state.formOptions ?: return
        val userType = state.selectedUserType.ifBlank { options.userTypes.firstOrNull().orEmpty() }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                submitVerificationApplication(
                    VerificationApplication(
                        userType = userType,
                        fullName = state.fullName,
                        email = state.email,
                        country = state.country,
                        documents = state.documents,
                        note = state.note
                    )
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        submissionResult = result,
                        status = result.status
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Failed to submit verification"
                    )
                }
            }
        }
    }

    fun onDismissResult() {
        _uiState.update { it.copy(submissionResult = null) }
    }
}

