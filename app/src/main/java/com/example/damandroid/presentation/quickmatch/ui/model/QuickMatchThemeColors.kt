package com.example.damandroid.presentation.quickmatch.ui.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.damandroid.ui.theme.LocalThemeController

@Stable
data class QuickMatchThemeColors(
    val isDark: Boolean,
    val backgroundGradient: List<Color>,
    val orbGradients: List<List<Color>>,
    val cardGlowGradient: List<Color>,
    val translucentSurface: Color,
    val translucentBorder: Color,
    val headerIcon: Color,
    val headerTitle: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val chipSurface: Color,
    val chipBorder: Color,
    val buttonGlowGradient: List<Color>,
    val buttonFillGradient: List<Color>,
    val buttonBorder: Color,
    val buttonText: Color
)

@Composable
fun rememberQuickMatchThemeColors(): QuickMatchThemeColors {
    val isDark = LocalThemeController.current.isDarkMode
    return remember(isDark) {
        if (isDark) {
            QuickMatchThemeColors(
                isDark = true,
                backgroundGradient = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF1E293B),
                    Color(0xFF111827)
                ),
                orbGradients = listOf(
                    listOf(Color(0xFF3730A3).copy(alpha = 0.35f), Color(0xFF312E81).copy(alpha = 0.25f)),
                    listOf(Color(0xFFBE123C).copy(alpha = 0.4f), Color(0xFF9D174D).copy(alpha = 0.25f)),
                    listOf(Color(0xFF1E3A8A).copy(alpha = 0.4f), Color(0xFF1D4ED8).copy(alpha = 0.3f)),
                    listOf(Color(0xFFF59E0B).copy(alpha = 0.4f), Color(0xFFD97706).copy(alpha = 0.25f))
                ),
                cardGlowGradient = listOf(
                    Color(0xFF312E81).copy(alpha = 0.5f),
                    Color(0xFF1E293B).copy(alpha = 0.4f)
                ),
                translucentSurface = Color(0xFF1F2937),
                translucentBorder = Color(0xFF334155),
                headerIcon = Color(0xFFE2E8F0),
                headerTitle = Color(0xFFF8FAFC),
                primaryText = Color(0xFFE2E8F0),
                secondaryText = Color(0xFF94A3B8),
                chipSurface = Color(0xFF1E293B),
                chipBorder = Color(0xFF334155),
                buttonGlowGradient = listOf(
                    Color(0xFF3730A3).copy(alpha = 0.3f),
                    Color(0xFF9D174D).copy(alpha = 0.3f),
                    Color(0xFF1E1B4B).copy(alpha = 0.3f)
                ),
                buttonFillGradient = listOf(
                    Color(0xFF1F2937),
                    Color(0xFF111827)
                ),
                buttonBorder = Color(0xFF334155),
                buttonText = Color(0xFFE2E8F0)
            )
        } else {
            QuickMatchThemeColors(
                isDark = false,
                backgroundGradient = listOf(
                    Color(0xFFF5F3FF),
                    Color(0xFFFDF4FF),
                    Color(0xFFF0F4F8)
                ),
                orbGradients = listOf(
                    listOf(Color(0xFFE9D5FF).copy(alpha = 0.4f), Color(0xFFDDD6FE).copy(alpha = 0.3f)),
                    listOf(Color(0xFFFCE7F3).copy(alpha = 0.5f), Color(0xFFFBCFE8).copy(alpha = 0.3f)),
                    listOf(Color(0xFFE0E7FF).copy(alpha = 0.4f), Color(0xFFC7D2FE).copy(alpha = 0.3f)),
                    listOf(Color(0xFFFEF3C7).copy(alpha = 0.4f), Color(0xFFFDE68A).copy(alpha = 0.3f))
                ),
                cardGlowGradient = listOf(
                    Color(0xFFE9D5FF).copy(alpha = 0.6f),
                    Color(0xFFDDD6FE).copy(alpha = 0.5f)
                ),
                translucentSurface = Color.White,
                translucentBorder = Color.White.copy(alpha = 0.8f),
                headerIcon = Color(0xFF2D3748),
                headerTitle = Color(0xFF1A202C),
                primaryText = Color(0xFF2D3748),
                secondaryText = Color(0xFF718096),
                chipSurface = Color.White,
                chipBorder = Color.White.copy(alpha = 0.6f),
                buttonGlowGradient = listOf(
                    Color(0xFFE9D5FF).copy(alpha = 0.3f),
                    Color(0xFFFCE7F3).copy(alpha = 0.3f),
                    Color(0xFFE0E7FF).copy(alpha = 0.3f)
                ),
                buttonFillGradient = listOf(
                    Color.White.copy(alpha = 0.7f),
                    Color.White.copy(alpha = 0.5f)
                ),
                buttonBorder = Color.White.copy(alpha = 0.7f),
                buttonText = Color(0xFF2D3748)
            )
        }
    }
}

