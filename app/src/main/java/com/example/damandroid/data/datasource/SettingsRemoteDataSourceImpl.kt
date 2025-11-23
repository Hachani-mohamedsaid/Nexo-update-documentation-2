package com.example.damandroid.data.datasource

import com.example.damandroid.data.model.SettingsItemDto
import com.example.damandroid.data.model.UserSettingsDto

class SettingsRemoteDataSourceImpl : SettingsRemoteDataSource {

    private var settings = seedSettings()

    override suspend fun fetchSettings(): UserSettingsDto = settings

    override suspend fun updateToggle(itemId: String, enabled: Boolean): UserSettingsDto {
        settings = settings.copy(
            aiPreferences = settings.aiPreferences.map { it.toggleIfMatches(itemId, enabled) },
            privacySettings = settings.privacySettings.map { it.toggleIfMatches(itemId, enabled) },
            notificationSettings = settings.notificationSettings.map { it.toggleIfMatches(itemId, enabled) }
        )
        return settings
    }

    private fun SettingsItemDto.toggleIfMatches(id: String, enabled: Boolean): SettingsItemDto =
        if (this.id == id) copy(value = enabled) else this

    private fun seedSettings(): UserSettingsDto = UserSettingsDto(
        verificationStatus = "none",
        accountSettings = listOf(
            SettingsItemDto("edit_profile", "person", "Edit Profile", "navigate", false, null),
            SettingsItemDto("change_password", "lock", "Change Password", "navigate", false, null)
        ),
        aiPreferences = listOf(
            SettingsItemDto("ai_suggestions", "sparkles", "AI Suggestions", "toggle", true, null),
            SettingsItemDto("motivation_tips", "sparkles", "Motivation Tips", "toggle", true, null),
            SettingsItemDto("coach_recommendations", "sparkles", "Coach Recommendations", "toggle", true, null),
            SettingsItemDto("smart_notifications", "sparkles", "Smart Notifications", "toggle", false, null)
        ),
        privacySettings = listOf(
            SettingsItemDto("public_profile", "shield", "Public Profile", "toggle", true, null),
            SettingsItemDto("show_location", "location", "Show Location", "toggle", true, null),
            SettingsItemDto("blocked_users", "person_off", "Blocked Users", "navigate", false, null)
        ),
        notificationSettings = listOf(
            SettingsItemDto("push_notifications", "notifications", "Push Notifications", "toggle", true, null),
            SettingsItemDto("email_notifications", "mail", "Email Notifications", "toggle", false, null),
            SettingsItemDto("notification_sound", "volume", "Sound", "toggle", true, null)
        ),
        appInfoItems = listOf(
            SettingsItemDto("terms", "info", "Terms of Service", "navigate", false, null),
            SettingsItemDto("privacy", "info", "Privacy Policy", "navigate", false, null),
            SettingsItemDto("support", "info", "Contact Support", "navigate", false, null),
            SettingsItemDto("about", "info", "About", "navigate", false, "v1.0.0")
        )
    )
}

