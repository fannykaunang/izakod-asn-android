// ReportListScreen.kt (REFACTORED)

package com.kominfo_mkq.izakod_asn.ui.screens

// alias supaya tidak bentrok dengan androidx.compose.ui.graphics.Color
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan
import com.kominfo_mkq.izakod_asn.data.model.isNonAsnJabatan
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.components.StatusBadge
import com.kominfo_mkq.izakod_asn.ui.components.StatusType
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.viewmodel.LaporanListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.graphics.Color as GColor

enum class FilterType { ALL, DRAFT, PENDING, APPROVED, REJECTED, REVISED }

private enum class ReportOwnerScope { MINE, SUBORDINATES }

/** UI model yang dipakai list + PDF (jangan pakai API model langsung di UI) */
private data class ReportUi(
    val id: Int,
    val tanggalLabel: String,
    val pegawaiNama: String?,
    val namaKegiatan: String,
    val kategoriLabel: String,
    val jamLabel: String,
    val durasiMenit: Int,
    val deskripsi: String,
    val status: StatusType,
    val catatanAtasan: String?
)

data class PersonBlock(
    val nama: String,
    val nip: String,
    val jabatan: String
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ReportListScreen(
    onBack: () -> Unit,
    onReportClick: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onCreateReport: () -> Unit,
    showBackButton: Boolean = true,
    showCreateFab: Boolean = true,
    includeSubordinates: Boolean = false,
    initialFilterKey: String = "all",
    viewModel: LaporanListViewModel = viewModel()
) {
    @Suppress("UNUSED_PARAMETER")

    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val initialFilter = remember(initialFilterKey) { filterTypeFromRoute(initialFilterKey) }
    val initialOwnerScope = remember(includeSubordinates) {
        if (includeSubordinates) ReportOwnerScope.SUBORDINATES else ReportOwnerScope.MINE
    }
    var selectedOwnerScope by remember(includeSubordinates) {
        mutableStateOf(initialOwnerScope)
    }
    var selectedFilter by remember(initialFilterKey, includeSubordinates) {
        mutableStateOf(initialFilter)
    }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Print preview state
    var showPrintPreview by remember { mutableStateOf(false) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfError by remember { mutableStateOf<String?>(null) }
    val isSubordinateMode = selectedOwnerScope == ReportOwnerScope.SUBORDINATES
    val showOwnerTabs = uiState.hasActiveSubordinates || isSubordinateMode

    // Load data when screen opens
    LaunchedEffect(includeSubordinates, initialFilterKey) {
        selectedFilter = initialFilter
        selectedOwnerScope = initialOwnerScope
    }

    LaunchedEffect(selectedOwnerScope) {
        val loadSubordinates = selectedOwnerScope == ReportOwnerScope.SUBORDINATES
        viewModel.loadLaporanList(
            context = context,
            includeSubordinates = loadSubordinates
        )
        if (!loadSubordinates) {
            viewModel.loadAtasanPegawai(context)
        }
    }

    val doRefresh = remember(uiState.filterBulan, uiState.filterTahun, selectedOwnerScope) {
        {
            val loadSubordinates = selectedOwnerScope == ReportOwnerScope.SUBORDINATES
            if (!loadSubordinates && uiState.filterBulan != null && uiState.filterTahun != null) {
                viewModel.loadLaporanBulanan(context, uiState.filterBulan!!, uiState.filterTahun!!)
            } else {
                viewModel.loadLaporanList(
                    context = context,
                    includeSubordinates = loadSubordinates
                )
            }
        }
    }

    val allReports: List<ReportUi> = remember(uiState.laporanList) {
        uiState.laporanList.map { it.toUi() }
    }

    val filteredReports = remember(allReports, selectedFilter) {
        allReports
            .asSequence()
            .filter { r ->
                when (selectedFilter) {
                    FilterType.ALL -> true
                    FilterType.DRAFT -> r.status == StatusType.DRAFT
                    FilterType.PENDING -> r.status == StatusType.PENDING
                    FilterType.APPROVED -> r.status == StatusType.APPROVED
                    FilterType.REJECTED -> r.status == StatusType.REJECTED
                    FilterType.REVISED -> r.status == StatusType.REVISED
                }
            }
            .toList()
    }

    val totalCount = remember(allReports) { allReports.size }
    val draftCount = remember(allReports) { allReports.count { it.status == StatusType.DRAFT } }
    val pendingCount = remember(allReports) { allReports.count { it.status == StatusType.PENDING } }
    val approvedCount = remember(allReports) { allReports.count { it.status == StatusType.APPROVED } }
    val attentionCount = remember(allReports) { allReports.count { it.needsEmployeeAttention() } }

    val actionReports = remember(filteredReports, selectedFilter, isSubordinateMode) {
        if (selectedFilter == FilterType.ALL) {
            filteredReports.filter {
                if (isSubordinateMode) {
                    it.needsSupervisorAttention()
                } else {
                    it.needsEmployeeAttention()
                }
            }
        } else {
            emptyList()
        }
    }

    val regularReports = remember(filteredReports, actionReports, selectedFilter) {
        if (selectedFilter == FilterType.ALL && actionReports.isNotEmpty()) {
            filteredReports.filterNot { it.needsEmployeeAttention() }
        } else {
            filteredReports
        }
    }

    val activePeriodLabel = remember(uiState.filterBulan, uiState.filterTahun) {
        if (uiState.filterBulan != null && uiState.filterTahun != null) {
            "${monthNameId(uiState.filterBulan!!)} ${uiState.filterTahun}"
        } else null
    }

    // Pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = doRefresh
    )

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = if (isSubordinateMode) "Laporan Bawahan" else "Laporan Kegiatan",
                subtitle = if (isSubordinateMode) {
                    "Review dan pantau laporan bawahan langsung"
                } else {
                    "Pantau draft, revisi, dan riwayat laporan"
                },
                compact = true,
                onBack = if (showBackButton) onBack else null,
                actions = {
                    // ✅ PRINT button
                    if (!isSubordinateMode) {
                        IconButton(onClick = onOpenSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Cari")
                        }
                    }

                    val isFilterActive = (uiState.filterBulan != null && uiState.filterTahun != null)
                    if (!isSubordinateMode) Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            BadgedBox(
                                badge = {
                                    if (isFilterActive) {
                                        Badge(
                                            containerColor = PrimaryLight,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opsi lainnya")
                            }
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cetak") },
                                leadingIcon = {
                                    Icon(Icons.Default.Print, contentDescription = null)
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showPrintPreview = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Filter") },
                                leadingIcon = {
                                    Icon(Icons.Default.FilterList, contentDescription = null)
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    showFilterDialog = true
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (showCreateFab && !isSubordinateMode) {
                ExtendedFloatingActionButton(
                    onClick = onCreateReport,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Buat Laporan") },
                    containerColor = PrimaryLight,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            when {
                uiState.isError -> ErrorState(
                    message = uiState.errorMessage ?: "Terjadi kesalahan",
                    onRetry = doRefresh
                )

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ReportSummaryStrip(
                            totalCount = totalCount,
                            draftCount = draftCount,
                            pendingCount = pendingCount,
                            approvedCount = approvedCount,
                            attentionCount = attentionCount
                        )

                        if (showOwnerTabs) {
                            ReportOwnerTabs(
                                selected = selectedOwnerScope,
                                subordinateActionCount = uiState.subordinateActionCount,
                                onSelect = { nextScope ->
                                    if (nextScope != selectedOwnerScope) {
                                        selectedOwnerScope = nextScope
                                        selectedFilter = FilterType.ALL
                                    }
                                }
                            )
                        }

                        FilterChipsRow(
                            selected = selectedFilter,
                            onSelect = { selectedFilter = it }
                        )

                        ReportListHeaderBar(
                            title = if (isSubordinateMode) "Laporan Bawahan" else "Semua Laporan",
                            totalDisplayed = filteredReports.size,
                            periodLabel = activePeriodLabel ?: "Periode",
                            isFiltered = activePeriodLabel != null,
                            showPeriodFilter = !isSubordinateMode,
                            onOpenFilter = { showFilterDialog = true }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pullRefresh(pullRefreshState)
                        ) {
                            if (filteredReports.isEmpty()) {
                                EmptyState(
                                    message = emptyReportMessage(
                                        selectedFilter = selectedFilter,
                                        activePeriodLabel = activePeriodLabel,
                                        isSubordinateMode = isSubordinateMode
                                    ),
                                    onAction = if (!isSubordinateMode && (selectedFilter == FilterType.ALL || selectedFilter == FilterType.DRAFT)) {
                                        onCreateReport
                                    } else {
                                        null
                                    },
                                    actionText = "Buat Laporan"
                                )
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 112.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (actionReports.isNotEmpty()) {
                                        item {
                                            SectionHeading(
                                                title = "Perlu Tindakan",
                                                subtitle = "${actionReports.size} laporan perlu perhatian"
                                            )
                                        }

                                        items(actionReports, key = { it.id }) { report ->
                                            ReportCard(
                                                report = report,
                                                showEmployeeName = isSubordinateMode,
                                                isSupervisorMode = isSubordinateMode,
                                                onClick = { onReportClick(report.id.toString()) }
                                            )
                                        }

                                        item {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            SectionHeading(
                                                title = "Laporan Lainnya",
                                                subtitle = if (regularReports.isNotEmpty()) {
                                                    "${regularReports.size} laporan lainnya"
                                                } else {
                                                    "Tidak ada laporan lainnya"
                                                }
                                            )
                                        }

                                        items(regularReports, key = { "regular-${it.id}" }) { report ->
                                            ReportCard(
                                                report = report,
                                                showEmployeeName = isSubordinateMode,
                                                isSupervisorMode = isSubordinateMode,
                                                onClick = { onReportClick(report.id.toString()) }
                                            )
                                        }
                                    } else {
                                        items(regularReports, key = { it.id }) { report ->
                                            ReportCard(
                                                report = report,
                                                showEmployeeName = isSubordinateMode,
                                                isSupervisorMode = isSubordinateMode,
                                                onClick = { onReportClick(report.id.toString()) }
                                            )
                                        }
                                    }
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
            }
        }
    }

    // Filter dialog (bulan/tahun)
    if (showFilterDialog && !isSubordinateMode) {
        MonthYearFilterDialog(
            initialMonth = uiState.filterBulan,
            initialYear = uiState.filterTahun,
            onDismiss = { showFilterDialog = false },
            onApply = { month, year ->
                showFilterDialog = false
                viewModel.loadLaporanBulanan(context, month, year)
            },
            onClear = {
                showFilterDialog = false
                viewModel.clearFilter(context, false)
            }
        )
    }

    // ✅ PRINT PREVIEW
    if (showPrintPreview && !isSubordinateMode) {
        val bulan = uiState.filterBulan ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
        val tahun = uiState.filterTahun ?: Calendar.getInstance().get(Calendar.YEAR)

        val atasanReady = uiState.atasanPegawai != null
        val atasanError = uiState.errorAtasan
        val atasanLoading = uiState.isLoadingAtasan

        PrintPreviewDialog(
            title = "Preview Cetak",
            month = bulan,
            year = tahun,
            total = filteredReports.size,
            isLoading = isGeneratingPdf,
            //errorText = pdfError,
            errorText = pdfError ?: uiState.errorAtasan,
            pdfFile = pdfFile,
            onDismiss = {
                showPrintPreview = false
                pdfFile = null
                pdfError = null
                isGeneratingPdf = false
            },
            autoGenerate = (uiState.atasanPegawai != null && uiState.errorAtasan.isNullOrBlank()),
//            onGenerate = {
//                scope.launch {
//                    isGeneratingPdf = true
//                    pdfError = null
//
//                    try {
//                        // 1) Pastikan data atasan sudah diload
//                        if (viewModel.uiState.value.atasanPegawai == null) {
//                            viewModel.loadAtasanPegawai(context)
//                        }
//
//                        // 2) Tunggu sampai state terisi (maks 10 detik biar tidak nge-hang)
////                        val data = withTimeout(10_000) {
////                            snapshotFlow { viewModel.uiState.value.atasanPegawai }
////                                .filterNotNull()
////                                .first()
////                        }
//
//                        val data = withTimeout(10_000) {
//                            snapshotFlow { viewModel.uiState.value }
//                                .first { it.atasanPegawai != null || it.errorAtasan != null }
//                                .let { state ->
//                                    if (state.errorAtasan != null) throw Exception(state.errorAtasan)
//                                    state.atasanPegawai!!
//                                }
//                        }
//
//                        val asn = PersonBlock(
//                            nama = data.pegawaiNama.orEmpty(),
//                            nip = data.pegawaiNip.orEmpty(),
//                            jabatan = data.pegawaiJabatan.orEmpty(),
////                            jabatan = listOfNotNull(data.pegawaiJabatan)
////                                .joinToString(" - ")
////                                .ifEmpty { "-" }
//                        )
//
//                        val atasan = PersonBlock(
//                            nama = data.atasanPegawaiNama.orEmpty(),      // sesuaikan nama field modelmu
//                            nip = data.atasanPegawaiNip.orEmpty(),
//                            jabatan = data.atasanPegawaiJabatan.orEmpty().ifEmpty { "-" }
//                        )
//
//                        val skpdTitle = data.pegawaiSkpd?.trim().orEmpty()
//                            .ifEmpty { "DINAS / UNIT KERJA" }
//                            .uppercase()
//
//                        // 3) Generate PDF (jangan di Main thread)
//                        val file = withContext(Dispatchers.Default) {
//                            generateLaporanPdfTppTemplate(
//                                context = context,
//                                bulan = bulan,
//                                tahun = tahun,
//                                laporan = filteredReports,
//                                asn = asn,
//                                atasan = atasan,
//                                skpdTitle = skpdTitle,
//                                kota = "Merauke",
//                                ttdResId = null,
//                                stempelResId = null
//                            )
//                        }
//
//                        pdfFile = file
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        pdfError = e.message ?: "Gagal membuat PDF"
//                        pdfFile = null
//                    } finally {
//                        isGeneratingPdf = false
//                    }
//                }
//            },
            onGenerate = {
                scope.launch {
                    pdfError = null

                    // 0) Kalau masih loading atasan, jangan mulai generate PDF
                    if (atasanLoading) {
                        pdfError = "Sedang memuat data atasan... coba lagi sebentar."
                        return@launch
                    }

                    // 1) Jika ada error / data atasan kosong, stop tanpa loading PDF
                    if (!atasanError.isNullOrBlank()) {
                        pdfError = atasanError
                        return@launch
                    }

                    if (!atasanReady) {
                        // Optional: panggil loadAtasanPegawai lagi (kalau belum pernah)
                        viewModel.loadAtasanPegawai(context)
                        pdfError = "Data atasan belum tersedia. Silakan coba lagi."
                        return@launch
                    }

                    // ✅ Baru mulai loading PDF kalau syarat sudah terpenuhi
                    isGeneratingPdf = true
                    try {
                        val data = uiState.atasanPegawai!!  // sudah pasti ada

                        val asn = PersonBlock(
                            nama = data.pegawaiNama.orEmpty(),
                            nip = data.pegawaiNip.orEmpty(),
                            jabatan = data.pegawaiJabatan.orEmpty()
                        )

                        val atasan = PersonBlock(
                            nama = data.atasanPegawaiNama.orEmpty(),
                            nip = data.atasanPegawaiNip.orEmpty(),
                            jabatan = data.atasanPegawaiJabatan.orEmpty().ifEmpty { "-" }
                        )

                        val skpdTitle = data.pegawaiSkpd?.trim().orEmpty()
                            .ifEmpty { "DINAS / UNIT KERJA" }
                            .uppercase()

                        val file = withContext(Dispatchers.Default) {
                            generateLaporanPdfTppTemplate(
                                context = context,
                                bulan = bulan,
                                tahun = tahun,
                                laporan = filteredReports,
                                asn = asn,
                                atasan = atasan,
                                skpdTitle = skpdTitle,
                                kota = "Merauke",
                                ttdResId = null,
                                stempelResId = null
                            )
                        }

                        pdfFile = file
                    } catch (e: Exception) {
                        pdfError = e.message ?: "Gagal membuat PDF"
                        pdfFile = null
                    } finally {
                        isGeneratingPdf = false
                    }
                }
            },
            onPrint = { file ->
                printPdf(context, file, jobName = "Laporan Kegiatan $bulan-$tahun")
            }
        )
    }
}

/* ----------------------------- UI pieces ----------------------------- */

@Composable
fun ReportSearchScreen(
    onBack: () -> Unit,
    onReportClick: (String) -> Unit,
    viewModel: LaporanListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadLaporanList(context)
    }

    val allReports = remember(uiState.laporanList) {
        uiState.laporanList.map { it.toUi() }
    }

    val filteredReports = remember(allReports, searchQuery) {
        val query = searchQuery.trim()
        if (query.isBlank()) {
            emptyList()
        } else {
            allReports.filter { report ->
                report.namaKegiatan.contains(query, ignoreCase = true) ||
                    report.kategoriLabel.contains(query, ignoreCase = true) ||
                    report.deskripsi.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onBack = onBack,
                placeholder = "Cari laporan..."
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
                uiState.isLoading && allReports.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.isError && allReports.isEmpty() -> {
                    ErrorState(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { viewModel.loadLaporanList(context) }
                    )
                }

                searchQuery.isBlank() -> {
                    SearchHintState()
                }

                filteredReports.isEmpty() -> {
                    EmptyState(message = "Tidak ada laporan yang cocok dengan pencarian")
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            SectionHeading(
                                title = "Hasil Pencarian",
                                subtitle = "${filteredReports.size} laporan ditemukan"
                            )
                        }

                        items(filteredReports, key = { it.id }) { report ->
                            ReportCard(
                                report = report,
                                onClick = { onReportClick(report.id.toString()) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    placeholder: String
) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali"
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(placeholder) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryLight,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Composable
private fun SearchHintState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Cari laporan kegiatan",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Cari nama kegiatan, kategori, atau deskripsi laporan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReportSummaryStrip(
    totalCount: Int,
    draftCount: Int,
    pendingCount: Int,
    approvedCount: Int,
    attentionCount: Int
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 10.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SummaryStatCard(
                modifier = Modifier.width(92.dp),
                value = totalCount.toString(),
                label = "Total",
                accent = MaterialTheme.colorScheme.primary
            )
        }
        item {
            SummaryStatCard(
                modifier = Modifier.width(92.dp),
                value = attentionCount.toString(),
                label = "Perlu",
                accent = StatusRejected
            )
        }
        item {
            SummaryStatCard(
                modifier = Modifier.width(92.dp),
                value = draftCount.toString(),
                label = "Draft",
                accent = MaterialTheme.colorScheme.secondary
            )
        }
        item {
            SummaryStatCard(
                modifier = Modifier.width(92.dp),
                value = pendingCount.toString(),
                label = "Diajukan",
                accent = StatusPending
            )
        }
        item {
            SummaryStatCard(
                modifier = Modifier.width(100.dp),
                value = approvedCount.toString(),
                label = "Disetujui",
                accent = StatusApproved
            )
        }
    }
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    accent: Color
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(accent)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ReportListHeaderBar(
    title: String,
    totalDisplayed: Int,
    periodLabel: String,
    isFiltered: Boolean,
    showPeriodFilter: Boolean,
    onOpenFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "$totalDisplayed laporan ditampilkan",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showPeriodFilter) {
            FilterChip(
                selected = isFiltered,
                onClick = onOpenFilter,
                label = {
                    Text(
                        text = periodLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun ReportOwnerTabs(
    selected: ReportOwnerScope,
    subordinateActionCount: Int,
    onSelect: (ReportOwnerScope) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                ReportOwnerScope.MINE to "Saya",
                ReportOwnerScope.SUBORDINATES to "Bawahan"
            ).forEach { (scope, label) ->
                val isSelected = selected == scope
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    onClick = { onSelect(scope) }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            if (scope == ReportOwnerScope.SUBORDINATES && subordinateActionCount > 0) {
                                Badge(containerColor = StatusRejected) {
                                    Text(
                                        text = if (subordinateActionCount > 99) "99+" else subordinateActionCount.toString(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selected: FilterType,
    onSelect: (FilterType) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == FilterType.ALL,
                onClick = { onSelect(FilterType.ALL) },
                label = { Text("Semua") }
            )
        }
        item {
            FilterChip(
                selected = selected == FilterType.DRAFT,
                onClick = { onSelect(FilterType.DRAFT) },
                label = { Text("Draft") },
                leadingIcon = {
                    Dot(MaterialTheme.colorScheme.secondary)
                }
            )
        }
        item {
            FilterChip(
                selected = selected == FilterType.PENDING,
                onClick = { onSelect(FilterType.PENDING) },
                label = { Text("Diajukan") },
                leadingIcon = {
                    Dot(StatusPending)
                }
            )
        }
        item {
            FilterChip(
                selected = selected == FilterType.APPROVED,
                onClick = { onSelect(FilterType.APPROVED) },
                label = { Text("Disetujui") },
                leadingIcon = {
                    Dot(StatusApproved)
                }
            )
        }
        item {
            FilterChip(
                selected = selected == FilterType.REJECTED,
                onClick = { onSelect(FilterType.REJECTED) },
                label = { Text("Ditolak") },
                leadingIcon = {
                    Dot(StatusRejected)
                }
            )
        }
        item {
            FilterChip(
                selected = selected == FilterType.REVISED,
                onClick = { onSelect(FilterType.REVISED) },
                label = { Text("Perlu Revisi") },
                leadingIcon = {
                    Dot(StatusRevised)
                }
            )
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
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
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Coba Lagi")
        }
    }
}

@Composable
private fun ReportCard(
    report: ReportUi,
    showEmployeeName: Boolean = false,
    isSupervisorMode: Boolean = false,
    onClick: () -> Unit
) {
    val needsAttention = if (isSupervisorMode) {
        report.needsSupervisorAttention()
    } else {
        report.needsEmployeeAttention()
    }
    val accentColor = when (report.status) {
        StatusType.DRAFT -> MaterialTheme.colorScheme.secondary
        StatusType.REJECTED -> StatusRejected
        StatusType.REVISED -> StatusRevised
        StatusType.PENDING -> StatusPending
        StatusType.APPROVED -> StatusApproved
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (needsAttention) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Text(
                        text = report.attentionLabel(isSupervisorMode),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.namaKegiatan,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (showEmployeeName && !report.pegawaiNama.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = report.pegawaiNama,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReportMetaItem(
                            icon = Icons.Default.CalendarToday,
                            text = report.tanggalLabel
                        )
                        ReportMetaItem(
                            icon = Icons.Default.AccessTime,
                            text = report.jamLabel
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(status = report.status)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryChip(label = report.kategoriLabel)
                ReportMetaItem(
                    icon = Icons.Default.AccessTime,
                    text = "${report.durasiMenit} menit"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            NextActionHint(
                report = report,
                accentColor = accentColor,
                isSupervisorMode = isSupervisorMode
            )

            if (needsAttention && !report.catatanAtasan.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                SupervisorNoteBlock(
                    title = report.supervisorNoteTitle(),
                    note = report.catatanAtasan,
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
private fun NextActionHint(
    report: ReportUi,
    accentColor: Color,
    isSupervisorMode: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = accentColor.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = report.nextActionIcon(),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(17.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = report.nextActionTitle(isSupervisorMode),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = accentColor
                )
                Text(
                    text = report.nextActionText(isSupervisorMode),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun SupervisorNoteBlock(
    title: String,
    note: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.12f))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Comment,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = accentColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = accentColor
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.84f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReportMetaItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)
        )
    }
}

@Composable
private fun CategoryChip(label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PrimaryLight.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Category,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = PrimaryLight
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = PrimaryLight,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrintPreviewDialog(
    title: String,
    month: Int,
    year: Int,
    total: Int,
    isLoading: Boolean,
    errorText: String?,
    pdfFile: File?,
    onDismiss: () -> Unit,
    onGenerate: () -> Unit,
    onPrint: (File) -> Unit,
    autoGenerate: Boolean,
) {
    // generate PDF first time dialog opened (kalau belum ada)
    LaunchedEffect(autoGenerate) {
        if (autoGenerate) onGenerate()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Bulan: ${monthNameId(month)} $year • Total: $total laporan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!errorText.isNullOrBlank()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Menyiapkan PDF...")
                    }
                } else if (pdfFile != null && pdfFile.exists()) {
                    val bmp = renderFirstPageToBitmap(pdfFile) // Bitmap
                    val ratio = remember(bmp) {
                        if (bmp != null) bmp.width.toFloat() / bmp.height.toFloat() else (842f / 595f)
                    }

                    if (bmp != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(ratio)
                                .height(360.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Preview PDF",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Text("Preview tidak tersedia, tapi file PDF sudah dibuat.")
                    }
                } else {
                    Text("PDF belum tersedia.")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val f = pdfFile
                    if (f != null && f.exists()) onPrint(f)
                },
                enabled = !isLoading && pdfFile != null && pdfFile.exists()
            ) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cetak")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss) { Text("Tutup") }
                TextButton(onClick = onGenerate, enabled = !isLoading) { Text("Generate Ulang") }
            }
        }
    )
}

/** Render first page pdf -> Bitmap (untuk preview) */
private fun renderFirstPageToBitmap(file: File): Bitmap? {
    return try {
        val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val page = renderer.openPage(0)

        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        page.close()
        renderer.close()
        pfd.close()

        bitmap
    } catch (_: Exception) {
        null
    }
}

/** Print PDF via Android Print Framework */
private fun printPdf(context: Context, file: File, jobName: String) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val adapter = PdfFilePrintAdapter(file)

    val attrs = PrintAttributes.Builder()
        .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asLandscape()) // ✅ landscape
        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
        .build()

    printManager.print(jobName, adapter, attrs)
}


private class PdfFilePrintAdapter(private val file: File) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: android.os.CancellationSignal?,
        callback: LayoutResultCallback,
        extras: android.os.Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val info = PrintDocumentInfo.Builder(file.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()

        callback.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<android.print.PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: android.os.CancellationSignal,
        callback: WriteResultCallback
    ) {
        try {
            file.inputStream().use { input ->
                FileOutputStream(destination.fileDescriptor).use { output ->
                    input.copyTo(output)
                }
            }
            callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback.onWriteFailed(e.message)
        }
    }
}

private class PdfPager(
    private val doc: PdfDocument,
    private val pageWidth: Int,
    private val pageHeight: Int
) {
    var pageNumber: Int = 1
        private set

    var page: PdfDocument.Page? = null
        private set

    var canvas: Canvas? = null
        private set

    fun startPage() {
        finishPageIfAny()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page = doc.startPage(pageInfo)
        canvas = page!!.canvas
        pageNumber++
    }

    fun finishPageIfAny() {
        page?.let { doc.finishPage(it) }
        page = null
        canvas = null
    }

    fun close() {
        finishPageIfAny()
        doc.close()
    }
}

private fun generateLaporanPdfTppTemplate(
    context: Context,
    bulan: Int,
    tahun: Int,
    laporan: List<ReportUi>,
    asn: PersonBlock,
    atasan: PersonBlock,
    skpdTitle: String,
    kota: String = "Merauke",
    @DrawableRes ttdResId: Int? = null,
    @DrawableRes stempelResId: Int? = null
): File {
    val pageWidth = 842
    val pageHeight = 595
    val margin = 24f

    val doc = PdfDocument()
    val pager = PdfPager(doc, pageWidth, pageHeight)

    // Paints
    val paintBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = GColor.BLACK
    }
    val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        color = GColor.BLACK
    }
    val paintSmall = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        color = GColor.BLACK
    }
    val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1f
        style = Paint.Style.STROKE
        color = GColor.BLACK
    }

    // Helpers
    fun measure(p: Paint, s: String) = p.measureText(s)
    fun wrapText(p: Paint, text: String, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("")
        val words = text.trim().split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var current = ""
        for (w in words) {
            val test = if (current.isEmpty()) w else "$current $w"
            if (measure(p, test) <= maxWidth) current = test
            else {
                if (current.isNotEmpty()) lines.add(current)
                current = w
            }
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines
    }

    fun drawTextBlock(canvas: Canvas, x: Float, y: Float, p: Paint, text: String, maxWidth: Float, lineHeight: Float): Float {
        val lines = wrapText(p, text, maxWidth)
        var yy = y
        for (ln in lines) {
            canvas.drawText(ln, x, yy, p)
            yy += lineHeight
        }
        return yy
    }

    // Layout Constants
    val lineHeight = 12f
    val cellPadY = 6f
    val footerReserve = 150f
    val colW = floatArrayOf(28f, 86f, 160f, 220f, 60f, 85f, 95f, 60f)
    val colX = FloatArray(colW.size + 1).apply {
        this[0] = margin
        for (i in colW.indices) this[i + 1] = this[i] + colW[i]
    }

    var tableTopYInPage = 0f // Untuk melacak awal merge TTD di tiap halaman

    fun drawMiniHeader(canvas: Canvas, yStart: Float): Float {
        canvas.drawText("VERIFIKASI AKTIVITAS BAWAHAN", margin, yStart, paintBold)
        val periodText = "Total Aktivitas Kerja pada bulan ${monthNameId(bulan)} $tahun"
        val periodX = (pageWidth / 2f) - (measure(paintText, periodText) / 2f)
        canvas.drawText(periodText, periodX, yStart, paintText)
        return yStart + 16f
    }

    fun drawTableHeader(canvas: Canvas, yStart: Float): Float {
        val headerH = 26f
        canvas.drawRect(colX[0], yStart, colX[8], yStart + headerH, paintLine)
        for (i in 1 until colX.size) {
            canvas.drawLine(colX[i], yStart, colX[i], yStart + headerH, paintLine)
        }
        val yt = yStart + 13f
        val yt2 = yStart + 23f

        // Header Text Alignment
        fun drawH(txt: String, x0: Float, x1: Float, y: Float) {
            canvas.drawText(txt, x0 + (x1 - x0 - measure(paintBold, txt)) / 2, y, paintBold)
        }
        drawH("NO", colX[0], colX[1], yStart + 16f)
        drawH("JAM", colX[1], colX[2], yStart + 16f)
        drawH("AKTIVITAS", colX[2], colX[3], yStart + 16f)
        drawH("KETERANGAN", colX[3], colX[4], yStart + 16f)
        drawH("SATUAN", colX[4], colX[5], yStart + 16f)
        drawH("LAMA", colX[5], colX[6], yt); drawH("(MENIT)", colX[5], colX[6], yt2)
        drawH("CATATAN", colX[6], colX[7], yStart + 16f)
        drawH("TTD", colX[7], colX[8], yt); drawH("VALIDATOR", colX[7], colX[8], yt2)

        return yStart + headerH
    }

    // Initial Page
    pager.startPage()
    var canvas = pager.canvas!!

    // --- DRAW FULL HEADER (Logo & Employee Data) ---
    val isNonAsn = asn.jabatan.isNonAsnJabatan()
    val reportTitle = if (isNonAsn) {
        "FORMULIR LAPORAN AKTIVITAS KERJA"
    } else {
        "FORMULIR LAPORAN AKTIVITAS KERJA TPP"
    }
    val employeeColumnTitle = if (isNonAsn) "Pegawai Non-ASN" else "ASN"
    var y = margin + 12f
    canvas.drawText("TAHUN ANGGARAN $tahun", margin, y, paintBold)
    canvas.drawText(skpdTitle, margin, y + 14f, paintBold)
    canvas.drawText("KABUPATEN MERAUKE", margin, y + 28f, paintBold)
    val centerX = (pageWidth / 2f) - (measure(paintBold, reportTitle) / 2f)
    canvas.drawText(reportTitle, centerX, y, paintBold)

    y += 46f
    val bLX = margin + 300f; val bRX = margin + 560f
    canvas.drawText(employeeColumnTitle, bLX, y, paintBold); canvas.drawText("Atasan Langsung", bRX, y, paintBold)
    y += 14f
    fun dKV(x: Float, y0: Float, k: String, v: String) = canvas.drawText("$k : $v", x, y0, paintText).run { y0 + 12f }
    val yL = dKV(bLX, dKV(bLX, dKV(bLX, y, "Nama", asn.nama), "NIP", asn.nip), "Jabatan", asn.jabatan)
    val yR = dKV(bRX, dKV(bRX, dKV(bRX, y, "Nama", atasan.nama), "NIP", atasan.nip), "Jabatan", atasan.jabatan)
    y = maxOf(yL, yR) + 10f

    y = drawMiniHeader(canvas, y)
    y = drawTableHeader(canvas, y)
    tableTopYInPage = y

    // --- DRAW ROWS ---
    laporan.forEachIndexed { idx, item ->
        val aL = wrapText(paintText, item.namaKegiatan, colW[2] - 12f)
        val kL = wrapText(paintText, item.deskripsi, colW[3] - 12f)
        val cL = wrapText(paintText, item.catatanAtasan ?: "", colW[6] - 12f)
        val rowH = (maxOf(aL.size, kL.size, cL.size, 1) * lineHeight) + (cellPadY * 2)

        if (y + rowH > (pageHeight - margin - footerReserve)) {
            // Close TTD Merge for current page before switching
            canvas.drawRect(colX[7], tableTopYInPage, colX[8], y, paintLine)

            pager.startPage()
            canvas = pager.canvas!!
            y = drawMiniHeader(canvas, margin + 16f)
            y = drawTableHeader(canvas, y)
            tableTopYInPage = y
        }

        val top = y
        val bottom = y + rowH

        // Draw Cell Borders (Except Horizontal lines for TTD column)
        canvas.drawLine(colX[0], bottom, colX[7], bottom, paintLine) // Horizontal line up to col 7
        for (i in 0..7) { // Vertical lines for cols 0 to 7
            canvas.drawLine(colX[i], top, colX[i], bottom, paintLine)
        }
        // Vertical line for the very end of table
        canvas.drawLine(colX[8], top, colX[8], bottom, paintLine)

        // Draw Content
        val ty = top + cellPadY + 10f
        canvas.drawText((idx + 1).toString(), colX[0] + 8f, ty, paintText)
        canvas.drawText(item.jamLabel, colX[1] + 8f, ty, paintText)
        var ay = ty; aL.forEach { canvas.drawText(it, colX[2] + 6f, ay, paintText); ay += lineHeight }
        var ky = ty; kL.forEach { canvas.drawText(it, colX[3] + 6f, ky, paintText); ky += lineHeight }
        canvas.drawText("1 Keg", colX[4] + 8f, ty, paintText)
        canvas.drawText(item.durasiMenit.toString(), colX[5] + 8f, ty, paintText)
        var cy = ty; cL.forEach { canvas.drawText(it, colX[6] + 6f, cy, paintText); cy += lineHeight }

        y = bottom
    }

    // Finalize TTD Merge for the last page
    canvas.drawRect(colX[7], tableTopYInPage, colX[8], y, paintLine)

    // --- FOOTER ---
    y += 18f
    canvas.drawText("Keterangan", margin, y, paintBold)
    var ky = y + 14f
    listOf("1. Kolom aktivitas...", "2. Kolom keterangan...", "3. Kolom satuan...", "4. Kolom waktu...", "5. Kolom catatan...", "6. Kolom validator...").forEach {
        ky = drawTextBlock(canvas, margin, ky, paintSmall, it, 420f, 12f) + 2f
    }

    val signX = pageWidth - margin - 320f
    canvas.drawText("$kota, ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Calendar.getInstance().time)}", signX, y, paintText)
    canvas.drawText("Atasan Langsung", signX, y + 14f, paintText)

    // TTD / Nama Atasan
    val nameY = y + 94f
    canvas.drawText(atasan.nama, signX, nameY, paintBold)
    canvas.drawText("NIP. ${atasan.nip}", signX, nameY + 14f, paintText)

    pager.finishPageIfAny()
    val outFile = File(context.cacheDir, "laporan_tpp_${bulan}_${tahun}.pdf")
    FileOutputStream(outFile).use { doc.writeTo(it) }
    doc.close()
    return outFile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthYearFilterDialog(
    initialMonth: Int?,
    initialYear: Int?,
    onDismiss: () -> Unit,
    onApply: (month: Int, year: Int) -> Unit,
    onClear: () -> Unit
) {
    val now = remember { Calendar.getInstance() }
    val defaultMonth = initialMonth ?: (now.get(Calendar.MONTH) + 1)
    val defaultYear = initialYear ?: now.get(Calendar.YEAR)

    var month by remember { mutableStateOf(defaultMonth) }
    var year by remember { mutableStateOf(defaultYear) }

    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val months = remember {
        listOf(
            1 to "Januari", 2 to "Februari", 3 to "Maret", 4 to "April",
            5 to "Mei", 6 to "Juni", 7 to "Juli", 8 to "Agustus",
            9 to "September", 10 to "Oktober", 11 to "November", 12 to "Desember"
        )
    }
    val years = remember {
        val current = now.get(Calendar.YEAR)
        (current - 5..current + 1).toList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Laporan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = !monthExpanded }
                ) {
                    OutlinedTextField(
                        value = months.first { it.first == month }.second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bulan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        months.forEach { (m, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { month = m; monthExpanded = false }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = yearExpanded,
                    onExpandedChange = { yearExpanded = !yearExpanded }
                ) {
                    OutlinedTextField(
                        value = year.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tahun") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString()) },
                                onClick = { year = y; yearExpanded = false }
                            )
                        }
                    }
                }

                Text(
                    "Pilih bulan dan tahun untuk menampilkan laporan kegiatan bulanan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = { Button(onClick = { onApply(month, year) }) { Text("Terapkan") } },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClear) { Text("Reset") }
                TextButton(onClick = onDismiss) { Text("Batal") }
            }
        }
    )
}

private fun monthNameId(month: Int): String = when (month) {
    1 -> "Januari"
    2 -> "Februari"
    3 -> "Maret"
    4 -> "April"
    5 -> "Mei"
    6 -> "Juni"
    7 -> "Juli"
    8 -> "Agustus"
    9 -> "September"
    10 -> "Oktober"
    11 -> "November"
    12 -> "Desember"
    else -> "?"
}

/* ----------------------------- Mapping + helpers ----------------------------- */

private fun LaporanKegiatan.toUi(): ReportUi {
    val statusEnum = mapStatusToEnum(statusLaporan)
    val tanggalLabel = formatDateId(tanggalKegiatan)

    val kategori = kategoriNama ?: "Kategori"
    val jam = "${waktuMulai.take(5)} - ${waktuSelesai.take(5)}"
    val durasi = durasiMenit ?: 0

    return ReportUi(
        id = laporanId,
        tanggalLabel = tanggalLabel,
        pegawaiNama = pegawaiNama,
        namaKegiatan = namaKegiatan,
        kategoriLabel = kategori,
        jamLabel = jam,
        durasiMenit = durasi,
        deskripsi = deskripsiKegiatan, // di model API kamu non-null
        status = statusEnum,
        catatanAtasan = catatanVerifikator
    )
}

private fun filterTypeFromRoute(value: String?): FilterType {
    return when (value?.trim()?.lowercase(Locale.getDefault())) {
        "draft" -> FilterType.DRAFT
        "pending", "diajukan" -> FilterType.PENDING
        "approved", "diverifikasi", "disetujui" -> FilterType.APPROVED
        "rejected", "ditolak" -> FilterType.REJECTED
        "revised", "revisi" -> FilterType.REVISED
        else -> FilterType.ALL
    }
}

private fun ReportUi.needsEmployeeAttention(): Boolean {
    return status == StatusType.DRAFT ||
        status == StatusType.REJECTED ||
        status == StatusType.REVISED
}

private fun ReportUi.needsSupervisorAttention(): Boolean {
    return status == StatusType.PENDING || status == StatusType.REVISED
}

private fun ReportUi.attentionLabel(isSupervisorMode: Boolean = false): String {
    if (isSupervisorMode) {
        return when (status) {
            StatusType.PENDING -> "Perlu review"
            StatusType.REVISED -> "Menunggu perbaikan"
            StatusType.REJECTED -> "Sudah ditolak"
            StatusType.DRAFT -> "Masih draft"
            StatusType.APPROVED -> "Sudah disetujui"
        }
    }
    return when (status) {
        StatusType.DRAFT -> "Belum diajukan"
        StatusType.REVISED -> "Perlu revisi"
        StatusType.REJECTED -> "Ditolak"
        else -> "Perlu tindakan"
    }
}

private fun ReportUi.nextActionText(isSupervisorMode: Boolean = false): String {
    if (isSupervisorMode) {
        return when (status) {
            StatusType.DRAFT -> "Laporan bawahan masih draft dan belum diajukan."
            StatusType.PENDING -> "Buka detail untuk memverifikasi atau mengembalikan laporan dengan catatan."
            StatusType.APPROVED -> "Laporan bawahan sudah disetujui. Tidak ada aksi lanjutan."
            StatusType.REJECTED -> "Laporan bawahan sudah ditolak."
            StatusType.REVISED -> "Laporan sudah dikembalikan. Pantau sampai bawahan mengajukan ulang."
        }
    }
    return when (status) {
        StatusType.DRAFT -> "Laporan masih draft. Buka detail, lengkapi data, lalu ajukan."
        StatusType.PENDING -> "Laporan sudah dikirim ke atasan. Tunggu verifikasi."
        StatusType.APPROVED -> "Laporan sudah disetujui. Tidak ada aksi lanjutan."
        StatusType.REJECTED -> "Laporan ditolak. Buka detail untuk membaca alasan penolakan."
        StatusType.REVISED -> "Atasan meminta perbaikan. Buka detail, ikuti catatan, lalu ajukan kembali."
    }
}

private fun ReportUi.nextActionTitle(isSupervisorMode: Boolean = false): String {
    if (isSupervisorMode) {
        return when (status) {
            StatusType.DRAFT -> "Belum diajukan"
            StatusType.PENDING -> "Perlu diverifikasi"
            StatusType.APPROVED -> "Sudah selesai"
            StatusType.REJECTED -> "Sudah ditolak"
            StatusType.REVISED -> "Menunggu perbaikan bawahan"
        }
    }
    return when (status) {
        StatusType.DRAFT -> "Belum dikirim ke atasan"
        StatusType.PENDING -> "Menunggu verifikasi"
        StatusType.APPROVED -> "Sudah selesai"
        StatusType.REJECTED -> "Perlu tindak lanjut"
        StatusType.REVISED -> "Perlu diperbaiki"
    }
}

private fun ReportUi.nextActionIcon(): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        StatusType.DRAFT -> Icons.Default.Edit
        StatusType.PENDING -> Icons.Default.AccessTime
        StatusType.APPROVED -> Icons.Default.Description
        StatusType.REJECTED -> Icons.Default.Error
        StatusType.REVISED -> Icons.AutoMirrored.Filled.Comment
    }
}

private fun ReportUi.supervisorNoteTitle(): String {
    return when (status) {
        StatusType.REJECTED -> "Alasan penolakan dari atasan"
        StatusType.REVISED -> "Catatan revisi dari atasan"
        else -> "Catatan atasan untuk diperhatikan"
    }
}

private fun emptyReportMessage(
    selectedFilter: FilterType,
    activePeriodLabel: String?,
    isSubordinateMode: Boolean = false
): String {
    val periodSuffix = activePeriodLabel?.let { " pada $it" }.orEmpty()
    if (isSubordinateMode) {
        return when (selectedFilter) {
            FilterType.ALL -> "Belum ada laporan bawahan"
            FilterType.DRAFT -> "Belum ada laporan bawahan yang masih draft"
            FilterType.PENDING -> "Belum ada laporan bawahan yang sedang diajukan"
            FilterType.APPROVED -> "Belum ada laporan bawahan yang sudah disetujui"
            FilterType.REJECTED -> "Belum ada laporan bawahan yang ditolak"
            FilterType.REVISED -> "Belum ada laporan bawahan yang perlu revisi"
        }
    }
    return when (selectedFilter) {
        FilterType.ALL -> "Belum ada laporan kegiatan$periodSuffix"
        FilterType.DRAFT -> "Belum ada laporan draft$periodSuffix"
        FilterType.PENDING -> "Belum ada laporan yang sedang diajukan$periodSuffix"
        FilterType.APPROVED -> "Belum ada laporan yang sudah disetujui$periodSuffix"
        FilterType.REJECTED -> "Belum ada laporan yang ditolak$periodSuffix"
        FilterType.REVISED -> "Belum ada laporan yang perlu revisi$periodSuffix"
    }
}

private fun formatDateId(dateString: String): String {
    return try {
        val datePart = dateString.split("T")[0]
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val date = inputFormat.parse(datePart)
        date?.let { outputFormat.format(it) } ?: datePart
    } catch (_: Exception) {
        dateString.split("T")[0]
    }
}

private fun mapStatusToEnum(status: String): StatusType {
    return when (status.lowercase()) {
        "draft" -> StatusType.DRAFT
        "pending", "diajukan" -> StatusType.PENDING
        "diverifikasi", "approved", "verified" -> StatusType.APPROVED
        "ditolak", "rejected" -> StatusType.REJECTED
        "revisi", "perlu revisi", "revised", "revision" -> StatusType.REVISED
        else -> StatusType.PENDING
    }
}

@Composable
private fun EmptyState(
    message: String,
    onAction: (() -> Unit)? = null,
    actionText: String = "Action"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        if (onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onAction, shape = RoundedCornerShape(12.dp)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(actionText)
            }
        }
    }
}
