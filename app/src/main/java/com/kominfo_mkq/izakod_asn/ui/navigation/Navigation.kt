package com.kominfo_mkq.izakod_asn.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kominfo_mkq.izakod_asn.ui.components.StatusType
import com.kominfo_mkq.izakod_asn.ui.screens.*

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
}

@Composable
fun IZAKODNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
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

        // Dashboard Screen - UPDATED untuk use ViewModel & API
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToCreateReport = {
                    navController.navigate(Screen.CreateReport.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.ReportList.route)
                },
                onNavigateToTemplates = {
                    navController.navigate(Screen.Templates.route)
                },
                onNavigateToReminder = {
                    navController.navigate(Screen.Reminders.route)
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
            val sampleReports = remember {
                listOf(
                    LaporanKegiatan(
                        id = "1",
                        tanggal = "28 Desember 2024",
                        namaKegiatan = "Rapat Koordinasi Tim Pengembangan",
                        kategori = "Rapat/Pertemuan",
                        durasi = "2 jam 30 menit",
                        status = StatusType.APPROVED
                    ),
                    LaporanKegiatan(
                        id = "2",
                        tanggal = "27 Desember 2024",
                        namaKegiatan = "Verifikasi Data Pegawai ASN",
                        kategori = "Administrasi",
                        durasi = "4 jam",
                        status = StatusType.PENDING
                    ),
                    LaporanKegiatan(
                        id = "3",
                        tanggal = "26 Desember 2024",
                        namaKegiatan = "Pembuatan Laporan Bulanan",
                        kategori = "Pelaporan",
                        durasi = "3 jam",
                        status = StatusType.REVISED,
                        catatan = "Mohon dilengkapi dengan data statistik terbaru"
                    ),
                    LaporanKegiatan(
                        id = "4",
                        tanggal = "25 Desember 2024",
                        namaKegiatan = "Monitoring Sistem E-Absen",
                        kategori = "Teknologi",
                        durasi = "5 jam",
                        status = StatusType.APPROVED
                    ),
                    LaporanKegiatan(
                        id = "5",
                        tanggal = "24 Desember 2024",
                        namaKegiatan = "Sosialisasi Aplikasi IZAKOD",
                        kategori = "Sosialisasi",
                        durasi = "3 jam 30 menit",
                        status = StatusType.REJECTED,
                        catatan = "Dokumentasi kegiatan tidak lengkap"
                    )
                )
            }

            ReportListScreen(
                reports = sampleReports,
                onReportClick = { reportId ->
                    navController.navigate(Screen.ReportDetail.createRoute(reportId))
                },
                onCreateReport = {
                    navController.navigate(Screen.CreateReport.route)
                },
                onBack = {
                    navController.popBackStack()
                }
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
                    android.util.Log.d("Navigation", "🔙 onNavigateBack called")
                    val popped = navController.popBackStack()
                    android.util.Log.d("Navigation", "🔙 popBackStack result: $popped")
                }
            )
        }

        // Edit Report Screen - TODO
        composable(
            route = Screen.EditReport.route,
            arguments = listOf(
                navArgument("reportId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId")

            // TODO: Implement EditReportScreen
            PlaceholderScreen(
                title = "Edit Laporan",
                message = "Report ID: $reportId\n\nScreen ini akan digunakan untuk mengedit laporan kegiatan.",
                onBack = { navController.popBackStack() }
            )
        }

        // Templates Screen - TODO
        composable(Screen.Templates.route) {
            // TODO: Implement TemplatesScreen
            PlaceholderScreen(
                title = "Template Kegiatan",
                message = "Screen ini akan menampilkan template kegiatan yang tersimpan.",
                onBack = { navController.popBackStack() }
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