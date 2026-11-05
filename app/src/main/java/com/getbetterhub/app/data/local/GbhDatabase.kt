package com.getbetterhub.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserStatsEntity::class,
        RecentSessionEntity::class,
        LyricDraftEntity::class,
        JournalTakeEntity::class,
        ArenaPostEntity::class,
        JoinedChallengeEntity::class,
        ProfileEntity::class,
        NotificationEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class GbhDatabase : RoomDatabase() {
    abstract fun userStatsDao(): UserStatsDao
    abstract fun recentSessionDao(): RecentSessionDao
    abstract fun lyricDraftDao(): LyricDraftDao
    abstract fun journalTakeDao(): JournalTakeDao
    abstract fun arenaPostDao(): ArenaPostDao
    abstract fun joinedChallengeDao(): JoinedChallengeDao
    abstract fun profileDao(): ProfileDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile private var INSTANCE: GbhDatabase? = null

        fun getInstance(context: Context): GbhDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GbhDatabase::class.java,
                    "gbh.db"
                )
                    // No real migration path yet at this stage of the project — acceptable for a
                    // student build still in active development. Before any real release, replace
                    // this with a proper Migration(1, 2) that preserves existing local data.
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
