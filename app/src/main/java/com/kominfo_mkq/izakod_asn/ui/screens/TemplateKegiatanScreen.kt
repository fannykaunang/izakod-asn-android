package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.material3.Switch
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatan
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateRequest
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TemplateKegiatanViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TemplateKegiatanScreen(
    onNavigateBack: () -> Unit,
    onTemplateClick: (TemplateKegiatan) -> Unit,
    viewModel: TemplateKegiatanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var filterPublic by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    // dialog states
    var showFormDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<TemplateKegiatan?>(null) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deletingTemplate by remember { mutableStateOf<TemplateKegiatan?>(null) }

    // snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.actionMessage) {
        val msg = uiState.actionMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeActionMessage()
        }
    }

    // Load templates on screen open
    LaunchedEffect(Unit) {
        viewModel.loadTemplates()
    }

    val filteredTemplates = remember(uiState.templates, searchQuery, filterPublic) {
        val keyword = searchQuery.trim()
        uiState.templates.filter { template ->
            val matchesSearch = keyword.isBlank() || listOfNotNull(
                template.namaTemplate,
                template.deskripsi,
                template.kategoriNama,
                template.targetOutputDefault,
                template.lokasiDefault
            ).any { it.contains(keyword, ignoreCase = true) }
            val matchesFilter = !filterPublic || template.isPublic == 1
            matchesSearch && matchesFilter
        }
    }

// kategori list from existing templates (buat dropdown add/edit)
    val kategoriOptions = remember(uiState.templates) {
        uiState.templates
            .map { it.kategoriId to (it.kategoriNama ?: "Kategori ${it.kategoriId}") }
            .distinctBy { it.first }
            .sortedBy { it.second }
    }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isLoading,
        onRefresh = { viewModel.loadTemplates() }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TemplateKegiatanTopBar(
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                filterActive = filterPublic,
                onSearchQueryChange = { searchQuery = it },
                onToggleSearch = { isSearchActive = true },
                onCloseSearch = {
                    isSearchActive = false
                    searchQuery = ""
                },
                onOpenFilter = { showFilterDialog = true },
                onBack = onNavigateBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTemplate = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (uiState.isMutating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Template", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .pullRefresh(pullRefreshState)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TemplateOverviewCard(
                    total = uiState.templates.size,
                    publicCount = uiState.templates.count { it.isPublic == 1 },
                    privateCount = uiState.templates.count { it.isPublic == 0 },
                    onCreate = {
                        editingTemplate = null
                        showFormDialog = true
                    },
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
                )

                when {
                    uiState.isLoading -> TemplateKegiatanLoadingContent()
                    uiState.isError -> TemplateKegiatanErrorContent(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { viewModel.loadTemplates() }
                    )
                    filteredTemplates.isEmpty() -> EmptyContent(
                        message = if (searchQuery.isNotEmpty())
                            "Tidak ada template yang cocok dengan pencarian"
                        else
                            "Belum ada template kegiatan",
                        actionLabel = if (searchQuery.isBlank()) "Buat Template" else null,
                        onAction = if (searchQuery.isBlank()) {
                            {
                                editingTemplate = null
                                showFormDialog = true
                            }
                        } else null
                    )
                    else -> {
                        TemplateList(
                            templates = filteredTemplates,
                            onUse = onTemplateClick,
                            onEdit = { t ->
                                editingTemplate = t
                                showFormDialog = true
                            },
                            onDelete = { t ->
                                deletingTemplate = t
                                showDeleteConfirm = true
                            }
                        )
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

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            filterPublic = filterPublic,
            onFilterChange = { filterPublic = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    // ✅ Add/Edit dialog
    if (showFormDialog) {
        TemplateFormDialog(
            kategoriOptions = kategoriOptions,
            initial = editingTemplate,
            onDismiss = { showFormDialog = false },
            onSubmit = { req ->
                showFormDialog = false
                val edited = editingTemplate
                if (edited == null) viewModel.createTemplate(req)
                else viewModel.updateTemplate(edited.templateId, req)
            }
        )
    }

    // ✅ Delete confirm
    if (showDeleteConfirm) {
        val t = deletingTemplate
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Hapus Template?") },
            text = { Text("Template \"${t?.namaTemplate ?: "-"}\" akan dihapus permanen.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        t?.let { viewModel.deleteTemplate(it.templateId) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateKegiatanTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    filterActive: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit,
    onCloseSearch: () -> Unit,
    onOpenFilter: () -> Unit,
    onBack: () -> Unit
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
                            text = "Cari template...",
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
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Template Kegiatan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Pakai pola laporan yang sering dibuat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
        },
        actions = {
            IconButton(
                onClick = if (isSearchActive) onCloseSearch else onToggleSearch
            ) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchActive) "Tutup pencarian" else "Cari template"
                )
            }
            IconButton(onClick = onOpenFilter) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = if (filterActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun TemplateOverviewCard(
    total: Int,
    publicCount: Int,
    privateCount: Int,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Template siap pakai",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Pilih template untuk mengisi laporan lebih cepat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = onCreate,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Buat")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TemplateOverviewStat("Total", total.toString(), Modifier.weight(1f))
                TemplateOverviewStat("Umum", publicCount.toString(), Modifier.weight(1f))
                TemplateOverviewStat("Pribadi", privateCount.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TemplateOverviewStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
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
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun TemplateList(
    templates: List<TemplateKegiatan>,
    onUse: (TemplateKegiatan) -> Unit,
    onEdit: (TemplateKegiatan) -> Unit,
    onDelete: (TemplateKegiatan) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val groupedTemplates = templates.groupBy { it.kategoriNama ?: "Lainnya" }

        groupedTemplates.forEach { (kategori, templateList) ->
            item {
                Text(
                    text = kategori,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(templateList, key = { it.templateId }) { template ->
                TemplateCard(
                    template = template,
                    onUse = { onUse(template) },
                    onEdit = { onEdit(template) },
                    onDelete = { onDelete(template) }
                )
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: TemplateKegiatan,
    onUse: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = template.namaTemplate,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!template.deskripsi.isNullOrBlank()) {
                        Text(
                            text = template.deskripsi,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TemplateVisibilityBadge(isPublic = template.isPublic == 1)
            }

            if (!template.targetOutputDefault.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Output laporan",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = template.targetOutputDefault,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailChip(
                    icon = Icons.Default.Category,
                    text = template.kategoriNama ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailChip(
                    icon = Icons.Default.Timer,
                    text = template.estimasiDurasi?.let { "$it mnt" } ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailChip(
                    icon = Icons.Default.Repeat,
                    text = "${template.jumlah_penggunaan ?: 0}x",
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onUse,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Gunakan")
                }
                FilledTonalIconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit template")
                }
                FilledTonalIconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus template",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (template.isPublic == 0 && !template.unitKerja.isNullOrBlank()) {
                DetailChip(icon = Icons.Default.Business, text = template.unitKerja)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateFormDialog(
    kategoriOptions: List<Pair<Int, String>>,
    initial: TemplateKegiatan?,
    onDismiss: () -> Unit,
    onSubmit: (TemplateKegiatanCreateRequest) -> Unit
) {
    val isEdit = initial != null

    var nama by remember { mutableStateOf(initial?.namaTemplate.orEmpty()) }
    var deskripsi by remember { mutableStateOf(initial?.deskripsi.orEmpty()) }
    var targetOutput by remember { mutableStateOf(initial?.targetOutputDefault.orEmpty()) }
    var lokasi by remember { mutableStateOf(initial?.lokasiDefault.orEmpty()) }
    var durasiText by remember { mutableStateOf((initial?.estimasiDurasi ?: 60).toString()) }
    var isPublic by remember { mutableStateOf(initial?.isPublic == 1) }

    var kategoriId by remember { mutableIntStateOf(initial?.kategoriId ?: (kategoriOptions.firstOrNull()?.first ?: 0)) }
    var kategoriExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Template" else "Buat Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama template*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Kategori: dropdown jika ada opsi, kalau tidak ada, input manual
                if (kategoriOptions.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = kategoriExpanded,
                        onExpandedChange = { kategoriExpanded = !kategoriExpanded }
                    ) {
                        OutlinedTextField(
                            value = kategoriOptions.firstOrNull { it.first == kategoriId }?.second ?: "Kategori $kategoriId",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori*") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = kategoriExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = kategoriExpanded,
                            onDismissRequest = { kategoriExpanded = false }
                        ) {
                            kategoriOptions.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        kategoriId = id
                                        kategoriExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = kategoriId.toString(),
                        onValueChange = { kategoriId = it.toIntOrNull() ?: 0 },
                        label = { Text("Kategori ID*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Catatan singkat") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetOutput,
                    onValueChange = { targetOutput = it },
                    label = { Text("Output yang biasa dihasilkan") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    label = { Text("Lokasi bawaan") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durasiText,
                    onValueChange = { durasiText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Perkiraan durasi (menit)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Template umum")
                        Text(
                            text = "Bisa dipakai pegawai lain bila diizinkan sistem.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = isPublic, onCheckedChange = { isPublic = it })
                }

                Text(
                    text = "* wajib diisi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isBlank() || kategoriId <= 0) return@Button

                    onSubmit(
                        TemplateKegiatanCreateRequest(
                            namaTemplate = nama.trim(),
                            kategoriId = kategoriId,
                            deskripsiTemplate = deskripsi.trim().ifBlank { null },
                            targetOutputDefault = targetOutput.trim().ifBlank { null },
                            lokasiDefault = lokasi.trim().ifBlank { null },
                            durasiEstimasiMenit = durasiText.toIntOrNull() ?: 60,
                            isPublic = if (isPublic) 1 else 0,
                            unitKerjaAkses = null,
                            isActive = 1
                        )
                    )
                }
            ) { Text(if (isEdit) "Simpan" else "Buat") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun TemplateVisibilityBadge(isPublic: Boolean) {
    val label = if (isPublic) "Umum" else "Pribadi"
    val icon = if (isPublic) Icons.Default.Public else Icons.Default.Lock
    val color = if (isPublic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.12f),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FilterDialog(
    filterPublic: Boolean,
    onFilterChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Template") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFilterChange(!filterPublic) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hanya template umum")
                    Checkbox(
                        checked = filterPublic,
                        onCheckedChange = onFilterChange
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
private fun TemplateKegiatanLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Memuat template...")
        }
    }
}

@Composable
private fun TemplateKegiatanErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                "Terjadi Kesalahan",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
private fun EmptyContent(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(actionLabel)
                }
            }
        }
    }
}
