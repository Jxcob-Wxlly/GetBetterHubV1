package com.getbetterhub.app

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.getbetterhub.app.core.*
import com.getbetterhub.app.data.local.GbhRepository
import com.getbetterhub.app.data.local.NotificationEntity
import com.getbetterhub.app.data.remote.GbhNetworkRepository
import com.getbetterhub.app.data.remote.NetworkResult
import com.getbetterhub.app.domain.*
import com.getbetterhub.app.ui.components.*
import com.getbetterhub.app.ui.screens.*
import com.getbetterhub.app.ui.theme.GetBetterHubTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainActivity"

/**
 * Single-activity host. All screens are Composables reached via Navigation Compose.
 *
 * Offline-first: everything the user does is written to Room first and pushed to the Get Better
 * Hub API when the phone is online (see GbhRepository.syncPendingWithServer). The interface
 * language is chosen in Settings; [attachBaseContext] applies it on every launch and the activity
 * is recreated when it changes, so the whole app switches language at once.
 * Reference: Android developers, "Per-app language preferences" / Configuration.setLocale.
 */
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase, SettingsStore(newBase).language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as GetBetterHubApp
        setContent {
            GetBetterHubTheme {
                GbhApp(
                    activity = this,
                    repository = app.repository,
                    networkRepository = app.networkRepository,
                    settings = app.settings,
                    networkMonitor = app.networkMonitor
                )
            }
        }
    }
}

private object Routes {
    const val Login = "login"
    const val Register = "register"
    const val Home = "home"
    const val Practice = "practice"
    const val Mood = "mood"
    const val Lyrics = "lyrics"
    const val Journal = "journal"
    const val Arena = "arena"
    const val Profile = "profile"
    const val PublicProfile = "public_profile"
    const val Settings = "settings"
    const val Search = "search"
}

private fun routeFor(tab: GbhTab) = when (tab) {
    GbhTab.Home -> Routes.Home
    GbhTab.Practice -> Routes.Practice
    GbhTab.Mood -> Routes.Mood
    GbhTab.Lyrics -> Routes.Lyrics
    GbhTab.Journal -> Routes.Journal
    GbhTab.Arena -> Routes.Arena
}

@Composable
fun GbhApp(
    activity: ComponentActivity,
    repository: GbhRepository,
    networkRepository: GbhNetworkRepository,
    settings: SettingsStore,
    networkMonitor: NetworkMonitor
) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val audioRecorder = remember { com.getbetterhub.app.audio.AudioRecorder(context) }
    val audioPlayer = remember { com.getbetterhub.app.audio.AudioPlayer() }
    var micPermissionGranted by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micPermissionGranted = granted
        if (granted) audioRecorder.start() else Toast.makeText(context, R.string.mic_denied, Toast.LENGTH_SHORT).show()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        AppLog.i(TAG, "notification permission granted=$granted")
    }

    // ---- Settings (kept in SharedPreferences so the language applies before Compose starts) ----
    var language by remember { mutableStateOf(settings.language) }
    var practiceReminders by remember { mutableStateOf(settings.practiceReminders) }
    var alertNewFollowers by remember { mutableStateOf(settings.alertNewFollowers) }
    var alertFollowRequests by remember { mutableStateOf(settings.alertFollowRequests) }
    var alertRequestsApproved by remember { mutableStateOf(settings.alertRequestsApproved) }
    var lastSyncMillis by remember { mutableStateOf(settings.lastSyncMillis) }
    var settingsMessage by remember { mutableStateOf<String?>(null) }

    val online by networkMonitor.online.collectAsState()
    var isSyncing by remember { mutableStateOf(false) }
    var pendingCount by remember { mutableStateOf(0) }

    // ---- Auth state ----
    // isSignedIn starts true if a token is already stored, so returning users skip Login.
    var isSignedIn by remember { mutableStateOf(networkRepository.isSignedIn()) }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regError by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf(networkRepository.cachedUsername() ?: "there") }

    fun onSignedIn(name: String) {
        username = name
        isSignedIn = true
        scope.launch { repository.ensureProfile(name, networkRepository.currentUserId()) }
        AppLog.i(TAG, "signed in")
        nav.navigate(Routes.Home) { popUpTo(0) }
    }

    // ---- Google Sign-In (SSO) ----
    val googleSignInClient = remember {
        val opts = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, opts)
    }
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val idToken = task.getResult(ApiException::class.java).idToken
                if (idToken != null) {
                    scope.launch {
                        isAuthLoading = true
                        when (val res = networkRepository.googleLogin(idToken)) {
                            is NetworkResult.Success -> onSignedIn(res.data.username)
                            is NetworkResult.ApiError -> loginError = res.message
                            is NetworkResult.NetworkError -> loginError = context.getString(R.string.err_network)
                        }
                        isAuthLoading = false
                    }
                }
            } catch (e: ApiException) {
                AppLog.w(TAG, "google sign-in failed code=${e.statusCode}", e)
                loginError = context.getString(R.string.err_google_cancel) + " (code ${e.statusCode})"
            }
        } else {
            // Cancelled, or Google rejected the app (code 10 = OAuth client not set up for this build).
            val code = try {
                GoogleSignIn.getSignedInAccountFromIntent(result.data).exception.let { (it as? ApiException)?.statusCode }
            } catch (e: Exception) { null }
            loginError = context.getString(R.string.err_google_cancel) + (code?.let { " (code $it)" } ?: "")
            regError = loginError
        }
    }

    // ---- Home / gamification state — loaded from and persisted to Room via GbhRepository ----
    var dayStreak by remember { mutableStateOf(0) }
    var level by remember { mutableStateOf(1) }
    var xp by remember { mutableStateOf(0) }
    var commentsMade by remember { mutableStateOf(0) }
    var statsLoaded by remember { mutableStateOf(false) }
    val xpTargetForLevel = 250
    var lastSessionDateEpochDay by remember { mutableStateOf<Long?>(null) }

    var recentSessions by remember { mutableStateOf(listOf<String>()) }
    var sessionRecords by remember { mutableStateOf(listOf<SessionRecord>()) }
    var lyricDrafts by remember { mutableStateOf(listOf<LyricDraft>()) }
    var journalTakes by remember { mutableStateOf(listOf<JournalTake>()) }
    var arenaPosts by remember { mutableStateOf(listOf<ArenaPost>()) }
    var joinedChallengeTitles by remember { mutableStateOf(listOf<String>()) }
    var profile by remember { mutableStateOf<Profile?>(null) }
    var notifications by remember { mutableStateOf(listOf<NotificationEntity>()) }

    LaunchedEffect(Unit) {
        launch {
            repository.observeStats().collect { stats ->
                if (stats != null) {
                    username = stats.username
                    dayStreak = stats.dayStreak
                    level = stats.level
                    xp = stats.xp
                    commentsMade = stats.commentsMade
                    lastSessionDateEpochDay = stats.lastSessionDateEpochDay
                    statsLoaded = true
                }
            }
        }
        launch { repository.observeRecentSessions().collect { recentSessions = it } }
        launch { repository.observeSessionRecords().collect { sessionRecords = it } }
        launch { repository.observeLyricDrafts().collect { lyricDrafts = it } }
        launch { repository.observeJournalTakes().collect { journalTakes = it } }
        launch { repository.observeArenaPosts().collect { arenaPosts = it } }
        launch { repository.observeJoinedChallengeTitles().collect { joinedChallengeTitles = it } }
        launch { repository.observeProfile().collect { profile = it } }
        launch { repository.observeNotifications().collect { notifications = it } }
        launch { repository.observePendingCount().collect { pendingCount = it } }
    }

    // ---- Sync: pushes anything pending, pulls follower counts and server notifications ----
    suspend fun doSync() {
        if (isSyncing) return
        isSyncing = true
        try {
            repository.syncPendingWithServer(networkRepository, practiceReminders, language.tag)
            repository.pullProfile(networkRepository)
            val fresh = repository.pullServerNotifications(networkRepository)
            fresh.forEach { n ->
                val allowed = when (n.type) {
                    "new_follower" -> alertNewFollowers
                    "follow_request" -> alertFollowRequests
                    "request_approved" -> alertRequestsApproved
                    else -> true
                }
                if (allowed) {
                    Notifier.show(context, n.id.hashCode(), context.getString(R.string.app_name), notificationText(context, n))
                }
            }
            lastSyncMillis = System.currentTimeMillis()
            settings.lastSyncMillis = lastSyncMillis
        } catch (e: Exception) {
            AppLog.w("Sync", "sync attempt failed", e)
        } finally {
            isSyncing = false
        }
    }

    fun runSync() {
        scope.launch { doSync() }
    }

    // The moment the user is signed in and the phone has a connection, sync. The connectivity
    // callback fires as soon as the radio is up, which can be a moment before DNS/TLS actually
    // work, so the first attempt may fail. We therefore retry with a growing delay until nothing
    // is pending (or we give up and wait for the next reconnect / tap on the sync pill).
    LaunchedEffect(isSignedIn, online) {
        if (isSignedIn && online) {
            var attempt = 0
            while (attempt < 8) {
                kotlinx.coroutines.delay(if (attempt == 0) 1500L else 4000L * attempt)
                while (isSyncing) kotlinx.coroutines.delay(300)
                doSync()
                if (repository.pendingNow() == 0) break
                attempt++
            }
        }
    }

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            repository.ensureProfile(username, networkRepository.currentUserId())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifier.canPost(context)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(practiceReminders) {
        if (practiceReminders) ReminderWorker.schedule(context) else ReminderWorker.cancel(context)
    }

    fun persistStats() {
        scope.launch {
            repository.saveStats(
                GbhRepository.Stats(username, dayStreak, level, xp, lastSessionDateEpochDay)
            )
        }
    }

    fun addXp(amount: Int) {
        xp += amount
        if (xp >= xpTargetForLevel) { xp -= xpTargetForLevel; level += 1 }
        persistStats()
    }

    // ---- Profile / drills ----
    val currentProfile = profile ?: Profile(displayName = username, handle = "")
    val practisedInstruments = currentProfile.normalised().instruments

    var profileMessage by remember { mutableStateOf<String?>(null) }
    var handleError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(profileMessage) { if (profileMessage != null) { delay(2500); profileMessage = null } }
    LaunchedEffect(settingsMessage) { if (settingsMessage != null) { delay(2500); settingsMessage = null } }

    fun saveProfile(updated: Profile) {
        scope.launch {
            when (repository.saveProfile(updated, networkRepository, practiceReminders, language.tag)) {
                GbhRepository.SaveResult.Saved -> { handleError = null; profileMessage = context.getString(R.string.profile_saved) }
                GbhRepository.SaveResult.SavedOffline -> { handleError = null; profileMessage = context.getString(R.string.saved_offline) }
                GbhRepository.SaveResult.HandleTaken -> handleError = context.getString(R.string.handle_taken)
            }
        }
    }

    // ---- Achievements (computed live from what the user has done) ----
    val achievements = remember(sessionRecords, dayStreak, level, xp, lyricDrafts, journalTakes, arenaPosts, commentsMade, joinedChallengeTitles, practisedInstruments) {
        Achievements.computeAll(
            ProgressInputs(
                sessions = sessionRecords,
                dayStreak = dayStreak,
                totalXp = (level - 1) * xpTargetForLevel + xp,
                lyricWords = lyricDrafts.sumOf { Achievements.wordCount(it.content) },
                lyricDrafts = lyricDrafts.size,
                recordings = journalTakes.size,
                posts = arenaPosts.size,
                commentsMade = commentsMade,
                challengesJoined = joinedChallengeTitles.size,
                practisedInstruments = practisedInstruments.toSet()
            )
        )
    }
    // Raise one notification per newly unlocked achievement (remembered across launches).
    LaunchedEffect(achievements, statsLoaded) {
        if (!statsLoaded || !isSignedIn) return@LaunchedEffect
        val known = settings.unlockedAchievements
        val newlyUnlocked = achievements.filter { it.unlocked && it.def.id !in known }
        if (newlyUnlocked.isNotEmpty()) {
            settings.unlockedAchievements = known + newlyUnlocked.map { it.def.id }
            newlyUnlocked.forEach { a ->
                repository.addNotification("achievement", a.def.name, id = "ach-${a.def.id}")
                Notifier.show(context, a.def.id.hashCode(), context.getString(R.string.app_name), context.getString(R.string.notif_achievement, a.def.name))
                AppLog.i(TAG, "achievement unlocked: ${a.def.id}")
            }
        }
    }

    // ---- Practice Roulette state ----
    var currentDrill by remember { mutableStateOf<Drill?>(null) }
    var currentMood by remember { mutableStateOf<String?>(null) }
    var timerSeconds by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            timerSeconds++
        }
    }

    // ---- Mood Coach state ----
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var energyLevel by remember { mutableStateOf(3) }

    // ---- Lyric Sketchpad state ----
    var openDraft by remember { mutableStateOf<LyricDraft?>(null) }

    // ---- Soundscape Journal state ----
    var journalTitle by remember { mutableStateOf("") }
    var journalTimer by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            journalTimer++
        }
    }

    // Drives the playback scrubber for whichever take is playing, polling the real MediaPlayer position.
    val currentlyPlayingId = journalTakes.firstOrNull { it.isPlaying }?.id
    LaunchedEffect(currentlyPlayingId) {
        while (currentlyPlayingId != null && audioPlayer.isPlaying()) {
            delay(500)
            val pos = audioPlayer.currentPositionSeconds()
            journalTakes = journalTakes.map { if (it.id == currentlyPlayingId) it.copy(positionSeconds = pos) else it }
        }
    }

    // ---- Challenge Arena state ----
    var arenaTab by remember { mutableStateOf(ArenaTab.Feed) }
    var postCaption by remember { mutableStateOf("") }
    var selectedEvidenceTakeId by remember { mutableStateOf<String?>(null) }
    val arenaChallenges = listOf(
        ArenaChallenge("30 Second Hook", "Post a thirty second hook idea. Raw phone recordings welcome.", "Record a 30 second hook and share it.", 60),
        ArenaChallenge("One Take Tuesday", "No edits, no punch-ins. One take only.", "Post a single unedited take of anything you are working on.", 80),
        ArenaChallenge("Language Switch", "Perform a line in a second language.", "Sing or rap one bar in isiZulu, Afrikaans or English.", 100),
        ArenaChallenge("Minimal Kit", "Make something with three sounds or fewer.", "Post a loop built from three elements maximum.", 70)
    )
    val leaderboard = listOf(ArenaLeaderboardRow(1, username, level, xp, true))

    // ---- Search / follow state ----
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<PersonSummary>()) }
    var followRequests by remember { mutableStateOf(listOf<PersonSummary>()) }
    var isSearching by remember { mutableStateOf(false) }

    // ---- Top bar / bottom bar ----
    var bellOpen by remember { mutableStateOf(false) }
    val inbox = notifications.map { InboxItem(it.id, notificationText(context, it), it.read) }
    val syncState = when {
        !online -> SyncPillState.Offline
        isSyncing -> SyncPillState.Syncing
        pendingCount > 0 -> SyncPillState.Offline
        else -> SyncPillState.Synced
    }

    fun signOut(nav: NavHostController) {
        networkRepository.signOut()
        googleSignInClient.signOut()
        isSignedIn = false
        username = "there"
        bellOpen = false
        nav.navigate(Routes.Login) { popUpTo(0) }
    }

    fun topBar(nav: NavHostController): @Composable () -> Unit = {
        GbhTopBar(
            syncState = syncState,
            unreadCount = notifications.count { !it.read },
            inbox = inbox,
            bellOpen = bellOpen,
            onProfileClick = { nav.navigate(Routes.Profile) { launchSingleTop = true } },
            onSearchClick = { nav.navigate(Routes.Search) { launchSingleTop = true } },
            onBellClick = { bellOpen = !bellOpen },
            onBellDismiss = { bellOpen = false; scope.launch { repository.markNotificationsRead() } },
            onOpenSettings = {
                bellOpen = false
                scope.launch { repository.markNotificationsRead() }
                nav.navigate(Routes.Settings) { launchSingleTop = true }
            },
            onLogout = { signOut(nav) }
        )
    }

    fun bottomBar(nav: NavHostController, current: GbhTab?): @Composable () -> Unit = {
        GbhBottomNav(current = current, onSelect = { tab -> nav.navigate(routeFor(tab)) { launchSingleTop = true } })
    }

    fun greeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return context.getString(when {
            hour < 12 -> R.string.good_morning
            hour < 18 -> R.string.good_afternoon
            else -> R.string.good_evening
        })
    }

    NavHost(navController = nav, startDestination = if (isSignedIn) Routes.Home else Routes.Login) {

        composable(Routes.Login) {
            LoginScreen(
                email = loginEmail,
                onEmailChange = { loginEmail = it },
                password = loginPassword,
                onPasswordChange = { loginPassword = it },
                onSignIn = {
                    loginError = null
                    scope.launch {
                        isAuthLoading = true
                        when (val res = networkRepository.login(loginEmail, loginPassword)) {
                            is NetworkResult.Success -> onSignedIn(res.data.username)
                            is NetworkResult.ApiError -> loginError = res.message
                            is NetworkResult.NetworkError -> loginError = context.getString(R.string.err_network)
                        }
                        isAuthLoading = false
                    }
                },
                onContinueWithGoogle = { googleLauncher.launch(googleSignInClient.signInIntent) },
                onGoToRegister = { loginError = null; nav.navigate(Routes.Register) },
                errorMessage = loginError,
                isLoading = isAuthLoading
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                displayName = regName,
                onDisplayNameChange = { regName = it },
                email = regEmail,
                onEmailChange = { regEmail = it },
                password = regPassword,
                onPasswordChange = { regPassword = it },
                onCreateAccount = {
                    regError = null
                    scope.launch {
                        isAuthLoading = true
                        when (val res = networkRepository.register(
                            username = regName.ifBlank { regEmail.substringBefore("@") },
                            email = regEmail,
                            password = regPassword,
                            preferredLanguage = language.tag,
                            primaryInstrument = null
                        )) {
                            is NetworkResult.Success -> onSignedIn(res.data.username)
                            is NetworkResult.ApiError -> regError = res.message
                            is NetworkResult.NetworkError -> regError = context.getString(R.string.err_network)
                        }
                        isAuthLoading = false
                    }
                },
                onContinueWithGoogle = { googleLauncher.launch(googleSignInClient.signInIntent) },
                onGoToSignIn = { regError = null; nav.navigate(Routes.Login) },
                errorMessage = regError,
                isLoading = isAuthLoading
            )
        }

        composable(Routes.Home) {
            HomeScreen(
                greeting = greeting(),
                username = username,
                dayStreak = dayStreak,
                level = level,
                xp = xp,
                xpTargetForLevel = xpTargetForLevel,
                recentSessions = recentSessions,
                onJumpBackIn = { tab -> nav.navigate(routeFor(tab)) },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, GbhTab.Home)
            )
        }

        composable(Routes.Practice) {
            PracticeRouletteScreen(
                drill = currentDrill,
                practisedInstruments = practisedInstruments,
                primaryInstrument = currentProfile.primaryInstrument,
                timerSeconds = timerSeconds,
                isTimerRunning = isTimerRunning,
                exercises = sessionRecords.groupBy { it.label }
                    .map { (label, rows) -> ExerciseRow(label, rows.sumOf { it.durationSeconds }) }
                    .sortedByDescending { it.durationSeconds },
                recentSessions = sessionRecords,
                noDrillsMessage = DrillLibrary.pool(practisedInstruments, currentProfile.level).isEmpty(),
                onSpin = {
                    currentDrill = DrillLibrary.spin(practisedInstruments, currentProfile.level, currentDrill)
                    currentMood = null
                    timerSeconds = 0
                    isTimerRunning = false
                },
                onStartTimer = { isTimerRunning = !isTimerRunning },
                onMarkComplete = {
                    currentDrill?.let { drill ->
                        val today = java.time.LocalDate.now().toEpochDay()
                        dayStreak = when (lastSessionDateEpochDay) {
                            null -> 1
                            today -> dayStreak // already practised today — streak unchanged
                            today - 1 -> dayStreak + 1 // consecutive day
                            else -> 1 // streak broken, restart
                        }
                        lastSessionDateEpochDay = today
                        addXp(20)
                        scope.launch {
                            repository.addRecentSession(
                                label = drill.title,
                                instrument = drill.instrument,
                                drillType = drill.title,
                                mood = currentMood,
                                durationSeconds = timerSeconds,
                                network = networkRepository
                            )
                        }
                        timerSeconds = 0
                    }
                    isTimerRunning = false
                },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, GbhTab.Practice)
            )
        }

        composable(Routes.Mood) {
            MoodCoachScreen(
                selectedMood = selectedMood,
                onMoodSelected = { selectedMood = it },
                energyLevel = energyLevel,
                onEnergyLevelChange = { energyLevel = it },
                onGetPlan = {
                    selectedMood?.let { mood ->
                        currentDrill = DrillLibrary.forMood(mood, energyLevel, practisedInstruments, currentProfile.level)
                        currentMood = mood
                        timerSeconds = 0
                        isTimerRunning = false
                        nav.navigate(Routes.Practice)
                    }
                },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, GbhTab.Mood)
            )
        }

        composable(Routes.Lyrics) {
            LyricSketchpadScreen(
                drafts = lyricDrafts,
                openDraft = openDraft,
                onNewDraft = { openDraft = LyricDraft(id = java.util.UUID.randomUUID().toString(), title = "", content = "", synced = false) },
                onOpenDraft = { openDraft = it },
                onTitleChange = { openDraft = openDraft?.copy(title = it) },
                onContentChange = { openDraft = openDraft?.copy(content = it) },
                onSave = {
                    openDraft?.let { draft ->
                        scope.launch { repository.saveLyricDraft(draft.copy(synced = true), networkRepository) }
                        openDraft = null
                    }
                },
                onDelete = { draft -> scope.launch { repository.deleteLyricDraft(draft, networkRepository) }; openDraft = null },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, GbhTab.Lyrics)
            )
        }

        composable(Routes.Journal) {
            SoundscapeJournalScreen(
                titleDraft = journalTitle,
                onTitleChange = { journalTitle = it },
                timerSeconds = journalTimer,
                isRecording = isRecording,
                onToggleRecord = {
                    if (isRecording) {
                        val file = audioRecorder.stop()
                        val take = JournalTake(
                            id = java.util.UUID.randomUUID().toString(),
                            title = journalTitle.ifBlank { context.getString(R.string.untitled_take) },
                            synced = false,
                            positionSeconds = 0,
                            durationSeconds = journalTimer,
                            isPlaying = false,
                            localFilePath = file?.absolutePath
                        )
                        scope.launch { repository.addJournalTake(take, file, networkRepository) }
                        journalTitle = ""
                        journalTimer = 0
                        isRecording = false
                    } else if (micPermissionGranted) {
                        audioRecorder.start()
                        isRecording = true
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                takes = journalTakes,
                onTogglePlay = { take ->
                    if (take.isPlaying) {
                        audioPlayer.stop()
                        journalTakes = journalTakes.map { if (it.id == take.id) it.copy(isPlaying = false) else it }
                    } else if (take.localFilePath != null) {
                        journalTakes = journalTakes.map { it.copy(isPlaying = it.id == take.id) }
                        audioPlayer.play(take.localFilePath) {
                            journalTakes = journalTakes.map { if (it.id == take.id) it.copy(isPlaying = false, positionSeconds = 0) else it }
                        }
                    }
                },
                onDelete = { take ->
                    if (take.isPlaying) audioPlayer.stop()
                    take.localFilePath?.let { java.io.File(it).delete() }
                    scope.launch { repository.deleteJournalTake(take) }
                },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, GbhTab.Journal)
            )
        }

        composable(Routes.Arena) {
            val syncedTakes = journalTakes.filter { it.synced && it.audioUrl != null }
            val selectedLabel = syncedTakes.firstOrNull { it.id == selectedEvidenceTakeId }?.title
                ?: context.getString(R.string.no_linked_session)
            ChallengeArenaScreen(
                currentTab = arenaTab,
                onTabSelected = { arenaTab = it },
                caption = postCaption,
                onCaptionChange = { postCaption = it },
                selectedChallengeLabel = context.getString(R.string.no_challenge),
                selectedSessionLabel = selectedLabel,
                onCycleEvidence = {
                    // Cycles none → take 1 → take 2 → … → none.
                    val ids = syncedTakes.map { it.id }
                    selectedEvidenceTakeId = if (ids.isEmpty()) null else {
                        val next = ids.indexOf(selectedEvidenceTakeId) + 1
                        if (next >= ids.size) null else ids[next]
                    }
                },
                onSharePost = {
                    if (postCaption.isNotBlank()) {
                        val evidenceUrl = syncedTakes.firstOrNull { it.id == selectedEvidenceTakeId }?.audioUrl
                        scope.launch {
                            repository.addArenaPost(
                                ArenaPost(username, emptyList(), postCaption, 0, 0),
                                evidenceAudioUrl = evidenceUrl,
                                network = networkRepository
                            )
                        }
                        postCaption = ""
                        selectedEvidenceTakeId = null
                    }
                },
                posts = arenaPosts,
                challenges = arenaChallenges,
                onJoinChallenge = { challenge ->
                    if (challenge.title !in joinedChallengeTitles) {
                        addXp(challenge.xpReward)
                        scope.launch { repository.markChallengeJoined(challenge.title) }
                    }
                },
                leaderboard = leaderboard,
                achievements = achievements,
                onComment = { post, _ -> scope.launch { repository.addComment(post.id) } },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, GbhTab.Arena)
            )
        }

        composable(Routes.Profile) {
            ProfileScreen(
                profile = currentProfile,
                stats = ProfileStats(dayStreak, level, xp, xpTargetForLevel, sessionRecords.size, joinedChallengeTitles.size),
                achievements = achievements,
                exercises = sessionRecords.groupBy { it.label }
                    .map { (label, rows) -> ExerciseRow(label, rows.sumOf { it.durationSeconds }) }
                    .sortedByDescending { it.durationSeconds },
                message = profileMessage,
                handleError = handleError,
                onUpdate = { saveProfile(it) },
                onSaveHandle = { handle ->
                    val clean = handle.trim().lowercase().removePrefix("@")
                    if (!Profile.isValidHandle(clean)) handleError = context.getString(R.string.handle_invalid)
                    else saveProfile(currentProfile.copy(handle = clean))
                },
                onBannerPicked = { uri ->
                    scope.launch {
                        val path = withContext(Dispatchers.IO) { ImageUtils.copyToPrivateStorage(context, uri, "banner") }
                        if (path != null) {
                            ImageUtils.delete(currentProfile.bannerPath)
                            saveProfile(currentProfile.copy(bannerPath = path))
                        }
                    }
                },
                onAvatarPicked = { uri ->
                    scope.launch {
                        val path = withContext(Dispatchers.IO) { ImageUtils.copyToPrivateStorage(context, uri, "avatar") }
                        if (path != null) {
                            ImageUtils.delete(currentProfile.avatarPath)
                            saveProfile(currentProfile.copy(avatarPath = path))
                        }
                    }
                },
                onRemovePhoto = {
                    ImageUtils.delete(currentProfile.avatarPath)
                    saveProfile(currentProfile.copy(avatarPath = null))
                },
                onViewPublicProfile = { nav.navigate(Routes.PublicProfile) },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, null)
            )
        }

        composable(Routes.PublicProfile) {
            PublicProfileScreen(
                profile = currentProfile,
                level = level,
                posts = arenaPosts.filter { it.author == username },
                badgeEmojis = achievements.filter { it.unlocked }.map { it.def.emoji },
                onEditProfile = { nav.popBackStack() },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, null)
            )
        }

        composable(Routes.Settings) {
            SettingsScreen(
                state = SettingsState(
                    language = language,
                    practiceReminders = practiceReminders,
                    alertNewFollowers = alertNewFollowers,
                    alertFollowRequests = alertFollowRequests,
                    alertRequestsApproved = alertRequestsApproved,
                    isOnline = online,
                    pendingCount = pendingCount,
                    lastSyncMillis = lastSyncMillis
                ),
                message = settingsMessage,
                onLanguageChange = { picked ->
                    if (picked != language) {
                        settings.language = picked
                        language = picked
                        AppLog.i(TAG, "language -> ${picked.tag}")
                        activity.recreate() // rebuilds every screen with the new resources
                    }
                },
                onRemindersChange = { practiceReminders = it; settings.practiceReminders = it; settingsMessage = context.getString(R.string.settings_saved) },
                onAlertNewFollowers = { alertNewFollowers = it; settings.alertNewFollowers = it },
                onAlertFollowRequests = { alertFollowRequests = it; settings.alertFollowRequests = it },
                onAlertRequestsApproved = { alertRequestsApproved = it; settings.alertRequestsApproved = it },
                onTestNotification = {
                    if (!Notifier.canPost(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        scope.launch { repository.addNotification("test", "") }
                        Notifier.show(context, 2001, context.getString(R.string.app_name), context.getString(R.string.notif_test_body))
                    }
                },
                onSyncNow = { runSync() },
                onSignOut = { signOut(nav) },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, null)
            )
        }

        composable(Routes.Search) {
            // Follow requests are loaded once when the screen opens; search is debounced.
            LaunchedEffect(online) {
                if (online) {
                    val res = networkRepository.followRequests()
                    if (res is NetworkResult.Success) followRequests = res.data.mapNotNull { it.toSummary() }
                }
            }
            LaunchedEffect(searchQuery, online) {
                if (!online || searchQuery.isBlank()) { searchResults = emptyList(); return@LaunchedEffect }
                delay(300)
                isSearching = true
                val res = networkRepository.searchPeople(searchQuery.trim())
                if (res is NetworkResult.Success) searchResults = res.data.mapNotNull { it.toSummary() }
                isSearching = false
            }
            fun setRelationship(person: PersonSummary, relationship: String) {
                searchResults = searchResults.map { if (it.userId == person.userId) it.copy(relationship = relationship) else it }
            }
            SearchScreen(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                results = searchResults,
                requests = followRequests,
                isOnline = online,
                isLoading = isSearching,
                onFollow = { person ->
                    scope.launch {
                        val res = networkRepository.follow(person.userId)
                        if (res is NetworkResult.Success) setRelationship(person, if (res.data.status == "requested") "requested" else "following")
                    }
                },
                onUnfollow = { person ->
                    scope.launch {
                        if (networkRepository.unfollow(person.userId) !is NetworkResult.NetworkError) setRelationship(person, "none")
                    }
                },
                onApprove = { person ->
                    scope.launch {
                        if (networkRepository.approveRequest(person.userId) !is NetworkResult.NetworkError) {
                            followRequests = followRequests.filter { it.userId != person.userId }
                        }
                    }
                },
                onDecline = { person ->
                    scope.launch {
                        if (networkRepository.declineRequest(person.userId) !is NetworkResult.NetworkError) {
                            followRequests = followRequests.filter { it.userId != person.userId }
                        }
                    }
                },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, null)
            )
        }
    }
}

private fun com.getbetterhub.app.data.remote.PersonResponse.toSummary(): PersonSummary? {
    val id = userId ?: return null
    return PersonSummary(id, displayName ?: "?", handle ?: "", level ?: 1, isPrivate ?: false, relationship ?: "none")
}

/** Turns a stored notification into text in the app's current language. */
private fun notificationText(context: Context, n: NotificationEntity): String = when (n.type) {
    "new_follower" -> context.getString(R.string.notif_new_follower, n.arg)
    "follow_request" -> context.getString(R.string.notif_follow_request, n.arg)
    "request_approved" -> context.getString(R.string.notif_request_approved, n.arg)
    "achievement" -> context.getString(R.string.notif_achievement, n.arg)
    "reminder" -> context.getString(R.string.notif_reminder_body)
    "test" -> context.getString(R.string.notif_test_body)
    else -> n.arg
}
