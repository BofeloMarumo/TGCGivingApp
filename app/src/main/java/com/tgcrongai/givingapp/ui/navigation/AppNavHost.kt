package com.tgcrongai.givingapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tgcrongai.givingapp.ui.dashboard.DashboardScreen
import com.tgcrongai.givingapp.ui.followup.FollowUpScreen
import com.tgcrongai.givingapp.ui.settings.SettingsScreen
import com.tgcrongai.givingapp.ui.templates.TemplatesScreen
import com.tgcrongai.givingapp.ui.theme.Teal600
import com.tgcrongai.givingapp.ui.theme.InkSoft

private sealed class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Tab("dashboard", "Giving", Icons.Filled.Home)
    object FollowUp : Tab("followup", "Follow Up", Icons.Filled.Repeat)
    object Templates : Tab("templates", "Templates", Icons.Filled.Description)
    object Settings : Tab("settings", "Settings", Icons.Filled.Settings)
}

private val tabs = listOf(Tab.Dashboard, Tab.FollowUp, Tab.Templates, Tab.Settings)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentRoute?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Teal600,
                            selectedTextColor = Teal600,
                            unselectedIconColor = InkSoft,
                            unselectedTextColor = InkSoft
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Dashboard.route,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Tab.Dashboard.route) { DashboardScreen() }
            composable(Tab.FollowUp.route) { FollowUpScreen() }
            composable(Tab.Templates.route) { TemplatesScreen() }
            composable(Tab.Settings.route) { SettingsScreen() }
        }
    }
}
