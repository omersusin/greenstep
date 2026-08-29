package io.greenstep.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.greenstep.ui.activity.ActivityScreen
import io.greenstep.ui.history.HistoryScreen
import io.greenstep.ui.home.HomeScreen
import io.greenstep.ui.insights.InsightsScreen
import io.greenstep.ui.map.MapScreen
import io.greenstep.ui.settings.SettingsScreen
import io.greenstep.ui.shop.ShopScreen

@Composable
fun GreenStepNav() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomBarDestinations.forEach { dest ->
                    val selected = currentRoute?.let { route ->
                        backStackEntry?.destination?.hierarchy?.any { it.route == dest.route }
                    } ?: (currentRoute == dest.route)

                    NavigationBarItem(
                        selected = selected == true || currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = stringResource(dest.labelRes),
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(dest.labelRes),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                            )
                        },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destination.Home.route) { HomeScreen(onShopClick = { navController.navigate(Destination.Shop.route) }) }
            composable(Destination.Activity.route)  { ActivityScreen() }
            composable(Destination.Map.route)       { MapScreen() }
            composable(Destination.History.route)   { HistoryScreen() }
            composable(Destination.Insights.route)  { InsightsScreen() }
            composable(Destination.Shop.route)      { ShopScreen() }
            composable(Destination.Settings.route)  { SettingsScreen() }
        }
    }
}
