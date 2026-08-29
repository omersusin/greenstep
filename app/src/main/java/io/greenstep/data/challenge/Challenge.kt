package io.greenstep.data.challenge

data class Challenge(
    val id: String,
    val title: String,
    val daysLeft: Int,
    val progress: Float,
    val participants: Int
)
