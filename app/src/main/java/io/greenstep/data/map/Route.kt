package io.greenstep.data.map

import kotlinx.serialization.Serializable

@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class Route(
    val id: String,
    val points: List<LatLng>,
    val distanceKm: Double,
    val durationMs: Long,
    val saved: Boolean,
)
