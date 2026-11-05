package com.getbetterhub.app.domain

import java.time.Instant
import java.time.ZoneId

enum class Rarity { Common, Rare, Epic, Legendary }

/** What an achievement counts. The UI translates the unit label (unit_* strings). */
enum class UnitKind { Sessions, Drafts, StreakDays, Comments, Recordings, Posts, Challenges, Words, Minutes, InstrumentSessions }

data class AchievementDef(
    val id: String,
    val name: String,
    val tagline: String,
    val requirement: String,
    val rarity: Rarity,
    val unit: UnitKind,
    val target: Int,
    val emoji: String,
    /** Only shown to people who practise this instrument (and counts only that instrument's sessions). */
    val instrument: String? = null,
    /** Count only sessions that start within these local hours (e.g. night owl / early bird). */
    val hours: Set<Int>? = null,
    /** Second condition (Grammy in Progress needs total XP *and* challenges). */
    val xpTarget: Int? = null
)

/** Everything achievement progress is computed from — plain data so it is trivially unit-testable. */
data class ProgressInputs(
    val sessions: List<SessionRecord>,
    val dayStreak: Int,
    val totalXp: Int,
    val lyricWords: Int,
    val lyricDrafts: Int,
    val recordings: Int,
    val posts: Int,
    val commentsMade: Int,
    val challengesJoined: Int,
    val practisedInstruments: Set<String>
)

data class SessionRecord(
    val label: String,
    val instrument: String,
    val durationSeconds: Int,
    val completedAtEpochMillis: Long,
    val synced: Boolean
)

data class AchievementProgress(
    val def: AchievementDef,
    val current: Int,
    val xpCurrent: Int? = null
) {
    val target: Int get() = def.target
    val unlocked: Boolean get() = current >= def.target && (def.xpTarget == null || (xpCurrent ?: 0) >= def.xpTarget)
    val remaining: Int get() = (def.target - current).coerceAtLeast(0)
    val xpRemaining: Int get() = ((def.xpTarget ?: 0) - (xpCurrent ?: 0)).coerceAtLeast(0)

    /** 0..100; with two conditions it is the average so both bars matter. */
    val percent: Int get() {
        val a = (current.toFloat() / def.target).coerceIn(0f, 1f)
        val b = def.xpTarget?.let { ((xpCurrent ?: 0).toFloat() / it).coerceIn(0f, 1f) } ?: a
        return (((a + b) / 2f) * 100).toInt()
    }
}

object Achievements {

    private val core = listOf(
        AchievementDef("couch_shredder", "Couch Shredder", "Technically practice. Technically on the couch. We count it.", "Complete your first practice session", Rarity.Common, UnitKind.Sessions, 1, "🛋️"),
        AchievementDef("metronome_connoisseur", "Metronome Connoisseur", "You and the click are now on a first-name basis.", "Complete 10 practice sessions", Rarity.Rare, UnitKind.Sessions, 10, "⏱️"),
        AchievementDef("night_owl_jammer", "Night Owl Jammer", "Your neighbours have opinions. You have a groove.", "Practise 3 sessions between 10pm and 4am", Rarity.Rare, UnitKind.Sessions, 3, "🦉", hours = setOf(22, 23, 0, 1, 2, 3)),
        AchievementDef("earworm_architect", "Earworm Architect", "Building hooks people will hum against their will.", "Write 5 lyric drafts", Rarity.Rare, UnitKind.Drafts, 5, "🪱"),
        AchievementDef("seven_day_streak", "Seven-Day Streak Beast", "A full week without skipping. Rest days fear you.", "Reach a 7-day practice streak", Rarity.Epic, UnitKind.StreakDays, 7, "🔥"),
        AchievementDef("feedback_fiend", "Feedback Fiend", "Hype-man, critic and cheerleader — all in one comment box.", "Leave 10 comments in the Arena", Rarity.Rare, UnitKind.Comments, 10, "💬"),
        AchievementDef("voice_memo_hoarder", "Voice Memo Hoarder", "47 untitled takes and counting. One of them is a hit.", "Save 5 journal recordings", Rarity.Common, UnitKind.Recordings, 5, "🎙️"),
        AchievementDef("top_one_percent", "Top 1% Practicer", "Your practice log has its own fan club.", "Log 100 completed sessions", Rarity.Legendary, UnitKind.Sessions, 100, "👑"),
        AchievementDef("grammy_in_progress", "Grammy in Progress", "Speech drafted. Outfit picked. Just need the trophy.", "Earn 2,500 XP and enter 3 challenges", Rarity.Legendary, UnitKind.Challenges, 3, "🏆", xpTarget = 2500),
        AchievementDef("early_bird_scales", "Early Bird Scales", "Coffee in one hand, metronome in the other.", "Practise 3 sessions between 5am and 8am", Rarity.Rare, UnitKind.Sessions, 3, "🌅", hours = setOf(5, 6, 7)),
        AchievementDef("marathon_mode", "Marathon Mode", "Hydrate. Stretch. Keep shredding.", "Log 10 hours of total practice", Rarity.Epic, UnitKind.Minutes, 600, "🏃"),
        AchievementDef("arena_regular", "Arena Regular", "The crowd knows your name. Mostly.", "Share 3 posts in the Arena", Rarity.Common, UnitKind.Posts, 3, "🎪"),
        AchievementDef("challenge_hopper", "Challenge Hopper", "Commitment issues, but make it productive.", "Join 5 challenges", Rarity.Rare, UnitKind.Challenges, 5, "🦘"),
        AchievementDef("lyric_novelist", "Lyric Novelist", "Your verses have chapters now.", "Write 2,000 words across your drafts", Rarity.Epic, UnitKind.Words, 2000, "📖"),
        AchievementDef("month_of_mayhem", "Month of Mayhem", "Thirty days. Zero excuses. Absolute menace.", "Reach a 30-day practice streak", Rarity.Legendary, UnitKind.StreakDays, 30, "🌪️")
    )

    private val byInstrument = listOf(
        AchievementDef("golden_pipes", "Golden Pipes", "The shower stage misses you already.", "Complete 10 vocals sessions", Rarity.Epic, UnitKind.InstrumentSessions, 10, "🎤", instrument = "vocals"),
        AchievementDef("fretboard_nomad", "Fretboard Nomad", "Six strings, zero left behind.", "Complete 10 guitar sessions", Rarity.Epic, UnitKind.InstrumentSessions, 10, "🎸", instrument = "guitar"),
        AchievementDef("ivory_wanderer", "Ivory Wanderer", "Eighty-eight keys, one very loyal player.", "Complete 10 piano sessions", Rarity.Epic, UnitKind.InstrumentSessions, 10, "🎹", instrument = "piano"),
        AchievementDef("groove_machine", "Groove Machine", "The pocket is your permanent address.", "Complete 10 drums sessions", Rarity.Epic, UnitKind.InstrumentSessions, 10, "🥁", instrument = "drums"),
        AchievementDef("low_end_theory", "Low-End Theory", "Felt before it is heard.", "Complete 10 bass sessions", Rarity.Epic, UnitKind.InstrumentSessions, 10, "🎶", instrument = "bass"),
        AchievementDef("beat_architect", "Beat Architect", "Every loop is a blueprint.", "Complete 10 production sessions", Rarity.Epic, UnitKind.InstrumentSessions, 10, "🎧", instrument = "production"),
        AchievementDef("note_hoarder", "Note Hoarder", "Every napkin is a potential hit single.", "Complete 10 songwriting sessions", Rarity.Epic, UnitKind.InstrumentSessions, 10, "📝", instrument = "songwriting")
    )

    /** Core achievements plus one per practised instrument — so the list is 15 + however many instruments you tick. */
    fun visibleFor(instruments: Set<String>): List<AchievementDef> {
        val wanted = instruments.map { it.lowercase() }.toSet()
        return core + byInstrument.filter { it.instrument in wanted }
    }

    fun countSessions(def: AchievementDef, sessions: List<SessionRecord>, zone: ZoneId = ZoneId.systemDefault()): Int {
        val matching = sessions.filter { s ->
            (def.instrument == null || s.instrument.equals(def.instrument, ignoreCase = true)) &&
                (def.hours == null || Instant.ofEpochMilli(s.completedAtEpochMillis).atZone(zone).hour in def.hours)
        }
        return matching.size
    }

    fun progress(def: AchievementDef, input: ProgressInputs, zone: ZoneId = ZoneId.systemDefault()): AchievementProgress {
        val current = when (def.unit) {
            UnitKind.Sessions, UnitKind.InstrumentSessions -> countSessions(def, input.sessions, zone)
            UnitKind.Drafts -> input.lyricDrafts
            UnitKind.StreakDays -> input.dayStreak
            UnitKind.Comments -> input.commentsMade
            UnitKind.Recordings -> input.recordings
            UnitKind.Posts -> input.posts
            UnitKind.Challenges -> input.challengesJoined
            UnitKind.Words -> input.lyricWords
            UnitKind.Minutes -> input.sessions.sumOf { it.durationSeconds } / 60
        }
        return AchievementProgress(def, current.coerceAtMost(def.target), if (def.xpTarget != null) input.totalXp else null)
    }

    fun computeAll(input: ProgressInputs, zone: ZoneId = ZoneId.systemDefault()): List<AchievementProgress> =
        visibleFor(input.practisedInstruments).map { progress(it, input, zone) }

    /** Word count used by "Lyric Novelist" — whitespace separated, blank text counts as zero. */
    fun wordCount(text: String): Int = text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
}
