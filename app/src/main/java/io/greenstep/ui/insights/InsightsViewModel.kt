package io.greenstep.ui.insights

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.greenstep.GreenStepApplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class InsightsUiState(
    val stepsLast14: List<Int> = emptyList(),
    val todaySteps: Int = 0,
    val weeklyAvg: Int = 0,
    val bestDaySteps: Int = 0,
    val bestDayLabel: String = ""
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as GreenStepApplication).greenStepDatabase.dayDao()
    val uiState: StateFlow<InsightsUiState> = dao.getAllDays().map { days ->
        val sorted = days.sortedBy { it.date }
        val last14 = if (sorted.size >= 14) sorted.takeLast(14) else sorted
        val stepsLast14 = last14.map { it.steps }
        val today = LocalDate.now()
        val todaySteps = days.find { it.date == today }?.steps ?: 0
        val weeklyAvg = if (stepsLast14.isEmpty()) 0 else stepsLast14.takeLast(7).average().toInt()
        val best = days.maxByOrNull { it.steps }
        InsightsUiState(
            stepsLast14 = stepsLast14,
            todaySteps = todaySteps,
            weeklyAvg = weeklyAvg,
            bestDaySteps = best?.steps ?: 0,
            bestDayLabel = best?.date?.format(DateTimeFormatter.ofPattern("MMM d")) ?: ""
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsUiState())
}
