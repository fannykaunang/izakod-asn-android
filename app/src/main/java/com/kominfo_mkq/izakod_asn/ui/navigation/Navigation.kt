package com.kominfo_mkq.izakod_asn.ui.navigation

import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.kominfo_mkq.izakod_asn.data.local.AuthSessionManager
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatan
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODBottomNavigationBar
import com.kominfo_mkq.izakod_asn.ui.screens.AiPanduanChatScreen
import com.kominfo_mkq.izakod_asn.ui.screens.CreateLaporanScreen
import com.kominfo_mkq.izakod_asn.ui.screens.DashboardScreen
import com.kominfo_mkq.izakod_asn.ui.screens.DeveloperScreen
import com.kominfo_mkq.izakod_asn.ui.screens.EditLaporanScreen
import com.kominfo_mkq.izakod_asn.ui.screens.GajiSayaDetailScreen
import com.kominfo_mkq.izakod_asn.ui.screens.GajiSayaScreen
import com.kominfo_mkq.izakod_asn.ui.screens.LoginScreen
import com.kominfo_mkq.izakod_asn.ui.screens.MobileSsoBridgeScreen
import com.kominfo_mkq.izakod_asn.ui.screens.NotificationDetailScreen
import com.kominfo_mkq.izakod_asn.ui.screens.NotificationScreen
import com.kominfo_mkq.izakod_asn.ui.screens.PenilaianBelumDibuatScreen
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
import com.kominfo_mkq.izakod_asn.ui.screens.TargetKinerjaSubordinatePeriodScreen
import com.kominfo_mkq.izakod_asn.ui.screens.TemplateKegiatanScreen
import com.kominfo_mkq.izakod_asn.ui.screens.TertundaScreen
import com.kominfo_mkq.izakod_asn.ui.screens.TppSayaDetailScreen
import com.kominfo_mkq.izakod_asn.ui.screens.TppSayaScreen
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
    object TargetKinerjaSubordinatePeriod : Screen("target_kinerja_bawahan")
    object TargetKinerjaCreate : Screen("target_kinerja_create")
    object PenilaianKinerja : Screen("penilaian_kinerja")
    object PenilaianBelumDibuat : Screen("penilaian_belum_dibuat")
    object Tertunda : Screen("tertunda")
    object TppSaya : Screen("tpp_saya")
    object GajiSaya : Screen("gaji_saya")
    object Reminders : Screen("reminders")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object Developer : Screen("developer")
    object AiPanduanChat : Screen("ai_panduan_chat")
    object CreateReport : Screen("create_report")
    object Notifications : Screen("notifications")
    object NotificationDetail : Screen("notification_detail")
    object MobileSsoBridge : Screen("mobile_sso_bridge?ssoTicket={ssoTicket}&fallbackRoute={fallbackRoute}")
}

private const val DASHBOARD_PAYROLL_TAB_INDEX = 3

fun mobileSsoBridgeRoute(ticket: String, fallbackRoute: String?): String {
    return buildString {
        append("mobile_sso_bridge?ssoTicket=")
        append(Uri.encode(ticket))
        append("&fallbackRoute=")
        append(Uri.encode(fallbackRoute.orEmpty()))
    }
}

private fun isMobileSsoBridgeRoute(route: String): Boolean {
    return route.startsWith("mobile_sso_bridge?")
}

private fun isPayrollDetailRoute(route: String): Boolean {
    return route.startsWith("tpp_saya_detail/") || route.startsWith("gaji_saya_detail/")
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun IZAKODNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route,
    initialSsoTicket: String? = null,
    initialSsoFallbackRoute: String? = null,
    pendingExternalRoute: String? = null,
    onPendingExternalRouteConsumed: () -> Unit = {},
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var loginNoticeMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingReportTemplate by remember { mutableStateOf<TemplateKegiatan?>(null) }
    var dashboardRequestedTabIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var payrollDetailReturnToDashboard by rememberSaveable { mutableStateOf(false) }

    val mainRoutes = setOf(
        Screen.Dashboard.route,
        Screen.ReportList.route,
        Screen.Statistics.route,
        Screen.Profile.route
    )

    val showMainNavigation = currentRoute in mainRoutes

    LaunchedEffect(navController) {
        AuthSessionManager.sessionExpiredEvents.collect { message ->
            loginNoticeMessage = message
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(pendingExternalRoute, currentRoute) {
        val route = pendingExternalRoute ?: return@LaunchedEffect
        if (currentRoute == null) return@LaunchedEffect
        if (currentRoute == Screen.Login.route && !isMobileSsoBridgeRoute(route)) return@LaunchedEffect

        payrollDetailReturnToDashboard = isPayrollDetailRoute(route)
        navController.navigate(route) {
            launchSingleTop = true
        }
        onPendingExternalRouteConsumed()
    }

    val navigateToCreateReport = {
        pendingReportTemplate = null
        navController.navigate(Screen.CreateReport.route)
    }

    val navigateToDashboardPayrollTab = {
        dashboardRequestedTabIndex = DASHBOARD_PAYROLL_TAB_INDEX
        payrollDetailReturnToDashboard = false
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }

    val navigateBackFromPayrollDetail = {
        if (payrollDetailReturnToDashboard) {
            navigateToDashboardPayrollTab()
        } else {
            val returned = navController.popBackStack()
            if (!returned) navigateToDashboardPayrollTab()
        }
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
                    sessionMessage = loginNoticeMessage,
                    onSessionMessageConsumed = { loginNoticeMessage = null },
                    onLoginSuccess = {
                        val targetRoute = pendingExternalRoute ?: Screen.Dashboard.route
                        payrollDetailReturnToDashboard = isPayrollDetailRoute(targetRoute)
                        navController.navigate(targetRoute) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                        if (pendingExternalRoute != null) {
                            onPendingExternalRouteConsumed()
                        }
                    }
                )
            }

            composable(
                route = Screen.MobileSsoBridge.route,
                arguments = listOf(
                    navArgument("ssoTicket") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("fallbackRoute") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val ticket = backStackEntry.arguments
                    ?.getString("ssoTicket")
                    ?.takeIf { it.isNotBlank() }
                    ?: initialSsoTicket.orEmpty()
                val fallbackRoute = backStackEntry.arguments
                    ?.getString("fallbackRoute")
                    ?.takeIf { it.isNotBlank() }
                    ?: initialSsoFallbackRoute?.takeIf { it.isNotBlank() }

                MobileSsoBridgeScreen(
                    ticket = ticket,
                    fallbackRoute = fallbackRoute,
                    onSuccess = { route ->
                        payrollDetailReturnToDashboard = isPayrollDetailRoute(route)
                        navController.navigate(route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                        onPendingExternalRouteConsumed()
                    },
                    onFailure = { message ->
                        loginNoticeMessage = "$message Silakan login kembali."
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                        onPendingExternalRouteConsumed()
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
                    onNavigateToTargetCreate = { tahun, bulan ->
                        val route = if (tahun != null && bulan != null) {
                            "${Screen.TargetKinerjaCreate.route}?tahun=$tahun&bulan=$bulan"
                        } else {
                            Screen.TargetKinerjaCreate.route
                        }
                        navController.navigate(route)
                    },
                    onNavigateToTargetDetail = { targetId, reviewMode ->
                        navController.navigate("target_kinerja_detail/$targetId?reviewMode=$reviewMode") {
                            // Memastikan Dashboard tetap tersimpan di backstack dengan statenya
                            restoreState = true
                        }
                    },
                    onNavigateToSubordinateTargetPeriod = { tahun, bulan ->
                        navController.navigate("${Screen.TargetKinerjaSubordinatePeriod.route}/$tahun/$bulan")
                    },
                    onNavigateToPenilaianKinerja = { navController.navigate(penilaianKinerjaRoute("mine")) },
                    onNavigateToPenilaianBawahan = { navController.navigate(penilaianKinerjaRoute("subordinate")) },
                    onNavigateToPenilaianBelumDibuat = { navController.navigate(Screen.PenilaianBelumDibuat.route) },
                    onNavigateToTertunda = { navController.navigate(Screen.Tertunda.route) },
                    onNavigateToTppDetail = { tahun, bulan ->
                        navController.navigate("tpp_saya_detail/$tahun/$bulan")
                    },
                    onNavigateToGajiDetail = { tahun, bulan ->
                        navController.navigate("gaji_saya_detail/$tahun/$bulan")
                    },
                    onNavigateToTemplates = { navController.navigate(Screen.Templates.route) },
                    onNavigateToReminder = { navController.navigate(Screen.Reminders.route) },
                    onNavigateToAssistant = { navController.navigate(Screen.AiPanduanChat.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    requestedTabIndex = dashboardRequestedTabIndex,
                    onRequestedTabConsumed = { dashboardRequestedTabIndex = null }
                )
            }

            composable(Screen.Tertunda.route) {
                TertundaScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenLaporan = { laporanId -> navController.navigate("laporan_detail/$laporanId") },
                    onOpenTarget = { targetId ->
                        navController.navigate("target_kinerja_detail/$targetId?reviewMode=false")
                    },
                    onOpenPenilaian = { assessmentId ->
                        navController.navigate("penilaian_kinerja_detail/$assessmentId")
                    },
                    onOpenReports = { navController.navigate(Screen.ReportList.route) },
                    onOpenTargets = { navController.navigate(Screen.TargetKinerja.route) },
                    onOpenPenilaianList = { navController.navigate(penilaianKinerjaRoute("mine")) }
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
                    onNavigateToCreate = { tahun, bulan ->
                        navController.navigate("${Screen.TargetKinerjaCreate.route}?tahun=$tahun&bulan=$bulan")
                    }
                )
            }

            composable(
                route = "${Screen.TargetKinerjaSubordinatePeriod.route}/{tahun}/{bulan}",
                arguments = listOf(
                    navArgument("tahun") { type = NavType.IntType },
                    navArgument("bulan") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val tahun = backStackEntry.arguments?.getInt("tahun") ?: 0
                val bulan = backStackEntry.arguments?.getInt("bulan") ?: 0
                TargetKinerjaSubordinatePeriodScreen(
                    tahun = tahun,
                    bulan = bulan,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { targetId, reviewMode ->
                        navController.navigate("target_kinerja_detail/$targetId?reviewMode=$reviewMode")
                    }
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

            composable(Screen.PenilaianBelumDibuat.route) {
                PenilaianBelumDibuatScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { assessmentId ->
                        navController.navigate("penilaian_kinerja_detail/$assessmentId")
                    }
                )
            }

            composable(
                route = "${Screen.TargetKinerjaCreate.route}?tahun={tahun}&bulan={bulan}",
                arguments = listOf(
                    navArgument("tahun") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument("bulan") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val initialTahun = backStackEntry.arguments?.getInt("tahun")
                    ?.takeIf { it > 0 }
                val initialBulan = backStackEntry.arguments?.getInt("bulan")
                    ?.takeIf { it in 1..12 }
                TargetKinerjaFormScreen(
                    targetId = null,
                    initialTahun = initialTahun,
                    initialBulan = initialBulan,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { targetId ->
                        navController.navigate("target_kinerja_detail/$targetId") {
                            popUpTo(Screen.TargetKinerjaCreate.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.TppSaya.route) {
                TppSayaScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { tahun, bulan ->
                        navController.navigate("tpp_saya_detail/$tahun/$bulan")
                    }
                )
            }

            composable(Screen.GajiSaya.route) {
                GajiSayaScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { tahun, bulan ->
                        navController.navigate("gaji_saya_detail/$tahun/$bulan")
                    }
                )
            }

            composable(
                route = "tpp_saya_detail/{tahun}/{bulan}",
                arguments = listOf(
                    navArgument("tahun") { type = NavType.IntType },
                    navArgument("bulan") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val tahun = backStackEntry.arguments?.getInt("tahun") ?: 0
                val bulan = backStackEntry.arguments?.getInt("bulan") ?: 0
                BackHandler { navigateBackFromPayrollDetail() }
                TppSayaDetailScreen(
                    tahun = tahun,
                    bulan = bulan,
                    onNavigateBack = navigateBackFromPayrollDetail
                )
            }

            composable(
                route = "gaji_saya_detail/{tahun}/{bulan}",
                arguments = listOf(
                    navArgument("tahun") { type = NavType.IntType },
                    navArgument("bulan") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val tahun = backStackEntry.arguments?.getInt("tahun") ?: 0
                val bulan = backStackEntry.arguments?.getInt("bulan") ?: 0
                BackHandler { navigateBackFromPayrollDetail() }
                GajiSayaDetailScreen(
                    tahun = tahun,
                    bulan = bulan,
                    onNavigateBack = navigateBackFromPayrollDetail
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
                val createLaporanViewModel: CreateLaporanViewModel = viewModel()

                CreateLaporanScreen(
                    onNavigateBack = {
                        createLaporanViewModel.clearForm()
                        pendingReportTemplate = null
                        navController.popBackStack()
                    },
                    viewModel = createLaporanViewModel,
                    initialTemplate = pendingReportTemplate,
                    onInitialTemplateConsumed = { pendingReportTemplate = null }
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
                        pendingReportTemplate = template
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
                    onNavigateToAssistant = { navController.navigate(Screen.AiPanduanChat.route) },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    viewModel = profileViewModel
                )
            }
            composable(Screen.AiPanduanChat.route) {
                AiPanduanChatScreen(
                    onNavigateBack = { navController.popBackStack() }
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
                    onNotificationClick = { notificationId ->
                        navController.navigate("${Screen.NotificationDetail.route}/$notificationId")
                    }
                )
            }
            composable(
                route = "${Screen.NotificationDetail.route}/{notificationId}",
                arguments = listOf(navArgument("notificationId") { type = NavType.IntType })
            ) { backStackEntry ->
                val notificationId = backStackEntry.arguments?.getInt("notificationId") ?: 0
                NotificationDetailScreen(
                    notificationId = notificationId,
                    onNavigateBack = { navController.popBackStack() },
                    onOpenLaporan = { laporanId ->
                        navController.navigate("laporan_detail/$laporanId")
                    }
                )
            }
        }
    }
}
