package com.gafure.hyperlauncher.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val STORE_NAME = "launcher_settings"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = STORE_NAME)

/**
 * Persists [LauncherSettings] in Preferences DataStore.
 *
 * The store is constructor-injected (with a Context convenience constructor) so tests can
 * drive it with a temporary file. Corrupt files degrade to defaults instead of crashing at
 * startup, and [settings] is distinct-until-changed so the UI only recomposes on real edits.
 */
class SettingsStore(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.settingsDataStore)

    private fun prefs(): Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val settings: Flow<LauncherSettings> = prefs()
        .map { it.toLauncherSettings() }
        .distinctUntilChanged()

    /**
     * Applies a transform to the current settings inside a single atomic edit. The transform
     * runs on the DataStore scope, so concurrent updates cannot interleave.
     */
    suspend fun update(transform: (LauncherSettings) -> LauncherSettings) {
        dataStore.edit { prefs ->
            val target = transform(prefs.toLauncherSettings()).normalized()
            prefs[SettingsKeys.GRID_COLUMNS] = target.gridColumns
            prefs[SettingsKeys.DRAWER_COLUMNS] = target.drawerColumns
            prefs[SettingsKeys.ICON_SIZE] = target.iconSize.id
            prefs[SettingsKeys.SHOW_LABELS] = target.showLabels
            prefs[SettingsKeys.THEME_MODE] = target.themeMode.id
            prefs[SettingsKeys.ANIMATION_SCALE] = target.animationScale
            prefs[SettingsKeys.SCRIM_STRENGTH] = target.scrimStrength
        }
    }
}
