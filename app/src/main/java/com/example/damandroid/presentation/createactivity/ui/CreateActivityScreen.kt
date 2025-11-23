package com.example.damandroid.presentation.createactivity.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.damandroid.domain.model.ActivityVisibility
import com.example.damandroid.domain.model.SkillLevel
import com.example.damandroid.domain.model.SportCategory
import com.example.damandroid.presentation.createactivity.model.CreateActivityUiState
import com.example.damandroid.presentation.createactivity.viewmodel.CreateActivityViewModel
import com.example.damandroid.presentation.achievements.ui.components.BadgeUnlockedDialog
import com.example.damandroid.presentation.achievements.ui.components.LevelUpDialog
import com.example.damandroid.presentation.achievements.ui.components.ChallengeCompletedDialog
import com.example.damandroid.presentation.achievements.NotificationDisplayStore
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import com.example.damandroid.api.RetrofitClient
import com.example.damandroid.api.CityLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun CreateActivityRoute(
    viewModel: CreateActivityViewModel,
    achievementsViewModel: com.example.damandroid.presentation.achievements.viewmodel.AchievementsViewModel? = null,
    onBack: () -> Unit,
    onSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Réinitialiser le formulaire quand l'écran entre dans la composition
    // Utiliser DisposableEffect pour s'assurer que c'est fait immédiatement
    DisposableEffect(Unit) {
        // Réinitialiser success immédiatement pour éviter le déclenchement prématuré
        // Cela garantit que success est null avant que LaunchedEffect(uiState.success) ne se déclenche
        viewModel.resetForm()
        onDispose {
            // Optionnel : réinitialiser aussi à la sortie pour être sûr
        }
    }
    
    val context = LocalContext.current
    val notificationStore = remember { NotificationDisplayStore(context) }
    
    // Écouter les événements de création d'activité pour rafraîchir les achievements
    achievementsViewModel?.let { achievementsVM ->
        val newBadges by achievementsVM.newBadgesUnlocked.collectAsState()
        val levelUp by achievementsVM.levelUpEvent.collectAsState()
        
        // Écouter les activités créées et rafraîchir les badges
        // Selon le guide : Rafraîchir les badges après 1.5-2 secondes pour laisser le backend traiter
        LaunchedEffect(viewModel) {
            viewModel.activityCreated.collect { activityId ->
                // Rafraîchir les achievements après création
                // Le backend devrait créer le badge "Premier Hôte" automatiquement
                // refreshAllAfterActivityCompletion() attend déjà 1.5 secondes avant de rafraîchir
                achievementsVM.refreshAllAfterActivityCompletion()
            }
        }
        
        // Collecter les challenges complétés dans un state
        var currentChallengeCompleted by remember { mutableStateOf<com.example.damandroid.presentation.achievements.viewmodel.ChallengeCompletedEvent?>(null) }
        
        LaunchedEffect(achievementsVM) {
            achievementsVM.challengeCompletedEvent.collect { event ->
                // Vérifier dans le store si le challenge a déjà été affiché
                if (!notificationStore.isChallengeShown(event.challengeId)) {
                    currentChallengeCompleted = event
                    notificationStore.markChallengeAsShown(event.challengeId)
                }
            }
        }
        
        // Afficher les notifications de badges débloqués (uniquement si pas déjà affichées)
        // Selon le guide : Détecter et afficher automatiquement les nouveaux badges débloqués
        newBadges.forEach { badge ->
            if (!notificationStore.isBadgeShown(badge.id)) {
                BadgeUnlockedDialog(badge = badge) {
                    // Clear après affichage pour éviter les doublons
                    achievementsVM.clearNewBadge(badge.id)
                    notificationStore.markBadgeAsShown(badge.id)
                }
            }
        }
        
        // Afficher la notification de montée de niveau (uniquement si pas déjà affichée)
        levelUp?.let { event ->
            if (!notificationStore.isLevelUpShown(event.newLevel)) {
                LevelUpDialog(event = event) {
                    achievementsVM.clearLevelUpEvent()
                    notificationStore.markLevelUpAsShown(event.newLevel)
                }
            }
        }
        
        // Afficher les notifications de challenges complétés
        currentChallengeCompleted?.let { event ->
            ChallengeCompletedDialog(event = event) {
                currentChallengeCompleted = null
            }
        }
    }
    
    // Naviguer vers home quand la création réussit
    // Suivre les succès déjà traités pour éviter les déclenchements multiples
    var handledSuccessId by remember { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // S'assurer que le formulaire est réinitialisé et que handledSuccessId est prêt
    LaunchedEffect(Unit) {
        // Attendre un peu pour s'assurer que resetForm() a été exécuté
        kotlinx.coroutines.delay(50)
        // Réinitialiser handledSuccessId si success est null après la réinitialisation
        if (uiState.success == null) {
            handledSuccessId = null
        } else {
            // Si success n'est pas null, c'est un ancien succès qu'on ignore
            handledSuccessId = uiState.success?.activityId
        }
        isInitialized = true
    }
    
    // Naviguer vers home quand la création réussit (seulement après l'initialisation)
    LaunchedEffect(uiState.success, isInitialized) {
        // Attendre que l'initialisation soit terminée
        if (!isInitialized) return@LaunchedEffect
        
        val currentSuccess = uiState.success
        if (currentSuccess != null) {
            val successId = currentSuccess.activityId
            // Ne déclencher que si c'est un nouveau succès (pas celui qu'on a déjà traité)
            if (successId != handledSuccessId) {
                // Nouveau succès détecté - marquer comme traité et déclencher onSuccess
                handledSuccessId = successId
                onSuccess()
                // Réinitialiser le succès après un court délai pour permettre la navigation
                kotlinx.coroutines.delay(150)
                viewModel.onSuccessDialogDismissed()
            }
        } else {
            // Réinitialiser handledSuccessId quand success redevient null
            handledSuccessId = null
        }
    }
    
    CreateActivityScreen(
        state = uiState,
        onBack = onBack,
        onSportSelected = viewModel::onSportSelected,
        onTitleChange = viewModel::onTitleChanged,
        onDescriptionChange = viewModel::onDescriptionChanged,
        onLocationChange = viewModel::onLocationChanged,
        onDateChange = viewModel::onDateChanged,
        onTimeChange = viewModel::onTimeChanged,
        onParticipantsChange = viewModel::onParticipantsChanged,
        onSkillLevelChange = viewModel::onLevelSelected,
        onVisibilityChange = viewModel::onVisibilitySelected,
        onSubmit = viewModel::onSubmit,
        modifier = modifier
    )
}

@Composable
fun CreateActivityScreen(
    state: CreateActivityUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onParticipantsChange: (Int) -> Unit,
    onSportSelected: (SportCategory) -> Unit,
    onSkillLevelChange: (SkillLevel) -> Unit,
    onVisibilityChange: (ActivityVisibility) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.isLoading -> LoadingState(modifier)
        state.error != null -> ErrorState(state.error, modifier)
        else -> ContentState(state, onBack, onTitleChange, onDescriptionChange, onLocationChange, onDateChange, onTimeChange, onParticipantsChange, onSportSelected, onSkillLevelChange, onVisibilityChange, onSubmit, modifier)
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ContentState(
    state: CreateActivityUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onParticipantsChange: (Int) -> Unit,
    onSportSelected: (SportCategory) -> Unit,
    onSkillLevelChange: (SkillLevel) -> Unit,
    onVisibilityChange: (ActivityVisibility) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = rememberCreatePalette(rememberAppThemeColors(LocalThemeController.current.isDarkMode))
    var sportExpanded by remember { mutableStateOf(false) }
    var levelExpanded by remember { mutableStateOf(false) }
    var visibilityExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(onBack = onBack, palette = palette)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SportSelector(
                    sportExpanded,
                    onExpandedChange = { sportExpanded = it },
                    selectedSport = state.selectedSport,
                    sports = state.sportCategories,
                    palette = palette,
                    onSportSelected = {
                        sportExpanded = false
                        onSportSelected(it)
                    }
                )

                ActivityTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = "Activity Title *",
                    placeholder = "e.g., Morning run at the park",
                    palette = palette
                )

                ActivityTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = "Description",
                    placeholder = "Tell participants what to expect...",
                    palette = palette,
                    minLines = 4
                )

                LocationFieldWithMapPicker(
                    value = state.location,
                    onValueChange = onLocationChange,
                    label = "Location *",
                    placeholder = "Enter address or venue name",
                    palette = palette
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    DatePickerField(
                        value = state.date,
                        onValueChange = onDateChange,
                        label = "Date *",
                        placeholder = "Select date",
                        palette = palette,
                        modifier = Modifier.weight(1f)
                    )
                    TimePickerField(
                        value = state.time,
                        onValueChange = onTimeChange,
                        label = "Time *",
                        placeholder = "Select time",
                        palette = palette,
                        modifier = Modifier.weight(1f)
                    )
                }

                ParticipantsSection(participants = state.participants, onParticipantsChange = onParticipantsChange, palette = palette)

                LevelSelector(
                    expanded = levelExpanded,
                    onExpandedChange = { levelExpanded = !levelExpanded },
                    selected = state.level,
                    onSelected = {
                        levelExpanded = false
                        onSkillLevelChange(it)
                    },
                    palette = palette
                )

                VisibilitySelector(
                    expanded = visibilityExpanded,
                    onExpandedChange = { visibilityExpanded = !visibilityExpanded },
                    selected = state.visibility,
                    onSelected = {
                        visibilityExpanded = false
                        onVisibilityChange(it)
                    },
                    palette = palette
                )
            }

            SubmitBar(onSubmit = onSubmit, palette = palette)
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit, palette: CreatePalette) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = palette.glassSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, palette.glassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = palette.primaryText)
            }
            Text(text = "Create Activity", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = palette.primaryText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportSelector(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedSport: SportCategory?,
    sports: List<SportCategory>,
    palette: CreatePalette,
    onSportSelected: (SportCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel("Sport Type *", palette)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
            OutlinedTextField(
                value = selectedSport?.name.orEmpty(),
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select a sport", color = palette.mutedText) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(18.dp),
                colors = textFieldColors(palette)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                sports.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = category.icon, fontSize = 18.sp)
                                Text(text = category.name, color = palette.primaryText)
                            }
                        },
                        onClick = { onSportSelected(category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    palette: CreatePalette,
    minLines: Int = 1,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        FieldLabel(label, palette)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            placeholder = { Text(placeholder, color = palette.mutedText) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            minLines = minLines,
            maxLines = minLines * 2,
            leadingIcon = leadingIcon,
            colors = textFieldColors(palette)
        )
    }
}

@Composable
private fun LocationFieldWithMapPicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    palette: CreatePalette
) {
    var showMapPicker by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label, palette)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = palette.mutedText) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = palette.mutedText) },
                colors = textFieldColors(palette)
            )
            
            // Pick from Map Button
            Button(
                onClick = { showMapPicker = true },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accentPurple
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "Pick from Map",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
    
    // Map Location Picker Dialog
    if (showMapPicker) {
        MapLocationPickerDialog(
            onDismiss = { showMapPicker = false },
            onLocationSelected = { address ->
                onValueChange(address)
                showMapPicker = false
            },
            palette = palette
        )
    }
}

@Composable
private fun FieldLabel(text: String, palette: CreatePalette) {
    Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.secondaryText)
}

@Composable
private fun ParticipantsSection(participants: Int, onParticipantsChange: (Int) -> Unit, palette: CreatePalette) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel("Participants", palette)
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = palette.primaryText, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = participants.toString(), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = palette.primaryText)
            }
            Text(text = "Max 20", fontSize = 12.sp, color = palette.mutedText)
        }
        Slider(
            value = participants.toFloat(),
            onValueChange = { onParticipantsChange(it.toInt()) },
            valueRange = 2f..20f,
            steps = 18,
            colors = SliderDefaults.colors(
                thumbColor = palette.accentPurple,
                activeTrackColor = palette.accentPurple,
                inactiveTrackColor = palette.sliderTrack
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelSelector(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selected: SkillLevel?,
    onSelected: (SkillLevel) -> Unit,
    palette: CreatePalette
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel("Skill Level", palette)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { onExpandedChange(!expanded) }) {
            OutlinedTextField(
                value = selected?.displayName.orEmpty(),
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select level", color = palette.mutedText) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(18.dp),
                colors = textFieldColors(palette)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                SkillLevel.values().forEach { level ->
                    DropdownMenuItem(text = { Text(level.displayName, color = palette.primaryText) }, onClick = { onSelected(level) })
                }
            }
        }
    }
}

private val SkillLevel.displayName: String
    get() = when (this) {
        SkillLevel.BEGINNER -> "Beginner"
        SkillLevel.INTERMEDIATE -> "Intermediate"
        SkillLevel.ADVANCED -> "Advanced"
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VisibilitySelector(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selected: ActivityVisibility,
    onSelected: (ActivityVisibility) -> Unit,
    palette: CreatePalette
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel("Visibility", palette)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { onExpandedChange(!expanded) }) {
            OutlinedTextField(
                value = selected.label,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select visibility", color = palette.mutedText) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(18.dp),
                colors = textFieldColors(palette)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                ActivityVisibility.values().forEach { visibility ->
                    DropdownMenuItem(text = { Text(visibility.label, color = palette.primaryText) }, onClick = { onSelected(visibility) })
                }
            }
        }
    }
}

private val ActivityVisibility.label: String
    get() = when (this) {
        ActivityVisibility.PUBLIC -> "Public"
        ActivityVisibility.FRIENDS -> "Friends"
    }

@Composable
private fun SubmitBar(onSubmit: () -> Unit, palette: CreatePalette) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        shape = RoundedCornerShape(20.dp),
        color = palette.glassSurface,
        border = BorderStroke(1.dp, palette.glassBorder)
    ) {
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = palette.accentPurple)
        ) {
            Text(text = "Create Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = palette.iconOnAccent)
        }
    }
}


@Composable
private fun textFieldColors(palette: CreatePalette) = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = palette.glassSurface,
    unfocusedContainerColor = palette.glassSurface,
    focusedBorderColor = palette.accentPurple,
    unfocusedBorderColor = palette.glassBorder,
    unfocusedTextColor = palette.primaryText,
    focusedTextColor = palette.primaryText,
    cursorColor = palette.primaryText,
    unfocusedLabelColor = palette.secondaryText,
    focusedLabelColor = palette.primaryText
)

// region Palette

data class CreatePalette(
    val background: Brush,
    val glassSurface: Color,
    val glassBorder: Color,
    val cardSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val iconOnAccent: Color,
    val accentPurple: Color,
    val accentBlue: Color,
    val sliderTrack: Color
)

@Composable
private fun rememberCreatePalette(colors: AppThemeColors): CreatePalette {
    return CreatePalette(
        background = colors.backgroundGradient,
        glassSurface = colors.glassSurface,
        glassBorder = colors.glassBorder,
        cardSurface = colors.glassSurface.copy(alpha = 0.95f),
        primaryText = colors.primaryText,
        secondaryText = colors.secondaryText,
        mutedText = colors.mutedText,
        iconOnAccent = colors.iconOnAccent,
        accentPurple = colors.accentPurple,
        accentBlue = colors.accentBlue,
        sliderTrack = colors.subtleSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    palette: CreatePalette,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Parser la date actuelle si elle existe (mémorisé pour éviter les recalculs)
    val currentDate = remember(value) {
        try {
            if (value.isNotBlank()) {
                LocalDate.parse(value, DateTimeFormatter.ISO_DATE)
            } else {
                LocalDate.now()
            }
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
    
    // Formater la date pour l'affichage (format lisible) - mémorisé
    val displayValue = remember(value) {
        if (value.isNotBlank()) {
            try {
                val date = LocalDate.parse(value, DateTimeFormatter.ISO_DATE)
                date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
            } catch (e: Exception) {
                value
            }
        } else {
            ""
        }
    }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = remember(currentDate) {
            currentDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
    )
    
    ActivityTextField(
        value = displayValue,
        onValueChange = { },
        label = label,
        placeholder = placeholder,
        palette = palette,
        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = palette.mutedText) },
        modifier = modifier.clickable { showDatePicker = true },
        enabled = false
    )
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onValueChange(selectedDate.format(DateTimeFormatter.ISO_DATE))
                        }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentPurple)
                ) {
                    Text("OK", color = palette.iconOnAccent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = palette.primaryText)
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    palette: CreatePalette,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Parser l'heure actuelle si elle existe (mémorisé pour éviter les recalculs)
    val currentTime = remember(value) {
        try {
            if (value.isNotBlank()) {
                LocalTime.parse(value, DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                LocalTime.now()
            }
        } catch (e: Exception) {
            LocalTime.now()
        }
    }
    
    val timePickerState = rememberTimePickerState(
        initialHour = remember(currentTime) { currentTime.hour },
        initialMinute = remember(currentTime) { currentTime.minute },
        is24Hour = true
    )
    
    ActivityTextField(
        value = value,
        onValueChange = { },
        label = label,
        placeholder = placeholder,
        palette = palette,
        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = palette.mutedText) },
        modifier = modifier.clickable { showTimePicker = true },
        enabled = false
    )
    
    if (showTimePicker) {
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        onValueChange(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")))
                        showTimePicker = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accentPurple)
                ) {
                    Text("OK", color = palette.iconOnAccent)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = palette.primaryText)
                ) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

// endregion

// region Map Location Picker

@Composable
private fun MapLocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit,
    palette: CreatePalette
) {
    val context = LocalContext.current
    val geocodingService = remember { com.example.damandroid.location.GeocodingService() }
    val scope = rememberCoroutineScope()
    
    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var isLoadingAddress by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    
    // Default location (Tunis)
    val defaultLat = 36.8065
    val defaultLng = 10.1815
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = palette.glassSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = palette.primaryText
                            )
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Pick Location on Map",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.primaryText
                            )
                            Text(
                                text = "Tap anywhere on the map to select your location",
                                fontSize = 13.sp,
                                color = palette.secondaryText,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(36.dp)) // Balance the close button
                    }
                    
                    // Map
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                    ) {
                        // Configure OSMDroid
                        LaunchedEffect(Unit) {
                            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE))
                            Configuration.getInstance().userAgentValue = "DamAndroid/1.0"
                        }
                        
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    mapViewRef = this
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    minZoomLevel = 3.0
                                    maxZoomLevel = 19.0
                                    
                                    val startPoint = GeoPoint(defaultLat, defaultLng)
                                    controller.setCenter(startPoint)
                                    controller.setZoom(13.0)
                                    
                                    // Hide built-in zoom controls
                                    post {
                                        for (i in 0 until childCount) {
                                            val child = getChildAt(i)
                                            if (child.javaClass.simpleName.contains("Zoom", ignoreCase = true)) {
                                                child.visibility = android.view.View.GONE
                                            }
                                        }
                                    }
                                    
                                    // Add tap listener using MapEventsReceiver
                                    val mapEventsReceiver = object : MapEventsReceiver {
                                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                            p?.let { geoPoint ->
                                                selectedLat = geoPoint.latitude
                                                selectedLng = geoPoint.longitude
                                                selectedAddress = null
                                                isLoadingAddress = true
                                                
                                                // Reverse geocode
                                                scope.launch {
                                                    val address = geocodingService.reverseGeocode(geoPoint.latitude, geoPoint.longitude)
                                                    selectedAddress = address ?: "Unknown Location"
                                                    isLoadingAddress = false
                                                }
                                                
                                                // Update marker
                                                overlays.removeAll { it is Marker }
                                                val marker = Marker(this@apply).apply {
                                                    position = geoPoint
                                                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                    icon = createLocationPinIcon(context)
                                                }
                                                overlays.add(marker)
                                                invalidate()
                                            }
                                            return true
                                        }
                                        
                                        override fun longPressHelper(p: GeoPoint?): Boolean {
                                            return false
                                        }
                                    }
                                    val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
                                    overlays.add(mapEventsOverlay)
                                    
                                    // Add initial marker
                                    val initialMarker = Marker(this).apply {
                                        position = startPoint
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        icon = createLocationPinIcon(context)
                                    }
                                    overlays.add(initialMarker)
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20.dp))
                        )
                        
                        // Zoom controls
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { mapViewRef?.controller?.zoomIn() },
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 2.dp,
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Zoom In",
                                        tint = Color(0xFF424242),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { mapViewRef?.controller?.zoomOut() },
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 2.dp,
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Zoom Out",
                                        tint = Color(0xFF424242),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Selected Location Card
                    if (selectedLat != null && selectedLng != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.cardSurface),
                            border = BorderStroke(1.dp, palette.glassBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9800).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Selected Location",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = palette.secondaryText
                                    )
                                    
                                    if (isLoadingAddress) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = palette.accentPurple
                                            )
                                            Text(
                                                text = "Fetching address...",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = palette.primaryText
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = selectedAddress ?: "Unknown Location",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = palette.primaryText,
                                            maxLines = 2
                                        )
                                    }
                                    
                                    Text(
                                        text = "Coordinates: ${String.format("%.4f, %.4f", selectedLat!!, selectedLng!!)}",
                                        fontSize = 12.sp,
                                        color = palette.secondaryText
                                    )
                                }
                            }
                        }
                    }
                    
                    // Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.glassSurface
                            ),
                            border = BorderStroke(1.dp, palette.glassBorder)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.primaryText
                            )
                        }
                        
                        Button(
                            onClick = {
                                if (selectedAddress != null) {
                                    onLocationSelected(selectedAddress!!)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800)
                            ),
                            enabled = selectedAddress != null && !isLoadingAddress
                        ) {
                            Text(
                                text = "Confirm Location",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun createLocationPinIcon(context: android.content.Context): android.graphics.drawable.Drawable {
    val size = 60
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    val centerX = size / 2f
    val centerY = size / 2f
    
    // Orange pin color
    val pinPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#FF9800")
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    
    // Draw pin shape (teardrop)
    val path = android.graphics.Path().apply {
        moveTo(centerX, centerY - size / 2f + 10f)
        lineTo(centerX - size / 3f, centerY - 5f)
        lineTo(centerX - size / 4f, centerY + 5f)
        arcTo(
            centerX - size / 4f, centerY + 5f,
            centerX + size / 4f, centerY + 15f,
            -90f, 180f, false
        )
        lineTo(centerX + size / 3f, centerY - 5f)
        close()
    }
    canvas.drawPath(path, pinPaint)
    
    // Draw white circle in center
    val circlePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }
    canvas.drawCircle(centerX, centerY - 2f, 8f, circlePaint)
    
    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}

// endregion

