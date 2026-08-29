package io.greenstep.data.history

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "days")
data class Day(
    @PrimaryKey val date: String,
    val steps: Int,
    val distanceMeters: Int = 0,
    val calories: Int = 0,
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM days ORDER BY date DESC")
    fun observeDays(): Flow<List<Day>>

    @Query("SELECT * FROM days ORDER BY date DESC")
    suspend fun getDays(): List<Day>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: Day)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(days: List<Day>)

    @Query("DELETE FROM days")
    suspend fun clear()
}

@Database(entities = [Day::class], version = 1, exportSchema = false)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}

class HistoryRepository(
    private val dao: HistoryDao,
) {
    fun observeDays(): Flow<List<Day>> = dao.observeDays()

    suspend fun getDays(): List<Day> = dao.getDays()
}
