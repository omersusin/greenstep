package io.greenstep.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeChoice { LIGHT, DARK, MEADOW, NIGHT, AUTO }

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

object ThemeManager {
    private val KEY_THEME = stringPreferencesKey("theme_choice")
    private val KEY_SHAPE = stringPreferencesKey("shape_family")
    private val HAPTICS_KEY = stringPreferencesKey("haptics_enabled")
    private val UNITS_KEY = stringPreferencesKey("units")
    private val REDUCE_MOTION_KEY = stringPreferencesKey("reduce_motion")

    fun themeFlow(context: Context): Flow<AppTheme> = context.themeDataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME]
        if (raw == null) return@map AppTheme.System
        try {
            AppTheme.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            try {
                when (ThemeChoice.valueOf(raw)) {
                    ThemeChoice.LIGHT -> AppTheme.Light
                    ThemeChoice.DARK -> AppTheme.Dark
                    ThemeChoice.MEADOW -> AppTheme.Meadow
                    ThemeChoice.NIGHT -> AppTheme.Dark
                    ThemeChoice.AUTO -> AppTheme.System
                }
            } catch (_: IllegalArgumentException) { AppTheme.System }
        }
    }

    fun themeChoiceFlow(context: Context): Flow<ThemeChoice> = context.themeDataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME] ?: ThemeChoice.AUTO.name
        try { ThemeChoice.valueOf(raw) } catch (_: IllegalArgumentException) {
            try { AppTheme.valueOf(raw).toThemeChoice() } catch (_: Exception) { ThemeChoice.AUTO }
        }
    }

    private fun AppTheme.toThemeChoice(): ThemeChoice = when (this) {
        AppTheme.Light -> ThemeChoice.LIGHT
        AppTheme.Dark -> ThemeChoice.DARK
        AppTheme.Meadow -> ThemeChoice.MEADOW
        AppTheme.Ocean -> ThemeChoice.MEADOW
        AppTheme.Sunset -> ThemeChoice.MEADOW
        AppTheme.System -> ThemeChoice.AUTO
    }

    suspend fun setTheme(context: Context, choice: AppTheme) {
        context.themeDataStore.edit { it[KEY_THEME] = choice.name }
    }

    suspend fun setTheme(context: Context, choice: ThemeChoice) {
        val mapped = when (choice) {
            ThemeChoice.LIGHT -> AppTheme.Light
            ThemeChoice.DARK -> AppTheme.Dark
            ThemeChoice.MEADOW -> AppTheme.Meadow
            ThemeChoice.NIGHT -> AppTheme.Dark
            ThemeChoice.AUTO -> AppTheme.System
        }
        setTheme(context, mapped)
    }

    suspend fun setThemeChoice(context: Context, choice: ThemeChoice) {
        setTheme(context, choice)
    }

    fun appThemeFlow(context: Context): Flow<AppTheme> = themeFlow(context)

    fun shapeFlow(context: Context): Flow<ShapeFamily> = context.themeDataStore.data.map { prefs ->
        val raw = prefs[KEY_SHAPE] ?: ShapeFamily.Rounded.name
        try { ShapeFamily.valueOf(raw) } catch (_: IllegalArgumentException) { ShapeFamily.Rounded }
    }

    suspend fun setShape(context: Context, family: ShapeFamily) {
        context.themeDataStore.edit { it[KEY_SHAPE] = family.name }
    }

    fun hapticsEnabledFlow(context: Context): Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[HAPTICS_KEY]?.toBooleanStrictOrNull() ?: true
    }

    suspend fun setHapticsEnabled(context: Context, enabled: Boolean) {
        context.themeDataStore.edit { it[HAPTICS_KEY] = enabled.toString() }
    }

    fun unitsFlow(context: Context): Flow<String> = context.themeDataStore.data.map { prefs ->
        prefs[UNITS_KEY] ?: "km"
    }

    suspend fun setUnits(context: Context, units: String) {
        context.themeDataStore.edit { it[UNITS_KEY] = units }
    }

    fun reduceMotionFlow(context: Context): Flow<Boolean> = context.themeDataStore.data.map { prefs ->
        prefs[REDUCE_MOTION_KEY]?.toBooleanStrictOrNull() ?: false
    }

    suspend fun setReduceMotion(context: Context, enabled: Boolean) {
        context.themeDataStore.edit { it[REDUCE_MOTION_KEY] = enabled.toString() }
    }
}
