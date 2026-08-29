package io.greenstep.data.streak

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeParseException

private val Context.streakDataStore by preferencesDataStore(name = "streak_prefs")

class StreakStore(private val context: Context) {

    private object Keys {
        val CurrentStreak = intPreferencesKey("currentStreak")
        val LongestStreak = intPreferencesKey("longestStreak")
        val LastHitDate = stringPreferencesKey("lastHitDate")
        val FreezeTokens = intPreferencesKey("freezeTokens")
    }

    val streakFlow: Flow<Streak> = context.streakDataStore.data.map { prefs ->
        val current = prefs[Keys.CurrentStreak] ?: 0
        val longest = prefs[Keys.LongestStreak] ?: 0
        val lastHitRaw = prefs[Keys.LastHitDate]
        val lastHit = lastHitRaw?.let {
            try { LocalDate.parse(it) } catch (_: DateTimeParseException) { null }
        }
        val freezes = prefs[Keys.FreezeTokens] ?: 0
        Streak(current, longest, lastHit, freezes)
    }

    suspend fun incrementStreak() {
        context.streakDataStore.edit { prefs ->
            val today = LocalDate.now()
            val lastRaw = prefs[Keys.LastHitDate]
            val lastHit = lastRaw?.let {
                try { LocalDate.parse(it) } catch (_: DateTimeParseException) { null }
            }
            if (lastHit == today) return@edit
            val current = prefs[Keys.CurrentStreak] ?: 0
            val longest = prefs[Keys.LongestStreak] ?: 0
            val newCurrent = when {
                lastHit == null -> 1
                lastHit == today.minusDays(1) -> current + 1
                else -> 1
            }
            prefs[Keys.CurrentStreak] = newCurrent
            prefs[Keys.LongestStreak] = maxOf(longest, newCurrent)
            prefs[Keys.LastHitDate] = today.toString()
        }
    }

    suspend fun breakStreak() {
        context.streakDataStore.edit { prefs ->
            prefs[Keys.CurrentStreak] = 0
        }
    }

    suspend fun useFreeze(): Boolean {
        var consumed = false
        context.streakDataStore.edit { prefs ->
            val freezes = prefs[Keys.FreezeTokens] ?: 0
            if (freezes > 0) {
                prefs[Keys.FreezeTokens] = freezes - 1
                prefs[Keys.LastHitDate] = LocalDate.now().toString()
                if ((prefs[Keys.CurrentStreak] ?: 0) == 0) {
                    prefs[Keys.CurrentStreak] = 1
                }
                consumed = true
            }
        }
        return consumed
    }

    suspend fun addFreezeTokens(count: Int) {
        if (count <= 0) return
        context.streakDataStore.edit { prefs ->
            val c = prefs[Keys.FreezeTokens] ?: 0
            prefs[Keys.FreezeTokens] = c + count
        }
    }

    suspend fun setStreakForPreview(streak: Streak) {
        context.streakDataStore.edit { prefs ->
            prefs[Keys.CurrentStreak] = streak.current
            prefs[Keys.LongestStreak] = streak.longest
            prefs[Keys.FreezeTokens] = streak.freezes
            if (streak.lastHit != null) prefs[Keys.LastHitDate] = streak.lastHit.toString()
            else prefs.remove(Keys.LastHitDate)
        }
    }

    suspend fun clear() { context.streakDataStore.edit { it.clear() } }
}
