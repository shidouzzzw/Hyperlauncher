package com.gafure.hyperlauncher.ui

import androidx.compose.runtime.Immutable
import com.gafure.hyperlauncher.data.settings.LauncherSettings
import com.gafure.hyperlauncher.model.AppInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Everything the home screen draws, already immutable so composables stay skippable. */
@Immutable
data class HomeUiState(
    val homeApps: ImmutableList<AppInfo> = persistentListOf(),
    val drawerApps: ImmutableList<AppInfo> = persistentListOf(),
    val dockApps: ImmutableList<AppInfo> = persistentListOf(),
    val settings: LauncherSettings = LauncherSettings(),
    val isLoading: Boolean = true,
)
