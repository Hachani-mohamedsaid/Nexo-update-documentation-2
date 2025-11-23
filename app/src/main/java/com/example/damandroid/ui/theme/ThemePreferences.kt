package com.example.damandroid.ui.theme

import android.content.Context

class ThemePreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSavedDarkMode(): Boolean? {
        return if (prefs.contains(KEY_DARK_MODE)) {
            prefs.getBoolean(KEY_DARK_MODE, false)
        } else {
            null
        }
    }

    fun saveDarkMode(isDarkMode: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
    }

    companion object {
        private const val PREFS_NAME = "damandroid_theme_prefs"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
    }
}

