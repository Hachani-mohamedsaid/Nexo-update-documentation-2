package com.example.damandroid.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

data class ThemeController(
    val isDarkMode: Boolean,
    val setDarkMode: (Boolean) -> Unit
) {
    fun toggle() = setDarkMode(!isDarkMode)
}

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("ThemeController not provided")
}

