package com.getbetterhub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.getbetterhub.app.ui.components.GbhBottomNav
import com.getbetterhub.app.ui.components.GbhTab
import com.getbetterhub.app.ui.components.GbhTopBar
import com.getbetterhub.app.ui.screens.*
import com.getbetterhub.app.ui.theme.GetBetterHubTheme
import kotlinx.coroutines.delay

/**
 * Single-activity host. All screens are Composables reached via Navigation Compose.
 * State here is in-memory only (mutableStateOf) as a stand-in for the real Room/Retrofit
 * data layer that Part 2 will wire in — every screen already takes plain data + callbacks,
 * so swapping this for a real ViewModel later is a drop-in change, not a rewrite.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GetBetterHubTheme {
                GbhApp()
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
}

@Composable
fun GbhApp() {
    val nav = rememberNavController()

    // ---- Auth state ----
    var isSignedIn by remember { mutableStateOf(false) }
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("there") }

    // ---- Home / gamification state ----
    var dayStreak by remember { mutableStateOf(0) }
    var level by remember { mutableStateOf(1) }
    var xp by remember { mutableStateOf(0) }
    val xpTargetForLevel = 250
    var recentSessions by remember { mutableStateOf(listOf<String>()) }

    // ---- Practice Roulette state ----
    var currentDrill by remember { mutableStateOf<Drill?>(null) }
    var timerSeconds by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }
    val drillPool = listOf(
        Drill("VOCALS · BEGINNER · WARMUP", "Lip Trill Ladder", "Trill up and down a five-note scale, staying relaxed through the passaggio.", 5),
        Drill("GUITAR · INTERMEDIATE · TECHNIQUE", "Pentatonic Run", "Play the A minor pentatonic box shape ascending and descending at a steady tempo.", 8),
        Drill("PIANO · BEGINNER · WARMUP", "Five-Finger Scale", "Play a five-finger scale hands separately, then hands together, staying relaxed.", 6)
    )

    // Practice timer counts up once per second while running, matching the mockup's live 0:00 → 0:07 behaviour.
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
    var lyricDrafts by remember { mutableStateOf(listOf<LyricDraft>()) }
    var openDraft by remember { mutableStateOf<LyricDraft?>(null) }

    // ---- Soundscape Journal state ----
    var journalTitle by remember { mutableStateOf("") }
    var journalTimer by remember { mutableStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var journalTakes by remember { mutableStateOf(listOf<JournalTake>()) }

    // Recording timer counts up once per second while active, matching the mockup's 0:00 → 0:07 behaviour.
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            journalTimer++
        }
    }

    // Playback position for whichever take currently has isPlaying = true, ticking up to its own duration.
    val playingTakeId = journalTakes.firstOrNull { it.isPlaying }?.id
    LaunchedEffect(playingTakeId) {
        if (playingTakeId != null) {
            val duration = journalTakes.first { it.id == playingTakeId }.durationSeconds
            var pos = journalTakes.first { it.id == playingTakeId }.positionSeconds
            while (pos < duration) {
                delay(1000)
                pos++
                journalTakes = journalTakes.map { if (it.id == playingTakeId) it.copy(positionSeconds = pos) else it }
            }
            // Playback finished — reset to the start and stop.
            journalTakes = journalTakes.map { if (it.id == playingTakeId) it.copy(positionSeconds = 0, isPlaying = false) else it }
        }
    }

    // ---- Challenge Arena state ----
    var arenaTab by remember { mutableStateOf(ArenaTab.Feed) }
    var postCaption by remember { mutableStateOf("") }
    var arenaPosts by remember { mutableStateOf(listOf<ArenaPost>()) }
    val arenaChallenges = listOf(
        ArenaChallenge("30 Second Hook", "Post a thirty second hook idea. Raw phone recordings welcome.", "Record a 30 second hook and share it.", 60),
        ArenaChallenge("One Take Tuesday", "No edits, no punch-ins. One take only.", "Post a single unedited take of anything you are working on.", 80),
        ArenaChallenge("Language Switch", "Perform a line in a second language.", "Sing or rap one bar in isiZulu, Afrikaans or English.", 100),
        ArenaChallenge("Minimal Kit", "Make something with three sounds or fewer.", "Post a loop built from three elements maximum.", 70)
    )
    val leaderboard = listOf(ArenaLeaderboardRow(1, username, level, xp, true))
    val badges = listOf(
        ArenaBadge("First Steps", "Complete your first practice session.", false),
        ArenaBadge("Three Day Streak", "Practise three days in a row.", false),
        ArenaBadge("Rookie", "Earn 100 XP.", false),
        ArenaBadge("Regular", "Earn 500 XP.", false)
    )

    fun greeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    fun topBar(nav: NavHostController): @Composable () -> Unit = {
        GbhTopBar(synced = true, onLogout = {
            isSignedIn = false
            nav.navigate(Routes.Login) { popUpTo(0) }
        })
    }

    fun bottomBar(nav: NavHostController, current: GbhTab): @Composable () -> Unit = {
        GbhBottomNav(current = current, onSelect = { tab ->
            val route = when (tab) {
                GbhTab.Home -> Routes.Home
                GbhTab.Practice -> Routes.Practice
                GbhTab.Mood -> Routes.Mood
                GbhTab.Lyrics -> Routes.Lyrics
                GbhTab.Journal -> Routes.Journal
                GbhTab.Arena -> Routes.Arena
            }
            nav.navigate(route) { launchSingleTop = true }
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
                    username = loginEmail.substringBefore("@").ifBlank { "there" }
                    isSignedIn = true
                    nav.navigate(Routes.Home) { popUpTo(0) }
                },
                onContinueWithGoogle = {
                    isSignedIn = true
                    nav.navigate(Routes.Home) { popUpTo(0) }
                },
                onGoToRegister = { nav.navigate(Routes.Register) }
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
                    username = regName.ifBlank { regEmail.substringBefore("@") }.ifBlank { "there" }
                    isSignedIn = true
                    nav.navigate(Routes.Home) { popUpTo(0) }
                },
                onContinueWithGoogle = {
                    isSignedIn = true
                    nav.navigate(Routes.Home) { popUpTo(0) }
                },
                onGoToSignIn = { nav.navigate(Routes.Login) }
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
                onJumpBackIn = { tab ->
                    val route = when (tab) {
                        GbhTab.Practice -> Routes.Practice
                        GbhTab.Mood -> Routes.Mood
                        GbhTab.Lyrics -> Routes.Lyrics
                        GbhTab.Journal -> Routes.Journal
                        GbhTab.Arena -> Routes.Arena
                        GbhTab.Home -> Routes.Home
                    }
                    nav.navigate(route)
                },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, GbhTab.Home)
            )
        }

        composable(Routes.Practice) {
            PracticeRouletteScreen(
                drill = currentDrill,
                timerSeconds = timerSeconds,
                isTimerRunning = isTimerRunning,
                recentSessions = recentSessions,
                onSpin = { currentDrill = drillPool.random(); timerSeconds = 0; isTimerRunning = false },
                onStartTimer = { isTimerRunning = !isTimerRunning },
                onMarkComplete = {
                    currentDrill?.let { drill ->
                        recentSessions = listOf("${drill.title} — completed") + recentSessions
                        xp += 20
                        dayStreak = if (dayStreak == 0) 1 else dayStreak
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
                    if (selectedMood != null) {
                        currentDrill = drillPool.random()
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
                onNewDraft = {
                    val draft = LyricDraft(id = java.util.UUID.randomUUID().toString(), title = "", content = "", synced = false)
                    openDraft = draft
                },
                onOpenDraft = { openDraft = it },
                onTitleChange = { openDraft = openDraft?.copy(title = it) },
                onContentChange = { openDraft = openDraft?.copy(content = it) },
                onSave = {
                    openDraft?.let { draft ->
                        val synced = draft.copy(synced = true)
                        lyricDrafts = if (lyricDrafts.any { it.id == draft.id }) {
                            lyricDrafts.map { if (it.id == draft.id) synced else it }
                        } else {
                            listOf(synced) + lyricDrafts
                        }
                        openDraft = null
                    }
                },
                onDelete = { draft -> lyricDrafts = lyricDrafts.filterNot { it.id == draft.id }; openDraft = null },
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
                        journalTakes = listOf(
                            JournalTake(
                                id = java.util.UUID.randomUUID().toString(),
                                title = journalTitle.ifBlank { "Untitled take" },
                                synced = false,
                                positionSeconds = 0,
                                durationSeconds = journalTimer,
                                isPlaying = false
                            )
                        ) + journalTakes
                        journalTitle = ""
                        journalTimer = 0
                    }
                    isRecording = !isRecording
                },
                takes = journalTakes,
                onTogglePlay = { take ->
                    journalTakes = journalTakes.map { if (it.id == take.id) it.copy(isPlaying = !it.isPlaying) else it }
                },
                onDelete = { take -> journalTakes = journalTakes.filterNot { it.id == take.id } },
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, GbhTab.Journal)
            )
        }

        composable(Routes.Arena) {
            ChallengeArenaScreen(
                currentTab = arenaTab,
                onTabSelected = { arenaTab = it },
                caption = postCaption,
                onCaptionChange = { postCaption = it },
                selectedChallengeLabel = "No challenge",
                selectedSessionLabel = "No linked session",
                onSharePost = {
                    if (postCaption.isNotBlank()) {
                        arenaPosts = listOf(ArenaPost(username, listOf("Demo"), postCaption, 0, 0)) + arenaPosts
                        postCaption = ""
                    }
                },
                posts = arenaPosts,
                challenges = arenaChallenges,
                onJoinChallenge = { challenge -> xp += challenge.xpReward },
                leaderboard = leaderboard,
                badges = badges,
                topBar = topBar(nav),
                bottomBar = bottomBar(nav, GbhTab.Arena)
            )
        }
    }
}
