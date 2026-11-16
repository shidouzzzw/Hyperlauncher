package com.gafure.hyperlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.gafure.hyperlauncher.ui.HyperHomeScreen
import com.gafure.hyperlauncher.ui.theme.HyperLauncherTheme

/**
 * Home launcher entry point. The transparent window (see `Theme.HyperLauncher`) keeps the
 * system wallpaper visible; the Compose tree draws the scrim, clock, grid and dock on top.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
        )
        setContent {
            HyperLauncherTheme {
                HyperHomeScreen()
            }
        }
    }
}
