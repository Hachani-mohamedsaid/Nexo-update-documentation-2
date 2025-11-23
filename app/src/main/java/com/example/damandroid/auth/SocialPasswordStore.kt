package com.example.damandroid.auth

import android.content.Context

class SocialPasswordStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPassword(provider: String, email: String): String? {
        return preferences.getString(buildKey(provider, email), null)
    }

    fun savePassword(provider: String, email: String, password: String) {
        preferences.edit()
            .putString(buildKey(provider, email), password)
            .apply()
    }

    fun clearPassword(provider: String, email: String) {
        preferences.edit()
            .remove(buildKey(provider, email))
            .apply()
    }

    private fun buildKey(provider: String, email: String): String {
        return "${provider.lowercase()}|${email.lowercase()}"
    }

    companion object {
        private const val PREFS_NAME = "social_login_passwords"
    }
}

