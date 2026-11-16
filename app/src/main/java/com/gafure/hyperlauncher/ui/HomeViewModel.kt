package com.gafure.hyperlauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gafure.hyperlauncher.HyperLauncherApp
import com.gafure.hyperlauncher.data.AppRepository
import com.gafure.hyperlauncher.data.HomeLayoutStore
import com.gafure.hyperlauncher.data.settings.IconSize
import com.gafure.hyperlauncher.data.settings.LauncherSettings
import com.gafure.hyperlauncher.data.settings.SettingsStore
import com.gafure.hyperlauncher.data.settings.ThemeMode
import com.gafure.hyperlauncher.model.AppInfo
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Joins the installed app list, the saved home layout and the persisted settings. Nothing
 * here runs on the main thread beyond collecting state; DataStore and PackageManager work
 * happen on their own dispatchers.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository = (application as HyperLauncherApp).repository
    private val layoutStore = HomeLayoutStore(application)
    private val settingsStore = SettingsStore(application)

    init {
        viewModelScope.launch {
            if (layoutStore.isSeeded()) return@launch
            // First launch: the first 16 installed apps seed the home screen.
            val installed = repository.apps.first { it.isNotEmpty() }
            layoutStore.seedIfNeeded(installed.take(DEFAULT_HOME_SLOTS).map { it.key })
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.apps,
        layoutStore.homeKeys,
        settingsStore.settings,
    ) { apps, homeKeys, settings ->
        val appsByKey = apps.associateBy { it.key }
        HomeUiState(
            homeApps = homeKeys.mapNotNull { appsByKey[it] }.toImmutableList(),
            drawerApps = apps,
            dockApps = repository.pickDockApps(apps),
            settings = settings,
            isLoading = apps.isEmpty(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = HomeUiState(),
    )

    fun openApp(app: AppInfo) = repository.launch(app)

    fun openAppInfo(app: AppInfo) = repository.openAppInfo(app)

    fun addToHome(app: AppInfo) {
        viewModelScope.launch {
            layoutStore.updateHomeKeys { keys -> if (app.key in keys) keys else keys + app.key }
        }
    }

    fun removeFromHome(app: AppInfo) {
        viewModelScope.launch {
            layoutStore.updateHomeKeys { keys -> keys - app.key }
        }
    }

    fun refreshApps() = repository.refresh()

    fun setGridColumns(columns: Int) = updateSettings { it.copy(gridColumns = columns) }

    fun setDrawerColumns(columns: Int) = updateSettings { it.copy(drawerColumns = columns) }

    fun setIconSize(iconSize: IconSize) = updateSettings { it.copy(iconSize = iconSize) }

    fun setShowLabels(show: Boolean) = updateSettings { it.copy(showLabels = show) }

    fun setThemeMode(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }

    fun setAnimationScale(scale: Float) = updateSettings { it.copy(animationScale = scale) }

    fun setScrimStrength(strength: Float) = updateSettings { it.copy(scrimStrength = strength) }

    private fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch { settingsStore.update(transform) }
    }

    private companion object {
        const val DEFAULT_HOME_SLOTS = 16
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
