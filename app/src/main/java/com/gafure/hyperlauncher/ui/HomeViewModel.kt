package com.gafure.hyperlauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gafure.hyperlauncher.HyperLauncherApp
import com.gafure.hyperlauncher.data.AppRepository
import com.gafure.hyperlauncher.data.HomeLayoutStore
import com.gafure.hyperlauncher.model.AppInfo
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Joins the installed app list with the saved home layout. Nothing here runs on the main
 * thread beyond collecting state; DataStore and PackageManager work happen on their own
 * dispatchers.
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository = (application as HyperLauncherApp).repository
    private val layoutStore = HomeLayoutStore(application)

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
    ) { apps, homeKeys ->
        val appsByKey = apps.associateBy { it.key }
        HomeUiState(
            homeApps = homeKeys.mapNotNull { appsByKey[it] }.toImmutableList(),
            drawerApps = apps,
            dockApps = repository.pickDockApps(apps),
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

    private companion object {
        const val DEFAULT_HOME_SLOTS = 16
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
