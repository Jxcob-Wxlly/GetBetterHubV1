package com.getbetterhub.app.data.remote

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.IOException

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class ApiError(val code: Int, val message: String) : NetworkResult<Nothing>()
    data class NetworkError(val message: String) : NetworkResult<Nothing>()
}

/**
 * Wraps every ApiService call so the rest of the app never has to deal with raw Retrofit
 * exceptions or unwrap a Response object — every call comes back as a NetworkResult that's
 * either a typed success, a server-returned error (bad password, duplicate email, etc.), or
 * a connectivity failure (API unreachable — the expected case when offline).
 */
class GbhNetworkRepository(private val api: ApiService, private val tokenStore: TokenStore) {

    private suspend fun <T> safeCall(block: suspend () -> retrofit2.Response<T>): NetworkResult<T> {
        return try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                NetworkResult.Success(body)
            } else {
                val errorMsg = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    ?: "Request failed (${response.code()})"
                NetworkResult.ApiError(response.code(), errorMsg)
            }
        } catch (e: IOException) {
            // No connection, DNS failure, timeout, emulator can't reach 10.0.2.2, etc. — this is
            // the "we're offline" case callers should treat as non-fatal and fall back to local data.
            NetworkResult.NetworkError(e.message ?: "Network unavailable")
        } catch (e: Exception) {
            NetworkResult.NetworkError(e.message ?: "Unexpected error")
        }
    }

    private fun rememberSession(auth: AuthResponse) {
        tokenStore.token = auth.token
        tokenStore.userId = auth.userId
        tokenStore.username = auth.username
    }

    suspend fun register(username: String, email: String, password: String, preferredLanguage: String, primaryInstrument: String?): NetworkResult<AuthResponse> {
        val result = safeCall { api.register(RegisterRequest(username, email, password, preferredLanguage, primaryInstrument)) }
        if (result is NetworkResult.Success) rememberSession(result.data)
        return result
    }

    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        val result = safeCall { api.login(LoginRequest(email, password)) }
        if (result is NetworkResult.Success) rememberSession(result.data)
        return result
    }

    suspend fun googleLogin(idToken: String): NetworkResult<AuthResponse> {
        val result = safeCall { api.googleLogin(GoogleLoginRequest(idToken)) }
        if (result is NetworkResult.Success) rememberSession(result.data)
        return result
    }

    suspend fun syncPracticeSession(instrument: String, drillType: String?, mood: String?, durationSeconds: Int): NetworkResult<PracticeSessionResponse> =
        safeCall { api.createPracticeSession(CreatePracticeSessionRequest(instrument, drillType, mood, durationSeconds)) }

    suspend fun syncJournalEntry(audioUrl: String? = null, note: String?): NetworkResult<JournalEntryResponse> =
        safeCall { api.createJournalEntry(CreateJournalEntryRequest(audioUrl, note)) }

    /** Uploads a local audio file and returns the URL it's now reachable at, or null on failure
     *  (offline, or the file didn't survive validation) — callers decide how to handle that. */
    suspend fun uploadAudio(file: java.io.File): String? {
        val requestBody = file.asRequestBody("audio/mp4".toMediaTypeOrNull())
        val part = okhttp3.MultipartBody.Part.createFormData("file", file.name, requestBody)
        return when (val result = safeCall { api.uploadAudio(part) }) {
            is NetworkResult.Success -> result.data.url
            else -> null
        }
    }

    suspend fun saveLyricDraft(id: String, title: String?, content: String): NetworkResult<LyricDraftResponse> =
        safeCall { api.saveLyricDraft(id, SaveLyricDraftRequest(title, content)) }

    suspend fun deleteLyricDraft(id: String): NetworkResult<Unit> =
        safeCall { api.deleteLyricDraft(id) }

    suspend fun createPerformancePost(audioUrl: String, challengeId: String?, caption: String?): NetworkResult<PerformancePostResponse> =
        safeCall { api.createPerformancePost(CreatePerformancePostRequest(audioUrl, challengeId, caption)) }

    suspend fun likePost(id: String): NetworkResult<Unit> =
        safeCall { api.likePost(id) }

    // ---- Profile / people / notifications (final build) ----
    suspend fun fetchMyProfile(): NetworkResult<UserProfileResponse> = safeCall { api.getMyProfile() }

    suspend fun pushProfile(request: UpdateProfileRequest): NetworkResult<UserProfileResponse> =
        safeCall { api.updateMyProfile(request) }

    suspend fun searchPeople(query: String): NetworkResult<List<PersonResponse>> =
        safeCall { api.searchPeople(query) }

    suspend fun follow(userId: String): NetworkResult<FollowResponse> = safeCall { api.follow(userId) }

    suspend fun unfollow(userId: String): NetworkResult<Unit> = safeCall { api.unfollow(userId) }

    suspend fun followRequests(): NetworkResult<List<PersonResponse>> = safeCall { api.getFollowRequests() }

    suspend fun approveRequest(userId: String): NetworkResult<Unit> = safeCall { api.approveRequest(userId) }

    suspend fun declineRequest(userId: String): NetworkResult<Unit> = safeCall { api.declineRequest(userId) }

    suspend fun serverNotifications(): NetworkResult<List<ServerNotificationResponse>> =
        safeCall { api.getNotifications() }

    fun currentUserId(): String? = tokenStore.userId

    fun signOut() = tokenStore.clear()

    fun isSignedIn(): Boolean = tokenStore.token != null

    /** The username saved at the last successful login/register — used to greet a returning
     *  user immediately on app launch, before any network round-trip has happened. */
    fun cachedUsername(): String? = tokenStore.username
}
