package io.greenstep.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary               = Green400,
    onPrimary             = Color.Black,
    primaryContainer      = Green800,
    onPrimaryContainer    = Green100,
    secondary             = StreakFlameDim,
    onSecondary           = Color.Black,
    secondaryContainer    = StreakFlame,
    onSecondaryContainer  = Color.Black,
    tertiary              = GemPurpleDim,
    onTertiary            = Color.Black,
    tertiaryContainer     = GemPurple,
    onTertiaryContainer   = Color.White,
    background            = DarkBackground,
    onBackground          = DarkOnBackground,
    surface               = DarkSurface,
    onSurface             = DarkOnSurface,
    surfaceVariant        = DarkSurfaceVariant,
    onSurfaceVariant      = DarkOnSurfaceVariant,
    error                 = ErrorRed,
    onError               = Color.Black,
)

private val LightColors = lightColorScheme(
    primary               = Green700,
    onPrimary             = Color.White,
    primaryContainer      = Green100,
    onPrimaryContainer    = Green900,
    secondary             = StreakFlame,
    onSecondary           = Color.White,
    secondaryContainer    = StreakFlameDim,
    onSecondaryContainer  = Color.Black,
    tertiary              = GemPurple,
    onTertiary            = Color.White,
    tertiaryContainer     = GemPurpleDim,
    onTertiaryContainer   = Color.Black,
    background            = LightBackground,
    onBackground          = LightOnBackground,
    surface               = LightSurface,
    onSurface             = LightOnSurface,
    surfaceVariant        = LightSurfaceVariant,
    onSurfaceVariant      = LightOnSurfaceVariant,
    error                 = ErrorRedDark,
    onError               = Color.White,
)

@Composable
fun GreenStepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !darkTheme
            insets.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = GreenStepTypography,
        content = content,
    )
}
