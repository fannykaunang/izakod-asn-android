package com.kominfo_mkq.izakod_asn.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.KategoriKegiatan
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.viewmodel.EditLaporanViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLaporanScreen(
    laporanId: String,
    onNavigateBack: () -> Unit,
    viewModel: EditLaporanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showAdditionalDetails by rememberSaveable { mutableStateOf(false) }
    var showSupportingDetails by rememberSaveable { mutableStateOf(false) }

    // Load laporan when screen opens
    LaunchedEffect(laporanId) {
        viewModel.loadLaporan(laporanId.toInt())
    }

    // Show toast for errors
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    // Show toast for success and navigate back
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, "Laporan berhasil diperbarui!", Toast.LENGTH_SHORT).show()
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
                title = "Edit Laporan",
                compact = true,
                onBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoadingData -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Memuat data laporan...")
                        }
                    }
                }
                uiState.loadError -> {
                    // Error loading
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
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text("Gagal memuat data")
                            Button(onClick = { viewModel.loadLaporan(laporanId.toInt()) }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                else -> {
                    // Form content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        EditFormHeroCard(
                            statusLaporan = uiState.statusLaporan
                        )

                        // Form Sections
                        Spacer(modifier = Modifier.height(16.dp))

                        // Basic Information Section
                        BasicInformationSection(
                            tanggalKegiatan = uiState.tanggalKegiatan,
                            kategoriId = uiState.kategoriId,
                            namaKegiatan = uiState.namaKegiatan,
                            deskripsiKegiatan = uiState.deskripsiKegiatan,
                            kategoris = uiState.kategoris,
                            errors = uiState.errors,
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
                            subtitle = "Lokasi, target hasil, dan peserta bisa diperbarui bila memang diperlukan.",
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
                            subtitle = "Tambahkan kendala, solusi, atau link pendukung jika laporan perlu dilengkapi.",
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

                            LinkSection(
                                linkReferensi = uiState.linkReferensi,
                                onLinkChange = { viewModel.updateLinkReferensi(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        EditActionButtons(
                            isLoading = uiState.isUpdating,
                            onUpdate = { viewModel.updateLaporan(context) },
                            onCancel = onNavigateBack
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            android.util.Log.d("DatePicker", "📅 Raw millis: $millis")

                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis

                            // ✅ DEBUG: Log calendar date
                            android.util.Log.d("DatePicker", "📅 Calendar date: ${calendar.time}")
                            android.util.Log.d("DatePicker", "📅 Year: ${calendar.get(Calendar.YEAR)}")
                            android.util.Log.d("DatePicker", "📅 Month: ${calendar.get(Calendar.MONTH) + 1}")
                            android.util.Log.d("DatePicker", "📅 Day: ${calendar.get(Calendar.DAY_OF_MONTH)}")

                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH) + 1
                            val day = calendar.get(Calendar.DAY_OF_MONTH)

                            val dateString = String.format("%04d-%02d-%02d", year, month, day)

                            android.util.Log.d("DatePicker", "📅 Final string: $dateString")

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

@Composable
private fun EditFormHeroCard(
    statusLaporan: String
) {
    val normalizedStatus = statusLaporan.lowercase(Locale.getDefault())
    val statusLabel = when (normalizedStatus) {
        "revisi", "direvisi" -> "Perlu revisi"
        "ditolak", "rejected" -> "Ditolak"
        "diajukan", "submitted" -> "Menunggu review"
        "disetujui", "approved" -> "Sudah disetujui"
        else -> if (statusLaporan.isNotBlank()) statusLaporan else "Masih bisa diperbarui"
    }
    val statusMessage = when (normalizedStatus) {
        "revisi", "direvisi" -> "Perbarui bagian yang perlu disesuaikan sebelum laporan diajukan kembali."
        "ditolak", "rejected" -> "Cek kembali isi laporan dan sesuaikan data sebelum mencoba mengirim ulang."
        "diajukan", "submitted" -> "Laporan masih bisa diperbarui selama belum masuk proses review akhir."
        "disetujui", "approved" -> "Perubahan sebaiknya dilakukan hanya jika memang masih diperbolehkan oleh alur kerja."
        else -> "Perbarui data yang diperlukan, lalu simpan perubahan saat semua isi laporan sudah tepat."
    }

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
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Perbarui laporan lebih cepat",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "Fokus pada informasi utama dan waktu pelaksanaan terlebih dahulu, lalu lengkapi detail pendukung setelahnya.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormHintChip(
                    text = statusLabel,
                    icon = when (normalizedStatus) {
                        "revisi", "direvisi", "ditolak", "rejected" -> Icons.Default.Warning
                        "disetujui", "approved" -> Icons.Default.CheckCircle
                        else -> Icons.Default.Schedule
                    }
                )
                FormHintChip(
                    text = "Field wajib bertanda *",
                    icon = Icons.Default.TaskAlt
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
                        "Status laporan saat ini",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        statusMessage,
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

@Composable
private fun InfoAlert() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    "Edit Laporan:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "• Ubah data yang diperlukan\n" +
                            "• Field bertanda * wajib diisi\n" +
                            "• Tekan 'Simpan Perubahan' untuk menyimpan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun EditActionButtons(
    isLoading: Boolean,
    onUpdate: () -> Unit,
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
                "Perubahan akan tersimpan pada laporan ini. Pastikan informasi utama dan waktu sudah benar sebelum menekan simpan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Update Button
            Button(
                onClick = onUpdate,
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
                    Spacer(Modifier.width(8.dp))
                    Text("Menyimpan...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Simpan Perubahan")
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicInformationSection(
    tanggalKegiatan: String,
    kategoriId: String,
    namaKegiatan: String,
    deskripsiKegiatan: String,
    kategoris: List<KategoriKegiatan>,
    errors: Map<String, String>,
    onTanggalClick: () -> Unit,
    onKategoriChange: (String) -> Unit,
    onNamaChange: (String) -> Unit,
    onDeskripsiChange: (String) -> Unit
) {
    SectionCard(
        title = "Informasi Dasar",
        icon = Icons.Default.CalendarToday
    ) {
        SectionCaption("Perbarui tanggal, kategori, nama kegiatan, dan deskripsi sebagai inti laporan.")

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
        errors["tanggal_kegiatan"]?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
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
        SectionCaption("Tambahkan target dan hasil jika perlu diperjelas untuk reviewer.")

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
        SectionCaption("Waktu mulai dan selesai sebaiknya akurat agar durasi kegiatan terbaca dengan benar.")

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
        SectionCaption("Sesuaikan lokasi jika tempat pelaksanaan berubah atau perlu diperjelas.")

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
        SectionCaption("Perbarui peserta hanya jika memang ada perubahan yang relevan.")

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
        SectionCaption("Gunakan bagian ini untuk menambahkan konteks kendala yang muncul selama kegiatan.")

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
        SectionCaption("Link pendukung bisa membantu saat laporan perlu dilengkapi kembali.")

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

//@Composable
//private fun ImageUploadSection(
//    selectedImages: List<Uri>,
//    onAddImages: (List<Uri>) -> Unit,
//    onRemoveImage: (Uri) -> Unit
//) {
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetMultipleContents()
//    ) { uris -> if (uris.isNotEmpty()) onAddImages(uris) }
//
//    SectionCard(
//        title = "Foto Kegiatan",
//        icon = Icons.Default.Image
//    ) {
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            Text(
//                "Upload foto kegiatan (maksimal 5 foto)",
//                style = MaterialTheme.typography.bodySmall
//            )
//
//            if (selectedImages.isNotEmpty()) {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    items(selectedImages.size) { index ->
//                        ImagePreview(
//                            uri = selectedImages[index],
//                            onRemove = { onRemoveImage(selectedImages[index]) }
//                        )
//                    }
//                }
//            }
//
//            if (selectedImages.size < 5) {
//                OutlinedButton(
//                    onClick = { imagePickerLauncher.launch("image/*") },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Icon(Icons.Default.AddPhotoAlternate, null)
//                    Spacer(Modifier.width(8.dp))
//                    Text("Tambah Foto (${selectedImages.size}/5)")
//                }
//            }
//        }
//    }
//}

//@Composable
//private fun ImagePreview(uri: Uri, onRemove: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .size(100.dp)
//            .clip(RoundedCornerShape(8.dp))
//    ) {
//        AsyncImage(
//            model = uri,
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//
//        IconButton(
//            onClick = onRemove,
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .size(24.dp)
//                .background(
//                    MaterialTheme.colorScheme.error.copy(0.9f),
//                    CircleShape
//                )
//        ) {
//            Icon(
//                Icons.Default.Close,
//                null,
//                tint = Color.White,
//                modifier = Modifier.size(16.dp)
//            )
//        }
//    }
//}

