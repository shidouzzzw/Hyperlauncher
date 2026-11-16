package com.gafure.hyperlauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gafure.hyperlauncher.data.settings.ThemeMode

private val DarkColors = darkColorScheme(
    primary = HyperBlue,
    onPrimary = Color.White,
    secondary = HyperOrange,
    onSecondary = Color.Black,
    background = HyperInk,
    onBackground = Color(0xFFF5F6F8),
    surface = HyperInkElevated,
    onSurface = Color(0xFFF5F6F8),
    surfaceVariant = Color(0xFF24272E),
    onSurfaceVariant = Color(0xFFB9BDC6),
)

private val LightColors = lightColorScheme(
    primary = HyperBlue,
    onPrimary = Color.White,
    secondary = HyperOrange,
    onSecondary = Color.Black,
    background = HyperPaper,
    onBackground = HyperInk,
    surface = HyperPaperElevated,
    onSurface = HyperInk,
    surfaceVariant = Color(0xFFE7E9EF),
    onSurfaceVariant = Color(0xFF4A4F59),
)

/** Resolves the user's [ThemeMode] against the device setting. */
@Composable
fun ThemeMode.isDarkTheme(): Boolean = when (this) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
}

/**
 * Dark and light schemes follow the system setting unless the user picked an override in
 * settings; every brightness-dependent surface reads this one flag.
 */
@Composable
fun HyperLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = HyperTypography,
        content = content,
    )
}
