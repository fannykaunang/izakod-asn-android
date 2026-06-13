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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.TppApelHarian
import com.kominfo_mkq.izakod_asn.data.model.TppMeData
import com.kominfo_mkq.izakod_asn.data.model.TppNominal
import com.kominfo_mkq.izakod_asn.data.model.TppRekapKehadiran
import com.kominfo_mkq.izakod_asn.ui.components.ElevatedCard
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.SecondaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.TertiaryLight
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TppSayaUiState
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TppSayaViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TppSayaScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int, Int) -> Unit,
    viewModel: TppSayaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "TPP Saya",
                subtitle = periodLabel(uiState.tahun, uiState.bulan),
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
                uiState.isLoading -> TppLoadingContent()
                uiState.isError && uiState.data == null -> TppErrorContent(
                    message = uiState.errorMessage ?: "Gagal memuat TPP Saya",
                    onRetry = { viewModel.refresh() }
                )
                else -> TppSayaContent(
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
fun TppSayaDashboardTabContent(
    uiState: TppSayaUiState,
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
            uiState.isLoading -> TppLoadingContent()
            uiState.isError && uiState.data == null -> TppErrorContent(
                message = uiState.errorMessage ?: "Gagal memuat TPP Saya",
                onRetry = onRefresh
            )
            else -> TppSayaContent(
                uiState = uiState,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth,
                onRefresh = onRefresh,
                onNavigateToDetail = onNavigateToDetail,
                contentTopPadding = 0.dp,
                headerContent = {
                    TppDashboardHeader()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TppSayaDetailScreen(
    tahun: Int,
    bulan: Int,
    onNavigateBack: () -> Unit,
    viewModel: TppSayaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tahun, bulan) {
        viewModel.setPeriod(tahun, bulan)
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Detail TPP",
                subtitle = periodLabel(tahun, bulan),
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
                uiState.isLoading -> TppLoadingContent()
                uiState.isError && uiState.data == null -> TppErrorContent(
                    message = uiState.errorMessage ?: "Gagal memuat detail TPP",
                    onRetry = { viewModel.refresh() }
                )
                else -> TppSayaDetailContent(
                    uiState = uiState,
                    onRefresh = { viewModel.refresh() }
                )
            }
        }
    }
}

@Composable
private fun TppSayaContent(
    uiState: TppSayaUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateToDetail: (Int, Int) -> Unit,
    contentTopPadding: Dp = 16.dp,
    headerContent: (@Composable () -> Unit)? = null
) {
    val data = uiState.data
    val header = headerContent

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, top = contentTopPadding, end = 20.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (header != null) {
            item {
                header()
            }
        }

        item {
            TppPeriodSelector(
                label = periodLabel(uiState.tahun, uiState.bulan),
                isRefreshing = uiState.isRefreshing,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }

        if (uiState.isError && uiState.data != null) {
            item {
                TppInlineWarning(
                    text = uiState.errorMessage ?: "Data terakhir ditampilkan karena refresh gagal."
                )
            }
        }

        if (data == null) {
            item {
                TppEmptyState(
                    title = "Data TPP belum tersedia",
                    subtitle = "Belum ada data TPP untuk periode ini.",
                    onRetry = onRefresh
                )
            }
        } else {
            item {
                TppProfileSummaryCard(data = data)
            }
            item {
                TppNominalSummaryCard(nominal = data.nominalTpp)
            }
            item {
                TppRekapSummaryCard(
                    rekap = data.rekap,
                    hasEffectivePayrollData = data.nominalTpp.hasEffectiveTppData(),
                    onOpenDetail = { onNavigateToDetail(uiState.tahun, uiState.bulan) }
                )
            }
            item {
                TppApelSummaryCard(rekap = data.rekap, apelHarian = data.apelHarian)
            }
            item {
                TppReadinessCard(data = data)
            }
        }
    }
}

@Composable
private fun TppSayaDetailContent(
    uiState: TppSayaUiState,
    onRefresh: () -> Unit
) {
    val data = uiState.data

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.isError && uiState.data != null) {
            item {
                TppInlineWarning(
                    text = uiState.errorMessage ?: "Data terakhir ditampilkan karena refresh gagal."
                )
            }
        }

        if (data == null) {
            item {
                TppEmptyState(
                    title = "Detail belum tersedia",
                    subtitle = "Belum ada detail TPP untuk periode ini.",
                    onRetry = onRefresh
                )
            }
        } else {
            item {
                TppProfileSummaryCard(data = data)
            }
            item {
                TppNominalDetailCard(nominal = data.nominalTpp)
            }
            item {
                TppRekapDetailCard(rekap = data.rekap)
            }
            item {
                TppApelSummaryCard(rekap = data.rekap, apelHarian = data.apelHarian)
            }
            item {
                Text(
                    text = "Rincian Apel Harian",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (data.apelHarian.isEmpty()) {
                item {
                    TppEmptyCard(text = "Belum ada snapshot apel harian untuk periode ini.")
                }
            } else {
                items(data.apelHarian) { item ->
                    TppApelDailyItem(item = item)
                }
            }
        }
    }
}

@Composable
private fun TppDashboardHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "TPP Saya",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Estimasi dan rincian TPP Anda",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TppPeriodSelector(
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
            IconButton(
                onClick = onPreviousMonth,
                enabled = !isRefreshing
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Bulan sebelumnya"
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Periode",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onNextMonth,
                enabled = !isRefreshing
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Bulan berikutnya"
                )
            }
        }

        if (isRefreshing) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(
                    text = "Memuat ulang data TPP...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TppProfileSummaryCard(data: TppMeData) {
    val statusColor = statusColor(data.status.label ?: data.profile?.statusTpp)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = data.pegawai.pegawaiNama ?: "Pegawai",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = data.pegawai.skpd ?: data.profile?.skpdSnapshot ?: "-",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = data.profile?.jabatanSnapshot ?: data.pegawai.jabatan ?: "-",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TppChip(
                text = data.status.label ?: data.profile?.statusTpp ?: "Belum siap",
                color = statusColor
            )
        }
    }
}

@Composable
private fun TppNominalSummaryCard(nominal: TppNominal?) {
    val isLiveEstimate = nominal?.status?.equals("estimasi_berjalan", ignoreCase = true) == true
    val title = when {
        nominal == null -> "TPP Belum Dihitung"
        isLiveEstimate -> "Estimasi TPP Diterima"
        else -> "TPP Diterima"
    }
    val chipColor = when {
        nominal?.isFinal == true -> StatusApproved
        isLiveEstimate -> PrimaryLight
        else -> StatusPending
    }

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
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = nominal?.estimasiDiterima.formatCurrency(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = PrimaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = nominal?.let {
                        "Disiplin ${it.nilaiDisiplin.formatCurrency()} + Kinerja ${it.nilaiKinerja.formatCurrency()}"
                    } ?: "Nominal belum dihitung untuk periode ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TppChip(
                text = nominal?.label ?: "Belum dihitung",
                color = chipColor
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TppSmallCountCurrency(
                label = "Total Netto",
                value = nominal?.totalNetto ?: 0.0,
                color = SecondaryLight,
                modifier = Modifier.weight(1f)
            )
            TppSmallCountCurrency(
                label = "Total Potongan",
                value = nominal?.totalPotongan ?: 0.0,
                color = if ((nominal?.totalPotongan ?: 0.0) > 0.0) StatusRejected else StatusApproved,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TppNominalDetailCard(nominal: TppNominal?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Text(
            text = "Detail Nilai TPP",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = nominal?.label ?: "Belum dihitung",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        TppDetailRow("TPP dasar", nominal?.totalTppDasar.formatCurrency())
        TppDetailRow("Nilai disiplin", nominal?.nilaiDisiplin.formatCurrency())
        TppDetailRow("Nilai kinerja", nominal?.nilaiKinerja.formatCurrency())
        TppDetailRow("Potongan disiplin", nominal?.potonganDisiplin.formatCurrency())
        TppDetailRow("Potongan kinerja", nominal?.potonganKinerja.formatCurrency())
        TppDetailRow("Potongan lainnya", nominal?.potonganLainnya.formatCurrency())
        TppDetailRow("Potongan pajak", nominal?.potonganPajak.formatCurrency())
        TppDetailRow("Total potongan", nominal?.totalPotongan.formatCurrency())
        TppDetailRow("Total netto", nominal?.totalNetto.formatCurrency())
        TppDetailRow("Total dibayar", nominal?.totalDibayar.formatCurrency())
        TppDetailRow("Perkiraan diterima", nominal?.estimasiDiterima.formatCurrency())
        TppDetailRow("Status", nominal?.status ?: "-")
        TppDetailRow("Dihitung", formatDateTime(nominal?.calculatedAt))
    }
}

@Composable
private fun TppRekapSummaryCard(
    rekap: TppRekapKehadiran?,
    hasEffectivePayrollData: Boolean,
    onOpenDetail: () -> Unit
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Rekap Kehadiran TPP",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = rekap?.status ?: "Belum ada rekap",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onOpenDetail,
                enabled = rekap != null || hasEffectivePayrollData
            ) {
                Text(text = "Detail")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TppMetricCard(
                label = "Hadir Sah",
                value = "${rekap?.hadirSahValue() ?: 0} / ${rekap?.totalHariKerja ?: 0}",
                subtitle = "Scan ${rekap?.hadirScanLengkap ?: 0} + Resmi ${rekap?.hadirPengecualianResmi ?: 0}",
                icon = Icons.Default.FactCheck,
                color = StatusApproved,
                modifier = Modifier.weight(1f)
            )
            TppMetricCard(
                label = "Potongan",
                value = formatPercent(rekap?.potonganDisiplinPersen ?: 0.0),
                subtitle = "Faktor ${formatPercent(rekap?.faktorBayarTppPersen ?: 100.0)}",
                icon = Icons.Default.Warning,
                color = if ((rekap?.potonganDisiplinPersen ?: 0.0) > 0.0) StatusRejected else StatusApproved,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TppRekapDetailCard(rekap: TppRekapKehadiran?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Text(
            text = "Detail Kehadiran",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(12.dp))

        TppDetailRow("Hari kerja", "${rekap?.totalHariKerja ?: 0}")
        TppDetailRow("Hadir sah", "${rekap?.hadirSahValue() ?: 0}")
        TppDetailRow("Scan lengkap", "${rekap?.hadirScanLengkap ?: 0}")
        TppDetailRow("Resmi dibayar penuh", "${rekap?.hadirPengecualianResmi ?: 0}")
        TppDetailRow("D/I/S/C", "${rekap?.discTotal() ?: 0}")
        TppDetailRow("TK", "${rekap?.tk ?: 0}")
        TppDetailRow("Tidak lengkap", "${rekap?.tidakLengkap ?: 0}")
        TppDetailRow("TL", "${rekap?.tlTotal() ?: 0}")
        TppDetailRow("PC", "${rekap?.pcTotal() ?: 0}")
        TppDetailRow("Potongan disiplin", formatPercent(rekap?.potonganDisiplinPersen ?: 0.0))
        TppDetailRow("Sumber", rekap?.sumberRekap ?: "-")
        TppDetailRow("Dihitung", formatDateTime(rekap?.calculatedAt))
    }
}

@Composable
private fun TppApelSummaryCard(
    rekap: TppRekapKehadiran?,
    apelHarian: List<TppApelHarian>
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Text(
            text = "Absen Apel",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Snapshot classifier Apel yang dipakai TPP.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TppMetricCard(
                label = "Valid",
                value = "${rekap?.apelValid ?: apelHarian.sumOf { it.apelValid }}",
                subtitle = "${rekap?.apelTotalSesi ?: apelHarian.sumOf { it.totalSesi }} sesi",
                icon = Icons.Default.EventAvailable,
                color = StatusApproved,
                modifier = Modifier.weight(1f)
            )
            TppMetricCard(
                label = "Tidak Ikut",
                value = "${rekap?.apelTidakIkut ?: apelHarian.sumOf { it.tidakIkutValue() }}",
                subtitle = formatPercent(rekap?.potonganApelPersen ?: 0.0),
                icon = Icons.Default.Checklist,
                color = SecondaryLight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TppReadinessCard(data: TppMeData) {
    val issues = data.status.profileReadinessIssues

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 20.dp
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (data.status.siapDihitung) Icons.Default.FactCheck else Icons.Default.Warning,
                contentDescription = null,
                tint = if (data.status.siapDihitung) StatusApproved else StatusPending
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = if (data.status.siapDihitung) "Siap dihitung" else "Belum siap dihitung",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = data.status.dataSource ?: "Sumber data belum tersedia",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (issues.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            issues.forEach { issue ->
                Text(
                    text = "- $issue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TppApelDailyItem(item: TppApelHarian) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 0.dp,
        cornerRadius = 18.dp
    ) {
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
                    text = formatDate(item.tanggal),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = item.jenisApelTercatat ?: "Apel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TppChip(
                text = item.statusClassifier ?: if (item.apelValid > 0) "VALID" else "TIDAK IKUT",
                color = when {
                    item.apelValid > 0 -> StatusApproved
                    item.apelReview > 0 || item.sesiBelumFinal > 0 -> StatusPending
                    else -> SecondaryLight
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TppSmallCount("Valid", item.apelValid, StatusApproved, Modifier.weight(1f))
            TppSmallCount("Review", item.apelReview, StatusPending, Modifier.weight(1f))
            TppSmallCount("Tidak hadir", item.tidakIkutValue(), SecondaryLight, Modifier.weight(1f))
        }

        if (!item.catatan.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.catatan,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TppMetricCard(
    label: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TppDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TppSmallCountCurrency(
    label: String,
    value: Double,
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
                text = value.formatCurrency(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TppSmallCount(
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
private fun TppChip(text: String, color: Color) {
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
private fun TppInlineWarning(text: String) {
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
private fun TppEmptyState(
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
            tint = PrimaryLight,
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(onClick = onRetry) {
            Text(text = "Muat ulang")
        }
    }
}

@Composable
private fun TppEmptyCard(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TppLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Memuat data TPP...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TppErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TppEmptyState(
            title = "TPP belum dapat dimuat",
            subtitle = message,
            onRetry = onRetry
        )
    }
}

private fun TppRekapKehadiran.hadirSahValue(): Int {
    return hadirSah ?: (hadirScanLengkap + hadirPengecualianResmi)
}

private fun TppRekapKehadiran.discTotal(): Int {
    return dinasLuar + izin + sakitDenganSurat + sakitDenganSuratPotong2 +
        sakitDenganSuratPotong3 + sakitLebih3Bulan + sakitTanpaSurat +
        cutiNormatif + cutiPribadi + cutiMelahirkan + cutiAlasanPenting + cutiTahunan12Hari
}

private fun TppRekapKehadiran.tlTotal(): Int {
    return tl1 + tl2 + tl3 + tl4
}

private fun TppRekapKehadiran.pcTotal(): Int {
    return pc1 + pc2 + pc3 + pc4
}

private fun TppApelHarian.tidakIkutValue(): Int {
    return (totalSesi - apelValid - apelReview - apelRejected).coerceAtLeast(apelTidakHadir)
}

private fun monthName(month: Int): String {
    val names = listOf(
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
    return names.getOrElse(month - 1) { "-" }
}

private fun periodLabel(tahun: Int, bulan: Int): String {
    return "${monthName(bulan)} $tahun"
}

private fun formatPercent(value: Double): String {
    val locale = Locale("id", "ID")
    val number = if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(locale, "%.1f", value)
    }
    return "$number%"
}

private fun Double?.formatCurrency(): String {
    val value = this ?: 0.0
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(value)
}

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank()) return "-"

    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        formatter.format(parser.parse(value.take(10))!!)
    }.getOrElse {
        value.take(10)
    }
}

private fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "-"

    return runCatching {
        val normalized = value.replace("T", " ").take(19)
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("id", "ID"))
        val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
        formatter.format(parser.parse(normalized)!!)
    }.getOrElse {
        value
    }
}

private fun TppNominal?.hasEffectiveTppData(): Boolean {
    val nominal = this ?: return false
    return nominal.estimasiDiterima > 0.0 ||
        nominal.totalDibayar > 0.0 ||
        nominal.totalNetto > 0.0 ||
        !nominal.status.isNullOrBlank()
}

private fun statusColor(status: String?): Color {
    val normalized = status.orEmpty().lowercase(Locale.getDefault())
    return when {
        normalized.contains("review") || normalized.contains("belum") || normalized.contains("perlu") -> StatusPending
        normalized.contains("siap") || normalized.contains("verified") || normalized.contains("valid") -> StatusApproved
        normalized.contains("gagal") || normalized.contains("reject") || normalized.contains("tolak") -> StatusRejected
        else -> TertiaryLight
    }
}
