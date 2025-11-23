package com.example.damandroid.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class AppThemeColors(
    val isDark: Boolean,
    val backgroundGradient: Brush,
    val altBackgroundGradient: Brush,
    val glassSurface: Color,
    val glassBorder: Color,
    val cardSurface: Color,
    val cardBorder: Color,
    val subtleSurface: Color,
    val subtleBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val iconOnAccent: Color,
    val accentPurple: Color,
    val accentPink: Color,
    val accentBlue: Color,
    val accentTeal: Color,
    val accentGreen: Color,
    val accentGold: Color,
    val accentOrange: Color,
    val success: Color,
    val danger: Color,
    val warning: Color,
    val outline: Color,
)

private fun createAppThemeColors(isDark: Boolean): AppThemeColors {
    return if (isDark) {
        AppThemeColors(
            isDark = true,
            backgroundGradient = Brush.verticalGradient(
                listOf(
                    Color(0xFF0F172A),
                    Color(0xFF111827),
                    Color(0xFF1F2937)
                )
            ),
            altBackgroundGradient = Brush.verticalGradient(
                listOf(
                    Color(0xFF0B1120),
                    Color(0xFF151A2C),
                    Color(0xFF111827)
                )
            ),
            glassSurface = Color(0xFF1F2533).copy(alpha = 0.9f),
            glassBorder = Color(0xFF2F3545),
            cardSurface = Color(0xFF1F2937),
            cardBorder = Color(0xFF2D3748),
            subtleSurface = Color(0xFF262D3B),
            subtleBorder = Color(0xFF3A4254),
            primaryText = Color(0xFFF9FAFB),
            secondaryText = Color(0xFFD1D5DB),
            mutedText = Color(0xFF9CA3AF),
            iconOnAccent = Color.White,
            accentPurple = Color(0xFF8B5CF6),
            accentPink = Color(0xFFEC4899),
            accentBlue = Color(0xFF2563EB),
            accentTeal = Color(0xFF14B8A6),
            accentGreen = Color(0xFF22D3EE),
            accentGold = Color(0xFFFACC15),
            accentOrange = Color(0xFFF97316),
            success = Color(0xFF34D399),
            danger = Color(0xFFEF4444),
            warning = Color(0xFFFBBF24),
            outline = Color(0xFF374151)
        )
    } else {
        AppThemeColors(
            isDark = false,
            backgroundGradient = Brush.verticalGradient(
                listOf(
                    Color(0xFFE8D5F2),
                    Color(0xFFFFE4F1),
                    Color(0xFFE5E5F0)
                )
            ),
            altBackgroundGradient = Brush.verticalGradient(
                listOf(
                    Color(0xFFF5F3FF),
                    Color(0xFFFDF4FF),
                    Color(0xFFF0F4F8)
                )
            ),
            glassSurface = Color.White.copy(alpha = 0.72f),
            glassBorder = Color.White.copy(alpha = 0.82f),
            cardSurface = Color.White,
            cardBorder = Color(0xFFE5E7EB),
            subtleSurface = Color.White.copy(alpha = 0.6f),
            subtleBorder = Color.White.copy(alpha = 0.6f),
            primaryText = Color(0xFF111827),
            secondaryText = Color(0xFF374151),
            mutedText = Color(0xFF6B7280),
            iconOnAccent = Color.White,
            accentPurple = Color(0xFF8B5CF6),
            accentPink = Color(0xFFEC4899),
            accentBlue = Color(0xFF3B82F6),
            accentTeal = Color(0xFF14B8A6),
            accentGreen = Color(0xFF22C55E),
            accentGold = Color(0xFFFBBF24),
            accentOrange = Color(0xFFF97316),
            success = Color(0xFF22C55E),
            danger = Color(0xFFEF4444),
            warning = Color(0xFFF97316),
            outline = Color(0xFFD1D5DB)
        )
    }
}

@Composable
fun rememberAppThemeColors(isDarkMode: Boolean = LocalThemeController.current.isDarkMode): AppThemeColors {
    return remember(isDarkMode) { createAppThemeColors(isDarkMode) }
}

