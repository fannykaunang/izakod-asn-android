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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.PenilaianBelumDibuatItem
import com.kominfo_mkq.izakod_asn.data.model.PenilaianKinerjaAutoFill
import com.kominfo_mkq.izakod_asn.data.model.PenilaianKinerjaItem
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.viewmodel.PenilaianBelumDibuatViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.PenilaianKinerjaDetailViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.PenilaianKinerjaListViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class PenilaianListMode { MINE, SUBORDINATE }

private enum class PenilaianStatusFilter(val label: String, val raw: String?) {
    ALL("Semua", null),
    DRAFT("Draft", "draft"),
    REVIEW("Review", "review"),
    FINAL("Final", "final")
}

private fun PenilaianKinerjaItem.isPendingReview(): Boolean {
    return statusFinalisasi.trim().lowercase(Locale.getDefault()) in setOf("draft", "review")
}

private fun formatPenilaianPeriode(bulan: Int, tahun: Int): String {
    return SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(
        Calendar.getInstance().apply {
            set(Calendar.YEAR, tahun)
            set(Calendar.MONTH, bulan - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }.time
    )
}

private fun formatPenilaianScore(value: Double?): String {
    if (value == null || value.isNaN()) return "-"
    return DecimalFormat("#.##").format(value)
}

private fun formatPenilaianEditableNumber(value: Double?): String {
    if (value == null || value.isNaN()) return ""
    return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
}

private fun formatTargetStatusPenilaian(value: String): String {
    return when (value.lowercase(Locale.getDefault())) {
        "draft" -> "Draft"
        "diajukan" -> "Diajukan"
        "disetujui" -> "Disetujui"
        "revisi" -> "Revisi"
        "final" -> "Final"
        else -> value.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}

private fun shiftMonth(month: Int, year: Int, delta: Int): Pair<Int, Int> {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        add(Calendar.MONTH, delta)
    }

    return (calendar.get(Calendar.MONTH) + 1) to calendar.get(Calendar.YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenilaianKinerjaListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    initialMode: String = "mine",
    viewModel: PenilaianKinerjaListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val now = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableStateOf(now.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(now.get(Calendar.YEAR)) }
    var listMode by remember(initialMode) {
        mutableStateOf(
            if (initialMode.equals("subordinate", ignoreCase = true)) {
                PenilaianListMode.SUBORDINATE
            } else {
                PenilaianListMode.MINE
            }
        )
    }
    var statusFilter by remember { mutableStateOf(PenilaianStatusFilter.ALL) }

    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.refresh(
            tahun = selectedYear,
            bulan = selectedMonth
        )
    }

    val mineAssessments = remember(uiState.assessments, uiState.currentPegawaiId) {
        uiState.assessments.filter { it.pegawaiId == uiState.currentPegawaiId }
    }
    val subordinateAssessments = remember(uiState.assessments, uiState.currentPegawaiId) {
        uiState.assessments.filter { it.pegawaiId != uiState.currentPegawaiId }
    }
    val pendingSubordinateReviewCount = remember(subordinateAssessments) {
        subordinateAssessments.count { it.isPendingReview() }
    }

    LaunchedEffect(uiState.canReviewSubordinates, uiState.isLoading) {
        if (!uiState.isLoading && !uiState.canReviewSubordinates && listMode == PenilaianListMode.SUBORDINATE) {
            listMode = PenilaianListMode.MINE
        }
    }

    val activeSource = if (listMode == PenilaianListMode.MINE) {
        mineAssessments
    } else {
        subordinateAssessments
    }
    val filteredAssessments = activeSource.filter { assessment ->
        statusFilter.raw == null ||
            assessment.statusFinalisasi.equals(statusFilter.raw, ignoreCase = true)
    }
    val hasActiveAssessments = activeSource.isNotEmpty()

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Penilaian Kinerja",
                compact = true,
                onBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.refresh(
                                tahun = selectedYear,
                                bulan = selectedMonth
                            )
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            if (listMode == PenilaianListMode.MINE) {
                FloatingActionButton(
                    onClick = {
                        viewModel.createDraft(
                            tahun = selectedYear,
                            bulan = selectedMonth,
                            onSuccess = { assessmentId ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Draft penilaian berhasil dibuat")
                                }
                                onNavigateToDetail(assessmentId)
                            },
                            onError = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    },
                    containerColor = PrimaryLight,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Buat draft")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                        text = "Pantau draft, review, dan finalisasi penilaian periode aktif.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (uiState.canReviewSubordinates) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = listMode == PenilaianListMode.MINE,
                                onClick = { listMode = PenilaianListMode.MINE },
                                label = { Text("Penilaian Saya") }
                            )
                            PenilaianReviewBawahanFilterChip(
                                selected = listMode == PenilaianListMode.SUBORDINATE,
                                onClick = { listMode = PenilaianListMode.SUBORDINATE },
                                count = pendingSubordinateReviewCount
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
                                val (month, year) = shiftMonth(selectedMonth, selectedYear, -1)
                                selectedMonth = month
                                selectedYear = year
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Periode sebelumnya")
                        }

                        Text(
                            text = formatPenilaianPeriode(selectedMonth, selectedYear),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        IconButton(
                            onClick = {
                                val (month, year) = shiftMonth(selectedMonth, selectedYear, 1)
                                selectedMonth = month
                                selectedYear = year
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Periode berikutnya")
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
                items(PenilaianStatusFilter.entries) { filter ->
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
                    PenilaianStateCard(
                        icon = Icons.Default.Warning,
                        title = "Gagal memuat penilaian",
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        actionText = "Coba Lagi",
                        onAction = {
                            viewModel.refresh(
                                tahun = selectedYear,
                                bulan = selectedMonth
                            )
                        }
                    )
                }

                filteredAssessments.isEmpty() -> {
                    PenilaianEmptyState(
                        icon = if (listMode == PenilaianListMode.MINE) {
                            Icons.Default.AssignmentTurnedIn
                        } else {
                            Icons.Default.Groups
                        },
                        message = when {
                            !hasActiveAssessments && listMode == PenilaianListMode.MINE -> {
                                "Belum ada draft penilaian"
                            }
                            !hasActiveAssessments -> "Belum ada penilaian bawahan"
                            statusFilter != PenilaianStatusFilter.ALL -> {
                                "Belum ada penilaian ${statusFilter.label.lowercase(Locale.getDefault())}"
                            }
                            listMode == PenilaianListMode.MINE -> "Belum ada draft penilaian"
                            else -> "Belum ada penilaian bawahan"
                        },
                        actionText = if (listMode == PenilaianListMode.MINE && !hasActiveAssessments) {
                            "Buat Draft"
                        } else {
                            null
                        },
                        onAction = if (listMode == PenilaianListMode.MINE && !hasActiveAssessments) {
                            {
                                viewModel.createDraft(
                                    tahun = selectedYear,
                                    bulan = selectedMonth,
                                    onSuccess = onNavigateToDetail,
                                    onError = { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    }
                                )
                            }
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
                        items(filteredAssessments, key = { it.id }) { assessment ->
                            PenilaianKinerjaCard(
                                assessment = assessment,
                                showPegawaiName = listMode == PenilaianListMode.SUBORDINATE,
                                onClick = { onNavigateToDetail(assessment.id) }
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
private fun PenilaianReviewBawahanFilterChip(
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
        PenilaianReviewBawahanBadge(count = count)
    }
}

@Composable
private fun PenilaianReviewBawahanBadge(count: Int) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenilaianBelumDibuatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Int) -> Unit,
    viewModel: PenilaianBelumDibuatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val now = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableStateOf(now.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(now.get(Calendar.YEAR)) }

    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.refresh(
            tahun = selectedYear,
            bulan = selectedMonth
        )
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Penilaian Belum Dibuat",
                onBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.refresh(
                                tahun = selectedYear,
                                bulan = selectedMonth
                            )
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Target pegawai sudah siap dinilai, tetapi draft penilaian belum dibuat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val (month, year) = shiftMonth(selectedMonth, selectedYear, -1)
                                selectedMonth = month
                                selectedYear = year
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Periode sebelumnya")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatPenilaianPeriode(selectedMonth, selectedYear),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${uiState.items.size} pegawai",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                val (month, year) = shiftMonth(selectedMonth, selectedYear, 1)
                                selectedMonth = month
                                selectedYear = year
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Periode berikutnya")
                        }
                    }
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.isError -> {
                    PenilaianStateCard(
                        icon = Icons.Default.Warning,
                        title = "Gagal memuat daftar",
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        actionText = "Coba Lagi",
                        onAction = {
                            viewModel.refresh(
                                tahun = selectedYear,
                                bulan = selectedMonth
                            )
                        }
                    )
                }

                uiState.items.isEmpty() -> {
                    PenilaianStateCard(
                        icon = Icons.Default.AssignmentTurnedIn,
                        title = "Semua penilaian sudah dibuat",
                        message = "Tidak ada target pegawai yang menunggu draft penilaian pada periode ini.",
                        actionText = "Refresh",
                        onAction = {
                            viewModel.refresh(
                                tahun = selectedYear,
                                bulan = selectedMonth
                            )
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.items, key = { "${it.targetKinerjaId}-${it.pegawaiId}" }) { item ->
                            PenilaianBelumDibuatCard(
                                item = item,
                                isCreating = uiState.isCreating &&
                                    uiState.creatingPegawaiId == item.pegawaiId,
                                onCreateDraft = {
                                    viewModel.createDraftFor(
                                        item = item,
                                        onSuccess = { assessmentId ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Draft penilaian berhasil dibuat")
                                            }
                                            onNavigateToDetail(assessmentId)
                                        },
                                        onError = { message ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(message)
                                            }
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PenilaianKinerjaDetailScreen(
    assessmentId: Int,
    onNavigateBack: () -> Unit,
    viewModel: PenilaianKinerjaDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var nilaiTarget by remember { mutableStateOf("") }
    var nilaiRealisasi by remember { mutableStateOf("") }
    var nilaiAkhir by remember { mutableStateOf("") }
    var predikat by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }

    LaunchedEffect(assessmentId) {
        viewModel.loadDetail(assessmentId)
    }

    LaunchedEffect(uiState.assessment?.id) {
        val assessment = uiState.assessment ?: return@LaunchedEffect
        nilaiTarget = formatPenilaianEditableNumber(assessment.nilaiTarget)
        nilaiRealisasi = formatPenilaianEditableNumber(assessment.nilaiRealisasi)
        nilaiAkhir = formatPenilaianEditableNumber(assessment.nilaiAkhir)
        predikat = assessment.predikat.orEmpty()
        catatan = assessment.catatan.orEmpty()
    }

    val assessment = uiState.assessment
    val readonly = !uiState.canReview || assessment?.statusFinalisasi == "final"

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Detail Penilaian",
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

            uiState.isError || assessment == null -> {
                PenilaianStateCard(
                    icon = Icons.Default.Warning,
                    title = "Detail penilaian tidak ditemukan",
                    message = uiState.errorMessage ?: "Gagal memuat detail penilaian",
                    actionText = "Coba Lagi",
                    onAction = { viewModel.loadDetail(assessmentId) }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = assessment.pegawaiNama ?: "Pegawai #${assessment.pegawaiId}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${assessment.pegawaiSkpd ?: "-"} • ${formatPenilaianPeriode(assessment.bulan, assessment.tahun)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                PenilaianStatusBadge(status = assessment.statusFinalisasi)
                            }
                        }
                    }

                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Berikan Penilaian",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = if (readonly) {
                                                "Mode baca saja. Reviewer atau admin dapat memperbarui nilai."
                                            } else {
                                                "Isi nilai, catatan, lalu finalkan bila sudah siap."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                }

                                OutlinedTextField(
                                    value = nilaiTarget,
                                    onValueChange = { nilaiTarget = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nilai Target") },
                                    enabled = !readonly
                                )
                                OutlinedTextField(
                                    value = nilaiRealisasi,
                                    onValueChange = { nilaiRealisasi = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nilai Realisasi") },
                                    enabled = !readonly
                                )
                                OutlinedTextField(
                                    value = nilaiAkhir,
                                    onValueChange = { nilaiAkhir = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Nilai Akhir") },
                                    enabled = !readonly
                                )
                                OutlinedTextField(
                                    value = predikat,
                                    onValueChange = { predikat = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Predikat") },
                                    enabled = !readonly
                                )
                                OutlinedTextField(
                                    value = catatan,
                                    onValueChange = { catatan = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 4,
                                    label = { Text("Catatan Penilaian") },
                                    enabled = !readonly
                                )

                                if (!readonly) {
                                    Button(
                                        onClick = {
                                            viewModel.saveAssessment(
                                                assessmentId = assessmentId,
                                                nilaiTarget = nilaiTarget.toDoubleOrNull(),
                                                nilaiRealisasi = nilaiRealisasi.toDoubleOrNull(),
                                                nilaiAkhir = nilaiAkhir.toDoubleOrNull(),
                                                predikat = predikat.ifBlank { null },
                                                catatan = catatan.ifBlank { null },
                                                onSuccess = { message ->
                                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                                },
                                                onError = { message ->
                                                    scope.launch { snackbarHostState.showSnackbar(message) }
                                                }
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !uiState.isSaving
                                    ) {
                                        Text(if (uiState.isSaving) "Menyimpan..." else "Simpan Penilaian")
                                    }

                                    if (assessment.statusFinalisasi != "final") {
                                        TextButton(
                                            onClick = {
                                                viewModel.finalisasiAssessment(
                                                    assessmentId = assessmentId,
                                                    onSuccess = { message ->
                                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                                    },
                                                    onError = { message ->
                                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                                    }
                                                )
                                            },
                                            enabled = !uiState.isFinalizing,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(if (uiState.isFinalizing) "Memfinalkan..." else "Finalkan Penilaian")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        PenilaianAutoFillCard(
                            autoFill = uiState.autoFill,
                            enabled = !readonly,
                            onUseAutoFill = { autoFill ->
                                nilaiTarget = formatPenilaianEditableNumber(autoFill.suggestedNilaiTarget)
                                nilaiRealisasi = formatPenilaianEditableNumber(autoFill.suggestedNilaiRealisasi)
                                nilaiAkhir = formatPenilaianEditableNumber(autoFill.suggestedNilaiAkhir)
                                predikat = autoFill.suggestedPredikat.orEmpty()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PenilaianBelumDibuatCard(
    item: PenilaianBelumDibuatItem,
    isCreating: Boolean,
    onCreateDraft: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = item.pegawaiNama ?: "Pegawai #${item.pegawaiId}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.pegawaiSkpd ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = StatusPending.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "Belum dibuat",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusPending
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PenilaianMetricBox(
                    label = "Target",
                    value = formatTargetStatusPenilaian(item.status),
                    modifier = Modifier.weight(1f)
                )
                PenilaianMetricBox(
                    label = "Realisasi",
                    value = "${item.itemSudahRealisasi}/${item.totalItem}",
                    modifier = Modifier.weight(1f)
                )
                PenilaianMetricBox(
                    label = "Laporan",
                    value = item.totalLaporanTertaut.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            LinearProgressIndicator(
                progress = { (item.persentaseProgress / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = StatusApproved,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Button(
                onClick = onCreateDraft,
                enabled = !isCreating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Membuat draft...")
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Draft Penilaian")
                }
            }
        }
    }
}

@Composable
private fun PenilaianKinerjaCard(
    assessment: PenilaianKinerjaItem,
    showPegawaiName: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (showPegawaiName) {
                            assessment.pegawaiNama ?: "Pegawai #${assessment.pegawaiId}"
                        } else {
                            formatPenilaianPeriode(assessment.bulan, assessment.tahun)
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (showPegawaiName) {
                            "${formatPenilaianPeriode(assessment.bulan, assessment.tahun)} • ${assessment.pegawaiSkpd ?: "-"}"
                        } else {
                            assessment.pegawaiSkpd ?: "-"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PenilaianStatusBadge(status = assessment.statusFinalisasi)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PenilaianMetricBox(
                    label = "Target",
                    value = formatPenilaianScore(assessment.nilaiTarget),
                    modifier = Modifier.weight(1f)
                )
                PenilaianMetricBox(
                    label = "Realisasi",
                    value = formatPenilaianScore(assessment.nilaiRealisasi),
                    modifier = Modifier.weight(1f)
                )
                PenilaianMetricBox(
                    label = "Akhir",
                    value = formatPenilaianScore(assessment.nilaiAkhir),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Predikat: ${assessment.predikat ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PenilaianAutoFillCard(
    autoFill: PenilaianKinerjaAutoFill?,
    enabled: Boolean,
    onUseAutoFill: (PenilaianKinerjaAutoFill) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoGraph, contentDescription = null, tint = PrimaryLight)
                Text(
                    text = "Saran Nilai Otomatis",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (autoFill == null) {
                Text(
                    text = "Belum ada data target/realisasi yang cukup untuk menghasilkan nilai otomatis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PenilaianMetricBox(
                        label = "Progress",
                        value = "${formatPenilaianScore(autoFill.persentaseProgress)}%",
                        modifier = Modifier.weight(1f)
                    )
                    PenilaianMetricBox(
                        label = "Item Terisi",
                        value = "${autoFill.itemSudahRealisasi}/${autoFill.totalItem}",
                        modifier = Modifier.weight(1f)
                    )
                    PenilaianMetricBox(
                        label = "Laporan",
                        value = autoFill.totalLaporanTertaut.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PenilaianMetricBox(
                        label = "Saran Target",
                        value = formatPenilaianScore(autoFill.suggestedNilaiTarget),
                        modifier = Modifier.weight(1f)
                    )
                    PenilaianMetricBox(
                        label = "Saran Realisasi",
                        value = formatPenilaianScore(autoFill.suggestedNilaiRealisasi),
                        modifier = Modifier.weight(1f)
                    )
                    PenilaianMetricBox(
                        label = "Saran Akhir",
                        value = formatPenilaianScore(autoFill.suggestedNilaiAkhir),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "Saran Predikat: ${autoFill.suggestedPredikat ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { onUseAutoFill(autoFill) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gunakan Nilai Otomatis")
                }
            }
        }
    }
}

@Composable
private fun PenilaianStatusBadge(status: String) {
    val (label, color) = when (status) {
        "draft" -> "Draft" to MaterialTheme.colorScheme.secondary
        "review" -> "Review" to StatusPending
        "final" -> "Final" to StatusApproved
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
private fun PenilaianMetricBox(
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
private fun PenilaianEmptyState(
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
private fun PenilaianStateCard(
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
