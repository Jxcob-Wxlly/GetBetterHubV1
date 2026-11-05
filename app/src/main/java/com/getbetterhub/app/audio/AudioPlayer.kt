package com.getbetterhub.app.audio

import android.media.MediaPlayer

/**
 * Thin wrapper around [MediaPlayer] for playing back a single local audio file at a time.
 * Only one clip plays at once in this app (matches the mockups — tapping a new take's play
 * button implicitly stops whatever else was playing), so a single shared player is enough.
 */
class AudioPlayer {
    private var player: MediaPlayer? = null
    private var onCompleteCallback: (() -> Unit)? = null

    /** Starts playback of [filePath] from the beginning. Replaces any clip already playing. */
    fun play(filePath: String, onComplete: () -> Unit) {
        stop()
        onCompleteCallback = onComplete
        player = MediaPlayer().apply {
            setDataSource(filePath)
            setOnCompletionListener { onCompleteCallback?.invoke() }
            prepare()
            start()
        }
    }

    /** Current playback position, in whole seconds — used to drive the UI's position label/scrubber. */
    fun currentPositionSeconds(): Int = (player?.currentPosition ?: 0) / 1000

    fun isPlaying(): Boolean = player?.isPlaying == true

    fun stop() {
        player?.apply {
            try { if (isPlaying) stop() } catch (_: IllegalStateException) { /* already stopped */ }
            release()
        }
        player = null
        onCompleteCallback = null
    }
}
