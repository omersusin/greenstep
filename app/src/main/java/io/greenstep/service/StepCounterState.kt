package io.greenstep.service

import java.time.LocalDate

data class StepCounterState(
    val date: LocalDate,
    val steps: Int,
    val goal: Int,
    val distanceKm: Float,
    val calories: Int
)
