package com.gafure.hyperlauncher.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Minimal in-memory [DataStore] so layout tests need no file system or DataStore actor. */
private class FakePreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = state

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences {
        val updated = transform(state.value.toMutablePreferences()).toPreferences()
        state.value = updated
        return updated
    }
}

class HomeLayoutStoreTest {

    private lateinit var store: HomeLayoutStore

    @Before
    fun setUp() {
        store = HomeLayoutStore(FakePreferencesDataStore())
    }

    @Test
    fun starts_unseeded_with_an_empty_home() = runTest {
        assertFalse(store.isSeeded())
        assertEquals(emptyList<String>(), store.homeKeys.first())
    }

    @Test
    fun seed_persists_ordered_unique_keys_and_marks_seeded() = runTest {
        store.seedIfNeeded(listOf("b", "a", "b"))
        assertTrue(store.isSeeded())
        assertEquals(listOf("b", "a"), store.homeKeys.first())
    }

    @Test
    fun second_seed_never_overwrites_the_first() = runTest {
        store.seedIfNeeded(listOf("first"))
        store.seedIfNeeded(listOf("second"))
        assertEquals(listOf("first"), store.homeKeys.first())
    }

    @Test
    fun update_appends_without_duplicating_and_marks_seeded() = runTest {
        store.updateHomeKeys { keys -> keys + "x" }
        store.updateHomeKeys { keys -> keys + "x" + "y" }
        assertTrue(store.isSeeded())
        assertEquals(listOf("x", "y"), store.homeKeys.first())
    }

    @Test
    fun update_can_remove_a_key() = runTest {
        store.seedIfNeeded(listOf("a", "b", "c"))
        store.updateHomeKeys { keys -> keys - "b" }
        assertEquals(listOf("a", "c"), store.homeKeys.first())
    }
}
