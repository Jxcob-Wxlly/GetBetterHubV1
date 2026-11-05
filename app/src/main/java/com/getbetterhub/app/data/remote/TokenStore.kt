package com.getbetterhub.app.data.remote

import android.content.Context

/**
 * Persists the signed-in JWT in a private SharedPreferences file so the user doesn't have to
 * log in again every time the app process restarts. This is fine for a student project; a
 * production app would use EncryptedSharedPreferences (androidx.security:security-crypto)
 * instead, since plain SharedPreferences is readable on a rooted device.
 */
class TokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("gbh_auth", Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_TOKEN, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) = prefs.edit().putString(KEY_USERNAME, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
    }
}
