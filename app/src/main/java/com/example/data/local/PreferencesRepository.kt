package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "reader_settings")

data class AppPreferences(
    val voiceProfile: String = "Calm — English (UK)",
    val language: String = "English",
    val isSeeded: Boolean = false
)

class PreferencesRepository(private val context: Context) {
    private object Keys {
        val VOICE_PROFILE = stringPreferencesKey("voice_profile")
        val LANGUAGE = stringPreferencesKey("language")
        val IS_SEEDED = booleanPreferencesKey("is_seeded")
    }

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            voiceProfile = prefs[Keys.VOICE_PROFILE] ?: "Calm — English (UK)",
            language = prefs[Keys.LANGUAGE] ?: "English",
            isSeeded = prefs[Keys.IS_SEEDED] ?: false
        )
    }

    suspend fun setVoiceProfile(profile: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VOICE_PROFILE] = profile
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language
        }
    }

    suspend fun markSeeded() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_SEEDED] = true
        }
    }
}
