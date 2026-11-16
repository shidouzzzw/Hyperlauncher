package com.gafure.hyperlauncher.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gafure.hyperlauncher.ui.components.AppDock
import com.gafure.hyperlauncher.ui.components.AppDrawer
import com.gafure.hyperlauncher.ui.components.AppGridPager
import com.gafure.hyperlauncher.ui.components.ClockSection

private val DrawerOpenThreshold = 72.dp
private const val HomeBlurRadius = 26f

/**
 * Home screen: wallpaper scrim, clock, paged grid of the user's chosen apps and the fixed
 * dock. Swiping up from empty space opens the [AppDrawer], which blurs this content via
 * RenderEffect. All geometry (columns, icon size, labels, animation, scrim) comes from
 * persisted settings.
 */
@Composable
fun HyperHomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    var drawerOpen by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    val homeBlur = remember { mutableFloatStateOf(0f) }
    val iconSize = remember(settings.iconSize) { settings.iconSize.sizeDp.dp }

    val onOpenApp = remember(viewModel) { viewModel::openApp }
    val onRemoveFromHome = remember(viewModel) { viewModel::removeFromHome }
    val onAppInfo = remember(viewModel) { viewModel::openAppInfo }
    val onAddToHome = remember(viewModel) { viewModel::addToHome }

    // Animate the blur without recomposition: the value is only read in graphicsLayer.
    LaunchedEffect(drawerOpen) {
        animate(
            initialValue = homeBlur.floatValue,
            targetValue = if (drawerOpen) HomeBlurRadius else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ) { value, _ -> homeBlur.floatValue = value }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        WallpaperScrim(scrimStrength = settings.scrimStrength)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val radius = homeBlur.floatValue
                    renderEffect = if (radius > 0.5f) {
                        BlurEffect(radiusX = radius, radiusY = radius, edgeTreatment = TileMode.Clamp)
                    } else {
                        null
                    }
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .pointerInput(Unit) {
                    val openThreshold = DrawerOpenThreshold.toPx()
                    var dragged = 0f
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragged <= -openThreshold) drawerOpen = true
                            dragged = 0f
                        },
                        onDragCancel = { dragged = 0f },
                    ) { change, dragAmount ->
                        dragged += dragAmount
                        change.consume()
                    }
                },
        ) {
            ClockSection(
                modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 16.dp),
            )
            Spacer(modifier = Modifier.height(18.dp))
            AppGridPager(
                apps = uiState.homeApps,
                columns = settings.gridColumns,
                iconSize = iconSize,
                showLabels = settings.showLabels,
                animationScale = settings.animationScale,
                onAppClick = onOpenApp,
                onAppRemove = onRemoveFromHome,
                onAppInfo = onAppInfo,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            AppDock(
                apps = uiState.dockApps,
                onAppClick = onOpenApp,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        if (drawerOpen) {
            AppDrawer(
                apps = uiState.drawerApps,
                drawerColumns = settings.drawerColumns,
                iconSize = iconSize,
                showLabels = settings.showLabels,
                animationScale = settings.animationScale,
                onClose = { drawerOpen = false },
                onOpenApp = onOpenApp,
                onAddToHome = onAddToHome,
                onAppInfo = onAppInfo,
                onOpenSettings = {
                    drawerOpen = false
                    settingsOpen = true
                },
            )
        }

        if (settingsOpen) {
            SettingsSheet(
                settings = settings,
                onDismiss = { settingsOpen = false },
                onGridColumnsChange = viewModel::setGridColumns,
                onDrawerColumnsChange = viewModel::setDrawerColumns,
                onIconSizeChange = viewModel::setIconSize,
                onShowLabelsChange = viewModel::setShowLabels,
                onThemeModeChange = viewModel::setThemeMode,
                onAnimationScaleChange = viewModel::setAnimationScale,
                onScrimStrengthChange = viewModel::setScrimStrength,
            )
        }
    }
}

/** Dark/light aware darkening gradient over the wallpaper, scaled by the scrim setting. */
@Composable
private fun WallpaperScrim(
    scrimStrength: Float,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val brush = remember(isDark, scrimStrength) {
        val peak = (if (isDark) 0.82f else 0.62f) * scrimStrength
        Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = peak),
                Color.Black.copy(alpha = peak * 0.62f),
                Color.Black.copy(alpha = peak * 0.85f),
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush),
    )
}
