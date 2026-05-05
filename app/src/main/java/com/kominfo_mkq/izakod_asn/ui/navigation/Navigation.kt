package com.kominfo_mkq.izakod_asn.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODBottomNavigationBar
import com.kominfo_mkq.izakod_asn.ui.screens.CreateLaporanScreen
import com.kominfo_mkq.izakod_asn.ui.screens.DashboardScreen
import com.kominfo_mkq.izakod_asn.ui.screens.DeveloperScreen
import com.kominfo_mkq.izakod_asn.ui.screens.EditLaporanScreen
import com.kominfo_mkq.izakod_asn.ui.screens.LoginScreen
import com.kominfo_mkq.izakod_asn.ui.screens.NotificationScreen
import com.kominfo_mkq.izakod_asn.ui.screens.PenilaianKinerjaDetailScreen
import com.kominfo_mkq.izakod_asn.ui.screens.PenilaianKinerjaListScreen
import com.kominfo_mkq.izakod_asn.ui.screens.ProfileScreen
import com.kominfo_mkq.izakod_asn.ui.screens.ReminderScreen
import com.kominfo_mkq.izakod_asn.ui.screens.ReportDetailScreen
import com.kominfo_mkq.izakod_asn.ui.screens.ReportListScreen
import com.kominfo_mkq.izakod_asn.ui.screens.ReportSearchScreen
import com.kominfo_mkq.izakod_asn.ui.screens.SettingsScreen
import com.kominfo_mkq.izakod_asn.ui.screens.StatisticsScreen
import com.kominfo_mkq.izakod_asn.ui.screens.TargetKinerjaDetailScreen
import com.kominfo_mkq.izakod_asn.ui.screens.TargetKinerjaFormScreen
import com.kominfo_mkq.izakod_asn.ui.screens.TargetKinerjaListScreen
import com.kominfo_mkq.izakod_asn.ui.screens.TemplateKegiatanScreen
import com.kominfo_mkq.izakod_asn.ui.screens.VerifikasiLaporanScreen
import com.kominfo_mkq.izakod_asn.ui.viewmodel.CreateLaporanViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.ProfileViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object ReportList : Screen("report_list")
    object ReportSearch : Screen("report_search")
    object Statistics : Screen("statistics")
    object Templates : Screen("templates")
    object TargetKinerja : Screen("target_kinerja")
    object TargetKinerjaCreate : Screen("target_kinerja_create")
    object PenilaianKinerja : Screen("penilaian_kinerja")
    object Reminders : Screen("reminders")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Developer : Screen("developer")
    object CreateReport : Screen("create_report")
    object Notifications : Screen("notifications")
}

private fun penilaianKinerjaRoute(mode: String = "mine"): String {
    return "${Screen.PenilaianKinerja.route}?mode=$mode"
}

fun NavHostController.backToDashboardAlways() {
    navigate(Screen.Dashboard.route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun IZAKODNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val createLaporanViewModel: CreateLaporanViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainRoutes = setOf(
        Screen.Dashboard.route,
        Screen.ReportList.route,
        Screen.Statistics.route,
        Screen.Profile.route
    )

    val showMainNavigation = currentRoute in mainRoutes

    val navigateToCreateReport = {
        createLaporanViewModel.startFreshForm()
        navController.navigate(Screen.CreateReport.route)
    }

    Scaffold(
        bottomBar = {
            if (showMainNavigation) {
                IZAKODBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route == Screen.Dashboard.route) {
                            val returnedToDashboard = navController.popBackStack(
                                Screen.Dashboard.route,
                                inclusive = false
                            )

                            if (!returnedToDashboard) {
                                navController.navigate(Screen.Dashboard.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        } else {
                            navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onCreateReport = navigateToCreateReport
                )
            }
        }
    ) { innerPadding ->
        val layoutDirection = LocalLayoutDirection.current
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(
                start = innerPadding.calculateStartPadding(layoutDirection),
                top = innerPadding.calculateTopPadding(),
                end = innerPadding.calculateEndPadding(layoutDirection),
                bottom = 0.dp
            )
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToReports = {
                        navController.navigate(Screen.ReportList.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToTargetKinerja = { navController.navigate(Screen.TargetKinerja.route) },
                    onNavigateToTargetDetail = { targetId, reviewMode ->
                        navController.navigate("target_kinerja_detail/$targetId?reviewMode=$reviewMode") {
                            // Memastikan Dashboard tetap tersimpan di backstack dengan statenya
                            restoreState = true
                        }
                    },
                    onNavigateToPenilaianKinerja = { navController.navigate(penilaianKinerjaRoute("mine")) },
                    onNavigateToPenilaianBawahan = { navController.navigate(penilaianKinerjaRoute("subordinate")) },
                    onNavigateToTemplates = { navController.navigate(Screen.Templates.route) },
                    onNavigateToReminder = { navController.navigate(Screen.Reminders.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
                )
            }

            composable(Screen.ReportList.route) {
                ReportListScreen(
                    onBack = { navController.popBackStack() },
                    onReportClick = { reportId ->
                        navController.navigate("laporan_detail/$reportId")
                    },
                    onOpenSearch = {
                        navController.navigate(Screen.ReportSearch.route)
                    },
                    onCreateReport = navigateToCreateReport,
                    showBackButton = false,
                    showCreateFab = false
                )
            }

            composable(Screen.ReportSearch.route) {
                ReportSearchScreen(
                    onBack = { navController.popBackStack() },
                    onReportClick = { reportId ->
                        navController.navigate("laporan_detail/$reportId")
                    }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsScreen()
            }

            composable(Screen.TargetKinerja.route) {
                TargetKinerjaListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { targetId, reviewMode ->
                        navController.navigate("target_kinerja_detail/$targetId?reviewMode=$reviewMode")
                    },
                    onNavigateToCreate = { navController.navigate(Screen.TargetKinerjaCreate.route) }
                )
            }

            composable(
                route = "${Screen.PenilaianKinerja.route}?mode={mode}",
                arguments = listOf(
                    navArgument("mode") {
                        type = NavType.StringType
                        defaultValue = "mine"
                    }
                )
            ) { backStackEntry ->
                val initialMode = backStackEntry.arguments?.getString("mode") ?: "mine"
                PenilaianKinerjaListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { assessmentId ->
                        navController.navigate("penilaian_kinerja_detail/$assessmentId")
                    },
                    initialMode = initialMode
                )
            }

            composable(Screen.TargetKinerjaCreate.route) {
                TargetKinerjaFormScreen(
                    targetId = null,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { targetId ->
                        navController.navigate("target_kinerja_detail/$targetId") {
                            popUpTo(Screen.TargetKinerjaCreate.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = "target_kinerja_detail/{targetId}?reviewMode={reviewMode}",
                arguments = listOf(
                    navArgument("targetId") { type = NavType.IntType },
                    navArgument("reviewMode") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val targetId = backStackEntry.arguments?.getInt("targetId") ?: 0
                val reviewMode = backStackEntry.arguments?.getBoolean("reviewMode") ?: false
                TargetKinerjaDetailScreen(
                    targetId = targetId,
                    reviewMode = reviewMode,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate("target_kinerja_edit/$id") },
                    onDeleted = {
                        navController.popBackStack(Screen.TargetKinerja.route, false)
                    }
                )
            }

            composable(
                route = "target_kinerja_edit/{targetId}",
                arguments = listOf(navArgument("targetId") { type = NavType.IntType })
            ) { backStackEntry ->
                val targetId = backStackEntry.arguments?.getInt("targetId")
                TargetKinerjaFormScreen(
                    targetId = targetId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { id ->
                        navController.popBackStack()
                        navController.navigate("target_kinerja_detail/$id")
                    }
                )
            }

            composable(
                route = "penilaian_kinerja_detail/{assessmentId}",
                arguments = listOf(navArgument("assessmentId") { type = NavType.IntType })
            ) { backStackEntry ->
                val assessmentId = backStackEntry.arguments?.getInt("assessmentId") ?: 0
                PenilaianKinerjaDetailScreen(
                    assessmentId = assessmentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "laporan_detail/{laporanId}",
                arguments = listOf(navArgument("laporanId") { type = NavType.StringType })
            ) { backStackEntry ->
                val laporanId = backStackEntry.arguments?.getString("laporanId") ?: ""
                ReportDetailScreen(
                    laporanId = laporanId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate("laporan_edit/$id") },
                    onNavigateToVerify = { id -> navController.navigate("laporan_verify/$id") }
                )
            }

            composable(Screen.CreateReport.route) {
                CreateLaporanScreen(
                    onNavigateBack = {
                        createLaporanViewModel.clearForm()
                        navController.popBackStack()
                    },
                    viewModel = createLaporanViewModel
                )
            }

            composable(
                route = "laporan_edit/{laporanId}",
                arguments = listOf(navArgument("laporanId") { type = NavType.StringType })
            ) { backStackEntry ->
                val laporanId = backStackEntry.arguments?.getString("laporanId") ?: ""
                EditLaporanScreen(
                    laporanId = laporanId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Templates.route) {
                TemplateKegiatanScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTemplateClick = { template ->
                        createLaporanViewModel.loadFromTemplate(template)
                        navController.navigate(Screen.CreateReport.route)
                    }
                )
            }

            composable(Screen.Reminders.route) {
                ReminderScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "laporan_verify/{laporanId}",
                arguments = listOf(navArgument("laporanId") { type = NavType.StringType })
            ) { backStackEntry ->
                val laporanId = backStackEntry.arguments?.getString("laporanId") ?: ""
                VerifikasiLaporanScreen(
                    laporanId = laporanId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBackToDashboard = { navController.backToDashboardAlways() },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    isRootTab = true,
                    viewModel = profileViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    viewModel = profileViewModel
                )
            }
            composable(Screen.Developer.route) {
                DeveloperScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Notifications.route) {
                NotificationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNotificationClick = { laporanId ->
                        navController.navigate("laporan_detail/$laporanId")
                    }
                )
            }
        }
    }
}
