package io.greenstep.data.streak

import java.time.LocalDate

data class Streak(
    val current: Int = 0,
    val longest: Int = 0,
    val lastHit: LocalDate? = null,
    val freezes: Int = 0
)
