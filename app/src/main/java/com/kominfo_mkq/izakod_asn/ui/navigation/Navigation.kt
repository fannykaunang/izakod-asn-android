package com.kominfo_mkq.izakod_asn.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kominfo_mkq.izakod_asn.ui.screens.*
import com.kominfo_mkq.izakod_asn.ui.viewmodel.CreateLaporanViewModel

/**
 * Navigation Routes untuk IZAKOD-ASN App
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object ReportList : Screen("report_list")
    object ReportDetail : Screen("report_detail/{reportId}") {
        fun createRoute(reportId: String) = "report_detail/$reportId"
    }
    object EditReport : Screen("edit_report/{reportId}") {
        fun createRoute(reportId: String) = "edit_report/$reportId"
    }
    object Templates : Screen("templates")
    object Reminders : Screen("reminders")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Verification : Screen("verification/{reportId}") {
        fun createRoute(reportId: String) = "verification/$reportId"
    }
    object PrintReport : Screen("print_report")
    object CreateReport : Screen("create_report")
    object TemplateKegiatan : Screen("template_kegiatan")
}

@Composable
fun IZAKODNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    val createLaporanViewModel: CreateLaporanViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Screen
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
                    createLaporanViewModel.startFreshForm()
                    navController.navigate(Screen.CreateReport.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.ReportList.route)
                },
                onNavigateToTemplates = {
                    navController.navigate("templates")
                },
                onNavigateToReminder = {
                    navController.navigate(Screen.Reminders.route)
                },
                onNavigateToProfile = {  // ✅ Add this
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Report List Screen
        composable(Screen.ReportList.route) {
            ReportListScreen(
                onBack = { navController.popBackStack() },
                onReportClick = { laporanId ->
                    navController.navigate("laporan_detail/$laporanId")
                },
                onCreateReport = {
                    createLaporanViewModel.startFreshForm()
                    navController.navigate(Screen.CreateReport.route)
                },
                reports = emptyList() // Not used anymore, kept for compatibility
            )
        }

        composable(
            route = "laporan_detail/{laporanId}",
            arguments = listOf(
                navArgument("laporanId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val laporanId = backStackEntry.arguments?.getString("laporanId") ?: ""

            ReportDetailScreen(
                laporanId = laporanId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate("laporan_edit/$id")
                },
                onNavigateToVerify = { id ->  // ✅ Add this
                    navController.navigate("laporan_verify/$id")
                }
            )
        }

        composable(
            route = "laporan_edit_placeholder/{laporanId}",
            arguments = listOf(navArgument("laporanId") { type = NavType.StringType })
        ) { backStackEntry ->
            val laporanId = backStackEntry.arguments?.getString("laporanId") ?: ""

            PlaceholderScreen(
                title = "Edit Laporan",
                message = "Laporan ID: $laporanId",
                navController = navController
            )
        }

        // Report Detail Screen - TODO
        composable(
            route = Screen.ReportDetail.route,
            arguments = listOf(
                navArgument("reportId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId")

            // TODO: Implement ReportDetailScreen
            // Placeholder screen
            PlaceholderScreen(
                title = "Report Detail",
                message = "Report ID: $reportId\n\nScreen ini akan menampilkan detail laporan kegiatan.",
                onBack = { navController.popBackStack() }
            )
        }

        // Create Report Screen - TODO
        composable(Screen.CreateReport.route) {
            CreateLaporanScreen(
                onNavigateBack = {
                    createLaporanViewModel.clearForm()
                    navController.popBackStack()
                },
                viewModel = createLaporanViewModel
            )
        }

        // Edit Report Screen - TODO
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

        // Reminders Screen - TODO
        composable(Screen.Reminders.route) {
            // TODO: Implement RemindersScreen
            PlaceholderScreen(
                title = "Pengingat",
                message = "Screen ini akan menampilkan pengaturan pengingat untuk input laporan.",
                onBack = { navController.popBackStack() }
            )
        }

        // Profile Screen - TODO
        composable(Screen.Profile.route) {
            // TODO: Implement ProfileScreen
            PlaceholderScreen(
                title = "Profil",
                message = "Screen ini akan menampilkan profil pengguna dan opsi logout.",
                onBack = { navController.popBackStack() }
            )
        }

        // Settings Screen - TODO
        composable(Screen.Settings.route) {
            // TODO: Implement SettingsScreen
            PlaceholderScreen(
                title = "Pengaturan",
                message = "Screen ini akan menampilkan pengaturan aplikasi.",
                onBack = { navController.popBackStack() }
            )
        }

        // Verification Screen (untuk atasan) - TODO
        composable(
            route = Screen.Verification.route,
            arguments = listOf(
                navArgument("reportId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId")

            // TODO: Implement VerificationScreen
            PlaceholderScreen(
                title = "Verifikasi Laporan",
                message = "Report ID: $reportId\n\nScreen ini akan digunakan atasan untuk memverifikasi laporan kegiatan.",
                onBack = { navController.popBackStack() }
            )
        }

        // Print Report Screen - TODO
        composable(Screen.PrintReport.route) {
            // TODO: Implement PrintReportScreen
            PlaceholderScreen(
                title = "Cetak Laporan",
                message = "Screen ini akan digunakan untuk mencetak laporan kegiatan.",
                onBack = { navController.popBackStack() }
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

        // Add verifikasi route
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
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    message: String = ""
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { /* navController.popBackStack() */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Coming soon...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(
    title: String,
    message: String = "",
    navController: NavHostController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (message.isNotEmpty()) {
                    Text(message, style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    "Coming soon...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Extension functions untuk navigasi yang lebih mudah
 */
fun NavHostController.navigateToReportDetail(reportId: String) {
    navigate(Screen.ReportDetail.createRoute(reportId))
}

fun NavHostController.navigateToEditReport(reportId: String) {
    navigate(Screen.EditReport.createRoute(reportId))
}

fun NavHostController.navigateToVerification(reportId: String) {
    navigate(Screen.Verification.createRoute(reportId))
}