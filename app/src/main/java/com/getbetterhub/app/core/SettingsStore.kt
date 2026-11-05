package com.getbetterhub.app.core

import android.content.Context

/**
 * Small on-device settings store (SharedPreferences). Settings live on the phone so they work
 * offline and take effect instantly; the notification toggles are also mirrored to the server
 * profile when online (see GbhRepository.saveProfile).
 */
class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("gbh_settings", Context.MODE_PRIVATE)

    var language: AppLanguage
        get() = prefs.getString(KEY_LANGUAGE, null)?.let { AppLanguage.fromTag(it) } ?: AppLanguage.fromDevice()
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value.tag).apply()

    var practiceReminders: Boolean
        get() = prefs.getBoolean(KEY_REMINDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_REMINDERS, value).apply()

    var alertNewFollowers: Boolean
        get() = prefs.getBoolean(KEY_ALERT_FOLLOWERS, true)
        set(value) = prefs.edit().putBoolean(KEY_ALERT_FOLLOWERS, value).apply()

    var alertFollowRequests: Boolean
        get() = prefs.getBoolean(KEY_ALERT_REQUESTS, true)
        set(value) = prefs.edit().putBoolean(KEY_ALERT_REQUESTS, value).apply()

    var alertRequestsApproved: Boolean
        get() = prefs.getBoolean(KEY_ALERT_APPROVED, true)
        set(value) = prefs.edit().putBoolean(KEY_ALERT_APPROVED, value).apply()

    var lastSyncMillis: Long
        get() = prefs.getLong(KEY_LAST_SYNC, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC, value).apply()

    /** Epoch-day the last practice reminder was shown, so we nudge at most once per day. */
    var lastReminderEpochDay: Long
        get() = prefs.getLong(KEY_LAST_REMINDER, -1L)
        set(value) = prefs.edit().putLong(KEY_LAST_REMINDER, value).apply()

    var unlockedAchievements: Set<String>
        get() = prefs.getStringSet(KEY_UNLOCKED, emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_UNLOCKED, value).apply()

    /** Highest server notification id already shown as a system notification (avoids repeats). */
    var lastSeenServerNotification: String?
        get() = prefs.getString(KEY_LAST_SERVER_NOTIF, null)
        set(value) = prefs.edit().putString(KEY_LAST_SERVER_NOTIF, value).apply()

    private companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_REMINDERS = "practice_reminders"
        const val KEY_ALERT_FOLLOWERS = "alert_new_followers"
        const val KEY_ALERT_REQUESTS = "alert_follow_requests"
        const val KEY_ALERT_APPROVED = "alert_requests_approved"
        const val KEY_LAST_SYNC = "last_sync_millis"
        const val KEY_LAST_REMINDER = "last_reminder_day"
        const val KEY_UNLOCKED = "unlocked_achievements"
        const val KEY_LAST_SERVER_NOTIF = "last_server_notification"
    }
}
