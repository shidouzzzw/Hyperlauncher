package com.gafure.hyperlauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val STORE_NAME = "home_layout"
private const val KEY_SEPARATOR = "\u0001"

private val Context.homeLayoutDataStore: DataStore<Preferences> by preferencesDataStore(name = STORE_NAME)

private val HOME_KEYS = stringPreferencesKey("home_app_keys")
private val SEEDED = booleanPreferencesKey("home_seeded")

/**
 * Persists which apps sit on the home screen, in order, as a single ordered string so the
 * grid survives process death and reboots.
 *
 * The [DataStore] is constructor-injected so unit tests can supply a file-backed test
 * instance; production passes the app-scoped delegate. A corrupt preferences file falls
 * back to empty instead of crashing the launcher at startup.
 */
class HomeLayoutStore(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.homeLayoutDataStore)

    private fun prefs(): Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val homeKeys: Flow<List<String>> = prefs().map { prefs ->
        prefs[HOME_KEYS].toKeyList()
    }

    suspend fun isSeeded(): Boolean = prefs().first()[SEEDED] == true

    suspend fun seedIfNeeded(keys: List<String>) {
        dataStore.edit { prefs ->
            if (prefs[SEEDED] == true) return@edit
            prefs[HOME_KEYS] = keys.distinct().joinToString(KEY_SEPARATOR)
            prefs[SEEDED] = true
        }
    }

    suspend fun updateHomeKeys(transform: (List<String>) -> List<String>) {
        dataStore.edit { prefs ->
            val updated = transform(prefs[HOME_KEYS].toKeyList()).distinct()
            prefs[HOME_KEYS] = updated.joinToString(KEY_SEPARATOR)
            prefs[SEEDED] = true
        }
    }

    private fun String?.toKeyList(): List<String> =
        this?.split(KEY_SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()
}
