package io.greenstep.data.map

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val id: String,
    val pointsJson: String,
    val distanceKm: Double,
    val durationMs: Long,
    val saved: Boolean,
)

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY id DESC")
    fun observeAll(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RouteEntity)

    @Delete
    suspend fun delete(entity: RouteEntity)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM routes WHERE saved = 1 ORDER BY id DESC")
    fun observeSaved(): Flow<List<RouteEntity>>
}

fun RouteEntity.toDomain(converters: Converters): Route = Route(
    id = id,
    points = converters.toLatLngList(pointsJson),
    distanceKm = distanceKm,
    durationMs = durationMs,
    saved = saved,
)

fun Route.toEntity(converters: Converters): RouteEntity = RouteEntity(
    id = id,
    pointsJson = converters.fromLatLngList(points),
    distanceKm = distanceKm,
    durationMs = durationMs,
    saved = saved,
)
