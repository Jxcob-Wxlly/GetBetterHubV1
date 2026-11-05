package com.getbetterhub.app

import android.app.Application
import com.getbetterhub.app.core.NetworkMonitor
import com.getbetterhub.app.core.Notifier
import com.getbetterhub.app.core.SettingsStore
import com.getbetterhub.app.data.local.GbhDatabase
import com.getbetterhub.app.data.local.GbhRepository
import com.getbetterhub.app.data.remote.GbhNetworkRepository
import com.getbetterhub.app.data.remote.NetworkModule
import com.getbetterhub.app.data.remote.TokenStore

/**
 * App entry point. Builds the Room database (local persistence) and the Retrofit-backed
 * network repository (calls to the Get Better Hub API) once, and exposes both as singletons —
 * MainActivity reads/writes through these rather than constructing them itself.
 */
class GetBetterHubApp : Application() {
    lateinit var repository: GbhRepository
        private set
    lateinit var networkRepository: GbhNetworkRepository
        private set
    lateinit var tokenStore: TokenStore
        private set
    lateinit var settings: SettingsStore
        private set
    lateinit var networkMonitor: NetworkMonitor
        private set

    override fun onCreate() {
        super.onCreate()
        settings = SettingsStore(this)
        networkMonitor = NetworkMonitor(this)
        Notifier.ensureChannel(this)

        val db = GbhDatabase.getInstance(this)
        repository = GbhRepository(db)

        tokenStore = TokenStore(this)
        val apiService = NetworkModule.create(tokenStore, isDebugBuild = BuildConfig.DEBUG)
        networkRepository = GbhNetworkRepository(apiService, tokenStore)
    }
}


