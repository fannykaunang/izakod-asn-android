package com.kominfo_mkq.izakod_asn.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.repository.StatistikRepository
import com.kominfo_mkq.izakod_asn.ui.components.*
import com.kominfo_mkq.izakod_asn.ui.theme.*
import com.kominfo_mkq.izakod_asn.ui.viewmodel.DashboardViewModel
import com.kominfo_mkq.izakod_asn.R
import com.kominfo_mkq.izakod_asn.data.model.PegawaiProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun DashboardScreen(
    onNavigateToCreateReport: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onNavigateToReminder: () -> Unit,
    onNavigateToProfile: () -> Unit,
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
            android.util.Log.d("DashboardScreen", "📱 PIN ditemukan: $pin. Memanggil profile...")
            viewModel.loadPegawaiProfile(pin)
        } else {
            android.util.Log.e("DashboardScreen", "❌ PIN TIDAK DITEMUKAN di UserPreferences")
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                scrollBehavior = scrollBehavior,
                onNavigateToProfile= onNavigateToProfile,
                pegawaiProfile = uiState.pegawaiProfile,
                photoUrl = uiState.photoUrl,
                isLoadingProfile = uiState.isLoadingProfile
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    // Loading State
                    LoadingContent()
                }
                uiState.isError -> {
                    // Error State
                    ErrorContent(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { viewModel.retry() }
                    )
                }
                else -> {
                    // Success State
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Hero Section
                        item {
                            HeroSection(
                                totalKegiatan = uiState.metrics?.totalKegiatan?.toIntOrNull() ?: 0,
                                rataRataPerHari = uiState.metrics?.rataRataKegiatanPerHari?.toDoubleOrNull() ?: 0.0
                            )
                        }

                        // Quick Actions
                        item {
                            QuickActionsSection(
                                onCreateReport = onNavigateToCreateReport,
                                onViewReports = onNavigateToReports,
                                onTemplates = onNavigateToTemplates,
                                onReminder = onNavigateToReminder
                            )
                        }

                        // Statistics Overview
                        item {
                            StatisticsSection(metrics = uiState.metrics)
                        }

                        // Time Series Chart (if available)
                        if (uiState.timeSeries.isNotEmpty()) {
                            item {
                                TimeSeriesSection(timeSeries = uiState.timeSeries)
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
    onNavigateToProfile: () -> Unit,
    pegawaiProfile: PegawaiProfile?,
    photoUrl: String?,
    isLoadingProfile: Boolean
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "IZAKOD-ASN",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                )
//                Text(
//                    text = "Dashboard",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                )
                pegawaiProfile?.let { profile ->
                    Text(
                        profile.pegawaiNama,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { /* Notifikasi */ }) {
                Badge(containerColor = StatusRejected) {
                    Text("3")
                }
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifikasi"
                )
            }
            ProfilePhotoButton(
                photoUrl = photoUrl,
                isLoading = isLoadingProfile,
                onClick = onNavigateToProfile
            )
//            IconButton(onClick = { showLogoutDialog = true }) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
//                    contentDescription = "Logout"
//                )
//            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

/**
 * ✅ Profile Photo Button with AsyncImage
 */
@Composable
private fun ProfilePhotoButton(
    photoUrl: String?,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        if (isLoading) {
            // ✅ Show loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 2.dp
            )
        } else if (photoUrl != null) {
            // ✅ Show photo from URL
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_foreground)  // Optional placeholder
                    .error(R.drawable.ic_launcher_foreground)  // Fallback if error
                    .build(),
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        } else {
            // ✅ Fallback to default icon
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
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
                text = "Memuat data...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = ErrorLight
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
fun HeroSection(
    totalKegiatan: Int,
    rataRataPerHari: Double
) {
    GradientCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp),
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Selamat Datang! 👋",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ayo catat kegiatanmu hari ini",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Display Stats
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalKegiatan",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = "Total Kegiatan",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rata-rata per hari
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Rata-rata per hari",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
            Text(
                text = String.format("%.2f", rataRataPerHari),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun QuickActionsSection(
    onCreateReport: () -> Unit,
    onViewReports: () -> Unit,
    onTemplates: () -> Unit,
    onReminder: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = "Buat Laporan",
                icon = Icons.Default.Add,
                color = PrimaryLight,
                modifier = Modifier.weight(1f),
                onClick = onCreateReport
            )
            QuickActionButton(
                title = "Lihat Laporan",
                icon = Icons.Default.List,
                color = SecondaryLight,
                modifier = Modifier.weight(1f),
                onClick = onViewReports
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTemplates() },
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                title = "Template",
                icon = Icons.Default.Bookmark,
                color = TertiaryLight,
                modifier = Modifier.weight(1f),
                onClick = onTemplates
            )
            QuickActionButton(
                title = "Reminder",
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
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onClick,
        elevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StatisticsSection(metrics: com.kominfo_mkq.izakod_asn.data.model.MetricsData?) {
    if (metrics == null) return

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Statistik Laporan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Diverifikasi",
                value = metrics.totalDiverifikasi,
                color = StatusApproved,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Pending",
                value = metrics.totalPending,
                color = StatusPending,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Ditolak",
                value = metrics.totalDitolak,
                color = StatusRejected,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Perlu Revisi",
                value = metrics.totalRevisi,
                color = StatusRevised,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Total Durasi Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Durasi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val totalMenit = metrics.totalDurasiMenit.toIntOrNull() ?: 0
                    val jam = totalMenit / 60
                    val menit = totalMenit % 60
                    Text(
                        text = "${jam}j ${menit}m",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = SecondaryLight
                    )
                }
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = SecondaryLight.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
        }
    }
}

@Composable
fun TimeSeriesSection(timeSeries: List<com.kominfo_mkq.izakod_asn.data.model.TimeSeriesItem>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Tren 6 Bulan Terakhir",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        timeSeries.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.bulanNama,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${item.totalKegiatan} kegiatan",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = PrimaryLight
                )
            }
        }
    }
}