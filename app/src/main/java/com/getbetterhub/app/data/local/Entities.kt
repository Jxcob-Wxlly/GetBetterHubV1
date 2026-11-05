package com.getbetterhub.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table holding the signed-in user's gamification stats (streak, level, XP)
 * and last-known identity, so Home/Arena survive an app restart. Row id is always 0 —
 * this app is single-user/offline-first for now; multi-account support is a later change.
 */
@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 0,
    val username: String,
    val dayStreak: Int,
    val level: Int,
    val xp: Int,
    val lastSessionDateEpochDay: Long?,
    val commentsMade: Int = 0
)

@Entity(tableName = "recent_sessions")
data class RecentSessionEntity(
    @PrimaryKey val id: String,
    val label: String,
    val instrument: String,
    val drillType: String?,
    val mood: String?,
    val durationSeconds: Int,
    val synced: Boolean,
    val completedAtEpochMillis: Long
)

@Entity(tableName = "lyric_drafts")
data class LyricDraftEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val synced: Boolean,
    val lastEditedAtEpochMillis: Long
)

@Entity(tableName = "journal_takes")
data class JournalTakeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val synced: Boolean,
    val durationSeconds: Int,
    val recordedAtEpochMillis: Long,
    val localFilePath: String?,
    val audioUrl: String?
    // Playback position/isPlaying are transient UI state, not persisted.
)

@Entity(tableName = "arena_posts")
data class ArenaPostEntity(
    @PrimaryKey val id: String,
    val author: String,
    val tagsCsv: String,
    val caption: String,
    val likeCount: Int,
    val commentCount: Int,
    val audioUrl: String?,
    val synced: Boolean,
    val postedAtEpochMillis: Long
)

@Entity(tableName = "joined_challenges")
data class JoinedChallengeEntity(
    @PrimaryKey val challengeTitle: String,
    val joinedAtEpochMillis: Long
)

/**
 * The signed-in user's profile + privacy choices (single row, id 0). Saved locally first with
 * `synced = false`, pushed to the API when online — so editing a profile works offline.
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 0,
    val displayName: String,
    val handle: String,
    val bio: String,
    val primaryInstrument: String,
    val level: String,
    val instrumentsCsv: String,
    val avatarColorIndex: Int,
    val avatarEmoji: String,
    val avatarRound: Boolean,
    val avatarPath: String?,
    val bannerPath: String?,
    val isPrivate: Boolean,
    val bioPublic: Boolean,
    val instrumentsPublic: Boolean,
    val picturePublic: Boolean,
    val badgesPublic: Boolean,
    val followers: Int,
    val following: Int,
    val synced: Boolean
)

/**
 * In-app notification inbox entry. We store the *type* and a small argument (e.g. a username or
 * achievement name) rather than finished text, so the entry re-translates when the language changes.
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String,
    val arg: String,
    val createdAtEpochMillis: Long,
    val read: Boolean
)
