package io.greenstep.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.greenstep.GreenStepApplication
import io.greenstep.data.day.Day
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as GreenStepApplication).greenStepDatabase.dayDao()
    val days: StateFlow<List<Day>> = dao.getAllDays().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
