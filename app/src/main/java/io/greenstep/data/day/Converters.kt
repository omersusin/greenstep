package io.greenstep.data.day

import androidx.room.TypeConverter
import java.time.LocalDate

object Converters {
    @TypeConverter
    fun localDateToTimestamp(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun timestampToLocalDate(timestamp: Long): LocalDate = LocalDate.ofEpochDay(timestamp)
}
