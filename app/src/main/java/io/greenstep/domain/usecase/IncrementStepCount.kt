package io.greenstep.domain.usecase

import io.greenstep.data.day.DayRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.first

class IncrementStepCount(
    private val repository: DayRepository,
    private val getDay: GetDay
) {
    suspend operator fun invoke(date: LocalDate, by: Int) {
        if (by <= 0) return
        val day = getDay(date).first()
        val updated = day.copy(steps = day.steps + by)
        repository.upsertDay(updated)
    }
}
