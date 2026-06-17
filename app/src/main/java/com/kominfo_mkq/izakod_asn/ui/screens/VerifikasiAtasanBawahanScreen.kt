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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.kominfo_mkq.izakod_asn.data.model.AtasanPegawaiUsulanItem
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.ErrorLight
import com.kominfo_mkq.izakod_asn.ui.theme.GradientEndLight
import com.kominfo_mkq.izakod_asn.ui.theme.GradientStartLight
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.SecondaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.viewmodel.AtasanPegawaiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class VerifikasiAtasanBawahanTab(val title: String) {
    WAITING("Menunggu"),
    HISTORY("Riwayat")
}

private data class VerificationDecision(
    val item: AtasanPegawaiUsulanItem,
    val keputusan: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifikasiAtasanBawahanScreen(
    onNavigateBack: () -> Unit,
    viewModel: AtasanPegawaiViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var activeTab by rememberSaveable { mutableStateOf(VerifikasiAtasanBawahanTab.WAITING) }
    var decision by remember { mutableStateOf<VerificationDecision?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadVerificationQueue(context)
    }

    LaunchedEffect(uiState.errorMessage, uiState.actionMessage) {
        val message = uiState.errorMessage ?: uiState.actionMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    val pendingItems = uiState.usulan.filter {
        it.status.equals("diajukan", ignoreCase = true)
    }
    val historyItems = uiState.usulan.filter {
        it.status.equals("disetujui", ignoreCase = true) ||
            it.status.equals("ditolak", ignoreCase = true)
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Verifikasi",
                subtitle = "Usulan atasan-bawahan OPD",
                compact = true,
                onBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.loadVerificationQueue(context, silent = true) },
                        enabled = !uiState.isLoading && !uiState.isRefreshing
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryLight
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            top = 16.dp,
                            end = 20.dp,
                            bottom = 32.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            VerifikasiHeroCard(
                                pendingCount = pendingItems.size,
                                historyCount = historyItems.size
                            )
                        }

                        item {
                            VerifikasiTabSwitcher(
                                activeTab = activeTab,
                                onTabSelected = { activeTab = it },
                                pendingCount = pendingItems.size,
                                historyCount = historyItems.size
                            )
                        }

                        val visibleItems = if (activeTab == VerifikasiAtasanBawahanTab.WAITING) {
                            pendingItems
                        } else {
                            historyItems
                        }

                        if (visibleItems.isEmpty()) {
                            item {
                                VerifikasiEmptyState(
                                    icon = if (activeTab == VerifikasiAtasanBawahanTab.WAITING) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.HourglassTop
                                    },
                                    title = if (activeTab == VerifikasiAtasanBawahanTab.WAITING) {
                                        "Tidak ada usulan menunggu"
                                    } else {
                                        "Belum ada riwayat"
                                    },
                                    message = if (activeTab == VerifikasiAtasanBawahanTab.WAITING) {
                                        "Usulan atasan-bawahan yang perlu dicek akan tampil di sini."
                                    } else {
                                        "Usulan yang sudah disetujui atau ditolak akan tampil di sini."
                                    }
                                )
                            }
                        } else {
                            items(visibleItems, key = { it.id }) { item ->
                                VerifikasiProposalCard(
                                    item = item,
                                    isMutating = uiState.isMutating,
                                    onApprove = {
                                        decision = VerificationDecision(item, "setuju")
                                    },
                                    onReject = {
                                        decision = VerificationDecision(item, "tolak")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    decision?.let { currentDecision ->
        VerificationDecisionDialog(
            decision = currentDecision,
            isMutating = uiState.isMutating,
            onDismiss = { decision = null },
            onConfirm = { note ->
                viewModel.verifyProposal(
                    context = context,
                    item = currentDecision.item,
                    keputusan = currentDecision.keputusan,
                    catatan = note,
                    verificationOnly = true
                )
                decision = null
            }
        )
    }
}

@Composable
private fun VerifikasiHeroCard(
    pendingCount: Int,
    historyCount: Int
) {
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
                            text = "Verifikasi Relasi",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Cek usulan atasan-bawahan sebelum aktif di sistem.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.86f)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(14.dp)
                                .size(30.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VerifikasiHeroChip("$pendingCount", "Menunggu")
                    VerifikasiHeroChip("$historyCount", "Riwayat")
                }
            }
        }
    }
}

@Composable
private fun VerifikasiHeroChip(value: String, label: String) {
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
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.88f)
            )
        }
    }
}

@Composable
private fun VerifikasiTabSwitcher(
    activeTab: VerifikasiAtasanBawahanTab,
    onTabSelected: (VerifikasiAtasanBawahanTab) -> Unit,
    pendingCount: Int,
    historyCount: Int
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
            VerifikasiAtasanBawahanTab.entries.forEach { tab ->
                val selected = tab == activeTab
                val count = if (tab == VerifikasiAtasanBawahanTab.WAITING) {
                    pendingCount
                } else {
                    historyCount
                }
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
                            text = "${tab.title} ($count)",
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
private fun VerifikasiProposalCard(
    item: AtasanPegawaiUsulanItem,
    isMutating: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val statusColor = item.verifikasiStatusColor()
    val alasanPengajuan = item.alasanPengajuan?.takeIf { it.isNotBlank() }
    val diajukanAt = item.diajukanAt?.takeIf { it.isNotBlank() }
    val isWaiting = item.status.equals("diajukan", ignoreCase = true)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
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
                VerifikasiIconBadge(
                    icon = item.verifikasiActionIcon(),
                    color = statusColor,
                    background = statusColor.copy(alpha = 0.12f)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = item.verifikasiActionTitle(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.pegawaiNama?.takeIf { it.isNotBlank() } ?: "Pegawai #${item.pegawaiId}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Atasan: ${item.atasanNama?.takeIf { it.isNotBlank() } ?: "Pegawai #${item.atasanId}"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                VerifikasiStatusPill(text = item.verifikasiStatusLabel(), color = statusColor)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VerifikasiInfoPill(
                        label = "Jenis",
                        value = item.jenisAtasan,
                        modifier = Modifier.weight(1f)
                    )
                    VerifikasiInfoPill(
                        label = "Mulai",
                        value = item.tanggalMulai.toVerifikasiDisplayDate(),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (diajukanAt != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        VerifikasiInfoPill(
                            label = "Diajukan",
                            value = diajukanAt.toVerifikasiDisplayDateTime()
                        )
                    }
                }
            }

            if (alasanPengajuan != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = alasanPengajuan,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!item.catatanVerifikasi.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StatusRejected.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = item.catatanVerifikasi,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (isWaiting) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onApprove,
                        enabled = !isMutating,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusApproved)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Setujui")
                    }
                    FilledTonalButton(
                        onClick = onReject,
                        enabled = !isMutating,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ErrorLight.copy(alpha = 0.12f),
                            contentColor = ErrorLight
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tolak")
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationDecisionDialog(
    decision: VerificationDecision,
    isMutating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var note by rememberSaveable(decision.item.id, decision.keputusan) { mutableStateOf("") }
    val isReject = decision.keputusan == "tolak"
    val canConfirm = !isMutating && (!isReject || note.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (isReject) Icons.Default.Close else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isReject) StatusRejected else StatusApproved
            )
        },
        title = {
            Text(if (isReject) "Tolak usulan?" else "Setujui usulan?")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isReject) {
                        "Usulan ${decision.item.verifikasiActionTitle().lowercase(Locale("id", "ID"))} akan ditolak."
                    } else {
                        "Usulan ${decision.item.verifikasiActionTitle().lowercase(Locale("id", "ID"))} akan disetujui dan diterapkan."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = decision.item.pegawaiNama?.takeIf { it.isNotBlank() } ?: "Pegawai #${decision.item.pegawaiId}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isReject) {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Catatan penolakan") },
                        placeholder = { Text("Contoh: atasan yang dipilih belum sesuai struktur OPD") },
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(note.takeIf { it.isNotBlank() }) },
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReject) StatusRejected else StatusApproved
                )
            ) {
                Text(if (isReject) "Tolak" else "Setujui")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isMutating) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun VerifikasiEmptyState(
    icon: ImageVector,
    title: String,
    message: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            VerifikasiIconBadge(
                icon = icon,
                color = PrimaryLight,
                background = PrimaryLight.copy(alpha = 0.12f)
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
        }
    }
}

@Composable
private fun VerifikasiIconBadge(
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
private fun VerifikasiStatusPill(text: String, color: Color) {
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

@Composable
private fun VerifikasiInfoPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun AtasanPegawaiUsulanItem.verifikasiActionTitle(): String {
    return when (aksi.lowercase(Locale("id", "ID"))) {
        "ubah" -> "Ubah relasi bawahan"
        "nonaktif" -> "Nonaktifkan bawahan"
        else -> "Tambah bawahan"
    }
}

private fun AtasanPegawaiUsulanItem.verifikasiActionIcon(): ImageVector {
    return when (aksi.lowercase(Locale("id", "ID"))) {
        "ubah" -> Icons.Default.Edit
        "nonaktif" -> Icons.Default.PersonOff
        else -> Icons.Default.PersonAdd
    }
}

private fun AtasanPegawaiUsulanItem.verifikasiStatusLabel(): String {
    return when (status.lowercase(Locale("id", "ID"))) {
        "draft" -> "Draft"
        "diajukan" -> "Diajukan"
        "disetujui" -> "Disetujui"
        "ditolak" -> "Ditolak"
        "dibatalkan" -> "Dibatalkan"
        else -> status
    }
}

private fun AtasanPegawaiUsulanItem.verifikasiStatusColor(): Color {
    return when (status.lowercase(Locale("id", "ID"))) {
        "draft" -> SecondaryLight
        "diajukan" -> StatusPending
        "disetujui" -> StatusApproved
        "ditolak" -> StatusRejected
        "dibatalkan" -> StatusRevised
        else -> PrimaryLight
    }
}

private fun String?.toVerifikasiDisplayDate(): String {
    val raw = this?.trim()?.takeIf { it.isNotBlank() } ?: return "-"
    val parsed = raw.parseVerifikasiApiDate() ?: return raw
    return SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")).format(parsed)
}

private fun String?.toVerifikasiDisplayDateTime(): String {
    val raw = this?.trim()?.takeIf { it.isNotBlank() } ?: return "-"
    val parsed = raw.parseVerifikasiApiDate() ?: return raw
    return SimpleDateFormat("d MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(parsed)
}

private fun String.parseVerifikasiApiDate(): Date? {
    val raw = trim()
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    for (pattern in patterns) {
        val formatter = SimpleDateFormat(pattern, Locale.US).apply {
            isLenient = false
            if (pattern.endsWith("'Z'")) {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
        }
        val parsed = runCatching { formatter.parse(raw) }.getOrNull()
        if (parsed != null) return parsed
    }

    return null
}
