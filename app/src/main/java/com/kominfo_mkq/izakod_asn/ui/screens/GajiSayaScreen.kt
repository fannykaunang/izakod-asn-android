package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnMeData
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnPerhitungan
import com.kominfo_mkq.izakod_asn.data.model.GajiNonAsnRekap
import com.kominfo_mkq.izakod_asn.data.model.PayrollDisplay
import com.kominfo_mkq.izakod_asn.ui.components.ElevatedCard
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.SecondaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.viewmodel.GajiSayaUiState
import com.kominfo_mkq.izakod_asn.ui.viewmodel.GajiSayaViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GajiSayaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int, Int) -> Unit,
    viewModel: GajiSayaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Gaji Saya",
                subtitle = gajiPeriodLabel(uiState.tahun, uiState.bulan),
                onBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isLoading && !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat ulang"
                        )
                    }
                }
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
                uiState.isLoading -> GajiLoadingContent()
                uiState.isError && uiState.data == null -> GajiErrorContent(
                    message = uiState.errorMessage ?: "Gagal memuat Gaji Saya",
                    onRetry = { viewModel.refresh() }
                )
                else -> GajiSayaContent(
                    uiState = uiState,
                    onPreviousMonth = { viewModel.moveMonth(-1) },
                    onNextMonth = { viewModel.moveMonth(1) },
                    onRefresh = { viewModel.refresh() },
                    onNavigateToDetail = onNavigateToDetail
                )
            }
        }
    }
}

@Composable
fun GajiSayaDashboardTabContent(
    uiState: GajiSayaUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToDetail: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> GajiLoadingContent()
            uiState.isError && uiState.data == null -> GajiErrorContent(
                message = uiState.errorMessage ?: "Gagal memuat Gaji Saya",
                onRetry = onRefresh
            )
            else -> GajiSayaContent(
                uiState = uiState,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onRefresh = onRefresh,
                onNavigateToDetail = onNavigateToDetail,
                contentTopPadding = 0.dp,
                headerContent = {
                    GajiDashboardHeader()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GajiSayaDetailScreen(
    tahun: Int,
    bulan: Int,
    onNavigateBack: () -> Unit,
    viewModel: GajiSayaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tahun, bulan) {
        viewModel.setPeriod(tahun, bulan)
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Detail Gaji",
                subtitle = gajiPeriodLabel(tahun, bulan),
                onBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isLoading && !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Muat ulang"
                        )
                    }
                }
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
                uiState.isLoading -> GajiLoadingContent()
                uiState.isError && uiState.data == null -> GajiErrorContent(
                    message = uiState.errorMessage ?: "Gagal memuat detail gaji",
                    onRetry = { viewModel.refresh() }
                )
                else -> GajiSayaDetailContent(
                    uiState = uiState,
                    onRefresh = { viewModel.refresh() }
                )
            }
        }
    }
}

@Composable
private fun GajiSayaContent(
    uiState: GajiSayaUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToDetail: (Int, Int) -> Unit,
    contentTopPadding: Dp = 16.dp,
    headerContent: (@Composable () -> Unit)? = null
) {
    val data = uiState.data

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, top = contentTopPadding, end = 20.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (headerContent != null) {
            item {
                headerContent()
            }
        }

        item {
            GajiPeriodSelector(
                label = gajiPeriodLabel(uiState.tahun, uiState.bulan),
                isRefreshing = uiState.isRefreshing,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }

        if (uiState.isError && uiState.data != null) {
            item {
                GajiInlineWarning(
                    text = uiState.errorMessage ?: "Data terakhir ditampilkan karena refresh gagal."
                )
            }
        }

        if (data == null) {
            item {
                GajiEmptyState(
                    title = "Data gaji belum tersedia",
                    subtitle = "Belum ada data Gaji Non-ASN untuk periode ini.",
                    onRetry = onRefresh
                )
            }
        } else {
            item {
                GajiProfileSummaryCard(data = data)
            }
            item {
                GajiNominalSummaryCard(
                    calculation = data.perhitungan,
                    display = data.displayPayroll,
                    onOpenDetail = { onNavigateToDetail(uiState.tahun, uiState.bulan) }
                )
            }
            item {
                GajiRekapSummaryCard(rekap = data.rekap)
            }
            item {
                GajiReadinessCard(data = data)
            }
        }
    }
}

@Composable
private fun GajiSayaDetailContent(
    uiState: GajiSayaUiState,
    onRefresh: () -> Unit
) {
    val data = uiState.data

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.isError && uiState.data != null) {
            item {
                GajiInlineWarning(
                    text = uiState.errorMessage ?: "Data terakhir ditampilkan karena refresh gagal."
                )
            }
        }

        if (data == null) {
            item {
                GajiEmptyState(
                    title = "Detail belum tersedia",
                    subtitle = "Belum ada detail gaji untuk periode ini.",
                    onRetry = onRefresh
                )
            }
        } else {
            item {
                GajiProfileSummaryCard(data = data)
            }
            item {
                GajiNominalDetailCard(
                    calculation = data.perhitungan,
                    display = data.displayPayroll
                )
            }
            item {
                GajiRekapSummaryCard(rekap = data.rekap)
            }
            item {
                GajiReadinessCard(data = data)
            }
        }
    }
}

@Composable
private fun GajiDashboardHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Gaji Saya",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Gaji Non-ASN berdasarkan rekap resmi atau estimasi berjalan",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GajiProfileSummaryCard(data: GajiNonAsnMeData) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Text(
            text = data.pegawai.pegawaiNama ?: "-",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = listOfNotNull(data.pegawai.jabatan, data.pegawai.skpd)
                .filter { it.isNotBlank() }
                .joinToString(" - ")
                .ifBlank { "Informasi pegawai belum lengkap" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GajiInfoTile(
                label = "Pendidikan",
                value = data.profile?.pendidikanNama ?: data.profile?.pendidikanSnapshot ?: "-",
                modifier = Modifier.weight(1f)
            )
            GajiInfoTile(
                label = "Kontrak",
                value = data.kontrak?.nomorKontrak ?: "-",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GajiNominalSummaryCard(
    calculation: GajiNonAsnPerhitungan?,
    display: PayrollDisplay?,
    onOpenDetail: () -> Unit
) {
    val displayStatus = display?.status?.lowercase(Locale.ROOT)
    val isLiveEstimate = displayStatus == "estimasi" ||
        calculation?.status?.equals("estimasi_berjalan", ignoreCase = true) == true
    val statusLabel = display?.badge ?: display?.label ?: gajiStatusLabel(calculation?.status)
    val hasEffectiveCalculation = display.hasDisplayPayrollNominal() || calculation.hasEffectiveGajiData()
    val displayNominal = display?.nominal ?: calculation?.totalDibayar

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = when {
                        displayStatus == "belum" || !hasEffectiveCalculation -> "Gaji Belum Dihitung"
                        isLiveEstimate -> "Estimasi Gaji Diterima"
                        else -> "Gaji Diterima"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = displayNominal.formatGajiCurrency(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = PrimaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = display?.message?.takeIf { it.isNotBlank() } ?: calculation?.let {
                        "Gaji pokok ${it.gajiPokok.formatGajiCurrency()} - Potongan ${it.totalPotongan.formatGajiCurrency()}"
                    } ?: "Gaji belum dihitung untuk periode ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            GajiChip(
                text = statusLabel.uppercase(Locale.ROOT),
                color = displayPayrollColor(display, calculation)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onOpenDetail,
            enabled = hasEffectiveCalculation,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lihat rincian gaji")
        }
    }
}

@Composable
private fun GajiNominalDetailCard(
    calculation: GajiNonAsnPerhitungan?,
    display: PayrollDisplay?
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Text(
            text = "Detail Perhitungan Gaji",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = display?.message ?: gajiStatusLabel(calculation?.status),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        GajiDetailRow("Gaji pokok", calculation?.gajiPokok.formatGajiCurrency())
        GajiDetailRow("Potongan TK", calculation?.potonganTk.formatGajiCurrency())
        GajiDetailRow("Potongan tidak lengkap", calculation?.potonganTidakLengkap.formatGajiCurrency())
        GajiDetailRow("Potongan lainnya", calculation?.potonganLainnya.formatGajiCurrency())
        GajiDetailRow("Total potongan", calculation?.totalPotongan.formatGajiCurrency())
        GajiDetailRow("Total dibayar", calculation?.totalDibayar.formatGajiCurrency())
        GajiDetailRow("Status tampilan", display?.label ?: "-")
        GajiDetailRow("Sumber", display?.source ?: "-")
        GajiDetailRow("Status perhitungan", display?.detailStatus ?: gajiStatusLabel(calculation?.status))
        GajiDetailRow("Dihitung", formatGajiDateTime(calculation?.calculatedAt))
    }
}

@Composable
private fun GajiRekapSummaryCard(rekap: GajiNonAsnRekap?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Text(
            text = "Rekap Kehadiran",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = rekap?.sumberRekap ?: "Belum ada rekap resmi untuk periode ini.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GajiMetricTile("Hari kerja", rekap?.totalHariKerja ?: 0, SecondaryLight, Modifier.weight(1f))
            GajiMetricTile("Hadir", rekap?.hadirSah ?: 0, StatusApproved, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GajiMetricTile("TK", rekap?.tk ?: 0, StatusRejected, Modifier.weight(1f))
            GajiMetricTile("Tidak lengkap", rekap?.tidakLengkap ?: 0, StatusPending, Modifier.weight(1f))
        }
    }
}

@Composable
private fun GajiReadinessCard(data: GajiNonAsnMeData) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Text(
            text = "Status Data",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = data.status.label ?: if (data.status.ready) "Data gaji siap" else "Data gaji belum lengkap",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (data.status.issues.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            data.status.issues.take(4).forEach { issue ->
                Text(
                    text = "- $issue",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusPending
                )
            }
        }
    }
}

@Composable
private fun GajiPeriodSelector(
    label: String,
    isRefreshing: Boolean,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth, enabled = !isRefreshing) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Bulan sebelumnya")
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onNextMonth, enabled = !isRefreshing) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Bulan berikutnya")
            }
        }
    }
}

@Composable
private fun GajiInfoTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GajiMetricTile(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
        }
    }
}

@Composable
private fun GajiDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GajiChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GajiInlineWarning(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = StatusPending.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = StatusPending
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun GajiEmptyState(
    title: String,
    subtitle: String,
    onRetry: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = PrimaryLight
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) {
            Text("Muat ulang")
        }
    }
}

@Composable
private fun GajiErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = StatusRejected
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onRetry) {
                Text("Coba lagi")
            }
        }
    }
}

@Composable
private fun GajiLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private fun gajiStatusColor(status: String?): Color {
    val value = status?.lowercase(Locale.ROOT).orEmpty()
    return when (value) {
        "final", "final_opd", "dibayar", "arsip" -> StatusApproved
        "dihitung", "draft", "estimasi_berjalan" -> PrimaryLight
        "revisi", "dikembalikan" -> StatusPending
        "ditolak", "batal" -> StatusRejected
        else -> StatusPending
    }
}

private fun displayPayrollColor(
    display: PayrollDisplay?,
    calculation: GajiNonAsnPerhitungan?
): Color {
    return when (display?.status?.lowercase(Locale.ROOT)) {
        "resmi" -> if (display.isFinal) StatusApproved else PrimaryLight
        "estimasi" -> PrimaryLight
        "belum" -> StatusPending
        else -> gajiStatusColor(calculation?.status)
    }
}

private fun gajiStatusLabel(status: String?): String {
    return when (status?.lowercase(Locale.ROOT)) {
        "estimasi_berjalan" -> "Estimasi"
        "dihitung" -> "Dihitung"
        "final", "final_opd" -> "Final"
        "dibayar" -> "Dibayar"
        "arsip" -> "Arsip"
        "draft" -> "Draft"
        "revisi", "dikembalikan" -> "Revisi"
        "ditolak" -> "Ditolak"
        "batal" -> "Batal"
        else -> "Belum"
    }
}

private fun GajiNonAsnPerhitungan?.hasEffectiveGajiData(): Boolean {
    val calculation = this ?: return false
    return calculation.totalDibayar != null ||
        calculation.gajiPokok != null ||
        !calculation.status.isNullOrBlank()
}

private fun PayrollDisplay?.hasDisplayPayrollNominal(): Boolean {
    val display = this ?: return false
    return display.status == "estimasi" ||
        display.status == "resmi" ||
        display.nominal != null
}

private fun gajiPeriodLabel(tahun: Int, bulan: Int): String {
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

private fun Double?.formatGajiCurrency(): String {
    val value = this ?: return "-"
    val formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(value)
}

private fun formatGajiDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "-"
    return try {
        val source = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val target = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
        target.format(source.parse(value)!!)
    } catch (_: Exception) {
        value
    }
}
