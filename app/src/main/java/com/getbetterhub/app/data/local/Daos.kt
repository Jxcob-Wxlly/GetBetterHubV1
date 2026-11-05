package com.getbetterhub.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 0 LIMIT 1")
    fun observe(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 0 LIMIT 1")
    suspend fun getOnce(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: UserStatsEntity)
}

@Dao
interface RecentSessionDao {
    @Query("SELECT * FROM recent_sessions ORDER BY completedAtEpochMillis DESC LIMIT 20")
    fun observeAll(): Flow<List<RecentSessionEntity>>

    /** Every session (not just the latest 20) — achievements and exercise progress are computed from this. */
    @Query("SELECT * FROM recent_sessions ORDER BY completedAtEpochMillis DESC")
    fun observeEverything(): Flow<List<RecentSessionEntity>>

    @Query("SELECT COUNT(*) FROM recent_sessions WHERE synced = 0")
    fun observeUnsyncedCount(): Flow<Int>

    @Query("SELECT * FROM recent_sessions WHERE synced = 0")
    suspend fun getUnsynced(): List<RecentSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: RecentSessionEntity)

    @Query("UPDATE recent_sessions SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)
}

@Dao
interface LyricDraftDao {
    @Query("SELECT * FROM lyric_drafts ORDER BY lastEditedAtEpochMillis DESC")
    fun observeAll(): Flow<List<LyricDraftEntity>>

    @Query("SELECT COUNT(*) FROM lyric_drafts WHERE synced = 0")
    fun observeUnsyncedCount(): Flow<Int>

    @Query("SELECT * FROM lyric_drafts WHERE synced = 0")
    suspend fun getUnsynced(): List<LyricDraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(draft: LyricDraftEntity)

    @Query("UPDATE lyric_drafts SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Delete
    suspend fun delete(draft: LyricDraftEntity)
}

@Dao
interface JournalTakeDao {
    @Query("SELECT * FROM journal_takes ORDER BY recordedAtEpochMillis DESC")
    fun observeAll(): Flow<List<JournalTakeEntity>>

    @Query("SELECT COUNT(*) FROM journal_takes WHERE synced = 0")
    fun observeUnsyncedCount(): Flow<Int>

    @Query("SELECT * FROM journal_takes WHERE synced = 0")
    suspend fun getUnsynced(): List<JournalTakeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(take: JournalTakeEntity)

    @Query("UPDATE journal_takes SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Delete
    suspend fun delete(take: JournalTakeEntity)
}

@Dao
interface ArenaPostDao {
    @Query("SELECT * FROM arena_posts ORDER BY postedAtEpochMillis DESC")
    fun observeAll(): Flow<List<ArenaPostEntity>>

    @Query("SELECT * FROM arena_posts WHERE synced = 0 AND audioUrl IS NOT NULL")
    suspend fun getUnsynced(): List<ArenaPostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: ArenaPostEntity)

    @Query("UPDATE arena_posts SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("UPDATE arena_posts SET commentCount = commentCount + 1 WHERE id = :id")
    suspend fun addComment(id: String)
}

@Dao
interface JoinedChallengeDao {
    @Query("SELECT * FROM joined_challenges")
    fun observeAll(): Flow<List<JoinedChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(joined: JoinedChallengeEntity)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    fun observe(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 0 LIMIT 1")
    suspend fun getOnce(): ProfileEntity?

    @Query("SELECT COUNT(*) FROM profile WHERE synced = 0")
    fun observeUnsyncedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ProfileEntity)

    @Query("UPDATE profile SET synced = 1 WHERE id = 0")
    suspend fun markSynced()

    @Query("UPDATE profile SET followers = :followers, following = :following WHERE id = 0")
    suspend fun updateCounts(followers: Int, following: Int)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY createdAtEpochMillis DESC LIMIT 50")
    fun observeAll(): Flow<List<NotificationEntity>>

    /** Returns the new row id, or -1 if a notification with this id already exists (de-duplication). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET read = 1")
    suspend fun markAllRead()

    @Query("DELETE FROM notifications")
    suspend fun clear()
}
