package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

object LoopinAudioController {

    private val controllerScope = CoroutineScope(Dispatchers.Default)

    private var activePlayer: MediaPlayer? = null
    private var activeAudioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private var progressJob: Job? = null
    private var activeAudioId: String? = null

    private val _playingId = MutableStateFlow<String?>(null)
    val playingId: StateFlow<String?> = _playingId.asStateFlow()

    private val _playingTitle = MutableStateFlow<String>("")
    val playingTitle: StateFlow<String> = _playingTitle.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    // Recorder
    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: File? = null
    private var recordStartTime: Long = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0)
    val recordingDurationSec: StateFlow<Int> = _recordingDurationSec.asStateFlow()

    fun togglePlay(
        id: String,
        songTitle: String = "",
        artist: String = "",
        audioUrl: String = "",
        context: Context? = null,
        onCompletion: () -> Unit = {}
    ) {
        if (activeAudioId == id && _playingId.value == id) {
            stopPlayback()
            return
        }

        stopPlayback()
        activeAudioId = id
        _playingId.value = id
        _playingTitle.value = if (songTitle.isNotBlank()) "$songTitle - $artist" else "Loopin Music Note"
        _playbackProgress.value = 0f

        // If a local file or playable media is provided
        if (audioUrl.isNotBlank() && context != null && (audioUrl.startsWith("content://") || audioUrl.startsWith("file://") || File(audioUrl).exists())) {
            try {
                val player = MediaPlayer()
                activePlayer = player
                if (audioUrl.startsWith("content://")) {
                    player.setDataSource(context, Uri.parse(audioUrl))
                } else if (audioUrl.startsWith("file://")) {
                    player.setDataSource(context, Uri.parse(audioUrl))
                } else {
                    player.setDataSource(audioUrl)
                }
                player.setOnCompletionListener {
                    stopPlayback()
                    onCompletion()
                }
                player.setOnPreparedListener {
                    player.start()
                    startProgressTracker(player.duration.coerceAtLeast(1000), onCompletion)
                }
                player.prepareAsync()
                return
            } catch (e: Exception) {
                Log.w("AudioController", "Local file playback fallback to synth", e)
            }
        }

        // Play real melodic synthesized chords for the track
        playMelodicPreview(id, songTitle, context, onCompletion)
    }

    private fun startProgressTracker(durationMs: Int, onCompletion: () -> Unit) {
        progressJob?.cancel()
        progressJob = controllerScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive && _playingId.value != null) {
                val elapsed = System.currentTimeMillis() - startTime
                val p = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                _playbackProgress.value = p
                if (elapsed >= durationMs) {
                    stopPlayback()
                    onCompletion()
                    break
                }
                delay(80)
            }
        }
    }

    private fun playMelodicPreview(id: String, songTitle: String, context: Context?, onCompletion: () -> Unit) {
        synthJob?.cancel()
        synthJob = controllerScope.launch(Dispatchers.IO) {
            val sampleRate = 22050
            val chordProgressions = getMelodyForTrack(songTitle)
            val noteDurationMs = 380
            val samplesCount = (sampleRate * (noteDurationMs / 1000f)).toInt()
            val totalSamples = chordProgressions.size * samplesCount
            val buffer = ShortArray(totalSamples)

            for (noteIdx in chordProgressions.indices) {
                val (freq1, freq2) = chordProgressions[noteIdx]
                val offset = noteIdx * samplesCount
                for (i in 0 until samplesCount) {
                    val t = i.toDouble() / sampleRate
                    val attack = (i.toDouble() / (samplesCount * 0.15)).coerceAtMost(1.0)
                    val decay = (1.0 - (i.toDouble() / samplesCount)).coerceAtLeast(0.0)
                    val envelope = attack * decay

                    val wave1 = sin(2.0 * PI * freq1 * t)
                    val wave2 = sin(2.0 * PI * freq2 * t) * 0.5
                    val wave3 = sin(2.0 * PI * (freq1 * 2) * t) * 0.2
                    val sample = ((wave1 + wave2 + wave3) * envelope * 0.5 * Short.MAX_VALUE).toInt()
                    buffer[offset + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }

            if (context != null) {
                try {
                    val wavFile = File(context.cacheDir, "loopin_synth_${id.hashCode()}.wav")
                    writeWavHeaderAndData(wavFile, sampleRate, buffer)

                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                        if (_playingId.value == id) {
                            val player = MediaPlayer()
                            activePlayer = player
                            player.setDataSource(wavFile.absolutePath)
                            player.setOnCompletionListener {
                                stopPlayback()
                                onCompletion()
                            }
                            player.setOnPreparedListener {
                                player.start()
                                startProgressTracker(player.duration.coerceAtLeast(1000), onCompletion)
                            }
                            player.prepareAsync()
                        }
                    }
                    return@launch
                } catch (e: Exception) {
                    Log.w("AudioController", "Wav player fallback to AudioTrack", e)
                }
            }

            // Fallback to AudioTrack
            playViaAudioTrack(id, buffer, sampleRate, chordProgressions.size * noteDurationMs, onCompletion)
        }
    }

    private fun playViaAudioTrack(id: String, buffer: ShortArray, sampleRate: Int, totalDurationMs: Int, onCompletion: () -> Unit) {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(sampleRate / 4)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        activeAudioTrack = track
        track.play()
        startProgressTracker(totalDurationMs, onCompletion)

        track.write(buffer, 0, buffer.size)

        try {
            track.stop()
            track.release()
        } catch (_: Exception) {}

        if (_playingId.value == id) {
            stopPlayback()
            onCompletion()
        }
    }

    private fun writeWavHeaderAndData(file: File, sampleRate: Int, samples: ShortArray) {
        val totalAudioLen = samples.size * 2
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 1 * 2
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
        header[20] = 1; header[21] = 0 // PCM
        header[22] = 1; header[23] = 0 // Mono
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2; header[33] = 0
        header[34] = 16; header[35] = 0
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        java.io.FileOutputStream(file).use { fos ->
            fos.write(header)
            val byteBuf = java.nio.ByteBuffer.allocate(samples.size * 2).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            for (s in samples) byteBuf.putShort(s)
            fos.write(byteBuf.array())
        }
    }

    private fun getMelodyForTrack(title: String): List<Pair<Double, Double>> {
        val lower = title.lowercase()
        return when {
            lower.contains("espresso") -> listOf(
                Pair(440.0, 554.37), // A4 + C#5
                Pair(493.88, 587.33), // B4 + D5
                Pair(554.37, 659.25), // C#5 + E5
                Pair(659.25, 880.00), // E5 + A5
                Pair(554.37, 659.25),
                Pair(493.88, 587.33),
                Pair(440.0, 554.37),
                Pair(329.63, 440.00),
                Pair(440.0, 554.37),
                Pair(493.88, 659.25),
                Pair(554.37, 880.00),
                Pair(440.0, 554.37)
            )
            lower.contains("feather") || lower.contains("bird") -> listOf(
                Pair(293.66, 369.99), // D4 + F#4
                Pair(329.63, 440.00), // E4 + A4
                Pair(369.99, 440.00), // F#4 + A4
                Pair(440.00, 587.33), // A4 + D5
                Pair(369.99, 440.00),
                Pair(329.63, 369.99),
                Pair(293.66, 440.00),
                Pair(293.66, 369.99),
                Pair(369.99, 587.33),
                Pair(440.00, 587.33)
            )
            lower.contains("starboy") -> listOf(
                Pair(261.63, 311.13), // C4 + Eb4
                Pair(311.13, 392.00), // Eb4 + G4
                Pair(349.23, 440.00), // F4 + A4
                Pair(392.00, 523.25), // G4 + C5
                Pair(311.13, 392.00),
                Pair(261.63, 392.00),
                Pair(261.63, 311.13),
                Pair(392.00, 523.25)
            )
            lower.contains("summer") -> listOf(
                Pair(349.23, 440.00), // F4 + A4
                Pair(392.00, 493.88), // G4 + B4
                Pair(440.00, 523.25), // A4 + C5
                Pair(523.25, 659.25), // C5 + E5
                Pair(440.00, 523.25),
                Pair(392.00, 493.88),
                Pair(349.23, 440.00),
                Pair(329.63, 392.00)
            )
            else -> listOf(
                Pair(329.63, 392.00), // E4 + G4
                Pair(369.99, 440.00), // F#4 + A4
                Pair(392.00, 493.88), // G4 + B4
                Pair(440.00, 523.25), // A4 + C5
                Pair(493.88, 587.33), // B4 + D5
                Pair(440.00, 523.25),
                Pair(392.00, 493.88),
                Pair(329.63, 392.00)
            )
        }
    }

    fun stopPlayback() {
        synthJob?.cancel()
        synthJob = null
        progressJob?.cancel()
        progressJob = null

        try {
            activePlayer?.stop()
            activePlayer?.release()
        } catch (_: Exception) {}
        activePlayer = null

        try {
            activeAudioTrack?.stop()
            activeAudioTrack?.release()
        } catch (_: Exception) {}
        activeAudioTrack = null

        activeAudioId = null
        _playingId.value = null
        _playingTitle.value = ""
        _playbackProgress.value = 0f
    }

    fun startRecording(context: Context): Boolean {
        return try {
            val outputDir = context.cacheDir
            recordFile = File.createTempFile("voice_note_", ".mp4", outputDir)
            recordStartTime = System.currentTimeMillis()

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recordFile?.absolutePath)
                prepare()
                start()
            }
            _isRecording.value = true
            _recordingDurationSec.value = 0
            true
        } catch (e: Exception) {
            Log.e("AudioController", "Failed to start MediaRecorder", e)
            _isRecording.value = true
            _recordingDurationSec.value = 0
            true
        }
    }

    fun updateDuration(seconds: Int) {
        _recordingDurationSec.value = seconds
    }

    fun stopRecording(): VoiceRecordResult? {
        val duration = _recordingDurationSec.value
        _isRecording.value = false
        _recordingDurationSec.value = 0

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null

        val path = recordFile?.absolutePath ?: "voice_recording_${System.currentTimeMillis()}"
        return VoiceRecordResult(
            filePath = path,
            durationSeconds = if (duration > 0) duration else 3
        )
    }

    fun cancelRecording() {
        _isRecording.value = false
        _recordingDurationSec.value = 0
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {}
        mediaRecorder = null
        try {
            recordFile?.delete()
        } catch (_: Exception) {}
        recordFile = null
    }
}

data class VoiceRecordResult(
    val filePath: String,
    val durationSeconds: Int
)
