package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kominfo_mkq.izakod_asn.data.model.LampiranKegiatan
import com.kominfo_mkq.izakod_asn.data.model.LaporanDetail
import com.kominfo_mkq.izakod_asn.data.remote.ApiClient
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.viewmodel.LaporanDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
            IZAKODHeaderBar(
                title = "Detail Laporan",
                compact = true,
                onBack = onNavigateBack
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
        StatusHeroCard(laporan = laporan)

        MainReportSection(laporan = laporan)

        QuickFactsSection(laporan = laporan)

        // Target & Output Section
        if (laporan.targetOutput != null || laporan.hasilOutput != null) {
            TargetOutputSection(laporan = laporan)
        }

        // Problems & Solutions Section
        if (laporan.kendala != null || laporan.solusi != null) {
            ProblemsSolutionsSection(laporan = laporan)
        }

        if (laporan.lampiran.isNotEmpty()) {
            LampiranSection(laporan = laporan)
        }

        // Verification Section
        if (
            laporan.statusLaporan == "Diverifikasi" ||
            laporan.verifikatorNama != null ||
            laporan.tanggalVerifikasi != null ||
            laporan.catatanVerifikator != null
        ) {
            VerificationSection(laporan = laporan)
        }

        // Links Section
        if (laporan.linkReferensi != null) {
            LinksSection(laporan = laporan)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Metadata Section
        MetadataSection(laporan = laporan)

        Spacer(modifier = Modifier.height(if (canEdit || canVerify) 32.dp else 16.dp))
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

private data class StatusPresentation(
    val background: Color,
    val foreground: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String
)

@Composable
private fun statusPresentation(status: String): StatusPresentation {
    return when (status) {
        "Draft" -> StatusPresentation(
            background = MaterialTheme.colorScheme.surfaceVariant,
            foreground = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Default.Edit,
            title = "Draft",
            subtitle = "Laporan masih bisa dilengkapi sebelum diajukan"
        )
        "Diajukan", "Pending" -> StatusPresentation(
            background = StatusPending.copy(alpha = 0.14f),
            foreground = StatusPending,
            icon = Icons.Default.Schedule,
            title = "Menunggu Verifikasi",
            subtitle = "Laporan sudah diajukan dan menunggu penilaian atasan"
        )
        "Diverifikasi", "Approved" -> StatusPresentation(
            background = StatusApproved.copy(alpha = 0.14f),
            foreground = StatusApproved,
            icon = Icons.Default.CheckCircle,
            title = "Diverifikasi",
            subtitle = "Laporan telah diverifikasi dan tidak memerlukan tindakan lanjutan"
        )
        "Ditolak", "Rejected" -> StatusPresentation(
            background = StatusRejected.copy(alpha = 0.14f),
            foreground = StatusRejected,
            icon = Icons.Default.Cancel,
            title = "Ditolak",
            subtitle = "Laporan ditolak dan perlu ditinjau ulang sebelum diajukan kembali"
        )
        "Revisi", "Revised" -> StatusPresentation(
            background = StatusRevised.copy(alpha = 0.14f),
            foreground = StatusRevised,
            icon = Icons.Default.Edit,
            title = "Perlu Revisi",
            subtitle = "Ada catatan atasan yang perlu diperbaiki pada laporan ini"
        )
        else -> StatusPresentation(
            background = MaterialTheme.colorScheme.surfaceVariant,
            foreground = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Default.Info,
            title = status,
            subtitle = "Status laporan saat ini"
        )
    }
}

@Composable
private fun StatusHeroCard(laporan: LaporanDetail) {
    val statusUi = statusPresentation(laporan.statusLaporan)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusUi.background)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = statusUi.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = statusUi.foreground
                )
                Text(
                    text = statusUi.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = statusUi.foreground
                )
            }

            Text(
                text = statusUi.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetaPill(
                    icon = Icons.Default.CalendarToday,
                    text = formatDate(laporan.tanggalKegiatan),
                    modifier = Modifier.weight(1f)
                )
                MetaPill(
                    icon = Icons.Default.AccessTime,
                    text = "${formatTimeShort(laporan.waktuMulai)} - ${formatTimeShort(laporan.waktuSelesai)}",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                laporan.durasiMenit?.let {
                    MetaPill(
                        icon = Icons.Default.Timer,
                        text = "$it menit",
                        modifier = Modifier.weight(1f)
                    )
                }
                MetaPill(
                    icon = Icons.Default.Category,
                    text = laporan.kategoriNama ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MainReportSection(laporan: LaporanDetail) {
    SectionCard(title = "Inti Laporan", icon = Icons.Default.Description) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(
                    "Nama Kegiatan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    laporan.namaKegiatan,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Divider()

            Column {
                Text(
                    "Deskripsi Kegiatan",
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
private fun QuickFactsSection(laporan: LaporanDetail) {
    SectionCard(title = "Informasi Kerja", icon = Icons.Default.Info) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FactTile(
                    icon = Icons.Default.Person,
                    label = "Pegawai",
                    value = laporan.pegawaiNama ?: "-",
                    modifier = Modifier.weight(1f)
                )
                FactTile(
                    icon = Icons.Default.CalendarMonth,
                    label = "NIP",
                    value = laporan.pegawaiNip ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FactTile(
                    icon = Icons.Default.Place,
                    label = "Lokasi",
                    value = laporan.lokasiKegiatan ?: "-",
                    modifier = Modifier.weight(1f)
                )
                FactTile(
                    icon = Icons.Default.People,
                    label = "Peserta",
                    value = laporan.jumlahPeserta?.let { "$it orang" } ?: (laporan.pesertaKegiatan ?: "-"),
                    modifier = Modifier.weight(1f)
                )
            }

            if (!laporan.skpd.isNullOrBlank()) {
                FactTile(
                    icon = Icons.Default.People,
                    label = "Unit Kerja",
                    value = laporan.skpd,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (laporan.latitude != null && laporan.longitude != null) {
                FactTile(
                    icon = Icons.Default.MyLocation,
                    label = "Koordinat",
                    value = "${laporan.latitude}, ${laporan.longitude}",
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
private fun LampiranSection(laporan: LaporanDetail) {
    SectionCard(title = "Foto & Lampiran", icon = Icons.Default.Description) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(laporan.lampiran.size) { index ->
                LampiranCard(lampiran = laporan.lampiran[index])
            }
        }
    }
}

@Composable
private fun LampiranCard(lampiran: LampiranKegiatan) {
    val relativePath = lampiran.pathFile?.trim().orEmpty()
    val fileUrl = buildLampiranUrl(relativePath)
    val isImage = lampiran.tipeFile?.lowercase(Locale.getDefault())?.startsWith("image/") == true

    Surface(
        modifier = Modifier.width(188.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isImage && fileUrl != null) {
                AsyncImage(
                    model = fileUrl,
                    contentDescription = lampiran.namaFileAsli ?: "Lampiran laporan",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lampiran.tipeFile?.uppercase(Locale.getDefault()) ?: "FILE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = lampiran.namaFileAsli ?: "Lampiran kegiatan",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2
            )

            Text(
                text = lampiran.createdAt?.let { formatDateTime(it) } ?: "Waktu upload tidak tersedia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VerificationSection(laporan: LaporanDetail) {
    SectionCard(
        title = "Verifikasi",
        icon = Icons.Default.VerifiedUser
    ) {
        DetailRow(
            icon = Icons.Default.Person,
            label = "Diverifikasi oleh",
            value = laporan.verifikatorNama?.takeIf { it.isNotBlank() } ?: "-"
        )

        Divider()

        DetailRow(
            icon = Icons.Default.EventAvailable,
            label = "Tanggal Verifikasi",
            value = laporan.tanggalVerifikasi?.let { formatDateTime(it) } ?: "-"
        )

        if (laporan.rating != null) {
            Divider()

            DetailRow(
                icon = Icons.Default.Star,
                label = "Rating",
                value = "${formatRating(laporan.rating)}/5"
            )
        }

        Divider()

        Column {
            Text(
                "Catatan Verifikasi",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                laporan.catatanVerifikator?.takeIf { it.isNotBlank() } ?: "-",
                style = MaterialTheme.typography.bodyMedium
            )
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
private fun MetaPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            maxLines = 1
        )
    }
}

@Composable
private fun FactTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
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

private fun formatRating(rating: Double): String {
    val roundedWhole = rating.toInt().toDouble()
    return if (rating == roundedWhole) {
        roundedWhole.toInt().toString()
    } else {
        rating.toString()
    }
}

private fun formatTimeShort(timeString: String): String {
    return timeString.take(5)
}

private fun buildLampiranUrl(path: String): String? {
    if (path.isBlank()) return null
    if (path.startsWith("http://") || path.startsWith("https://")) return path

    return "${ApiClient.BASE_URL.trimEnd('/')}/${path.trimStart('/')}"
}

// Helper data class for status tuple
private data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
