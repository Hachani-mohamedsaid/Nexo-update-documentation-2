package com.example.damandroid.data.model

data class UserSettingsDto(
    val verificationStatus: String,
    val accountSettings: List<SettingsItemDto>,
    val aiPreferences: List<SettingsItemDto>,
    val privacySettings: List<SettingsItemDto>,
    val notificationSettings: List<SettingsItemDto>,
    val appInfoItems: List<SettingsItemDto>
)

data class SettingsItemDto(
    val id: String,
    val icon: String,
    val label: String,
    val type: String,
    val value: Boolean,
    val extra: String?
)

