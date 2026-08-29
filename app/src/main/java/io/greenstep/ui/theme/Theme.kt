package io.greenstep.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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

private val MeadowColors = lightColorScheme(
    primary               = MeadowPrimary,
    onPrimary             = MeadowOnPrimary,
    primaryContainer      = MeadowPrimaryContainer,
    onPrimaryContainer    = MeadowOnPrimaryContainer,
    secondary             = StreakFlame,
    onSecondary           = Color.White,
    secondaryContainer    = StreakFlameDim,
    onSecondaryContainer  = Color.Black,
    tertiary              = GemPurple,
    onTertiary            = Color.White,
    tertiaryContainer     = GemPurpleDim,
    onTertiaryContainer   = Color.Black,
    background            = MeadowBackground,
    onBackground          = MeadowOnBackground,
    surface               = MeadowSurface,
    onSurface             = MeadowOnSurface,
    surfaceVariant        = MeadowSurfaceVariant,
    onSurfaceVariant      = MeadowOnSurfaceVariant,
    error                 = ErrorRedDark,
    onError               = Color.White,
)

private val NightColors = darkColorScheme(
    primary               = NightPrimary,
    onPrimary             = NightOnPrimary,
    primaryContainer      = NightPrimaryContainer,
    onPrimaryContainer    = NightOnPrimaryContainer,
    secondary             = StreakFlameDim,
    onSecondary           = Color.Black,
    secondaryContainer    = StreakFlame,
    onSecondaryContainer  = Color.Black,
    tertiary              = GemPurpleDim,
    onTertiary            = Color.Black,
    tertiaryContainer     = GemPurple,
    onTertiaryContainer   = Color.White,
    background            = NightBackground,
    onBackground          = NightOnBackground,
    surface               = NightSurface,
    onSurface             = NightOnSurface,
    surfaceVariant        = NightSurfaceVariant,
    onSurfaceVariant      = NightOnSurfaceVariant,
    error                 = ErrorRed,
    onError               = Color.Black,
)

@Composable
fun GreenStepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeChoice: ThemeChoice? = null,
    content: @Composable () -> Unit,
) {
    val colors = when (themeChoice) {
        ThemeChoice.MEADOW -> MeadowColors
        ThemeChoice.NIGHT -> NightColors
        ThemeChoice.DARK -> DarkColors
        ThemeChoice.LIGHT -> LightColors
        null -> if (darkTheme) DarkColors else LightColors
    }

    val isLight = colors.background.luminance() > 0.5f
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = isLight
            insets.isAppearanceLightNavigationBars = isLight
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = GreenStepTypography,
        content = content,
    )
}
