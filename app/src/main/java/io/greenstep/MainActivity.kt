package io.greenstep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.greenstep.ui.navigation.GreenStepNav
import io.greenstep.ui.theme.GreenStepTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreenStepTheme {
                GreenStepNav()
            }
        }
    }
}
