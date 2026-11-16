package com.gafure.hyperlauncher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.gafure.hyperlauncher.model.AppInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the launchable-app list. The list is built on [Dispatchers.Default] (icons included) and
 * published as an immutable [StateFlow]; the only refresh triggers are the first load and
 * package add/remove/change broadcasts, so no thread blocks the main thread on PackageManager.
 */
class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val iconCache = AppIconCache(
        packageManager = packageManager,
        iconSizePx = AppIconCache.iconSizePx(context.resources.displayMetrics.density),
    )
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _apps = MutableStateFlow<ImmutableList<AppInfo>>(persistentListOf())
    val apps: StateFlow<ImmutableList<AppInfo>> = _apps.asStateFlow()

    private var receiverRegistered = false
    private val refreshInFlight = AtomicBoolean(false)
    private var refreshJob: Job? = null

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context?, intent: Intent?) {
            intent?.data?.schemeSpecificPart?.let(iconCache::evict)
            refresh()
        }
    }

    fun start() {
        if (!receiverRegistered) {
            ContextCompat.registerReceiver(
                context,
                packageReceiver,
                packageFilter(),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            receiverRegistered = true
        }
        scope.launch { refreshNow() }
    }

    /**
     * Debounced refresh for package broadcasts: installs fire several broadcasts in a
     * burst, and each full query re-touches PackageManager, so coalesce them.
     */
    fun refresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            delay(REFRESH_DEBOUNCE_MS)
            refreshNow()
        }
    }

    private suspend fun refreshNow() {
        if (!refreshInFlight.compareAndSet(false, true)) return
        try {
            _apps.value = queryApps()
        } finally {
            refreshInFlight.set(false)
        }
    }

    private fun queryApps(): ImmutableList<AppInfo> {
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved: List<ResolveInfo> = packageManager.queryIntentActivities(
            launcherIntent,
            PackageManager.MATCH_ALL,
        )
        val ownPackage = context.packageName

        // Shape the list with pure, unit-tested logic; only then touch the icon cache.
        val entries = resolved.asSequence()
            .mapNotNull { info ->
                val activityInfo = info.activityInfo ?: return@mapNotNull null
                val packageName = activityInfo.packageName ?: return@mapNotNull null
                val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                AppEntry(
                    label = label.ifEmpty { packageName },
                    packageName = packageName,
                    activityName = activityInfo.name,
                )
            }
            .toList()
            .withoutPackage(ownPackage)
            .distinctByKey()
            .sortedByLabel()

        return entries.map { entry ->
            AppInfo(
                label = entry.label,
                packageName = entry.packageName,
                activityName = entry.activityName,
                icon = iconCache.getOrLoad(entry.packageName),
            )
        }.toImmutableList()
    }

    /** Picks up to [slots] dock apps: phone, messages, browser and camera when installed. */
    fun pickDockApps(apps: List<AppInfo>, slots: Int = DOCK_SLOTS): ImmutableList<AppInfo> {
        if (apps.isEmpty() || slots <= 0) return persistentListOf()
        val byKey = apps.associateBy { it.key }
        return DockPicker.pick(
            apps = apps.map { AppEntry(it.label, it.packageName, it.activityName) },
            slots = slots,
            hintGroups = DOCK_HINTS,
        ).mapNotNull { byKey[it.key] }.toImmutableList()
    }

    fun launch(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setClassName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        runCatching { context.startActivity(intent) }
    }

    fun openAppInfo(app: AppInfo) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", app.packageName, null),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    private fun packageFilter(): IntentFilter = IntentFilter().apply {
        addAction(Intent.ACTION_PACKAGE_ADDED)
        addAction(Intent.ACTION_PACKAGE_REMOVED)
        addAction(Intent.ACTION_PACKAGE_REPLACED)
        addAction(Intent.ACTION_PACKAGE_CHANGED)
        addDataScheme("package")
    }

    private companion object {
        const val REFRESH_DEBOUNCE_MS = 600L
        const val DOCK_SLOTS = 4
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
