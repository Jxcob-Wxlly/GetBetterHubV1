package com.getbetterhub.app.data.remote

// Mirrors the backend's DTOs/AuthDtos.cs and DTOs/ResourceDtos.cs exactly — field names and
// casing must match what Gson will (de)serialize against the API's JSON.

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val preferredLanguage: String,
    val primaryInstrument: String?
)

data class LoginRequest(val email: String, val password: String)

data class GoogleLoginRequest(val idToken: String)

data class AuthResponse(
    val token: String,
    val expiresAt: String,
    val userId: String,
    val username: String
)

/**
 * Profile as returned by GET/PUT api/users/me. Fields added in the final build are nullable on
 * purpose: Gson leaves absent JSON fields as null even for Kotlin non-null types, which would crash.
 */
data class UserProfileResponse(
    val userId: String,
    val username: String,
    val email: String,
    val preferredLanguage: String,
    val primaryInstrument: String?,
    val profileImageUrl: String?,
    val bio: String?,
    val streakCount: Int,
    val level: Int,
    val notificationsEnabled: Boolean,
    val displayName: String? = null,
    val handle: String? = null,
    val skillLevel: String? = null,
    val instrumentsCsv: String? = null,
    val avatarColorIndex: Int? = null,
    val avatarEmoji: String? = null,
    val avatarRound: Boolean? = null,
    val isPrivate: Boolean? = null,
    val bioPublic: Boolean? = null,
    val instrumentsPublic: Boolean? = null,
    val picturePublic: Boolean? = null,
    val badgesPublic: Boolean? = null,
    val followerCount: Int? = null,
    val followingCount: Int? = null
)

data class UpdateProfileRequest(
    val bio: String? = null,
    val preferredLanguage: String? = null,
    val primaryInstrument: String? = null,
    val profileImageUrl: String? = null,
    val notificationsEnabled: Boolean? = null,
    val displayName: String? = null,
    val handle: String? = null,
    val skillLevel: String? = null,
    val instrumentsCsv: String? = null,
    val avatarColorIndex: Int? = null,
    val avatarEmoji: String? = null,
    val avatarRound: Boolean? = null,
    val isPrivate: Boolean? = null,
    val bioPublic: Boolean? = null,
    val instrumentsPublic: Boolean? = null,
    val picturePublic: Boolean? = null,
    val badgesPublic: Boolean? = null
)

/** A person in search results / follow requests, with the caller's relationship to them. */
data class PersonResponse(
    val userId: String?,
    val displayName: String?,
    val handle: String?,
    val level: Int?,
    val isPrivate: Boolean?,
    /** "none" | "following" | "requested" */
    val relationship: String?
)

data class FollowResponse(val status: String?)

/** Server-side event for this user (new follower, follow request, request approved). */
data class ServerNotificationResponse(
    val id: String?,
    /** "new_follower" | "follow_request" | "request_approved" */
    val type: String?,
    val actorName: String?,
    val createdAt: String?
)

data class CreatePracticeSessionRequest(
    val instrument: String,
    val drillType: String?,
    val mood: String?,
    val durationSeconds: Int
)

data class PracticeSessionResponse(
    val sessionId: String,
    val instrument: String,
    val drillType: String?,
    val mood: String?,
    val durationSeconds: Int,
    val completedAt: String
)

data class SaveLyricDraftRequest(val title: String?, val content: String)

data class LyricDraftResponse(
    val draftId: String,
    val title: String?,
    val content: String,
    val lastEditedAt: String
)

data class CreatePerformancePostRequest(
    val audioUrl: String,
    val challengeId: String?,
    val caption: String?
)

data class PerformancePostResponse(
    val postId: String,
    val author: String,
    val audioUrl: String,
    val caption: String?,
    val likeCount: Int,
    val commentCount: Int,
    val postedAt: String
)

data class CreateCommentRequest(val text: String)

data class CommentResponse(
    val commentId: String,
    val author: String,
    val text: String,
    val postedAt: String
)

data class CreateJournalEntryRequest(val audioUrl: String?, val note: String?)

data class JournalEntryResponse(
    val entryId: String,
    val audioUrl: String?,
    val note: String?,
    val recordedAt: String,
    val syncStatus: String
)

data class UploadResponse(val url: String)
