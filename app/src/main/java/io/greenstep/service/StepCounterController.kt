package io.greenstep.service

import io.greenstep.domain.usecase.GetDay
import io.greenstep.domain.usecase.IncrementStepCount
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class StepCounterEvent(val stepCount: Int, val eventDate: LocalDate)

class StepCounterController(
    private val getDay: GetDay,
    private val incrementStepCount: IncrementStepCount,
    private val coroutineScope: CoroutineScope,
    currentDateFlow: StateFlow<LocalDate>
) {
    private val _stats = MutableStateFlow(StepCounterState(LocalDate.now(), 0, 0, 0f, 0))
    val stats: StateFlow<StepCounterState> = _stats.asStateFlow()
    private var getStatsJob: Job? = null

    init {
        coroutineScope.launch { currentDateFlow.collect { getStats(it) } }
    }

    private fun getStats(date: LocalDate) {
        getStatsJob?.cancel()
        getStatsJob = getDay(date).onEach { day ->
            _stats.value = StepCounterState(
                date = date,
                steps = day.steps,
                goal = day.goal,
                distanceKm = day.distanceKm,
                calories = day.calories.roundToInt()
            )
        }.launchIn(coroutineScope)
    }

    private val rawStepSensorReadings = MutableStateFlow(StepCounterEvent(0, LocalDate.MIN))
    private var previousStepCount: Int? = null

    init {
        rawStepSensorReadings.drop(1).onEach { event ->
            val diff = event.stepCount - (previousStepCount ?: event.stepCount)
            previousStepCount = event.stepCount
            if (diff > 0) incrementStepCount(event.eventDate, diff)
        }.launchIn(coroutineScope)
    }

    fun onStepCountChanged(newStepCount: Int, eventDate: LocalDate) {
        rawStepSensorReadings.value = StepCounterEvent(newStepCount, eventDate)
    }

    fun onStepDetected(eventDate: LocalDate) {
        coroutineScope.launch { incrementStepCount(eventDate, 1) }
    }
}
