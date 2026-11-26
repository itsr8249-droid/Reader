package com.example.data.services

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class ReadAloudManager(context: Context) {

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var isReady = false
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            isReady = (status == TextToSpeech.SUCCESS)
            if (isReady) {
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun togglePreview(voiceProfile: String) {
        if (_isSpeaking.value) {
            stop()
            return
        }
        speakText(
            text = "Welcome to Reader. Your read aloud voice preview is ready.",
            voiceProfile = voiceProfile
        )
    }

    fun speakText(text: String, voiceProfile: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        if (_isSpeaking.value) {
            stop()
            return
        }
        val engine = tts ?: return
        if (!isReady) return

        val (locale, pitch, rate) = when {
            voiceProfile.contains("US") -> Triple(Locale.US, 1.0f, 0.96f)
            voiceProfile.contains("AU") -> Triple(Locale("en", "AU"), 1.05f, 1.0f)
            else -> Triple(Locale.UK, 0.94f, 0.92f)
        }
        engine.language = locale
        engine.setPitch(pitch)
        engine.setSpeechRate(rate)
        _isSpeaking.value = true
        engine.speak(
            clean.take(3800),
            TextToSpeech.QUEUE_FLUSH,
            null,
            "reader_read_aloud"
        )
    }

    fun stop() {
        runCatching { tts?.stop() }
        _isSpeaking.value = false
    }

    fun release() {
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
        _isSpeaking.value = false
    }
}
