package com.gafure.hyperlauncher.data.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/** Preference keys, single source of truth for the settings DataStore. */
internal object SettingsKeys {
    val GRID_COLUMNS = intPreferencesKey("grid_columns")
    val DRAWER_COLUMNS = intPreferencesKey("drawer_columns")
    val ICON_SIZE = stringPreferencesKey("icon_size")
    val SHOW_LABELS = booleanPreferencesKey("show_labels")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val ANIMATION_SCALE = floatPreferencesKey("animation_scale")
    val SCRIM_STRENGTH = floatPreferencesKey("scrim_strength")
}

/**
 * Preferences → [LauncherSettings]. Pure so it can be unit-tested without DataStore, and
 * defensive: out-of-range or unknown stored values fall back to safe defaults instead of
 * crashing or producing an unusable grid.
 */
internal fun Preferences.toLauncherSettings(): LauncherSettings {
    val grid = this[SettingsKeys.GRID_COLUMNS] ?: DEFAULT_GRID_COLUMNS
    val drawer = this[SettingsKeys.DRAWER_COLUMNS] ?: DEFAULT_DRAWER_COLUMNS
    val animation = this[SettingsKeys.ANIMATION_SCALE] ?: 1f
    val scrim = this[SettingsKeys.SCRIM_STRENGTH] ?: DEFAULT_SCRIM_STRENGTH
    return LauncherSettings(
        gridColumns = grid.coerceIn(MIN_COLUMNS, MAX_COLUMNS),
        drawerColumns = drawer.coerceIn(MIN_COLUMNS, MAX_COLUMNS),
        iconSize = IconSize.fromId(this[SettingsKeys.ICON_SIZE]),
        showLabels = this[SettingsKeys.SHOW_LABELS] ?: true,
        themeMode = ThemeMode.fromId(this[SettingsKeys.THEME_MODE]),
        animationScale = if (animation.isNaN()) {
            1f
        } else {
            animation.coerceIn(MIN_ANIMATION_SCALE, MAX_ANIMATION_SCALE)
        },
        scrimStrength = if (scrim.isNaN()) {
            DEFAULT_SCRIM_STRENGTH
        } else {
            scrim.coerceIn(MIN_SCRIM_STRENGTH, MAX_SCRIM_STRENGTH)
        },
    )
}

/** Clamps a settings value before it is written, so storage never holds invalid state. */
fun LauncherSettings.normalized(): LauncherSettings = copy(
    gridColumns = gridColumns.coerceIn(MIN_COLUMNS, MAX_COLUMNS),
    drawerColumns = drawerColumns.coerceIn(MIN_COLUMNS, MAX_COLUMNS),
    animationScale = if (animationScale.isNaN()) {
        1f
    } else {
        animationScale.coerceIn(MIN_ANIMATION_SCALE, MAX_ANIMATION_SCALE)
    },
    scrimStrength = if (scrimStrength.isNaN()) {
        DEFAULT_SCRIM_STRENGTH
    } else {
        scrimStrength.coerceIn(MIN_SCRIM_STRENGTH, MAX_SCRIM_STRENGTH)
    },
)
