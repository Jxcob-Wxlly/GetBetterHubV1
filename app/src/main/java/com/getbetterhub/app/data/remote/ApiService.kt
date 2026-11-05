package com.getbetterhub.app.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Matches the endpoints exposed by GetBetterHub.Api's controllers one-for-one. Every method
 * other than the three auth calls requires a valid JWT — AuthInterceptor (see NetworkModule)
 * attaches it automatically, so callers never pass a token explicitly.
 */
interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthResponse>

    @GET("api/users/me")
    suspend fun getMyProfile(): Response<UserProfileResponse>

    @PUT("api/users/me")
    suspend fun updateMyProfile(@Body request: UpdateProfileRequest): Response<UserProfileResponse>

    @GET("api/people/search")
    suspend fun searchPeople(@Query("q") query: String): Response<List<PersonResponse>>

    @POST("api/follows/{userId}")
    suspend fun follow(@Path("userId") userId: String): Response<FollowResponse>

    @DELETE("api/follows/{userId}")
    suspend fun unfollow(@Path("userId") userId: String): Response<Unit>

    @GET("api/follows/requests")
    suspend fun getFollowRequests(): Response<List<PersonResponse>>

    @POST("api/follows/requests/{userId}/approve")
    suspend fun approveRequest(@Path("userId") userId: String): Response<Unit>

    @POST("api/follows/requests/{userId}/decline")
    suspend fun declineRequest(@Path("userId") userId: String): Response<Unit>

    @GET("api/notifications")
    suspend fun getNotifications(): Response<List<ServerNotificationResponse>>

    @GET("api/practice-sessions")
    suspend fun getPracticeSessions(): Response<List<PracticeSessionResponse>>

    @POST("api/practice-sessions")
    suspend fun createPracticeSession(@Body request: CreatePracticeSessionRequest): Response<PracticeSessionResponse>

    @GET("api/lyric-drafts")
    suspend fun getLyricDrafts(): Response<List<LyricDraftResponse>>

    @PUT("api/lyric-drafts/{id}")
    suspend fun saveLyricDraft(@Path("id") id: String, @Body request: SaveLyricDraftRequest): Response<LyricDraftResponse>

    @DELETE("api/lyric-drafts/{id}")
    suspend fun deleteLyricDraft(@Path("id") id: String): Response<Unit>

    @GET("api/performance-posts")
    suspend fun getPerformancePosts(): Response<List<PerformancePostResponse>>

    @POST("api/performance-posts")
    suspend fun createPerformancePost(@Body request: CreatePerformancePostRequest): Response<PerformancePostResponse>

    @POST("api/performance-posts/{id}/like")
    suspend fun likePost(@Path("id") id: String): Response<Unit>

    @GET("api/performance-posts/{id}/comments")
    suspend fun getComments(@Path("id") id: String): Response<List<CommentResponse>>

    @POST("api/performance-posts/{id}/comments")
    suspend fun addComment(@Path("id") id: String, @Body request: CreateCommentRequest): Response<CommentResponse>

    @GET("api/journal-entries")
    suspend fun getJournalEntries(): Response<List<JournalEntryResponse>>

    @POST("api/journal-entries")
    suspend fun createJournalEntry(@Body request: CreateJournalEntryRequest): Response<JournalEntryResponse>

    @DELETE("api/journal-entries/{id}")
    suspend fun deleteJournalEntry(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("api/uploads/audio")
    suspend fun uploadAudio(@Part file: MultipartBody.Part): Response<UploadResponse>
}
