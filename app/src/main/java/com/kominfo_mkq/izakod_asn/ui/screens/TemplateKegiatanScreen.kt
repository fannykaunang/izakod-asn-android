package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TemplateKegiatanViewModel
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatan
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateKegiatanScreen(
    onNavigateBack: () -> Unit,
    onTemplateClick: (TemplateKegiatan) -> Unit,
    viewModel: TemplateKegiatanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
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
        uiState.templates.filter { template ->
            val matchesSearch =
                template.deskripsi?.contains(searchQuery, ignoreCase = true) == true
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Template Kegiatan",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (filterPublic) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.loadTemplates() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(16.dp)
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
                        "Belum ada template kegiatan"
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

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Cari template...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
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
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUse),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.namaTemplate,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (template.isPublic == 1) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Public") },
                            leadingIcon = { Icon(Icons.Default.Public, null, modifier = Modifier.size(16.dp)) }
                        )
                        Spacer(Modifier.width(8.dp))
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opsi")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Gunakan template") },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                                onClick = {
                                    menuExpanded = false
                                    onUse()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Default.Edit, null) },
                                onClick = {
                                    menuExpanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Hapus") },
                                leadingIcon = { Icon(Icons.Default.Delete, null) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            if (!template.deskripsi.isNullOrBlank()) {
                Text(
                    text = template.deskripsi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailChip(icon = Icons.Default.Category, text = template.kategoriNama ?: "-")
                template.estimasiDurasi?.let {
                    DetailChip(icon = Icons.Default.Timer, text = "$it menit")
                }
                DetailChip(icon = Icons.Default.Repeat, text = "${template.jumlah_penggunaan ?: 0} kali")
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
        title = { Text(if (isEdit) "Edit Template" else "Tambah Template") },
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
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetOutput,
                    onValueChange = { targetOutput = it },
                    label = { Text("Target output default") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    label = { Text("Lokasi default") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durasiText,
                    onValueChange = { durasiText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Durasi estimasi (menit)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Public")
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
            ) { Text(if (isEdit) "Simpan" else "Tambah") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
private fun DetailChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                    Text("Hanya Template Public")
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
private fun EmptyContent(message: String) {
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
        }
    }
}