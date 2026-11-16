package com.gafure.hyperlauncher.data

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.roundToInt

/**
 * Decodes app icons exactly once, off the main thread, into screen-appropriate sizes and keeps
 * them in an [LruCache] (count-bounded, ~96 entries) so recomposition never touches a Drawable.
 */
internal class AppIconCache(
    private val packageManager: PackageManager,
    private val iconSizePx: Int,
) {

    private val cache = LruCache<String, ImageBitmap>(MAX_ENTRIES)

    fun getOrLoad(packageName: String): ImageBitmap =
        cache.get(packageName) ?: load(packageName).also { cache.put(packageName, it) }

    fun evict(packageName: String) {
        cache.remove(packageName)
    }

    private fun load(packageName: String): ImageBitmap {
        val drawable = runCatching { packageManager.getApplicationIcon(packageName) }
            .getOrElse { packageManager.defaultActivityIcon }
        return drawable
            .toBitmap(width = iconSizePx, height = iconSizePx, config = Bitmap.Config.ARGB_8888)
            .asImageBitmap()
    }

    companion object {
        private const val MAX_ENTRIES = 96
        private const val MIN_ICON_PX = 96
        private const val MAX_ICON_PX = 256
        private const val ICON_DP = 64f

        /** Icon bitmaps are only ever drawn at ~54dp, so ask for a little more than that in px. */
        fun iconSizePx(density: Float): Int =
            (density * ICON_DP).roundToInt().coerceIn(MIN_ICON_PX, MAX_ICON_PX)
    }
}
