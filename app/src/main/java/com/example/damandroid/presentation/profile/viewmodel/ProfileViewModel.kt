package com.example.damandroid.presentation.profile.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.damandroid.domain.model.ProfileImageUpload
import com.example.damandroid.domain.model.ProfileUpdate
import com.example.damandroid.domain.usecase.ChangeUserPassword
import com.example.damandroid.domain.usecase.GetCurrentUserProfile
import com.example.damandroid.domain.usecase.UpdateUserProfile
import com.example.damandroid.domain.usecase.UploadProfileImage
import com.example.damandroid.presentation.profile.model.ProfileTab
import com.example.damandroid.presentation.profile.model.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getCurrentUserProfile: GetCurrentUserProfile,
    private val updateUserProfile: UpdateUserProfile,
    private val uploadProfileImage: UploadProfileImage,
    private val changeUserPassword: ChangeUserPassword
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    isChangingPassword = false,
                    changePasswordMessage = null,
                    changePasswordError = null
                )
            }
            runCatching { getCurrentUserProfile() }
                .onSuccess { profile ->
                    android.util.Log.d("ProfileViewModel", "Profile loaded successfully: activities=${profile.activities.size}, medals=${profile.medals.size}")
                    profile.activities.forEach { activity ->
                        android.util.Log.d("ProfileViewModel", "Activity: ${activity.title}")
                    }
                    profile.medals.forEach { medal ->
                        android.util.Log.d("ProfileViewModel", "Medal: ${medal.title}")
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = profile,
                            error = null,
                            isChangingPassword = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load profile",
                            isChangingPassword = false
                        )
                    }
                }
        }
    }

    fun onTabSelected(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updateProfile(update: ProfileUpdate) {
        val currentId = _uiState.value.profile?.id
            ?: com.example.damandroid.auth.UserSession.user?.id
            ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching { updateUserProfile(currentId, update) }
                .onSuccess { updatedProfile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = updatedProfile,
                            error = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to update profile"
                        )
                    }
                }
        }
    }

    fun uploadProfileImage(uri: Uri, context: Context) {
        val currentId = _uiState.value.profile?.id
            ?: com.example.damandroid.auth.UserSession.user?.id
            ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(uri) ?: "image/jpeg"
                val extension = mimeType.substringAfter('/', "jpg")
                val fileName = "profile_${System.currentTimeMillis()}.$extension"
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalArgumentException("Unable to read selected image")

                val upload = ProfileImageUpload(
                    fileName = fileName,
                    mimeType = mimeType,
                    bytes = bytes
                )

                uploadProfileImage(currentId, upload)
            }.onSuccess { updatedProfile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = updatedProfile,
                        error = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Unable to upload profile image"
                    )
                }
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        val currentId = _uiState.value.profile?.id
            ?: com.example.damandroid.auth.UserSession.user?.id
            ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isChangingPassword = true,
                    changePasswordError = null,
                    changePasswordMessage = null
                )
            }

            runCatching { changeUserPassword(currentId, currentPassword, newPassword) }
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            changePasswordMessage = response.message,
                            changePasswordError = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isChangingPassword = false,
                            changePasswordError = throwable.message ?: "Unable to change password"
                        )
                    }
                }
        }
    }

    fun resetChangePasswordStatus() {
        _uiState.update {
            it.copy(
                changePasswordMessage = null,
                changePasswordError = null,
                isChangingPassword = false
            )
        }
    }
}

