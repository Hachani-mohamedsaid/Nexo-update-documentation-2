package com.example.damandroid.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val bio: String,
    val location: String,
    val isVerified: Boolean,
    val stats: UserStatsOverview,
    val achievements: List<Achievement>,
    val activities: List<ProfileActivity>,
    val medals: List<ProfileMedal>
)

data class UserStatsOverview(
    val sessionsJoined: Int,
    val sessionsHosted: Int,
    val followers: Int,
    val following: Int,
    val favoriteSports: List<String>,
    val rating: Double? = null // Rating from backend, null if not available
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String
)

data class ProfileActivity(
    val id: String,
    val title: String,
    val sportIcon: String,
    val date: String,
    val time: String,
    val location: String,
    val status: ActivityStatus
)

enum class ActivityStatus {
    UPCOMING,
    COMPLETED,
    CANCELLED
}

data class ProfileMedal(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val rarity: MedalRarity
)

enum class MedalRarity {
    COMMON,
    UNCOMMON,  // Rareté intermédiaire entre COMMON et RARE
    RARE,
    LEGENDARY
}

data class ProfileUpdate(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val location: String? = null,
    val about: String? = null,
    val sportsInterests: List<String>? = null,
    val profileImageUrl: String? = null
)

data class ProfileImageUpload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray
)

data class PasswordChangeResult(
    val message: String
)

