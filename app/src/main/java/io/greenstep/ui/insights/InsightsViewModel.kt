package io.greenstep.ui.insights

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class InsightsUiState(
    val stepsLast14: List<Int> = emptyList(),
    val todaySteps: Int = 0,
    val weeklyAvg: Int = 0,
    val bestDaySteps: Int = 0,
    val bestDayLabel: String = "",
)

class InsightsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState
}
