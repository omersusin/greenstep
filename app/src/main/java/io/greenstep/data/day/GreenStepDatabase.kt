package io.greenstep.data.day

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.greenstep.data.map.RouteEntity
import io.greenstep.data.map.RouteDao
import io.greenstep.data.day.Converters as DayConverters
import io.greenstep.data.map.Converters as RouteConverters

@Database(entities = [Day::class, RouteEntity::class], version = 1, exportSchema = false)
@TypeConverters(DayConverters::class, RouteConverters::class)
abstract class GreenStepDatabase : RoomDatabase() {
    abstract fun dayDao(): DayDao
    abstract fun routeDao(): RouteDao

    companion object {
        @Volatile
        private var INSTANCE: GreenStepDatabase? = null

        fun getDatabase(context: Context): GreenStepDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                GreenStepDatabase::class.java,
                "greenstep_database"
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
