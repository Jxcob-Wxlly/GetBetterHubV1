package com.getbetterhub.app.domain

/** Keys are stored/synced as-is; screens translate them via string resources (inst_*, lvl_*, cat_*). */
object Instruments {
    val all = listOf("vocals", "guitar", "piano", "drums", "bass", "production", "songwriting")
}

object Levels {
    val all = listOf("beginner", "intermediate", "advanced")
}

data class Drill(
    val id: String,
    val instrument: String,
    val level: String,
    val category: String,
    val title: String,
    val description: String,
    val suggestedMinutes: Int
)

/**
 * The Practice Roulette drill library: 7 instruments x 3 levels x 3 drills. The roulette only draws
 * from instruments the user ticked on their profile, at their chosen level. Titles/descriptions are
 * practice content and stay in English; the labels around them are translated.
 */
object DrillLibrary {

    private fun d(inst: String, lvl: String, cat: String, title: String, desc: String, min: Int) =
        Drill("$inst-$lvl-${title.lowercase().replace(' ', '-')}", inst, lvl, cat, title, desc, min)

    val all: List<Drill> = listOf(
        // ---- vocals
        d("vocals", "beginner", "warmup", "Lip Trill Ladder", "Trill up and down a five-note scale, staying relaxed through the passaggio.", 5),
        d("vocals", "beginner", "scales", "Five-Note Siren", "Slide smoothly through a five-note range on an open 'ng' sound, breathing low.", 5),
        d("vocals", "beginner", "ear", "Match The Note", "Play a note, hum it back, then check your pitch against the source.", 6),
        d("vocals", "intermediate", "technique", "Breath Support Holds", "Sustain a comfortable note for 20 seconds with steady airflow, five reps.", 7),
        d("vocals", "intermediate", "scales", "Major Scale Legato", "Sing a major scale on 'oo' connecting each note without breaks.", 8),
        d("vocals", "intermediate", "rhythm", "Phrase Placement", "Speak a lyric in time, then sing it landing each syllable on the beat.", 8),
        d("vocals", "advanced", "technique", "Agility Runs", "Sing fast five-note runs cleanly at 90 bpm, then raise the tempo.", 10),
        d("vocals", "advanced", "melody", "Ornament Practice", "Add turns and slides to a simple melody without losing pitch.", 10),
        d("vocals", "advanced", "ear", "Harmony Layering", "Record a melody, then sing a third above it in tune.", 12),
        // ---- guitar
        d("guitar", "beginner", "warmup", "Spider Walk", "Fret 1-2-3-4 across all strings with one finger per fret, slowly and cleanly.", 5),
        d("guitar", "beginner", "technique", "Open Chord Changes", "Switch between G, C and D chords in time, one strum per bar.", 6),
        d("guitar", "beginner", "rhythm", "Down-Up Strumming", "Keep a steady down-up strum on one muted chord at 70 bpm.", 6),
        d("guitar", "intermediate", "scales", "Pentatonic Boxes", "Connect pentatonic positions 1-3 across the neck with alternate picking.", 7),
        d("guitar", "intermediate", "technique", "Barre Chord Endurance", "Move an F barre shape up the neck, holding each chord for four beats.", 8),
        d("guitar", "intermediate", "rhythm", "Palm Mute Gallop", "Play a palm-muted gallop rhythm on the low strings, tight with a metronome.", 7),
        d("guitar", "advanced", "technique", "Sweep Arpeggios", "Practise three-string sweep arpeggios slowly, then add speed.", 10),
        d("guitar", "advanced", "scales", "Modal Shapes", "Play Dorian and Mixolydian over one root, hearing the difference.", 10),
        d("guitar", "advanced", "creative", "Improv Over A Loop", "Loop a two-chord vamp and improvise using only three target notes.", 12),
        // ---- piano
        d("piano", "beginner", "warmup", "Five-Finger Scale", "Play a five-finger scale hands separately, then together, staying relaxed.", 6),
        d("piano", "beginner", "technique", "Finger Independence", "Hold down three keys and tap the others quietly, rotating fingers.", 5),
        d("piano", "beginner", "theory", "Triad Spotting", "Build C, F and G major triads and play them in root position.", 6),
        d("piano", "intermediate", "scales", "Two-Octave Scales", "Play C, G and D major scales two octaves, hands together, evenly.", 8),
        d("piano", "intermediate", "technique", "Alberti Bass", "Play an Alberti bass pattern in the left hand while the right plays a melody.", 8),
        d("piano", "intermediate", "theory", "Chord Inversions", "Play C major in all three inversions and move smoothly between them.", 7),
        d("piano", "advanced", "technique", "Hanon Speed Builder", "Take a Hanon exercise up in tempo, keeping each note even.", 10),
        d("piano", "advanced", "theory", "Jazz ii-V-I", "Play ii-V-I in three keys using shell voicings.", 10),
        d("piano", "advanced", "creative", "Reharmonise A Melody", "Take a simple melody and play it over three different chord sets.", 12),
        // ---- drums
        d("drums", "beginner", "warmup", "Single Stroke Roll", "Alternate R-L on a pad at a comfortable tempo, keeping both hands even.", 5),
        d("drums", "beginner", "rhythm", "Basic Rock Beat", "Hold a steady kick-snare rock beat with eighth-note hi-hats for two minutes.", 6),
        d("drums", "beginner", "technique", "Stick Control", "Play the first page of paradiddle patterns slowly with a metronome.", 6),
        d("drums", "intermediate", "technique", "Paradiddle Orchestration", "Move a paradiddle around the toms while the kick keeps quarter notes.", 8),
        d("drums", "intermediate", "rhythm", "Ghost Note Groove", "Add soft ghost notes between the snare backbeats of a funk groove.", 8),
        d("drums", "intermediate", "creative", "Four-Bar Fills", "Play three bars of groove then a one-bar fill, changing the fill each time.", 8),
        d("drums", "advanced", "technique", "Linear Patterns", "Play a linear pattern where no two limbs hit at the same time.", 10),
        d("drums", "advanced", "rhythm", "Odd-Time Feel", "Groove in 7/8, counting 2+2+3 out loud.", 10),
        d("drums", "advanced", "creative", "Solo Phrasing", "Build a 16-bar solo from one repeated motif.", 12),
        // ---- bass
        d("bass", "beginner", "warmup", "Root Note Walk", "Walk a root note up the neck on one string, plucking evenly.", 5),
        d("bass", "beginner", "rhythm", "Locking With The Kick", "Play roots on the beat of a simple drum loop.", 6),
        d("bass", "beginner", "scales", "Major Scale One Octave", "Play a one-octave major scale with alternating fingers.", 6),
        d("bass", "intermediate", "technique", "Fingerstyle Speed", "Alternate index-middle at 100 bpm on a single string, even volume.", 7),
        d("bass", "intermediate", "rhythm", "Syncopated Grooves", "Play a groove that lands off the beat and still locks with a click.", 8),
        d("bass", "intermediate", "theory", "Walking Bass Line", "Walk a 12-bar blues using root, third, fifth and approach notes.", 9),
        d("bass", "advanced", "technique", "Slap And Pop", "Practise thumb slaps and index pops in a steady sixteenth groove.", 10),
        d("bass", "advanced", "theory", "Chord Tone Soloing", "Solo over a changing progression using only chord tones.", 10),
        d("bass", "advanced", "creative", "Write A Hook Line", "Write a four-bar bass hook that works without any other instrument.", 12),
        // ---- production
        d("production", "beginner", "ear", "Kick And Bass Balance", "Set a kick and bass loop so both are clear, adjusting levels only.", 8),
        d("production", "beginner", "creative", "Four-Bar Loop", "Build a four-bar loop from just drums and one melodic sound.", 10),
        d("production", "beginner", "technique", "Sample Chopping", "Chop a short sample into four pieces and rearrange them.", 10),
        d("production", "intermediate", "technique", "EQ Carving", "Take two clashing sounds and carve space for each with EQ.", 10),
        d("production", "intermediate", "creative", "Sound Design Patch", "Build a simple synth bass patch from scratch and save it.", 12),
        d("production", "intermediate", "ear", "Reference Track Match", "Compare your mix with a pro track and note three differences.", 12),
        d("production", "advanced", "technique", "Parallel Compression", "Add parallel compression to the drum bus and match levels.", 12),
        d("production", "advanced", "creative", "Arrangement Map", "Map a full song in sections and fill the gaps in your current beat.", 15),
        d("production", "advanced", "theory", "Sidechain Groove", "Create a pumping sidechain groove that follows the kick.", 12),
        // ---- songwriting
        d("songwriting", "beginner", "creative", "Title First Song Start", "Pick a title you like and write the first four lines around it.", 7),
        d("songwriting", "beginner", "melody", "Hum Three Hooks", "Hum three different short hook ideas and record the best one.", 6),
        d("songwriting", "beginner", "theory", "Four-Chord Song", "Write a verse over I-V-vi-IV and sing anything on top.", 8),
        d("songwriting", "intermediate", "melody", "Melody Over One Chord", "Write three distinct melodies over a single sustained chord.", 8),
        d("songwriting", "intermediate", "creative", "Rhyme Ladders", "Write a verse using an AABB then an ABAB rhyme scheme and compare.", 9),
        d("songwriting", "intermediate", "theory", "Chorus Lift", "Raise the chorus melody higher than the verse and feel the difference.", 8),
        d("songwriting", "advanced", "creative", "Story In Sixteen Bars", "Tell a complete story with a turning point in sixteen bars.", 12),
        d("songwriting", "advanced", "theory", "Modulate The Last Chorus", "Lift the final chorus up a key and make the change feel earned.", 12),
        d("songwriting", "advanced", "melody", "Counter-Melody", "Write a counter-melody that weaves around your main vocal line.", 10)
    )

    /** Drills the roulette may draw from for the given instruments and level. */
    fun pool(instruments: Collection<String>, level: String): List<Drill> {
        val wanted = instruments.map { it.lowercase() }.toSet()
        return all.filter { it.instrument in wanted && it.level == level.lowercase() }
    }

    /** Random drill from the pool, avoiding an immediate repeat of [current] when there is a choice. */
    fun spin(instruments: Collection<String>, level: String, current: Drill? = null, random: kotlin.random.Random = kotlin.random.Random): Drill? {
        val pool = pool(instruments, level)
        if (pool.isEmpty()) return null
        val choices = if (pool.size > 1 && current != null) pool.filter { it.id != current.id } else pool
        return choices[random.nextInt(choices.size)]
    }

    /**
     * Mood Coach: picks a drill whose category suits how the player feels. Low energy leans to
     * gentle work, high energy to demanding work. Falls back to the whole pool if nothing matches.
     */
    fun forMood(mood: String, energy: Int, instruments: Collection<String>, level: String, random: kotlin.random.Random = kotlin.random.Random): Drill? {
        val pool = pool(instruments, level)
        if (pool.isEmpty()) return null
        val preferred = when {
            mood == "energised" || energy >= 4 -> setOf("technique", "rhythm", "scales")
            mood == "inspired" -> setOf("creative", "melody")
            mood == "anxious" -> setOf("warmup", "scales", "ear")
            mood == "blocked" -> setOf("creative", "melody", "ear")
            mood == "frustrated" -> setOf("rhythm", "warmup")
            else -> setOf("warmup", "ear", "theory") // flat / low energy
        }
        val matching = pool.filter { it.category in preferred }
        val choices = if (matching.isNotEmpty()) matching else pool
        return choices[random.nextInt(choices.size)]
    }
}
