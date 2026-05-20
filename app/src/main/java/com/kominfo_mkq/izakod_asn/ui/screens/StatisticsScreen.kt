package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.RotateLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.DailyMetricsData
import com.kominfo_mkq.izakod_asn.data.model.DailyTimeSeriesItem
import com.kominfo_mkq.izakod_asn.data.model.MetricsData
import com.kominfo_mkq.izakod_asn.data.model.TimeSeriesItem
import com.kominfo_mkq.izakod_asn.ui.components.ElevatedCard
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.SecondaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.theme.TertiaryLight
import com.kominfo_mkq.izakod_asn.ui.viewmodel.StatisticsViewModel
import kotlin.math.max

private fun String?.toIntSafe(): Int = this?.toIntOrNull() ?: 0
private fun String?.toDoubleSafe(): Double = this?.toDoubleOrNull() ?: 0.0
private fun String?.toFloatSafe(): Float = this?.toFloatOrNull() ?: 0f

private fun formatDuration(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0j 0m"
    val jam = totalMinutes / 60
    val menit = totalMinutes % 60
    return "${jam}j ${menit}m"
}

private fun formatDateLabel(value: String): String {
    val parts = value.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}" else value
}

private data class StatisticCardItem(
    val title: String,
    val value: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color
)

private enum class StatisticsTab(val title: String) {
    DAILY("Harian"),
    MONTHLY("Bulanan"),
    SUMMARY("Ringkasan")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeTab by remember { mutableStateOf(StatisticsTab.DAILY) }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Statistik",
                compact = true
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
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> StatisticsLoadingContent()
                uiState.isError &&
                    uiState.dailyMetrics == null &&
                    uiState.monthlyMetrics == null &&
                    uiState.summaryMetrics == null -> {
                    ErrorContent(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { viewModel.refresh() }
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            StatisticsHeroCard()
                        }

                        item {
                            StatisticsTabSwitcher(
                                activeTab = activeTab,
                                onTabSelected = { activeTab = it }
                            )
                        }

                        when (activeTab) {
                            StatisticsTab.DAILY -> {
                                uiState.dailyMetrics?.let { dailyMetrics ->
                                    item {
                                        DailyStatisticsSection(
                                            metrics = dailyMetrics,
                                            timeSeries = uiState.dailyTimeSeries
                                        )
                                    }
                                }
                            }

                            StatisticsTab.MONTHLY -> {
                                uiState.monthlyMetrics?.let { monthlyMetrics ->
                                    item {
                                        MonthlyStatisticsSection(
                                            metrics = monthlyMetrics,
                                            timeSeries = uiState.monthlyTimeSeries
                                        )
                                    }
                                }
                            }

                            StatisticsTab.SUMMARY -> {
                                item {
                                    PerformanceSection(
                                        metrics = uiState.summaryMetrics ?: uiState.monthlyMetrics
                                    )
                                }
                            }
                        }

                        if (uiState.isError && uiState.errorMessage != null) {
                            item {
                                InlineWarningCard(message = uiState.errorMessage!!)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsHeroCard() {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Statistik Kinerja",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Pilih tab harian, bulanan, atau ringkasan untuk melihat metrik yang lebih fokus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatisticsTabSwitcher(
    activeTab: StatisticsTab,
    onTabSelected: (StatisticsTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatisticsTab.entries.forEach { tab ->
                val selected = tab == activeTab
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    onClick = { onTabSelected(tab) }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.CircularProgressIndicator()
            Text(
                text = "Memuat statistik harian dan bulanan...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun DailyStatisticsSection(
    metrics: DailyMetricsData,
    timeSeries: List<DailyTimeSeriesItem>
) {
    val cards = listOf(
        StatisticCardItem(
            title = "Diverifikasi",
            value = metrics.jumlahDiverifikasi.toIntSafe().toString(),
            description = "Total kegiatan yang diverifikasi",
            icon = Icons.Default.CheckCircle,
            accent = StatusApproved
        ),
        StatisticCardItem(
            title = "Pending",
            value = metrics.jumlahPending.toIntSafe().toString(),
            description = "Total kegiatan yang pending",
            icon = Icons.Default.Schedule,
            accent = StatusPending
        ),
        StatisticCardItem(
            title = "Ditolak",
            value = metrics.jumlahDitolak.toIntSafe().toString(),
            description = "Total kegiatan yang ditolak",
            icon = Icons.Default.Close,
            accent = StatusRejected
        ),
        StatisticCardItem(
            title = "AVG Produktivitas",
            value = String.format("%.2f%%", metrics.avgProduktivitas.toDoubleSafe()),
            description = "Produktivitas rata-rata",
            icon = Icons.Default.TrendingUp,
            accent = PrimaryLight
        ),
        StatisticCardItem(
            title = "AVG Rating",
            value = String.format("%.2f", metrics.rataRataRating.toDoubleSafe()),
            description = "Rating rata-rata kegiatan",
            icon = Icons.Default.Star,
            accent = TertiaryLight
        ),
        StatisticCardItem(
            title = "Total Durasi",
            value = formatDuration(metrics.totalDurasi.toIntSafe()),
            description = "Total durasi kegiatan",
            icon = Icons.Default.AccessTime,
            accent = SecondaryLight
        )
    )

    StatisticsSection(
        title = "Statistik Harian",
        subtitle = "Ringkasan 30 hari terakhir seperti di halaman web statistik harian.",
        cards = cards,
        chartTitle = "Tren Jumlah Kegiatan (30 Hari Terakhir)",
        chartSubtitle = "Grafik jumlah kegiatan berdasarkan tanggal",
        chartValues = timeSeries.map { it.jumlahKegiatan.toFloatSafe() },
        chartLabels = timeSeries.map { formatDateLabel(it.tanggal) }
    )
}

@Composable
private fun MonthlyStatisticsSection(
    metrics: MetricsData,
    timeSeries: List<TimeSeriesItem>
) {
    val cards = listOf(
        StatisticCardItem(
            title = "Total Kegiatan",
            value = metrics.totalKegiatan.toIntSafe().toString(),
            description = "Total kegiatan bulanan",
            icon = Icons.Default.Assessment,
            accent = PrimaryLight
        ),
        StatisticCardItem(
            title = "Total Durasi",
            value = formatDuration(metrics.totalDurasiMenit.toIntSafe()),
            description = "Total durasi kegiatan",
            icon = Icons.Default.Schedule,
            accent = SecondaryLight
        ),
        StatisticCardItem(
            title = "Rata-rata Per Hari",
            value = String.format("%.2f", metrics.rataRataKegiatanPerHari.toDoubleSafe()),
            description = "Kegiatan rata-rata per hari",
            icon = Icons.Default.CalendarMonth,
            accent = TertiaryLight
        ),
        StatisticCardItem(
            title = "Diverifikasi",
            value = metrics.totalDiverifikasi.toIntSafe().toString(),
            description = "Total kegiatan diverifikasi",
            icon = Icons.Default.CheckCircle,
            accent = StatusApproved
        ),
        StatisticCardItem(
            title = "Pending",
            value = metrics.totalPending.toIntSafe().toString(),
            description = "Total kegiatan pending",
            icon = Icons.Default.Schedule,
            accent = StatusPending
        ),
        StatisticCardItem(
            title = "Ditolak",
            value = metrics.totalDitolak.toIntSafe().toString(),
            description = "Total kegiatan ditolak",
            icon = Icons.Default.Close,
            accent = StatusRejected
        ),
        StatisticCardItem(
            title = "Persentase Verifikasi",
            value = String.format("%.2f%%", metrics.persentaseVerifikasi.toFloatSafe()),
            description = "Persentase kegiatan terverifikasi",
            icon = Icons.Default.TrendingUp,
            accent = Color(0xFF14B8A6)
        ),
        StatisticCardItem(
            title = "Rata-rata Rating",
            value = String.format("%.2f", metrics.rataRataRating.toDoubleSafe()),
            description = "Rating rata-rata kegiatan",
            icon = Icons.Default.Star,
            accent = Color(0xFFF97316)
        ),
        StatisticCardItem(
            title = "Total Revisi",
            value = metrics.totalRevisi.toIntSafe().toString(),
            description = "Total kegiatan direvisi",
            icon = Icons.Outlined.RotateLeft,
            accent = StatusRevised
        )
    )

    StatisticsSection(
        title = "Statistik Bulanan",
        subtitle = "Ringkasan agregat seperti di halaman web statistik bulanan.",
        cards = cards,
        chartTitle = "Tren Total Kegiatan (6 Bulan Terakhir)",
        chartSubtitle = "Grafik total kegiatan berdasarkan bulan dan tahun",
        chartValues = timeSeries.map { it.totalKegiatan.toFloatSafe() },
        chartLabels = timeSeries.map { it.bulanNama.substringBefore(" ") }
    )
}

@Composable
private fun StatisticsSection(
    title: String,
    subtitle: String,
    cards: List<StatisticCardItem>,
    chartTitle: String,
    chartSubtitle: String,
    chartValues: List<Float>,
    chartLabels: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        cards.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    StatisticSummaryCard(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        StatisticChartCard(
            title = chartTitle,
            subtitle = chartSubtitle,
            values = chartValues,
            labels = chartLabels
        )
    }
}

@Composable
private fun StatisticSummaryCard(
    item: StatisticCardItem,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = 1.dp,
        cornerRadius = 22.dp
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
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = item.accent.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.accent
                )
            }
        }
    }
}

@Composable
private fun StatisticChartCard(
    title: String,
    subtitle: String,
    values: List<Float>,
    labels: List<String>
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        cornerRadius = 24.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (values.isEmpty()) {
                EmptyChartState()
            } else {
                SimpleLineChart(
                    values = values,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )

                ChartLabelRow(labels = labels)
            }
        }
    }
}

@Composable
private fun EmptyChartState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Belum ada data grafik",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "Grafik akan tampil saat data statistik tersedia.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ChartLabelRow(labels: List<String>) {
    if (labels.isEmpty()) return

    val displayed = when {
        labels.size <= 4 -> labels
        else -> listOf(
            labels.first(),
            labels[labels.size / 3],
            labels[(labels.size * 2) / 3],
            labels.last()
        )
    }.distinct()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        displayed.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SimpleLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = PrimaryLight
) {
    val maxValue = max(values.maxOrNull()?.takeIf { it > 0f }?.toInt() ?: 0, 1).toFloat()

    Canvas(modifier = modifier) {
        val paddingX = 24.dp.toPx()
        val paddingTop = 18.dp.toPx()
        val paddingBottom = 28.dp.toPx()
        val chartWidth = size.width - paddingX * 2
        val chartHeight = size.height - paddingTop - paddingBottom

        repeat(4) { index ->
            val y = paddingTop + (chartHeight / 3f) * index
            drawLine(
                color = Color.Gray.copy(alpha = 0.16f),
                start = Offset(paddingX, y),
                end = Offset(size.width - paddingX, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val points = values.mapIndexed { index, value ->
            val x = if (values.size == 1) {
                size.width / 2f
            } else {
                paddingX + (chartWidth / (values.size - 1)) * index
            }
            val normalized = value / maxValue
            val y = paddingTop + chartHeight - (normalized * chartHeight)
            Offset(x, y)
        }

        if (points.isNotEmpty()) {
            val fillPath = Path().apply {
                moveTo(points.first().x, paddingTop + chartHeight)
                points.forEachIndexed { index, point ->
                    if (index == 0) {
                        lineTo(point.x, point.y)
                    } else {
                        lineTo(point.x, point.y)
                    }
                }
                lineTo(points.last().x, paddingTop + chartHeight)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.24f),
                        lineColor.copy(alpha = 0.02f)
                    )
                )
            )

            val linePath = Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
            }

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            points.forEach { point ->
                drawCircle(
                    color = lineColor,
                    radius = 5.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.5.dp.toPx(),
                    center = point
                )
            }

        }
    }
}

@Composable
private fun InlineWarningCard(message: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = StatusPending.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SyncProblem,
                contentDescription = null,
                tint = StatusPending
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
