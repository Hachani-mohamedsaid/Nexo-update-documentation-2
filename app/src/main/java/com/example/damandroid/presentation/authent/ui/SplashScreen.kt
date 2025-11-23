package com.example.damandroid

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.damandroid.ui.theme.AuthScreenPalette
import com.example.damandroid.ui.theme.LocalThemeController
import com.example.damandroid.ui.theme.rememberAppThemeColors
import com.example.damandroid.ui.theme.rememberAuthScreenPalette

@Composable
fun SplashScreen(
    onComplete: () -> Unit,
    logoResId: Int = R.drawable.ic_launcher_foreground,
) {
    val themeController = LocalThemeController.current
    val appTheme = rememberAppThemeColors(themeController.isDarkMode)
    val palette = rememberAuthScreenPalette(appTheme)
    val orbColors = palette.orbColors

    LaunchedEffect(Unit) {
        delay(3500)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Floating orbs for depth - Neon Electric Accents
        Orb(
            size = 128.dp,
            color = orbColors.getOrElse(0) { Color(0xFF8B5CF6).copy(alpha = 0.30f) },
            top = 80.dp,
            start = 40.dp,
            pulseDurationMs = 1600,
        )

        Orb(
            size = 160.dp,
            color = orbColors.getOrElse(1) { Color(0xFFEC4899).copy(alpha = 0.35f) },
            bottom = 160.dp,
            end = 40.dp,
            pulseDurationMs = 1600,
            startDelayMs = 1000,
        )

        Orb(
            size = 96.dp,
            color = orbColors.getOrElse(2) { Color(0xFF0066FF).copy(alpha = 0.30f) },
            center = true,
            pulseDurationMs = 1600,
            startDelayMs = 2000,
        )

        Orb(
            size = 80.dp,
            color = orbColors.getOrElse(3) { Color(0xFF2ECC71).copy(alpha = 0.25f) },
            top = 120.dp,
            end = 80.dp,
            pulseDurationMs = 1600,
            startDelayMs = 3000,
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated logo with up and down movement (only the image, not the frame)
            val infiniteTransition = rememberInfiniteTransition(label = "logo-bounce")
            val logoOffsetY by infiniteTransition.animateFloat(
                initialValue = -15f,
                targetValue = 15f,
                animationSpec = InfiniteRepeatableSpec(
                    animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "logo-float"
            )
            
            // Logo with Crystal Glass Effect
            Box {
                // Outer Shimmer Effect
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = orbColors.map { it.copy(alpha = 0.2f) }
                            )
                        )
                        .blur(32.dp)
                        .scale(1.1f)
                )

                // Crystal Glass Container
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(24.dp))
                            .background(palette.glassSurface)
                            .border(width = 2.dp, color = palette.glassBorder, shape = RoundedCornerShape(24.dp))
                ) {
                    // Outer glow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = orbColors.map { it.copy(alpha = 0.35f) }
                                )
                            )
                            .blur(16.dp)
                            .offset(x = (-4).dp, y = (-4).dp)
                    )

                    // Top highlight - creates glass shine effect
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(palette.highlightGradient)
                    )

                    Image(
                        painter = painterResource(id = logoResId),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .offset(y = logoOffsetY.dp)
                            .size(96.dp)
                            .align(Alignment.Center),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "NEXO",
                color = palette.primaryText,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Connect. Play. Excel.",
                color = palette.mutedText,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            BouncingDots(palette)
        }
    }
}

@Composable
private fun Orb(
    size: Dp,
    color: Color,
    top: Dp? = null,
    start: Dp? = null,
    end: Dp? = null,
    bottom: Dp? = null,
    center: Boolean = false,
    pulseDurationMs: Int,
    startDelayMs: Int = 0,
) {
    val infinite = rememberInfiniteTransition(label = "orb")
    val scale by infinite.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(durationMillis = pulseDurationMs, easing = LinearEasing, delayMillis = startDelayMs),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb-scale"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        var mod = Modifier
            .size(size)
            .blur(48.dp)
            .background(color = color, shape = RoundedCornerShape(999.dp))

        mod = if (center) {
            mod.align(Alignment.Center)
        } else {
            val alignment = when {
                bottom != null && end != null -> Alignment.BottomEnd
                bottom != null -> Alignment.BottomStart
                end != null -> Alignment.TopEnd
                else -> Alignment.TopStart
            }
            mod.align(alignment)
        }

        if (top != null) mod = mod.offset(y = top)
        if (start != null) mod = mod.offset(x = start)
        if (end != null) mod = mod.offset(x = (-end))
        if (bottom != null) mod = mod.offset(y = (-bottom))

        Box(modifier = mod) {}
    }
}

@Composable
private fun BouncingDots(palette: AuthScreenPalette) {
    val infinite = rememberInfiniteTransition(label = "dots")
    val y1 by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(800, easing = FastOutSlowInEasing, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val y2 by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(800, easing = FastOutSlowInEasing, delayMillis = 100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val y3 by infinite.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(800, easing = FastOutSlowInEasing, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Dot(offsetY = y1, palette = palette)
        Dot(offsetY = y2, palette = palette)
        Dot(offsetY = y3, palette = palette)
    }
}

@Composable
private fun Dot(offsetY: Float, palette: AuthScreenPalette) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .offset(y = offsetY.dp)
            .clip(CircleShape)
            .background(palette.buttonText.copy(alpha = 0.8f))
            .border(width = 1.dp, color = palette.glassBorder, shape = CircleShape)
    )
}


