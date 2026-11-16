package com.gafure.hyperlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gafure.hyperlauncher.ui.HomeViewModel
import com.gafure.hyperlauncher.ui.HyperHomeScreen
import com.gafure.hyperlauncher.ui.theme.HyperLauncherTheme
import com.gafure.hyperlauncher.ui.theme.isDarkTheme

/**
 * Home launcher entry point. The transparent window (see `Theme.HyperLauncher`) keeps the
 * system wallpaper visible; the Compose tree draws the scrim, clock, grid and dock on top.
 * The theme override lives in settings, so the Material theme is resolved here.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
        )
        setContent {
            val viewModel: HomeViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            HyperLauncherTheme(darkTheme = state.settings.themeMode.isDarkTheme()) {
                HyperHomeScreen(viewModel = viewModel)
            }
        }
    }
}
