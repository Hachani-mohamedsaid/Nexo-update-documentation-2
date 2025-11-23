package com.example.damandroid.presentation.profile.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.damandroid.auth.UserSession
import com.example.damandroid.api.AuthRepository
import com.example.damandroid.domain.model.ProfileUpdate
import com.example.damandroid.domain.model.UserProfile
import com.example.damandroid.presentation.profile.viewmodel.ProfileViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import kotlin.math.max
import kotlinx.coroutines.launch

@Composable
fun EditProfileRoute(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSave: (ProfileUpdate) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    EditProfileScreen(
        profile = uiState.profile,
        onBack = onBack,
        onSave = { update ->
            viewModel.updateProfile(update)
            onSave(update)
        },
        onImageSelected = { uri -> viewModel.uploadProfileImage(uri, context) },
        modifier = modifier,
        stateLoading = uiState.isLoading,
        error = uiState.error
    )
}

@Composable
fun EditProfileScreen(
    profile: UserProfile?,
    onBack: () -> Unit,
    onSave: (ProfileUpdate) -> Unit,
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    stateLoading: Boolean = false,
    error: String? = null
) {
    val themeController = LocalThemeController.current
    val colors = rememberAppThemeColors(themeController.isDarkMode)
    val scrollState = rememberScrollState()

    val sessionUser = UserSession.user

    val defaultAvatar = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&h=150&fit=crop"
    val defaultEmail = sessionUser?.email ?: "alex.johnson@email.com"
    val defaultPhone = "+1 (555) 123-4567"
    val defaultLocation = "San Francisco, CA"
    val defaultBio = "Passionate about sports and staying active! Love meeting new people through fitness."
    val defaultDateOfBirth = "1995-06-15"

    var fullName by remember(profile) { mutableStateOf(profile?.name ?: sessionUser?.name ?: "Alex Johnson") }
    var email by remember(profile) { mutableStateOf(defaultEmail) }
    var phone by remember(profile) { mutableStateOf(defaultPhone) }
    var location by remember(profile) { mutableStateOf(profile?.location ?: defaultLocation) }
    var bio by remember(profile) { mutableStateOf(profile?.bio ?: defaultBio) }
    var dateOfBirth by remember(profile) { mutableStateOf(defaultDateOfBirth) }
    var avatarUrl by remember(profile) { mutableStateOf(profile?.avatarUrl ?: defaultAvatar) }
    var isEmailVerified by remember(profile) { mutableStateOf(profile?.isVerified == true) }
    var isSendingVerification by remember { mutableStateOf(false) }
    var verificationMessage by remember { mutableStateOf<String?>(null) }
    var verificationMessageSuccess by remember { mutableStateOf<Boolean?>(null) }

    val availableSports = remember {
        listOf(
            "Basketball",
            "Tennis",
            "Running",
            "Swimming",
            "Soccer",
            "Volleyball",
            "Badminton",
            "Yoga",
            "Cycling",
            "Boxing",
            "Climbing",
            "Golf"
        )
    }
    val selectedSports = remember(profile) {
        val initial = profile?.stats?.favoriteSports?.takeIf { it.isNotEmpty() }
            ?: listOf("Basketball", "Tennis", "Running", "Swimming")
        mutableStateListOf<String>().apply { addAll(initial) }
    }

    val context = LocalContext.current
    val authRepository = remember(context) { AuthRepository(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(profile?.avatarUrl) {
        val remoteAvatar = profile?.avatarUrl
        if (!remoteAvatar.isNullOrBlank() && remoteAvatar != avatarUrl && !remoteAvatar.startsWith("content://")) {
            avatarUrl = remoteAvatar
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            avatarUrl = it.toString()
            onImageSelected(it)
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
        FloatingEditProfileOrbs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            EditProfileHeader(
                colors = colors,
                onBack = onBack,
                onSave = {
                    val update = ProfileUpdate(
                        name = fullName.trim().takeIf { it.isNotEmpty() },
                        email = email.trim().takeIf { it.isNotEmpty() },
                        phone = phone.trim().takeIf { it.isNotEmpty() },
                        dateOfBirth = dateOfBirth.trim().takeIf { it.isNotEmpty() },
                        location = location.trim().takeIf { it.isNotEmpty() },
                        about = bio.trim().takeIf { it.isNotEmpty() },
                        sportsInterests = selectedSports.toList(),
                        profileImageUrl = avatarUrl.trim()
                            .takeIf { it.isNotEmpty() && !it.startsWith("content://") }
                    )
                    onSave(update)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            ProfilePhotoCard(
                colors = colors,
                fullName = fullName,
                avatarUrl = avatarUrl,
                onChangePhoto = { imagePickerLauncher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            GlassSectionCard(
                colors = colors,
                title = "Basic Information"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    EmailVerificationCard(
                        colors = colors,
                        email = email,
                        verified = isEmailVerified,
                        isSending = isSendingVerification,
                        statusMessage = verificationMessage,
                        statusSuccess = verificationMessageSuccess,
                        onSend = {
                            val trimmed = email.trim()
                            if (trimmed.isBlank()) {
                                verificationMessage = "Enter an email address first"
                                verificationMessageSuccess = false
                                return@EmailVerificationCard
                            }
                            verificationMessage = null
                            verificationMessageSuccess = null
                            isSendingVerification = true
                            coroutineScope.launch {
                                val result = authRepository.sendVerificationEmail(trimmed)
                                isSendingVerification = false
                                when (result) {
                                    is AuthRepository.PasswordResetResult.Success -> {
                                        verificationMessage = result.message
                                        verificationMessageSuccess = true
                                    }
                                    is AuthRepository.PasswordResetResult.Error -> {
                                        verificationMessage = result.message
                                        verificationMessageSuccess = false
                                    }
                                }
                            }
                        }
                    )
                    LegacyInputField(
                        label = "Full Name",
                        value = fullName,
                        onValueChange = { fullName = it }
                    )
                    LegacyInputField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        leadingIcon = Icons.Default.MailOutline,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    LegacyInputField(
                        label = "Phone",
                        value = phone,
                        onValueChange = { phone = it },
                        leadingIcon = Icons.Default.Phone,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    LegacyInputField(
                        label = "Date of Birth",
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        leadingIcon = Icons.Default.CalendarToday,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    LegacyInputField(
                        label = "Location",
                        value = location,
                        onValueChange = { location = it },
                        leadingIcon = Icons.Default.LocationOn
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassSectionCard(
                colors = colors,
                title = "About Me"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegacyInputField(
                        label = null,
                        value = bio,
                        onValueChange = { bio = it },
                        singleLine = false,
                        minLines = 4
                    )
                    Text(
                        text = "${bio.length}/200 characters",
                        fontSize = 12.sp,
                        color = colors.mutedText
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            GlassSectionCard(
                colors = colors,
                title = "Sports Interests",
                titleIcon = Icons.Default.FavoriteBorder
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Select the sports you're interested in",
                        fontSize = 13.sp,
                        color = colors.secondaryText
                    )
                    LegacyFlowRow(
                        horizontalSpacing = 12.dp,
                        verticalSpacing = 12.dp
                    ) {
                        availableSports.forEach { sport ->
                            val isSelected = sport in selectedSports
                            SelectableBadge(
                                text = sport,
                                selected = isSelected,
                                colors = colors,
                                onClick = {
                                    if (isSelected) {
                                        selectedSports.remove(sport)
                                    } else {
                                        selectedSports.add(sport)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
        if (stateLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Surface(
                    color = Color(0xFFFFE4E6),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Text(
                        text = message,
                        color = Color(0xFFB91C1C),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EditProfileHeader(
    colors: AppThemeColors,
    onBack: () -> Unit,
    onSave: () -> Unit
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
                        text = "Edit Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                }
                GradientButton(
                    text = "Save",
                    colors = colors,
                    onClick = onSave
                )
            }
        }
    }
}

@Composable
private fun ProfilePhotoCard(
    colors: AppThemeColors,
    fullName: String,
    avatarUrl: String,
    onChangePhoto: () -> Unit
) {
    GlassSectionCard(colors = colors) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(4.dp, colors.glassBorder, CircleShape),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onChangePhoto),
                    shape = CircleShape,
                    color = colors.accentPurple,
                    contentColor = colors.iconOnAccent,
                    border = BorderStroke(2.dp, Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Change photo",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Text(
                text = "Tap camera to change photo",
                fontSize = 13.sp,
                color = colors.mutedText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GlassSectionCard(
    colors: AppThemeColors,
    title: String? = null,
    titleIcon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (title != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (titleIcon != null) {
                    Icon(
                        imageVector = titleIcon,
                        contentDescription = null,
                        tint = colors.accentPink,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 6.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primaryText
                )
            }
        }

        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                colors.accentPurple.copy(alpha = 0.16f),
                                colors.accentPink.copy(alpha = 0.14f)
                            )
                        )
                    )
                    .blur(30.dp)
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun LegacyInputField(
    label: String?,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.Black.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.6f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.55f),
                focusedBorderColor = Color.White.copy(alpha = 0.8f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                cursorColor = Color.Black.copy(alpha = 0.7f),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
    }
}

@Composable
private fun EmailVerificationCard(
    colors: AppThemeColors,
    email: String,
    verified: Boolean,
    isSending: Boolean,
    statusMessage: String?,
    statusSuccess: Boolean?,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.glassSurface),
        border = BorderStroke(2.dp, colors.glassBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (verified) Color(0xFFE6F6EB) else Color(0xFFFFF4E5),
                    contentColor = if (verified) Color(0xFF22C55E) else Color(0xFFF59E0B),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (verified) Color(0xFFB7F0C1) else Color(0xFFFFE0B2)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (verified) Icons.Default.CheckCircle else Icons.Default.PriorityHigh,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (verified) "Email Verified" else "Verify Your Email",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.primaryText
                    )
                    val message = if (verified) {
                        "$email is verified. You're all set!"
                    } else {
                        "Please verify $email to unlock all features and secure your account."
                    }
                    Text(
                        text = message,
                        fontSize = 13.sp,
                        color = colors.secondaryText
                    )
                }
            }

            Button(
                onClick = onSend,
                shape = RoundedCornerShape(20.dp),
                enabled = !verified && !isSending,
                colors = when {
                    verified -> ButtonDefaults.buttonColors(
                        containerColor = colors.subtleSurface,
                        contentColor = colors.mutedText,
                        disabledContainerColor = colors.subtleSurface,
                        disabledContentColor = colors.mutedText
                    )
                    else -> ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.White
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (verified) colors.mutedText else Color.White
                    )
                } else {
                    Text(
                        text = when {
                            verified -> "Email verified"
                            else -> "Send verification email"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            statusMessage?.let { message ->
                val statusColor = when (statusSuccess) {
                    true -> Color(0xFF22C55E)
                    false -> Color(0xFFDC2626)
                    else -> colors.secondaryText
                }
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun SelectableBadge(
    text: String,
    selected: Boolean,
    colors: AppThemeColors,
    onClick: () -> Unit
) {
    val background = if (selected) {
        Brush.linearGradient(listOf(colors.accentPurple, colors.accentPink))
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.65f)
            )
        )
    }
    val contentColor = if (selected) colors.iconOnAccent else Color.Black.copy(alpha = 0.75f)
    val border = if (selected) Color.Transparent else Color.White.copy(alpha = 0.8f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .border(2.dp, border, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    colors: AppThemeColors,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.accentPurple,
                        colors.accentPink
                    )
                )
            )
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.iconOnAccent
        )
    }
}

@Composable
private fun LegacyFlowRow(
    modifier: Modifier = Modifier,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    verticalSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        if (measurables.isEmpty()) {
            return@Layout layout(constraints.minWidth, constraints.minHeight) {}
        }

        val horizontalSpacingPx = horizontalSpacing.roundToPx()
        val verticalSpacingPx = verticalSpacing.roundToPx()

        val placeables = measurables.map { measurable ->
            measurable.measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0
                )
            )
        }

        val positions = mutableListOf<Pair<Int, Int>>()
        var currentX = 0
        var currentY = 0
        var rowHeight = 0
        var maxWidthUsed = 0

        placeables.forEach { placeable ->
            if (currentX > 0 && currentX + placeable.width > constraints.maxWidth) {
                currentX = 0
                currentY += rowHeight + verticalSpacingPx
                rowHeight = 0
            }

            positions += currentX to currentY

            currentX += placeable.width + horizontalSpacingPx
            rowHeight = max(rowHeight, placeable.height)
            maxWidthUsed = max(maxWidthUsed, currentX - horizontalSpacingPx)
        }

        val finalWidth = when {
            constraints.maxWidth == Constraints.Infinity -> maxWidthUsed.coerceAtLeast(constraints.minWidth)
            else -> max(constraints.minWidth, minOf(maxWidthUsed, constraints.maxWidth))
        }
        val finalHeight = max(constraints.minHeight, currentY + rowHeight)

        return@Layout layout(finalWidth, finalHeight) {
            placeables.forEachIndexed { index, placeable ->
                val position = positions[index]
                placeable.placeRelative(position.first, position.second)
            }
        }
    }
}

@Composable
private fun FloatingEditProfileOrbs() {
    val colors = listOf(
        Color(0xFF8B5CF6).copy(alpha = 0.22f),
        Color(0xFFEC4899).copy(alpha = 0.2f),
        Color(0xFF60A5FA).copy(alpha = 0.18f)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        val transition = rememberInfiniteTransition(label = "edit-profile-orbs")
        val offset1 by transition.animateFloat(
            initialValue = 0f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "edit-profile-orb1"
        )
        val offset2 by transition.animateFloat(
            initialValue = 0f,
            targetValue = 28f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "edit-profile-orb2"
        )

        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 32.dp + offset1.dp, y = 80.dp)
                .clip(CircleShape)
                .background(colors[0])
                .blur(80.dp)
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-40).dp, y = 380.dp + offset2.dp)
                .clip(CircleShape)
                .background(colors[1])
                .blur(70.dp)
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-24).dp, y = (-80).dp - offset1.dp)
                .clip(CircleShape)
                .background(colors[2])
                .blur(90.dp)
        )
    }
}

