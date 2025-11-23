package com.example.damandroid.domain.model

data class UserSettings(
    val verificationStatus: VerificationStatus,
    val accountSettings: List<SettingsItem>,
    val aiPreferences: List<SettingsItem>,
    val privacySettings: List<SettingsItem>,
    val notificationSettings: List<SettingsItem>,
    val appInfoItems: List<SettingsItem>
)

data class SettingsItem(
    val id: String,
    val icon: String,
    val label: String,
    val type: SettingsItemType,
    val value: Boolean = false,
    val extra: String? = null
)

enum class SettingsItemType {
    NAVIGATE,
    TOGGLE
}

