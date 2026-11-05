package com.getbetterhub.app.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID

/**
 * Thin wrapper around [MediaRecorder] for capturing short voice clips (journal entries,
 * performance takes). Records to an AAC .m4a file in the app's private files directory —
 * no external storage permission needed since the file lives in app-private storage.
 */
class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    /** Starts recording and returns the file being written to. Call [stop] to finish it. */
    fun start(): File {
        val file = File(context.filesDir, "gbh_rec_${UUID.randomUUID()}.m4a")
        outputFile = file

        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        r.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = r
        return file
    }

    /** Stops the current recording. Returns the finished file, or null if nothing was recording. */
    fun stop(): File? {
        val r = recorder ?: return null
        return try {
            r.stop()
            outputFile
        } catch (e: RuntimeException) {
            // stop() throws if start() never actually began recording (e.g. mic grabbed by
            // another app) — treat that as "no clip produced" rather than crashing the screen.
            outputFile?.delete()
            null
        } finally {
            r.reset()
            r.release()
            recorder = null
            outputFile = null
        }
    }

    /** True while a recording is in progress. */
    fun isRecording(): Boolean = recorder != null
}
