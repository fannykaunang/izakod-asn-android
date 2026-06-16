package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.GradientEndLight
import com.kominfo_mkq.izakod_asn.ui.theme.GradientStartLight
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TertundaLaporanItem
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TertundaPenilaianItem
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TertundaTargetItem
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TertundaTargetKind
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TertundaUiState
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TertundaViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TertundaScreen(
    onNavigateBack: () -> Unit,
    onOpenLaporan: (Int) -> Unit,
    onOpenTarget: (Int) -> Unit,
    onOpenPenilaian: (Int) -> Unit,
    onOpenReports: () -> Unit,
    onOpenTargets: () -> Unit,
    onOpenPenilaianList: () -> Unit,
    viewModel: TertundaViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh(context) }
    )

    LaunchedEffect(Unit) {
        viewModel.refresh(context)
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Tertunda",
                subtitle = "Laporan, target, dan penilaian yang perlu ditindaklanjuti",
                compact = true,
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.refresh(context) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoading && uiState.total == 0 -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.isError -> {
                    TertundaStateCard(
                        icon = Icons.Default.Warning,
                        title = "Gagal memuat tertunda",
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        actionText = "Coba Lagi",
                        onAction = { viewModel.refresh(context) },
                        modifier = Modifier
                            .padding(20.dp)
                            .align(Alignment.Center)
                    )
                }

                else -> {
                    TertundaContent(
                        uiState = uiState,
                        onOpenLaporan = onOpenLaporan,
                        onOpenTarget = onOpenTarget,
                        onOpenPenilaian = onOpenPenilaian,
                        onOpenReports = onOpenReports,
                        onOpenTargets = onOpenTargets,
                        onOpenPenilaianList = onOpenPenilaianList
                    )
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun TertundaContent(
    uiState: TertundaUiState,
    onOpenLaporan: (Int) -> Unit,
    onOpenTarget: (Int) -> Unit,
    onOpenPenilaian: (Int) -> Unit,
    onOpenReports: () -> Unit,
    onOpenTargets: () -> Unit,
    onOpenPenilaianList: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TertundaHeroCard(uiState = uiState)
        }

        if (uiState.total == 0) {
            item {
                TertundaStateCard(
                    icon = Icons.Default.CheckCircle,
                    title = "Tidak ada tugas tertunda",
                    message = "Semua laporan, target, dan penilaian tertunda sudah aman.",
                    actionText = "Lihat Laporan",
                    onAction = onOpenReports
                )
            }
            return@LazyColumn
        }

        item {
            TertundaSectionHeader(
                title = "Laporan",
                subtitle = "Draft, revisi, atau ditolak yang perlu dibenahi.",
                count = uiState.laporan.size,
                accent = StatusRevised,
                onOpenAll = onOpenReports
            )
        }
        if (uiState.laporan.isEmpty()) {
            item {
                TertundaMiniEmptyCard("Tidak ada laporan yang perlu ditindaklanjuti.")
            }
        } else {
            items(uiState.laporan, key = { "laporan-${it.id}" }) { item ->
                TertundaLaporanCard(item = item, onClick = { onOpenLaporan(item.id) })
            }
        }

        item {
                TertundaSectionHeader(
                    title = "Target",
                    subtitle = "Target draft/revisi atau realisasi belum lengkap, lengkap dengan periodenya.",
                    count = uiState.target.size,
                    accent = StatusPending,
                    onOpenAll = onOpenTargets
            )
        }
        if (uiState.target.isEmpty()) {
            item {
                TertundaMiniEmptyCard("Tidak ada target yang perlu ditindaklanjuti.")
            }
        } else {
            items(uiState.target, key = { "target-${it.id}-${it.kind}" }) { item ->
                TertundaTargetCard(item = item, onClick = { onOpenTarget(item.id) })
            }
        }

        item {
                TertundaSectionHeader(
                    title = "Penilaian",
                    subtitle = "Draft penilaian saya yang belum final, lengkap dengan periodenya.",
                    count = uiState.penilaian.size,
                    accent = PrimaryLight,
                    onOpenAll = onOpenPenilaianList
            )
        }
        if (uiState.penilaian.isEmpty()) {
            item {
                TertundaMiniEmptyCard("Tidak ada penilaian saya yang tertunda.")
            }
        } else {
            items(uiState.penilaian, key = { "penilaian-${it.id}" }) { item ->
                TertundaPenilaianCard(item = item, onClick = { onOpenPenilaian(item.id) })
            }
        }
    }
}

@Composable
private fun TertundaHeroCard(uiState: TertundaUiState) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(GradientStartLight, GradientEndLight)))
                .padding(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
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
                            text = "Pekerjaan Tertunda",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = if (uiState.total > 0) {
                                "${uiState.total} item perlu ditindaklanjuti agar alur kerja tidak tertahan."
                            } else {
                                "Semua laporan, target, dan penilaian dalam kondisi aman."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.86f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Icon(
                            imageVector = if (uiState.total > 0) Icons.Default.AccessTime else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(14.dp)
                                .size(30.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TertundaSummaryPill(value = uiState.laporan.size, label = "Laporan")
                    TertundaSummaryPill(value = uiState.target.size, label = "Target")
                    TertundaSummaryPill(value = uiState.penilaian.size, label = "Penilaian")
                }
            }
        }
    }
}

@Composable
private fun TertundaSummaryPill(
    value: Int,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.16f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.88f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TertundaSectionHeader(
    title: String,
    subtitle: String,
    count: Int,
    accent: Color,
    onOpenAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = count.toString(),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = accent
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onOpenAll) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Buka semua")
        }
    }
}

@Composable
private fun TertundaLaporanCard(
    item: TertundaLaporanItem,
    onClick: () -> Unit
) {
    TertundaItemCard(
        icon = Icons.Default.Description,
        accent = reportStatusColor(item.status),
        title = item.title,
        subtitle = formatDateShort(item.date),
        status = reportStatusLabel(item.status),
        body = item.note?.takeIf { it.isNotBlank() } ?: item.category.orEmpty().ifBlank { "Laporan perlu dibuka kembali." },
        onClick = onClick
    )
}

@Composable
private fun TertundaTargetCard(
    item: TertundaTargetItem,
    onClick: () -> Unit
) {
    val title = if (item.kind == TertundaTargetKind.REALISASI) {
        "Realisasi belum lengkap"
    } else {
        item.title
    }
    val body = if (item.kind == TertundaTargetKind.REALISASI) {
        "${item.filledItems}/${item.totalItems} item terisi, ${item.missingItems} item perlu dilengkapi."
    } else {
        "Status target ${targetStatusLabel(item.status)}. Buka target untuk melanjutkan."
    }

    TertundaItemCard(
        icon = if (item.kind == TertundaTargetKind.REALISASI) Icons.Default.TaskAlt else Icons.Default.Flag,
        accent = if (item.kind == TertundaTargetKind.REALISASI) StatusPending else StatusRevised,
        title = title,
        subtitle = "Periode ${item.periodLabel}",
        status = if (item.kind == TertundaTargetKind.REALISASI) "Realisasi" else targetStatusLabel(item.status),
        body = body,
        onClick = onClick
    )
}

@Composable
private fun TertundaPenilaianCard(
    item: TertundaPenilaianItem,
    onClick: () -> Unit
) {
    val value = item.nilaiAkhir?.let { DecimalFormat("#.##").format(it) } ?: "-"
    val body = "Nilai akhir $value, predikat ${item.predikat?.takeIf { it.isNotBlank() } ?: "-"}."

    TertundaItemCard(
        icon = Icons.Default.Star,
        accent = PrimaryLight,
        title = item.title,
        subtitle = "Periode ${item.periodLabel}",
        status = penilaianStatusLabel(item.status),
        body = body,
        onClick = onClick
    )
}

@Composable
private fun TertundaItemCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    status: String,
    body: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                TertundaIconBadge(
                    icon = icon,
                    color = accent,
                    background = accent.copy(alpha = 0.12f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TertundaStatusPill(text = status, color = accent)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Text(
                    text = body,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TertundaMiniEmptyCard(message: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TertundaIconBadge(
                icon = Icons.Default.CheckCircle,
                color = StatusApproved,
                background = StatusApproved.copy(alpha = 0.12f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TertundaStateCard(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (icon == Icons.Default.Warning) StatusRejected else StatusApproved

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            TertundaIconBadge(
                icon = icon,
                color = accent,
                background = accent.copy(alpha = 0.12f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}

@Composable
private fun TertundaIconBadge(
    icon: ImageVector,
    color: Color,
    background: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = background
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(13.dp)
                .size(26.dp),
            tint = color
        )
    }
}

@Composable
private fun TertundaStatusPill(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.13f)
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

private fun reportStatusLabel(status: String): String {
    return when (status.lowercase().trim()) {
        "draft" -> "Draft"
        "revisi", "perlu revisi", "revised", "revision" -> "Revisi"
        "ditolak", "rejected" -> "Ditolak"
        else -> status.toTitleLabel()
    }
}

private fun reportStatusColor(status: String): Color {
    return when (status.lowercase().trim()) {
        "ditolak", "rejected" -> StatusRejected
        "revisi", "perlu revisi", "revised", "revision" -> StatusRevised
        else -> StatusPending
    }
}

private fun targetStatusLabel(status: String): String {
    return when (status.lowercase().trim()) {
        "draft" -> "Draft"
        "revisi" -> "Revisi"
        "diajukan" -> "Diajukan"
        "disetujui" -> "Disetujui"
        "final" -> "Final"
        else -> status.toTitleLabel()
    }
}

private fun penilaianStatusLabel(status: String): String {
    return when (status.lowercase().trim()) {
        "draft" -> "Draft"
        "review" -> "Review"
        "final" -> "Final"
        else -> status.toTitleLabel()
    }
}

private fun String.toTitleLabel(): String {
    return replace("_", " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
        .ifBlank { "-" }
}

private fun formatDateShort(value: String): String {
    return try {
        val datePart = value.take(10)
        val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val output = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        output.format(input.parse(datePart) ?: return datePart)
    } catch (_: Exception) {
        value.take(10)
    }
}
