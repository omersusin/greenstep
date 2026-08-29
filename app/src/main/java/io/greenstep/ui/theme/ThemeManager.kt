package io.greenstep.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeChoice { LIGHT, DARK, MEADOW, NIGHT }

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

object ThemeManager {
    private val KEY_THEME = stringPreferencesKey("theme_choice")
    private val HAPTICS_KEY = stringPreferencesKey("haptics_enabled")

    fun themeFlow(context: Context): Flow<ThemeChoice> = context.themeDataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME] ?: ThemeChoice.LIGHT.name
        try { ThemeChoice.valueOf(raw) } catch (_: IllegalArgumentException) { ThemeChoice.LIGHT }
    }

    suspend fun setTheme(context: Context, choice: ThemeChoice) {
        context.themeDataStore.edit { it[KEY_THEME] = choice.name }
    }

    fun hapticsEnabledFlow(context: Context): Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[HAPTICS_KEY]?.toBooleanStrictOrNull() ?: true
    }

    suspend fun setHapticsEnabled(context: Context, enabled: Boolean) {
        context.themeDataStore.edit { it[HAPTICS_KEY] = enabled.toString() }
    }
}
