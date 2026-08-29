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

enum class AppTheme { System, Light, Dark, Meadow, Ocean, Sunset }

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

private val MeadowDarkColors = darkColorScheme(
    primary               = MeadowDarkPrimary,
    onPrimary             = MeadowDarkOnPrimary,
    primaryContainer      = MeadowDarkPrimaryContainer,
    onPrimaryContainer    = MeadowDarkOnPrimaryContainer,
    secondary             = StreakFlameDim,
    onSecondary           = Color.Black,
    secondaryContainer    = StreakFlame,
    onSecondaryContainer  = Color.Black,
    tertiary              = GemPurpleDim,
    onTertiary            = Color.Black,
    tertiaryContainer     = GemPurple,
    onTertiaryContainer   = Color.White,
    background            = MeadowDarkBackground,
    onBackground          = MeadowDarkOnBackground,
    surface               = MeadowDarkSurface,
    onSurface             = MeadowDarkOnSurface,
    surfaceVariant        = MeadowDarkSurfaceVariant,
    onSurfaceVariant      = MeadowDarkOnSurfaceVariant,
    error                 = ErrorRed,
    onError               = Color.Black,
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

private val OceanColors = lightColorScheme(
    primary               = OceanPrimary,
    onPrimary             = OceanOnPrimary,
    primaryContainer      = OceanPrimaryContainer,
    onPrimaryContainer    = OceanOnPrimaryContainer,
    secondary             = StreakFlame,
    onSecondary           = Color.White,
    secondaryContainer    = StreakFlameDim,
    onSecondaryContainer  = Color.Black,
    tertiary              = GemPurple,
    onTertiary            = Color.White,
    tertiaryContainer     = GemPurpleDim,
    onTertiaryContainer   = Color.Black,
    background            = OceanBackground,
    onBackground          = OceanOnBackground,
    surface               = OceanSurface,
    onSurface             = OceanOnSurface,
    surfaceVariant        = OceanSurfaceVariant,
    onSurfaceVariant      = OceanOnSurfaceVariant,
    error                 = ErrorRedDark,
    onError               = Color.White,
)

private val OceanDarkColors = darkColorScheme(
    primary               = OceanDarkPrimary,
    onPrimary             = OceanDarkOnPrimary,
    primaryContainer      = OceanDarkPrimaryContainer,
    onPrimaryContainer    = OceanDarkOnPrimaryContainer,
    secondary             = StreakFlameDim,
    onSecondary           = Color.Black,
    secondaryContainer    = StreakFlame,
    onSecondaryContainer  = Color.Black,
    tertiary              = GemPurpleDim,
    onTertiary            = Color.Black,
    tertiaryContainer     = GemPurple,
    onTertiaryContainer   = Color.White,
    background            = OceanDarkBackground,
    onBackground          = OceanDarkOnBackground,
    surface               = OceanDarkSurface,
    onSurface             = OceanDarkOnSurface,
    surfaceVariant        = OceanDarkSurfaceVariant,
    onSurfaceVariant      = OceanDarkOnSurfaceVariant,
    error                 = ErrorRed,
    onError               = Color.Black,
)

private val SunsetColors = lightColorScheme(
    primary               = SunsetPrimary,
    onPrimary             = SunsetOnPrimary,
    primaryContainer      = SunsetPrimaryContainer,
    onPrimaryContainer    = SunsetOnPrimaryContainer,
    secondary             = StreakFlame,
    onSecondary           = Color.White,
    secondaryContainer    = StreakFlameDim,
    onSecondaryContainer  = Color.Black,
    tertiary              = GemPurple,
    onTertiary            = Color.White,
    tertiaryContainer     = GemPurpleDim,
    onTertiaryContainer   = Color.Black,
    background            = SunsetBackground,
    onBackground          = SunsetOnBackground,
    surface               = SunsetSurface,
    onSurface             = SunsetOnSurface,
    surfaceVariant        = SunsetSurfaceVariant,
    onSurfaceVariant      = SunsetOnSurfaceVariant,
    error                 = ErrorRedDark,
    onError               = Color.White,
)

private val SunsetDarkColors = darkColorScheme(
    primary               = SunsetDarkPrimary,
    onPrimary             = SunsetDarkOnPrimary,
    primaryContainer      = SunsetDarkPrimaryContainer,
    onPrimaryContainer    = SunsetDarkOnPrimaryContainer,
    secondary             = StreakFlameDim,
    onSecondary           = Color.Black,
    secondaryContainer    = StreakFlame,
    onSecondaryContainer  = Color.Black,
    tertiary              = GemPurpleDim,
    onTertiary            = Color.Black,
    tertiaryContainer     = GemPurple,
    onTertiaryContainer   = Color.White,
    background            = SunsetDarkBackground,
    onBackground          = SunsetDarkOnBackground,
    surface               = SunsetDarkSurface,
    onSurface             = SunsetDarkOnSurface,
    surfaceVariant        = SunsetDarkSurfaceVariant,
    onSurfaceVariant      = SunsetDarkOnSurfaceVariant,
    error                 = ErrorRed,
    onError               = Color.Black,
)

@Composable
fun GreenStepTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    appTheme: AppTheme = AppTheme.System,
    themeChoice: ThemeChoice? = null,
    shapeFamily: ShapeFamily = ShapeFamily.Rounded,
    content: @Composable () -> Unit,
) {
    val colors = when {
        themeChoice != null -> when (themeChoice) {
            ThemeChoice.MEADOW -> if (darkTheme) MeadowDarkColors else MeadowColors
            ThemeChoice.OCEAN -> if (darkTheme) OceanDarkColors else OceanColors
            ThemeChoice.SUNSET -> if (darkTheme) SunsetDarkColors else SunsetColors
            ThemeChoice.NIGHT -> NightColors
            ThemeChoice.DARK -> DarkColors
            ThemeChoice.LIGHT -> LightColors
            ThemeChoice.AUTO -> if (darkTheme) DarkColors else LightColors
        }
        else -> when (appTheme) {
            AppTheme.Meadow -> MeadowColors
            AppTheme.Ocean -> OceanColors
            AppTheme.Sunset -> SunsetColors
            AppTheme.Dark -> DarkColors
            AppTheme.Light -> LightColors
            AppTheme.System -> if (darkTheme) DarkColors else LightColors
        }
    }
    val shapes = shapesFor(shapeFamily)
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
        shapes = shapes,
        content = content,
    )
}
