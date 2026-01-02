package com.kominfo_mkq.izakod_asn.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kominfo_mkq.izakod_asn.ui.screens.*
import com.kominfo_mkq.izakod_asn.ui.viewmodel.CreateLaporanViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.ProfileViewModel

/**
 * Navigation Routes untuk IZAKOD-ASN App
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object ReportList : Screen("report_list")
    object Templates : Screen("templates")
    object Reminders : Screen("reminders")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object CreateReport : Screen("create_report")
    object Notifications : Screen("notifications")
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
    val createlaporanViewModel: CreateLaporanViewModel = viewModel()

    val profileViewModel: ProfileViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                onNavigateToCreateReport = {
                    createlaporanViewModel.startFreshForm()
                    navController.navigate(Screen.CreateReport.route)
                },
                onNavigateToReports = { navController.navigate(Screen.ReportList.route) },
                onNavigateToTemplates = { navController.navigate(Screen.Templates.route) },
                onNavigateToReminder = { navController.navigate(Screen.Reminders.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                }
            )
        }

        composable(Screen.ReportList.route) {
            ReportListScreen(
                onBack = { navController.popBackStack() },
                onReportClick = { reportId ->
                    navController.navigate("laporan_detail/$reportId")
                },
                onCreateReport = {
                    createlaporanViewModel.startFreshForm()
                    navController.navigate(Screen.CreateReport.route)
                }
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
                    createlaporanViewModel.clearForm()
                    navController.popBackStack()
                },
                viewModel = createlaporanViewModel
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
                    createlaporanViewModel.loadFromTemplate(template)
                    navController.navigate(Screen.CreateReport.route)
                }
            )
        }

        composable(Screen.Reminders.route) {
            ReminderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Verifikasi route
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
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Profile.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                viewModel = profileViewModel
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackToDashboard = { navController.backToDashboardAlways() },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(Screen.Profile.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                viewModel = profileViewModel
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
