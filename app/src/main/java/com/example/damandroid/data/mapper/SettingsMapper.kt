package com.example.damandroid.data.mapper

import com.example.damandroid.data.model.SettingsItemDto
import com.example.damandroid.data.model.UserSettingsDto
import com.example.damandroid.domain.model.SettingsItem
import com.example.damandroid.domain.model.SettingsItemType
import com.example.damandroid.domain.model.UserSettings
import com.example.damandroid.domain.model.VerificationStatus

fun UserSettingsDto.toDomain(): UserSettings = UserSettings(
    verificationStatus = VerificationStatus.valueOf(verificationStatus.uppercase()),
    accountSettings = accountSettings.map(SettingsItemDto::toDomain),
    aiPreferences = aiPreferences.map(SettingsItemDto::toDomain),
    privacySettings = privacySettings.map(SettingsItemDto::toDomain),
    notificationSettings = notificationSettings.map(SettingsItemDto::toDomain),
    appInfoItems = appInfoItems.map(SettingsItemDto::toDomain)
)

fun SettingsItemDto.toDomain(): SettingsItem = SettingsItem(
    id = id,
    icon = icon,
    label = label,
    type = SettingsItemType.valueOf(type.uppercase()),
    value = value,
    extra = extra
)

