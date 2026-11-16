package com.gafure.hyperlauncher.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * A launchable app on the device.
 *
 * Marked [Immutable] so Compose can skip recomposition; [icon] is decoded once in
 * [com.gafure.hyperlauncher.data.AppRepository] and cached, never rebuilt per frame.
 */
@Immutable
data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap,
) {
    /** Stable identity for lazy keys, pointer-input restarts and the saved home layout. */
    val key: String = "$packageName/$activityName"
}
