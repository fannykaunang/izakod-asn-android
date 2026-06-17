package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
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
                subtitle = "Laporan kegiatan harian",
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
                    DetailLoadingContent()
                }
                uiState.isError -> {
                    DetailErrorContent(
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
    val normalizedStatus = statusLaporan.normalizedReportStatus()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (canVerify && normalizedStatus == "diajukan") {
                Button(
                    onClick = onVerify,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusApproved
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(statusLaporan.editActionLabel())
                }
            }

            // ✅ Info text if no actions available
            if (!canEdit && !canVerify) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            statusLaporan.noActionMessage(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
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
    val showSupervisorActionNote = laporan.shouldShowSupervisorActionNote()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatusHeroCard(laporan = laporan)

        if (showSupervisorActionNote) {
            SupervisorActionNoteSection(laporan = laporan)
        }

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
            laporan.statusLaporan.normalizedReportStatus() == "disetujui" ||
            laporan.verifikatorNama != null ||
            laporan.tanggalVerifikasi != null ||
            laporan.rating != null ||
            (!showSupervisorActionNote && laporan.catatanVerifikator != null)
        ) {
            VerificationSection(
                laporan = laporan,
                showNote = !showSupervisorActionNote
            )
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

private data class StatusPresentation(
    val background: Color,
    val foreground: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val actionTitle: String,
    val actionText: String
)

@Composable
private fun statusPresentation(status: String): StatusPresentation {
    return when (status.normalizedReportStatus()) {
        "draft" -> StatusPresentation(
            background = MaterialTheme.colorScheme.surfaceVariant,
            foreground = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Default.Edit,
            title = "Draft",
            subtitle = "Laporan masih draft dan belum dikirim ke atasan.",
            actionTitle = "Belum dikirim ke atasan",
            actionText = "Lengkapi data laporan, lalu ajukan agar bisa diverifikasi."
        )
        "diajukan" -> StatusPresentation(
            background = StatusPending.copy(alpha = 0.14f),
            foreground = StatusPending,
            icon = Icons.Default.Schedule,
            title = "Menunggu Verifikasi",
            subtitle = "Laporan sudah dikirim dan sedang menunggu atasan.",
            actionTitle = "Menunggu atasan",
            actionText = "Tidak ada yang perlu diubah sekarang. Tunggu verifikasi dari atasan."
        )
        "disetujui" -> StatusPresentation(
            background = StatusApproved.copy(alpha = 0.14f),
            foreground = StatusApproved,
            icon = Icons.Default.CheckCircle,
            title = "Disetujui",
            subtitle = "Laporan sudah disetujui oleh atasan.",
            actionTitle = "Sudah selesai",
            actionText = "Tidak ada aksi lanjutan. Laporan ini sudah aman."
        )
        "ditolak" -> StatusPresentation(
            background = StatusRejected.copy(alpha = 0.14f),
            foreground = StatusRejected,
            icon = Icons.Default.Cancel,
            title = "Ditolak",
            subtitle = "Laporan ditolak. Baca catatan atasan sebelum membuat tindak lanjut.",
            actionTitle = "Perlu tindak lanjut",
            actionText = "Buka catatan atasan, pahami alasan penolakan, lalu perbaiki jika tersedia tombol edit."
        )
        "revisi" -> StatusPresentation(
            background = StatusRevised.copy(alpha = 0.14f),
            foreground = StatusRevised,
            icon = Icons.Default.Edit,
            title = "Perlu Revisi",
            subtitle = "Atasan meminta perbaikan pada laporan ini.",
            actionTitle = "Perlu diperbaiki",
            actionText = "Baca catatan atasan, perbaiki laporan, lalu ajukan kembali."
        )
        else -> StatusPresentation(
            background = MaterialTheme.colorScheme.surfaceVariant,
            foreground = MaterialTheme.colorScheme.onSurfaceVariant,
            icon = Icons.Default.Info,
            title = status,
            subtitle = "Status laporan saat ini.",
            actionTitle = "Periksa status",
            actionText = "Buka informasi laporan dan ikuti arahan yang tersedia."
        )
    }
}

@Composable
private fun StatusHeroCard(laporan: LaporanDetail) {
    val statusUi = statusPresentation(laporan.statusLaporan)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = statusUi.background
                    ) {
                        Icon(
                            imageVector = statusUi.icon,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp),
                            tint = statusUi.foreground
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Status Laporan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = statusUi.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = statusUi.foreground
                        )
                    }
                }

                StatusDot(color = statusUi.foreground)
            }

            Text(
                text = laporan.namaKegiatan,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = statusUi.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            StatusActionGuidance(
                presentation = statusUi
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
private fun StatusDot(color: Color) {
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(12.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun StatusActionGuidance(
    presentation: StatusPresentation
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = presentation.foreground.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(presentation.foreground.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = presentation.icon,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = presentation.foreground
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = presentation.actionTitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = presentation.foreground
                )
                Text(
                    text = presentation.actionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SupervisorActionNoteSection(laporan: LaporanDetail) {
    val statusUi = statusPresentation(laporan.statusLaporan)
    val note = laporan.catatanVerifikator?.takeIf { it.isNotBlank() } ?: return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = statusUi.foreground.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, statusUi.foreground.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(statusUi.foreground.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = statusUi.foreground
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = laporan.supervisorNoteTitle(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusUi.foreground
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MainReportSection(laporan: LaporanDetail) {
    SectionCard(title = "Uraian Kegiatan", icon = Icons.Default.Description) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ReportTextBlock(
                label = "Nama Kegiatan",
                value = laporan.namaKegiatan,
                emphasized = true
            )

            HorizontalDivider()

            ReportTextBlock(
                label = "Deskripsi Kegiatan",
                value = laporan.deskripsiKegiatan
            )

            if (!laporan.targetDetailUraian.isNullOrBlank()) {
                HorizontalDivider()
                ReportTextBlock(
                    label = "Terkait Target Kinerja",
                    value = laporan.targetDetailUraian
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
    SectionCard(title = "Target & Hasil", icon = Icons.Default.EventAvailable) {
        if (laporan.targetOutput != null) {
            ReportTextBlock(
                label = "Target Output",
                value = laporan.targetOutput
            )
        }

        if (laporan.hasilOutput != null) {
            if (laporan.targetOutput != null) HorizontalDivider()

            ReportTextBlock(
                label = "Hasil Output",
                value = laporan.hasilOutput
            )
        }
    }
}

@Composable
private fun ProblemsSolutionsSection(laporan: LaporanDetail) {
    SectionCard(title = "Kendala & Solusi", icon = Icons.Default.Info) {
        if (laporan.kendala != null) {
            ReportTextBlock(
                label = "Kendala",
                value = laporan.kendala
            )
        }

        if (laporan.solusi != null) {
            if (laporan.kendala != null) HorizontalDivider()

            ReportTextBlock(
                label = "Solusi",
                value = laporan.solusi
            )
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
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
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
private fun VerificationSection(
    laporan: LaporanDetail,
    showNote: Boolean = true
) {
    SectionCard(
        title = "Verifikasi",
        icon = Icons.Default.VerifiedUser
    ) {
        DetailRow(
            icon = Icons.Default.Person,
            label = "Diverifikasi oleh",
            value = laporan.verifikatorNama?.takeIf { it.isNotBlank() } ?: "-"
        )

        HorizontalDivider()

        DetailRow(
            icon = Icons.Default.EventAvailable,
            label = "Tanggal Verifikasi",
            value = laporan.tanggalVerifikasi?.let { formatDateTime(it) } ?: "-"
        )

        if (laporan.rating != null) {
            HorizontalDivider()

            DetailRow(
                icon = Icons.Default.Star,
                label = "Rating",
                value = "${formatRating(laporan.rating)}/5"
            )
        }

        if (showNote) {
            HorizontalDivider()

            Column {
                Text(
                    laporan.supervisorNoteTitle(),
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
}

@Composable
private fun LinksSection(laporan: LaporanDetail) {
    SectionCard(title = "Link Referensi", icon = Icons.Default.Link) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    laporan.linkReferensi ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(9.dp)
                            .size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            content()
        }
    }
}

@Composable
private fun ReportTextBlock(
    label: String,
    value: String,
    emphasized: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifBlank { "-" },
            style = if (emphasized) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onSurface
        )
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
        Surface(
            shape = RoundedCornerShape(13.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(3.dp))
            Text(
                value.ifBlank { "-" },
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
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
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
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
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DetailLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Memuat detail laporan...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Coba Lagi")
                }
            }
        }
    }
}

// Helper functions
private fun String.normalizedReportStatus(): String {
    return when (trim().lowercase(Locale.getDefault())) {
        "draft" -> "draft"
        "pending", "diajukan" -> "diajukan"
        "diverifikasi", "disetujui", "approved", "verified" -> "disetujui"
        "ditolak", "rejected" -> "ditolak"
        "revisi", "perlu revisi", "revised", "revision" -> "revisi"
        else -> trim().lowercase(Locale.getDefault())
    }
}

private fun String.editActionLabel(): String {
    return when (normalizedReportStatus()) {
        "draft" -> "Edit & Ajukan Laporan"
        "revisi" -> "Perbaiki Laporan"
        "ditolak" -> "Tinjau Laporan"
        else -> "Edit Laporan"
    }
}

private fun String.noActionMessage(): String {
    return when (normalizedReportStatus()) {
        "diajukan" -> "Menunggu verifikasi atasan."
        "disetujui" -> "Laporan sudah disetujui. Tidak ada aksi lanjutan."
        "draft" -> "Laporan draft belum bisa diedit saat ini."
        "revisi" -> "Laporan perlu revisi, tetapi aksi edit belum tersedia untuk akun ini."
        "ditolak" -> "Laporan ditolak. Baca catatan atasan pada detail laporan."
        else -> "Tidak ada aksi yang tersedia."
    }
}

private fun LaporanDetail.shouldShowSupervisorActionNote(): Boolean {
    val hasNote = !catatanVerifikator.isNullOrBlank()
    return hasNote && statusLaporan.normalizedReportStatus() in setOf("revisi", "ditolak")
}

private fun LaporanDetail.supervisorNoteTitle(): String {
    return when (statusLaporan.normalizedReportStatus()) {
        "ditolak" -> "Alasan penolakan dari atasan"
        "revisi" -> "Catatan revisi dari atasan"
        else -> "Catatan atasan"
    }
}

private fun formatDate(dateString: String): String {
    return try {
        // ✅ FIX: Extract date part only, ignore timezone
        val datePart = dateString.split("T")[0] // "2025-12-29T00:00:00.000Z" -> "2025-12-29"

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))

        val date = inputFormat.parse(datePart)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (_: Exception) {
        // If parsing fails, just show the string
        dateString.split("T")[0] // At least show date part
    }
}

private fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        val date = inputFormat.parse(dateTimeString)
        date?.let { outputFormat.format(it) } ?: dateTimeString
    } catch (_: Exception) {
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
