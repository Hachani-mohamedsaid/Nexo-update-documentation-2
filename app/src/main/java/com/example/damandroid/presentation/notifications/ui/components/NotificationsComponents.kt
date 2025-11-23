package com.example.damandroid.presentation.notifications.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.damandroid.domain.model.NotificationItem
import com.example.damandroid.ui.theme.AppThemeColors
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NotificationsContent(
    notifications: List<NotificationItem>,
    unreadCount: Int,
    onBack: (() -> Unit)?,
    onNotificationRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onLikeBack: ((String) -> Unit)? = null,
    onStartChat: ((String, String) -> Unit)? = null, // userId, userName
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val colors = rememberAppThemeColors(themeController.isDarkMode)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (colors.isDark) colors.backgroundGradient else Brush.linearGradient(
                    listOf(
                        Color(0xFFF5F3FF),
                        Color(0xFFFDF4FF),
                        Color(0xFFF0F4F8)
                    )
                )
            )
    ) {
        FloatingNotificationOrbs(colors)

        Column(modifier = Modifier.fillMaxSize()) {
            NotificationsHeader(onBack = onBack, colors = colors)

            if (notifications.isEmpty()) {
                EmptyState(colors = colors, modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        NotificationCard(
                            item = item,
                            colors = colors,
                            onAction = { onNotificationRead(item.id) },
                            onDismiss = { onNotificationRead(item.id) },
                            onLikeBack = onLikeBack,
                            onStartChat = onStartChat
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsHeader(
    onBack: (() -> Unit)?,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.primaryText)
                    }
                }
            }
            Text(
                text = "Notifications",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.primaryText,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp)) // Balance the back button width
        }
    }
}

@Composable
private fun NotificationCard(
    item: NotificationItem,
    colors: AppThemeColors,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    onLikeBack: ((String) -> Unit)? = null,
    onStartChat: ((String, String) -> Unit)? = null
) {
    val icon = remember(item) { item.iconEmoji() }
    val actionText = remember(item) { item.actionLabel() }
    val timestamp = remember(item) { item.formattedTimestamp() }
    
    // Pour les notifications de likes
    val likeNotification = if (item is NotificationItem.LikeNotification) item else null

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            colors.accentPurple.copy(alpha = 0.18f),
                            colors.accentPink.copy(alpha = 0.18f)
                        )
                    )
                )
                .blur(18.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.glassSurface),
            border = BorderStroke(2.dp, colors.glassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                            .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Afficher l'icône du badge si c'est une URL, sinon utiliser l'emoji
                        when (item) {
                            is NotificationItem.AchievementUnlocked -> {
                                val badgeIcon = item.badgeIcon
                                if (badgeIcon.isNotEmpty() && 
                                    (badgeIcon.startsWith("http://") || badgeIcon.startsWith("https://"))) {
                                    // C'est une URL d'image
                                    AsyncImage(
                                        model = badgeIcon,
                                        contentDescription = item.badgeName,
                                        modifier = Modifier.size(36.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    // C'est un emoji ou pas d'icône
                                    Text(
                                        text = badgeIcon.ifEmpty { "🏆" },
                                        fontSize = 22.sp
                                    )
                                }
                            }
                            else -> {
                                // Pour les autres types de notifications, utiliser l'emoji
                                Text(text = icon, fontSize = 22.sp)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = item.message, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.primaryText, maxLines = 2)
                        Text(text = timestamp, fontSize = 12.sp, color = colors.mutedText)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = colors.mutedText)
                    }
                }

                // Afficher les boutons selon le type de notification
                when {
                    likeNotification != null -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (likeNotification.isMatch) {
                                // Si c'est un match, afficher "Welcome" et "Chat"
                                Button(
                                    onClick = {
                                        onStartChat?.invoke(
                                            likeNotification.fromUserId,
                                            likeNotification.fromUserName
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPurple),
                                    contentPadding = PaddingValues(horizontal = 20.dp)
                                ) {
                                    Text(
                                        text = "Chat",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.iconOnAccent
                                    )
                                }
                                Button(
                                    onClick = onAction,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPink),
                                    contentPadding = PaddingValues(horizontal = 20.dp)
                                ) {
                                    Text(
                                        text = "Welcome",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            } else {
                                // Si ce n'est pas encore un match, afficher "Like Back"
                                Button(
                                    onClick = {
                                        onLikeBack?.invoke(likeNotification.fromUserId)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPurple),
                                    contentPadding = PaddingValues(horizontal = 20.dp)
                                ) {
                                    Text(
                                        text = "Like Back",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.iconOnAccent
                                    )
                                }
                            }
                        }
                    }
                    actionText != null -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(
                                onClick = onAction,
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accentPurple),
                                contentPadding = PaddingValues(horizontal = 20.dp)
                            ) {
                                Box(Modifier.height(16.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = actionText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.iconOnAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(colors: AppThemeColors, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = colors.glassSurface,
            border = BorderStroke(2.dp, colors.glassBorder)
        ) {
            Text(text = "🔔", fontSize = 46.sp, modifier = Modifier.padding(32.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "No notifications", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.primaryText)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "We'll notify you when something happens", fontSize = 14.sp, color = colors.mutedText, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FloatingNotificationOrbs(colors: AppThemeColors) {
    val transition = rememberInfiniteTransition(label = "notification-orbs")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb-offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(colors.accentPurple.copy(alpha = 0.22f))
                .blur(60.dp)
                .offset(x = (-80).dp + offset.dp, y = (-60).dp)
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(colors.accentPink.copy(alpha = 0.20f))
                .blur(50.dp)
                .offset(x = 140.dp - offset.dp, y = 120.dp)
        )
    }
}

private fun NotificationItem.iconEmoji(): String = when (this) {
    is NotificationItem.SessionInvite -> "✨"
    is NotificationItem.ActivityReminder -> "🏃"
    is NotificationItem.AchievementUnlocked -> this.badgeIcon.ifEmpty { "🏆" } // Utiliser badgeIcon du modèle, ou emoji par défaut si vide
    is NotificationItem.XpEarned -> "⭐"
    is NotificationItem.LevelUp -> "⬆️"
    is NotificationItem.ChallengeCompleted -> "🎯"
    is NotificationItem.SystemMessage -> "ℹ️"
    is NotificationItem.LikeNotification -> if (this.isMatch) "💕" else "❤️"
}

private fun NotificationItem.actionLabel(): String? = when (this) {
    is NotificationItem.SessionInvite -> "View"
    is NotificationItem.ActivityReminder -> "Join"
    is NotificationItem.AchievementUnlocked -> "View"
    is NotificationItem.XpEarned -> null
    is NotificationItem.LevelUp -> null
    is NotificationItem.ChallengeCompleted -> null
    is NotificationItem.SystemMessage -> null
    is NotificationItem.LikeNotification -> null // Les boutons sont gérés directement dans le composant
}

private fun NotificationItem.formattedTimestamp(): String {
    val zonedDateTime = timestamp.atZoneSameInstant(ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.getDefault())
    return zonedDateTime.format(formatter)
}

