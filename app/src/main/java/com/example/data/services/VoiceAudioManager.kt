package com.example.data.services

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

class VoiceAudioManager(private val context: Context) {

    private val sampleRate = 16_000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _waveformBars = MutableStateFlow(defaultWaveform())
    val waveformBars: StateFlow<List<Float>> = _waveformBars.asStateFlow()

    private val _playingMemoId = MutableStateFlow<Long?>(null)
    val playingMemoId: StateFlow<Long?> = _playingMemoId.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var timerJob: Job? = null
    private var currentFile: File? = null
    private var mediaPlayer: MediaPlayer? = null

    fun resolveMemoFile(relativePath: String): File = File(context.filesDir, relativePath)

    @SuppressLint("MissingPermission")
    fun startRecording(scope: CoroutineScope, relativePath: String) {
        if (_isRecording.value) return
        stopPlayback()

        val destFile = resolveMemoFile(relativePath).apply {
            parentFile?.mkdirs()
        }
        currentFile = destFile

        val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            minBuf * 2
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return
        }

        audioRecord = recorder
        _elapsedSeconds.value = 0
        _isRecording.value = true
        recorder.startRecording()

        timerJob = scope.launch {
            while (isActive && _isRecording.value) {
                delay(1000L)
                _elapsedSeconds.value += 1
            }
        }

        recordingJob = scope.launch(Dispatchers.IO) {
            val buffer = ShortArray(minBuf / 2)
            FileOutputStream(destFile).use { fos ->
                // Write placeholder 44-byte WAV header
                fos.write(ByteArray(44))
                var totalAudioBytes = 0L
                val byteBuffer = ByteArray(buffer.size * 2)

                while (isActive && _isRecording.value) {
                    val readCount = recorder.read(buffer, 0, buffer.size)
                    if (readCount > 0) {
                        var peak = 0
                        for (i in 0 until readCount) {
                            val sample = buffer[i]
                            val absSample = abs(sample.toInt())
                            if (absSample > peak) peak = absSample
                            byteBuffer[i * 2] = (sample.toInt() and 0xFF).toByte()
                            byteBuffer[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
                        }
                        fos.write(byteBuffer, 0, readCount * 2)
                        totalAudioBytes += readCount * 2

                        val normalized = (peak / 18000f).coerceIn(0.14f, 1.0f)
                        val nextBars = _waveformBars.value.drop(1) + normalized
                        _waveformBars.value = nextBars
                    }
                }
                fos.flush()
                writeWavHeader(destFile, totalAudioBytes)
            }
        }
    }

    suspend fun stopRecording(): Pair<String, Int>? {
        if (!_isRecording.value) return null
        _isRecording.value = false
        timerJob?.cancel()
        runCatching { audioRecord?.stop() }
        recordingJob?.join()
        runCatching { audioRecord?.release() }
        audioRecord = null
        val savedFile = currentFile
        val duration = _elapsedSeconds.value.coerceAtLeast(1)
        _waveformBars.value = defaultWaveform()
        return if (savedFile != null && savedFile.exists()) {
            val rel = savedFile.relativeTo(context.filesDir).path
            rel to duration
        } else {
            null
        }
    }

    fun togglePlayback(memoId: Long, relativePath: String) {
        if (_playingMemoId.value == memoId) {
            stopPlayback()
            return
        }
        stopPlayback()
        val file = resolveMemoFile(relativePath)
        if (!file.exists()) return
        runCatching {
            val player = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    _playingMemoId.value = null
                    release()
                    mediaPlayer = null
                }
                prepare()
                start()
            }
            mediaPlayer = player
            _playingMemoId.value = memoId
        }
    }

    fun stopPlayback() {
        runCatching {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
        mediaPlayer = null
        _playingMemoId.value = null
    }

    fun release() {
        _isRecording.value = false
        timerJob?.cancel()
        recordingJob?.cancel()
        runCatching {
            audioRecord?.stop()
            audioRecord?.release()
        }
        audioRecord = null
        stopPlayback()
    }

    suspend fun generateSampleWav(dest: File, baseFreqHz: Double, durationSec: Int) =
        withContext(Dispatchers.IO) {
            dest.parentFile?.mkdirs()
            val totalSamples = sampleRate * durationSec
            val dataBytes = totalSamples * 2L
            val baos = ByteArrayOutputStream(totalSamples * 2)
            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                val envelope = min(1.0, i / 1200.0) * min(1.0, (totalSamples - i) / 1600.0)
                val tone = sin(2.0 * PI * baseFreqHz * t) * 0.45 +
                    sin(2.0 * PI * (baseFreqHz * 1.25) * t) * 0.25
                val pulse = 0.65 + 0.35 * sin(2.0 * PI * 1.5 * t)
                val sample = (tone * pulse * envelope * 7500.0).toInt().coerceIn(-32767, 32767)
                baos.write(sample and 0xFF)
                baos.write((sample shr 8) and 0xFF)
            }
            FileOutputStream(dest).use { fos ->
                fos.write(ByteArray(44))
                fos.write(baos.toByteArray())
            }
            writeWavHeader(dest, dataBytes)
        }

    private fun writeWavHeader(file: File, totalAudioLen: Long) {
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 2L
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
        writeIntLE(header, 4, totalDataLen.toInt())
        header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
        writeIntLE(header, 16, 16) // PCM chunk size
        writeShortLE(header, 20, 1) // PCM format
        writeShortLE(header, 22, 1) // Mono
        writeIntLE(header, 24, sampleRate)
        writeIntLE(header, 28, byteRate.toInt())
        writeShortLE(header, 32, 2) // Block align
        writeShortLE(header, 34, 16) // Bits per sample
        header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
        writeIntLE(header, 40, totalAudioLen.toInt())

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.write(header)
        }
    }

    private fun writeIntLE(buf: ByteArray, offset: Int, value: Int) {
        buf[offset] = (value and 0xFF).toByte()
        buf[offset + 1] = ((value shr 8) and 0xFF).toByte()
        buf[offset + 2] = ((value shr 16) and 0xFF).toByte()
        buf[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun writeShortLE(buf: ByteArray, offset: Int, value: Short) {
        buf[offset] = (value.toInt() and 0xFF).toByte()
        buf[offset + 1] = ((value.toInt() shr 8) and 0xFF).toByte()
    }

    companion object {
        fun defaultWaveform(): List<Float> = listOf(
            0.28f, 0.52f, 0.40f, 0.70f, 0.45f, 0.85f, 0.60f, 0.34f, 0.66f,
            0.92f, 0.54f, 0.40f, 0.78f, 0.60f, 0.28f, 0.46f, 0.72f, 0.34f,
            0.58f, 0.78f, 0.40f, 0.52f, 0.28f, 0.46f, 0.34f, 0.22f, 0.16f
        )
    }
}
