package com.getbetterhub.app.core

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.getbetterhub.app.R
import com.getbetterhub.app.data.local.GbhDatabase
import com.getbetterhub.app.data.local.NotificationEntity
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Runs about hourly in the background (WorkManager keeps it alive across app/phone restarts) and,
 * in the early evening, nudges the user if they have not practised today. At most one reminder
 * per day, only when "Practice reminders" is on in Settings. Works fully offline.
 * Reference: Android developers, "Schedule tasks with WorkManager" (PeriodicWorkRequest).
 */
class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = SettingsStore(applicationContext)
        if (!settings.practiceReminders) return Result.success()

        val today = LocalDate.now().toEpochDay()
        if (settings.lastReminderEpochDay == today) return Result.success()
        if (LocalTime.now().hour !in REMINDER_HOURS) return Result.success()

        val db = GbhDatabase.getInstance(applicationContext)
        val stats = db.userStatsDao().getOnce()
        if (stats == null) return Result.success() // nobody signed in yet
        if (stats.lastSessionDateEpochDay == today) return Result.success() // already practised

        val localised = LocaleHelper.wrap(applicationContext, settings.language)
        Notifier.show(
            localised, NOTIFICATION_ID,
            localised.getString(R.string.notif_reminder_title),
            localised.getString(R.string.notif_reminder_body)
        )
        db.notificationDao().insert(
            NotificationEntity(UUID.randomUUID().toString(), TYPE_REMINDER, "", System.currentTimeMillis(), false)
        )
        settings.lastReminderEpochDay = today
        AppLog.i("ReminderWorker", "practice reminder sent")
        return Result.success()
    }

    companion object {
        const val TYPE_REMINDER = "reminder"
        private const val NOTIFICATION_ID = 1001
        private const val WORK_NAME = "gbh_practice_reminder"
        private val REMINDER_HOURS = 17..20

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.HOURS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
