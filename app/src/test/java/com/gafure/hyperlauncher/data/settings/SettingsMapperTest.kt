package com.gafure.hyperlauncher.data.settings

import androidx.datastore.preferences.core.mutablePreferencesOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsMapperTest {

    @Test
    fun empty_preferences_give_documented_defaults() {
        val settings = mutablePreferencesOf().toLauncherSettings()
        assertEquals(DEFAULT_GRID_COLUMNS, settings.gridColumns)
        assertEquals(DEFAULT_DRAWER_COLUMNS, settings.drawerColumns)
        assertEquals(IconSize.Medium, settings.iconSize)
        assertTrue(settings.showLabels)
        assertEquals(ThemeMode.System, settings.themeMode)
        assertEquals(1f, settings.animationScale, 0f)
        assertEquals(DEFAULT_SCRIM_STRENGTH, settings.scrimStrength, 0f)
    }

    @Test
    fun stored_values_are_read_back() {
        val prefs = mutablePreferencesOf(
            SettingsKeys.GRID_COLUMNS to 5,
            SettingsKeys.DRAWER_COLUMNS to 6,
            SettingsKeys.ICON_SIZE to IconSize.Large.id,
            SettingsKeys.SHOW_LABELS to false,
            SettingsKeys.THEME_MODE to ThemeMode.Dark.id,
            SettingsKeys.ANIMATION_SCALE to 1.5f,
            SettingsKeys.SCRIM_STRENGTH to 0.4f,
        )
        val settings = prefs.toLauncherSettings()
        assertEquals(5, settings.gridColumns)
        assertEquals(6, settings.drawerColumns)
        assertEquals(IconSize.Large, settings.iconSize)
        assertEquals(false, settings.showLabels)
        assertEquals(ThemeMode.Dark, settings.themeMode)
        assertEquals(1.5f, settings.animationScale, 0f)
        assertEquals(0.4f, settings.scrimStrength, 0f)
    }

    @Test
    fun out_of_range_numbers_are_clamped() {
        val prefs = mutablePreferencesOf(
            SettingsKeys.GRID_COLUMNS to 99,
            SettingsKeys.DRAWER_COLUMNS to 0,
            SettingsKeys.ANIMATION_SCALE to 42f,
            SettingsKeys.SCRIM_STRENGTH to -3f,
        )
        val settings = prefs.toLauncherSettings()
        assertEquals(MAX_COLUMNS, settings.gridColumns)
        assertEquals(MIN_COLUMNS, settings.drawerColumns)
        assertEquals(MAX_ANIMATION_SCALE, settings.animationScale, 0f)
        assertEquals(MIN_SCRIM_STRENGTH, settings.scrimStrength, 0f)
    }

    @Test
    fun unknown_enum_ids_fall_back_to_defaults() {
        val prefs = mutablePreferencesOf(
            SettingsKeys.ICON_SIZE to "gigantic",
            SettingsKeys.THEME_MODE to "purple",
        )
        val settings = prefs.toLauncherSettings()
        assertEquals(IconSize.Medium, settings.iconSize)
        assertEquals(ThemeMode.System, settings.themeMode)
    }

    @Test
    fun nan_values_fall_back_to_defaults() {
        val prefs = mutablePreferencesOf(
            SettingsKeys.ANIMATION_SCALE to Float.NaN,
            SettingsKeys.SCRIM_STRENGTH to Float.NaN,
        )
        val settings = prefs.toLauncherSettings()
        assertEquals(1f, settings.animationScale, 0f)
        assertEquals(DEFAULT_SCRIM_STRENGTH, settings.scrimStrength, 0f)
    }

    @Test
    fun normalized_clamps_values_before_writing() {
        val normalized = LauncherSettings(
            gridColumns = 1,
            drawerColumns = 100,
            animationScale = 9f,
            scrimStrength = 0f,
        ).normalized()
        assertEquals(MIN_COLUMNS, normalized.gridColumns)
        assertEquals(MAX_COLUMNS, normalized.drawerColumns)
        assertEquals(MAX_ANIMATION_SCALE, normalized.animationScale, 0f)
        assertEquals(MIN_SCRIM_STRENGTH, normalized.scrimStrength, 0f)
    }
}
