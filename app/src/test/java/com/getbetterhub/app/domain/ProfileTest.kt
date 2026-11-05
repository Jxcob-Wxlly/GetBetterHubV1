package com.getbetterhub.app.domain

import org.junit.Assert.*
import org.junit.Test

class ProfileTest {

    @Test fun validHandlesAreLowercaseLettersDigitsUnderscores() {
        assertTrue(Profile.isValidHandle("jacobwally_c572f"))
        assertTrue(Profile.isValidHandle("abc"))
        assertTrue(Profile.isValidHandle("a".repeat(20)))
    }

    @Test fun invalidHandlesAreRejected() {
        assertFalse(Profile.isValidHandle("ab"))
        assertFalse(Profile.isValidHandle("a".repeat(21)))
        assertFalse(Profile.isValidHandle("Has Space"))
        assertFalse(Profile.isValidHandle("UPPER"))
        assertFalse(Profile.isValidHandle("dash-ed"))
        assertFalse(Profile.isValidHandle(""))
    }

    @Test fun defaultHandleIsValidAndStable() {
        val handle = Profile.defaultHandle("Jacob Wally!", "abc-123-XYZ-c572f")
        assertTrue(handle, Profile.isValidHandle(handle))
        assertEquals(handle, Profile.defaultHandle("Jacob Wally!", "abc-123-XYZ-c572f"))
        assertTrue(Profile.isValidHandle(Profile.defaultHandle("!!!", "1")))
    }

    @Test fun primaryInstrumentIsAlwaysInTheRotation() {
        val p = Profile(displayName = "J", handle = "jjj", primaryInstrument = "guitar", instruments = listOf("vocals")).normalised()
        assertTrue("guitar" in p.instruments)
        assertTrue("vocals" in p.instruments)
        assertEquals(p.instruments.distinct(), p.instruments)
    }
}
