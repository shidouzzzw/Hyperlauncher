package com.gafure.hyperlauncher.data.settings

import androidx.compose.runtime.Immutable

const val MIN_COLUMNS = 3
const val MAX_COLUMNS = 6
const val DEFAULT_GRID_COLUMNS = 4
const val DEFAULT_DRAWER_COLUMNS = 4
const val MIN_ANIMATION_SCALE = 0f
const val MAX_ANIMATION_SCALE = 2f
const val MIN_SCRIM_STRENGTH = 0.2f
const val MAX_SCRIM_STRENGTH = 1f
const val DEFAULT_SCRIM_STRENGTH = 0.7f

/** Icon size presets, in dp. */
enum class IconSize(val id: String, val sizeDp: Int) {
    Compact("compact", 46),
    Medium("medium", 54),
    Large("large", 62),
    ;

    companion object {
        fun fromId(id: String?): IconSize = entries.firstOrNull { it.id == id } ?: Medium
    }
}

/** Launcher theme choice; [System] defers to the device setting. */
enum class ThemeMode(val id: String) {
    System("system"),
    Light("light"),
    Dark("dark"),
    ;

    companion object {
        fun fromId(id: String?): ThemeMode = entries.firstOrNull { it.id == id } ?: System
    }
}

/**
 * Every user-facing setting. Immutable so Compose can skip, and every field is applied by
 * the UI — there are no decorative controls.
 */
@Immutable
data class LauncherSettings(
    val gridColumns: Int = DEFAULT_GRID_COLUMNS,
    val drawerColumns: Int = DEFAULT_DRAWER_COLUMNS,
    val iconSize: IconSize = IconSize.Medium,
    val showLabels: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val animationScale: Float = 1f,
    val scrimStrength: Float = DEFAULT_SCRIM_STRENGTH,
)
