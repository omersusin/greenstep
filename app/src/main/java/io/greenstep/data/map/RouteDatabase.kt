package io.greenstep.data.map

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromLatLngList(points: List<LatLng>): String = json.encodeToString(points)

    @TypeConverter
    fun toLatLngList(value: String): List<LatLng> {
        if (value.isBlank()) return emptyList()
        return try { json.decodeFromString(value) } catch (_: Exception) { emptyList() }
    }
}

@Database(entities = [RouteEntity::class], version = 1, exportSchema = false)
@androidx.room.TypeConverters(Converters::class)
abstract class RouteDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao
}
