package com.getbetterhub.app.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * The API address comes from BuildConfig.API_BASE_URL (set in app/build.gradle.kts, overridable with
 * -PapiBaseUrl=...). Default for debug builds is http://localhost:5080/, which works on a physical
 * phone after `adb reverse tcp:5080 tcp:5080` (and on the emulator the same way).
 */
object NetworkModule {

    fun create(tokenStore: TokenStore, isDebugBuild: Boolean, baseUrl: String = com.getbetterhub.app.BuildConfig.API_BASE_URL): ApiService {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = tokenStore.token
            val request = if (token != null) {
                original.newBuilder().addHeader("Authorization", "Bearer $token").build()
            } else {
                original
            }
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebugBuild) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(45, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }
}
