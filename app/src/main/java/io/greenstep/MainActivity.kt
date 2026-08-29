package io.greenstep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.greenstep.ui.navigation.GreenStepNav
import io.greenstep.ui.theme.AppTheme
import io.greenstep.ui.theme.GreenStepTheme
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
            GreenStepTheme(appTheme = appTheme, shapeFamily = shapeFamily) {
                GreenStepNav()
            }
        }
    }
}
