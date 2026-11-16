package com.gafure.hyperlauncher.model

import androidx.compose.ui.graphics.ImageBitmap

/**
 * A launchable app on the device.
 *
 * The icon is decoded once in [com.gafure.hyperlauncher.data.AppRepository] and kept as an
 * [ImageBitmap] so lists never touch [android.content.pm.PackageManager] during composition.
 */
data class AppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap,
) {
    /** Stable identity for `LazyVerticalGrid` keys and pointer-input restarts. */
    val key: String = "$packageName/$activityName"
}
