package io.greenstep.data.map

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

    companion object {
        @Volatile private var INSTANCE: RouteDatabase? = null
        fun getInstance(context: Context): RouteDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, RouteDatabase::class.java, "routes.db").build().also { INSTANCE = it }
        }
    }
}

object GpxExporter {
    fun toGpx(points: List<LatLng>, routeName: String = "GreenStep Route"): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append('\n')
        sb.append("""<gpx version="1.1" creator="GreenStep" xmlns="http://www.topografix.com/GPX/1/1">""").append('\n')
        sb.append("  <trk><name>").append(escape(routeName)).append("</name><trkseg>\n")
        for (p in points) sb.append("    <trkpt lat=\"").append(p.latitude).append("\" lon=\"").append(p.longitude).append("\"></trkpt>\n")
        sb.append("  </trkseg></trk>\n</gpx>")
        return sb.toString()
    }
    private fun escape(s: String): String = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}

fun haversineKm(a: LatLng, b: LatLng): Double {
    val r = 6371.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLng = Math.toRadians(b.longitude - a.longitude)
    val s1 = kotlin.math.sin(dLat / 2)
    val s2 = kotlin.math.sin(dLng / 2)
    val h = s1 * s1 + kotlin.math.cos(Math.toRadians(a.latitude)) * kotlin.math.cos(Math.toRadians(b.latitude)) * s2 * s2
    return 2 * r * kotlin.math.atan2(kotlin.math.sqrt(h), kotlin.math.sqrt(1 - h))
}

fun totalDistanceKm(points: List<LatLng>): Double {
    if (points.size < 2) return 0.0
    var sum = 0.0
    for (i in 1 until points.size) sum += haversineKm(points[i - 1], points[i])
    return sum
}

fun formatPace(durationMs: Long, distanceKm: Double): String {
    if (distanceKm <= 0.001) return "--:--"
    val paceMinPerKm = (durationMs / 60000.0) / distanceKm
    val m = paceMinPerKm.toInt()
    val s = ((paceMinPerKm - m) * 60).toInt().coerceIn(0, 59)
    return "%d:%02d /km".format(m, s)
}

fun formatDuration(durationMs: Long): String {
    val s = (durationMs / 1000) % 60
    val m = (durationMs / 60000) % 60
    val h = durationMs / 3600000
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
