package com.example.damandroid.presentation.settings.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damandroid.presentation.profile.viewmodel.ProfileViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import kotlinx.coroutines.delay

@Composable
fun ChangePasswordRoute(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val colors = rememberAppThemeColors(themeController.isDarkMode)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetChangePasswordStatus()
    }

    ChangePasswordScreen(
        colors = colors,
        onBack = onBack,
        onChangePassword = viewModel::changePassword,
        onDismissMessage = viewModel::resetChangePasswordStatus,
        isProcessing = uiState.isChangingPassword,
        successMessage = uiState.changePasswordMessage,
        errorMessage = uiState.changePasswordError,
        modifier = modifier
    )
}

@Composable
private fun ChangePasswordScreen(
    colors: AppThemeColors,
    onBack: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    onDismissMessage: () -> Unit,
    isProcessing: Boolean,
    successMessage: String?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    var currentError by remember { mutableStateOf<String?>(null) }
    var newError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    val passwordPattern = remember { Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+") }

    fun validate(): Boolean {
        var isValid = true
        if (currentPassword.isBlank()) {
            currentError = "Current password is required"
            isValid = false
        } else {
            currentError = null
        }

        if (newPassword.isBlank()) {
            newError = "New password is required"
            isValid = false
        } else if (newPassword.length < 8) {
            newError = "Password must be at least 8 characters"
            isValid = false
        } else if (newPassword == currentPassword) {
            newError = "New password must be different from current"
            isValid = false
        } else if (!passwordPattern.matches(newPassword)) {
            newError = "Must include upper, lower, number, special char"
            isValid = false
        } else {
            newError = null
        }

        if (confirmPassword.isBlank()) {
            confirmError = "Please confirm your password"
            isValid = false
        } else if (newPassword != confirmPassword) {
            confirmError = "Passwords do not match"
            isValid = false
        } else {
            confirmError = null
        }
        return isValid
    }

    fun handleSave() {
        if (validate()) {
            onChangePassword(currentPassword, newPassword)
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            // Give a short delay to show the success message before navigating back
            delay(600)
            onDismissMessage()
            onBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE8D5F2),
                        Color(0xFFFFE4F1),
                        Color(0xFFE5E5F0)
                    )
                )
            )
    ) {
        FloatingChangePasswordOrbs()

        if (errorMessage != null && successMessage == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.clickable { onDismissMessage() }
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFB91C1C),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        fontSize = 13.sp
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            ChangePasswordHeader(
                colors = colors,
                onBack = {
                    onDismissMessage()
                    onBack()
                },
                onSave = ::handleSave,
                isProcessing = isProcessing
            )

            Spacer(modifier = Modifier.height(20.dp))

            SecurityTipCard(colors)

            Spacer(modifier = Modifier.height(20.dp))

            PasswordInformationCard(
                colors = colors,
                currentPassword = currentPassword,
                onCurrentPasswordChange = {
                    currentPassword = it
                    currentError = null
                },
                currentVisible = showCurrent,
                onCurrentVisibilityToggle = { showCurrent = !showCurrent },
                currentError = currentError,
                newPassword = newPassword,
                onNewPasswordChange = {
                    newPassword = it
                    newError = null
                },
                newVisible = showNew,
                onNewVisibilityToggle = { showNew = !showNew },
                newError = newError,
                confirmPassword = confirmPassword,
                onConfirmPasswordChange = {
                    confirmPassword = it
                    confirmError = null
                },
                confirmVisible = showConfirm,
                onConfirmVisibilityToggle = { showConfirm = !showConfirm },
                confirmError = confirmError,
                enabled = !isProcessing
            )

            Spacer(modifier = Modifier.height(20.dp))

            PasswordRequirementsCard(colors)

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ChangePasswordHeader(
    colors: AppThemeColors,
    onBack: () -> Unit,
    onSave: () -> Unit,
    isProcessing: Boolean
) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.accentPurple.copy(alpha = 0.18f),
                            colors.accentPink.copy(alpha = 0.16f)
                        )
                    )
                )
                .blur(32.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colors.glassSurface),
            border = BorderStroke(2.dp, colors.glassBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.primaryText
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Change Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                }
                GradientButton(
                    colors = colors,
                    onClick = onSave,
                    enabled = !isProcessing,
                    isProcessing = isProcessing
                )
            }
        }
    }
}

@Composable
private fun GradientButton(
    colors: AppThemeColors,
    onClick: () -> Unit,
    enabled: Boolean,
    isProcessing: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.accentPurple, colors.accentPink)
                )
            )
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = colors.iconOnAccent
            )
        } else {
            Text(
                text = "Save",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.iconOnAccent
            )
        }
    }
}

@Composable
private fun SecurityTipCard(colors: AppThemeColors) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.accentPurple.copy(alpha = 0.2f),
                            colors.accentPink.copy(alpha = 0.2f)
                        )
                    )
                )
                .blur(32.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = colors.glassSurface),
            border = BorderStroke(2.dp, colors.glassBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.accentPurple.copy(alpha = 0.18f))
                        .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = colors.accentPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Security Tip",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                    Text(
                        text = "Use a strong password with at least 8 characters, including letters, numbers, and symbols.",
                        fontSize = 12.sp,
                        color = colors.secondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordInformationCard(
    colors: AppThemeColors,
    currentPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    currentVisible: Boolean,
    onCurrentVisibilityToggle: () -> Unit,
    currentError: String?,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    newVisible: Boolean,
    onNewVisibilityToggle: () -> Unit,
    newError: String?,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmVisible: Boolean,
    onConfirmVisibilityToggle: () -> Unit,
    confirmError: String?,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Password Information",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.primaryText
        )
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colors.accentPurple.copy(alpha = 0.15f),
                                colors.accentPink.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .blur(28.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = colors.glassSurface),
                border = BorderStroke(2.dp, colors.glassBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    PasswordField(
                        label = "Current Password",
                        placeholder = "Enter current password",
                        value = currentPassword,
                        onValueChange = onCurrentPasswordChange,
                        isVisible = currentVisible,
                        onVisibilityToggle = onCurrentVisibilityToggle,
                        error = currentError,
                        colors = colors,
                        enabled = enabled
                    )
                    PasswordField(
                        label = "New Password",
                        placeholder = "Enter new password",
                        value = newPassword,
                        onValueChange = onNewPasswordChange,
                        isVisible = newVisible,
                        onVisibilityToggle = onNewVisibilityToggle,
                        error = newError,
                        colors = colors,
                        enabled = enabled
                    )
                    PasswordField(
                        label = "Confirm New Password",
                        placeholder = "Confirm new password",
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        isVisible = confirmVisible,
                        onVisibilityToggle = onConfirmVisibilityToggle,
                        error = confirmError,
                        colors = colors,
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    error: String?,
    colors: AppThemeColors,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = colors.secondaryText
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = colors.secondaryText)
            },
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isVisible) "Hide password" else "Show password",
                        tint = colors.secondaryText
                    )
                }
            },
            placeholder = {
                Text(text = placeholder, color = colors.secondaryText.copy(alpha = 0.6f))
            },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.6f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.55f),
                focusedBorderColor = Color.White.copy(alpha = 0.8f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                cursorColor = colors.primaryText,
                focusedTextColor = colors.primaryText,
                unfocusedTextColor = colors.primaryText
            )
        )
        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                fontSize = 11.sp,
                color = Color(0xFFE11D48)
            )
        }
    }
}

@Composable
private fun PasswordRequirementsCard(colors: AppThemeColors) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.accentPurple.copy(alpha = 0.12f),
                            colors.accentPink.copy(alpha = 0.12f)
                        )
                    )
                )
                .blur(26.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.glassSurface.copy(alpha = 0.9f)),
            border = BorderStroke(2.dp, colors.glassBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Password Requirements:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primaryText
                )
                RequirementBullet("At least 8 characters long", colors)
                RequirementBullet("Include uppercase and lowercase letters", colors)
                RequirementBullet("Include at least one number", colors)
                RequirementBullet("Include at least one special character", colors)
            }
        }
    }
}

@Composable
private fun RequirementBullet(text: String, colors: AppThemeColors) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(colors.accentPurple)
        )
        Text(text = text, fontSize = 11.sp, color = colors.secondaryText)
    }
}

@Composable
private fun FloatingChangePasswordOrbs() {
    Box(modifier = Modifier.fillMaxSize()) {
        val transition = rememberInfiniteTransition(label = "change-password-orbs")
        val offset1 by transition.animateFloat(
            initialValue = 0f,
            targetValue = 24f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb1"
        )
        val offset2 by transition.animateFloat(
            initialValue = 0f,
            targetValue = 28f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4800, easing = LinearEasing, delayMillis = 600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb2"
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 32.dp + offset1.dp, y = 100.dp)
                .clip(CircleShape)
                .background(Color(0xFF8B5CF6).copy(alpha = 0.25f))
                .blur(90.dp)
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-20).dp, y = 360.dp + offset2.dp)
                .clip(CircleShape)
                .background(Color(0xFF60A5FA).copy(alpha = 0.2f))
                .blur(80.dp)
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-16).dp, y = (-60).dp - offset1.dp)
                .clip(CircleShape)
                .background(Color(0xFFEC4899).copy(alpha = 0.22f))
                .blur(85.dp)
        )
    }
}
