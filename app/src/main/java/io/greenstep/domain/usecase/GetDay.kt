package io.greenstep.domain.usecase

import io.greenstep.data.day.Day
import io.greenstep.data.day.DayRepository
import io.greenstep.data.day.SettingsCompat
import io.greenstep.data.day.of
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetDay(private val repo: DayRepository) {
    operator fun invoke(date: LocalDate): Flow<Day> = repo.getDay(date).map { day ->
        day ?: Day.of(date, SettingsCompat(), 0)
    }
}
