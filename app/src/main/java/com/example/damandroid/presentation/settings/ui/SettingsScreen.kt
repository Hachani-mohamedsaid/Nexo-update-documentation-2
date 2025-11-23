package com.example.damandroid.presentation.settings.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damandroid.domain.model.SettingsItem
import com.example.damandroid.domain.model.SettingsItemType
import com.example.damandroid.domain.model.UserSettings
import com.example.damandroid.domain.model.VerificationStatus
import com.example.damandroid.presentation.settings.model.SettingsUiState
import com.example.damandroid.presentation.settings.viewmodel.SettingsViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onApplyVerification: (() -> Unit)?,
    onEditProfile: (() -> Unit)?,
    onChangePassword: (() -> Unit)?,
    onLogout: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        state = uiState,
        onBack = onBack,
        onApplyVerification = onApplyVerification,
        onEditProfile = onEditProfile,
        onChangePassword = onChangePassword,
        onLogout = onLogout,
        onToggleChanged = viewModel::onToggleChanged,
        onRefresh = viewModel::refresh,
        modifier = modifier
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onApplyVerification: (() -> Unit)?,
    onEditProfile: (() -> Unit)?,
    onChangePassword: (() -> Unit)?,
    onLogout: (() -> Unit)?,
    onToggleChanged: (String, Boolean) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(message = state.error, onRetry = onRefresh, modifier = modifier)
        state.settings != null -> ContentState(
            settings = state.settings,
            onBack = onBack,
            onApplyVerification = onApplyVerification,
            onEditProfile = onEditProfile,
            onChangePassword = onChangePassword,
            onLogout = onLogout,
            onToggleChanged = onToggleChanged,
            modifier = modifier
        )
        else -> ErrorState(
            message = "Settings unavailable",
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
private fun ContentState(
    settings: UserSettings,
    onBack: () -> Unit,
    onApplyVerification: (() -> Unit)?,
    onEditProfile: (() -> Unit)?,
    onChangePassword: (() -> Unit)?,
    onLogout: (() -> Unit)?,
    onToggleChanged: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val colors = rememberAppThemeColors(themeController.isDarkMode)

    // Create Appearance section with Night Mode toggle
    val appearanceSection = SettingsSection(
        "Appearance",
        listOf(
            SettingsItem(
                id = "night_mode",
                label = "Night Mode",
                icon = "dark_mode",
                type = SettingsItemType.TOGGLE,
                value = themeController.isDarkMode,
                extra = null
            )
        )
    )

    val sections = listOfNotNull(
        appearanceSection,
        SettingsSection("Account", settings.accountSettings),
        SettingsSection("AI Preferences", settings.aiPreferences),
        SettingsSection("Privacy", settings.privacySettings),
        SettingsSection("Notifications", settings.notificationSettings),
        SettingsSection("App Info", settings.appInfoItems)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundGradient)
    ) {
        FloatingSettingsOrbs(colors)

        Column(modifier = Modifier.fillMaxSize()) {
            SettingsHeader(onBack = onBack, colors = colors)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    VerificationStatusCard(
                        status = settings.verificationStatus,
                        colors = colors,
                        onApplyVerification = onApplyVerification
                    )
                }

                sections.forEach { section ->
                    item {
                        SettingsSectionCard(
                            title = section.title,
                            items = section.items,
                            colors = colors,
                            onToggleChanged = { id, value ->
                                if (id == "night_mode") {
                                    themeController.toggle()
                                } else {
                                    onToggleChanged(id, value)
                                }
                            },
                            onNavigate = { item ->
                                when (item.id) {
                                    "edit_profile" -> onEditProfile?.invoke()
                                    "change_password" -> onChangePassword?.invoke()
                                    "blocked_users",
                                    "terms",
                                    "privacy",
                                    "support",
                                    "about" -> { /* TODO */ }
                                }
                            }
                        )
                    }
                }

                if (onLogout != null) {
                    item {
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.danger),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = colors.iconOnAccent,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Log out",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.iconOnAccent
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

@Composable
private fun SettingsHeader(
    onBack: () -> Unit,
    colors: AppThemeColors
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.glassSurface),
        border = BorderStroke(1.dp, colors.glassBorder),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primaryText,
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = "Profile & Settings",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryText,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
private fun VerificationStatusCard(
    status: VerificationStatus,
    colors: AppThemeColors,
    onApplyVerification: (() -> Unit)?
) {
    val uiModel = when (status) {
        VerificationStatus.NONE -> VerificationStatusUiModel(
            title = "Get verified",
            description = "Unlock the verified badge to build trust with other athletes.",
            buttonLabel = "Apply now",
            buttonEnabled = true,
            badgeColor = colors.accentPurple
        )
        VerificationStatus.PENDING -> VerificationStatusUiModel(
            title = "Verification pending",
            description = "Your application is under review. We'll notify you once it's approved.",
            buttonLabel = "View status",
            buttonEnabled = false,
            badgeColor = colors.warning
        )
        VerificationStatus.APPROVED -> VerificationStatusUiModel(
            title = "You're verified",
            description = "Your profile is verified. Thanks for being a trusted member!",
            buttonLabel = "Verified",
            buttonEnabled = false,
            badgeColor = colors.success
        )
        VerificationStatus.REJECTED -> VerificationStatusUiModel(
            title = "Verification declined",
            description = "We couldn't verify your profile this time. You can review and apply again.",
            buttonLabel = "Apply again",
            buttonEnabled = true,
            badgeColor = colors.danger
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.glassSurface),
        border = BorderStroke(1.dp, colors.glassBorder),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                uiModel.badgeColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = uiModel.badgeColor,
                        contentColor = colors.iconOnAccent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null)
                        }
                    }
                    Column {
                        Text(text = uiModel.title, color = colors.primaryText, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text(text = uiModel.description, color = colors.secondaryText, fontSize = 13.sp)
                    }
                }

                if (onApplyVerification != null) {
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onApplyVerification,
                            enabled = uiModel.buttonEnabled,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, uiModel.badgeColor.copy(alpha = 0.6f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = uiModel.badgeColor)
                        ) {
                            Text(text = uiModel.buttonLabel, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

private data class VerificationStatusUiModel(
    val title: String,
    val description: String,
    val buttonLabel: String,
    val buttonEnabled: Boolean,
    val badgeColor: Color
)

@Composable
private fun SettingsSectionCard(
    title: String,
    items: List<SettingsItem>,
    colors: AppThemeColors,
    onToggleChanged: (String, Boolean) -> Unit,
    onNavigate: (SettingsItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.primaryText,
            modifier = Modifier.padding(start = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.glassSurface),
            border = BorderStroke(1.dp, colors.glassBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEachIndexed { index, item ->
                    SettingRow(
                        item = item,
                        colors = colors,
                        onToggleChanged = onToggleChanged,
                        onNavigate = onNavigate
                    )
                    if (index != items.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(1.dp)
                                .background(colors.glassBorder.copy(alpha = 0.4f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    item: SettingsItem,
    colors: AppThemeColors,
    onToggleChanged: (String, Boolean) -> Unit,
    onNavigate: (SettingsItem) -> Unit
) {
    val clickableModifier = if (item.type == SettingsItemType.NAVIGATE) {
        Modifier.clickable { onNavigate(item) }
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(14.dp),
            color = colors.subtleSurface,
            contentColor = if (item.id == "night_mode") colors.accentPurple else colors.primaryText,
            border = BorderStroke(1.dp, colors.glassBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = item.icon.toImageVector(),
                    contentDescription = null,
                    tint = if (item.id == "night_mode") colors.accentPurple else colors.primaryText
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.label, fontSize = 15.sp, color = colors.primaryText)
            if (!item.extra.isNullOrBlank()) {
                Text(text = item.extra, fontSize = 12.sp, color = colors.mutedText)
            }
        }

        when (item.type) {
            SettingsItemType.NAVIGATE -> {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.mutedText,
                    modifier = Modifier.size(18.dp)
                )
            }
            SettingsItemType.TOGGLE -> {
                Switch(
                    checked = item.value,
                    onCheckedChange = { onToggleChanged(item.id, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.iconOnAccent,
                        checkedTrackColor = colors.accentPurple,
                        uncheckedThumbColor = colors.glassSurface,
                        uncheckedTrackColor = colors.glassBorder
                    )
                )
            }
        }
    }
}

private fun String.toImageVector(): ImageVector = when (this.lowercase()) {
    "person" -> Icons.Default.Person
    "lock" -> Icons.Default.Lock
    "sparkles" -> Icons.Default.AutoAwesome
    "shield" -> Icons.Default.Shield
    "person_off" -> Icons.Default.PersonOff
    "notifications" -> Icons.Default.Notifications
    "mail" -> Icons.Default.MailOutline
    "volume" -> Icons.Default.Notifications
    "info" -> Icons.Default.Info
    "location" -> Icons.Default.Place
    "dark_mode" -> Icons.Default.DarkMode
    else -> Icons.Default.Settings
}

@Composable
private fun FloatingSettingsOrbs(colors: AppThemeColors) {
    val accentPurple = colors.accentPurple
    val accentPink = colors.accentPink
    val accentBlue = colors.accentBlue
    val accentTeal = colors.accentTeal
    val glassSurface = colors.glassSurface

    Box(modifier = Modifier.fillMaxSize()) {
        val orb1Transition = rememberInfiniteTransition(label = "settings-orb1")
        val orb1Alpha by orb1Transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "settings-orb1-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = 36.dp, y = 96.dp)
                .size(132.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accentPurple.copy(alpha = orb1Alpha),
                            glassSurface.copy(alpha = orb1Alpha * 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .blur(48.dp)
        )

        val orb2Transition = rememberInfiniteTransition(label = "settings-orb2")
        val orb2Alpha by orb2Transition.animateFloat(
            initialValue = 0.32f,
            targetValue = 0.16f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, delayMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "settings-orb2-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = (-32).dp, y = (-48).dp)
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accentPink.copy(alpha = orb2Alpha),
                            glassSurface.copy(alpha = orb2Alpha * 0.55f),
                            Color.Transparent
                        )
                    )
                )
                .blur(52.dp)
                .align(Alignment.BottomEnd)
        )

        val orb3Transition = rememberInfiniteTransition(label = "settings-orb3")
        val orb3Alpha by orb3Transition.animateFloat(
            initialValue = 0.28f,
            targetValue = 0.14f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, delayMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "settings-orb3-alpha"
        )

        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accentBlue.copy(alpha = orb3Alpha),
                            glassSurface.copy(alpha = orb3Alpha * 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .blur(36.dp)
                .align(Alignment.Center)
        )

        val orb4Transition = rememberInfiniteTransition(label = "settings-orb4")
        val orb4Alpha by orb4Transition.animateFloat(
            initialValue = 0.24f,
            targetValue = 0.12f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3200, delayMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "settings-orb4-alpha"
        )

        Box(
            modifier = Modifier
                .offset(x = (-12).dp, y = 120.dp)
                .size(88.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accentTeal.copy(alpha = orb4Alpha),
                            glassSurface.copy(alpha = orb4Alpha * 0.55f),
                            Color.Transparent
                        )
                    )
                )
                .blur(32.dp)
                .align(Alignment.TopEnd)
        )
    }
}

