package io.greenstep.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeChoice { LIGHT, DARK, MEADOW, OCEAN, SUNSET, NIGHT, AUTO }

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

object ThemeManager {
    private val KEY_THEME = stringPreferencesKey("theme_choice")
    private val KEY_SHAPE = stringPreferencesKey("shape_family")
    private val HAPTICS_KEY = stringPreferencesKey("haptics_enabled")
    private val UNITS_KEY = stringPreferencesKey("units")
    private val REDUCE_MOTION_KEY = stringPreferencesKey("reduce_motion")
    private val ONBOARDING_KEY = booleanPreferencesKey("onboarding_completed")
    private val DAILY_GOAL_KEY = intPreferencesKey("daily_goal")
    private val STEP_LENGTH_KEY = intPreferencesKey("step_length_cm")
    private val HEIGHT_KEY = intPreferencesKey("height_cm")
    private val WEIGHT_KEY = intPreferencesKey("weight_kg")
    private val PACE_KEY = floatPreferencesKey("pace_factor")

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
                    ThemeChoice.OCEAN -> AppTheme.Ocean
                    ThemeChoice.SUNSET -> AppTheme.Sunset
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
        AppTheme.Ocean -> ThemeChoice.OCEAN
        AppTheme.Sunset -> ThemeChoice.SUNSET
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
            ThemeChoice.OCEAN -> AppTheme.Ocean
            ThemeChoice.SUNSET -> AppTheme.Sunset
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

    fun onboardingCompletedFlow(context: Context): Flow<Boolean> = context.themeDataStore.data.map { it[ONBOARDING_KEY] ?: false }
    suspend fun setOnboardingCompleted(context: Context, completed: Boolean) { context.themeDataStore.edit { it[ONBOARDING_KEY] = completed } }

    fun dailyGoalFlow(context: Context): Flow<Int> = context.themeDataStore.data.map { it[DAILY_GOAL_KEY] ?: 7500 }
    suspend fun setDailyGoal(context: Context, goal: Int) { context.themeDataStore.edit { it[DAILY_GOAL_KEY] = goal.coerceIn(1000, 20000) } }

    fun stepLengthFlow(context: Context): Flow<Int> = context.themeDataStore.data.map { it[STEP_LENGTH_KEY] ?: 72 }
    suspend fun setStepLength(context: Context, v: Int) { context.themeDataStore.edit { it[STEP_LENGTH_KEY] = v.coerceIn(40, 120) } }

    fun heightFlow(context: Context): Flow<Int> = context.themeDataStore.data.map { it[HEIGHT_KEY] ?: 182 }
    suspend fun setHeight(context: Context, v: Int) { context.themeDataStore.edit { it[HEIGHT_KEY] = v.coerceIn(120, 220) } }

    fun weightFlow(context: Context): Flow<Int> = context.themeDataStore.data.map { it[WEIGHT_KEY] ?: 70 }
    suspend fun setWeight(context: Context, v: Int) { context.themeDataStore.edit { it[WEIGHT_KEY] = v.coerceIn(30, 200) } }

    fun paceFlow(context: Context): Flow<Float> = context.themeDataStore.data.map { it[PACE_KEY] ?: 1f }
    suspend fun setPace(context: Context, v: Float) { context.themeDataStore.edit { it[PACE_KEY] = v.coerceIn(0.5f, 2f) } }

    suspend fun clearAll(context: Context) { context.themeDataStore.edit { it.clear() } }
}
