// ReportListScreen.kt (REFACTORED)

package com.kominfo_mkq.izakod_asn.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
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
import com.kominfo_mkq.izakod_asn.ui.components.StatusBadge
import com.kominfo_mkq.izakod_asn.ui.components.StatusType
import com.kominfo_mkq.izakod_asn.ui.theme.*
import com.kominfo_mkq.izakod_asn.ui.viewmodel.LaporanListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

// alias supaya tidak bentrok dengan androidx.compose.ui.graphics.Color
import android.graphics.Color as GColor

enum class FilterType { ALL, PENDING, APPROVED, REJECTED, REVISED }

/** UI model yang dipakai list + PDF (jangan pakai API model langsung di UI) */
private data class ReportUi(
    val id: Int,
    val tanggalLabel: String,
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
    onCreateReport: () -> Unit,
    viewModel: LaporanListViewModel = viewModel()
) {
    @Suppress("UNUSED_PARAMETER")

    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FilterType.ALL) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // Print preview state
    var showPrintPreview by remember { mutableStateOf(false) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }
    var pdfError by remember { mutableStateOf<String?>(null) }

    // Load data when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadLaporanList(context)
        viewModel.loadAtasanPegawai(context)
    }

    val doRefresh = remember(uiState.filterBulan, uiState.filterTahun) {
        {
            if (uiState.filterBulan != null && uiState.filterTahun != null) {
                viewModel.loadLaporanBulanan(context, uiState.filterBulan!!, uiState.filterTahun!!)
            } else {
                viewModel.loadLaporanList(context)
            }
        }
    }

    val allReports: List<ReportUi> = remember(uiState.laporanList) {
        uiState.laporanList.map { it.toUi() }
    }

    val filteredReports = remember(allReports, searchQuery, selectedFilter) {
        allReports
            .asSequence()
            .filter { r ->
                val q = searchQuery.trim()
                if (q.isEmpty()) true else {
                    r.namaKegiatan.contains(q, ignoreCase = true) ||
                            r.kategoriLabel.contains(q, ignoreCase = true) ||
                            r.deskripsi.contains(q, ignoreCase = true)
                }
            }
            .filter { r ->
                when (selectedFilter) {
                    FilterType.ALL -> true
                    FilterType.PENDING -> r.status == StatusType.PENDING
                    FilterType.APPROVED -> r.status == StatusType.APPROVED
                    FilterType.REJECTED -> r.status == StatusType.REJECTED
                    FilterType.REVISED -> r.status == StatusType.REVISED
                }
            }
            .toList()
    }

    // Pull-to-refresh state
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = doRefresh
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Laporan Kegiatan",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    // ✅ PRINT button
                    IconButton(onClick = { showPrintPreview = true }) {
                        Icon(Icons.Default.Print, contentDescription = "Print")
                    }

                    // Filter button + dot indicator
                    val isFilterActive = (uiState.filterBulan != null && uiState.filterTahun != null)
                    IconButton(onClick = { showFilterDialog = true }) {
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
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateReport,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Buat Laporan") },
                containerColor = PrimaryLight,
                contentColor = Color.White
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
                uiState.isError -> ErrorState(
                    message = uiState.errorMessage ?: "Terjadi kesalahan",
                    onRetry = doRefresh
                )

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        SearchBar(
                            value = searchQuery,
                            onChange = { searchQuery = it },
                            onClear = { searchQuery = "" }
                        )

                        FilterChipsRow(
                            selected = selectedFilter,
                            onSelect = { selectedFilter = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pullRefresh(pullRefreshState)
                        ) {
                            if (filteredReports.isEmpty()) {
                                EmptyState(
                                    message = if (searchQuery.isNotEmpty())
                                        "Tidak ada laporan yang cocok dengan pencarian"
                                    else
                                        "Belum ada laporan kegiatan",
                                    onAction = if (searchQuery.isEmpty()) onCreateReport else null,
                                    actionText = "Buat Laporan"
                                )
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "${filteredReports.size} Laporan",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }

                                    items(filteredReports, key = { it.id }) { report ->
                                        ReportCard(
                                            report = report,
                                            onClick = { onReportClick(report.id.toString()) }
                                        )
                                    }

                                    item { Spacer(modifier = Modifier.height(80.dp)) }
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
    if (showFilterDialog) {
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
                viewModel.clearFilter(context)
            }
        )
    }

    // ✅ PRINT PREVIEW
    if (showPrintPreview) {
        val bulan = uiState.filterBulan ?: (Calendar.getInstance().get(Calendar.MONTH) + 1)
        val tahun = uiState.filterTahun ?: Calendar.getInstance().get(Calendar.YEAR)

        PrintPreviewDialog(
            title = "Preview Cetak",
            month = bulan,
            year = tahun,
            total = filteredReports.size,
            isLoading = isGeneratingPdf,
            errorText = pdfError,
            pdfFile = pdfFile,
            onDismiss = {
                showPrintPreview = false
                pdfFile = null
                pdfError = null
                isGeneratingPdf = false
            },
            onGenerate = {
                scope.launch {
                    isGeneratingPdf = true
                    pdfError = null

                    try {
                        // 1) Pastikan data atasan sudah diload
                        if (viewModel.uiState.value.atasanPegawai == null) {
                            viewModel.loadAtasanPegawai(context)
                        }

                        // 2) Tunggu sampai state terisi (maks 10 detik biar tidak nge-hang)
                        val data = withTimeout(10_000) {
                            snapshotFlow { viewModel.uiState.value.atasanPegawai }
                                .filterNotNull()
                                .first()
                        }

                        val asn = PersonBlock(
                            nama = data.pegawaiNama.orEmpty(),
                            nip = data.pegawaiNip.orEmpty(),
                            jabatan = data.pegawaiJabatan.orEmpty(),
//                            jabatan = listOfNotNull(data.pegawaiJabatan)
//                                .joinToString(" - ")
//                                .ifEmpty { "-" }
                        )

                        val atasan = PersonBlock(
                            nama = data.atasanPegawaiNama.orEmpty(),      // sesuaikan nama field modelmu
                            nip = data.atasanPegawaiNip.orEmpty(),
                            jabatan = data.atasanPegawaiJabatan.orEmpty().ifEmpty { "-" }
                        )

                        val skpdTitle = data.pegawaiSkpd?.trim().orEmpty()
                            .ifEmpty { "DINAS / UNIT KERJA" }
                            .uppercase()

                        // 3) Generate PDF (jangan di Main thread)
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
                        e.printStackTrace()
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
private fun SearchBar(
    value: String,
    onChange: (String) -> Unit,
    onClear: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        placeholder = { Text("Cari laporan...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
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
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.namaKegiatan,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = report.tanggalLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))
                StatusBadge(status = report.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryLight
                    )
                    Text(
                        text = report.kategoriLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = PrimaryLight
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${report.durasiMenit} menit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if ((report.status == StatusType.REJECTED || report.status == StatusType.REVISED) &&
                !report.catatanAtasan.isNullOrBlank()
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (report.status == StatusType.REJECTED) StatusRejected.copy(alpha = 0.1f)
                            else StatusRevised.copy(alpha = 0.1f)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (report.status == StatusType.REJECTED) StatusRejected else StatusRevised
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.catatanAtasan,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
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
    onPrint: (File) -> Unit
) {
    // generate PDF first time dialog opened (kalau belum ada)
    LaunchedEffect(Unit) { onGenerate() }

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
                Text("Print")
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
    // A4 Landscape @ ~72dpi
    val pageWidth = 842
    val pageHeight = 595
    val margin = 24f

    val doc = PdfDocument()
    val pager = PdfPager(doc, pageWidth, pageHeight)

    val paintTitle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = GColor.BLACK
    }
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
        return lines.ifEmpty { listOf("") }
    }

    fun drawTextBlock(
        canvas: Canvas,
        x: Float,
        y: Float,
        p: Paint,
        text: String,
        maxWidth: Float,
        lineHeight: Float
    ): Float {
        val lines = wrapText(p, text, maxWidth)
        var yy = y
        for (ln in lines) {
            canvas.drawText(ln, x, yy, p)
            yy += lineHeight
        }
        return yy
    }

    fun nowId(): String {
        val fmt = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return fmt.format(Calendar.getInstance().time)
    }

    // ----- layout constants -----
    val lineHeight = 12f
    val cellPadY = 6f
    val footerReserve = 150f

    // table columns (total harus pas)
    val tableLeft = margin
    val tableRight = pageWidth - margin
    val tableWidth = tableRight - tableLeft
    val colW = floatArrayOf(
        28f,   // NO
        86f,   // JAM
        160f,  // AKTIVITAS
        220f,  // KETERANGAN
        60f,   // SATUAN
        85f,   // LAMA
        95f,   // CATATAN
        60f    // TTD
    )
    // validasi cepat (optional)
    // require(colW.sum() <= tableWidth + 0.5f) { "Lebar kolom melebihi tableWidth" }

    val colX = FloatArray(colW.size + 1)
    colX[0] = tableLeft
    for (i in colW.indices) colX[i + 1] = colX[i] + colW[i]

    val periodText = "Total Aktivitas Kerja pada bulan ${monthNameId(bulan)} $tahun"

    fun drawMiniHeader(canvas: Canvas, yStart: Float): Float {
        var y = yStart
        canvas.drawText("VERIFIKASI AKTIVITAS BAWAHAN", margin, y, paintBold)
        val periodX = (pageWidth / 2f) - (measure(paintText, periodText) / 2f)
        canvas.drawText(periodText, periodX, y, paintText)
        return y + 16f
    }

    fun drawFullHeader(canvas: Canvas): Float {
        var y = margin + 12f

        // kiri
        canvas.drawText("TAHUN ANGGARAN $tahun", margin, y, paintBold)
        canvas.drawText(skpdTitle, margin, y + 14f, paintBold)
        canvas.drawText("KABUPATEN MERAUKE", margin, y + 28f, paintBold)

        // tengah
        val centerTitle = "FORMULIR LAPORAN AKTIVITAS KERJA TPP"
        val centerX = (pageWidth / 2f) - (measure(paintBold, centerTitle) / 2f)
        canvas.drawText(centerTitle, centerX, y, paintBold)

        y += 46f

        // blok 2 kolom (ASN & Atasan)
        val blockLeftX = margin + 300f
        val blockRightX = margin + 560f

        canvas.drawText("ASN", blockLeftX, y, paintBold)
        canvas.drawText("Atasan Langsung", blockRightX, y, paintBold)
        y += 14f

        fun drawKV(x: Float, y0: Float, key: String, value: String): Float {
            canvas.drawText(key, x, y0, paintText)
            canvas.drawText(": $value", x + 60f, y0, paintText)
            return y0 + 12f
        }

        var yL = y
        var yR = y
        yL = drawKV(blockLeftX, yL, "Nama", asn.nama)
        yL = drawKV(blockLeftX, yL, "NIP", asn.nip)
        yL = drawKV(blockLeftX, yL, "Jabatan", asn.jabatan)

        yR = drawKV(blockRightX, yR, "Nama", atasan.nama)
        yR = drawKV(blockRightX, yR, "NIP", atasan.nip)
        yR = drawKV(blockRightX, yR, "Jabatan", atasan.jabatan)

        y = maxOf(yL, yR) + 10f

        y = drawMiniHeader(canvas, y)
        return y
    }

    fun drawTableHeader(canvas: Canvas, yStart: Float): Float {
        var y = yStart
        val headerTop = y
        val headerH = 26f

        canvas.drawRect(tableLeft, headerTop, tableRight, headerTop + headerH, paintLine)
        for (i in 1 until colX.size) {
            canvas.drawLine(colX[i], headerTop, colX[i], headerTop + headerH, paintLine)
        }

        fun drawHeaderText(text: String, x0: Float, x1: Float, yText: Float) {
            val tw = measure(paintBold, text)
            val xx = x0 + ((x1 - x0) - tw) / 2f
            canvas.drawText(text, xx, yText, paintBold)
        }
        // TODO template kegiatan bikin bisa di input sendiri
        // TODO bikin template kegiatan swipe down refresh
        val yText1 = headerTop + 16f
        drawHeaderText("NO", colX[0], colX[1], yText1)
        drawHeaderText("JAM", colX[1], colX[2], yText1)
        drawHeaderText("AKTIVITAS", colX[2], colX[3], yText1)
        drawHeaderText("KETERANGAN AKTIVITAS", colX[3], colX[4], yText1)
        drawHeaderText("SATUAN", colX[4], colX[5], yText1)
        drawHeaderText("LAMA WAKTU", colX[5], colX[6], headerTop + 13f)
        drawHeaderText("(MENIT)", colX[5], colX[6], headerTop + 23f)
        drawHeaderText("CATATAN ATASAN", colX[6], colX[7], yText1)
        drawHeaderText("TTD", colX[7], colX[8], headerTop + 13f)
        drawHeaderText("VALIDATOR", colX[7], colX[8], headerTop + 23f)

        return headerTop + headerH
    }

    fun needNewPage(currentY: Float, required: Float): Boolean {
        return currentY + required > (pageHeight - margin - footerReserve)
    }

    // ----- start first page -----
    pager.startPage()
    var canvas = pager.canvas!!
    var y = drawFullHeader(canvas)
    y = drawTableHeader(canvas, y)

    fun startNextTablePage() {
        pager.startPage()
        canvas = pager.canvas!!
        y = margin + 16f
        y = drawMiniHeader(canvas, y)
        y = drawTableHeader(canvas, y)
    }

    fun drawRow(index: Int, item: ReportUi) {
        val aktivitasLines = wrapText(paintText, item.namaKegiatan, colW[2] - 12f)
        val ketLines = wrapText(paintText, item.deskripsi, colW[3] - 12f)
        val catLines = wrapText(paintText, item.catatanAtasan ?: "", colW[6] - 12f)

        val maxLines = maxOf(aktivitasLines.size, ketLines.size, catLines.size, 1)
        val rowH = (maxLines * lineHeight) + (cellPadY * 2)

        if (needNewPage(y, rowH + 2f)) startNextTablePage()

        val top = y
        val bottom = y + rowH

        // row border
        canvas.drawRect(tableLeft, top, tableRight, bottom, paintLine)
        for (i in 1 until colX.size) {
            canvas.drawLine(colX[i], top, colX[i], bottom, paintLine)
        }

        val textY0 = top + cellPadY + 10f
        fun drawLines(lines: List<String>, x: Float) {
            var yy = textY0
            for (ln in lines) {
                canvas.drawText(ln, x, yy, paintText)
                yy += lineHeight
            }
        }

        canvas.drawText((index + 1).toString(), colX[0] + 8f, textY0, paintText)
        canvas.drawText(item.jamLabel, colX[1] + 8f, textY0, paintText)
        drawLines(aktivitasLines, colX[2] + 6f)
        drawLines(ketLines, colX[3] + 6f)
        canvas.drawText("1 Keg", colX[4] + 8f, textY0, paintText)
        canvas.drawText(item.durasiMenit.toString(), colX[5] + 8f, textY0, paintText)
        drawLines(catLines, colX[6] + 6f)
        // kolom TTD per baris dibiarkan kosong

        y = bottom
    }

    laporan.forEachIndexed { idx, item -> drawRow(idx, item) }

    // footer (keterangan + tanda tangan)
    fun ensureFooterSpace(required: Float) {
        if (y + required > pageHeight - margin) {
            pager.startPage()
            canvas = pager.canvas!!
            y = margin + 16f
        }
    }

    ensureFooterSpace(140f)
    y += 18f

    // Keterangan (kiri bawah)
    canvas.drawText("Keterangan", margin, y, paintBold)
    val bullets = listOf(
        "1. Kolom aktivitas diisi dengan aktivitas pokok (misal: Apel Pagi, Mengkonsep, Merencanakan, Melaksanakan tugas tambahan, dll)",
        "2. Kolom keterangan aktivitas diisi dengan penjelasan atas aktivitas pokok yang dikerjakan (misal: Mengkonsep apa, Merencanakan apa, Melakukan tugas tambahan apa, dst)",
        "3. Kolom satuan diisi dengan (1 dokumen atau 1 aktivitas / kegiatan)",
        "4. Kolom waktu diisi dengan satuan waktu menit untuk masing2 aktivitas",
        "5. Kolom catatan diisi oleh atasan langsung",
        "6. Kolom validator diisi oleh atasan"
    )

    var ky = y + 14f
    val ketMaxWidth = 420f
    bullets.forEach { b ->
        ky = drawTextBlock(canvas, margin, ky, paintSmall, b, ketMaxWidth, 12f) + 2f
    }

    // Tanda tangan (kanan bawah)
    val signBlockW = 320f
    val signX = pageWidth - margin - signBlockW
    var sy = y

    canvas.drawText("$kota, ${nowId()}", signX, sy, paintText)
    sy += 14f
    canvas.drawText("Atasan Langsung", signX, sy, paintText)
    sy += 8f

    val ttdBitmap = ttdResId?.let { BitmapFactory.decodeResource(context.resources, it) }
    val stempelBitmap = stempelResId?.let { BitmapFactory.decodeResource(context.resources, it) }

    val signAreaTop = sy + 6f

    if (ttdBitmap != null) {
        val maxW = 160f
        val maxH = 60f
        val scale = min(maxW / ttdBitmap.width, maxH / ttdBitmap.height)
        val w = ttdBitmap.width * scale
        val h = ttdBitmap.height * scale
        val dst = RectF(signX, signAreaTop, signX + w, signAreaTop + h)
        canvas.drawBitmap(ttdBitmap, null, dst, null)
    } else {
        // fallback garis tanda tangan
        canvas.drawLine(signX, signAreaTop + 50f, signX + 180f, signAreaTop + 50f, paintLine)
    }

    if (stempelBitmap != null) {
        val maxW = 90f
        val maxH = 90f
        val scale = min(maxW / stempelBitmap.width, maxH / stempelBitmap.height)
        val w = stempelBitmap.width * scale
        val h = stempelBitmap.height * scale
        val dst = RectF(signX + 120f, signAreaTop + 5f, signX + 120f + w, signAreaTop + 5f + h)
        val alphaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 180 }
        canvas.drawBitmap(stempelBitmap, null, dst, alphaPaint)
    }

    val nameY = signAreaTop + 80f
    canvas.drawText(atasan.nama, signX, nameY, paintBold)
    canvas.drawText("NIP. ${atasan.nip}", signX, nameY + 14f, paintText)

    // Finish & save
    pager.finishPageIfAny()
    val outFile = File(context.cacheDir, "laporan_tpp_${tahun}_${bulan}.pdf")
    FileOutputStream(outFile).use { fos -> doc.writeTo(fos) }
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
        namaKegiatan = namaKegiatan,
        kategoriLabel = kategori,
        jamLabel = jam,
        durasiMenit = durasi,
        deskripsi = deskripsiKegiatan, // di model API kamu non-null
        status = statusEnum,
        catatanAtasan = catatanVerifikator
    )
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
        "pending", "diajukan", "draft" -> StatusType.PENDING
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
