package io.greenstep.data.day

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DayDao {
    @Query("SELECT * FROM days WHERE date = :date")
    fun getDay(date: LocalDate): Flow<Day?>

    @Query("SELECT * FROM days WHERE date BETWEEN :start AND :end")
    fun getDays(start: LocalDate, end: LocalDate): Flow<List<Day>>

    @Query("SELECT * FROM days ORDER BY date DESC")
    fun getAllDays(): Flow<List<Day>>

    @Query("SELECT * FROM days ORDER BY date ASC LIMIT 1")
    fun getFirstDay(): Flow<Day?>

    @Query("SELECT COUNT(*) FROM days WHERE steps >= goal")
    fun getTreeCount(): Flow<Int>

    @Upsert
    suspend fun upsert(day: Day)

    @Update
    suspend fun update(day: Day)

    @Update(entity = Day::class)
    suspend fun updateDaySettings(settings: DaySettings)
}
