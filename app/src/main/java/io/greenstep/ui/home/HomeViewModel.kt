package io.greenstep.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.greenstep.GreenStepApplication
import io.greenstep.data.day.Day
import io.greenstep.data.day.of
import io.greenstep.data.day.SettingsCompat
import io.greenstep.data.streak.Streak
import io.greenstep.data.streak.StreakStore
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val day: Day = Day.of(LocalDate.now(), SettingsCompat(), 0),
    val streak: Streak = Streak(),
    val yesterdaySteps: Int? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as GreenStepApplication).greenStepDatabase
    private val dao = db.dayDao()
    private val streakStore = StreakStore(application)
    private val app = application as GreenStepApplication

    val uiState: StateFlow<HomeUiState> = app.currentDate.flatMapLatest { today ->
        combine(
            dao.getDay(today).map { it ?: Day.of(today, SettingsCompat(), 0) },
            streakStore.streakFlow,
            dao.getDay(today.minusDays(1)).map { it?.steps }
        ) { day, streak, ySteps -> HomeUiState(day, streak, ySteps) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    val dayFlow: StateFlow<Day> = uiState.map { it.day }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState().day)

    val weeklyDays: StateFlow<List<Day>> = app.currentDate.flatMapLatest { today ->
        dao.getDays(today.minusDays(6), today)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
