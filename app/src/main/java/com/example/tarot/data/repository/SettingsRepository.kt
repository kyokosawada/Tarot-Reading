package com.example.tarot.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension to create DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context
) {
    // Define preference keys
    private object PreferencesKeys {
        val ALLOW_REVERSED_CARDS = booleanPreferencesKey("allow_reversed_cards")
    }

    // Flow to observe reversed cards setting
    val allowReversedCards: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ALLOW_REVERSED_CARDS] ?: true // Default to true
    }

    // Update reversed cards setting
    suspend fun setAllowReversedCards(allow: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALLOW_REVERSED_CARDS] = allow
        }
    }

    // Get current value synchronously (for immediate use)
    suspend fun getCurrentAllowReversedCards(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ALLOW_REVERSED_CARDS] ?: true
        }.first()
    }
}