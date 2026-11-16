package com.gafure.hyperlauncher.data

import java.text.Collator

/**
 * Platform-free description of a launchable app. All list shaping (sort, dedupe, dock
 * picks) operates on [AppEntry] so it is unit-testable on the JVM; icons are attached
 * afterwards by [AppRepository].
 */
data class AppEntry(
    val label: String,
    val packageName: String,
    val activityName: String,
) {
    val key: String = "$packageName/$activityName"
}

/** Locale-aware label ordering, matching what the device shows in Settings. */
fun List<AppEntry>.sortedByLabel(collator: Collator = Collator.getInstance()): List<AppEntry> =
    sortedWith { first, second -> collator.compare(first.label, second.label) }

fun List<AppEntry>.distinctByKey(): List<AppEntry> = distinctBy { it.key }

fun List<AppEntry>.withoutPackage(packageName: String): List<AppEntry> =
    filterNot { it.packageName == packageName }

/**
 * Picks dock slots by package-name hints (dialer, messenger, browser, camera, …) in
 * priority order, then fills remaining slots in list order. Pure and deterministic.
 */
object DockPicker {

    fun pick(
        apps: List<AppEntry>,
        slots: Int,
        hintGroups: List<List<String>>,
    ): List<AppEntry> {
        if (apps.isEmpty() || slots <= 0) return emptyList()
        val picked = LinkedHashMap<String, AppEntry>()
        for (hints in hintGroups) {
            if (picked.size >= slots) break
            apps.firstOrNull { app ->
                val pkg = app.packageName.lowercase()
                app.key !in picked && hints.any { pkg.contains(it) }
            }?.let { picked[it.key] = it }
        }
        for (app in apps) {
            if (picked.size >= slots) break
            picked[app.key] = app
        }
        return picked.values.toList()
    }
}
