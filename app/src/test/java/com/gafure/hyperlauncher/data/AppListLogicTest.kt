package com.gafure.hyperlauncher.data

import java.text.Collator
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppListLogicTest {

    private fun entry(label: String, pkg: String, activity: String = "Main"): AppEntry =
        AppEntry(label = label, packageName = pkg, activityName = activity)

    @Test
    fun sorting_orders_labels_with_a_collator() {
        val apps = listOf(
            entry("Zebra", "com.z"),
            entry("apple", "com.a"),
            entry("Banana", "com.b"),
        )
        val sorted = apps.sortedByLabel(Collator.getInstance(Locale.ENGLISH))
        val labels = sorted.map { it.label }
        assertEquals(labels.sortedWith(Collator.getInstance(Locale.ENGLISH)), labels)
    }

    @Test
    fun own_package_is_filtered_out() {
        val apps = listOf(entry("Me", "com.gafure.hyperlauncher"), entry("Other", "com.other"))
        val result = apps.withoutPackage("com.gafure.hyperlauncher")
        assertEquals(listOf("com.other"), result.map { it.packageName })
    }

    @Test
    fun distinctByKey_keeps_first_occurrence() {
        val apps = listOf(
            entry("One", "com.same", "A"),
            entry("Two", "com.same", "B"),
            entry("One", "com.same", "A"),
        )
        val result = apps.distinctByKey()
        assertEquals(2, result.size)
        assertEquals("One", result.first().label)
    }

    @Test
    fun dock_picker_prefers_hinted_packages_in_priority_order() {
        val apps = listOf(
            entry("Settings", "com.android.settings"),
            entry("Phone", "com.samsung.android.dialer"),
            entry("Messages", "com.samsung.android.messaging"),
            entry("Camera", "com.sec.android.app.camera"),
            entry("Browser", "com.android.chrome"),
        )
        val hints = listOf(
            listOf("dialer", "phone"),
            listOf("messag", "mms", "sms"),
            listOf("chrome", "browser", "internet"),
            listOf("camera"),
        )
        val picked = DockPicker.pick(apps, slots = 4, hintGroups = hints)
        assertEquals(
            listOf(
                "com.samsung.android.dialer",
                "com.samsung.android.messaging",
                "com.android.chrome",
                "com.sec.android.app.camera",
            ),
            picked.map { it.packageName },
        )
    }

    @Test
    fun dock_picker_fills_missing_slots_in_list_order() {
        val apps = listOf(entry("Alpha", "com.alpha"), entry("Beta", "com.beta"))
        val picked = DockPicker.pick(apps, slots = 4, hintGroups = listOf(listOf("dialer")))
        assertEquals(listOf("com.alpha", "com.beta"), picked.map { it.packageName })
    }

    @Test
    fun dock_picker_never_duplicates_an_app() {
        val apps = listOf(entry("Phone", "com.phone.dialer"))
        val picked = DockPicker.pick(
            apps,
            slots = 4,
            hintGroups = listOf(listOf("dialer", "phone"), listOf("phone")),
        )
        assertEquals(1, picked.size)
    }

    @Test
    fun dock_picker_respects_slot_count_and_empty_input() {
        val apps = listOf(entry("A", "com.a"), entry("B", "com.b"), entry("C", "com.c"))
        assertEquals(2, DockPicker.pick(apps, slots = 2, hintGroups = emptyList()).size)
        assertTrue(DockPicker.pick(emptyList(), slots = 4, hintGroups = emptyList()).isEmpty())
        assertTrue(DockPicker.pick(apps, slots = 0, hintGroups = emptyList()).isEmpty())
    }
}
