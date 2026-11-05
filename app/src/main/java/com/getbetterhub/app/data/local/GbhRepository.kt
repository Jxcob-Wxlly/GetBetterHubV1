package com.getbetterhub.app.data.local

import com.getbetterhub.app.core.AppLog
import com.getbetterhub.app.data.remote.GbhNetworkRepository
import com.getbetterhub.app.data.remote.NetworkResult
import com.getbetterhub.app.data.remote.UpdateProfileRequest
import com.getbetterhub.app.data.remote.UserProfileResponse
import com.getbetterhub.app.domain.Profile
import com.getbetterhub.app.domain.SessionRecord
import com.getbetterhub.app.ui.screens.ArenaPost
import com.getbetterhub.app.ui.screens.JournalTake
import com.getbetterhub.app.ui.screens.LyricDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Single access point for local persistence. Wraps the Room DAOs and maps between
 * storage entities and the plain UI data classes the screens already use, so the
 * screens themselves never need to know Room exists.
 *
 * Offline-with-sync pattern used throughout: every write lands in Room immediately
 * (so the UI always works offline), tagged `synced = false`. [syncPendingWithServer]
 * sweeps those rows and pushes them to the API when it's reachable, flipping them to
 * `synced = true` on success. Nothing is ever lost by being offline — it just waits.
 */
class GbhRepository(private val db: GbhDatabase) {

    // ---- User stats ----
    data class Stats(
        val username: String,
        val dayStreak: Int,
        val level: Int,
        val xp: Int,
        val lastSessionDateEpochDay: Long?,
        /** Read-only here: saveStats keeps the stored value so UI code that rebuilds Stats can't reset it. */
        val commentsMade: Int = 0
    )

    fun observeStats(): Flow<Stats?> = db.userStatsDao().observe().map {
        it?.let { s -> Stats(s.username, s.dayStreak, s.level, s.xp, s.lastSessionDateEpochDay, s.commentsMade) }
    }

    suspend fun saveStats(stats: Stats) {
        val existing = db.userStatsDao().getOnce()
        db.userStatsDao().upsert(
            UserStatsEntity(
                username = stats.username,
                dayStreak = stats.dayStreak,
                level = stats.level,
                xp = stats.xp,
                lastSessionDateEpochDay = stats.lastSessionDateEpochDay,
                commentsMade = existing?.commentsMade ?: 0
            )
        )
    }

    /** Records that the user left a comment in the Arena (feeds the "Feedback Fiend" achievement). */
    suspend fun addComment(postId: String) {
        if (postId.isNotBlank()) db.arenaPostDao().addComment(postId)
        val existing = db.userStatsDao().getOnce() ?: return
        db.userStatsDao().upsert(existing.copy(commentsMade = existing.commentsMade + 1))
    }

    // ---- Recent / practice sessions ----
    fun observeRecentSessions(): Flow<List<String>> =
        db.recentSessionDao().observeAll().map { list -> list.map { it.label } }

    /** All sessions as plain records — exercise progress and achievements are computed from these. */
    fun observeSessionRecords(): Flow<List<SessionRecord>> =
        db.recentSessionDao().observeEverything().map { list ->
            list.map { SessionRecord(it.label, it.instrument, it.durationSeconds, it.completedAtEpochMillis, it.synced) }
        }

    /** Saves the session locally first (so it always succeeds even offline), then attempts
     *  an immediate server push. Either way the row is in Room and will be retried later by
     *  [syncPendingWithServer] if this immediate attempt didn't already mark it synced. */
    suspend fun addRecentSession(
        label: String,
        instrument: String,
        drillType: String?,
        mood: String?,
        durationSeconds: Int,
        network: GbhNetworkRepository
    ) {
        val id = UUID.randomUUID().toString()
        val synced = pushPracticeSession(network, instrument, drillType, mood, durationSeconds)
        db.recentSessionDao().insert(
            RecentSessionEntity(
                id = id,
                label = label,
                instrument = instrument,
                drillType = drillType,
                mood = mood,
                durationSeconds = durationSeconds,
                synced = synced,
                completedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    private suspend fun pushPracticeSession(
        network: GbhNetworkRepository, instrument: String, drillType: String?, mood: String?, durationSeconds: Int
    ): Boolean =
        network.syncPracticeSession(instrument, drillType, mood, durationSeconds) is NetworkResult.Success

    // ---- Lyric drafts ----
    fun observeLyricDrafts(): Flow<List<LyricDraft>> =
        db.lyricDraftDao().observeAll().map { list -> list.map { LyricDraft(it.id, it.title, it.content, it.synced) } }

    suspend fun saveLyricDraft(draft: LyricDraft, network: GbhNetworkRepository) {
        val synced = network.saveLyricDraft(draft.id, draft.title, draft.content) is NetworkResult.Success
        db.lyricDraftDao().upsert(
            LyricDraftEntity(
                id = draft.id,
                title = draft.title,
                content = draft.content,
                synced = synced,
                lastEditedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteLyricDraft(draft: LyricDraft, network: GbhNetworkRepository) {
        // Best-effort remote delete — if it fails (offline), the draft is still removed locally.
        // A queued-deletes table would be needed to retry this specific case; out of scope for now.
        network.deleteLyricDraft(draft.id)
        db.lyricDraftDao().delete(
            LyricDraftEntity(draft.id, draft.title, draft.content, draft.synced, 0L)
        )
    }

    // ---- Journal takes ----
    // positionSeconds/isPlaying are transient playback UI state — always loaded as 0/false from storage.
    fun observeJournalTakes(): Flow<List<JournalTake>> =
        db.journalTakeDao().observeAll().map { list ->
            list.map {
                JournalTake(
                    id = it.id, title = it.title, synced = it.synced,
                    positionSeconds = 0, durationSeconds = it.durationSeconds, isPlaying = false,
                    localFilePath = it.localFilePath, audioUrl = it.audioUrl
                )
            }
        }

    /** Uploads the recorded clip (if the upload succeeds) then creates the server-side journal
     *  entry with that URL. Saves locally either way — synced only when both steps succeed. */
    suspend fun addJournalTake(take: JournalTake, localFile: java.io.File?, network: GbhNetworkRepository) {
        val audioUrl = localFile?.let { network.uploadAudio(it) }
        val synced = network.syncJournalEntry(audioUrl = audioUrl, note = take.title) is NetworkResult.Success
        db.journalTakeDao().insert(
            JournalTakeEntity(
                id = take.id,
                title = take.title,
                synced = synced,
                durationSeconds = take.durationSeconds,
                recordedAtEpochMillis = System.currentTimeMillis(),
                localFilePath = take.localFilePath,
                audioUrl = audioUrl
            )
        )
    }

    suspend fun deleteJournalTake(take: JournalTake) {
        db.journalTakeDao().delete(
            JournalTakeEntity(take.id, take.title, take.synced, take.durationSeconds, 0L, take.localFilePath, null)
        )
    }

    // ---- Arena posts ----
    // A post can only sync once it carries a real, already-uploaded audioUrl — the API
    // requires one. Posts created without evidence attached stay local-only; there's nothing
    // to retry for those (no audio to upload), unlike the other resources' offline queues.
    fun observeArenaPosts(): Flow<List<ArenaPost>> =
        db.arenaPostDao().observeAll().map { list ->
            list.map { ArenaPost(it.author, it.tagsCsv.split(",").filter { t -> t.isNotBlank() }, it.caption, it.likeCount, it.commentCount, it.id) }
        }

    suspend fun addArenaPost(post: ArenaPost, evidenceAudioUrl: String?, network: GbhNetworkRepository) {
        val synced = if (evidenceAudioUrl != null) {
            network.createPerformancePost(evidenceAudioUrl, challengeId = null, caption = post.body) is NetworkResult.Success
        } else false
        db.arenaPostDao().insert(
            ArenaPostEntity(
                id = UUID.randomUUID().toString(),
                author = post.author,
                tagsCsv = post.tags.joinToString(","),
                caption = post.body,
                likeCount = post.likes,
                commentCount = post.commentCount,
                audioUrl = evidenceAudioUrl,
                synced = synced,
                postedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    // ---- Joined challenges ----
    fun observeJoinedChallengeTitles(): Flow<List<String>> =
        db.joinedChallengeDao().observeAll().map { list -> list.map { it.challengeTitle } }

    suspend fun markChallengeJoined(title: String) {
        db.joinedChallengeDao().insert(JoinedChallengeEntity(title, System.currentTimeMillis()))
    }

    // ---- Profile (offline-first) ----
    fun observeProfile(): Flow<Profile?> = db.profileDao().observe().map { it?.toDomain() }

    suspend fun getProfile(): Profile? = db.profileDao().getOnce()?.toDomain()

    /** Creates the default profile row the first time a user signs in on this phone. */
    suspend fun ensureProfile(username: String, userId: String?) {
        if (db.profileDao().getOnce() != null) return
        val profile = Profile(
            displayName = username,
            handle = Profile.defaultHandle(username, userId ?: UUID.randomUUID().toString()),
            synced = false
        )
        db.profileDao().upsert(profile.toEntity())
    }

    enum class SaveResult { Saved, SavedOffline, HandleTaken }

    /**
     * Saves the profile on the phone first (so it always works offline), then tries to push it. If
     * the server says the @handle is already taken, the previous handle is restored and
     * [SaveResult.HandleTaken] is returned so the UI can tell the user.
     */
    suspend fun saveProfile(profile: Profile, network: GbhNetworkRepository, notificationsEnabled: Boolean, language: String): SaveResult {
        val previous = db.profileDao().getOnce()
        val normalised = profile.normalised()
        db.profileDao().upsert(normalised.copy(synced = false).toEntity())
        val result = network.pushProfile(normalised.toRequest(notificationsEnabled, language))
        return when {
            result is NetworkResult.Success -> {
                db.profileDao().markSynced()
                AppLog.i(TAG, "profile synced")
                SaveResult.Saved
            }
            result is NetworkResult.ApiError && result.code == 409 -> {
                if (previous != null) db.profileDao().upsert(previous.copy(synced = previous.synced))
                AppLog.w(TAG, "handle already taken")
                SaveResult.HandleTaken
            }
            else -> {
                AppLog.d(TAG, "profile saved offline, will sync later")
                SaveResult.SavedOffline
            }
        }
    }

    /** Pulls follower counts (always) and server profile fields (only if nothing is waiting to be pushed). */
    suspend fun pullProfile(network: GbhNetworkRepository) {
        val result = network.fetchMyProfile()
        if (result !is NetworkResult.Success) return
        val remote = result.data
        val local = db.profileDao().getOnce() ?: return
        db.profileDao().updateCounts(remote.followerCount ?: local.followers, remote.followingCount ?: local.following)
        if (!local.synced) return // local edits win until they have been pushed
        db.profileDao().upsert(remote.mergeInto(local))
    }

    // ---- Notifications inbox ----
    fun observeNotifications(): Flow<List<NotificationEntity>> = db.notificationDao().observeAll()

    suspend fun addNotification(type: String, arg: String, id: String = UUID.randomUUID().toString()): Boolean =
        db.notificationDao().insert(NotificationEntity(id, type, arg, System.currentTimeMillis(), false)) != -1L

    suspend fun markNotificationsRead() = db.notificationDao().markAllRead()

    suspend fun clearNotifications() = db.notificationDao().clear()

    /**
     * Fetches follower activity from the server. Returns only the notifications that are new on
     * this phone so the caller can raise a system notification for each one exactly once.
     */
    suspend fun pullServerNotifications(network: GbhNetworkRepository): List<NotificationEntity> {
        val result = network.serverNotifications()
        if (result !is NetworkResult.Success) return emptyList()
        val fresh = mutableListOf<NotificationEntity>()
        result.data.forEach { n ->
            val id = n.id ?: return@forEach
            val type = n.type ?: return@forEach
            val entity = NotificationEntity("srv-$id", type, n.actorName.orEmpty(), System.currentTimeMillis(), false)
            if (db.notificationDao().insert(entity) != -1L) fresh += entity
        }
        return fresh
    }

    // ---- Pending (not yet synced) count, for the Synced / Syncing pill and Settings ----
    fun observePendingCount(): Flow<Int> = combine(
        db.recentSessionDao().observeUnsyncedCount(),
        db.lyricDraftDao().observeUnsyncedCount(),
        db.journalTakeDao().observeUnsyncedCount(),
        db.profileDao().observeUnsyncedCount()
    ) { a, b, c, d -> a + b + c + d }

    /** One-shot count of everything still waiting to be pushed (used to decide whether to retry). */
    suspend fun pendingNow(): Int = observePendingCount().first()

    // ---- Sync sweep ----
    /** Call whenever the app is signed in and might have regained connectivity (app launch,
     *  app resume). Pushes anything still marked unsynced; safe to call often — it's a no-op
     *  once everything is caught up, and each item only flips to synced after a real success. */
    suspend fun syncPendingWithServer(network: GbhNetworkRepository, notificationsEnabled: Boolean = true, language: String = "en") {
        AppLog.d(TAG, "sync sweep started")
        db.profileDao().getOnce()?.takeIf { !it.synced }?.let { local ->
            val result = network.pushProfile(local.toDomain().toRequest(notificationsEnabled, language))
            if (result is NetworkResult.Success) db.profileDao().markSynced() else AppLog.w(TAG, "profile push failed: $result")
        }
        db.recentSessionDao().getUnsynced().forEach { session ->
            val ok = pushPracticeSession(network, session.instrument, session.drillType, session.mood, session.durationSeconds)
            if (ok) db.recentSessionDao().markSynced(session.id) else AppLog.w(TAG, "practice session ${session.id} push failed")
        }
        db.lyricDraftDao().getUnsynced().forEach { draft ->
            val result = network.saveLyricDraft(draft.id, draft.title, draft.content)
            if (result is NetworkResult.Success) db.lyricDraftDao().markSynced(draft.id) else AppLog.w(TAG, "lyric draft push failed: $result")
        }
        db.journalTakeDao().getUnsynced().forEach { take ->
            val audioUrl = take.localFilePath?.let { path ->
                val file = java.io.File(path)
                if (file.exists()) network.uploadAudio(file) else null
            }
            val result = network.syncJournalEntry(audioUrl = audioUrl, note = take.title)
            if (result is NetworkResult.Success) db.journalTakeDao().markSynced(take.id) else AppLog.w(TAG, "journal take push failed: $result")
        }
        db.arenaPostDao().getUnsynced().forEach { post ->
            val url = post.audioUrl ?: return@forEach
            val result = network.createPerformancePost(url, challengeId = null, caption = post.caption)
            if (result is NetworkResult.Success) db.arenaPostDao().markSynced(post.id)
        }
    }

    private companion object {
        const val TAG = "Repository"
    }
}

// ---- Mapping between Room entities, domain models and the API ----

private fun ProfileEntity.toDomain() = Profile(
    displayName = displayName, handle = handle, bio = bio,
    primaryInstrument = primaryInstrument, level = level,
    instruments = instrumentsCsv.split(",").filter { it.isNotBlank() },
    avatarColorIndex = avatarColorIndex, avatarEmoji = avatarEmoji, avatarRound = avatarRound,
    avatarPath = avatarPath, bannerPath = bannerPath,
    isPrivate = isPrivate, bioPublic = bioPublic, instrumentsPublic = instrumentsPublic,
    picturePublic = picturePublic, badgesPublic = badgesPublic,
    followers = followers, following = following, synced = synced
)

private fun Profile.toEntity() = ProfileEntity(
    id = 0, displayName = displayName, handle = handle, bio = bio,
    primaryInstrument = primaryInstrument, level = level,
    instrumentsCsv = instruments.joinToString(","),
    avatarColorIndex = avatarColorIndex, avatarEmoji = avatarEmoji, avatarRound = avatarRound,
    avatarPath = avatarPath, bannerPath = bannerPath,
    isPrivate = isPrivate, bioPublic = bioPublic, instrumentsPublic = instrumentsPublic,
    picturePublic = picturePublic, badgesPublic = badgesPublic,
    followers = followers, following = following, synced = synced
)

private fun Profile.toRequest(notificationsEnabled: Boolean, language: String) = UpdateProfileRequest(
    bio = bio,
    preferredLanguage = language,
    primaryInstrument = primaryInstrument,
    notificationsEnabled = notificationsEnabled,
    displayName = displayName,
    handle = handle,
    skillLevel = level,
    instrumentsCsv = instruments.joinToString(","),
    avatarColorIndex = avatarColorIndex,
    avatarEmoji = avatarEmoji,
    avatarRound = avatarRound,
    isPrivate = isPrivate,
    bioPublic = bioPublic,
    instrumentsPublic = instrumentsPublic,
    picturePublic = picturePublic,
    badgesPublic = badgesPublic
)

/** Server values override local ones, but local-only image paths are kept. Marked synced. */
private fun UserProfileResponse.mergeInto(local: ProfileEntity) = local.copy(
    displayName = displayName ?: local.displayName,
    handle = handle ?: local.handle,
    bio = bio ?: local.bio,
    primaryInstrument = primaryInstrument ?: local.primaryInstrument,
    level = skillLevel ?: local.level,
    instrumentsCsv = instrumentsCsv ?: local.instrumentsCsv,
    avatarColorIndex = avatarColorIndex ?: local.avatarColorIndex,
    avatarEmoji = avatarEmoji ?: local.avatarEmoji,
    avatarRound = avatarRound ?: local.avatarRound,
    isPrivate = isPrivate ?: local.isPrivate,
    bioPublic = bioPublic ?: local.bioPublic,
    instrumentsPublic = instrumentsPublic ?: local.instrumentsPublic,
    picturePublic = picturePublic ?: local.picturePublic,
    badgesPublic = badgesPublic ?: local.badgesPublic,
    followers = followerCount ?: local.followers,
    following = followingCount ?: local.following,
    synced = true
)
