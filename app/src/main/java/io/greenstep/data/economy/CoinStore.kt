package io.greenstep.data.economy

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.coinDataStore by preferencesDataStore(name = "coin_prefs")

class CoinStore(private val context: Context) {

    private object Keys {
        val Balance = intPreferencesKey("coins_balance")
    }

    val balanceFlow: Flow<Int> = context.coinDataStore.data.map { prefs ->
        prefs[Keys.Balance] ?: 0
    }

    suspend fun addCoins(amount: Int) {
        if (amount <= 0) return
        context.coinDataStore.edit { prefs ->
            val cur = prefs[Keys.Balance] ?: 0
            prefs[Keys.Balance] = cur + amount
        }
    }

    suspend fun spendCoins(amount: Int): Boolean {
        if (amount <= 0) return false
        var success = false
        context.coinDataStore.edit { prefs ->
            val cur = prefs[Keys.Balance] ?: 0
            if (cur >= amount) {
                prefs[Keys.Balance] = cur - amount
                success = true
            }
        }
        return success
    }

    suspend fun clear() { context.coinDataStore.edit { it.clear() } }
}
