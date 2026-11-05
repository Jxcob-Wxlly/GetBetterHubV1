package com.getbetterhub.app.domain

/** The signed-in user's profile as the screens see it (stored locally first, synced when online). */
data class Profile(
    val displayName: String,
    val handle: String,
    val bio: String = "",
    val primaryInstrument: String = "vocals",
    val level: String = "beginner",
    val instruments: List<String> = listOf("vocals"),
    val avatarColorIndex: Int = 0,
    val avatarEmoji: String = "",
    val avatarRound: Boolean = true,
    val avatarPath: String? = null,
    val bannerPath: String? = null,
    val isPrivate: Boolean = false,
    val bioPublic: Boolean = true,
    val instrumentsPublic: Boolean = true,
    val picturePublic: Boolean = true,
    val badgesPublic: Boolean = true,
    val followers: Int = 0,
    val following: Int = 0,
    val synced: Boolean = false
) {
    /** The primary instrument is always part of the practised set. */
    fun normalised(): Profile {
        val set = (instruments + primaryInstrument).distinct()
        return copy(instruments = set)
    }

    companion object {
        private val HANDLE_REGEX = Regex("^[a-z0-9_]{3,20}$")

        fun isValidHandle(handle: String) = HANDLE_REGEX.matches(handle)

        /** Default handle like "jacobwally_c572f" — name plus a short suffix so it is very likely unique. */
        fun defaultHandle(username: String, seed: String): String {
            val base = username.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "player" }.take(12)
            val suffix = seed.filter { it.isLetterOrDigit() }.lowercase().takeLast(5).padStart(5, '0')
            return "${base}_$suffix"
        }

        val avatarColors = listOf(0xFFF7A831, 0xFFF17166, 0xFF4CAF7D, 0xFF9B6FCF, 0xFFA6A2B0)
        val avatarEmojis = listOf("", "🎸", "🎤", "🎹", "🥁", "🎧", "🔥", "⭐")
    }
}

/** A person found through search, with the viewer's relationship to them. */
data class PersonSummary(
    val userId: String,
    val displayName: String,
    val handle: String,
    val level: Int,
    val isPrivate: Boolean,
    /** "none" | "following" | "requested" */
    val relationship: String
)
