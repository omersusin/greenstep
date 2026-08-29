package io.greenstep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.greenstep.ui.navigation.GreenStepNav
import io.greenstep.ui.onboarding.OnboardingScreen
import io.greenstep.ui.theme.AppTheme
import io.greenstep.ui.theme.GreenStepTheme
import io.greenstep.ui.theme.ProvideMotionScheme
import io.greenstep.ui.theme.ShapeFamily
import io.greenstep.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val ctx = this
        setContent {
            val appTheme by ThemeManager.themeFlow(ctx).collectAsState(initial = AppTheme.System)
            val shapeFamily by ThemeManager.shapeFlow(ctx).collectAsState(initial = ShapeFamily.Rounded)
            val reduceMotion by ThemeManager.reduceMotionFlow(ctx).collectAsState(initial = false)
            val onboardingCompleted by ThemeManager.onboardingCompletedFlow(ctx).collectAsState(initial = null)
            var finished by remember { mutableStateOf(false) }
            val showOnboarding = onboardingCompleted == false && !finished
            GreenStepTheme(appTheme = appTheme, shapeFamily = shapeFamily) {
                ProvideMotionScheme(reduceMotion = reduceMotion) {
                    if (onboardingCompleted == null) {
                        androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator()
                        }

                    } else if (showOnboarding) {
                        OnboardingScreen(onFinished = { finished = true })
                    } else {
                        GreenStepNav()
                    }
                }
            }
        }
    }
}
