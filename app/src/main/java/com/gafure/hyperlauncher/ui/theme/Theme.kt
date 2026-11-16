package com.gafure.hyperlauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

/**
 * Dark and light schemes follow the system setting. The launcher does not expose its own
 * theme switch; [isSystemInDarkTheme] drives every brightness-dependent surface.
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
