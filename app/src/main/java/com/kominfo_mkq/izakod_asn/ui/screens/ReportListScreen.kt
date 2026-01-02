// Updated ReportListScreen.kt - Connect to real API

package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.ui.components.*
import com.kominfo_mkq.izakod_asn.ui.theme.*
import com.kominfo_mkq.izakod_asn.ui.viewmodel.LaporanListViewModel
import com.kominfo_mkq.izakod_asn.data.model.LaporanKegiatan as ApiLaporanKegiatan
import java.text.SimpleDateFormat
import java.util.*

enum class FilterType {
    ALL,
    PENDING,
    APPROVED,
    REJECTED,
    REVISED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportListScreen(
    onBack: () -> Unit,
    onReportClick: (String) -> Unit,
    onCreateReport: () -> Unit,
    reports: List<LaporanKegiatan>, // Keep for backward compatibility
    viewModel: LaporanListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FilterType.ALL) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Load data when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadLaporanList(context)
    }

    // Convert API model to UI model
    val apiReports = remember(uiState.laporanList) {
        uiState.laporanList.map { apiLaporan ->
            LaporanKegiatan(
                id = apiLaporan.laporanId.toString(),
                tanggal = formatDate(apiLaporan.tanggalKegiatan),
                namaKegiatan = apiLaporan.namaKegiatan,
                kategori = apiLaporan.kategoriNama ?: "Kategori",
                durasi = "${apiLaporan.waktuMulai} - ${apiLaporan.waktuSelesai}",
                status = mapStatus(apiLaporan.statusLaporan),
                catatan = apiLaporan.catatanVerifikator
            )
        }
    }

    val filteredReports = remember(apiReports, searchQuery, selectedFilter) {
        apiReports.filter { report ->
            val matchesSearch = report.namaKegiatan.contains(searchQuery, ignoreCase = true) ||
                    report.kategori.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                FilterType.ALL -> true
                FilterType.PENDING -> report.status == StatusType.PENDING
                FilterType.APPROVED -> report.status == StatusType.APPROVED
                FilterType.REJECTED -> report.status == StatusType.REJECTED
                FilterType.REVISED -> report.status == StatusType.REVISED
            }

            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Laporan Kegiatan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    // Refresh button
                    IconButton(
                        onClick = {
                            if (uiState.filterBulan != null && uiState.filterTahun != null) {
                                viewModel.loadLaporanBulanan(
                                    context,
                                    uiState.filterBulan!!,
                                    uiState.filterTahun!!
                                )
                            } else {
                                viewModel.loadLaporanList(context)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }

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
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateReport,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
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
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Memuat laporan...")
                        }
                    }
                }
                uiState.isError -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            uiState.errorMessage ?: "Terjadi kesalahan",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {
                            if (uiState.filterBulan != null && uiState.filterTahun != null) {
                                viewModel.loadLaporanBulanan(context, uiState.filterBulan!!, uiState.filterTahun!!)
                            } else {
                                viewModel.loadLaporanList(context)
                            }
                        }) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Coba Lagi")
                        }
                    }
                }
                else -> {
                    // Success state
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            placeholder = { Text("Cari laporan...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear"
                                        )
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

                        // Filter Chips
                        LazyRow(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedFilter == FilterType.ALL,
                                    onClick = { selectedFilter = FilterType.ALL },
                                    label = { Text("Semua") }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedFilter == FilterType.PENDING,
                                    onClick = { selectedFilter = FilterType.PENDING },
                                    label = { Text("Diajukan") },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(StatusPending)
                                        )
                                    }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedFilter == FilterType.APPROVED,
                                    onClick = { selectedFilter = FilterType.APPROVED },
                                    label = { Text("Disetujui") },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(StatusApproved)
                                        )
                                    }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedFilter == FilterType.REJECTED,
                                    onClick = { selectedFilter = FilterType.REJECTED },
                                    label = { Text("Ditolak") },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(StatusRejected)
                                        )
                                    }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = selectedFilter == FilterType.REVISED,
                                    onClick = { selectedFilter = FilterType.REVISED },
                                    label = { Text("Perlu Revisi") },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(StatusRevised)
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Report List
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
                                        onClick = { onReportClick(report.id) }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
    val defaultMonth = initialMonth ?: (now.get(Calendar.MONTH) + 1) // 1..12
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

                // Bulan
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        months.forEach { (m, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    month = m
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }

                // Tahun
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
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString()) },
                                onClick = {
                                    year = y
                                    yearExpanded = false
                                }
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
        confirmButton = {
            Button(onClick = { onApply(month, year) }) {
                Text("Terapkan")
            }
        },
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


@Composable
fun ReportCard(
    report: LaporanKegiatan,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 2.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = report.namaKegiatan,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = report.tanggal,
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PrimaryLight
                    )
                    Text(
                        text = report.kategori,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = PrimaryLight
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = report.durasi,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            if ((report.status == StatusType.REJECTED || report.status == StatusType.REVISED)
                && report.catatan != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (report.status == StatusType.REJECTED)
                                StatusRejected.copy(alpha = 0.1f)
                            else
                                StatusRevised.copy(alpha = 0.1f)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (report.status == StatusType.REJECTED)
                            StatusRejected
                        else
                            StatusRevised
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = report.catatan,
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

@Composable
fun EmptyState(
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

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(actionText)
            }
        }
    }
}

// Helper functions
private fun formatDate(dateString: String): String {
    return try {
        // ✅ Same fix
        val datePart = dateString.split("T")[0]

        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

        val date = inputFormat.parse(datePart)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.split("T")[0]
    }
}

private fun mapStatus(status: String): StatusType {
    return when (status.lowercase()) {
        "pending", "diajukan", "draft" -> StatusType.PENDING
        "diverifikasi", "approved", "verified" -> StatusType.APPROVED
        "ditolak", "rejected" -> StatusType.REJECTED
        "revisi", "perlu revisi", "revised", "revision" -> StatusType.REVISED
        else -> {
            android.util.Log.w("ReportListScreen", "⚠️ Unknown status: $status, defaulting to PENDING")
            StatusType.PENDING
        }
    }
}

// Keep existing LaporanKegiatan data class for UI
data class LaporanKegiatan(
    val id: String,
    val tanggal: String,
    val namaKegiatan: String,
    val kategori: String,
    val durasi: String,
    val status: StatusType,
    val catatan: String? = null
)