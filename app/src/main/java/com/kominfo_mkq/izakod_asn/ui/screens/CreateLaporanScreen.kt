package com.kominfo_mkq.izakod_asn.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kominfo_mkq.izakod_asn.data.model.KategoriKegiatan
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatan
import com.kominfo_mkq.izakod_asn.data.model.TemplateKegiatanCreateRequest
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.viewmodel.CreateLaporanUiState
import com.kominfo_mkq.izakod_asn.ui.viewmodel.CreateLaporanViewModel
import com.kominfo_mkq.izakod_asn.ui.viewmodel.TemplateKegiatanViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.milliseconds

/**
 * CreateLaporanScreen - Form untuk membuat laporan kegiatan baru
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLaporanScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateLaporanViewModel = viewModel(),
    templateViewModel: TemplateKegiatanViewModel = viewModel(),
    initialTemplate: TemplateKegiatan? = null,
    onInitialTemplateConsumed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val templateUiState by templateViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    DisposableEffect(Unit) {
        onDispose {
            // Only clear if not submitted successfully
            if (!uiState.isSuccess) {
                android.util.Log.d("CreateLaporanScreen", "🗑️ Clearing form on back navigation")
                viewModel.clearForm()
            }
        }
    }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }
    var showAdditionalDetails by rememberSaveable { mutableStateOf(false) }
    var showSupportingDetails by rememberSaveable { mutableStateOf(false) }
    var showTemplatePicker by rememberSaveable { mutableStateOf(false) }
    var showTemplateCreateSheet by rememberSaveable { mutableStateOf(false) }
    var pendingTemplateToApply by remember { mutableStateOf<TemplateKegiatan?>(null) }
    var pendingTemplateWasCreated by rememberSaveable { mutableStateOf(false) }

    fun applyTemplate(template: TemplateKegiatan, wasCreated: Boolean = false) {
        viewModel.loadFromTemplate(template)
        pendingTemplateToApply = null
        pendingTemplateWasCreated = false
        if (!template.targetOutputDefault.isNullOrBlank() || !template.lokasiDefault.isNullOrBlank()) {
            showAdditionalDetails = true
        }
        Toast.makeText(
            context,
            if (wasCreated) "Template dibuat dan diterapkan ke laporan" else "Template diterapkan ke laporan",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun requestApplyTemplate(template: TemplateKegiatan, wasCreated: Boolean = false) {
        showTemplatePicker = false
        showTemplateCreateSheet = false
        if (uiState.hasTemplateOverwriteRisk()) {
            pendingTemplateToApply = template
            pendingTemplateWasCreated = wasCreated
        } else {
            applyTemplate(template, wasCreated)
        }
    }

    LaunchedEffect(Unit) {
        android.util.Log.d("CreateLaporanScreen", "🆕 Screen opened, resetting success")
        viewModel.startFreshForm()
        initialTemplate?.let { template ->
            viewModel.loadFromTemplate(template)
            onInitialTemplateConsumed()
        }
        viewModel.resetSuccess()
        templateViewModel.loadTemplates()
        hasNavigated = false
    }

    // ✅ Show Toast for errors
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_LONG  // Use LONG for validation errors
            ).show()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess && !hasNavigated) {
            android.util.Log.d("CreateLaporanScreen", "✅ Success! Showing toast and navigating...")
            Toast.makeText(context, "Laporan berhasil disimpan!", Toast.LENGTH_SHORT).show()

            kotlinx.coroutines.delay(500.milliseconds)
            onNavigateBack()
        }
    }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.getCurrentLocation(context)
        }
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Buat Laporan Kegiatan",
                compact = true,
                onBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            CreateFormHeroCard(
                hasCheckedAttendance = uiState.hasCheckedAttendance
            )

            // Form Sections
            Spacer(modifier = Modifier.height(16.dp))

            TemplateQuickFillSection(
                templates = templateUiState.templates,
                isLoading = templateUiState.isLoading,
                isError = templateUiState.isError,
                errorMessage = templateUiState.errorMessage,
                onOpenPicker = { showTemplatePicker = true },
                onCreateTemplate = { showTemplateCreateSheet = true },
                onRetry = { templateViewModel.loadTemplates() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Basic Information Section
            BasicInformationSection(
                tanggalKegiatan = uiState.tanggalKegiatan,
                kategoriId = uiState.kategoriId,
                namaKegiatan = uiState.namaKegiatan,
                deskripsiKegiatan = uiState.deskripsiKegiatan,
                kategoris = uiState.kategoris,
                errors = uiState.errors,
                dateHelpText = buildLaporanDateHelpText(
                    uiState.minTanggalKegiatan,
                    uiState.maxTanggalKegiatan,
                    uiState.submissionDeadlineDays
                ),
                onTanggalClick = { showDatePicker = true },
                onKategoriChange = { viewModel.updateKategori(it) },
                onNamaChange = { viewModel.updateNamaKegiatan(it) },
                onDeskripsiChange = { viewModel.updateDeskripsi(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Section
            TimeSection(
                waktuMulai = uiState.waktuMulai,
                waktuSelesai = uiState.waktuSelesai,
                errors = uiState.errors,
                onWaktuMulaiChange = { viewModel.updateWaktuMulai(it) },
                onWaktuSelesaiChange = { viewModel.updateWaktuSelesai(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LaporanTargetRelationSection(
                targetKinerjaList = uiState.targetKinerjaList,
                selectedTargetKinerjaId = uiState.selectedTargetKinerjaId,
                selectedTargetKinerjaDetailId = uiState.selectedTargetKinerjaDetailId,
                isLoading = uiState.isLoadingTargetKinerja,
                onTargetChange = { viewModel.updateTargetKinerja(it) },
                onDetailChange = { viewModel.updateTargetKinerjaDetail(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableSectionCard(
                title = "Tambah detail laporan",
                subtitle = "Lokasi, target hasil, dan peserta bisa diisi jika diperlukan.",
                isExpanded = showAdditionalDetails,
                onToggle = { showAdditionalDetails = !showAdditionalDetails }
            ) {
                LocationSection(
                    lokasiKegiatan = uiState.lokasiKegiatan,
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    gettingLocation = uiState.gettingLocation,
                    onLokasiChange = { viewModel.updateLokasiKegiatan(it) },
                    onGetLocation = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) -> {
                                viewModel.getCurrentLocation(context)
                            }
                            else -> {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TargetOutputSection(
                    targetOutput = uiState.targetOutput,
                    hasilOutput = uiState.hasilOutput,
                    onTargetChange = { viewModel.updateTargetOutput(it) },
                    onHasilChange = { viewModel.updateHasilOutput(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ParticipantsSection(
                    pesertaKegiatan = uiState.pesertaKegiatan,
                    jumlahPeserta = uiState.jumlahPeserta,
                    onPesertaChange = { viewModel.updatePesertaKegiatan(it) },
                    onJumlahChange = { viewModel.updateJumlahPeserta(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExpandableSectionCard(
                title = "Dokumentasi & pendukung",
                subtitle = "Kendala, solusi, foto, dan link referensi bisa ditambahkan untuk laporan yang lebih lengkap.",
                isExpanded = showSupportingDetails,
                onToggle = { showSupportingDetails = !showSupportingDetails }
            ) {
                ProblemsSection(
                    kendala = uiState.kendala,
                    solusi = uiState.solusi,
                    onKendalaChange = { viewModel.updateKendala(it) },
                    onSolusiChange = { viewModel.updateSolusi(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ImageUploadSection(
                    selectedImages = uiState.selectedImages,
                    onAddImages = { viewModel.addImages(it) },
                    onRemoveImage = { viewModel.removeImage(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinkSection(
                    linkReferensi = uiState.linkReferensi,
                    onLinkChange = { viewModel.updateLinkReferensi(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            ActionButtons(
                isLoading = uiState.isLoading,
                onSaveDraft = { viewModel.submitLaporan(context, "Draft") },
                onSubmit = { viewModel.submitLaporan(context, "Diajukan") },
                onCancel = onNavigateBack
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showTemplatePicker) {
        TemplatePickerBottomSheet(
            templates = templateUiState.templates,
            isLoading = templateUiState.isLoading,
            isError = templateUiState.isError,
            errorMessage = templateUiState.errorMessage,
            onRetry = { templateViewModel.loadTemplates() },
            onDismiss = { showTemplatePicker = false },
            onSelect = { template ->
                requestApplyTemplate(template)
            }
        )
    }

    if (showTemplateCreateSheet) {
        TemplateCreateBottomSheet(
            kategoris = uiState.kategoris,
            initialKategoriId = uiState.kategoriId.toIntOrNull(),
            initialName = uiState.namaKegiatan,
            initialDescription = uiState.deskripsiKegiatan,
            initialTargetOutput = uiState.targetOutput,
            initialLocation = uiState.lokasiKegiatan,
            isSaving = templateUiState.isMutating,
            onDismiss = { if (!templateUiState.isMutating) showTemplateCreateSheet = false },
            onSubmit = { request ->
                templateViewModel.createTemplate(request) { createdTemplate ->
                    templateViewModel.consumeActionMessage()
                    requestApplyTemplate(createdTemplate, wasCreated = true)
                }
            }
        )
    }

    pendingTemplateToApply?.let { template ->
        AlertDialog(
            onDismissRequest = {
                pendingTemplateToApply = null
                pendingTemplateWasCreated = false
            },
            title = { Text("Terapkan template?") },
            text = {
                Text(
                    "Template \"${template.namaTemplate}\" akan mengganti kategori, nama kegiatan, deskripsi, target output, dan lokasi yang sudah terisi."
                )
            },
            confirmButton = {
                Button(onClick = { applyTemplate(template, pendingTemplateWasCreated) }) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingTemplateToApply = null
                        pendingTemplateWasCreated = false
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val minDateMillis = remember(uiState.minTanggalKegiatan) {
            dateStringToUtcMillis(uiState.minTanggalKegiatan) ?: Long.MIN_VALUE
        }
        val maxDateMillis = remember(uiState.maxTanggalKegiatan) {
            dateStringToUtcMillis(uiState.maxTanggalKegiatan) ?: Long.MAX_VALUE
        }
        val selectedDateMillis = remember(
            uiState.tanggalKegiatan,
            minDateMillis,
            maxDateMillis
        ) {
            val selected = dateStringToUtcMillis(uiState.tanggalKegiatan)
                ?: System.currentTimeMillis()
            selected.coerceIn(minDateMillis, maxDateMillis)
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis in minDateMillis..maxDateMillis
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis

                            // Format using local timezone
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            // Format as YYYY-MM-DD
                            val dateString = String.format("%04d-%02d-%02d", year, month, day)

                            android.util.Log.d("DatePicker", "✅ Selected: $dateString")

                            viewModel.updateTanggalKegiatan(dateString)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun CreateLaporanUiState.hasTemplateOverwriteRisk(): Boolean {
    return listOf(
        kategoriId,
        namaKegiatan,
        deskripsiKegiatan,
        targetOutput,
        hasilOutput,
        selectedTargetKinerjaId,
        selectedTargetKinerjaDetailId,
        waktuMulai,
        waktuSelesai,
        lokasiKegiatan,
        pesertaKegiatan,
        linkReferensi,
        kendala,
        solusi
    ).any { it.isNotBlank() } ||
        jumlahPeserta.toIntOrNull()?.let { it > 0 } == true ||
        latitude != null ||
        longitude != null ||
        selectedImages.isNotEmpty()
}

private fun dateStringToUtcMillis(dateString: String): Long? {
    val parts = dateString.split("-")
    if (parts.size != 3) return null

    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null

    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(year, month - 1, day)
    }.timeInMillis
}

private fun buildLaporanDateHelpText(
    minDate: String,
    maxDate: String,
    deadlineDays: Int
): String {
    return "Pilih tanggal ${formatDate(minDate)} - ${formatDate(maxDate)}. Maksimal $deadlineDays hari ke belakang dan wajib sudah absen datang."
}

@Composable
private fun TemplateQuickFillSection(
    templates: List<TemplateKegiatan>,
    isLoading: Boolean,
    isError: Boolean,
    errorMessage: String?,
    onOpenPicker: () -> Unit,
    onCreateTemplate: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Pakai Template Kegiatan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = when {
                            isLoading -> "Memuat template yang tersedia..."
                            isError -> errorMessage ?: "Template belum bisa dimuat."
                            templates.isEmpty() -> "Belum ada template yang bisa dipakai."
                            else -> "${templates.size} template siap mengisi form lebih cepat."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onCreateTemplate) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah template kegiatan",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Muat Ulang",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onOpenPicker,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && templates.isNotEmpty(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Pilih Template",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateCreateBottomSheet(
    kategoris: List<KategoriKegiatan>,
    initialKategoriId: Int?,
    initialName: String,
    initialDescription: String,
    initialTargetOutput: String,
    initialLocation: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (TemplateKegiatanCreateRequest) -> Unit
) {
    val firstKategoriId = kategoris.firstOrNull()?.kategoriId ?: 0
    var nama by rememberSaveable { mutableStateOf(initialName) }
    var deskripsi by rememberSaveable { mutableStateOf(initialDescription) }
    var targetOutput by rememberSaveable { mutableStateOf(initialTargetOutput) }
    var lokasi by rememberSaveable { mutableStateOf(initialLocation) }
    var durasiText by rememberSaveable { mutableStateOf("60") }
    var isPublic by rememberSaveable { mutableStateOf(false) }
    var kategoriId by rememberSaveable {
        mutableStateOf(
            initialKategoriId
                ?.takeIf { selectedId -> kategoris.any { it.kategoriId == selectedId } }
                ?: firstKategoriId
        )
    }
    var kategoriExpanded by rememberSaveable { mutableStateOf(false) }

    val selectedKategoriName = kategoris.firstOrNull { it.kategoriId == kategoriId }?.namaKategori
        ?: if (kategoriId > 0) "Kategori $kategoriId" else "Pilih kategori"
    val canSubmit = nama.isNotBlank() && kategoriId > 0 && !isSaving

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Tambah Template Kegiatan",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Template baru akan disimpan lalu langsung dipakai untuk mengisi laporan ini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    enabled = !isSaving
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            if (kategoris.isEmpty()) {
                TemplatePickerMessage(
                    icon = Icons.Default.Warning,
                    title = "Kategori belum tersedia",
                    message = "Muat ulang halaman atau coba beberapa saat lagi sebelum membuat template."
                )
            } else {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nama template*") },
                    placeholder = { Text("Contoh: Rapat Koordinasi Mingguan") },
                    singleLine = true,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                ExposedDropdownMenuBox(
                    expanded = kategoriExpanded,
                    onExpandedChange = { if (!isSaving) kategoriExpanded = !kategoriExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedKategoriName,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !isSaving,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = { Text("Kategori*") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = kategoriExpanded)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded = kategoriExpanded,
                        onDismissRequest = { kategoriExpanded = false }
                    ) {
                        kategoris.forEach { kategori ->
                            DropdownMenuItem(
                                text = { Text(kategori.namaKategori) },
                                onClick = {
                                    kategoriId = kategori.kategoriId
                                    kategoriExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan singkat") },
                    placeholder = { Text("Contoh: Membahas progres pekerjaan dan tindak lanjut.") },
                    enabled = !isSaving,
                    minLines = 2
                )

                OutlinedTextField(
                    value = targetOutput,
                    onValueChange = { targetOutput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Output bawaan") },
                    placeholder = { Text("Contoh: Notulen rapat atau daftar tindak lanjut") },
                    enabled = !isSaving
                )

                OutlinedTextField(
                    value = lokasi,
                    onValueChange = { lokasi = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Lokasi bawaan") },
                    placeholder = { Text("Contoh: Ruang Rapat Lantai 2") },
                    enabled = !isSaving,
                    singleLine = true
                )

                OutlinedTextField(
                    value = durasiText,
                    onValueChange = { value -> durasiText = value.filter { it.isDigit() }.take(4) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Perkiraan durasi (menit)") },
                    placeholder = { Text("Contoh: 60") },
                    enabled = !isSaving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Jadikan template umum",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Matikan agar template hanya menjadi template pribadi/unit kerja.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPublic,
                        onCheckedChange = { isPublic = it },
                        enabled = !isSaving
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
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
                        },
                        enabled = canSubmit,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Simpan")
                    }
                }

                Text(
                    text = "* wajib diisi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplatePickerBottomSheet(
    templates: List<TemplateKegiatan>,
    isLoading: Boolean,
    isError: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onSelect: (TemplateKegiatan) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredTemplates = remember(templates, searchQuery) {
        val keyword = searchQuery.trim().lowercase(Locale.getDefault())
        if (keyword.isBlank()) {
            templates
        } else {
            templates.filter { template ->
                listOfNotNull(
                    template.namaTemplate,
                    template.deskripsi,
                    template.kategoriNama,
                    template.targetOutputDefault,
                    template.lokasiDefault,
                    template.unitKerja
                ).any { it.lowercase(Locale.getDefault()).contains(keyword) }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Pilih Template",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Data template akan mengisi kategori, nama kegiatan, deskripsi, target, dan lokasi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Cari template") },
                    placeholder = { Text("Contoh: rapat koordinasi") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Bersihkan pencarian")
                            }
                        }
                    },
                    singleLine = true
                )

                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                "Memuat template...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    isError -> {
                        TemplatePickerMessage(
                            icon = Icons.Default.Warning,
                            title = "Template gagal dimuat",
                            message = errorMessage ?: "Coba muat ulang daftar template.",
                            actionLabel = "Muat Ulang",
                            onAction = onRetry
                        )
                    }

                    filteredTemplates.isEmpty() -> {
                        TemplatePickerMessage(
                            icon = Icons.Default.SearchOff,
                            title = if (searchQuery.isBlank()) "Belum ada template" else "Template tidak ditemukan",
                            message = if (searchQuery.isBlank()) {
                                "Ketuk tombol tambah di section Pakai Template Kegiatan untuk membuat template baru."
                            } else {
                                "Coba gunakan kata kunci lain."
                            }
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 520.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredTemplates, key = { it.templateId }) { template ->
                                TemplatePickerItem(
                                    template = template,
                                    onClick = { onSelect(template) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplatePickerMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            OutlinedButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun TemplatePickerItem(
    template: TemplateKegiatan,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = template.namaTemplate,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (!template.deskripsi.isNullOrBlank()) {
                        Text(
                            text = template.deskripsi,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                TemplateMiniChip(
                    text = if (template.isPublic == 1) "Umum" else "Pribadi",
                    icon = if (template.isPublic == 1) Icons.Default.Public else Icons.Default.Lock
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    TemplateMiniChip(
                        text = template.kategoriNama ?: "Tanpa kategori",
                        icon = Icons.Default.Category
                    )
                }
                if (template.estimasiDurasi != null) {
                    item {
                        TemplateMiniChip(
                            text = "${template.estimasiDurasi} mnt",
                            icon = Icons.Default.AccessTime
                        )
                    }
                }
            }

            if (!template.targetOutputDefault.isNullOrBlank()) {
                TemplatePreviewLine(
                    icon = Icons.Default.TaskAlt,
                    text = template.targetOutputDefault
                )
            }

            if (!template.lokasiDefault.isNullOrBlank()) {
                TemplatePreviewLine(
                    icon = Icons.Default.Place,
                    text = template.lokasiDefault
                )
            }
        }
    }
}

@Composable
private fun TemplateMiniChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TemplatePreviewLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
        )
    }
}

@Composable
private fun CreateFormHeroCard(
    hasCheckedAttendance: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Mulai dari Informasi Dasar",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "Lengkapi tanggal, kategori, nama kegiatan, deskripsi, dan waktu terlebih dahulu. Detail lain bisa ditambahkan setelah bagian inti selesai.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormHintChip(
                    text = "Field wajib bertanda *",
                    icon = Icons.Default.TaskAlt
                )
                FormHintChip(
                    text = if (hasCheckedAttendance) "Siap diajukan" else "Pastikan sudah absen",
                    icon = if (hasCheckedAttendance) Icons.Default.CheckCircle else Icons.Default.Schedule
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Simpan sesuai kebutuhan",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Gunakan Draft jika laporan belum final. Gunakan Ajukan Laporan jika semua data sudah siap direview.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FormHintChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ExpandableSectionCard(
    title: String,
    subtitle: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SectionCaption(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 14.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicInformationSection(
    tanggalKegiatan: String,
    kategoriId: String,
    namaKegiatan: String,
    deskripsiKegiatan: String,
    kategoris: List<KategoriKegiatan>,
    errors: Map<String, String>,
    dateHelpText: String,
    onTanggalClick: () -> Unit,
    onKategoriChange: (String) -> Unit,
    onNamaChange: (String) -> Unit,
    onDeskripsiChange: (String) -> Unit
) {
    SectionCard(
        title = "Informasi Dasar",
        icon = Icons.Default.CalendarToday
    ) {
        SectionCaption("Isi bagian inti laporan terlebih dahulu agar proses simpan dan review lebih lancar.")

        // Tanggal Kegiatan
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onTanggalClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Tanggal Kegiatan *",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (tanggalKegiatan.isNotEmpty()) {
                            formatDate(tanggalKegiatan)
                        } else {
                            "Pilih Tanggal"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(Icons.Default.CalendarToday, contentDescription = null)
            }
        }
        val tanggalError = errors["tanggal_kegiatan"]
        if (tanggalError != null) {
            Text(
                tanggalError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        } else {
            Text(
                dateHelpText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Kategori
        Text(
            "Kategori Kegiatan *",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = kategoris.find { it.kategoriId.toString() == kategoriId }?.namaKategori ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Pilih Kategori") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = errors.containsKey("kategori_id")
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                kategoris.forEach { kategori ->
                    DropdownMenuItem(
                        text = { Text(kategori.namaKategori) },
                        onClick = {
                            onKategoriChange(kategori.kategoriId.toString())
                            expanded = false
                        }
                    )
                }
            }
        }
        errors["kategori_id"]?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nama Kegiatan
        OutlinedTextField(
            value = namaKegiatan,
            onValueChange = onNamaChange,
            label = { Text("Nama Kegiatan *") },
            placeholder = { Text("Contoh: Rapat Koordinasi Tim") },
            modifier = Modifier.fillMaxWidth(),
            isError = errors.containsKey("nama_kegiatan"),
            supportingText = errors["nama_kegiatan"]?.let { { Text(it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Deskripsi Kegiatan
        OutlinedTextField(
            value = deskripsiKegiatan,
            onValueChange = onDeskripsiChange,
            label = { Text("Deskripsi Kegiatan *") },
            placeholder = { Text("Jelaskan detail kegiatan...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 6,
            isError = errors.containsKey("deskripsi_kegiatan"),
            supportingText = errors["deskripsi_kegiatan"]?.let { { Text(it) } }
        )
    }
}

@Composable
private fun TargetOutputSection(
    targetOutput: String,
    hasilOutput: String,
    onTargetChange: (String) -> Unit,
    onHasilChange: (String) -> Unit
) {
    SectionCard(
        title = "Target & Hasil",
        icon = Icons.Default.CheckCircle
    ) {
        SectionCaption("Tambahkan target dan hasil jika sudah ada. Bagian ini membantu laporan terlihat lebih lengkap.")

        OutlinedTextField(
            value = targetOutput,
            onValueChange = onTargetChange,
            label = { Text("Target Output") },
            placeholder = { Text("Target yang ingin dicapai...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = hasilOutput,
            onValueChange = onHasilChange,
            label = { Text("Hasil Output") },
            placeholder = { Text("Hasil yang telah dicapai...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 4
        )
    }
}

@Composable
private fun TimeSection(
    waktuMulai: String,
    waktuSelesai: String,
    errors: Map<String, String>,
    onWaktuMulaiChange: (String) -> Unit,
    onWaktuSelesaiChange: (String) -> Unit
) {
    // ✅ Local state to maintain cursor position
    var waktuMulaiTF by remember(waktuMulai) {
        mutableStateOf(
            TextFieldValue(
                text = waktuMulai,
                selection = TextRange(waktuMulai.length)
            )
        )
    }

    var waktuSelesaiTF by remember(waktuSelesai) {
        mutableStateOf(
            TextFieldValue(
                text = waktuSelesai,
                selection = TextRange(waktuSelesai.length)
            )
        )
    }

    SectionCard(
        title = "Waktu Pelaksanaan",
        icon = Icons.Default.AccessTime
    ) {
        SectionCaption("Gunakan format jam yang ringkas agar durasi kegiatan terbaca jelas.")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ✅ Waktu Mulai - Fixed cursor jump
            OutlinedTextField(
                value = waktuMulaiTF,
                onValueChange = { newValue ->
                    // Auto-format to HH:mm
                    val digits = newValue.text.filter { it.isDigit() }
                    val formatted = when (digits.length) {
                        0 -> ""
                        1, 2 -> digits
                        3 -> "${digits.substring(0, 2)}:${digits[2]}"
                        else -> "${digits.substring(0, 2)}:${digits.substring(2, 4)}"
                    }

                    // Update local state with cursor at end
                    waktuMulaiTF = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )

                    // Update ViewModel
                    onWaktuMulaiChange(formatted)
                },
                label = { Text("Waktu Mulai *") },
                placeholder = { Text("08:05") },
                modifier = Modifier.weight(1f),
                isError = errors.containsKey("waktu_mulai"),
                supportingText = errors["waktu_mulai"]?.let { { Text(it) } },
                trailingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )

            // ✅ Waktu Selesai - Fixed cursor jump
            OutlinedTextField(
                value = waktuSelesaiTF,
                onValueChange = { newValue ->
                    // Auto-format to HH:mm
                    val digits = newValue.text.filter { it.isDigit() }
                    val formatted = when (digits.length) {
                        0 -> ""
                        1, 2 -> digits
                        3 -> "${digits.substring(0, 2)}:${digits[2]}"
                        else -> "${digits.substring(0, 2)}:${digits.substring(2, 4)}"
                    }

                    // Update local state with cursor at end
                    waktuSelesaiTF = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )

                    // Update ViewModel
                    onWaktuSelesaiChange(formatted)
                },
                label = { Text("Waktu Selesai *") },
                placeholder = { Text("17:00") },
                modifier = Modifier.weight(1f),
                isError = errors.containsKey("waktu_selesai"),
                supportingText = errors["waktu_selesai"]?.let { { Text(it) } },
                trailingIcon = {
                    Icon(Icons.Default.AccessTime, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
        }
    }
}

@Composable
private fun LocationSection(
    lokasiKegiatan: String,
    latitude: Double?,
    longitude: Double?,
    gettingLocation: Boolean,
    onLokasiChange: (String) -> Unit,
    onGetLocation: () -> Unit
) {
    SectionCard(
        title = "Lokasi & Koordinat",
        icon = Icons.Default.Place
    ) {
        SectionCaption("Lokasi membantu memperjelas konteks kegiatan. Koordinat akan terisi saat mengambil lokasi saat ini.")

        OutlinedTextField(
            value = lokasiKegiatan,
            onValueChange = onLokasiChange,
            label = { Text("Lokasi Kegiatan") },
            placeholder = { Text("Contoh: Ruang Rapat Lantai 2") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(Icons.Default.Place, contentDescription = null)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Koordinat Display
        if (latitude != null && longitude != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Koordinat Lokasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Lat: ${String.format("%.6f", latitude)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Lng: ${String.format("%.6f", longitude)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Get Location Button
        Button(
            onClick = onGetLocation,
            modifier = Modifier.fillMaxWidth(),
            enabled = !gettingLocation
        ) {
            if (gettingLocation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mengambil Lokasi...")
            } else {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ambil Lokasi Saat Ini")
            }
        }
    }
}

@Composable
private fun ParticipantsSection(
    pesertaKegiatan: String,
    jumlahPeserta: String,
    onPesertaChange: (String) -> Unit,
    onJumlahChange: (String) -> Unit
) {
    SectionCard(
        title = "Peserta Kegiatan",
        icon = Icons.Default.Person
    ) {
        SectionCaption("Isi jika kegiatan melibatkan tim, rapat, atau peserta lain.")

        OutlinedTextField(
            value = pesertaKegiatan,
            onValueChange = onPesertaChange,
            label = { Text("Peserta Kegiatan") },
            placeholder = { Text("John Doe, Jane Smith") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = jumlahPeserta,
            onValueChange = onJumlahChange,
            label = { Text("Jumlah Peserta") },
            placeholder = { Text("0") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun ProblemsSection(
    kendala: String,
    solusi: String,
    onKendalaChange: (String) -> Unit,
    onSolusiChange: (String) -> Unit
) {
    SectionCard(
        title = "Kendala & Solusi",
        icon = Icons.Default.Warning
    ) {
        SectionCaption("Bagian ini opsional, tetapi berguna jika ada hambatan yang perlu dicatat.")

        OutlinedTextField(
            value = kendala,
            onValueChange = onKendalaChange,
            label = { Text("Kendala") },
            placeholder = { Text("Kendala yang dihadapi (jika ada)...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = solusi,
            onValueChange = onSolusiChange,
            label = { Text("Solusi") },
            placeholder = { Text("Solusi yang dilakukan (jika ada)...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 4
        )
    }
}

@Composable
private fun LinkSection(
    linkReferensi: String,
    onLinkChange: (String) -> Unit
) {
    SectionCard(
        title = "Link Referensi",
        icon = Icons.Default.Link
    ) {
        SectionCaption("Tambahkan tautan dokumen, drive, atau sumber pendukung bila diperlukan.")

        OutlinedTextField(
            value = linkReferensi,
            onValueChange = onLinkChange,
            label = { Text("Link Terkait") },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
        Text(
            "Link dokumen, drive, atau referensi lain yang terkait",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@Composable
private fun ActionButtons(
    isLoading: Boolean,
    onSaveDraft: () -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Laporan masih bisa disimpan sebagai draft sebelum diajukan untuk review.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Submit Button
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mengirim...")
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajukan Laporan")
                }
            }

            // Save Draft Button
            OutlinedButton(
                onClick = onSaveDraft,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Draft")
            }

            // Cancel Button
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Batal")
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Section Content
            content()
        }
    }
}

// Helper function to format date
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (_: Exception) {
        dateString
    }
}

@Composable
private fun ImageUploadSection(
    selectedImages: List<Uri>,
    onAddImages: (List<Uri>) -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris -> if (uris.isNotEmpty()) onAddImages(uris) }

    SectionCard(
        title = "Foto Kegiatan",
        icon = Icons.Default.Image
    ) {
        SectionCaption("Lampiran foto dapat membantu reviewer memahami kegiatan dengan lebih cepat.")

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Upload foto kegiatan (maksimal 5 foto)",
                style = MaterialTheme.typography.bodySmall
            )

            if (selectedImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(selectedImages.size) { index ->
                        ImagePreview(
                            uri = selectedImages[index],
                            onRemove = { onRemoveImage(selectedImages[index]) }
                        )
                    }
                }
            }

            if (selectedImages.size < 5) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah Foto (${selectedImages.size}/5)")
                }
            }
        }
    }
}

@Composable
private fun ImagePreview(uri: Uri, onRemove: () -> Unit) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(
                    MaterialTheme.colorScheme.error.copy(0.9f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
