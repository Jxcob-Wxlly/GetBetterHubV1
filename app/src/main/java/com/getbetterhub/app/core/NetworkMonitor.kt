package com.getbetterhub.app.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Tracks whether the phone currently has a working internet connection. The UI uses it for the
 * Synced / Syncing / Offline pill, and MainActivity triggers a sync sweep the moment the phone
 * comes back online. (Being "online" only means a network exists — each API call still handles
 * its own failure, so an unreachable server just leaves items queued as pending.)
 * Reference: Android developers, "Monitor connectivity status and connection metering".
 */
class NetworkMonitor(context: Context) {
    private val manager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _online = MutableStateFlow(checkOnline())
    val online: StateFlow<Boolean> = _online

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _online.value = true
            AppLog.d("Network", "online")
        }

        override fun onLost(network: Network) {
            _online.value = checkOnline()
            AppLog.d("Network", "lost -> online=${_online.value}")
        }
    }

    init {
        try {
            manager.registerDefaultNetworkCallback(callback)
        } catch (e: Exception) {
            AppLog.w("Network", "could not register connectivity callback", e)
        }
    }

    private fun checkOnline(): Boolean {
        val network = manager.activeNetwork ?: return false
        val caps = manager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
