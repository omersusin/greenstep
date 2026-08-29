package io.greenstep.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Top-level navigation destinations. Phase 1 keeps four tabs.
 * Achievements and Settings are reached from Home/Settings entry points,
 * not as separate tabs.
 */
enum class Destination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
) {
    Home(
        route = "home",
        labelRes = io.greenstep.R.string.nav_home,
        icon = Icons.Outlined.Home,
    ),
    Activity(
        route = "activity",
        labelRes = io.greenstep.R.string.nav_activity,
        icon = Icons.Outlined.DirectionsRun,
    ),
    History(
        route = "history",
        labelRes = io.greenstep.R.string.nav_history,
        icon = Icons.Outlined.History,
    ),
    Insights(
        route = "insights",
        labelRes = io.greenstep.R.string.nav_insights,
        icon = Icons.Outlined.BarChart,
    ),
    Map(
        route = "map",
        labelRes = io.greenstep.R.string.nav_map,
        icon = Icons.Outlined.Map,
    ),
}

val BottomBarDestinations: List<Destination> = listOf(
    Destination.Home,
    Destination.Activity,
    Destination.Map,
    Destination.History,
    Destination.Insights,
)
