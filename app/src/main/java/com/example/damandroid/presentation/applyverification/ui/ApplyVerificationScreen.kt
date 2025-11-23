package com.example.damandroid.presentation.applyverification.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.damandroid.presentation.applyverification.model.ApplyVerificationUiState
import com.example.damandroid.presentation.applyverification.viewmodel.ApplyVerificationViewModel
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

private enum class LegacyUserType {
    COACH_TRAINER,
    CLUB_OWNER
}

private fun LegacyUserType.displayLabel(): String = when (this) {
    LegacyUserType.COACH_TRAINER -> "Coach / Trainer"
    LegacyUserType.CLUB_OWNER -> "Club Owner"
}

private fun LegacyUserType.keywordMatchers(): List<String> = when (this) {
    LegacyUserType.COACH_TRAINER -> listOf("coach", "trainer")
    LegacyUserType.CLUB_OWNER -> listOf("club", "owner")
}

private fun LegacyUserType.resolveOption(options: List<String>?): String {
    val keywords = keywordMatchers()
    val match = options?.firstOrNull { option ->
        val normalized = option.lowercase()
        keywords.any { keyword -> normalized.contains(keyword) }
    }
    return match ?: displayLabel()
}

private fun mapToLegacyUserType(value: String?): LegacyUserType? {
    val normalized = value?.lowercase()?.trim().orEmpty()
    return when {
        normalized.contains("club") || normalized.contains("owner") -> LegacyUserType.CLUB_OWNER
        normalized.contains("coach") || normalized.contains("trainer") -> LegacyUserType.COACH_TRAINER
        normalized.isBlank() -> null
        else -> null
    }
}

@Composable
fun ApplyVerificationRoute(
    viewModel: ApplyVerificationViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    ApplyVerificationScreen(
        state = uiState,
        onBack = onBack,
        onSubmit = viewModel::submit,
        onDismissResult = viewModel::onDismissResult,
        onUserTypeSelected = viewModel::onUserTypeSelected,
        onFullNameChanged = viewModel::onFullNameChanged,
        onAboutChanged = viewModel::onAboutChanged,
        onSpecializationChanged = viewModel::onSpecializationChanged,
        onClubNameChanged = viewModel::onClubNameChanged,
        onSportFocusChanged = viewModel::onSportFocusChanged,
        onYearsOfExperienceSelected = viewModel::onYearsOfExperienceSelected,
        onCertificationsChanged = viewModel::onCertificationsChanged,
        onLocationChanged = viewModel::onLocationChanged,
        onWebsiteChanged = viewModel::onWebsiteChanged,
        onDocumentsChanged = viewModel::onDocumentsChanged,
        modifier = modifier
    )
}

@Composable
fun ApplyVerificationScreen(
    state: ApplyVerificationUiState,
    onBack: (() -> Unit)?,
    onSubmit: () -> Unit,
    onDismissResult: () -> Unit,
    onUserTypeSelected: (String) -> Unit,
    onFullNameChanged: (String) -> Unit,
    onAboutChanged: (String) -> Unit,
    onSpecializationChanged: (String) -> Unit,
    onClubNameChanged: (String) -> Unit,
    onSportFocusChanged: (String) -> Unit,
    onYearsOfExperienceSelected: (String) -> Unit,
    onCertificationsChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onWebsiteChanged: (String) -> Unit,
    onDocumentsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(state.error, modifier)
        else -> LegacyApplyVerificationContent(
            state = state,
            onBack = onBack,
            onSubmit = onSubmit,
            onDismissResult = onDismissResult,
            onUserTypeSelected = onUserTypeSelected,
            onFullNameChanged = onFullNameChanged,
            onAboutChanged = onAboutChanged,
            onSpecializationChanged = onSpecializationChanged,
            onClubNameChanged = onClubNameChanged,
            onSportFocusChanged = onSportFocusChanged,
            onYearsOfExperienceSelected = onYearsOfExperienceSelected,
            onCertificationsChanged = onCertificationsChanged,
            onLocationChanged = onLocationChanged,
            onWebsiteChanged = onWebsiteChanged,
            onDocumentsChanged = onDocumentsChanged,
            modifier = modifier
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegacyApplyVerificationContent(
    state: ApplyVerificationUiState,
    onBack: (() -> Unit)?,
    onSubmit: () -> Unit,
    onDismissResult: () -> Unit,
    onUserTypeSelected: (String) -> Unit,
    onFullNameChanged: (String) -> Unit,
    onAboutChanged: (String) -> Unit,
    onSpecializationChanged: (String) -> Unit,
    onClubNameChanged: (String) -> Unit,
    onSportFocusChanged: (String) -> Unit,
    onYearsOfExperienceSelected: (String) -> Unit,
    onCertificationsChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onWebsiteChanged: (String) -> Unit,
    onDocumentsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val theme = rememberAppThemeColors(themeController.isDarkMode)
    val yearsOptions = remember { (1..50).map { "$it years" } }

    var yearsExpanded by remember { mutableStateOf(false) }
    val selectedUserType = remember { mutableStateOf(mapToLegacyUserType(state.selectedUserType) ?: LegacyUserType.COACH_TRAINER) }

    LaunchedEffect(state.selectedUserType) {
        mapToLegacyUserType(state.selectedUserType)?.let { selectedUserType.value = it }
    }

    val defaultsInitialized = remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading, state.formOptions) {
        if (!defaultsInitialized.value && !state.isLoading) {
            val initialType = mapToLegacyUserType(state.selectedUserType) ?: LegacyUserType.COACH_TRAINER
            val resolved = initialType.resolveOption(state.formOptions?.userTypes)
            if (state.selectedUserType.isBlank()) {
                selectedUserType.value = initialType
                onUserTypeSelected(resolved)
            }
            if (state.fullName.isBlank()) onFullNameChanged("John Smith")
            if (state.specialization.isBlank()) onSpecializationChanged("Running, Fitness")
            if (state.clubName.isBlank()) onClubNameChanged("SportHub LA")
            if (state.sportFocus.isBlank()) onSportFocusChanged("Tennis, Swimming")
            defaultsInitialized.value = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.backgroundGradient)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    color = theme.glassSurface,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onBack?.invoke() },
                            modifier = Modifier.size(40.dp),
                            enabled = onBack != null
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = theme.primaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = "Apply for Verification",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.primaryText
                        )

                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "I am a *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.secondaryText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val userTypeOptions = state.formOptions?.userTypes
                        UserTypeButton(
                            type = LegacyUserType.COACH_TRAINER,
                            isSelected = selectedUserType.value == LegacyUserType.COACH_TRAINER,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedUserType.value = LegacyUserType.COACH_TRAINER
                            onUserTypeSelected(LegacyUserType.COACH_TRAINER.resolveOption(userTypeOptions))
                        }
                        UserTypeButton(
                            type = LegacyUserType.CLUB_OWNER,
                            isSelected = selectedUserType.value == LegacyUserType.CLUB_OWNER,
                            theme = theme,
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedUserType.value = LegacyUserType.CLUB_OWNER
                            onUserTypeSelected(LegacyUserType.CLUB_OWNER.resolveOption(userTypeOptions))
                        }
                    }

                    OutlinedTextField(
                        value = state.fullName,
                        onValueChange = onFullNameChanged,
                        label = { Text("Full Name *", color = theme.secondaryText) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = legacyTextFieldColors(theme)
                    )

                    OutlinedTextField(
                        value = state.about,
                        onValueChange = onAboutChanged,
                        label = { Text("About *") },
                        placeholder = { Text("Tell us about your coaching experience and philosophy...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 4,
                        colors = legacyTextFieldColors(theme)
                    )

                    if (selectedUserType.value == LegacyUserType.COACH_TRAINER) {
                        OutlinedTextField(
                            value = state.specialization,
                            onValueChange = onSpecializationChanged,
                            label = { Text("Specialization *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = legacyTextFieldColors(theme)
                        )
                    } else {
                        OutlinedTextField(
                            value = state.clubName,
                            onValueChange = onClubNameChanged,
                            label = { Text("Club Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = legacyTextFieldColors(theme)
                        )

                        OutlinedTextField(
                            value = state.sportFocus,
                            onValueChange = onSportFocusChanged,
                            label = { Text("Sport Focus *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = legacyTextFieldColors(theme)
                        )
                    }

                    ExposedDropdownMenuBox(
                        expanded = yearsExpanded,
                        onExpandedChange = { yearsExpanded = !yearsExpanded }
                    ) {
                        OutlinedTextField(
                            value = state.yearsOfExperience,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Years of Experience *", color = theme.secondaryText) },
                            placeholder = { Text("Select years", color = theme.mutedText) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearsExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = legacyTextFieldColors(theme)
                        )
                        ExposedDropdownMenu(
                            expanded = yearsExpanded,
                            onDismissRequest = { yearsExpanded = false }
                        ) {
                            yearsOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        yearsExpanded = false
                                        onYearsOfExperienceSelected(option)
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = state.certifications,
                        onValueChange = onCertificationsChanged,
                        label = { Text("Certifications / License *") },
                        placeholder = {
                            Text(
                                if (selectedUserType.value == LegacyUserType.COACH_TRAINER) "NASM CPT, ACE, etc."
                                else "Business License Number"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = legacyTextFieldColors(theme)
                    )

                    OutlinedTextField(
                        value = state.location,
                        onValueChange = onLocationChanged,
                        label = { Text("Location *") },
                        placeholder = { Text("City, State") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = legacyTextFieldColors(theme)
                    )

                    OutlinedTextField(
                        value = state.website,
                        onValueChange = onWebsiteChanged,
                        label = { Text("Website / Social Media") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = legacyTextFieldColors(theme)
                    )

                    Text(
                        text = "Upload Verification Documents *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = theme.secondaryText,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable { onDocumentsChanged(listOf("document_placeholder.pdf")) },
                        shape = RoundedCornerShape(16.dp),
                        color = theme.subtleSurface,
                        border = BorderStroke(2.dp, theme.glassBorder)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Upload,
                                contentDescription = null,
                                tint = theme.accentPink,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Upload ID, Certifications, or Business License",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.primaryText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PDF, JPG, or PNG (max 5MB)",
                                fontSize = 11.sp,
                                color = theme.mutedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Button(
                        onClick = onSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = theme.iconOnAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Submit Application",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.iconOnAccent
                        )
                    }

                    Text(
                        text = "By submitting, you agree to our verification process and terms of service.",
                        fontSize = 11.sp,
                        color = theme.mutedText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }

    if (state.submissionResult != null) {
        onDismissResult()
    }
}

@Composable
private fun UserTypeButton(
    type: LegacyUserType,
    isSelected: Boolean,
    theme: AppThemeColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) theme.subtleSurface else theme.glassSurface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) theme.accentBlue else theme.glassBorder
        ),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (type == LegacyUserType.COACH_TRAINER) "🏆" else "🏢",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = type.displayLabel(),
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = theme.primaryText
            )
        }
    }
}

@Composable
private fun legacyTextFieldColors(theme: AppThemeColors) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = theme.glassSurface,
    unfocusedContainerColor = theme.glassSurface,
    focusedBorderColor = theme.accentPurple,
    unfocusedBorderColor = theme.glassBorder,
    unfocusedTextColor = theme.primaryText,
    focusedTextColor = theme.primaryText,
    cursorColor = theme.primaryText,
    unfocusedLabelColor = theme.secondaryText,
    focusedLabelColor = theme.primaryText
)

