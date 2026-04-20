package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.kominfo_mkq.izakod_asn.data.model.MetricsData
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToReports: () -> Unit,
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
                        pegawaiProfile = uiState.pegawaiProfile,
                        photoUrl = uiState.photoUrl,
                        unreadNotificationCount = uiState.unreadNotificationCount,
                        onViewReports = onNavigateToReports,
                        onTemplates = onNavigateToTemplates,
                        onReminder = onNavigateToReminder
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
    pegawaiProfile: PegawaiProfile?,
    photoUrl: String?,
    unreadNotificationCount: Int,
    onViewReports: () -> Unit,
    onTemplates: () -> Unit,
    onReminder: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            HeroSection(
                pegawaiProfile = pegawaiProfile,
                photoUrl = photoUrl,
                metrics = metrics,
                unreadNotificationCount = unreadNotificationCount
            )
        }

        item {
            QuickActionsSection(
                onViewReports = onViewReports,
                onTemplates = onTemplates,
                onReminder = onReminder
            )
        }

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
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
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
    unreadNotificationCount: Int
) {
    val totalKegiatan = metrics?.totalKegiatan.toIntSafe()
    val totalPending = metrics?.totalPending.toIntSafe()
    val greeting = currentGreeting()
    val nama = pegawaiProfile?.pegawaiNama.displayOrDash()
    val jabatan = pegawaiProfile?.jabatan.displayOrDash()
    val skpd = pegawaiProfile?.skpd.displayOrDash()

    GradientCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Status kerja hari ini",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "$greeting,",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.88f)
                )
                Text(
                    text = nama,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
                Text(
                    text = "$jabatan | $skpd",
                    style = MaterialTheme.typography.bodyMedium,
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
                        .size(72.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeroStatPill(
                label = "Kegiatan bulan ini",
                value = totalKegiatan.toString(),
                modifier = Modifier.weight(1f)
            )
            HeroStatPill(
                label = "Menunggu Review",
                value = totalPending.toString(),
                modifier = Modifier.weight(1f)
            )
            HeroStatPill(
                label = "Notifikasi Baru",
                value = unreadNotificationCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

    }
}

@Composable
private fun HeroStatPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.78f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = Color.White
            )
        }
    }
}

@Composable
fun QuickActionsSection(
    onViewReports: () -> Unit,
    onTemplates: () -> Unit,
    onReminder: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = "Laporan",
                subtitle = "Pantau status",
                icon = Icons.AutoMirrored.Filled.List,
                color = SecondaryLight,
                modifier = Modifier.weight(1f),
                onClick = onViewReports
            )
            QuickActionButton(
                title = "Template",
                subtitle = "Mulai lebih cepat",
                icon = Icons.Default.Bookmark,
                color = TertiaryLight,
                modifier = Modifier.weight(1f),
                onClick = onTemplates
            )
            QuickActionButton(
                title = "Reminder",
                subtitle = "Kelola agenda",
                icon = Icons.Default.AlarmOn,
                color = StatusRevised,
                modifier = Modifier.weight(1f),
                onClick = onReminder
            )
        }
    }
}

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
