package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlaylistAddCheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.AssessmentSummaryData
import com.kominfo_mkq.izakod_asn.data.model.DashboardActionAlertsData
import com.kominfo_mkq.izakod_asn.data.model.DashboardTargetSummaryData
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeData
import com.kominfo_mkq.izakod_asn.data.model.MetricsData
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.TimeSeriesItem
import com.kominfo_mkq.izakod_asn.data.model.TppMeData
import com.kominfo_mkq.izakod_asn.data.model.isNonAsnPegawai
import com.kominfo_mkq.izakod_asn.ui.viewmodel.GajiSayaUiState
import com.kominfo_mkq.izakod_asn.ui.viewmodel.GajiSayaViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TppSayaUiState
import com.kominfo_mkq.izakod_asn.ui.components.ElevatedCard
import com.kominfo_mkq.izakod_asn.ui.components.GradientCard
import com.kominfo_mkq.izakod_asn.ui.components.OutlinedCard
import com.kominfo_mkq.izakod_asn.ui.theme.ErrorLight
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.SecondaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.theme.TertiaryLight
import com.kominfo_mkq.izakod_asn.ui.viewmodel.DashboardViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TppSayaViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun String?.displayOrDash(): String = this?.takeIf { it.isNotBlank() } ?: "-"
private fun String?.toIntSafe(): Int = this?.toIntOrNull() ?: 0
private fun String?.toFloatSafe(): Float = this?.toFloatOrNull() ?: 0f
private fun String?.toDoubleSafe(): Double = this?.toDoubleOrNull() ?: 0.0

private fun currentGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..10 -> "Selamat pagi"
        in 11..14 -> "Selamat siang"
        in 15..18 -> "Selamat sore"
        else -> "Selamat malam"
    }
}

private fun currentDateLabel(): String {
    val formatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
    return formatter.format(Calendar.getInstance().time)
}

private fun monthYearLabel(tahun: Int, bulan: Int): String {
    val monthNames = listOf(
        "Januari",
        "Februari",
        "Maret",
        "April",
        "Mei",
        "Juni",
        "Juli",
        "Agustus",
        "September",
        "Oktober",
        "November",
        "Desember"
    )
    val monthName = monthNames.getOrNull(bulan - 1) ?: "Bulan $bulan"
    return "$monthName $tahun"
}

private fun String?.periodMonthOnly(): String? {
    val label = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val firstToken = label.substringBefore(" ").trim().trimEnd(',')
    return firstToken.takeIf { it.isNotBlank() }
}

private fun formatDuration(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0j 0m"
    val hour = totalMinutes / 60
    val minute = totalMinutes % 60
    return "${hour}j ${minute}m"
}

private fun formatNullableScore(value: Double?): String {
    if (value == null) return "-"
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale("id", "ID"), "%.2f", value)
    }
}

private fun Double?.formatDashboardCurrency(): String {
    val value = this ?: return "-"
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(value)
}

private data class TargetPeriodGroup(
    val tahun: Int,
    val bulan: Int,
    val label: String,
    val targets: List<TargetKinerjaItem>
)

private data class DashboardCoachmarkPrompt(
    val key: String,
    val title: String,
    val message: String,
    val actionLabel: String,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    onNavigateToReports: () -> Unit,
    onNavigateToTargetKinerja: () -> Unit,
    onNavigateToTargetCreate: (Int?, Int?) -> Unit,
    onNavigateToTargetDetail: (Int, Boolean) -> Unit,
    onNavigateToSubordinateTargetPeriod: (Int, Int) -> Unit,
    onNavigateToPenilaianKinerja: () -> Unit,
    onNavigateToPenilaianBawahan: () -> Unit,
    onNavigateToPenilaianBelumDibuat: () -> Unit,
    onNavigateToTertunda: () -> Unit,
    onNavigateToTppDetail: (Int, Int) -> Unit,
    onNavigateToGajiDetail: (Int, Int) -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToReminder: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    viewModel: DashboardViewModel = viewModel(),
    tppViewModel: TppSayaViewModel = viewModel(),
    gajiViewModel: GajiSayaViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    val tppUiState by tppViewModel.uiState.collectAsState()
    val gajiUiState by gajiViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val sessionData = remember { userPreferences.getSessionData() }
    val isNonAsnPayroll = uiState.pegawaiProfile.isNonAsnPegawai()
    val isPayrollRefreshing = if (isNonAsnPayroll) {
        gajiUiState.isRefreshing
    } else {
        tppUiState.isRefreshing
    }
    val isDashboardRefreshing = uiState.isRefreshing || isPayrollRefreshing
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isDashboardRefreshing,
        onRefresh = {
            viewModel.refresh(context, showFullLoading = false)
            sessionData?.pin?.takeIf { it.isNotEmpty() }?.let { pin ->
                viewModel.loadPegawaiProfile(pin)
            }
            if (uiState.pegawaiProfile != null) {
                if (isNonAsnPayroll) {
                    gajiViewModel.refresh()
                } else {
                    tppViewModel.refresh()
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.refresh(context)
    }

    LaunchedEffect(uiState.pegawaiProfile?.pegawaiId, isNonAsnPayroll) {
        if (uiState.pegawaiProfile != null) {
            if (isNonAsnPayroll) {
                gajiViewModel.refresh()
            } else {
                tppViewModel.refresh()
            }
        }
    }

    val activePegawaiId = uiState.currentPegawaiId ?: uiState.pegawaiProfile?.pegawaiId ?: sessionData?.pegawaiId
    var focusSectionBounds by remember { mutableStateOf<Rect?>(null) }
    var dismissedCoachmarkKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val pin = sessionData?.pin
        if (!pin.isNullOrEmpty()) {
            viewModel.loadPegawaiProfile(pin)
        }
    }

    val coachmarkPrompt = if (!uiState.isLoading && !uiState.isError) {
        buildDashboardCoachmarkPrompt(
            alerts = uiState.actionAlerts,
            targetSummary = uiState.targetSummary,
            assessmentSummary = uiState.assessmentSummary,
            targetItems = uiState.targetItems,
            currentPegawaiId = activePegawaiId,
            targetPeriodYear = uiState.targetPeriodYear,
            targetPeriodMonth = uiState.targetPeriodMonth,
            canReviewSubordinates = uiState.assessmentSummary?.canReviewSubordinates == true
        )
    } else {
        null
    }
    val visibleCoachmarkPrompt = coachmarkPrompt?.takeIf { prompt ->
        prompt.key != dismissedCoachmarkKey &&
            !userPreferences.isDashboardCoachmarkDismissed(prompt.key)
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                scrollBehavior = scrollBehavior,
                onNavigateToNotifications = onNavigateToNotifications,
                pegawaiProfile = uiState.pegawaiProfile,
                unreadNotificationCount = uiState.unreadNotificationCount,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    )
                )
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoading -> LoadingContent()
                uiState.isError -> {
                    ErrorContent(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { viewModel.retry(context) }
                    )
                }
                else -> {
                    DashboardContent(
                        metrics = uiState.metrics,
                        timeSeries = uiState.timeSeries,
                        assessmentSummary = uiState.assessmentSummary,
                        targetSummary = uiState.targetSummary,
                        actionAlerts = uiState.actionAlerts,
                        tertundaCount = uiState.tertundaCount,
                        targetItems = uiState.targetItems,
                        tppUiState = tppUiState,
                        tppData = tppUiState.data,
                        gajiUiState = gajiUiState,
                        gajiData = gajiUiState.data,
                        isNonAsnPayroll = isNonAsnPayroll,
                        currentPegawaiId = activePegawaiId,
                        targetPeriodYear = uiState.targetPeriodYear,
                        targetPeriodMonth = uiState.targetPeriodMonth,
                        pegawaiProfile = uiState.pegawaiProfile,
                        photoUrl = uiState.photoUrl,
                        unreadNotificationCount = uiState.unreadNotificationCount,
                        isDarkTheme = isDarkTheme,
                        onViewReports = onNavigateToReports,
                        onTargetKinerja = onNavigateToTargetKinerja,
                        onTargetCreate = onNavigateToTargetCreate,
                        onTargetDetail = onNavigateToTargetDetail,
                        onSubordinateTargetPeriod = onNavigateToSubordinateTargetPeriod,
                        onPenilaianKinerja = onNavigateToPenilaianKinerja,
                        onPenilaianBawahan = onNavigateToPenilaianBawahan,
                        onPenilaianBelumDibuat = onNavigateToPenilaianBelumDibuat,
                        onTertunda = onNavigateToTertunda,
                        onTppPreviousMonth = { tppViewModel.moveMonth(-1) },
                        onTppNextMonth = { tppViewModel.moveMonth(1) },
                        onTppRefresh = { tppViewModel.refresh() },
                        onTppDetail = onNavigateToTppDetail,
                        onGajiPreviousMonth = { gajiViewModel.moveMonth(-1) },
                        onGajiNextMonth = { gajiViewModel.moveMonth(1) },
                        onGajiRefresh = { gajiViewModel.refresh() },
                        onGajiDetail = onNavigateToGajiDetail,
                        onTargetPeriodSelected = viewModel::selectTargetPeriod,
                        onTemplates = onNavigateToTemplates,
                        onReminder = onNavigateToReminder,
                        viewModel = viewModel,
                        onFocusBoundsChanged = { focusSectionBounds = it }
                    )

                    val bounds = focusSectionBounds
                    if (visibleCoachmarkPrompt != null && bounds != null) {
                        DashboardCoachmarkOverlay(
                            prompt = visibleCoachmarkPrompt,
                            focusBounds = bounds,
                            onDismiss = {
                                userPreferences.setDashboardCoachmarkDismissed(visibleCoachmarkPrompt.key)
                                dismissedCoachmarkKey = visibleCoachmarkPrompt.key
                            }
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isDashboardRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun DashboardContent(
    metrics: MetricsData?,
    timeSeries: List<TimeSeriesItem>,
    assessmentSummary: AssessmentSummaryData?,
    targetSummary: DashboardTargetSummaryData?,
    actionAlerts: DashboardActionAlertsData?,
    tertundaCount: Int?,
    targetItems: List<TargetKinerjaItem>,
    tppUiState: TppSayaUiState,
    tppData: TppMeData?,
    gajiUiState: GajiSayaUiState,
    gajiData: GajiNonAsnMeData?,
    isNonAsnPayroll: Boolean,
    currentPegawaiId: Int?,
    targetPeriodYear: Int?,
    targetPeriodMonth: Int?,
    pegawaiProfile: PegawaiProfile?,
    photoUrl: String?,
    unreadNotificationCount: Int,
    isDarkTheme: Boolean,
    onViewReports: () -> Unit,
    onTargetKinerja: () -> Unit,
    onTargetCreate: (Int?, Int?) -> Unit,
    onTargetDetail: (Int, Boolean) -> Unit,
    onSubordinateTargetPeriod: (Int, Int) -> Unit,
    onPenilaianKinerja: () -> Unit,
    onPenilaianBawahan: () -> Unit,
    onPenilaianBelumDibuat: () -> Unit,
    onTertunda: () -> Unit,
    onTppPreviousMonth: () -> Unit,
    onTppNextMonth: () -> Unit,
    onTppRefresh: () -> Unit,
    onTppDetail: (Int, Int) -> Unit,
    onGajiPreviousMonth: () -> Unit,
    onGajiNextMonth: () -> Unit,
    onGajiRefresh: () -> Unit,
    onGajiDetail: (Int, Int) -> Unit,
    onTargetPeriodSelected: (Int, Int) -> Unit,
    onTemplates: () -> Unit,
    onReminder: () -> Unit,
    viewModel: DashboardViewModel,
    onFocusBoundsChanged: (Rect?) -> Unit = {}
) {
    val payrollTabTitle = if (isNonAsnPayroll) "Gaji" else "TPP"
    val payrollMenuLabel = if (isNonAsnPayroll) "Gaji Saya" else "TPP Saya"
    val tabs = listOf("Utama", "Target", "Penilaian", payrollTabTitle)

    val selectedTabIndex by viewModel.currentTabIndex.collectAsState()

    val canReviewSubordinates = assessmentSummary?.canReviewSubordinates == true

    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { tabs.size }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
            .filter { (_, isScrollInProgress) -> !isScrollInProgress }
            .map { (page, _) -> page }
            .distinctUntilChanged()
            .collect { page ->
                viewModel.updateTabIndex(page)
            }
    }

    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 20.dp,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        viewModel.updateTabIndex(index)
                    },
                    text = {
                        Text(
                            text = title,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Visible,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            key = { it }
        ) { page ->
            when (page) {
                0 -> {
                    val listState = remember(page) { androidx.compose.foundation.lazy.LazyListState() }
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            HeroPlaySectionV2(
                                pegawaiProfile = pegawaiProfile,
                                photoUrl = photoUrl,
                                assessmentSummary = assessmentSummary,
                                targetSummary = targetSummary,
                                currentTab = tabs[page],
                                payrollLabel = payrollMenuLabel,
                                isDarkTheme = isDarkTheme
                            )
                        }
                        item {
                            Box(
                                modifier = Modifier.onGloballyPositioned { coordinates ->
                                    val size = coordinates.size
                                    if (size.height > 0 && size.width > 0) {
                                        val position = coordinates.positionInRoot()
                                        onFocusBoundsChanged(
                                            Rect(
                                                left = position.x,
                                                top = position.y,
                                                right = position.x + size.width,
                                                bottom = position.y + size.height
                                            )
                                        )
                                    } else {
                                        onFocusBoundsChanged(null)
                                    }
                                }
                            ) {
                                DashboardFocusSection(
                                    alerts = actionAlerts,
                                    targetSummary = targetSummary,
                                    assessmentSummary = assessmentSummary,
                                    targetItems = targetItems,
                                    currentPegawaiId = currentPegawaiId,
                                    targetPeriodYear = targetPeriodYear,
                                    targetPeriodMonth = targetPeriodMonth,
                                    canReviewSubordinates = canReviewSubordinates,
                                    onOpenReports = onViewReports,
                                    onOpenTarget = onTargetKinerja,
                                    onCreateTarget = onTargetCreate,
                                    onOpenSubordinateTargetPeriod = onSubordinateTargetPeriod,
                                    onOpenPenilaian = onPenilaianKinerja,
                                    onOpenReview = onPenilaianBawahan,
                                    onOpenPenilaianBelumDibuat = onPenilaianBelumDibuat
                                )
                            }
                        }
                        item {
                            MinimalQuickActionsSection(
                                tertundaCount = tertundaCount,
                                onViewReports = onViewReports,
                                onTargetKinerja = onTargetKinerja,
                                onPenilaianKinerja = onPenilaianKinerja,
                                onTertunda = onTertunda,
                                onPayrollSaya = { viewModel.updateTabIndex(tabs.indexOf(payrollTabTitle)) },
                                payrollLabel = payrollTabTitle,
                                onTemplates = onTemplates,
                                onReminder = onReminder
                            )
                        }
                        item {
                            MinimalSummarySection(
                                metrics = metrics,
                                targetSummary = targetSummary,
                                assessmentSummary = assessmentSummary,
                                tppData = tppData,
                                gajiData = gajiData,
                                isNonAsnPayroll = isNonAsnPayroll
                            )
                        }
                    }
                }

                1 -> {
                    val listState = remember(page) { androidx.compose.foundation.lazy.LazyListState() }
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            TargetProgressSection(
                                summary = targetSummary,
                                assessmentSummary = assessmentSummary,
                                targetItems = targetItems,
                                currentPegawaiId = currentPegawaiId,
                                targetPeriodYear = targetPeriodYear,
                                targetPeriodMonth = targetPeriodMonth,
                                canReviewSubordinates = canReviewSubordinates,
                                onOpenReports = onViewReports,
                                onPeriodSelected = onTargetPeriodSelected,
                                onCreateTarget = onTargetCreate,
                                onOpenTargetDetail = onTargetDetail,
                                onOpenSubordinateTargetPeriod = onSubordinateTargetPeriod
                            )
                        }
                    }
                }

                2 -> {
                    val listState = remember(page) { androidx.compose.foundation.lazy.LazyListState() }
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AssessmentSummarySection(
                                summary = assessmentSummary,
                                targetSummary = targetSummary,
                                targetPeriodYear = targetPeriodYear,
                                targetPeriodMonth = targetPeriodMonth,
                                onPeriodSelected = onTargetPeriodSelected,
                                onOpenTarget = onTargetKinerja,
                                onOpenPenilaian = onPenilaianKinerja,
                                onOpenReviewBawahan = onPenilaianBawahan,
                                onOpenBelumDibuat = onPenilaianBelumDibuat
                            )
                        }
                    }
                }

                else -> {
                    if (isNonAsnPayroll) {
                        GajiSayaDashboardTabContent(
                            uiState = gajiUiState,
                            onPreviousMonth = onGajiPreviousMonth,
                            onNextMonth = onGajiNextMonth,
                            onRefresh = onGajiRefresh,
                            onNavigateToDetail = onGajiDetail
                        )
                    } else {
                        TppSayaDashboardTabContent(
                            uiState = tppUiState,
                            onPreviousMonth = onTppPreviousMonth,
                            onNextMonth = onTppNextMonth,
                            onRefresh = onTppRefresh,
                            onNavigateToDetail = onTppDetail
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateToNotifications: () -> Unit,
    pegawaiProfile: PegawaiProfile?,
    unreadNotificationCount: Int,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    TopAppBar(
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "IZAKOD-ASN",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = currentDateLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            IconButton(onClick = { onToggleTheme(!isDarkTheme) }) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = if (isDarkTheme) "Ubah ke mode terang" else "Ubah ke mode gelap"
                )
            }

            Box(
                modifier = Modifier
                    .padding(end = 2.dp)
                    .size(36.dp)
            ) {
                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-2).dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifikasi"
                    )
                }

                if (unreadNotificationCount > 0) {
                    Badge(
                        containerColor = StatusRejected,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-1).dp, y = 5.dp)
                    ) {
                        Text(
                            text = if (unreadNotificationCount > 9) "9+" else unreadNotificationCount.toString(),
                            color = Color.White
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Memuat ringkasan dashboard...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        OutlinedCard {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(58.dp),
                    tint = ErrorLight
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetry,
                    elevation = 0.dp,
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Coba Lagi")
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSection(
    pegawaiProfile: PegawaiProfile?,
    photoUrl: String?,
    metrics: MetricsData?,
    assessmentSummary: AssessmentSummaryData?,
    targetSummary: DashboardTargetSummaryData?,
    unreadNotificationCount: Int,
    isDarkTheme: Boolean
) {
    val totalKegiatan = metrics?.totalKegiatan.toIntSafe()
    val totalPending = metrics?.totalPending.toIntSafe()
    val activeAssessmentStatus = assessmentSummary?.activePeriodStatus.displayOrDash()
    val activeTargetStatus = targetSummary?.status.displayOrDash()
    val activePeriodLabel = targetSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: assessmentSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: currentDateLabel()
    val pendingOwnAssessment = assessmentSummary?.pendingOwnAssessment ?: 0
    val pendingSubordinates = assessmentSummary?.pendingSubordinateAssessments ?: 0
    val canReviewSubordinates = assessmentSummary?.canReviewSubordinates == true
    val greeting = currentGreeting()
    val nama = pegawaiProfile?.pegawaiNama.displayOrDash()
    val jabatan = pegawaiProfile?.jabatan.displayOrDash()
    val skpd = pegawaiProfile?.skpd.displayOrDash()
    val roleLine = listOf(jabatan, skpd)
        .filter { it.isNotBlank() && it != "-" }
        .joinToString(" • ")
        .ifBlank { "Informasi jabatan belum tersedia" }
    val heroStatuses = buildList {
        add(
            HeroStatusItem(
                label = "Penilaian: $activeAssessmentStatus",
                color = when (activeAssessmentStatus.lowercase(Locale.getDefault())) {
                    "final" -> StatusApproved
                    "review" -> StatusPending
                    "draft" -> PrimaryLight
                    else -> StatusRevised
                }
            )
        )
        if (canReviewSubordinates) {
            add(
                HeroStatusItem(
                    label = "$pendingSubordinates penilaian pegawai",
                    color = if (pendingSubordinates > 0) StatusRejected else SecondaryLight
                )
            )
        } else {
            add(
                HeroStatusItem(
                    label = "Pending: $totalPending",
                    color = if (totalPending > 0) StatusPending else SecondaryLight
                )
            )
        }
    }
    val highlightText = when {
        pendingOwnAssessment > 0 ->
            "Penilaian Anda belum final pada periode aktif"
        canReviewSubordinates && pendingSubordinates > 0 ->
            "$pendingSubordinates penilaian bawahan belum final"
        totalPending > 0 ->
            "$totalPending laporan masih menunggu tindak lanjut"
        unreadNotificationCount > 0 ->
            "$unreadNotificationCount notifikasi baru siap ditinjau"
        totalKegiatan > 0 ->
            "$totalKegiatan kegiatan sudah tercatat pada periode ini"
        else -> "Semua ringkasan kerja Anda akan muncul di sini"
    }

    GradientCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDarkTheme,
        cornerRadius = 24.dp,
        elevation = 5.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$greeting, $nama",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = roleLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = activePeriodLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.92f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        heroStatuses.forEach { statusItem ->
                            CompactHeroStatusChip(
                                text = statusItem.label,
                                color = statusItem.color
                            )
                        }
                    }

                    Text(
                        text = highlightText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto pegawai",
                        modifier = Modifier
                            .padding(start = 14.dp)
                            .size(60.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

    }
}

private data class HeroStatusItem(
    val label: String,
    val color: Color
)

@Composable
private fun CompactHeroStatusChip(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.92f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeroLegacySection(
    pegawaiProfile: PegawaiProfile?,
    photoUrl: String?,
    metrics: MetricsData?,
    assessmentSummary: AssessmentSummaryData?,
    targetSummary: DashboardTargetSummaryData?,
    unreadNotificationCount: Int,
    isDarkTheme: Boolean
) {
    val totalKegiatan = metrics?.totalKegiatan.toIntSafe()
    val targetStatus = targetSummary?.status.displayOrDash()
    val assessmentStatus = assessmentSummary?.activePeriodStatus.displayOrDash()
    val latestAssessmentScore = formatNullableScore(assessmentSummary?.latestFinalScore)
    val greeting = currentGreeting()
    val nama = pegawaiProfile?.pegawaiNama.displayOrDash()
    val jabatan = pegawaiProfile?.jabatan.displayOrDash()
    val skpd = pegawaiProfile?.skpd.displayOrDash()
    val roleLine = listOf(jabatan, skpd)
        .filter { it.isNotBlank() && it != "-" }
        .joinToString(" • ")
        .ifBlank { "Informasi jabatan belum tersedia" }
    val activePeriodLabel = targetSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: assessmentSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: currentDateLabel()
    val targetValue = when {
        targetStatus != "-" -> targetStatus
        (targetSummary?.totalTargets ?: 0) > 0 -> "${targetSummary?.totalTargets ?: 0} target"
        else -> "Belum ada"
    }
    val assessmentValue = when {
        assessmentStatus != "-" -> assessmentStatus
        latestAssessmentScore != "-" -> latestAssessmentScore
        unreadNotificationCount > 0 -> "$unreadNotificationCount baru"
        else -> "Belum ada"
    }

    GradientCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDarkTheme,
        cornerRadius = 30.dp,
        elevation = 5.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            if (photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto pegawai",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(66.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(100),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Periode: $activePeriodLabel",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "$greeting,",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = nama,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 28.sp
                        ),
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = roleLine,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        ),
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroMiniMetricCard(
                        title = "Kegiatan\nbulan ini",
                        value = totalKegiatan.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    HeroMiniMetricCard(
                        title = "Status\nTarget",
                        value = targetValue,
                        modifier = Modifier.weight(1f)
                    )
                    HeroMiniMetricCard(
                        title = "Status\nPenilaian",
                        value = assessmentValue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroMiniMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                color = Color.White.copy(alpha = 0.84f),
                lineHeight = 20.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeroPlaySection(
    pegawaiProfile: PegawaiProfile?,
    photoUrl: String?,
    assessmentSummary: AssessmentSummaryData?,
    targetSummary: DashboardTargetSummaryData?,
    currentTab: String,
    isDarkTheme: Boolean
) {
    val greeting = currentGreeting()
    val nama = pegawaiProfile?.pegawaiNama.displayOrDash()
    val jabatan = pegawaiProfile?.jabatan.displayOrDash()
    val skpd = pegawaiProfile?.skpd.displayOrDash()
    val activePeriodLabel = targetSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: assessmentSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: currentDateLabel()
    val roleLine = listOf(jabatan, skpd)
        .filter { it.isNotBlank() && it != "-" }
        .joinToString(" • ")
        .ifBlank { "Informasi jabatan belum tersedia" }
    val targetStatus = targetSummary?.status.displayOrDash()
    val assessmentStatus = assessmentSummary?.activePeriodStatus.displayOrDash()
    val heroStatus = when (currentTab) {
        "Target" -> "Target: $targetStatus"
        "Penilaian" -> "Penilaian: $assessmentStatus"
        "TPP" -> "TPP Saya"
        else -> listOf("Target: $targetStatus", "Penilaian: $assessmentStatus")
            .joinToString(" • ")
    }

    GradientCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDarkTheme,
        cornerRadius = 28.dp,
        elevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            if (photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto pegawai",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(76.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "$greeting, $nama",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = roleLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.84f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = activePeriodLabel,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.92f)
                )
                Surface(
                    shape = RoundedCornerShape(100),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = heroStatus,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroPlaySectionV2(
    pegawaiProfile: PegawaiProfile?,
    photoUrl: String?,
    assessmentSummary: AssessmentSummaryData?,
    targetSummary: DashboardTargetSummaryData?,
    currentTab: String,
    payrollLabel: String,
    isDarkTheme: Boolean
) {
    val greeting = currentGreeting()
    val nama = pegawaiProfile?.pegawaiNama.displayOrDash()
    val jabatan = pegawaiProfile?.jabatan.displayOrDash()
    val skpd = pegawaiProfile?.skpd.displayOrDash()
    val activePeriodLabel = targetSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: assessmentSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: currentDateLabel()
    val roleLine = listOf(jabatan, skpd)
        .filter { it.isNotBlank() && it != "-" }
        .joinToString(" • ")
        .ifBlank { "Informasi jabatan belum tersedia" }
    val targetStatus = targetSummary?.status.displayOrDash()
    val assessmentStatus = assessmentSummary?.activePeriodStatus.displayOrDash()
    val targetPeriodMonth = targetSummary?.activePeriodLabel.periodMonthOnly() ?: "Bulan Ini"
    val assessmentPeriodMonth = assessmentSummary?.activePeriodLabel.periodMonthOnly()
        ?: targetPeriodMonth
    val heroStatusItems = when (currentTab) {
        "Target" -> listOf("Target $targetPeriodMonth: $targetStatus")
        "Penilaian" -> listOf("Penilaian $assessmentPeriodMonth: $assessmentStatus")
        "TPP", "Gaji" -> listOf(payrollLabel)
        else -> listOf(
            "Target $targetPeriodMonth: $targetStatus",
            "Penilaian $assessmentPeriodMonth: $assessmentStatus"
        )
    }

    GradientCard(
        modifier = Modifier.fillMaxWidth(),
        isDark = isDarkTheme,
        cornerRadius = 28.dp,
        elevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            )

            if (photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto pegawai",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(58.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(end = if (photoUrl != null) 62.dp else 0.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$greeting,",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = nama,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = roleLine,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.84f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = activePeriodLabel,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White.copy(alpha = 0.92f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    heroStatusItems.forEach { item ->
                        HeroPlayStatusChipV2(
                            text = item,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPlayStatusChipV2(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(100),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun hasDashboardActionToShow(
    alerts: DashboardActionAlertsData?,
    targetSummary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?,
    canReviewSubordinates: Boolean,
    isTargetPeriodLocked: Boolean = false
): Boolean {
    val targetNeedAttention = alerts?.targetNeedAttentionCount ?: 0
    val realisasiNeedAttention = alerts?.realisasiNeedAttentionCount ?: 0
    val subordinateReview = alerts?.subordinateReviewCount ?: 0
    val missingAssessment = alerts?.missingAssessmentCount ?: 0
    val hasOwnAssessmentAction = assessmentSummary.hasOwnAssessmentAction(alerts)
    val isAssessmentPeriodFinal = assessmentSummary?.activePeriodStatus
        ?.equals("Final", ignoreCase = true) == true
    val targetActionLocked = isAssessmentPeriodFinal || isTargetPeriodLocked
    val hasTargetStatusAttention = targetSummary.hasTargetStatusAttention(
        isTargetPeriodLocked = targetActionLocked
    )
    val hasTargetAttention = !targetActionLocked &&
        (targetNeedAttention > 0 || realisasiNeedAttention > 0)

    return hasTargetStatusAttention ||
        hasOwnAssessmentAction ||
        (canReviewSubordinates && missingAssessment > 0) ||
        (canReviewSubordinates && subordinateReview > 0) ||
        hasTargetAttention
}

private fun AssessmentSummaryData?.hasOwnAssessmentAction(
    alerts: DashboardActionAlertsData?
): Boolean {
    val pendingCount = alerts?.ownAssessmentPendingCount ?: this?.pendingOwnAssessment ?: 0
    val activeStatus = this?.activePeriodStatus
        .orEmpty()
        .trim()
        .replace('_', ' ')
        .lowercase(Locale.getDefault())

    return pendingCount > 0 && activeStatus in setOf("draft", "review")
}

private fun DashboardTargetSummaryData?.hasTargetStatusAttention(
    isTargetPeriodLocked: Boolean = false
): Boolean {
    if (isTargetPeriodLocked) return false

    val status = this?.status
        .orEmpty()
        .trim()
        .replace('_', ' ')
        .lowercase(Locale.getDefault())

    return status == "draft" || status == "revisi"
}

private fun isOwnTargetPeriodLocked(
    assessmentSummary: AssessmentSummaryData?,
    targetItems: List<TargetKinerjaItem>,
    currentPegawaiId: Int?,
    targetPeriodYear: Int?,
    targetPeriodMonth: Int?
): Boolean {
    val isAssessmentPeriodFinal = assessmentSummary?.activePeriodStatus
        ?.equals("Final", ignoreCase = true) == true

    if (isAssessmentPeriodFinal) return true

    return targetItems.any { target ->
        val isOwnTarget = currentPegawaiId == null || target.pegawaiId == currentPegawaiId
        val isSelectedPeriod = (targetPeriodYear == null || target.tahun == targetPeriodYear) &&
            (targetPeriodMonth == null || target.bulan == targetPeriodMonth)

        isOwnTarget && isSelectedPeriod && target.isAssessmentFinalized == true
    }
}

private fun buildDashboardCoachmarkPrompt(
    alerts: DashboardActionAlertsData?,
    targetSummary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?,
    targetItems: List<TargetKinerjaItem>,
    currentPegawaiId: Int?,
    targetPeriodYear: Int?,
    targetPeriodMonth: Int?,
    canReviewSubordinates: Boolean
): DashboardCoachmarkPrompt? {
    val periodLabel = targetSummary?.activePeriodLabel
        ?.takeIf { it.isNotBlank() }
        ?: assessmentSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: if (targetPeriodYear != null && targetPeriodMonth != null) {
            monthYearLabel(targetPeriodYear, targetPeriodMonth)
        } else {
            "periode aktif"
        }
    val contextPrefix = listOf(
        "pegawai-${currentPegawaiId ?: "unknown"}",
        "periode-${periodLabel.toCoachmarkKeyPart()}"
    ).joinToString("|")
    val pendingTargetReview = pendingSubordinateTargetReview(
        targetItems = targetItems,
        currentPegawaiId = currentPegawaiId,
        targetPeriodYear = targetPeriodYear,
        targetPeriodMonth = targetPeriodMonth,
        canReviewSubordinates = canReviewSubordinates
    )

    if (pendingTargetReview != null) {
        return DashboardCoachmarkPrompt(
            key = "$contextPrefix|review-target-bawahan",
            title = "Review Target Bawahan",
            message = "Ada ${pendingTargetReview.count} target bawahan periode ${pendingTargetReview.periodLabel} yang menunggu keputusan.",
            actionLabel = "Mengerti",
            accentColor = StatusRejected
        )
    }

    val isTargetPeriodLocked = isOwnTargetPeriodLocked(
        assessmentSummary = assessmentSummary,
        targetItems = targetItems,
        currentPegawaiId = currentPegawaiId,
        targetPeriodYear = targetPeriodYear,
        targetPeriodMonth = targetPeriodMonth
    )
    val hasAction = hasDashboardActionToShow(
        alerts = alerts,
        targetSummary = targetSummary,
        assessmentSummary = assessmentSummary,
        canReviewSubordinates = canReviewSubordinates,
        isTargetPeriodLocked = isTargetPeriodLocked
    )

    if (hasAction) {
        val targetNeedAttention = alerts?.targetNeedAttentionCount ?: 0
        val realisasiNeedAttention = alerts?.realisasiNeedAttentionCount ?: 0
        val subordinateReview = alerts?.subordinateReviewCount ?: 0
        val missingAssessment = alerts?.missingAssessmentCount ?: 0
        val hasOwnAssessmentAction = assessmentSummary.hasOwnAssessmentAction(alerts)
        val targetStatus = targetSummary?.status
            .orEmpty()
            .trim()
            .replace('_', ' ')
            .lowercase(Locale.getDefault())
        val hasTargetStatusAttention = targetSummary.hasTargetStatusAttention(
            isTargetPeriodLocked = isTargetPeriodLocked
        )

        return when {
            hasTargetStatusAttention && targetStatus == "revisi" -> DashboardCoachmarkPrompt(
                key = "$contextPrefix|target-revisi",
                title = "Target Perlu Revisi",
                message = "Perbaiki target $periodLabel sesuai catatan review agar bisa diproses kembali.",
                actionLabel = "Mengerti",
                accentColor = StatusRejected
            )
            hasTargetStatusAttention -> DashboardCoachmarkPrompt(
                key = "$contextPrefix|target-draft",
                title = "Target Masih Draft",
                message = "Ajukan target $periodLabel agar bisa diproses atasan.",
                actionLabel = "Mengerti",
                accentColor = StatusRejected
            )
            hasOwnAssessmentAction -> DashboardCoachmarkPrompt(
                key = "$contextPrefix|penilaian-saya",
                title = "Penilaian Belum Final",
                message = "Lengkapi atau finalkan penilaian $periodLabel agar status periode ini selesai.",
                actionLabel = "Mengerti",
                accentColor = StatusRejected
            )
            canReviewSubordinates && missingAssessment > 0 -> DashboardCoachmarkPrompt(
                key = "$contextPrefix|penilaian-belum-dibuat",
                title = "Penilaian Bawahan Belum Dibuat",
                message = "Ada target bawahan yang sudah siap dinilai, tetapi draft penilaiannya belum dibuat.",
                actionLabel = "Mengerti",
                accentColor = StatusRejected
            )
            canReviewSubordinates && subordinateReview > 0 -> DashboardCoachmarkPrompt(
                key = "$contextPrefix|review-penilaian-bawahan",
                title = "Review Penilaian Bawahan",
                message = "Ada penilaian bawahan yang belum final dan perlu ditinjau.",
                actionLabel = "Mengerti",
                accentColor = StatusRejected
            )
            realisasiNeedAttention > 0 -> DashboardCoachmarkPrompt(
                key = "$contextPrefix|realisasi-belum-lengkap",
                title = "Realisasi Belum Lengkap",
                message = "Isi realisasi atau tautkan laporan kegiatan untuk target $periodLabel.",
                actionLabel = "Mengerti",
                accentColor = StatusRejected
            )
            targetNeedAttention > 0 -> DashboardCoachmarkPrompt(
                key = "$contextPrefix|target-perlu-perhatian",
                title = "Target Perlu Perhatian",
                message = "Buka target $periodLabel untuk melanjutkan proses periode ini.",
                actionLabel = "Mengerti",
                accentColor = StatusRejected
            )
            else -> null
        }
    }

    if (shouldShowStartTargetGuidance(targetSummary, assessmentSummary)) {
        return DashboardCoachmarkPrompt(
            key = "$contextPrefix|pegawai-baru",
            title = "Mulai dari Sini",
            message = "Buat target kerja untuk $periodLabel terlebih dahulu. Setelah itu laporan dan penilaian periode ini lebih mudah dipantau.",
            actionLabel = "Mengerti",
            accentColor = PrimaryLight
        )
    }

    return null
}

private fun String.toCoachmarkKeyPart(): String {
    return lowercase(Locale.getDefault())
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .ifBlank { "unknown" }
}

private fun shouldShowStartTargetGuidance(
    targetSummary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?
): Boolean {
    if (targetSummary == null) return false
    val isAssessmentPeriodFinal = assessmentSummary?.activePeriodStatus
        ?.equals("Final", ignoreCase = true) == true
    if (isAssessmentPeriodFinal) return false

    val targetStatus = targetSummary.status
        .orEmpty()
        .trim()
        .replace('_', ' ')
        .lowercase(Locale.getDefault())
    val statusMeansTargetExists = targetStatus in setOf(
        "draft",
        "diajukan",
        "disetujui",
        "final",
        "revisi",
        "ditolak"
    )

    return !statusMeansTargetExists &&
        (targetSummary.totalTargets ?: 0) <= 0 &&
        (targetSummary.totalItems ?: 0) <= 0
}

private data class PendingSubordinateTargetReview(
    val count: Int,
    val tahun: Int,
    val bulan: Int,
    val periodLabel: String
)

@Composable
private fun DashboardCoachmarkOverlay(
    prompt: DashboardCoachmarkPrompt,
    focusBounds: Rect,
    onDismiss: () -> Unit
) {
    val density = LocalDensity.current
    val overlayInteraction = remember { MutableInteractionSource() }
    var overlayRootOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                overlayRootOffset = coordinates.positionInRoot()
            }
    ) {
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val spotlightPaddingPx = with(density) { 8.dp.toPx() }
        val spotlightCornerPx = with(density) { 26.dp.toPx() }
        val bubbleReservePx = with(density) { 168.dp.toPx() }
        val bubbleGapPx = with(density) { 12.dp.toPx() }
        val bubbleMinTopPx = with(density) { 16.dp.toPx() }
        val relativeFocusLeft = focusBounds.left - overlayRootOffset.x
        val relativeFocusTop = focusBounds.top - overlayRootOffset.y
        val relativeFocusRight = focusBounds.right - overlayRootOffset.x
        val relativeFocusBottom = focusBounds.bottom - overlayRootOffset.y
        val focusLeft = (relativeFocusLeft - spotlightPaddingPx).coerceAtLeast(0f)
        val focusTop = (relativeFocusTop - spotlightPaddingPx).coerceAtLeast(0f)
        val focusRight = (relativeFocusRight + spotlightPaddingPx).coerceAtMost(maxWidthPx)
        val focusBottom = (relativeFocusBottom + spotlightPaddingPx).coerceAtMost(maxHeightPx)
        val preferredBubbleTop = focusBottom + bubbleGapPx
        val fallbackBubbleTop = focusTop - bubbleReservePx - bubbleGapPx
        val bubbleTop = if (preferredBubbleTop + bubbleReservePx <= maxHeightPx) {
            preferredBubbleTop
        } else {
            fallbackBubbleTop
        }.coerceAtLeast(bubbleMinTopPx)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawRect(Color.Black.copy(alpha = 0.46f))
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(focusLeft, focusTop),
                        size = Size(
                            width = (focusRight - focusLeft).coerceAtLeast(0f),
                            height = (focusBottom - focusTop).coerceAtLeast(0f)
                        ),
                        cornerRadius = CornerRadius(spotlightCornerPx, spotlightCornerPx),
                        blendMode = BlendMode.Clear
                    )
                }
                .clickable(
                    interactionSource = overlayInteraction,
                    indication = null,
                    onClick = onDismiss
                )
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset { IntOffset(0, bubbleTop.roundToInt()) },
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = prompt.accentColor.copy(alpha = 0.14f)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(9.dp)
                                .size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AttentionPulseDot(color = prompt.accentColor)
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = prompt.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = prompt.message,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = prompt.actionLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = prompt.accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttentionPulseDot(
    modifier: Modifier = Modifier,
    color: Color = StatusRejected
) {
    val transition = rememberInfiniteTransition()
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        )
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.42f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                    alpha = pulseAlpha
                }
                .clip(CircleShape)
                .background(color)
        )
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
    }
}

private fun pendingSubordinateTargetReview(
    targetItems: List<TargetKinerjaItem>,
    currentPegawaiId: Int?,
    targetPeriodYear: Int?,
    targetPeriodMonth: Int?,
    canReviewSubordinates: Boolean
): PendingSubordinateTargetReview? {
    if (!canReviewSubordinates) return null

    val pendingTargets = targetItems.filter { target ->
        val isSubmitted = target.status
            .trim()
            .replace('_', ' ')
            .equals("diajukan", ignoreCase = true)
        val isSubordinate = currentPegawaiId == null || target.pegawaiId != currentPegawaiId
        val isSelectedPeriod = (targetPeriodYear == null || target.tahun == targetPeriodYear) &&
            (targetPeriodMonth == null || target.bulan == targetPeriodMonth)

        isSubmitted && isSubordinate && isSelectedPeriod
    }

    if (pendingTargets.isEmpty()) return null

    val sample = pendingTargets.first()
    return PendingSubordinateTargetReview(
        count = pendingTargets.size,
        tahun = sample.tahun,
        bulan = sample.bulan,
        periodLabel = monthYearLabel(sample.tahun, sample.bulan)
    )
}

@Composable
private fun DashboardFocusSection(
    alerts: DashboardActionAlertsData?,
    targetSummary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?,
    targetItems: List<TargetKinerjaItem>,
    currentPegawaiId: Int?,
    targetPeriodYear: Int?,
    targetPeriodMonth: Int?,
    canReviewSubordinates: Boolean,
    onOpenReports: () -> Unit,
    onOpenTarget: () -> Unit,
    onCreateTarget: (Int?, Int?) -> Unit,
    onOpenSubordinateTargetPeriod: (Int, Int) -> Unit,
    onOpenPenilaian: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenPenilaianBelumDibuat: () -> Unit
) {
    val showStarter = shouldShowStartTargetGuidance(targetSummary, assessmentSummary)
    val pendingSubordinateTargetReview = pendingSubordinateTargetReview(
        targetItems = targetItems,
        currentPegawaiId = currentPegawaiId,
        targetPeriodYear = targetPeriodYear,
        targetPeriodMonth = targetPeriodMonth,
        canReviewSubordinates = canReviewSubordinates
    )
    val isTargetPeriodLocked = isOwnTargetPeriodLocked(
        assessmentSummary = assessmentSummary,
        targetItems = targetItems,
        currentPegawaiId = currentPegawaiId,
        targetPeriodYear = targetPeriodYear,
        targetPeriodMonth = targetPeriodMonth
    )
    val showGeneralAction = hasDashboardActionToShow(
        alerts = alerts,
        targetSummary = targetSummary,
        assessmentSummary = assessmentSummary,
        canReviewSubordinates = canReviewSubordinates,
        isTargetPeriodLocked = isTargetPeriodLocked
    )
    val showAction = pendingSubordinateTargetReview != null || showGeneralAction

    if (!showAction && !showStarter) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showAction) {
            DashboardFocusHeader(
                title = "Perlu Tindakan",
                subtitle = "Prioritas yang perlu diproses sekarang.",
                showPulse = true,
                pulseColor = StatusRejected
            )
            if (pendingSubordinateTargetReview != null) {
                SubordinateTargetReviewActionSection(
                    review = pendingSubordinateTargetReview,
                    onOpenReview = {
                        onOpenSubordinateTargetPeriod(
                            pendingSubordinateTargetReview.tahun,
                            pendingSubordinateTargetReview.bulan
                        )
                    }
                )
            } else {
                MinimalActionFocusSection(
                    alerts = alerts,
                    targetSummary = targetSummary,
                    assessmentSummary = assessmentSummary,
                    canReviewSubordinates = canReviewSubordinates,
                    isTargetPeriodLocked = isTargetPeriodLocked,
                    onOpenReports = onOpenReports,
                    onOpenTarget = onOpenTarget,
                    onOpenPenilaian = onOpenPenilaian,
                    onOpenReview = onOpenReview,
                    onOpenPenilaianBelumDibuat = onOpenPenilaianBelumDibuat
                )
            }
        } else {
            DashboardFocusHeader(
                title = "Pegawai Baru",
                subtitle = "Mulai alur kerja periode aktif dari target kerja.",
                showPulse = true,
                pulseColor = PrimaryLight
            )
            StarterGuidanceSection(
                targetSummary = targetSummary,
                assessmentSummary = assessmentSummary,
                targetPeriodYear = targetPeriodYear,
                targetPeriodMonth = targetPeriodMonth,
                onCreateTarget = onCreateTarget
            )
        }
    }
}

@Composable
private fun DashboardFocusHeader(
    title: String,
    subtitle: String,
    showPulse: Boolean = false,
    pulseColor: Color = StatusRejected
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (showPulse) {
            AttentionPulseDot(color = pulseColor)
        }
    }
}

@Composable
private fun SubordinateTargetReviewActionSection(
    review: PendingSubordinateTargetReview,
    onOpenReview: () -> Unit
) {
    val title = if (review.count == 1) {
        "1 target bawahan menunggu review"
    } else {
        "${review.count} target bawahan menunggu review"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tinjau target bawahan periode ${review.periodLabel}.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                modifier = Modifier.clickable(onClick = onOpenReview),
                shape = RoundedCornerShape(16.dp),
                color = StatusPending.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "Review Target",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = StatusPending,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun StarterGuidanceSection(
    targetSummary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?,
    targetPeriodYear: Int?,
    targetPeriodMonth: Int?,
    onCreateTarget: (Int?, Int?) -> Unit
) {
    if (!shouldShowStartTargetGuidance(targetSummary, assessmentSummary)) return

    val periodLabel = targetSummary?.activePeriodLabel
        ?.takeIf { it.isNotBlank() }
        ?: if (targetPeriodYear != null && targetPeriodMonth != null) {
            monthYearLabel(targetPeriodYear, targetPeriodMonth)
        } else {
            "periode aktif"
        }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = StatusApproved.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlaylistAddCheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(12.dp)
                            .size(26.dp),
                        tint = StatusApproved
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Mulai dari Sini",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = "Buat target kerja untuk $periodLabel terlebih dahulu.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "Setelah target dibuat, laporan kegiatan dan penilaian periode ini akan lebih mudah dipantau.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCreateTarget(targetPeriodYear, targetPeriodMonth) },
                shape = RoundedCornerShape(18.dp),
                color = PrimaryLight.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.28f))
            ) {
                Text(
                    text = "Buat Target",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = PrimaryLight,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MinimalActionFocusSection(
    alerts: DashboardActionAlertsData?,
    targetSummary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?,
    canReviewSubordinates: Boolean,
    isTargetPeriodLocked: Boolean,
    onOpenReports: () -> Unit,
    onOpenTarget: () -> Unit,
    onOpenPenilaian: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenPenilaianBelumDibuat: () -> Unit
) {
    val targetNeedAttention = alerts?.targetNeedAttentionCount ?: 0
    val realisasiNeedAttention = alerts?.realisasiNeedAttentionCount ?: 0
    val subordinateReview = alerts?.subordinateReviewCount ?: 0
    val missingAssessment = alerts?.missingAssessmentCount ?: 0
    val hasOwnAssessmentAction = assessmentSummary.hasOwnAssessmentAction(alerts)
    val targetPeriodMonth = targetSummary?.activePeriodLabel.periodMonthOnly() ?: "bulan ini"
    val targetStatus = targetSummary?.status
        .orEmpty()
        .trim()
        .replace('_', ' ')
        .lowercase(Locale.getDefault())
    val isAssessmentPeriodFinal = assessmentSummary?.activePeriodStatus
        ?.equals("Final", ignoreCase = true) == true
    val targetActionLocked = isAssessmentPeriodFinal || isTargetPeriodLocked
    val hasTargetStatusAttention = targetSummary.hasTargetStatusAttention(
        isTargetPeriodLocked = targetActionLocked
    )
    val hasTargetAttention = !targetActionLocked &&
        (targetNeedAttention > 0 || realisasiNeedAttention > 0)
    val hasActionToShow = hasDashboardActionToShow(
        alerts = alerts,
        targetSummary = targetSummary,
        assessmentSummary = assessmentSummary,
        canReviewSubordinates = canReviewSubordinates,
        isTargetPeriodLocked = isTargetPeriodLocked
    )

    if (!hasActionToShow) return

    val primaryText = when {
        hasTargetStatusAttention -> {
            if (targetStatus == "revisi") "Target perlu revisi" else "Target masih draft"
        }
        hasOwnAssessmentAction -> "Penilaian saya belum final"
        canReviewSubordinates && missingAssessment > 0 -> {
            if (missingAssessment == 1) "1 penilaian belum dibuat" else "$missingAssessment penilaian belum dibuat"
        }
        canReviewSubordinates && subordinateReview > 0 -> {
            if (subordinateReview == 1) "1 penilaian bawahan belum final" else "$subordinateReview penilaian bawahan belum final"
        }
        hasTargetAttention -> {
            val totalAttention = targetNeedAttention + realisasiNeedAttention
            when {
                realisasiNeedAttention > 0 && targetNeedAttention <= 0 -> {
                    if (realisasiNeedAttention == 1) {
                        "1 item perlu realisasi"
                    } else {
                        "$realisasiNeedAttention item perlu realisasi"
                    }
                }
                targetNeedAttention > 0 && realisasiNeedAttention <= 0 -> {
                    if (targetNeedAttention == 1) {
                        "1 target perlu perhatian"
                    } else {
                        "$targetNeedAttention target perlu perhatian"
                    }
                }
                totalAttention == 1 -> "1 pekerjaan target perlu perhatian"
                else -> "$totalAttention pekerjaan target perlu perhatian"
            }
        }
        isAssessmentPeriodFinal -> "Periode target sudah final"
        else -> "Tidak ada tindakan mendesak saat ini"
    }

    val secondaryText = when {
        hasTargetStatusAttention -> {
            if (targetStatus == "revisi") {
                "Perbaiki target bulan $targetPeriodMonth sesuai catatan review."
            } else {
                "Ajukan target bulan $targetPeriodMonth agar bisa diproses atasan."
            }
        }
        hasOwnAssessmentAction -> "Lengkapi atau finalkan penilaian periode aktif."
        canReviewSubordinates && missingAssessment > 0 -> "Target siap dinilai, draft penilaian belum ada."
        canReviewSubordinates && subordinateReview > 0 -> "Tidak termasuk penilaian saya"
        hasTargetAttention -> {
            if (realisasiNeedAttention > 0) {
                "Isi realisasi atau tautkan laporan kegiatan untuk bulan $targetPeriodMonth."
            } else {
                "Buka target bulan $targetPeriodMonth untuk melanjutkan."
            }
        }
        isAssessmentPeriodFinal -> "Target dan realisasi bulan $targetPeriodMonth hanya bisa dilihat."
        else -> "Semua ringkasan utama terlihat aman"
    }

    val actionLabel = when {
        hasTargetStatusAttention -> "Buka Target"
        hasOwnAssessmentAction -> "Buka Penilaian"
        canReviewSubordinates && missingAssessment > 0 -> "Lihat Daftar"
        canReviewSubordinates && subordinateReview > 0 -> "Buka Penilaian"
        hasTargetAttention -> "Buka Target"
        isAssessmentPeriodFinal -> "Lihat Target"
        else -> "Lihat Target"
    }

    val actionHandler = when {
        hasTargetStatusAttention -> onOpenTarget
        hasOwnAssessmentAction -> onOpenPenilaian
        canReviewSubordinates && missingAssessment > 0 -> onOpenPenilaianBelumDibuat
        canReviewSubordinates && subordinateReview > 0 -> onOpenReview
        hasTargetAttention -> onOpenTarget
        isAssessmentPeriodFinal -> onOpenTarget
        else -> onOpenTarget
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                modifier = Modifier.clickable(onClick = actionHandler),
                shape = RoundedCornerShape(16.dp),
                color = StatusPending.copy(alpha = 0.14f)
            ) {
                Text(
                    text = actionLabel,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = StatusPending,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MinimalQuickActionsSection(
    tertundaCount: Int?,
    onViewReports: () -> Unit,
    onTargetKinerja: () -> Unit,
    onPenilaianKinerja: () -> Unit,
    onTertunda: () -> Unit,
    onPayrollSaya: () -> Unit,
    payrollLabel: String,
    onTemplates: () -> Unit,
    onReminder: () -> Unit
) {
    val tertundaBadge = when {
        tertundaCount == null || tertundaCount <= 0 -> null
        tertundaCount > 99 -> "99+"
        else -> tertundaCount.toString()
    }

    val actions = listOf(
        CompactQuickActionData("Laporan", Icons.AutoMirrored.Filled.List, SecondaryLight, onViewReports),
        CompactQuickActionData("Target", Icons.Default.TaskAlt, PrimaryLight, onTargetKinerja),
        CompactQuickActionData("Template", Icons.Default.AssignmentTurnedIn, TertiaryLight, onTemplates),
        CompactQuickActionData(
            label = "Tertunda",
            icon = Icons.Default.AccessTime,
            color = StatusRevised,
            onClick = onTertunda,
            badgeText = tertundaBadge
        ),
        CompactQuickActionData("Penilaian", Icons.Default.Bookmark, StatusApproved, onPenilaianKinerja),
        CompactQuickActionData(payrollLabel, Icons.Default.PlaylistAddCheckCircle, StatusPending, onPayrollSaya),
        CompactQuickActionData("Pengingat", Icons.Default.AlarmOn, StatusApproved, onReminder)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Akses Cepat",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 24.sp
            )
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp,
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                actions.chunked(4).forEach { rowActions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowActions.forEach { action ->
                            QuickActionGridItem(
                                action = action,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(4 - rowActions.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionGridItem(
    action: CompactQuickActionData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = action.onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(action.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = action.color,
                    modifier = Modifier.size(22.dp)
                )
            }

            action.badgeText?.let { badgeText ->
                Box(
                    modifier = Modifier.offset(x = 6.dp, y = (-6).dp),
                    contentAlignment = Alignment.Center
                ) {
                    AttentionPulseDot(color = StatusRejected)
                    Badge(
                        containerColor = StatusRejected,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }
        }
        Text(
            text = action.label,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 13.sp
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MinimalSummarySection(
    metrics: MetricsData?,
    targetSummary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?,
    tppData: TppMeData?,
    gajiData: GajiNonAsnMeData?,
    isNonAsnPayroll: Boolean
) {
    val totalKegiatan = metrics?.totalKegiatan.toIntSafe()
    val progressValue = "${(targetSummary?.persentaseProgress ?: 0.0).toInt()}%"
    val targetDone = targetSummary?.itemSudahRealisasi ?: 0
    val targetTotal = targetSummary?.totalItems ?: targetSummary?.totalTargets ?: 0
    val targetProgressLabel = if (targetTotal > 0) {
        "$targetDone/$targetTotal item realisasi"
    } else {
        "Belum ada item target"
    }
    val score = formatNullableScore(assessmentSummary?.latestFinalScore)
    val latestScorePeriod = assessmentSummary?.latestFinalPeriodLabel
        ?.takeIf { it.isNotBlank() }
        ?: "Belum ada periode final"
    val nominalTpp = tppData?.nominalTpp
    val tppPeriod = tppData?.periode?.let { monthYearLabel(it.tahun, it.bulan) }
    val gajiCalculation = gajiData?.perhitungan
    val gajiPeriod = gajiData?.periode?.let { monthYearLabel(it.tahun, it.bulan) }
    val currentPeriodLabel = targetSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: tppPeriod
        ?: gajiPeriod
        ?: assessmentSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: "Bulan ini"
    val tppStatusKey = nominalTpp?.status?.lowercase(Locale.ROOT).orEmpty()
    val tppIsLiveEstimate = tppStatusKey == "estimasi_berjalan" ||
        nominalTpp?.label?.contains("estimasi", ignoreCase = true) == true
    val tppFinalityLabel = when {
        nominalTpp == null -> "Belum dihitung"
        tppIsLiveEstimate -> "Estimasi berjalan"
        nominalTpp.isFinal -> nominalTpp.label?.takeIf { it.isNotBlank() } ?: "Final"
        else -> "Belum final"
    }
    val tppBadge = when {
        nominalTpp == null -> "BELUM"
        tppIsLiveEstimate -> "ESTIMASI"
        nominalTpp.isFinal -> "FINAL"
        else -> "DRAFT"
    }
    val tppToneColor = when {
        nominalTpp == null -> MaterialTheme.colorScheme.outline
        nominalTpp.isFinal || tppStatusKey in setOf("final", "final_opd", "dibayar", "arsip") -> StatusApproved
        tppStatusKey in setOf("revisi", "dikembalikan") -> StatusRevised
        tppStatusKey in setOf("ditolak", "batal") -> StatusRejected
        tppIsLiveEstimate -> PrimaryLight
        else -> StatusPending
    }
    val tppSubtitle = if (!tppPeriod.isNullOrBlank()) {
        "Perkiraan $tppPeriod - $tppFinalityLabel"
    } else {
        tppFinalityLabel
    }
    val gajiStatusKey = gajiCalculation?.status?.lowercase(Locale.ROOT).orEmpty()
    val gajiIsFinal = gajiStatusKey in setOf("final", "final_opd", "dibayar", "arsip")
    val gajiIsLiveEstimate = gajiStatusKey == "estimasi_berjalan"
    val gajiFinalityLabel = when {
        gajiCalculation == null -> "Belum dihitung"
        gajiIsLiveEstimate -> "Estimasi berjalan"
        gajiIsFinal -> "Final"
        else -> "Belum final"
    }
    val gajiBadge = when {
        gajiCalculation == null -> "BELUM"
        gajiIsLiveEstimate -> "ESTIMASI"
        gajiIsFinal -> "FINAL"
        else -> "DIHITUNG"
    }
    val gajiToneColor = when {
        gajiCalculation == null -> MaterialTheme.colorScheme.outline
        gajiIsFinal -> StatusApproved
        gajiIsLiveEstimate -> PrimaryLight
        gajiStatusKey in setOf("revisi", "dikembalikan") -> StatusRevised
        gajiStatusKey in setOf("ditolak", "batal") -> StatusRejected
        else -> StatusApproved
    }
    val gajiSubtitle = if (!gajiPeriod.isNullOrBlank()) {
        "Periode $gajiPeriod - $gajiFinalityLabel"
    } else {
        gajiFinalityLabel
    }
    val payrollTitle = if (isNonAsnPayroll) "Gaji Saya" else "TPP Saya"
    val payrollValue = if (isNonAsnPayroll) {
        gajiCalculation?.totalDibayar.formatDashboardCurrency()
    } else {
        nominalTpp?.estimasiDiterima.formatDashboardCurrency()
    }
    val payrollSubtitle = if (isNonAsnPayroll) gajiSubtitle else tppSubtitle
    val payrollBadge = if (isNonAsnPayroll) gajiBadge else tppBadge
    val payrollToneColor = if (isNonAsnPayroll) gajiToneColor else tppToneColor
    val scoreSubtitle = if (latestScorePeriod == "Belum ada periode final") {
        latestScorePeriod
    } else {
        "Final $latestScorePeriod"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Ringkasan Bulan Ini",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 24.sp
            )
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp,
            cornerRadius = 24.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MinimalSummaryCard(
                        title = "Laporan Kegiatan",
                        value = totalKegiatan.toString(),
                        modifier = Modifier.weight(1f),
                        subtitle = currentPeriodLabel
                    )
                    MinimalSummaryCard(
                        title = "Progress Target",
                        value = progressValue,
                        modifier = Modifier.weight(1f),
                        subtitle = targetProgressLabel
                    )
                }
                MinimalTppSummaryCard(
                    title = payrollTitle,
                    value = payrollValue,
                    modifier = Modifier.fillMaxWidth(),
                    subtitle = payrollSubtitle,
                    badge = payrollBadge,
                    toneColor = payrollToneColor
                )
                MinimalScoreSummaryCard(
                    title = "Nilai Kinerja Terakhir",
                    value = score,
                    subtitle = scoreSubtitle,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun MinimalTppSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    badge: String,
    toneColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = toneColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, toneColor.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = RoundedCornerShape(100),
                    color = toneColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = toneColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MinimalScoreSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MinimalReviewerStatusCard(
    title: String,
    value: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 24.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = StatusRejected
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MinimalActionButton(
                label = actionLabel,
                icon = Icons.Default.Groups,
                onClick = onAction
            )
        }
    }
}

@Composable
private fun MinimalActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        elevation = 0.dp,
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MinimalSummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 74.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class CompactQuickActionData(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit,
    val badgeText: String? = null
)

@Composable
fun ActionAlertsSection(
    alerts: DashboardActionAlertsData?,
    canReviewSubordinates: Boolean
) {
    val targetNeedAttention = alerts?.targetNeedAttentionCount ?: 0
    val realisasiNeedAttention = alerts?.realisasiNeedAttentionCount ?: 0
    val laporanPending = alerts?.laporanPendingCount ?: 0
    val subordinateReview = alerts?.subordinateReviewCount ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Perlu Tindakan",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatInsightCard(
                title = "Target",
                value = targetNeedAttention.toString(),
                subtitle = "Draft atau revisi aktif",
                icon = Icons.Default.Checklist,
                accent = if (targetNeedAttention > 0) StatusPending else SecondaryLight,
                modifier = Modifier.weight(1f)
            )
            StatInsightCard(
                title = "Realisasi",
                value = realisasiNeedAttention.toString(),
                subtitle = "Item belum terisi",
                icon = Icons.Default.PlaylistAddCheckCircle,
                accent = if (realisasiNeedAttention > 0) StatusRevised else SecondaryLight,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatInsightCard(
                title = "Laporan",
                value = laporanPending.toString(),
                subtitle = "Menunggu tindak lanjut",
                icon = Icons.Default.AssignmentTurnedIn,
                accent = if (laporanPending > 0) StatusPending else SecondaryLight,
                modifier = Modifier.weight(1f)
            )
            StatInsightCard(
                title = if (canReviewSubordinates) "Penilaian Pegawai" else "Review",
                value = if (canReviewSubordinates) subordinateReview.toString() else "0",
                subtitle = if (canReviewSubordinates) "Belum final tanpa penilaian saya" else "Belum ada review",
                icon = if (canReviewSubordinates) Icons.Default.Groups else Icons.Default.TaskAlt,
                accent = if (canReviewSubordinates && subordinateReview > 0) StatusRejected else SecondaryLight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TargetProgressSection(
    summary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?,
    targetItems: List<TargetKinerjaItem>,
    currentPegawaiId: Int?,
    targetPeriodYear: Int?,
    targetPeriodMonth: Int?,
    canReviewSubordinates: Boolean,
    onOpenReports: () -> Unit,
    onPeriodSelected: (Int, Int) -> Unit,
    onCreateTarget: (Int?, Int?) -> Unit,
    onOpenTargetDetail: (Int, Boolean) -> Unit,
    onOpenSubordinateTargetPeriod: (Int, Int) -> Unit
) {
    val status = summary?.status.displayOrDash()
    val periodLabel = summary?.activePeriodLabel.displayOrDash()
    val summaryTotalTargets = summary?.totalTargets ?: 0
    val summaryTotalItems = summary?.totalItems ?: 0
    val itemSudahRealisasi = summary?.itemSudahRealisasi ?: 0
    val itemBelumRealisasi = summary?.itemBelumRealisasi ?: 0
    val totalLaporanTertaut = summary?.totalLaporanTertaut ?: 0
    val progress = (summary?.persentaseProgress ?: 0.0).coerceIn(0.0, 100.0)
    val myTargets = if (currentPegawaiId != null) {
        targetItems.filter { it.pegawaiId == currentPegawaiId }
    } else {
        targetItems
    }
    val subordinateTargets = if (currentPegawaiId != null) {
        targetItems.filter { it.pegawaiId != currentPegawaiId }
    } else {
        emptyList()
    }
    val statusColor = targetStatusColor(status)
    val normalizedStatus = status.lowercase(Locale.getDefault())
    val displayStatus = status.toTargetDisplayText()
    val periodMonth = periodLabel.periodMonthOnly() ?: "periode ini"
    val totalItemsFromTargets = myTargets.sumOf { target ->
        target.detailCount ?: target.details.orEmpty().size
    }
    val totalItems = maxOf(summaryTotalItems, totalItemsFromTargets)
    val totalTargets = maxOf(
        summaryTotalTargets,
        myTargets.size,
        if (totalItems > 0) 1 else 0
    )
    val hasTargetForPeriod = totalTargets > 0 || normalizedStatus in setOf(
        "draft",
        "diajukan",
        "disetujui",
        "final",
        "revisi",
        "ditolak"
    )
    val firstMyTarget = myTargets.firstOrNull()
    val isAssessmentPeriodFinal = assessmentSummary?.activePeriodStatus
        ?.equals("Final", ignoreCase = true) == true
    val isPeriodLocked = isAssessmentPeriodFinal || myTargets.any { it.isAssessmentFinalized == true }
    val displayItemBelumRealisasi = if (isPeriodLocked) 0 else itemBelumRealisasi
    val displayItemSudahRealisasi = if (isPeriodLocked && totalItems > 0) {
        maxOf(itemSudahRealisasi, totalItems)
    } else {
        itemSudahRealisasi
    }
    val displayProgress = if (isPeriodLocked && totalItems > 0) {
        100.0
    } else {
        progress
    }
    val needsRealisasi = hasTargetForPeriod && !isPeriodLocked && displayItemBelumRealisasi > 0
    val primaryMessage = when {
        !hasTargetForPeriod -> "Belum ada target periode ini"
        isPeriodLocked -> "Periode sudah dikunci"
        needsRealisasi -> "$displayItemBelumRealisasi item perlu realisasi"
        displayProgress >= 100.0 -> "Semua item target sudah terisi"
        else -> "$displayItemSudahRealisasi item realisasi terisi"
    }
    val heroTitle = when {
        !hasTargetForPeriod -> "Belum ada target aktif"
        normalizedStatus in setOf("final", "disetujui") -> "Target sudah disetujui"
        normalizedStatus == "diajukan" -> "Target sedang diajukan"
        normalizedStatus == "revisi" -> "Target perlu revisi"
        normalizedStatus == "draft" -> "Target masih draft"
        else -> "Target $displayStatus"
    }
    val heroDescription = when {
        !hasTargetForPeriod -> "Target untuk $periodMonth belum tersedia."
        isPeriodLocked -> "Periode $periodMonth sudah dikunci. Target dan realisasi hanya bisa dilihat."
        needsRealisasi -> "Sekarang lengkapi realisasi dari target bulan $periodMonth."
        displayProgress >= 100.0 -> "Realisasi target $periodMonth sudah lengkap."
        else -> "Pantau realisasi target bulan $periodMonth."
    }
    val progressDescription = when {
        !hasTargetForPeriod -> "Belum ada item target yang bisa direalisasikan."
        isPeriodLocked -> "Penilaian periode ini sudah final. Tidak ada tindakan yang perlu dilakukan di Android."
        needsRealisasi -> "Belum semua realisasi terisi. Buat laporan kegiatan lalu tautkan ke item target."
        displayProgress >= 100.0 -> "Semua item realisasi sudah terisi untuk periode ini."
        else -> "Sebagian realisasi sudah terisi, lanjutkan sampai lengkap."
    }
    val primaryActionLabel = when {
        firstMyTarget == null && !isPeriodLocked -> "Tambah Target"
        firstMyTarget == null -> "Periode Dikunci"
        isPeriodLocked -> "Lihat Detail Target"
        needsRealisasi -> "Lengkapi Realisasi"
        else -> "Lihat Detail Target"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Target",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Target dan realisasi pegawai",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TargetPeriodChip(
                periodLabel = periodLabel,
                selectedYear = targetPeriodYear,
                selectedMonth = targetPeriodMonth,
                onPeriodSelected = onPeriodSelected
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp,
            cornerRadius = 24.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Periode $periodMonth $targetPeriodYear",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.6.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = heroTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    lineHeight = 23.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        StatusChip(
                            text = displayStatus,
                            color = statusColor
                        )
                    }
                }

                TargetPeriodHint()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TargetProgressRing(progress = displayProgress)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = primaryMessage,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = progressDescription,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LinearProgressIndicator(
                            progress = { (displayProgress / 100.0).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(100)),
                            color = if (displayProgress >= 100.0) StatusApproved else PrimaryLight,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TargetMetricTile(
                        title = "Item terisi",
                        value = "$displayItemSudahRealisasi/$totalItems",
                        subtitle = when {
                            isPeriodLocked -> "Final"
                            displayItemBelumRealisasi > 0 -> "Belum lengkap"
                            else -> "Lengkap"
                        },
                        accent = if (displayItemBelumRealisasi > 0) StatusPending else StatusApproved,
                        modifier = Modifier.weight(1f)
                    )
                    TargetMetricTile(
                        title = "Laporan tertaut",
                        value = totalLaporanTertaut.toString(),
                        subtitle = if (totalLaporanTertaut > 0) "Sudah tertaut" else "Butuh laporan",
                        accent = SecondaryLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TargetMetricTile(
                        title = "Target aktif",
                        value = totalTargets.toString(),
                        subtitle = "Periode $periodMonth",
                        accent = StatusApproved,
                        modifier = Modifier.weight(1f)
                    )
                    TargetMetricTile(
                        title = "Sisa item",
                        value = displayItemBelumRealisasi.toString(),
                        subtitle = when {
                            isPeriodLocked -> "Dikunci"
                            displayItemBelumRealisasi > 0 -> "Perlu diisi"
                            else -> "Selesai"
                        },
                        accent = if (displayItemBelumRealisasi > 0) StatusPending else StatusApproved,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TargetActionButton(
                        label = primaryActionLabel,
                        color = PrimaryLight,
                        enabled = firstMyTarget != null || !isPeriodLocked,
                        modifier = Modifier.weight(1.15f),
                        onClick = {
                            firstMyTarget?.let {
                                onOpenTargetDetail(it.id, false)
                            } ?: onCreateTarget(targetPeriodYear, targetPeriodMonth)
                        }
                    )
                    TargetActionButton(
                        label = "Buka Laporan",
                        color = SecondaryLight,
                        modifier = Modifier.weight(0.85f),
                        onClick = onOpenReports
                    )
                }
            }
        }

        TargetListSection(
            title = "Target Saya",
            items = myTargets,
            emptyLabel = "Belum ada target saya pada periode aktif",
            currentPegawaiId = currentPegawaiId,
            needsRealisasi = needsRealisasi,
            onOpenTargetDetail = onOpenTargetDetail
        )

        if (canReviewSubordinates) {
            TargetSubordinatePeriodSection(
                items = subordinateTargets,
                emptyLabel = "Belum ada target bawahan pada periode $periodMonth",
                onOpenPeriod = onOpenSubordinateTargetPeriod
            )
        }
    }
}

private fun targetStatusColor(status: String): Color {
    return when (status.lowercase(Locale.getDefault())) {
        "final", "disetujui" -> StatusApproved
        "diajukan" -> StatusPending
        "draft" -> PrimaryLight
        "revisi" -> StatusRevised
        "ditolak" -> StatusRejected
        else -> SecondaryLight
    }
}

private fun String.toTargetDisplayText(): String {
    if (isBlank() || this == "-") return "-"
    return replace('_', ' ')
        .lowercase(Locale.getDefault())
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase(Locale("id", "ID")) else char.toString()
            }
        }
}

@Composable
private fun TargetPeriodChip(
    periodLabel: String,
    selectedYear: Int?,
    selectedMonth: Int?,
    onPeriodSelected: (Int, Int) -> Unit
) {
    val currentCalendar = remember { Calendar.getInstance() }
    val fallbackYear = currentCalendar.get(Calendar.YEAR)
    val fallbackMonth = currentCalendar.get(Calendar.MONTH) + 1
    var showDialog by remember { mutableStateOf(false) }
    var draftMonth by remember { mutableStateOf(selectedMonth?.takeIf { it in 1..12 } ?: fallbackMonth) }
    var draftYearText by remember { mutableStateOf((selectedYear ?: fallbackYear).toString()) }
    val draftYear = draftYearText.toIntOrNull()
    val isYearValid = draftYear != null && draftYear in 2000..2100

    Box {
        Surface(
            modifier = Modifier.clickable {
                draftMonth = selectedMonth?.takeIf { it in 1..12 } ?: fallbackMonth
                draftYearText = (selectedYear ?: fallbackYear).toString()
                showDialog = true
            },
            shape = RoundedCornerShape(100),
            color = PrimaryLight.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.26f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = PrimaryLight,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = periodLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = PrimaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "v",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = PrimaryLight
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Pilih Periode",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Bulan",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val monthLabels = listOf(
                        "Jan", "Feb", "Mar",
                        "Apr", "Mei", "Jun",
                        "Jul", "Agu", "Sep",
                        "Okt", "Nov", "Des"
                    )
                    monthLabels.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowMonths.forEachIndexed { columnIndex, label ->
                                val monthNumber = rowIndex * 3 + columnIndex + 1
                                val selected = draftMonth == monthNumber
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clickable { draftMonth = monthNumber },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) {
                                        PrimaryLight.copy(alpha = 0.16f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) PrimaryLight.copy(alpha = 0.38f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.ExtraBold
                                            ),
                                            color = if (selected) PrimaryLight
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = draftYearText,
                        onValueChange = { value ->
                            draftYearText = value.filter { it.isDigit() }.take(4)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Tahun") },
                        isError = draftYearText.isNotBlank() && !isYearValid,
                        supportingText = {
                            if (draftYearText.isNotBlank() && !isYearValid) {
                                Text("Masukkan tahun antara 2000 sampai 2100.")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Text(
                        text = "Periode dipilih: ${monthYearLabel(draftYear ?: fallbackYear, draftMonth)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            },
            confirmButton = {
                TextButton(
                    enabled = isYearValid,
                    onClick = {
                        draftYear?.let { year ->
                            showDialog = false
                            onPeriodSelected(year, draftMonth)
                        }
                    }
                ) {
                    Text("Terapkan")
                }
            }
        )
    }
}

@Composable
private fun TargetPeriodHint() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PrimaryLight.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, PrimaryLight.copy(alpha = 0.14f))
    ) {
        Text(
            text = "Pilih periode di kanan atas untuk melihat target bulan dan tahun yang dibutuhkan.",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TargetProgressRing(progress: Double) {
    val progressFraction = (progress / 100.0).toFloat().coerceIn(0f, 1f)
    val ringColor = if (progress >= 100.0) StatusApproved else StatusPending

    Box(
        modifier = Modifier.size(104.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxSize(),
            color = ringColor,
            trackColor = ringColor.copy(alpha = 0.15f),
            strokeWidth = 10.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${progress.toInt()}%",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1
            )
            Text(
                text = "Progress",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TargetMetricTile(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 82.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TargetActionButton(
    label: String,
    color: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = if (enabled) color.copy(alpha = 0.13f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, if (enabled) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = if (enabled) color else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TargetSubordinatePeriodSection(
    items: List<TargetKinerjaItem>,
    emptyLabel: String,
    onOpenPeriod: (Int, Int) -> Unit
) {
    val groups = remember(items) {
        items
            .groupBy { "${it.tahun}-${it.bulan}" }
            .map { (_, targets) ->
                val first = targets.first()
                TargetPeriodGroup(
                    tahun = first.tahun,
                    bulan = first.bulan,
                    label = formatDashboardTargetPeriod(first.bulan, first.tahun),
                    targets = targets.sortedBy { it.pegawaiNama.orEmpty() }
                )
            }
            .sortedWith(
                compareByDescending<TargetPeriodGroup> { it.tahun }
                    .thenByDescending { it.bulan }
            )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Target Bawahan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (groups.isNotEmpty()) {
                    Text(
                        text = "Dikelompokkan berdasarkan periode terbaru",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        when {
            items.isEmpty() -> {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 0.dp,
                    cornerRadius = 20.dp
                ) {
                    Text(
                        text = emptyLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                groups.take(8).forEach { group ->
                    TargetPeriodGroupCard(
                        group = group,
                        onClick = { onOpenPeriod(group.tahun, group.bulan) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetPeriodGroupCard(
    group: TargetPeriodGroup,
    onClick: () -> Unit
) {
    val employeeCount = group.targets.map { it.pegawaiId }.distinct().size
    val targetCount = group.targets.size
    val itemCount = group.targets.sumOf { it.detailCount ?: it.details.orEmpty().size }
    val finalCount = group.targets.count { it.isAssessmentFinalized == true || it.status.equals("final", ignoreCase = true) }
    val pendingCount = group.targets.count { it.status.equals("diajukan", ignoreCase = true) || it.status.equals("draft", ignoreCase = true) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 0.dp,
        cornerRadius = 20.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = group.label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$employeeCount pegawai sudah input target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(
                    text = "Lihat Bawahan",
                    color = StatusPending
                )
            }

            Text(
                text = "$targetCount target - $itemCount item target",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    text = "$employeeCount pegawai",
                    color = PrimaryLight,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    text = "$finalCount final",
                    color = StatusApproved,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    text = "$pendingCount proses",
                    color = StatusPending,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TargetSubordinateEmployeeCard(
    item: TargetKinerjaItem,
    reviewMode: Boolean,
    onClick: () -> Unit
) {
    val detailCount = item.detailCount ?: item.details.orEmpty().size
    val statusColor = targetStatusColor(item.status)
    val displayStatus = item.status.toTargetDisplayText()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 0.dp,
        cornerRadius = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.pegawaiNama?.takeIf { it.isNotBlank() } ?: "Pegawai #${item.pegawaiId}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${formatDashboardTargetPeriod(item.bulan, item.tahun)} - $detailCount item target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(
                    text = if (reviewMode) "Buka Detail" else displayStatus,
                    color = if (reviewMode) StatusPending else statusColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    text = "$detailCount item",
                    color = PrimaryLight,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    text = if (item.isAssessmentFinalized == true) "Periode Final" else displayStatus,
                    color = if (item.isAssessmentFinalized == true) StatusApproved else statusColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun formatDashboardTargetPeriod(bulan: Int, tahun: Int): String {
    return SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        .format(Calendar.getInstance().apply {
            set(Calendar.YEAR, tahun)
            set(Calendar.MONTH, bulan - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }.time)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() }
}

@Composable
private fun TargetListSection(
    title: String,
    items: List<TargetKinerjaItem>,
    emptyLabel: String,
    currentPegawaiId: Int?,
    needsRealisasi: Boolean,
    onOpenTargetDetail: (Int, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (items.isEmpty()) {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 0.dp,
                cornerRadius = 20.dp
            ) {
                Text(
                    text = emptyLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items.take(6).forEach { item ->
                TargetListItemCard(
                    item = item,
                    reviewMode = currentPegawaiId != null && item.pegawaiId != currentPegawaiId,
                    needsRealisasi = needsRealisasi,
                    onClick = { onOpenTargetDetail(item.id, currentPegawaiId != null && item.pegawaiId != currentPegawaiId) }
                )
            }
        }
    }
}

@Composable
private fun TargetListItemCard(
    item: TargetKinerjaItem,
    reviewMode: Boolean,
    needsRealisasi: Boolean,
    onClick: () -> Unit
) {
    val periodeLabel = remember(item.bulan, item.tahun) {
        SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
            .format(Calendar.getInstance().apply {
                set(Calendar.YEAR, item.tahun)
                set(Calendar.MONTH, item.bulan - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }.time)
    }
    val statusColor = when (item.status.lowercase(Locale.getDefault())) {
        "final", "disetujui" -> StatusApproved
        "diajukan" -> StatusPending
        "draft" -> PrimaryLight
        "revisi" -> StatusRevised
        else -> SecondaryLight
    }
    val displayStatus = item.status.toTargetDisplayText()
    val safeDetails = item.details.orEmpty()
    val itemTitle = safeDetails.firstOrNull()?.uraianTarget?.takeIf { it.isNotBlank() }
        ?: "Target ${periodeLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() }}"
    val detailCount = item.detailCount ?: safeDetails.size
    val actionStatus = when {
        reviewMode -> "Buka Review"
        needsRealisasi -> "Belum Realisasi"
        item.isAssessmentFinalized == true -> "Sudah Final"
        else -> displayStatus
    }
    val actionColor = when {
        reviewMode || needsRealisasi -> StatusPending
        item.isAssessmentFinalized == true -> StatusApproved
        else -> statusColor
    }
    val description = when {
        reviewMode -> "Buka detail untuk memantau target dan realisasi bawahan."
        needsRealisasi -> "Target ini belum lengkap. Buka detail untuk mengisi realisasi atau menautkan laporan."
        else -> "Buka detail untuk melihat status target dan realisasi."
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 0.dp,
        cornerRadius = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = itemTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${periodeLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() }} - $detailCount item target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(
                    text = actionStatus,
                    color = actionColor
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusChip(
                    text = "$detailCount item target",
                    color = PrimaryLight,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    text = item.approverNama?.takeIf { it.isNotBlank() } ?: displayStatus,
                    color = if (reviewMode) StatusPending else statusColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onViewReports: () -> Unit,
    onTargetKinerja: () -> Unit,
    onPenilaianKinerja: () -> Unit,
    onTemplates: () -> Unit,
    onReminder: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        val actions = listOf(
            QuickActionData(
                title = "Laporan",
                subtitle = "Pantau status",
                icon = Icons.AutoMirrored.Filled.List,
                color = SecondaryLight,
                onClick = onViewReports
            ),
            QuickActionData(
                title = "Target",
                subtitle = "Kelola target",
                icon = Icons.Default.TaskAlt,
                color = PrimaryLight,
                onClick = onTargetKinerja
            ),
            QuickActionData(
                title = "Penilaian",
                subtitle = "Review nilai",
                icon = Icons.Default.AssignmentTurnedIn,
                color = StatusApproved,
                onClick = onPenilaianKinerja
            ),
            QuickActionData(
                title = "Template",
                subtitle = "Mulai lebih cepat",
                icon = Icons.Default.Bookmark,
                color = TertiaryLight,
                onClick = onTemplates
            ),
            QuickActionData(
                title = "Reminder",
                subtitle = "Kelola agenda",
                icon = Icons.Default.AlarmOn,
                color = StatusRevised,
                onClick = onReminder
            )
        )

        actions.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { action ->
                    QuickActionButton(
                        title = action.title,
                        subtitle = action.subtitle,
                        icon = action.icon,
                        color = action.color,
                        modifier = Modifier.weight(1f),
                        onClick = action.onClick
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class QuickActionData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        elevation = 1.dp,
        cornerRadius = 22.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AssessmentSummarySection(
    summary: AssessmentSummaryData?,
    targetSummary: DashboardTargetSummaryData?,
    targetPeriodYear: Int?,
    targetPeriodMonth: Int?,
    onPeriodSelected: (Int, Int) -> Unit,
    onOpenTarget: () -> Unit,
    onOpenPenilaian: () -> Unit,
    onOpenReviewBawahan: () -> Unit,
    onOpenBelumDibuat: () -> Unit
) {
    val activeStatus = summary?.activePeriodStatus.displayOrDash()
    val activeLabel = summary?.activePeriodLabel.displayOrDash()
    val normalizedActiveStatus = activeStatus.lowercase(Locale.getDefault())
    val selectedScore = formatNullableScore(summary?.selectedPeriodScore)
    val selectedPredicate = summary?.selectedPeriodPredicate.displayOrDash()
    val pendingOwnAssessment = summary?.pendingOwnAssessment ?: 0
    val pendingSubordinates = summary?.pendingSubordinateAssessments ?: 0
    val missingSubordinates = summary?.missingSubordinateAssessments ?: 0
    val canReviewSubordinates = summary?.canReviewSubordinates == true
    val periodStatusSubtitle = listOf(activeStatus, activeLabel)
        .filter { it.isNotBlank() && it != "-" }
        .joinToString(" ")
        .ifBlank { "Periode dipilih" }
    val assessmentPeriodHeader = activeLabel
        .takeIf { it.isNotBlank() && it != "-" }
        ?.let { "Periode $it" }
        ?: "Periode dipilih"
    val isAssessmentFinal = normalizedActiveStatus == "final"
    val targetBlocksAssessment = targetSummary.hasTargetStatusAttention(
        isTargetPeriodLocked = isAssessmentFinal
    )
    val targetStatus = targetSummary?.status
        .orEmpty()
        .trim()
        .replace('_', ' ')
        .lowercase(Locale.getDefault())
    val assessmentHeroTitle = when {
        targetBlocksAssessment -> "Penilaian menunggu target"
        isAssessmentFinal -> "Penilaian sudah final"
        normalizedActiveStatus == "review" -> "Penilaian sedang review"
        normalizedActiveStatus == "draft" -> "Penilaian masih draft"
        normalizedActiveStatus == "belum dibuat" -> "Penilaian belum dibuat"
        activeStatus == "-" -> "Penilaian belum tersedia"
        else -> "Penilaian $activeStatus"
    }
    val hasOwnAssessmentDraft = normalizedActiveStatus in setOf("draft", "review")
    val primaryAssessmentActionLabel = when {
        targetBlocksAssessment -> "Buka Target"
        normalizedActiveStatus == "belum dibuat" -> "Lihat Penilaian Saya"
        else -> "Buka Penilaian Saya"
    }
    val primaryAssessmentAction = when {
        targetBlocksAssessment -> onOpenTarget
        else -> onOpenPenilaian
    }
    val primaryAssessmentIcon = when {
        targetBlocksAssessment -> Icons.Default.TaskAlt
        else -> Icons.Default.AssignmentTurnedIn
    }
    val primaryAssessmentIconColor = when {
        targetBlocksAssessment -> PrimaryLight
        else -> StatusApproved
    }
    val passiveAssessmentMessage = when {
        targetBlocksAssessment -> {
            if (targetStatus == "revisi") {
                "Perbaiki target periode ini terlebih dahulu sebelum penilaian bisa diproses."
            } else {
                "Ajukan target periode ini terlebih dahulu sebelum penilaian bisa dibuat."
            }
        }
        normalizedActiveStatus == "belum dibuat" ->
            "Belum ada draft penilaian untuk periode ini."
        else -> null
    }
    val pendingOwnDisplay = if (hasOwnAssessmentDraft) pendingOwnAssessment else 0
    val assessmentStatusColor = when {
        targetBlocksAssessment -> StatusPending
        normalizedActiveStatus == "final" -> StatusApproved
        normalizedActiveStatus == "review" -> StatusPending
        normalizedActiveStatus == "draft" -> PrimaryLight
        else -> StatusRevised
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Penilaian",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Penilaian kinerja pegawai",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TargetPeriodChip(
                periodLabel = activeLabel,
                selectedYear = targetPeriodYear,
                selectedMonth = targetPeriodMonth,
                onPeriodSelected = onPeriodSelected
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp,
            cornerRadius = 24.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = assessmentPeriodHeader,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = assessmentHeroTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                lineHeight = 23.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    StatusChip(
                        text = activeStatus,
                        color = assessmentStatusColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatInsightCard(
                        title = "Nilai Akhir",
                        value = selectedScore,
                        subtitle = periodStatusSubtitle,
                        icon = Icons.Default.TaskAlt,
                        accent = StatusApproved,
                        modifier = Modifier.weight(1f)
                    )
                    StatInsightCard(
                        title = "Predikat",
                        value = selectedPredicate,
                        subtitle = periodStatusSubtitle,
                        icon = Icons.Default.Star,
                        accent = TertiaryLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 0.dp,
                    cornerRadius = 20.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (passiveAssessmentMessage != null) {
                            AssessmentPassiveInfoRow(
                                title = if (targetBlocksAssessment) {
                                    if (targetStatus == "revisi") "Target perlu revisi" else "Target masih draft"
                                } else {
                                    "Penilaian belum dibuat"
                                },
                                subtitle = passiveAssessmentMessage
                            )
                        } else {
                            PendingAssessmentSummaryRow(
                                title = "Penilaian saya belum final",
                                subtitle = "Status penilaian periode dipilih milik saya",
                                value = pendingOwnDisplay,
                                highlightWhenPositive = true
                            )
                        }

                        if (canReviewSubordinates) {
                            PendingAssessmentSummaryRow(
                                title = "Penilaian bawahan belum final",
                                subtitle = "Tidak termasuk penilaian saya",
                                value = pendingSubordinates,
                                highlightWhenPositive = true
                            )
                            PendingAssessmentSummaryRow(
                                title = "Penilaian belum dibuat",
                                subtitle = "Target siap dinilai, draft belum ada",
                                value = missingSubordinates,
                                highlightWhenPositive = true
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        onClick = primaryAssessmentAction,
                        elevation = 0.dp,
                        cornerRadius = 18.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = primaryAssessmentIcon,
                                contentDescription = null,
                                tint = primaryAssessmentIconColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = primaryAssessmentActionLabel,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (canReviewSubordinates) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onOpenReviewBawahan,
                                elevation = 0.dp,
                                cornerRadius = 18.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TaskAlt,
                                        contentDescription = null,
                                        tint = PrimaryLight
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Review Bawahan",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onOpenBelumDibuat,
                                elevation = 0.dp,
                                cornerRadius = 18.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 52.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = StatusPending
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Belum Dibuat",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessmentPassiveInfoRow(
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = StatusPending.copy(alpha = 0.14f)
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier
                    .padding(10.dp)
                    .size(22.dp),
                tint = StatusPending
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PendingAssessmentSummaryRow(
    title: String,
    subtitle: String,
    value: Int,
    highlightWhenPositive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = if (highlightWhenPositive && value > 0) StatusRejected else StatusApproved
        )
    }
}

@Composable
fun PerformanceSection(metrics: MetricsData?) {
    if (metrics == null) return

    val totalDurasi = metrics.totalDurasiMenit.toIntSafe()
    val rataRata = metrics.rataRataKegiatanPerHari.toDoubleSafe()
    val rating = metrics.rataRataRating.toDoubleSafe()
    val verificationRate = metrics.persentaseVerifikasi.toFloatSafe().coerceIn(0f, 100f)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Ringkasan Kinerja",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatInsightCard(
                title = "Verifikasi",
                value = "${verificationRate.toInt()}%",
                subtitle = "Laporan telah diverifikasi",
                icon = Icons.Default.AssignmentTurnedIn,
                accent = StatusApproved,
                modifier = Modifier.weight(1f)
            )
            StatInsightCard(
                title = "Durasi",
                value = formatDuration(totalDurasi),
                subtitle = "Akumulasi waktu kegiatan",
                icon = Icons.Default.AccessTime,
                accent = SecondaryLight,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatInsightCard(
                title = "Produktivitas",
                value = String.format("%.1f", rataRata),
                subtitle = "Rata-rata kegiatan per hari",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                accent = PrimaryLight,
                modifier = Modifier.weight(1f)
            )
            StatInsightCard(
                title = "Rating",
                value = String.format("%.1f", rating),
                subtitle = "Penilaian kualitas laporan",
                icon = Icons.Default.Star,
                accent = TertiaryLight,
                modifier = Modifier.weight(1f)
            )
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp,
            cornerRadius = 24.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status Validasi Bulanan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${verificationRate.toInt()}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = StatusApproved
                    )
                }

                LinearProgressIndicator(
                    progress = { verificationRate / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(100)),
                    color = StatusApproved,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatusChip(
                        text = "${metrics.totalDiverifikasi} diverifikasi",
                        color = StatusApproved,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(
                        text = "${metrics.totalPending} pending",
                        color = StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(
                        text = "${metrics.totalDitolak} ditolak",
                        color = StatusRejected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatInsightCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = 1.dp,
        cornerRadius = 22.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TimeSeriesSection(timeSeries: List<TimeSeriesItem>) {
    val maxValue = timeSeries.maxOfOrNull { it.totalKegiatan.toIntSafe() }?.coerceAtLeast(1) ?: 1

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Tren Aktivitas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp,
            cornerRadius = 24.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Pergerakan 6 bulan terakhir",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                timeSeries.forEach { item ->
                    val total = item.totalKegiatan.toIntSafe()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = item.bulanNama,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${item.tahun}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PrimaryLight.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "$total kegiatan",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PrimaryLight
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { total.toFloat() / maxValue.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(100)),
                            color = PrimaryLight,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
