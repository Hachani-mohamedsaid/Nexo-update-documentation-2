package com.example.damandroid.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class AuthScreenPalette(
    val background: Brush,
    val orbColors: List<Color>,
    val glassSurface: Color,
    val glassBorder: Color,
    val cardSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val linkText: Color,
    val errorText: Color,
    val fieldContainer: Color,
    val fieldBorder: Color,
    val fieldBorderFocused: Color,
    val fieldText: Color,
    val fieldPlaceholder: Color,
    val fieldIcon: Color,
    val buttonGradient: Brush,
    val buttonBorder: Color,
    val buttonText: Color,
    val spinnerColor: Color,
    val dividerColor: Color,
    val dividerLabelBackground: Color,
    val socialGradient: Brush,
    val socialBorder: Color,
    val socialText: Color,
    val highlightGradient: Brush,
)

@Composable
fun rememberAuthScreenPalette(appTheme: AppThemeColors = rememberAppThemeColors()): AuthScreenPalette {
    return remember(appTheme) {
        if (appTheme.isDark) {
            AuthScreenPalette(
                background = appTheme.backgroundGradient,
                orbColors = listOf(
                    appTheme.accentPurple.copy(alpha = 0.32f),
                    appTheme.accentPink.copy(alpha = 0.28f),
                    appTheme.accentBlue.copy(alpha = 0.26f),
                    appTheme.accentGreen.copy(alpha = 0.22f)
                ),
                glassSurface = appTheme.glassSurface,
                glassBorder = appTheme.glassBorder,
                cardSurface = appTheme.cardSurface,
                primaryText = appTheme.primaryText,
                secondaryText = appTheme.secondaryText,
                mutedText = appTheme.mutedText,
                linkText = appTheme.accentPurple,
                errorText = appTheme.danger,
                fieldContainer = appTheme.cardSurface,
                fieldBorder = appTheme.cardBorder,
                fieldBorderFocused = appTheme.accentPurple.copy(alpha = 0.7f),
                fieldText = appTheme.primaryText,
                fieldPlaceholder = appTheme.mutedText,
                fieldIcon = appTheme.secondaryText,
                buttonGradient = Brush.linearGradient(
                    listOf(appTheme.accentPurple, appTheme.accentPink)
                ),
                buttonBorder = appTheme.accentPurple.copy(alpha = 0.4f),
                buttonText = appTheme.iconOnAccent,
                spinnerColor = appTheme.iconOnAccent,
                dividerColor = appTheme.subtleBorder,
                dividerLabelBackground = appTheme.glassSurface,
                socialGradient = Brush.linearGradient(
                    listOf(appTheme.cardSurface, appTheme.glassSurface)
                ),
                socialBorder = appTheme.cardBorder,
                socialText = appTheme.primaryText,
                highlightGradient = Brush.verticalGradient(
                    listOf(appTheme.glassSurface.copy(alpha = 0.6f), Color.Transparent)
                )
            )
        } else {
            AuthScreenPalette(
                background = appTheme.backgroundGradient,
                orbColors = listOf(
                    appTheme.accentPurple.copy(alpha = 0.30f),
                    appTheme.accentPink.copy(alpha = 0.30f),
                    appTheme.accentBlue.copy(alpha = 0.28f),
                    appTheme.accentGreen.copy(alpha = 0.25f)
                ),
                glassSurface = Color.White.copy(alpha = 0.72f),
                glassBorder = Color.White.copy(alpha = 0.82f),
                cardSurface = Color.White.copy(alpha = 0.9f),
                primaryText = Color(0xFF111827),
                secondaryText = Color(0xFF1F2937),
                mutedText = Color(0xFF6B7280),
                linkText = appTheme.accentPurple,
                errorText = appTheme.danger,
                fieldContainer = Color.White,
                fieldBorder = Color.White.copy(alpha = 0.6f),
                fieldBorderFocused = appTheme.accentPurple.copy(alpha = 0.6f),
                fieldText = Color.Black,
                fieldPlaceholder = Color(0xFF555555),
                fieldIcon = Color.Black.copy(alpha = 0.6f),
                buttonGradient = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.5f))
                ),
                buttonBorder = Color.White.copy(alpha = 0.7f),
                buttonText = Color.Black,
                spinnerColor = Color.Black,
                dividerColor = Color.White.copy(alpha = 0.4f),
                dividerLabelBackground = Color.White.copy(alpha = 0.7f),
                socialGradient = Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.5f))
                ),
                socialBorder = Color.White.copy(alpha = 0.7f),
                socialText = Color.Black,
                highlightGradient = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                )
            )
        }
    }
}

