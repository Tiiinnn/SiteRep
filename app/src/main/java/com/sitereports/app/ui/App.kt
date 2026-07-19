package com.sitereports.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.sitereports.app.data.ReportRepository
import com.sitereports.app.data.UnitRepository

private object Routes {
    const val Units = "units"
    const val Reports = "reports"
    const val AddUnit = "unit/add"
    const val NewReport = "report/new/{unitId}"
    const val ReportDetails = "report/{reportId}"

    fun newReport(id: Long) = "report/new/$id"
    fun reportDetails(id: Long) = "report/$id"
}

@Composable
fun SiteReportsApp(
    unitRepository: UnitRepository,
    reportRepository: ReportRepository,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == Routes.Units || currentRoute == Routes.Reports

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                    ) {
                        val colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary,
                            indicatorColor = Color.Transparent,
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.Units,
                            onClick = { navController.navigateTopLevel(Routes.Units) },
                            icon = { Icon(Icons.Outlined.Home, contentDescription = "Units") },
                            label = { Text("Units") },
                            colors = colors,
                        )
                        NavigationBarItem(
                            selected = currentRoute == Routes.Reports,
                            onClick = { navController.navigateTopLevel(Routes.Reports) },
                            icon = { Icon(Icons.Outlined.Description, contentDescription = "Reports") },
                            label = { Text("Reports") },
                            colors = colors,
                        )
                    }
                }
            }
        },
    ) { outerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Units,
            modifier = Modifier
                .padding(outerPadding)
                .windowInsetsPadding(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                ),
            enterTransition = {
                val switchingTabs = initialState.destination.route in setOf(Routes.Units, Routes.Reports) &&
                    targetState.destination.route in setOf(Routes.Units, Routes.Reports)
                if (switchingTabs) fadeIn(tween(160))
                else fadeIn(tween(180)) + slideInHorizontally(tween(220)) { it / 12 }
            },
            exitTransition = {
                val switchingTabs = initialState.destination.route in setOf(Routes.Units, Routes.Reports) &&
                    targetState.destination.route in setOf(Routes.Units, Routes.Reports)
                if (switchingTabs) fadeOut(tween(160))
                else fadeOut(tween(160)) + slideOutHorizontally(tween(220)) { -it / 12 }
            },
            popEnterTransition = {
                fadeIn(tween(160)) + slideInHorizontally(tween(180)) { -it / 12 }
            },
            popExitTransition = {
                fadeOut(tween(140)) + slideOutHorizontally(tween(180)) { it / 12 }
            },
        ) {
            composable(Routes.Units) {
                UnitsScreen(
                    repository = unitRepository,
                    onAdd = { navController.navigate(Routes.AddUnit) },
                    onOpenUnit = { navController.navigate(Routes.newReport(it)) },
                )
            }
            composable(Routes.Reports) {
                ReportsScreen(
                    repository = reportRepository,
                    onOpenReport = { navController.navigate(Routes.reportDetails(it)) },
                )
            }
            composable(Routes.AddUnit) {
                AddUnitScreen(
                    repository = unitRepository,
                    onCancel = { navController.popBackStack() },
                    onCreated = { unitId ->
                        navController.navigate(Routes.newReport(unitId)) {
                            popUpTo(Routes.AddUnit) { inclusive = true }
                        }
                    },
                )
            }
            composable(
                route = Routes.NewReport,
                arguments = listOf(navArgument("unitId") { type = NavType.LongType }),
            ) { entry ->
                ReportFormScreen(
                    unitId = entry.arguments?.getLong("unitId") ?: return@composable,
                    unitRepository = unitRepository,
                    reportRepository = reportRepository,
                    onBack = { navController.popBackStack() },
                    onSaved = { reportId ->
                        navController.navigate(Routes.reportDetails(reportId)) {
                            popUpTo(Routes.Units)
                        }
                    },
                )
            }
            composable(
                route = Routes.ReportDetails,
                arguments = listOf(navArgument("reportId") { type = NavType.LongType }),
            ) { entry ->
                ReportDetailsScreen(
                    reportId = entry.arguments?.getLong("reportId") ?: return@composable,
                    repository = reportRepository,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private fun androidx.navigation.NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
