package com.gafure.hyperlauncher.data

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.roundToInt

/**
 * Decodes app icons exactly once, off the main thread, into screen-appropriate sizes.
 *
 * Backed by a plain synchronized [LinkedHashMap] instead of `androidx.collection.LruCache`
 * so the cache does not depend on a transitive artifact that is not declared in
 * `app/build.gradle.kts`. The repository owns one instance for the whole process, so decoded
 * bitmaps survive package-change refreshes and only the changed package is re-decoded.
 */
internal class AppIconCache(
    private val packageManager: PackageManager,
    private val iconSizePx: Int,
) {

    private val lock = Any()
    private val cache = object : LinkedHashMap<String, ImageBitmap>(ACCESS_CAPACITY, LOAD_FACTOR, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean =
            size > MAX_ENTRIES
    }

    fun getOrLoad(packageName: String): ImageBitmap {
        synchronized(lock) { cache[packageName] }?.let { return it }
        val decoded = load(packageName)
        synchronized(lock) { cache[packageName] = decoded }
        return decoded
    }

    fun evict(packageName: String) {
        synchronized(lock) { cache.remove(packageName) }
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
        private const val ACCESS_CAPACITY = 128
        private const val LOAD_FACTOR = 0.75f
        private const val MIN_ICON_PX = 96
        private const val MAX_ICON_PX = 256
        private const val ICON_DP = 64f

        /** Icon bitmaps are only ever drawn at ~54dp, so ask for a little more than that in px. */
        fun iconSizePx(density: Float): Int =
            (density * ICON_DP).roundToInt().coerceIn(MIN_ICON_PX, MAX_ICON_PX)
    }
}
