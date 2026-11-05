package com.getbetterhub.app.core

import android.util.Log
import com.getbetterhub.app.BuildConfig

/**
 * Thin logging wrapper so the whole app logs consistently (one tag prefix, debug output only in
 * debug builds). View with:  adb logcat -s GBH
 * Never log passwords, tokens or other personal data through this class.
 */
object AppLog {
    private const val PREFIX = "GBH"

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(PREFIX, "[$tag] $message")
    }

    fun i(tag: String, message: String) {
        Log.i(PREFIX, "[$tag] $message")
    }

    fun w(tag: String, message: String, error: Throwable? = null) {
        if (error != null) Log.w(PREFIX, "[$tag] $message", error) else Log.w(PREFIX, "[$tag] $message")
    }

    fun e(tag: String, message: String, error: Throwable? = null) {
        if (error != null) Log.e(PREFIX, "[$tag] $message", error) else Log.e(PREFIX, "[$tag] $message")
    }
}
