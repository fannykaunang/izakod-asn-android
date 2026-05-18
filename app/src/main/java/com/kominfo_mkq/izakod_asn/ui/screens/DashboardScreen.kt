package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.AssessmentSummaryData
import com.kominfo_mkq.izakod_asn.data.model.DashboardActionAlertsData
import com.kominfo_mkq.izakod_asn.data.model.DashboardTargetSummaryData
import com.kominfo_mkq.izakod_asn.data.model.MetricsData
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.TimeSeriesItem
import com.kominfo_mkq.izakod_asn.data.model.TppMeData
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToReports: () -> Unit,
    onNavigateToTargetKinerja: () -> Unit,
    onNavigateToTargetCreate: (Int?, Int?) -> Unit,
    onNavigateToTargetDetail: (Int, Boolean) -> Unit,
    onNavigateToSubordinateTargetPeriod: (Int, Int) -> Unit,
    onNavigateToPenilaianKinerja: () -> Unit,
    onNavigateToPenilaianBawahan: () -> Unit,
    onNavigateToTppSaya: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToReminder: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    viewModel: DashboardViewModel = viewModel(),
    tppViewModel: TppSayaViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    val tppUiState by tppViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
        tppViewModel.refresh()
    }

    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val sessionData = remember { userPreferences.getSessionData() }

    LaunchedEffect(Unit) {
        val pin = sessionData?.pin
        if (!pin.isNullOrEmpty()) {
            viewModel.loadPegawaiProfile(pin)
        }
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
        ) {
            when {
                uiState.isLoading -> LoadingContent()
                uiState.isError -> {
                    ErrorContent(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { viewModel.retry() }
                    )
                }
                else -> {
                    DashboardContent(
                        metrics = uiState.metrics,
                        timeSeries = uiState.timeSeries,
                        assessmentSummary = uiState.assessmentSummary,
                        targetSummary = uiState.targetSummary,
                        actionAlerts = uiState.actionAlerts,
                        targetItems = uiState.targetItems,
                        tppData = tppUiState.data,
                        currentPegawaiId = uiState.currentPegawaiId ?: uiState.pegawaiProfile?.pegawaiId,
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
                        onTppSaya = onNavigateToTppSaya,
                        onTargetPeriodSelected = viewModel::selectTargetPeriod,
                        onTemplates = onNavigateToTemplates,
                        onReminder = onNavigateToReminder,
                        viewModel = viewModel
                    )
                }
            }
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
    targetItems: List<TargetKinerjaItem>,
    tppData: TppMeData?,
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
    onTppSaya: () -> Unit,
    onTargetPeriodSelected: (Int, Int) -> Unit,
    onTemplates: () -> Unit,
    onReminder: () -> Unit,
    viewModel: DashboardViewModel
) {
    val tabs = listOf("Utama", "Target", "Penilaian", "Kinerja")

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
                                isDarkTheme = isDarkTheme
                            )
                        }
                        item {
                            MinimalActionFocusSection(
                                alerts = actionAlerts,
                                targetSummary = targetSummary,
                                assessmentSummary = assessmentSummary,
                                canReviewSubordinates = canReviewSubordinates,
                                onOpenReports = onViewReports,
                                onOpenTarget = onTargetKinerja,
                                onOpenReview = onPenilaianBawahan
                            )
                        }
                        item {
                            MinimalSummarySection(
                                metrics = metrics,
                                targetSummary = targetSummary,
                                assessmentSummary = assessmentSummary,
                                tppData = tppData
                            )
                        }
                        item {
                            MinimalQuickActionsSection(
                                onViewReports = onViewReports,
                                onTargetKinerja = onTargetKinerja,
                                onPenilaianKinerja = onPenilaianKinerja,
                                onTppSaya = onTppSaya,
                                onTrailingAction = if (canReviewSubordinates) onPenilaianBawahan else onReminder,
                                trailingLabel = if (canReviewSubordinates) "Review" else "Pengingat"
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
                                onOpenPenilaian = onPenilaianKinerja,
                                onOpenReviewBawahan = onPenilaianBawahan
                            )
                        }
                        if (canReviewSubordinates) {
                            item {
                                MinimalReviewerStatusCard(
                                    title = "Review Penilaian Bawahan",
                                    value = "${assessmentSummary?.pendingSubordinateAssessments ?: 0}",
                                    subtitle = "Bawahan belum final pada periode aktif",
                                    actionLabel = "Buka Review",
                                    onAction = onPenilaianBawahan
                                )
                            }
                        }
                    }
                }

                else -> {
                    val listState = remember(page) { androidx.compose.foundation.lazy.LazyListState() }
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            PerformanceSection(metrics = metrics)
                        }
                        if (timeSeries.isNotEmpty()) {
                            item {
                                TimeSeriesSection(timeSeries = timeSeries)
                            }
                        }
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
                    label = "$pendingSubordinates review bawahan",
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
        canReviewSubordinates && pendingSubordinates > 0 ->
            "$pendingSubordinates penilaian bawahan perlu perhatian hari ini"
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
        "Kinerja" -> "Ringkasan Kinerja"
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
        "Kinerja" -> listOf("Ringkasan Kinerja")
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

@Composable
private fun MinimalActionFocusSection(
    alerts: DashboardActionAlertsData?,
    targetSummary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?,
    canReviewSubordinates: Boolean,
    onOpenReports: () -> Unit,
    onOpenTarget: () -> Unit,
    onOpenReview: () -> Unit
) {
    val targetNeedAttention = alerts?.targetNeedAttentionCount ?: 0
    val realisasiNeedAttention = alerts?.realisasiNeedAttentionCount ?: 0
    val laporanPending = alerts?.laporanPendingCount ?: 0
    val subordinateReview = alerts?.subordinateReviewCount ?: 0
    val targetPeriodMonth = targetSummary?.activePeriodLabel.periodMonthOnly() ?: "bulan ini"
    val isAssessmentPeriodFinal = assessmentSummary?.activePeriodStatus
        ?.equals("Final", ignoreCase = true) == true
    val hasTargetAttention = !isAssessmentPeriodFinal &&
        (targetNeedAttention > 0 || realisasiNeedAttention > 0)

    val primaryText = when {
        canReviewSubordinates && subordinateReview > 0 -> "$subordinateReview penilaian bawahan perlu review"
        hasTargetAttention -> {
            val totalAttention = targetNeedAttention + realisasiNeedAttention
            if (totalAttention == 1) "1 target perlu realisasi" else "$totalAttention target perlu realisasi"
        }
        isAssessmentPeriodFinal -> "Periode target sudah final"
        laporanPending > 0 -> "$laporanPending laporan menunggu tindak lanjut"
        else -> "Tidak ada tindakan mendesak saat ini"
    }

    val secondaryText = when {
        canReviewSubordinates && subordinateReview > 0 -> "Penilaian belum final"
        hasTargetAttention -> "Buat laporan kegiatan untuk bulan $targetPeriodMonth."
        isAssessmentPeriodFinal -> "Target dan realisasi bulan $targetPeriodMonth hanya bisa dilihat."
        laporanPending > 0 -> "Buka laporan untuk detail"
        else -> "Semua ringkasan utama terlihat aman"
    }

    val actionLabel = when {
        canReviewSubordinates && subordinateReview > 0 -> "Buka Review"
        hasTargetAttention -> "Buka Target"
        isAssessmentPeriodFinal -> "Lihat Target"
        laporanPending > 0 -> "Buka Laporan"
        else -> "Lihat Target"
    }

    val actionHandler = when {
        canReviewSubordinates && subordinateReview > 0 -> onOpenReview
        hasTargetAttention -> onOpenTarget
        isAssessmentPeriodFinal -> onOpenTarget
        laporanPending > 0 -> onOpenReports
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
    onViewReports: () -> Unit,
    onTargetKinerja: () -> Unit,
    onPenilaianKinerja: () -> Unit,
    onTppSaya: () -> Unit,
    onTrailingAction: () -> Unit,
    trailingLabel: String
) {
    val actions = listOf(
        CompactQuickActionData("Laporan", Icons.AutoMirrored.Filled.List, SecondaryLight, onViewReports),
        CompactQuickActionData("Target", Icons.Default.TaskAlt, PrimaryLight, onTargetKinerja),
        CompactQuickActionData("Penilaian", Icons.Default.Bookmark, StatusApproved, onPenilaianKinerja),
        CompactQuickActionData("TPP", Icons.Default.PlaylistAddCheckCircle, StatusPending, onTppSaya),
        CompactQuickActionData(trailingLabel, if (trailingLabel == "Review") Icons.Default.Groups else Icons.Default.AlarmOn, TertiaryLight, onTrailingAction)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Akses Cepat",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp,
            cornerRadius = 24.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actions.forEach { action ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = action.onClick)
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
            }
        }
    }
}

@Composable
private fun MinimalSummarySection(
    metrics: MetricsData?,
    targetSummary: DashboardTargetSummaryData?,
    assessmentSummary: AssessmentSummaryData?,
    tppData: TppMeData?
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
    val tppValue = nominalTpp?.estimasiDiterima.formatDashboardCurrency()
    val tppPeriod = tppData?.periode?.let { monthYearLabel(it.tahun, it.bulan) }
    val currentPeriodLabel = targetSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: tppPeriod
        ?: assessmentSummary?.activePeriodLabel?.takeIf { it.isNotBlank() }
        ?: "Bulan ini"
    val tppFinalityLabel = when {
        nominalTpp == null -> "Belum dihitung"
        nominalTpp.isFinal -> nominalTpp.label?.takeIf { it.isNotBlank() } ?: "Final"
        else -> "Belum final"
    }
    val tppBadge = when {
        nominalTpp == null -> "BELUM"
        nominalTpp.isFinal -> "FINAL"
        else -> "DRAFT"
    }
    val tppStatusKey = nominalTpp?.status?.lowercase(Locale.ROOT).orEmpty()
    val tppToneColor = when {
        nominalTpp == null -> MaterialTheme.colorScheme.outline
        nominalTpp.isFinal || tppStatusKey in setOf("final", "final_opd", "dibayar", "arsip") -> StatusApproved
        tppStatusKey in setOf("revisi", "dikembalikan") -> StatusRevised
        tppStatusKey in setOf("ditolak", "batal") -> StatusRejected
        else -> StatusPending
    }
    val tppSubtitle = if (!tppPeriod.isNullOrBlank()) {
        "Perkiraan $tppPeriod - $tppFinalityLabel"
    } else {
        tppFinalityLabel
    }
    val scoreSubtitle = if (latestScorePeriod == "Belum ada periode final") {
        latestScorePeriod
    } else {
        "Final $latestScorePeriod"
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Ringkasan Bulan Ini",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
                    title = "TPP Saya",
                    value = tppValue,
                    modifier = Modifier.fillMaxWidth(),
                    subtitle = tppSubtitle,
                    badge = tppBadge,
                    toneColor = tppToneColor
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
    val onClick: () -> Unit
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
                title = if (canReviewSubordinates) "Review Bawahan" else "Review",
                value = if (canReviewSubordinates) subordinateReview.toString() else "0",
                subtitle = if (canReviewSubordinates) "Belum final periode aktif" else "Belum ada review bawahan",
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
        target.detailCount ?: target.details.size
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
                    text = "Ringkasan target dan realisasi pegawai",
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
    val itemCount = group.targets.sumOf { it.detailCount ?: it.details.size }
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
    val detailCount = item.detailCount ?: item.details.size
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
    onOpenPenilaian: () -> Unit,
    onOpenReviewBawahan: () -> Unit
) {
    val activeStatus = summary?.activePeriodStatus.displayOrDash()
    val activeLabel = summary?.activePeriodLabel.displayOrDash()
    val latestScore = formatNullableScore(summary?.latestFinalScore)
    val latestPredicate = summary?.latestPredicate.displayOrDash()
    val latestFinalLabel = summary?.latestFinalPeriodLabel.displayOrDash()
    val pendingSubordinates = summary?.pendingSubordinateAssessments ?: 0
    val canReviewSubordinates = summary?.canReviewSubordinates == true

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Ringkasan Penilaian",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 1.dp,
            cornerRadius = 24.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Periode aktif",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = activeLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    StatusChip(
                        text = activeStatus,
                        color = when (activeStatus.lowercase(Locale.getDefault())) {
                            "final" -> StatusApproved
                            "review" -> StatusPending
                            "draft" -> PrimaryLight
                            else -> StatusRevised
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatInsightCard(
                        title = "Nilai Akhir",
                        value = latestScore,
                        subtitle = "Final terakhir",
                        icon = Icons.Default.TaskAlt,
                        accent = StatusApproved,
                        modifier = Modifier.weight(1f)
                    )
                    StatInsightCard(
                        title = "Predikat",
                        value = latestPredicate,
                        subtitle = latestFinalLabel,
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Bawahan belum dinilai",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Periode aktif yang belum final penilaiannya",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = pendingSubordinates.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (pendingSubordinates > 0) StatusRejected else StatusApproved
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedCard(
                        modifier = Modifier.weight(1f),
                        onClick = onOpenPenilaian,
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
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = StatusApproved
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Buka Penilaian Saya",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    if (canReviewSubordinates) {
                        ElevatedCard(
                            modifier = Modifier.weight(1f),
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
                    }
                }
            }
        }
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
