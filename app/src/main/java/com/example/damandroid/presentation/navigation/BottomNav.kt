package com.example.damandroid

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors

data class TabItem(
    val id: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun BottomNav(
    activeTab: String,
    onTabChange: (String) -> Unit,
    onAICoachClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val themeController = LocalThemeController.current
    val appColors = rememberAppThemeColors(themeController.isDarkMode)
    val activeColor = appColors.accentPurple
    val inactiveColor = appColors.mutedText
    val labelActive = appColors.accentPurple
    val labelInactive = appColors.mutedText

    val leftTabs = listOf(
        TabItem("home", Icons.Default.Home, "Home"),
        TabItem("map", Icons.Default.CalendarToday, "Sessions")
    )

    val rightTabs = listOf(
        TabItem("chat", Icons.Default.Chat, "Chat"),
        TabItem("profile", Icons.Default.Dashboard, "Dashboard")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .zIndex(50f)
    ) {
        // Glass effect backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    appColors.glassSurface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .border(
                    width = 2.dp,
                    color = appColors.glassBorder,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left tabs
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                leftTabs.forEach { tab ->
                    TabButton(
                        tab = tab,
                        isActive = activeTab == tab.id,
                        onClick = { onTabChange(tab.id) },
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                        labelActive = labelActive,
                        labelInactive = labelInactive,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Center AI Coach Button
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-32).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AICoachButton(
                    onClick = { onAICoachClick?.invoke() }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI Coach",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = appColors.primaryText,
                    textAlign = TextAlign.Center
                )
            }

            // Right tabs
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                rightTabs.forEach { tab ->
                    TabButton(
                        tab = tab,
                        isActive = activeTab == tab.id,
                        onClick = { onTabChange(tab.id) },
                        activeColor = activeColor,
                        inactiveColor = inactiveColor,
                        labelActive = labelActive,
                        labelInactive = labelInactive,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    tab: TabItem,
    isActive: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    labelActive: Color,
    labelInactive: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            modifier = Modifier.size(26.dp),
            tint = if (isActive) activeColor else inactiveColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = tab.label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
            color = if (isActive) labelActive else labelInactive,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AICoachButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-alpha"
    )

    val themeController = LocalThemeController.current
    val appColors = rememberAppThemeColors(themeController.isDarkMode)
    val accentGradient = Brush.linearGradient(
        colors = listOf(appColors.accentPurple, appColors.accentPink)
    )
    val outerGlow = Brush.linearGradient(
        colors = listOf(
            appColors.accentPurple.copy(alpha = 0.4f),
            appColors.accentPink.copy(alpha = 0.35f)
        )
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Floating shadow
        Box(
            modifier = Modifier
                .offset(y = 32.dp)
                .width(48.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(50))
                .background(appColors.accentPurple.copy(alpha = 0.25f))
                .blur(8.dp)
        )

        // Outer glow
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(outerGlow)
                .blur(16.dp)
        )

        // Pulse ring
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(appColors.accentPurple.copy(alpha = pulseAlpha))
        )

        // Main button
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(accentGradient)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            // Shine effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                    .clip(RoundedCornerShape(16.dp))
            )

            // Icon
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Coach",
                modifier = Modifier.size(28.dp),
                tint = Color.White
            )
        }
    }
}

