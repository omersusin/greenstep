package io.greenstep.data.day

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "days")
data class Day(
    @PrimaryKey val date: LocalDate,
    val steps: Int = 0,
    val goal: Int,
    val stepLengthCm: Int = 72,
    val heightCm: Int = 182,
    val weightKg: Int = 70,
    val pace: Float = 1f
) {
    companion object

    val distanceKm: Float get() = steps * stepLengthCm / 100000f
    val calories: Float get() = 0.04f * steps * pace * (heightCm / 182f + weightKg / 70f - 1f)
    val carbonSaved: Float get() = steps * 0.1925f / 1000f
    val coins: Int get() = steps / 100
}

data class DaySettings(
    val date: LocalDate,
    val goal: Int,
    val stepLengthCm: Int = 72,
    val heightCm: Int = 182,
    val weightKg: Int = 70,
    val pace: Float = 1f
)

fun Day.Companion.of(date: LocalDate, settings: DaySettings, steps: Int = 0): Day = Day(
    date = date,
    steps = steps,
    goal = settings.goal,
    stepLengthCm = settings.stepLengthCm,
    heightCm = settings.heightCm,
    weightKg = settings.weightKg,
    pace = settings.pace
)

fun Day.Companion.of(date: LocalDate, settings: SettingsCompat, steps: Int = 0): Day = Day(
    date = date,
    steps = steps,
    goal = settings.dailyGoal,
    stepLengthCm = settings.stepLength,
    heightCm = settings.height,
    weightKg = settings.weight,
    pace = settings.pace.toFloat()
)

data class SettingsCompat(
    val dailyGoal: Int = 7500,
    val stepLength: Int = 72,
    val height: Int = 182,
    val weight: Int = 70,
    val pace: Double = 1.0
)

fun coinsForSteps(steps: Int): Int = steps / 100
