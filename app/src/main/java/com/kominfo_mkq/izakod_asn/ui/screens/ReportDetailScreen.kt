package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.LaporanDetailViewModel
import com.kominfo_mkq.izakod_asn.data.model.LaporanDetail
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    laporanId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    onNavigateToVerify: ((String) -> Unit)? = null,
    viewModel: LaporanDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load data when screen opens
    LaunchedEffect(laporanId) {
        viewModel.loadLaporan(laporanId.toInt())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Detail Laporan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (!uiState.isLoading && !uiState.isError && uiState.laporan != null) {
                StickyActionButtons(
                    canEdit = uiState.canEdit,
                    canVerify = uiState.canVerify,
                    statusLaporan = uiState.laporan!!.statusLaporan,
                    onEdit = { onNavigateToEdit(laporanId) },
                    onVerify = { onNavigateToVerify?.invoke(laporanId) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.isError -> {
                    ErrorContent(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { viewModel.loadLaporan(laporanId.toInt()) }
                    )
                }
                uiState.laporan != null -> {
                    LaporanDetailContent(
                        laporan = uiState.laporan!!,
                        canEdit = uiState.canEdit,
                        canVerify = uiState.canVerify
                    )
                }
            }
        }
    }
}

@Composable
private fun StickyActionButtons(
    canEdit: Boolean,
    canVerify: Boolean,
    statusLaporan: String,
    onEdit: () -> Unit,
    onVerify: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ Verify Button (Primary - if can verify)
            if (canVerify && statusLaporan == "Diajukan") {
                Button(
                    onClick = onVerify,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Verifikasi Laporan",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // ✅ Edit Button (Secondary - if can edit)
            if (canEdit) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Laporan")
                }
            }

            // ✅ Info text if no actions available
            if (!canEdit && !canVerify) {
                Text(
                    "Tidak ada aksi yang tersedia",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LaporanDetailContent(
    laporan: LaporanDetail,
    canEdit: Boolean,
    canVerify: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        StatusCard(status = laporan.statusLaporan)

        // Basic Info Section
        BasicInfoSection(laporan = laporan)

        // Activity Details Section
        ActivityDetailsSection(laporan = laporan)

        // Time & Location Section
        TimeLocationSection(laporan = laporan)

        // Participants Section
        if (laporan.pesertaKegiatan != null || laporan.jumlahPeserta != null) {
            ParticipantsInfoSection(laporan = laporan)
        }

        // Target & Output Section
        if (laporan.targetOutput != null || laporan.hasilOutput != null) {
            TargetOutputSection(laporan = laporan)
        }

        // Problems & Solutions Section
        if (laporan.kendala != null || laporan.solusi != null) {
            ProblemsSolutionsSection(laporan = laporan)
        }

        // Verification Section (if verified)
        if (laporan.statusLaporan == "Diverifikasi" || laporan.catatanVerifikator != null) {
            VerificationSection(laporan = laporan)
        }

        // Links Section
        if (laporan.linkReferensi != null) {
            LinksSection(laporan = laporan)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Metadata Section
        MetadataSection(laporan = laporan)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatusCard(status: String) {
    val (bgColor, textColor, icon, label) = when (status) {
        "Draft" -> Tuple4(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Edit,
            "Draft"
        )
        "Diajukan", "Pending" -> Tuple4(
            StatusPending.copy(alpha = 0.15f),      // ✅ Amber background
            StatusPending,                           // ✅ Amber text
            Icons.Default.Schedule,
            "Menunggu Verifikasi"
        )
        "Diverifikasi", "Approved" -> Tuple4(
            StatusApproved.copy(alpha = 0.15f),     // ✅ Green background
            StatusApproved,                          // ✅ Green text
            Icons.Default.CheckCircle,
            "Diverifikasi"
        )
        "Ditolak", "Rejected" -> Tuple4(
            StatusRejected.copy(alpha = 0.15f),     // ✅ Red background
            StatusRejected,                          // ✅ Red text
            Icons.Default.Cancel,
            "Ditolak"
        )
        "Revisi", "Revised" -> Tuple4(              // ✅ Add Revisi status
            StatusRevised.copy(alpha = 0.15f),      // ✅ Orange background
            StatusRevised,                           // ✅ Orange text
            Icons.Default.Edit,
            "Perlu Revisi"
        )
        else -> Tuple4(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Info,
            status
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = textColor
            )
            Text(
                label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            )
        }
    }
}

@Composable
private fun BasicInfoSection(laporan: LaporanDetail) {
    SectionCard(title = "Informasi Dasar") {
        DetailRow(
            icon = Icons.Default.Person,
            label = "Pegawai",
            value = laporan.pegawaiNama ?: "-"
        )

        DetailRow(
            icon = Icons.Default.Category,
            label = "Kategori",
            value = laporan.kategoriNama ?: "-"
        )

        DetailRow(
            icon = Icons.Default.CalendarToday,
            label = "Tanggal Kegiatan",
            value = formatDate(laporan.tanggalKegiatan)
        )
    }
}

@Composable
private fun ActivityDetailsSection(laporan: LaporanDetail) {
    SectionCard(title = "Detail Kegiatan") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Nama Kegiatan
            Column {
                Text(
                    "Nama Kegiatan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    laporan.namaKegiatan,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Divider()

            // Deskripsi
            Column {
                Text(
                    "Deskripsi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    laporan.deskripsiKegiatan,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun TimeLocationSection(laporan: LaporanDetail) {
    SectionCard(title = "Waktu & Lokasi") {
        DetailRow(
            icon = Icons.Default.AccessTime,
            label = "Waktu Mulai",
            value = laporan.waktuMulai
        )

        DetailRow(
            icon = Icons.Default.AccessTime,
            label = "Waktu Selesai",
            value = laporan.waktuSelesai
        )

        if (laporan.durasiMenit != null) {
            DetailRow(
                icon = Icons.Default.Timer,
                label = "Durasi",
                value = "${laporan.durasiMenit} menit"
            )
        }

        if (laporan.lokasiKegiatan != null) {
            Divider()

            DetailRow(
                icon = Icons.Default.Place,
                label = "Lokasi",
                value = laporan.lokasiKegiatan
            )
        }

        if (laporan.latitude != null && laporan.longitude != null) {
            DetailRow(
                icon = Icons.Default.MyLocation,
                label = "Koordinat",
                value = "${laporan.latitude}, ${laporan.longitude}"
            )
        }
    }
}

@Composable
private fun ParticipantsInfoSection(laporan: LaporanDetail) {
    SectionCard(title = "Peserta") {
        if (laporan.pesertaKegiatan != null) {
            DetailRow(
                icon = Icons.Default.People,
                label = "Nama Peserta",
                value = laporan.pesertaKegiatan
            )
        }

        if (laporan.jumlahPeserta != null) {
            DetailRow(
                icon = Icons.Default.Tag,
                label = "Jumlah Peserta",
                value = "${laporan.jumlahPeserta} orang"
            )
        }
    }
}

@Composable
private fun TargetOutputSection(laporan: LaporanDetail) {
    SectionCard(title = "Target & Hasil") {
        if (laporan.targetOutput != null) {
            Column {
                Text(
                    "Target Output",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    laporan.targetOutput,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (laporan.hasilOutput != null) {
            if (laporan.targetOutput != null) Divider()

            Column {
                Text(
                    "Hasil Output",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    laporan.hasilOutput,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ProblemsSolutionsSection(laporan: LaporanDetail) {
    SectionCard(title = "Kendala & Solusi") {
        if (laporan.kendala != null) {
            Column {
                Text(
                    "Kendala",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    laporan.kendala,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (laporan.solusi != null) {
            if (laporan.kendala != null) Divider()

            Column {
                Text(
                    "Solusi",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    laporan.solusi,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun VerificationSection(laporan: LaporanDetail) {
    SectionCard(
        title = "Verifikasi",
        icon = Icons.Default.VerifiedUser
    ) {
        if (laporan.rating != null) {
            DetailRow(
                icon = Icons.Default.Star,
                label = "Rating",
                value = "${laporan.rating}/5"
            )
        }

        if (laporan.catatanVerifikator != null) {
            if (laporan.rating != null) Divider()

            Column {
                Text(
                    "Catatan Verifikator",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    laporan.catatanVerifikator,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun LinksSection(laporan: LaporanDetail) {
    SectionCard(title = "Link Referensi") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                laporan.linkReferensi ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun MetadataSection(laporan: LaporanDetail) {
    SectionCard(title = "Informasi Tambahan") {
        DetailRow(
            icon = Icons.Default.CalendarMonth,
            label = "Dibuat",
            value = formatDateTime(laporan.createdAt)
        )

        if (laporan.updatedAt != null) {
            DetailRow(
                icon = Icons.Default.Update,
                label = "Diperbarui",
                value = formatDateTime(laporan.updatedAt)
            )
        }
    }
}

// Helper components
@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Info,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            content()
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DetailLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Memuat detail laporan...")
        }
    }
}

@Composable
private fun DetailErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                "Terjadi Kesalahan",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

// Helper functions
private fun formatDate(dateString: String): String {
    return try {
        // ✅ FIX: Extract date part only, ignore timezone
        val datePart = dateString.split("T")[0] // "2025-12-29T00:00:00.000Z" -> "2025-12-29"

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

        val date = inputFormat.parse(datePart)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        // If parsing fails, just show the string
        dateString.split("T")[0] // At least show date part
    }
}

private fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val date = inputFormat.parse(dateTimeString)
        date?.let { outputFormat.format(it) } ?: dateTimeString
    } catch (e: Exception) {
        dateTimeString
    }
}

// Helper data class for status tuple
private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)