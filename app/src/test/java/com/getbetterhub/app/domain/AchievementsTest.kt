package com.getbetterhub.app.domain

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.*
import org.junit.Test

class AchievementsTest {

    private val utc = ZoneId.of("UTC")

    private fun session(instrument: String = "vocals", seconds: Int = 300, hour: Int = 12) = SessionRecord(
        label = "Drill", instrument = instrument, durationSeconds = seconds,
        completedAtEpochMillis = ZonedDateTime.of(2026, 3, 10, hour, 0, 0, 0, utc).toInstant().toEpochMilli(),
        synced = true
    )

    private fun inputs(
        sessions: List<SessionRecord> = emptyList(), streak: Int = 0, xp: Int = 0, words: Int = 0, drafts: Int = 0,
        recordings: Int = 0, posts: Int = 0, comments: Int = 0, challenges: Int = 0, instruments: Set<String> = setOf("vocals")
    ) = ProgressInputs(sessions, streak, xp, words, drafts, recordings, posts, comments, challenges, instruments)

    private fun find(list: List<AchievementProgress>, id: String) = list.first { it.def.id == id }

    @Test fun fifteenCoreAchievementsPlusOnePerInstrument() {
        assertEquals(15 + 1, Achievements.visibleFor(setOf("vocals")).size)
        assertEquals(15 + 3, Achievements.visibleFor(setOf("vocals", "guitar", "bass")).size)
    }

    @Test fun firstSessionUnlocksCouchShredder() {
        val before = find(Achievements.computeAll(inputs(), utc), "couch_shredder")
        val after = find(Achievements.computeAll(inputs(sessions = listOf(session())), utc), "couch_shredder")
        assertFalse(before.unlocked)
        assertTrue(after.unlocked)
    }

    @Test fun progressIsCappedAtTheTarget() {
        val p = find(Achievements.computeAll(inputs(sessions = List(25) { session() }), utc), "metronome_connoisseur")
        assertEquals(10, p.current)
        assertTrue(p.unlocked)
        assertEquals(0, p.remaining)
        assertEquals(100, p.percent)
    }

    @Test fun nightOwlOnlyCountsSessionsBetweenTenPmAndFourAm() {
        val sessions = listOf(session(hour = 23), session(hour = 2), session(hour = 14), session(hour = 4))
        val p = find(Achievements.computeAll(inputs(sessions = sessions), utc), "night_owl_jammer")
        assertEquals(2, p.current) // 23:00 and 02:00 count; 04:00 and 14:00 do not
    }

    @Test fun earlyBirdCountsFiveToEightAm() {
        val sessions = listOf(session(hour = 5), session(hour = 7), session(hour = 8))
        assertEquals(2, find(Achievements.computeAll(inputs(sessions = sessions), utc), "early_bird_scales").current)
    }

    @Test fun instrumentAchievementsOnlyCountThatInstrument() {
        val sessions = List(4) { session("vocals") } + List(6) { session("guitar") }
        val list = Achievements.computeAll(inputs(sessions = sessions, instruments = setOf("vocals", "guitar")), utc)
        assertEquals(4, find(list, "golden_pipes").current)
        assertEquals(6, find(list, "fretboard_nomad").current)
    }

    @Test fun marathonModeCountsMinutes() {
        val p = find(Achievements.computeAll(inputs(sessions = List(2) { session(seconds = 1800) }), utc), "marathon_mode")
        assertEquals(60, p.current)
        assertEquals(540, p.remaining)
    }

    @Test fun grammyNeedsBothXpAndChallenges() {
        val onlyXp = find(Achievements.computeAll(inputs(xp = 3000, challenges = 1), utc), "grammy_in_progress")
        val both = find(Achievements.computeAll(inputs(xp = 3000, challenges = 3), utc), "grammy_in_progress")
        assertFalse(onlyXp.unlocked)
        assertTrue(both.unlocked)
        assertEquals(1500, find(Achievements.computeAll(inputs(xp = 1000, challenges = 3), utc), "grammy_in_progress").xpRemaining)
    }

    @Test fun wordCountIgnoresExtraWhitespace() {
        assertEquals(0, Achievements.wordCount("   "))
        assertEquals(4, Achievements.wordCount("  hello   world\nthis  works "))
    }
}
