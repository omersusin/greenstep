package io.greenstep.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

enum class ShapeFamily { Rounded, Squircle, Cut }

val RoundedShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val SquircleShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(28.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

val CutShapes = Shapes(
    extraSmall = CutCornerShape(4.dp),
    small = CutCornerShape(8.dp),
    medium = CutCornerShape(12.dp),
    large = CutCornerShape(16.dp),
    extraLarge = CutCornerShape(24.dp)
)

fun shapesFor(family: ShapeFamily): Shapes = when (family) {
    ShapeFamily.Rounded -> RoundedShapes
    ShapeFamily.Squircle -> SquircleShapes
    ShapeFamily.Cut -> CutShapes
}
