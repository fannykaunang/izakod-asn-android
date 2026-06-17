package com.kominfo_mkq.izakod_asn.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.local.UserPreferences
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaHistoryItem
import com.kominfo_mkq.izakod_asn.data.model.RealisasiKinerjaItem
import com.kominfo_mkq.izakod_asn.data.model.RealisasiLinkedLaporanItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaDetailItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaDetailPayload
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaHistoryItem
import com.kominfo_mkq.izakod_asn.data.model.TargetKinerjaItem
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TargetKinerjaDetailViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TargetKinerjaFormViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TargetKinerjaListViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.isEditableBy
import kotlinx.coroutines.launch

private enum class TargetListMode { MINE, SUBORDINATE }

private enum class TargetStatusFilter(val label: String, val raw: String?) {
    ALL("Semua", null),
    DRAFT("Draft", "draft"),
    DIAJUKAN("Diajukan", "diajukan"),
    DISETUJUI("Disetujui", "disetujui"),
    REVISI("Revisi", "revisi"),
    FINAL("Final", "final")
}

private fun TargetKinerjaItem.isSubmittedForReview(): Boolean =
    status.trim().equals("diajukan", ignoreCase = true)

private data class TargetDetailFormState(
    val id: Int? = null,
    val uraianTarget: String = "",
    val indikator: String = "",
    val satuan: String = "",
    val targetKuantitas: String = "",
    val targetKualitas: String = "",
    val targetWaktu: String = "",
    val bobot: String = ""
)

private fun emptyTargetDetailFormState(): TargetDetailFormState = TargetDetailFormState()

private fun formatEditableNumber(value: Double?): String {
    if (value == null) return ""
    return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
}

private fun TargetKinerjaDetailItem.toFormState(): TargetDetailFormState = TargetDetailFormState(
    id = id,
    uraianTarget = uraianTarget,
    indikator = indikator.orEmpty(),
    satuan = satuan.orEmpty(),
    targetKuantitas = formatEditableNumber(targetKuantitas),
    targetKualitas = formatEditableNumber(targetKualitas),
    targetWaktu = formatEditableNumber(targetWaktu),
    bobot = formatEditableNumber(bobot)
)

private fun TargetDetailFormState.toPayload(): TargetKinerjaDetailPayload = TargetKinerjaDetailPayload(
    id = id,
    uraianTarget = uraianTarget.trim(),
    indikator = indikator.trim().ifBlank { null },
    satuan = satuan.trim().ifBlank { null },
    targetKuantitas = targetKuantitas.trim().ifBlank { null }?.toDoubleOrNull(),
    targetKualitas = targetKualitas.trim().ifBlank { null }?.toDoubleOrNull(),
    targetWaktu = targetWaktu.trim().ifBlank { null }?.toDoubleOrNull(),
    bobot = bobot.trim().ifBlank { null }?.toDoubleOrNull()
)

private fun formatPeriodeTarget(bulan: Int, tahun: Int): String {
    return java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("id", "ID"))
        .format(java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, tahun)
            set(java.util.Calendar.MONTH, bulan - 1)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }.time)
}

private fun shiftTargetMonth(month: Int, year: Int, delta: Int): Pair<Int, Int> {
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, year)
        set(java.util.Calendar.MONTH, month - 1)
        add(java.util.Calendar.MONTH, delta)
    }

    return (calendar.get(java.util.Calendar.MONTH) + 1) to calendar.get(java.util.Calendar.YEAR)
}

private fun formatNullableDouble(value: Double?): String {
    return if (value == null) "-" else {
        java.text.DecimalFormat("#.##").format(value)
    }
}

private data class RealisasiFormState(
    val realisasiKuantitas: String = "",
    val realisasiKualitas: String = "",
    val realisasiWaktu: String = "",
    val catatan: String = ""
)

private fun RealisasiKinerjaItem.toFormState(): RealisasiFormState = RealisasiFormState(
    realisasiKuantitas = formatEditableNumber(realisasiKuantitas),
    realisasiKualitas = formatEditableNumber(realisasiKualitas),
    realisasiWaktu = formatEditableNumber(realisasiWaktu),
    catatan = catatan.orEmpty()
)

private fun hasFilledRealisasi(item: RealisasiKinerjaItem?): Boolean {
    if (item == null) return false

    return listOf(
        item.realisasiKuantitas,
        item.realisasiKualitas,
        item.realisasiWaktu,
        item.catatan?.takeIf { it.isNotBlank() }
    ).any { it != null }
}

private fun formatTargetTanggalShort(value: String): String {
    return try {
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("id", "ID")).format(
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(value)
                ?: return value
        )
    } catch (_: Exception) {
        value
    }
}

private fun formatTargetRentangWaktu(waktuMulai: String?, waktuSelesai: String?): String {
    val mulai = waktuMulai?.take(5)
    val selesai = waktuSelesai?.take(5)
    return when {
        !mulai.isNullOrBlank() && !selesai.isNullOrBlank() -> "$mulai - $selesai"
        !mulai.isNullOrBlank() -> mulai
        else -> "-"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetKinerjaListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int, Boolean) -> Unit,
    onNavigateToCreate: (Int, Int) -> Unit,
    viewModel: TargetKinerjaListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val now = remember { java.util.Calendar.getInstance() }
    var selectedMonth by remember { mutableStateOf(now.get(java.util.Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(now.get(java.util.Calendar.YEAR)) }
    var listMode by remember { mutableStateOf(TargetListMode.MINE) }
    var statusFilter by remember { mutableStateOf(TargetStatusFilter.ALL) }

    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.refresh(tahun = selectedYear, bulan = selectedMonth)
    }

    val mineTargets = remember(uiState.targets, uiState.currentPegawaiId) {
        uiState.targets.filter { it.pegawaiId == uiState.currentPegawaiId }
    }
    val subordinateTargets = remember(uiState.targets, uiState.currentPegawaiId) {
        uiState.targets.filter { it.pegawaiId != uiState.currentPegawaiId }
    }
    val submittedSubordinateCount = remember(subordinateTargets) {
        subordinateTargets.count { it.isSubmittedForReview() }
    }

    val activeSource = if (listMode == TargetListMode.MINE) mineTargets else subordinateTargets
    val filteredTargets = activeSource.filter { target ->
        statusFilter.raw == null || target.status == statusFilter.raw
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Target Kinerja",
                compact = true,
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.refresh(tahun = selectedYear, bulan = selectedMonth) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            if (listMode == TargetListMode.MINE) {
                FloatingActionButton(
                    onClick = { onNavigateToCreate(selectedYear, selectedMonth) },
                    containerColor = PrimaryLight,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Buat target")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Kelola target bulanan dan pantau status persetujuannya.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (uiState.canReviewSubordinates) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = listMode == TargetListMode.MINE,
                                onClick = { listMode = TargetListMode.MINE },
                                label = { Text("Target Saya") }
                            )
                            ReviewBawahanFilterChip(
                                selected = listMode == TargetListMode.SUBORDINATE,
                                onClick = { listMode = TargetListMode.SUBORDINATE },
                                count = submittedSubordinateCount
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val (month, year) = shiftTargetMonth(selectedMonth, selectedYear, -1)
                                selectedMonth = month
                                selectedYear = year
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Periode sebelumnya"
                            )
                        }

                        Text(
                            text = formatPeriodeTarget(selectedMonth, selectedYear),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        IconButton(
                            onClick = {
                                val (month, year) = shiftTargetMonth(selectedMonth, selectedYear, 1)
                                selectedMonth = month
                                selectedYear = year
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Periode berikutnya"
                            )
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(TargetStatusFilter.entries) { filter ->
                    FilterChip(
                        selected = statusFilter == filter,
                        onClick = { statusFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.isError -> {
                    SimpleStateCard(
                        icon = Icons.Default.Warning,
                        title = "Gagal memuat target",
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        actionText = "Coba Lagi",
                        onAction = { viewModel.refresh(tahun = selectedYear, bulan = selectedMonth) }
                    )
                }

                filteredTargets.isEmpty() -> {
                    TargetEmptyState(
                        icon = if (listMode == TargetListMode.MINE) Icons.Outlined.Description else Icons.Default.Groups,
                        message = if (listMode == TargetListMode.MINE) {
                            "Belum ada target"
                        } else {
                            "Belum ada target bawahan"
                        },
                        actionText = if (listMode == TargetListMode.MINE) "Buat Target" else null,
                        onAction = if (listMode == TargetListMode.MINE) {
                            { onNavigateToCreate(selectedYear, selectedMonth) }
                        } else {
                            null
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTargets, key = { it.id }) { target ->
                            TargetKinerjaCard(
                                target = target,
                                showPegawaiName = listMode == TargetListMode.SUBORDINATE,
                                onClick = {
                                    onNavigateToDetail(target.id, listMode == TargetListMode.SUBORDINATE)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewBawahanFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    count: Int
) {
    Box(contentAlignment = Alignment.TopEnd) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text("Review Bawahan") }
        )
        ReviewBawahanBadge(count = count)
    }
}

@Composable
private fun ReviewBawahanBadge(count: Int) {
    if (count <= 0) return

    Badge(
        modifier = Modifier.offset(x = 6.dp, y = (-6).dp),
        containerColor = StatusRejected,
        contentColor = Color.White
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
fun TargetKinerjaSubordinatePeriodScreen(
    tahun: Int,
    bulan: Int,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int, Boolean) -> Unit,
    viewModel: TargetKinerjaListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var statusFilter by remember { mutableStateOf(TargetStatusFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    val periodLabel = remember(tahun, bulan) { formatPeriodeTarget(bulan, tahun) }

    LaunchedEffect(tahun, bulan) {
        viewModel.refresh(tahun = tahun, bulan = bulan)
    }

    val subordinateTargets = remember(uiState.targets, uiState.currentPegawaiId) {
        if (uiState.currentPegawaiId != null) {
            uiState.targets.filter { it.pegawaiId != uiState.currentPegawaiId }
        } else {
            emptyList()
        }
    }
    val filteredTargets = remember(subordinateTargets, statusFilter, searchQuery) {
        subordinateTargets.filter { target ->
            val matchesStatus = statusFilter.raw == null ||
                target.status.equals(statusFilter.raw, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                target.pegawaiNama.orEmpty().contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesSearch
        }.sortedBy { it.pegawaiNama.orEmpty() }
    }

    Scaffold(
        topBar = {
            TargetBawahanPeriodTopBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onToggleSearch = { isSearchActive = true },
                onCloseSearch = {
                    isSearchActive = false
                    searchQuery = ""
                },
                onRefresh = { viewModel.refresh(tahun = tahun, bulan = bulan) },
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "${subordinateTargets.size} target bawahan pada periode ini",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(TargetStatusFilter.entries) { filter ->
                    FilterChip(
                        selected = statusFilter == filter,
                        onClick = { statusFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.isError -> {
                    SimpleStateCard(
                        icon = Icons.Default.Warning,
                        title = "Gagal memuat target bawahan",
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        actionText = "Coba Lagi",
                        onAction = { viewModel.refresh(tahun = tahun, bulan = bulan) }
                    )
                }

                filteredTargets.isEmpty() -> {
                    TargetEmptyState(
                        icon = Icons.Default.Groups,
                        message = if (subordinateTargets.isEmpty()) {
                            "Belum ada target bawahan"
                        } else {
                            "Tidak ada hasil"
                        },
                        actionText = null,
                        onAction = null
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTargets, key = { it.id }) { target ->
                            TargetKinerjaCard(
                                target = target,
                                showPegawaiName = true,
                                onClick = {
                                    onNavigateToDetail(target.id, true)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TargetBawahanPeriodTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    placeholder = {
                        Text(
                            text = "Cari nama bawahan...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            } else {
                Text(
                    text = "Target Bawahan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
        },
        actions = {
            IconButton(
                onClick = if (isSearchActive) onCloseSearch else onToggleSearch
            ) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchActive) "Tutup pencarian" else "Cari bawahan"
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetKinerjaDetailScreen(
    targetId: Int,
    reviewMode: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onDeleted: () -> Unit,
    viewModel: TargetKinerjaDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionData = remember { UserPreferences(context).getSessionData() }
    val currentPegawaiId = sessionData?.pegawaiId
    val currentUserLevel = sessionData?.level ?: 0
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var reviewNote by remember { mutableStateOf("") }
    var laporanPickerDetailId by remember { mutableStateOf<Int?>(null) }
    var realisasiDrafts by remember { mutableStateOf<Map<Int, RealisasiFormState>>(emptyMap()) }

    LaunchedEffect(targetId) {
        viewModel.loadTarget(targetId)
    }

    LaunchedEffect(uiState.target?.catatanAtasan) {
        reviewNote = uiState.target?.catatanAtasan.orEmpty()
    }

    val target = uiState.target
    val isAssessmentFinalized = target?.isAssessmentFinalized == true
    val canEdit = target?.isEditableBy(currentPegawaiId) == true && !isAssessmentFinalized
    val isAdmin = currentUserLevel >= 3
    val isOwner = target?.pegawaiId == currentPegawaiId
    val canManageRealisasi = target != null &&
        (isOwner == true || isAdmin) &&
        (target.status == "disetujui" || target.status == "final") &&
        !isAssessmentFinalized
    val realisasiByDetailId = remember(uiState.realisasiItems) {
        uiState.realisasiItems.associateBy { it.targetKinerjaDetailId }
    }
    val linkedLaporanByDetailId = uiState.linkedLaporanByDetailId
    val candidateLaporanByDetailId = remember(
        target?.details,
        uiState.candidateLaporan,
        linkedLaporanByDetailId
    ) {
        (target?.details ?: emptyList()).associate { detail ->
            val linkedIds = linkedLaporanByDetailId[detail.id ?: -1]
                ?.map { it.laporanId }
                ?.toSet()
                ?: emptySet()
            (detail.id ?: -1) to uiState.candidateLaporan.filter { laporan ->
                laporan.laporanId !in linkedIds
            }
        }
    }

    LaunchedEffect(target?.details, uiState.realisasiItems) {
        val details = target?.details ?: emptyList()
        if (details.isEmpty()) return@LaunchedEffect

        realisasiDrafts = buildMap {
            details.forEach { detail ->
                val detailId = detail.id ?: return@forEach
                put(detailId, realisasiDrafts[detailId] ?: realisasiByDetailId[detailId]?.toFormState() ?: RealisasiFormState())
            }
        }
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Detail Target",
                compact = true,
                onBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.isError || target == null -> {
                SimpleStateCard(
                    icon = Icons.Default.Warning,
                    title = "Target tidak ditemukan",
                    message = uiState.errorMessage ?: "Gagal memuat detail target",
                    actionText = "Coba Lagi",
                    onAction = { viewModel.loadTarget(targetId) }
                )
            }

            else -> {
                val totalDetails = target.details.size
                val filledCount = target.details.count { detail ->
                    hasFilledRealisasi(realisasiByDetailId[detail.id ?: -1])
                }
                val linkedCount = linkedLaporanByDetailId.values.sumOf { it.size }
                val totalWeight = target.details.sumOf { it.bobot ?: 0.0 }
                val isRealisasiComplete = totalDetails > 0 && filledCount == totalDetails

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        TargetDetailHeroCard(
                            target = target,
                            isAssessmentFinalized = isAssessmentFinalized
                        )
                    }

                    if (isAssessmentFinalized) {
                        item {
                            LockedPeriodBanner()
                        }
                    }

                    item {
                        TargetDetailNoteCard(
                            title = "Catatan Pegawai",
                            body = target.catatanPegawai?.takeIf { it.isNotBlank() }
                                ?: "Belum ada catatan pegawai."
                        )
                    }

                    item {
                        TargetDetailProgressCard(
                            totalItems = totalDetails,
                            filledItems = filledCount,
                            linkedReports = linkedCount,
                            totalWeight = totalWeight,
                            isRefreshing = uiState.isRefreshingRealisasi
                        )
                    }

                    item {
                        TargetDetailFlowCard(
                            hasTarget = totalDetails > 0,
                            hasRealisasi = isRealisasiComplete,
                            hasLinkedLaporan = linkedCount > 0,
                            isFinal = isAssessmentFinalized
                        )
                    }

                    item {
                        Text(
                            text = "Item Target",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    items(target.details, key = { it.id ?: it.uraianTarget.hashCode() }) { detail ->
                        val detailId = detail.id ?: -1
                        TargetDetailItemCardModern(
                            detail = detail,
                            realisasi = realisasiByDetailId[detailId],
                            linkedLaporan = linkedLaporanByDetailId[detailId].orEmpty(),
                            candidateLaporan = candidateLaporanByDetailId[detailId].orEmpty(),
                            draft = realisasiDrafts[detailId] ?: RealisasiFormState(),
                            canManageRealisasi = canManageRealisasi,
                            isAssessmentFinalized = isAssessmentFinalized,
                            showRealisasiSection = canManageRealisasi || reviewMode || hasFilledRealisasi(realisasiByDetailId[detailId]) || linkedLaporanByDetailId[detailId].orEmpty().isNotEmpty(),
                            realisasiHistory = uiState.realisasiHistoryById[realisasiByDetailId[detailId]?.id ?: -1].orEmpty(),
                            isSaving = uiState.savingRealisasiDetailId == detailId,
                            isLinking = uiState.linkingDetailId == detailId,
                            unlinkingKey = uiState.unlinkingKey,
                            onDraftChange = { next ->
                                realisasiDrafts = realisasiDrafts.toMutableMap().apply {
                                    put(detailId, next)
                                }
                            },
                            onSaveRealisasi = {
                                viewModel.saveRealisasi(
                                    detailId = detailId,
                                    realisasiKuantitas = realisasiDrafts[detailId]?.realisasiKuantitas?.takeIf { it.isNotBlank() }?.toDoubleOrNull(),
                                    realisasiKualitas = realisasiDrafts[detailId]?.realisasiKualitas?.takeIf { it.isNotBlank() }?.toDoubleOrNull(),
                                    realisasiWaktu = realisasiDrafts[detailId]?.realisasiWaktu?.takeIf { it.isNotBlank() }?.toDoubleOrNull(),
                                    catatan = realisasiDrafts[detailId]?.catatan?.ifBlank { null },
                                    onSuccess = { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    },
                                    onError = { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    }
                                )
                            },
                            onOpenLaporanPicker = { laporanPickerDetailId = detailId },
                            onUnlinkLaporan = { laporanId ->
                                viewModel.unlinkLaporan(
                                    detailId = detailId,
                                    laporanId = laporanId,
                                    onSuccess = { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    },
                                    onError = { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    }
                                )
                            }
                        )
                    }

                    item {
                        TargetDetailNoteCard(
                            title = "Catatan Atasan",
                            body = target.catatanAtasan?.takeIf { it.isNotBlank() }
                                ?: "Belum ada catatan atasan."
                        )
                    }

                    item {
                        TargetHistorySection(history = uiState.targetHistory)
                    }

                    if (canEdit) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToEdit(target.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit")
                                }
                                Button(
                                    onClick = {
                                        viewModel.submitTarget(
                                            target.id,
                                            onSuccess = {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Target berhasil diajukan")
                                                }
                                            },
                                            onError = { message ->
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(message)
                                                }
                                            }
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ajukan")
                                }
                            }
                        }

                        item {
                            TextButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hapus Target")
                            }
                        }
                    }

                    if (reviewMode && !isAssessmentFinalized) {
                        item {
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Panel Review",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )

                                    OutlinedTextField(
                                        value = reviewNote,
                                        onValueChange = { reviewNote = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 4,
                                        label = { Text("Catatan Atasan") },
                                        placeholder = { Text("Contoh: Target disetujui, lanjutkan pelaksanaan.") }
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (target.status == "diajukan") {
                                            Button(
                                                onClick = {
                                                    viewModel.reviewTarget(
                                                        target.id,
                                                        aksi = "setujui",
                                                        catatanAtasan = reviewNote,
                                                        onSuccess = {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Target berhasil disetujui")
                                                            }
                                                        },
                                                        onError = { message ->
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(message)
                                                            }
                                                        }
                                                    )
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Setujui")
                                            }
                                            TextButton(
                                                onClick = {
                                                    viewModel.reviewTarget(
                                                        target.id,
                                                        aksi = "revisi",
                                                        catatanAtasan = reviewNote,
                                                        onSuccess = {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Revisi berhasil dikirim")
                                                            }
                                                        },
                                                        onError = { message ->
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(message)
                                                            }
                                                        }
                                                    )
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Revisi")
                                            }
                                        }

                                        if (target.status == "disetujui") {
                                            Button(
                                                onClick = {
                                                    viewModel.reviewTarget(
                                                        target.id,
                                                        aksi = "final",
                                                        catatanAtasan = reviewNote,
                                                        onSuccess = {
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("Target berhasil difinalkan")
                                                            }
                                                        },
                                                        onError = { message ->
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar(message)
                                                            }
                                                        }
                                                    )
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Finalkan")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && target != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus target?") },
            text = { Text("Target ${formatPeriodeTarget(target.bulan, target.tahun)} akan dihapus.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteTarget(
                            target.id,
                            onSuccess = onDeleted,
                            onError = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (laporanPickerDetailId != null && target != null) {
        val detailId = laporanPickerDetailId ?: -1
        val candidateLaporan = candidateLaporanByDetailId[detailId].orEmpty()

        AlertDialog(
            onDismissRequest = { laporanPickerDetailId = null },
            title = { Text("Pilih Laporan Pendukung") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (candidateLaporan.isEmpty()) {
                            "Tidak ada laporan kegiatan lain yang bisa ditautkan untuk item ini."
                        } else {
                            "Pilih salah satu laporan kegiatan pada periode yang sama untuk ditautkan ke realisasi."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )

                    candidateLaporan.forEach { laporan ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = uiState.linkingDetailId != detailId) {
                                    viewModel.linkLaporan(
                                        detailId = detailId,
                                        laporanId = laporan.laporanId,
                                        onSuccess = { message ->
                                            laporanPickerDetailId = null
                                            scope.launch { snackbarHostState.showSnackbar(message) }
                                        },
                                        onError = { message ->
                                            scope.launch { snackbarHostState.showSnackbar(message) }
                                        }
                                    )
                                },
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = laporan.namaKegiatan,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${formatTargetTanggalShort(laporan.tanggalKegiatan)} • ${formatTargetRentangWaktu(laporan.waktuMulai, laporan.waktuSelesai)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = laporan.kategoriNama ?: "Tanpa kategori",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { laporanPickerDetailId = null }) {
                    Text("Tutup")
                }
            },
            dismissButton = null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetKinerjaFormScreen(
    targetId: Int?,
    initialTahun: Int? = null,
    initialBulan: Int? = null,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    viewModel: TargetKinerjaFormViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditMode = targetId != null
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var tahun by remember {
        mutableStateOf(initialTahun ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
    }
    var bulan by remember {
        mutableStateOf(initialBulan ?: java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1)
    }
    var catatanPegawai by remember { mutableStateOf("") }
    var showPeriodDialog by remember { mutableStateOf(false) }
    val detailItems = remember { mutableStateListOf(emptyTargetDetailFormState()) }

    val submitTarget: () -> Unit = {
        val validDetails = detailItems
            .map { it.toPayload() }
            .filter { it.uraianTarget.isNotBlank() }

        when {
            tahun !in 2020..2100 -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Tahun target tidak valid.")
                }
            }

            bulan !in 1..12 -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Bulan target tidak valid.")
                }
            }

            validDetails.isEmpty() -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Isi minimal 1 uraian target.")
                }
            }

            targetId == null -> {
                viewModel.createTarget(
                    tahun = tahun,
                    bulan = bulan,
                    catatanPegawai = catatanPegawai,
                    details = validDetails,
                    onSuccess = { createdId ->
                        onNavigateToDetail(createdId)
                    },
                    onError = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }

            else -> {
                viewModel.updateTarget(
                    targetId = targetId,
                    tahun = tahun,
                    bulan = bulan,
                    catatanPegawai = catatanPegawai,
                    details = validDetails,
                    onSuccess = {
                        onNavigateToDetail(targetId)
                    },
                    onError = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(targetId) {
        if (targetId != null) {
            viewModel.loadTarget(targetId)
        }
    }

    LaunchedEffect(uiState.target?.id) {
        uiState.target?.let { target ->
            tahun = target.tahun
            bulan = target.bulan
            catatanPegawai = target.catatanPegawai.orEmpty()
            detailItems.clear()
            if (target.details.isNotEmpty()) {
                detailItems.addAll(target.details.map { it.toFormState() })
            } else {
                detailItems.add(emptyTargetDetailFormState())
            }
        }
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = if (isEditMode) "Edit Target" else "Buat Target",
                compact = true,
                onBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!uiState.isLoading && !uiState.isError) {
                TargetFormBottomAction(
                    isSaving = uiState.isSaving,
                    label = if (isEditMode) "Simpan Perubahan" else "Simpan & Lihat Detail",
                    onClick = submitTarget
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.isError -> {
                SimpleStateCard(
                    icon = Icons.Default.Warning,
                    title = "Gagal memuat target",
                    message = uiState.errorMessage ?: "Terjadi kesalahan",
                    actionText = "Kembali",
                    onAction = onNavigateBack
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        TargetFormHeroCard(
                            title = if (isEditMode) "Edit Target ${formatPeriodeTarget(bulan, tahun)}" else "Target ${formatPeriodeTarget(bulan, tahun)}",
                            subtitle = if (isEditMode) {
                                "Perbarui item target sebelum diajukan atau direview."
                            } else {
                                "Simpan dulu, lalu ajukan ke atasan setelah semua item lengkap."
                            },
                            onPeriodClick = { showPeriodDialog = true }
                        )
                    }

                    item {
                        TargetFormGuidanceCard()
                    }

                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Catatan Pegawai",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = "Tambahkan konteks singkat agar atasan memahami arah target bulan ini.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = catatanPegawai,
                                    onValueChange = { catatanPegawai = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 4,
                                    label = { Text("Catatan") },
                                    placeholder = { Text("Contoh: Target pelayanan dan pelaporan bulan ${formatPeriodeTarget(bulan, tahun).substringBefore(' ')}.") }
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Item Target",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            TextButton(
                                onClick = { detailItems.add(emptyTargetDetailFormState()) }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tambah Item")
                            }
                        }
                    }

                    items(detailItems.size) { index ->
                        TargetDetailEditorCard(
                            index = index,
                            state = detailItems[index],
                            onChange = { detailItems[index] = it },
                            canRemove = detailItems.size > 1,
                            onRemove = {
                                if (detailItems.size > 1) {
                                    detailItems.removeAt(index)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPeriodDialog) {
        TargetPeriodPickerDialog(
            selectedTahun = tahun,
            selectedBulan = bulan,
            onDismiss = { showPeriodDialog = false },
            onConfirm = { selectedTahun, selectedBulan ->
                tahun = selectedTahun
                bulan = selectedBulan
                showPeriodDialog = false
            }
        )
    }
}

@Composable
private fun TargetFormHeroCard(
    title: String,
    subtitle: String,
    onPeriodClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                TargetSoftPill(text = "Draft", color = StatusPending)
            }

            Surface(
                modifier = Modifier.clickable(onClick = onPeriodClick),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Ubah Periode",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetFormGuidanceCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(16.dp),
                color = StatusApproved.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = StatusApproved,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    text = "Isi target yang jelas, terukur, dan mudah direalisasikan.",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = "Gunakan angka sederhana agar atasan mudah menilai progres bulanan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { TargetSoftPill(text = "Kuantitas", color = MaterialTheme.colorScheme.primary) }
                    item { TargetSoftPill(text = "Kualitas", color = StatusApproved) }
                    item { TargetSoftPill(text = "Waktu", color = StatusPending) }
                }
            }
        }
    }
}

@Composable
private fun TargetPeriodPickerDialog(
    selectedTahun: Int,
    selectedBulan: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var draftTahun by remember(selectedTahun) { mutableStateOf(selectedTahun.toString()) }
    var draftBulan by remember(selectedBulan) { mutableStateOf(selectedBulan.coerceIn(1, 12)) }
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )
    val rows = months.chunked(3)
    val parsedYear = draftTahun.toIntOrNull()
    val isYearValid = parsedYear != null && parsedYear in 2020..2100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Periode") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                rows.forEachIndexed { rowIndex, rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMonths.forEachIndexed { columnIndex, label ->
                            val monthNumber = rowIndex * 3 + columnIndex + 1
                            FilterChip(
                                selected = draftBulan == monthNumber,
                                onClick = { draftBulan = monthNumber },
                                label = {
                                    Text(
                                        text = label,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = draftTahun,
                    onValueChange = { input ->
                        draftTahun = input.filter { it.isDigit() }.take(4)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tahun") },
                    placeholder = { Text("Contoh: 2026") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = draftTahun.isNotBlank() && !isYearValid,
                    supportingText = {
                        Text(
                            text = if (isYearValid) {
                                "Periode akan digunakan untuk target yang dibuat."
                            } else {
                                "Isi tahun 2020 sampai 2100."
                            }
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isYearValid,
                onClick = { onConfirm(parsedYear ?: selectedTahun, draftBulan) }
            ) {
                Text("Terapkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun TargetFormBottomAction(
    isSaving: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 8.dp
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            enabled = !isSaving,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isSaving) "Menyimpan..." else label)
        }
    }
}

@Composable
private fun TargetKinerjaCard(
    target: TargetKinerjaItem,
    showPegawaiName: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (showPegawaiName) {
                        Text(
                            text = target.pegawaiNama ?: "Pegawai #${target.pegawaiId}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = formatPeriodeTarget(target.bulan, target.tahun),
                        style = if (showPegawaiName) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                StatusBadgeTarget(status = target.status)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoMiniChip(
                    icon = Icons.Default.CalendarToday,
                    text = "${target.detailCount ?: target.details.size} item"
                )
                if (!target.approverNama.isNullOrBlank()) {
                    InfoMiniChip(
                        icon = Icons.Default.TaskAlt,
                        text = target.approverNama!!
                    )
                }
            }

            Text(
                text = target.catatanPegawai?.ifBlank { "Belum ada catatan pegawai." }
                    ?: "Belum ada catatan pegawai.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TargetDetailHeroCard(
    target: TargetKinerjaItem,
    isAssessmentFinalized: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        text = target.pegawaiNama ?: "Pegawai #${target.pegawaiId}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatPeriodeTarget(target.bulan, target.tahun),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                StatusBadgeTarget(status = target.status)
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    InfoMiniChip(
                        icon = Icons.Outlined.Description,
                        text = "${target.detailCount ?: target.details.size} item"
                    )
                }
                if (!target.approverNama.isNullOrBlank()) {
                    item {
                        InfoMiniChip(
                            icon = Icons.Default.TaskAlt,
                            text = "Reviewer: ${target.approverNama}"
                        )
                    }
                }
                if (isAssessmentFinalized) {
                    item {
                        InfoMiniChip(
                            icon = Icons.Default.Warning,
                            text = "Periode final"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TargetDetailProgressCard(
    totalItems: Int,
    filledItems: Int,
    linkedReports: Int,
    totalWeight: Double,
    isRefreshing: Boolean
) {
    val progress = if (totalItems == 0) 0.0 else (filledItems.toDouble() / totalItems.toDouble()) * 100.0
    val statusText = when {
        totalItems == 0 -> "Belum ada item"
        filledItems == totalItems -> "Lengkap"
        else -> "Belum lengkap"
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TargetProgressCircle(progress = progress)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Progress Realisasi",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = if (totalItems == 0) {
                            "Belum ada item target untuk periode ini."
                        } else {
                            "$filledItems dari $totalItems item sudah memiliki realisasi."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TargetSoftPill(
                        text = statusText,
                        color = if (filledItems == totalItems && totalItems > 0) StatusApproved else StatusPending
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoBoxTarget(
                    label = "Item terisi",
                    value = "$filledItems/$totalItems",
                    modifier = Modifier.weight(1f)
                )
                InfoBoxTarget(
                    label = "Laporan tertaut",
                    value = linkedReports.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoBoxTarget(
                    label = "Status",
                    value = statusText,
                    modifier = Modifier.weight(1f)
                )
                InfoBoxTarget(
                    label = "Total bobot",
                    value = "${formatNullableDouble(totalWeight)}%",
                    modifier = Modifier.weight(1f)
                )
            }

            if (isRefreshing) {
                Text(
                    text = "Memperbarui data realisasi...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TargetProgressCircle(progress: Double) {
    val progressFraction = (progress / 100.0).toFloat().coerceIn(0f, 1f)
    val ringColor = if (progress >= 100.0) StatusApproved else StatusPending

    Box(
        modifier = Modifier.size(112.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxSize(),
            color = ringColor,
            trackColor = ringColor.copy(alpha = 0.16f),
            strokeWidth = 10.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${progress.toInt()}%",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Progress",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TargetDetailFlowCard(
    hasTarget: Boolean,
    hasRealisasi: Boolean,
    hasLinkedLaporan: Boolean,
    isFinal: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Alur Target Bulan Ini",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item { TargetFlowStep("Target", if (hasTarget) "Sudah dibuat" else "Belum ada", hasTarget) }
                item { TargetFlowStep("Realisasi", if (hasRealisasi) "Terisi" else "Perlu diisi", hasRealisasi) }
                item { TargetFlowStep("Laporan", if (hasLinkedLaporan) "Tertaut" else "Opsional", hasLinkedLaporan) }
                item { TargetFlowStep("Final", if (isFinal) "Terkunci" else "Belum final", isFinal) }
            }
        }
    }
}

@Composable
private fun TargetFlowStep(
    title: String,
    subtitle: String,
    completed: Boolean
) {
    val color = if (completed) StatusApproved else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = if (completed) 0.14f else 0.08f),
        modifier = Modifier.width(132.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = RoundedCornerShape(50),
                color = color.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (completed) Icons.Default.TaskAlt else Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = color
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TargetDetailNoteCard(
    title: String,
    body: String
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TargetDetailItemCardModern(
    detail: TargetKinerjaDetailItem,
    realisasi: RealisasiKinerjaItem?,
    linkedLaporan: List<RealisasiLinkedLaporanItem>,
    candidateLaporan: List<LaporanKegiatan>,
    draft: RealisasiFormState,
    canManageRealisasi: Boolean,
    isAssessmentFinalized: Boolean,
    showRealisasiSection: Boolean,
    realisasiHistory: List<RealisasiKinerjaHistoryItem>,
    isSaving: Boolean,
    isLinking: Boolean,
    unlinkingKey: String?,
    onDraftChange: (RealisasiFormState) -> Unit,
    onSaveRealisasi: () -> Unit,
    onOpenLaporanPicker: () -> Unit,
    onUnlinkLaporan: (Int) -> Unit
) {
    val isFilled = hasFilledRealisasi(realisasi)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = detail.uraianTarget,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                Spacer(modifier = Modifier.width(10.dp))
                TargetSoftPill(
                    text = if (isFilled) "Realisasi terisi" else "Belum terisi",
                    color = if (isFilled) StatusApproved else StatusPending
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                item { InfoMiniChip(icon = Icons.Outlined.Description, text = detail.indikator ?: "-") }
                item { InfoMiniChip(icon = Icons.Default.Flag, text = "Bobot ${formatNullableDouble(detail.bobot)}%") }
            }

            HorizontalDivider()

            if (showRealisasiSection) {
                TargetRealityCompareGrid(
                    detail = detail,
                    realisasi = realisasi,
                    draft = draft
                )

                if (canManageRealisasi) {
                    Text(
                        text = "Isi Realisasi",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = draft.realisasiKuantitas,
                        onValueChange = { onDraftChange(draft.copy(realisasiKuantitas = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Realisasi Kuantitas") },
                        placeholder = { Text("Contoh: 12 dokumen") },
                        enabled = !isSaving
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = draft.realisasiKualitas,
                            onValueChange = { onDraftChange(draft.copy(realisasiKualitas = it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Kualitas") },
                            placeholder = { Text("Contoh: 90%") },
                            enabled = !isSaving
                        )
                        OutlinedTextField(
                            value = draft.realisasiWaktu,
                            onValueChange = { onDraftChange(draft.copy(realisasiWaktu = it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Waktu") },
                            placeholder = { Text("Contoh: 30 hari") },
                            enabled = !isSaving
                        )
                    }
                    OutlinedTextField(
                        value = draft.catatan,
                        onValueChange = { onDraftChange(draft.copy(catatan = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        label = { Text("Catatan Realisasi") },
                        placeholder = { Text("Contoh: Kegiatan selesai sesuai rencana.") },
                        enabled = !isSaving
                    )
                    Button(
                        onClick = onSaveRealisasi,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSaving) "Menyimpan..." else "Simpan Realisasi")
                    }
                } else {
                    TargetReadOnlyInfo(
                        text = if (isAssessmentFinalized) {
                            "Periode final. Realisasi dan tautan laporan hanya bisa dilihat."
                        } else {
                            "Mode baca saja. Realisasi diisi oleh pemilik target atau admin saat target sudah disetujui/final."
                        }
                    )
                    if (draft.catatan.isNotBlank()) {
                        TargetDetailNoteCard(
                            title = "Catatan Realisasi",
                            body = draft.catatan
                        )
                    }
                }

                TargetLinkedLaporanSection(
                    detailId = detail.id,
                    linkedLaporan = linkedLaporan,
                    candidateLaporan = candidateLaporan,
                    realisasi = realisasi,
                    canManageRealisasi = canManageRealisasi,
                    isAssessmentFinalized = isAssessmentFinalized,
                    isLinking = isLinking,
                    unlinkingKey = unlinkingKey,
                    onOpenLaporanPicker = onOpenLaporanPicker,
                    onUnlinkLaporan = onUnlinkLaporan
                )

                if (realisasiHistory.isNotEmpty()) {
                    HorizontalDivider()
                    RealisasiHistorySection(history = realisasiHistory)
                }
            } else {
                TargetMetricGrid(detail = detail)
            }
        }
    }
}

@Composable
private fun TargetMetricGrid(detail: TargetKinerjaDetailItem) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoBoxTarget(
                label = "Satuan",
                value = detail.satuan ?: "-",
                modifier = Modifier.weight(1f)
            )
            InfoBoxTarget(
                label = "Kuantitas",
                value = formatNullableDouble(detail.targetKuantitas),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            InfoBoxTarget(
                label = "Kualitas",
                value = formatNullableDouble(detail.targetKualitas),
                modifier = Modifier.weight(1f)
            )
            InfoBoxTarget(
                label = "Waktu",
                value = formatNullableDouble(detail.targetWaktu),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TargetRealityCompareGrid(
    detail: TargetKinerjaDetailItem,
    realisasi: RealisasiKinerjaItem?,
    draft: RealisasiFormState
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Target dan Realisasi",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TargetMetricCompareBox(
                label = detail.satuan?.takeIf { it.isNotBlank() } ?: "Kuantitas",
                target = formatNullableDouble(detail.targetKuantitas),
                realisasi = draft.realisasiKuantitas.takeIf { it.isNotBlank() }
                    ?: formatNullableDouble(realisasi?.realisasiKuantitas),
                modifier = Modifier.weight(1f)
            )
            TargetMetricCompareBox(
                label = "Kualitas",
                target = formatNullableDouble(detail.targetKualitas),
                realisasi = draft.realisasiKualitas.takeIf { it.isNotBlank() }
                    ?: formatNullableDouble(realisasi?.realisasiKualitas),
                modifier = Modifier.weight(1f)
            )
            TargetMetricCompareBox(
                label = "Waktu",
                target = formatNullableDouble(detail.targetWaktu),
                realisasi = draft.realisasiWaktu.takeIf { it.isNotBlank() }
                    ?: formatNullableDouble(realisasi?.realisasiWaktu),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TargetMetricCompareBox(
    label: String,
    target: String,
    realisasi: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Target",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = target,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            HorizontalDivider()
            Text(
                text = "Realisasi",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = realisasi,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TargetLinkedLaporanSection(
    detailId: Int?,
    linkedLaporan: List<RealisasiLinkedLaporanItem>,
    candidateLaporan: List<LaporanKegiatan>,
    realisasi: RealisasiKinerjaItem?,
    canManageRealisasi: Boolean,
    isAssessmentFinalized: Boolean,
    isLinking: Boolean,
    unlinkingKey: String?,
    onOpenLaporanPicker: () -> Unit,
    onUnlinkLaporan: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Laporan Pendukung",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            TargetSoftPill(
                text = "${linkedLaporan.size} tertaut",
                color = if (linkedLaporan.isNotEmpty()) StatusApproved else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (linkedLaporan.isEmpty()) {
            TargetEmptySupportCard(
                message = if (isAssessmentFinalized) {
                    "Belum ada laporan yang ditautkan. Periode final, jadi laporan tidak bisa ditambah."
                } else {
                    "Belum ada laporan yang ditautkan ke item ini."
                }
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                linkedLaporan.forEach { laporan ->
                    TargetLinkedLaporanCard(
                        detailId = detailId,
                        laporan = laporan,
                        canManageRealisasi = canManageRealisasi,
                        unlinkingKey = unlinkingKey,
                        onUnlinkLaporan = onUnlinkLaporan
                    )
                }
            }
        }

        if (canManageRealisasi) {
            Button(
                onClick = onOpenLaporanPicker,
                modifier = Modifier.fillMaxWidth(),
                enabled = realisasi != null && !isLinking && candidateLaporan.isNotEmpty()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    when {
                        realisasi == null -> "Simpan realisasi dulu"
                        candidateLaporan.isEmpty() -> "Semua laporan sudah tertaut"
                        isLinking -> "Menautkan..."
                        else -> "Tautkan Laporan Pendukung"
                    }
                )
            }
        }
    }
}

@Composable
private fun TargetLinkedLaporanCard(
    detailId: Int?,
    laporan: RealisasiLinkedLaporanItem,
    canManageRealisasi: Boolean,
    unlinkingKey: String?,
    onUnlinkLaporan: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = laporan.namaKegiatan,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "${formatTargetTanggalShort(laporan.tanggalKegiatan)} - ${formatTargetRentangWaktu(laporan.waktuMulai, laporan.waktuSelesai)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = laporan.namaKategori ?: "Tanpa kategori",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if (canManageRealisasi) {
                TextButton(
                    onClick = { onUnlinkLaporan(laporan.laporanId) },
                    enabled = unlinkingKey != "$detailId-${laporan.laporanId}"
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (unlinkingKey == "$detailId-${laporan.laporanId}") {
                            "Melepas..."
                        } else {
                            "Lepas"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetEmptySupportCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.24f)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TargetReadOnlyInfo(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = StatusPending.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = StatusPending,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TargetSoftPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f),
        modifier = Modifier.border(
            width = 1.dp,
            color = color.copy(alpha = 0.20f),
            shape = RoundedCornerShape(50)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LockedPeriodBanner() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = StatusPending.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusPending
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Periode sudah final",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Penilaian kinerja untuk periode ini sudah final. Target, realisasi, dan laporan pendukung hanya bisa dilihat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class TimelineHistoryEntry(
    val title: String,
    val subtitle: String?,
    val timestamp: String
)

@Composable
private fun TargetHistorySection(history: List<TargetKinerjaHistoryItem>) {
    TargetTimelineHistoryCard(
        title = "Riwayat Target",
        emptyMessage = "Belum ada riwayat target.",
        entries = history.map { item ->
            TimelineHistoryEntry(
                title = readableHistoryAction(item.aksi),
                subtitle = item.catatan?.takeIf { it.isNotBlank() },
                timestamp = formatHistoryTimestamp(item.createdAt)
            )
        }
    )
}

@Composable
private fun RealisasiHistorySection(history: List<RealisasiKinerjaHistoryItem>) {
    TargetTimelineHistoryCard(
        title = "Riwayat Realisasi",
        emptyMessage = "Belum ada riwayat realisasi.",
        entries = history.map { item ->
            TimelineHistoryEntry(
                title = readableHistoryAction(item.aksi),
                subtitle = item.catatan?.takeIf { it.isNotBlank() },
                timestamp = formatHistoryTimestamp(item.createdAt)
            )
        }
    )
}

@Composable
private fun TargetTimelineHistoryCard(
    title: String,
    emptyMessage: String,
    entries: List<TimelineHistoryEntry>
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (entries.isEmpty()) {
                TargetEmptySupportCard(message = emptyMessage)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    entries.forEachIndexed { index, entry ->
                        TimelineHistoryRow(
                            entry = entry,
                            isLast = index == entries.lastIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineHistoryRow(
    entry: TimelineHistoryEntry,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(20.dp),
                shape = RoundedCornerShape(50),
                color = StatusApproved
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(StatusApproved.copy(alpha = 0.65f))
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = entry.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
            if (!entry.subtitle.isNullOrBlank()) {
                Text(
                    text = entry.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun readableHistoryAction(value: String): String = when (value.lowercase()) {
    "create", "created" -> "Dibuat"
    "update", "updated" -> "Diperbarui"
    "submit", "submitted" -> "Diajukan"
    "review_setujui", "setujui" -> "Disetujui"
    "review_revisi", "revisi" -> "Diminta Revisi"
    "delete", "deleted" -> "Dihapus"
    "link_laporan" -> "Taut Laporan"
    "unlink_laporan" -> "Lepas Laporan"
    "finalize", "finalized" -> "Difinalkan"
    else -> value.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

private fun formatHistoryTimestamp(value: String): String {
    val inputPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd HH:mm:ss"
    )
    val output = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale("id", "ID"))
    inputPatterns.forEach { pattern ->
        try {
            val parser = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
            val parsed = parser.parse(value)
            if (parsed != null) return output.format(parsed)
        } catch (_: Exception) {
        }
    }
    return value.replace('T', ' ').replace("Z", "")
}

@Composable
private fun TargetDetailEditorCard(
    index: Int,
    state: TargetDetailFormState,
    onChange: (TargetDetailFormState) -> Unit,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Item ${index + 1}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = "Tuliskan satu target yang bisa diukur dan direalisasikan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canRemove) {
                    TextButton(onClick = onRemove) {
                        Text("Hapus")
                    }
                }
            }

            OutlinedTextField(
                value = state.uraianTarget,
                onValueChange = { onChange(state.copy(uraianTarget = it)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text("Uraian Target") },
                placeholder = { Text("Contoh: Menyusun laporan kegiatan dan apel pagi") }
            )

            OutlinedTextField(
                value = state.indikator,
                onValueChange = { onChange(state.copy(indikator = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Indikator") },
                placeholder = { Text("Contoh: Jumlah laporan kegiatan selesai") }
            )

            OutlinedTextField(
                value = state.satuan,
                onValueChange = { onChange(state.copy(satuan = it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Satuan") },
                placeholder = { Text("Contoh: Laporan, Dokumen, Kegiatan") }
            )

            Text(
                text = "Target dan Bobot",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.targetKuantitas,
                    onValueChange = { onChange(state.copy(targetKuantitas = it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Kuantitas") },
                    placeholder = { Text("30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.targetKualitas,
                    onValueChange = { onChange(state.copy(targetKualitas = it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Kualitas") },
                    placeholder = { Text("100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = state.targetWaktu,
                    onValueChange = { onChange(state.copy(targetWaktu = it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Waktu") },
                    placeholder = { Text("22") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.bobot,
                    onValueChange = { onChange(state.copy(bobot = it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Bobot (%)") },
                    placeholder = { Text("100") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            ) {
                Text(
                    text = "Contoh: 30 laporan, kualitas 100, waktu 22 hari kerja, bobot 100%.",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusBadgeTarget(status: String) {
    val (label, color) = when (status) {
        "draft" -> "Draft" to MaterialTheme.colorScheme.secondary
        "diajukan" -> "Diajukan" to StatusPending
        "disetujui" -> "Disetujui" to StatusApproved
        "revisi" -> "Revisi" to StatusRevised
        "final" -> "Final" to PrimaryLight
        else -> status to MaterialTheme.colorScheme.secondary
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
private fun InfoMiniChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun InfoBoxTarget(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun TargetEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
            textAlign = TextAlign.Center
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(actionText)
            }
        }
    }
}

@Composable
private fun SimpleStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
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
}
