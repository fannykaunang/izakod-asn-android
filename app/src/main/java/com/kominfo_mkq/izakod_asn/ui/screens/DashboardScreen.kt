package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToReports: () -> Unit,
    onNavigateToTargetKinerja: () -> Unit,
    onNavigateToTargetDetail: (Int, Boolean) -> Unit,
    onNavigateToPenilaianKinerja: () -> Unit,
    onNavigateToPenilaianBawahan: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToReminder: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
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
                        currentPegawaiId = uiState.currentPegawaiId ?: uiState.pegawaiProfile?.pegawaiId,
                        pegawaiProfile = uiState.pegawaiProfile,
                        photoUrl = uiState.photoUrl,
                        unreadNotificationCount = uiState.unreadNotificationCount,
                        isDarkTheme = isDarkTheme,
                        onViewReports = onNavigateToReports,
                        onTargetKinerja = onNavigateToTargetKinerja,
                        onTargetDetail = onNavigateToTargetDetail,
                        onPenilaianKinerja = onNavigateToPenilaianKinerja,
                        onPenilaianBawahan = onNavigateToPenilaianBawahan,
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
    currentPegawaiId: Int?,
    pegawaiProfile: PegawaiProfile?,
    photoUrl: String?,
    unreadNotificationCount: Int,
    isDarkTheme: Boolean,
    onViewReports: () -> Unit,
    onTargetKinerja: () -> Unit,
    onTargetDetail: (Int, Boolean) -> Unit,
    onPenilaianKinerja: () -> Unit,
    onPenilaianBawahan: () -> Unit,
    onTemplates: () -> Unit,
    onReminder: () -> Unit,
    viewModel: DashboardViewModel
) {
    val tabs = listOf("Overview", "Target", "Penilaian", "Kinerja")

    // 1. Ambil index dari ViewModel
    val selectedTabIndex by viewModel.currentTabIndex.collectAsState()

    val canReviewSubordinates = assessmentSummary?.canReviewSubordinates == true

    // 2. Inisialisasi PagerState
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { tabs.size }
    )
    val scope = rememberCoroutineScope()

    // 3. Sinkronisasi: Jika swipe manual, lapor ke ViewModel
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != selectedTabIndex) {
            viewModel.updateTabIndex(pagerState.currentPage)
        }
    }

    // 4. Sinkronisasi: Jika tab diklik atau kembali dari detail, paksa pager pindah
    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.scrollToPage(selectedTabIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        // Update di ViewModel dulu
                        viewModel.updateTabIndex(index)
                        // Baru jalankan animasi
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(text = title) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
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
                                canReviewSubordinates = canReviewSubordinates,
                                onOpenReports = onViewReports,
                                onOpenTarget = onTargetKinerja,
                                onOpenPenilaian = onPenilaianKinerja,
                                onOpenReview = onPenilaianBawahan
                            )
                        }
                        item {
                            MinimalQuickActionsSection(
                                onViewReports = onViewReports,
                                onTargetKinerja = onTargetKinerja,
                                onPenilaianKinerja = onPenilaianKinerja,
                                onTrailingAction = if (canReviewSubordinates) onPenilaianBawahan else onReminder,
                                trailingLabel = if (canReviewSubordinates) "Review" else "Reminder"
                            )
                        }
                        item {
                            MinimalSummarySection(
                                metrics = metrics,
                                targetSummary = targetSummary,
                                assessmentSummary = assessmentSummary
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
                                targetItems = targetItems,
                                currentPegawaiId = currentPegawaiId,
                                canReviewSubordinates = canReviewSubordinates,
                                onOpenTargetDetail = onTargetDetail
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
    val heroStatusItems = when (currentTab) {
        "Target" -> listOf("Target $targetStatus")
        "Penilaian" -> listOf("Penilaian $assessmentStatus")
        "Kinerja" -> listOf("Ringkasan Kinerja")
        else -> listOf("Target $targetStatus", "Penilaian $assessmentStatus")
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
                        HeroPlayStatusChipV2(text = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPlayStatusChipV2(text: String) {
    Surface(
        shape = RoundedCornerShape(100),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MinimalActionFocusSection(
    alerts: DashboardActionAlertsData?,
    canReviewSubordinates: Boolean,
    onOpenReports: () -> Unit,
    onOpenTarget: () -> Unit,
    onOpenPenilaian: () -> Unit,
    onOpenReview: () -> Unit
) {
    val targetNeedAttention = alerts?.targetNeedAttentionCount ?: 0
    val realisasiNeedAttention = alerts?.realisasiNeedAttentionCount ?: 0
    val laporanPending = alerts?.laporanPendingCount ?: 0
    val subordinateReview = alerts?.subordinateReviewCount ?: 0

    val primaryText = when {
        canReviewSubordinates && subordinateReview > 0 -> "$subordinateReview penilaian bawahan perlu review"
        targetNeedAttention > 0 || realisasiNeedAttention > 0 -> "${targetNeedAttention + realisasiNeedAttention} item target dan realisasi perlu perhatian"
        laporanPending > 0 -> "$laporanPending laporan menunggu tindak lanjut"
        else -> "Tidak ada tindakan mendesak saat ini"
    }

    val secondaryText = when {
        canReviewSubordinates && subordinateReview > 0 -> "Penilaian belum final"
        realisasiNeedAttention > 0 -> "$realisasiNeedAttention realisasi belum lengkap"
        targetNeedAttention > 0 -> "$targetNeedAttention target perlu dibereskan"
        laporanPending > 0 -> "Buka laporan untuk detail"
        else -> "Semua ringkasan utama terlihat aman"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 24.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Perlu Tindakan",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = primaryText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MinimalActionButton(
                    label = if (canReviewSubordinates && subordinateReview > 0) "Review" else "Target",
                    icon = if (canReviewSubordinates && subordinateReview > 0) Icons.Default.Groups else Icons.Default.TaskAlt,
                    modifier = Modifier.weight(1f),
                    onClick = if (canReviewSubordinates && subordinateReview > 0) onOpenReview else onOpenTarget
                )
                MinimalActionButton(
                    label = if (laporanPending > 0) "Laporan" else "Penilaian",
                    icon = if (laporanPending > 0) Icons.Default.AssignmentTurnedIn else Icons.Default.Bookmark,
                    modifier = Modifier.weight(1f),
                    onClick = if (laporanPending > 0) onOpenReports else onOpenPenilaian
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
    onTrailingAction: () -> Unit,
    trailingLabel: String
) {
    val actions = listOf(
        CompactQuickActionData("Laporan", Icons.AutoMirrored.Filled.List, SecondaryLight, onViewReports),
        CompactQuickActionData("Target", Icons.Default.TaskAlt, PrimaryLight, onTargetKinerja),
        CompactQuickActionData("Penilaian", Icons.Default.Bookmark, StatusApproved, onPenilaianKinerja),
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
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            maxLines = 2,
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
    assessmentSummary: AssessmentSummaryData?
) {
    val totalKegiatan = metrics?.totalKegiatan.toIntSafe()
    val targetStatus = targetSummary?.status.displayOrDash()
    val progressValue = "${(targetSummary?.persentaseProgress ?: 0.0).toInt()}%"
    val score = formatNullableScore(assessmentSummary?.latestFinalScore)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Ringkasan Singkat",
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
                    MinimalSummaryCard("Kegiatan", totalKegiatan.toString(), Modifier.weight(1f))
                    MinimalSummaryCard("Status Target", targetStatus, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MinimalSummaryCard("Progress", progressValue, Modifier.weight(1f))
                    MinimalSummaryCard("Nilai", if (score != "-") score else assessmentSummary?.activePeriodStatus.displayOrDash(), Modifier.weight(1f))
                }
            }
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
    modifier: Modifier = Modifier
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
    targetItems: List<TargetKinerjaItem>,
    currentPegawaiId: Int?,
    canReviewSubordinates: Boolean,
    onOpenTargetDetail: (Int, Boolean) -> Unit
) {
    val status = summary?.status.displayOrDash()
    val periodLabel = summary?.activePeriodLabel.displayOrDash()
    val totalTargets = summary?.totalTargets ?: 0
    val totalItems = summary?.totalItems ?: 0
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Status Target & Realisasi",
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
                            text = "Periode Aktif",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = periodLabel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    StatusChip(
                        text = status,
                        color = when (status.lowercase(Locale.getDefault())) {
                            "final", "disetujui" -> StatusApproved
                            "diajukan" -> StatusPending
                            "draft" -> PrimaryLight
                            "revisi" -> StatusRevised
                            else -> SecondaryLight
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatInsightCard(
                        title = "Target",
                        value = totalTargets.toString(),
                        subtitle = "Periode aktif",
                        icon = Icons.Default.TaskAlt,
                        accent = PrimaryLight,
                        modifier = Modifier.weight(1f)
                    )
                    StatInsightCard(
                        title = "Laporan",
                        value = totalLaporanTertaut.toString(),
                        subtitle = "Sudah tertaut",
                        icon = Icons.Default.AssignmentTurnedIn,
                        accent = SecondaryLight,
                        modifier = Modifier.weight(1f)
                    )
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = 0.dp,
                    cornerRadius = 20.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progress Realisasi",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${progress.toInt()}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (progress >= 100.0) StatusApproved else PrimaryLight
                            )
                        }

                        LinearProgressIndicator(
                            progress = { (progress / 100.0).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(100)),
                            color = if (progress >= 100.0) StatusApproved else PrimaryLight,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatusChip(
                                text = "$itemSudahRealisasi/$totalItems item terisi",
                                color = StatusApproved,
                                modifier = Modifier.weight(1f)
                            )
                            StatusChip(
                                text = "$itemBelumRealisasi item belum selesai",
                                color = if (itemBelumRealisasi > 0) StatusPending else SecondaryLight,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatInsightCard(
                title = "Target Saya",
                value = myTargets.size.toString(),
                subtitle = "Periode aktif",
                icon = Icons.Default.TaskAlt,
                accent = PrimaryLight,
                modifier = Modifier.weight(1f)
            )
            if (canReviewSubordinates) {
                StatInsightCard(
                    title = "Bawahan",
                    value = subordinateTargets.size.toString(),
                    subtitle = "Perlu dipantau",
                    icon = Icons.Default.Groups,
                    accent = StatusPending,
                    modifier = Modifier.weight(1f)
                )
            } else {
                StatInsightCard(
                    title = "Item Target",
                    value = totalItems.toString(),
                    subtitle = "Seluruh detail",
                    icon = Icons.Default.Checklist,
                    accent = SecondaryLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        TargetListSection(
            title = "Target Saya",
            items = myTargets,
            emptyLabel = "Belum ada target saya pada periode aktif",
            currentPegawaiId = currentPegawaiId,
            onOpenTargetDetail = onOpenTargetDetail
        )

        if (canReviewSubordinates) {
            TargetListSection(
                title = "Target Bawahan",
                items = subordinateTargets,
                emptyLabel = "Belum ada target bawahan pada periode aktif",
                currentPegawaiId = currentPegawaiId,
                onOpenTargetDetail = onOpenTargetDetail
            )
        }
    }
}

@Composable
private fun TargetListSection(
    title: String,
    items: List<TargetKinerjaItem>,
    emptyLabel: String,
    currentPegawaiId: Int?,
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

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 0.dp,
        cornerRadius = 18.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.pegawaiNama ?: "Pegawai",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = periodeLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(
                    text = item.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() },
                    color = statusColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusChip(
                    text = "${item.detailCount ?: 0} item target",
                    color = PrimaryLight,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    text = if (reviewMode) "Buka detail review" else item.approverNama?.takeIf { it.isNotBlank() } ?: "Belum direview",
                    color = if (reviewMode) StatusPending else SecondaryLight,
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
