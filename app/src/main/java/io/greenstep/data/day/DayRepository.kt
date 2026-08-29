package io.greenstep.data.day

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate

interface DayRepository {
    fun getDay(date: LocalDate): Flow<Day?>
    fun getDays(range: ClosedRange<LocalDate>): Flow<List<Day>>
    suspend fun getAllDays(): List<Day>
    fun getAllDaysFlow(): Flow<List<Day>>
    fun getFirstDay(): Flow<Day?>
    fun getTreeCount(): Flow<Int>
    suspend fun upsertDay(day: Day)
    suspend fun updateDaySettings(settings: DaySettings)
}

class DayRepositoryImpl(private val dao: DayDao) : DayRepository {
    override fun getDay(date: LocalDate): Flow<Day?> = dao.getDay(date)
    override fun getDays(range: ClosedRange<LocalDate>): Flow<List<Day>> = dao.getDays(range.start, range.endInclusive)
    override suspend fun getAllDays(): List<Day> = dao.getAllDays().first()
    override fun getAllDaysFlow(): Flow<List<Day>> = dao.getAllDays()
    override fun getFirstDay(): Flow<Day?> = dao.getFirstDay()
    override fun getTreeCount(): Flow<Int> = dao.getTreeCount()
    override suspend fun upsertDay(day: Day) = dao.upsert(day)
    override suspend fun updateDaySettings(settings: DaySettings) = dao.updateDaySettings(settings)
}
