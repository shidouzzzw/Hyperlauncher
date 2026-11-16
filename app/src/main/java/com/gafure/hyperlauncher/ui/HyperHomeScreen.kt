package com.gafure.hyperlauncher.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gafure.hyperlauncher.data.AppRepository
import com.gafure.hyperlauncher.model.AppInfo
import com.gafure.hyperlauncher.ui.components.AppDock
import com.gafure.hyperlauncher.ui.components.AppGridPager
import com.gafure.hyperlauncher.ui.components.ClockSection
import com.gafure.hyperlauncher.ui.theme.ScrimDark
import com.gafure.hyperlauncher.ui.theme.ScrimLight

/**
 * The whole home screen: wallpaper scrim, clock, paged app grid and the fixed dock.
 * The activity window is transparent, so the system wallpaper shows through.
 */
@Composable
fun HyperHomeScreen(repository: AppRepository) {
    val apps by produceState<List<AppInfo>?>(initialValue = null) {
        value = repository.loadApps()
    }
    val dockApps = remember(repository, apps) {
        apps?.let { repository.pickDockApps(it) }.orEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperScrim()

        val loadedApps = apps
        if (loadedApps != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                ClockSection(
                    modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 16.dp),
                )
                Spacer(modifier = Modifier.height(18.dp))
                AppGridPager(
                    apps = loadedApps,
                    onAppClick = repository::launch,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                AppDock(
                    apps = dockApps,
                    onAppClick = repository::launch,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
        }
    }
}

/** Light-dark aware darkening gradient over the wallpaper, denser behind the clock. */
@Composable
private fun WallpaperScrim(modifier: Modifier = Modifier) {
    val target = if (isSystemInDarkTheme()) ScrimDark else ScrimLight
    val scrim by animateColorAsState(targetValue = target, label = "wallpaperScrim")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = scrim.alpha),
                        Color.Black.copy(alpha = scrim.alpha * 0.62f),
                        Color.Black.copy(alpha = scrim.alpha * 0.85f),
                    ),
                ),
            ),
    )
}
