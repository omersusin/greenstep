package io.greenstep.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

object GreenStepMotion {
    val expressiveSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    val standardSpring: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    fun <T> expressiveSpringSpec(): AnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    fun <T> standardSpringSpec(): AnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

data class MotionScheme(
    val expressiveSpring: AnimationSpec<Float> = GreenStepMotion.expressiveSpring,
    val standardSpring: AnimationSpec<Float> = GreenStepMotion.standardSpring
)

val LocalMotionScheme = staticCompositionLocalOf { MotionScheme() }

@Composable
fun ProvideMotionScheme(content: @Composable () -> Unit) {
    content()
}
