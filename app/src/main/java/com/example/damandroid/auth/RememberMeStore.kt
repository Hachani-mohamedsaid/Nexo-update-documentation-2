package com.example.damandroid.auth

import android.content.Context

class RememberMeStore(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isRememberMeEnabled(): Boolean {
        return preferences.getBoolean(KEY_ENABLED, false)
    }

    fun getEmail(): String? {
        return preferences.getString(KEY_EMAIL, null)
    }

    fun getPassword(): String? {
        return preferences.getString(KEY_PASSWORD, null)
    }

    fun saveCredentials(email: String, password: String) {
        preferences.edit()
            .putBoolean(KEY_ENABLED, true)
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    fun setEnabled(enabled: Boolean) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_ENABLED, enabled)
        if (!enabled) {
            editor.remove(KEY_EMAIL)
            editor.remove(KEY_PASSWORD)
        }
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "remember_me_store"
        private const val KEY_ENABLED = "remember_me_enabled"
        private const val KEY_EMAIL = "remember_me_email"
        private const val KEY_PASSWORD = "remember_me_password"
    }
}


