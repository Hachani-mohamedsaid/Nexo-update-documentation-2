package com.example.damandroid.presentation.achievements.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.AchievementBadge
import com.example.damandroid.presentation.achievements.viewmodel.LevelUpEvent
import com.example.damandroid.presentation.achievements.viewmodel.ChallengeCompletedEvent
import kotlinx.coroutines.delay

/**
 * Dialogue pour afficher un badge débloqué
 */
@Composable
fun BadgeUnlockedDialog(
    badge: AchievementBadge,
    onDismiss: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    var scale by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "badge_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "badge_alpha"
    )

    LaunchedEffect(Unit) {
        scale = 1f
        alpha = 1f
        delay(3000) // Afficher pendant 3 secondes
        scale = 0f
        alpha = 0f
        delay(300)
        showDialog = false
        onDismiss()
    }

    if (showDialog) {
        Dialog(onDismissRequest = { 
            showDialog = false
            onDismiss()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(animatedScale)
                    .alpha(animatedAlpha),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon du badge
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = getRarityColor(badge.rarity).copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (badge.iconUrl != null && badge.iconUrl.isNotEmpty()) {
                            AsyncImage(
                                model = badge.iconUrl,
                                contentDescription = badge.title,
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = badge.icon,
                                fontSize = 48.sp
                            )
                        }
                    }

                    // Titre
                    Text(
                        text = "🎉 Nouveau Badge Débloqué !",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Nom du badge
                    val rarityColor = getRarityColor(badge.rarity)
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = rarityColor
                    )

                    // Description
                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Rareté
                    Surface(
                        color = rarityColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = badge.rarity.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = rarityColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Bouton de fermeture
                    Button(
                        onClick = {
                            showDialog = false
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = rarityColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Génial !")
                    }
                }
            }
        }
    }
}

/**
 * Dialogue pour afficher une montée de niveau
 */
@Composable
fun LevelUpDialog(
    event: LevelUpEvent,
    onDismiss: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    var scale by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "level_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "level_alpha"
    )

    LaunchedEffect(Unit) {
        scale = 1f
        alpha = 1f
        delay(3000) // Afficher pendant 3 secondes
        scale = 0f
        alpha = 0f
        delay(300)
        showDialog = false
        onDismiss()
    }

    if (showDialog) {
        Dialog(onDismissRequest = { 
            showDialog = false
            onDismiss()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(animatedScale)
                    .alpha(animatedAlpha),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon de montée de niveau
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    // Titre
                    Text(
                        text = "Niveau Supérieur ! ⬆️",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Niveaux
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Niveau ${event.oldLevel}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "Niveau ${event.newLevel}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // XP total
                    Text(
                        text = "Vous avez maintenant ${event.totalXp} XP !",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Bouton de fermeture
                    Button(
                        onClick = {
                            showDialog = false
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continuer")
                    }
                }
            }
        }
    }
}

/**
 * Dialogue pour afficher un challenge complété
 */
@Composable
fun ChallengeCompletedDialog(
    event: ChallengeCompletedEvent,
    onDismiss: () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    var scale by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "challenge_scale"
    )

    val animatedAlpha by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "challenge_alpha"
    )

    LaunchedEffect(Unit) {
        scale = 1f
        alpha = 1f
        delay(3000) // Afficher pendant 3 secondes
        scale = 0f
        alpha = 0f
        delay(300)
        showDialog = false
        onDismiss()
    }

    if (showDialog) {
        Dialog(onDismissRequest = { 
            showDialog = false
            onDismiss()
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(animatedScale)
                    .alpha(animatedAlpha),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Icon de challenge complété
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color(0xFF4CAF50)
                        )
                    }

                    // Titre
                    Text(
                        text = "🎯 Défi Complété !",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )

                    // Nom du challenge
                    Text(
                        text = event.challengeName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    // Message
                    Text(
                        text = "Félicitations ! Vous avez complété ce défi et gagné ${event.xpReward} XP !",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )

                    // Badge XP
                    Surface(
                        color = Color(0xFFFF9800).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+${event.xpReward} XP",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }

                    // Bouton de fermeture
                    Button(
                        onClick = {
                            showDialog = false
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Génial !")
                    }
                }
            }
        }
    }
}

/**
 * Obtient la couleur selon la rareté du badge
 */
@Composable
/**
 * Fonction utilitaire pour obtenir la couleur d'un badge selon sa rareté
 * Utilise les couleurs standard du guide :
 * - Common (Commun) : Vert (#4CAF50)
 * - Uncommon (Peu commun) : Bleu (#2196F3)
 * - Rare : Violet (#9C27B0)
 * - Epic (Épique) : Orange (#FF9800)
 * - Legendary (Légendaire) : Or (#FFD700)
 */
fun getRarityColor(rarity: String): Color {
    return when (rarity.lowercase()) {
        "common" -> Color(0xFF4CAF50) // Vert - Couleur corrigée selon le guide
        "uncommon" -> Color(0xFF2196F3) // Bleu
        "rare" -> Color(0xFF9C27B0) // Violet
        "epic" -> Color(0xFFFF9800) // Orange
        "legendary" -> Color(0xFFFFD700) // Or
        else -> MaterialTheme.colorScheme.primary
    }
}

