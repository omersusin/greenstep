package io.greenstep.data.history

import io.greenstep.data.day.Day
import io.greenstep.data.day.DayDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class HistoryRepository(private val dao: DayDao) {
    fun observeDays(): Flow<List<Day>> = dao.getAllDays()
    suspend fun getDays(): List<Day> = dao.getAllDays().first()
}
