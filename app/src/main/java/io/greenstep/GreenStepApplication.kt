package io.greenstep

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.room.Room
import io.greenstep.data.day.GreenStepDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class GreenStepApplication : Application() {
    lateinit var greenStepDatabase: GreenStepDatabase

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        greenStepDatabase = GreenStepDatabase.getDatabase(this)
        registerMidnightTimer()
    }

    private fun registerMidnightTimer() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
        }
        registerReceiver(midnightReceiver, filter)
    }

    private val midnightReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val today = LocalDate.now()
            if (today != _currentDate.value) _currentDate.value = today
        }
    }
}
