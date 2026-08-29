package io.greenstep.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

object GreenStepMotion {
    val expressiveSpring: AnimationSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 300f)
    val gentleSpring: AnimationSpec<Float> = spring(dampingRatio = 0.9f, stiffness = 200f)
    val softSpring: AnimationSpec<Float> = spring(dampingRatio = 0.85f, stiffness = 180f)
    val bouncySpring: AnimationSpec<Float> = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
    val standardSpring: AnimationSpec<Float> = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
    val pressSpring: AnimationSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 300f)
    fun <T> expressiveSpringSpec(): AnimationSpec<T> = spring(dampingRatio = 0.7f, stiffness = 300f)
    fun <T> gentleSpringSpec(): AnimationSpec<T> = spring(dampingRatio = 0.9f, stiffness = 200f)
    fun <T> standardSpringSpec(): AnimationSpec<T> = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
    fun <T> pressSpringSpec(): AnimationSpec<T> = spring(dampingRatio = 0.7f, stiffness = 300f)
}

data class MotionScheme(
    val expressiveSpring: AnimationSpec<Float> = GreenStepMotion.expressiveSpring,
    val gentleSpring: AnimationSpec<Float> = GreenStepMotion.gentleSpring,
    val standardSpring: AnimationSpec<Float> = GreenStepMotion.standardSpring,
    val pressSpring: AnimationSpec<Float> = GreenStepMotion.pressSpring,
    val reduceMotion: Boolean = false
)

val LocalMotionScheme = staticCompositionLocalOf { MotionScheme() }

@Composable
fun ProvideMotionScheme(reduceMotion: Boolean = false, content: @Composable () -> Unit) {
    val scheme = if (reduceMotion) MotionScheme(
        expressiveSpring = GreenStepMotion.gentleSpring,
        gentleSpring = GreenStepMotion.gentleSpring,
        standardSpring = GreenStepMotion.gentleSpring,
        pressSpring = GreenStepMotion.gentleSpring,
        reduceMotion = true
    ) else MotionScheme(reduceMotion = false)
    androidx.compose.runtime.CompositionLocalProvider(LocalMotionScheme provides scheme) { content() }
}
