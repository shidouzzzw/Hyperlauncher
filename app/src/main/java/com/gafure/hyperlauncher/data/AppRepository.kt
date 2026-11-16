package com.gafure.hyperlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.gafure.hyperlauncher.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator

/**
 * Reads the launchable apps from [PackageManager] and starts them.
 *
 * All package queries and icon decoding happen on [Dispatchers.IO]; the UI only receives
 * ready-to-draw [ImageBitmap]s.
 */
class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun loadApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved: List<ResolveInfo> = packageManager.queryIntentActivities(
            launcherIntent,
            PackageManager.MATCH_ALL,
        )
        val ownPackage = context.packageName
        val collator = Collator.getInstance()

        resolved.asSequence()
            .mapNotNull { info ->
                val activityInfo = info.activityInfo ?: return@mapNotNull null
                val packageName = activityInfo.packageName ?: return@mapNotNull null
                if (packageName == ownPackage) return@mapNotNull null
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                AppInfo(
                    label = label.ifEmpty { packageName },
                    packageName = packageName,
                    activityName = activityInfo.name,
                    icon = loadIcon(packageName),
                )
            }
            .distinctBy { it.key }
            .sortedWith { first, second -> collator.compare(first.label, second.label) }
            .toList()
    }

    /** Picks up to [slots] dock apps: phone, messages, browser and camera when installed. */
    fun pickDockApps(apps: List<AppInfo>, slots: Int = DOCK_SLOTS): List<AppInfo> {
        if (apps.isEmpty() || slots <= 0) return emptyList()
        val picked = LinkedHashMap<String, AppInfo>()
        for (hint in DOCK_HINTS) {
            if (picked.size >= slots) break
            val match = apps.firstOrNull { app ->
                val pkg = app.packageName.lowercase()
                pkg !in picked && hint.any { pkg.contains(it) }
            }
            if (match != null) picked[match.key] = match
        }
        for (app in apps) {
            if (picked.size >= slots) break
            picked[app.key] = app
        }
        return picked.values.toList()
    }

    fun launch(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        runCatching { context.startActivity(intent) }
    }

    private fun loadIcon(packageName: String): ImageBitmap {
        val drawable: Drawable = runCatching { packageManager.getApplicationIcon(packageName) }
            .getOrElse { packageManager.defaultActivityIcon }
        return drawable
            .toBitmap(width = ICON_SIZE_PX, height = ICON_SIZE_PX, config = Bitmap.Config.ARGB_8888)
            .asImageBitmap()
    }

    private companion object {
        const val DOCK_SLOTS = 4
        const val ICON_SIZE_PX = 192
        val DOCK_HINTS: List<List<String>> = listOf(
            listOf("dialer", "phone"),
            listOf("messag", "mms", "sms"),
            listOf("chrome", "browser", "internet"),
            listOf("camera"),
            listOf("contact"),
            listOf("gallery"),
            listOf("settings"),
        )
    }
}
