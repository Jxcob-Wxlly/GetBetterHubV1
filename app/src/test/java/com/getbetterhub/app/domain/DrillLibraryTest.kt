package com.getbetterhub.app.domain

import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

/** Practice Roulette picks drills by the instruments and level ticked on the profile. */
class DrillLibraryTest {

    @Test fun libraryCoversEveryInstrumentAndLevel() {
        for (instrument in Instruments.all) for (level in Levels.all) {
            assertTrue("no drills for $instrument/$level", DrillLibrary.pool(listOf(instrument), level).isNotEmpty())
        }
    }

    @Test fun drillIdsAreUnique() {
        assertEquals(DrillLibrary.all.size, DrillLibrary.all.map { it.id }.toSet().size)
    }

    @Test fun poolOnlyContainsChosenInstrumentsAtTheChosenLevel() {
        val pool = DrillLibrary.pool(listOf("vocals", "guitar"), "beginner")
        assertTrue(pool.isNotEmpty())
        assertTrue(pool.all { it.instrument in setOf("vocals", "guitar") && it.level == "beginner" })
    }

    @Test fun poolIsCaseInsensitive() {
        assertEquals(
            DrillLibrary.pool(listOf("piano"), "advanced"),
            DrillLibrary.pool(listOf("PIANO"), "Advanced")
        )
    }

    @Test fun spinReturnsNullWhenNoInstrumentSelected() {
        assertNull(DrillLibrary.spin(emptyList(), "beginner"))
    }

    @Test fun spinNeverRepeatsTheCurrentDrillWhenThereIsAChoice() {
        val first = DrillLibrary.spin(listOf("drums"), "beginner", random = Random(1))!!
        repeat(50) {
            val next = DrillLibrary.spin(listOf("drums"), "beginner", current = first, random = Random(it))!!
            assertNotEquals(first.id, next.id)
        }
    }

    @Test fun moodPlanFavoursSuitableCategories() {
        val drill = DrillLibrary.forMood("inspired", 3, listOf("songwriting"), "intermediate", Random(7))!!
        assertTrue(drill.category in setOf("creative", "melody"))
    }

    @Test fun moodPlanFallsBackToWholePoolIfNothingMatches() {
        assertNotNull(DrillLibrary.forMood("blocked", 3, listOf("drums"), "beginner", Random(3)))
    }
}
