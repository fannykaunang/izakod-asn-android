package com.kominfo_mkq.izakod_asn.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kominfo_mkq.izakod_asn.ui.viewmodel.CreateLaporanViewModel
import com.kominfo_mkq.izakod_asn.data.model.KategoriKegiatan
import java.text.SimpleDateFormat
import java.util.*

/**
 * CreateLaporanScreen - Form untuk membuat laporan kegiatan baru
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLaporanScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateLaporanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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

    LaunchedEffect(Unit) {
        android.util.Log.d("CreateLaporanScreen", "🆕 Screen opened, resetting success")
        viewModel.resetSuccess()
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

            kotlinx.coroutines.delay(500)
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
            TopAppBar(
                title = {
                    Text(
                        "Buat Laporan Kegiatan",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // Info Alert
            if (!uiState.hasCheckedAttendance) {
                InfoAlert()
            }

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

            // Target & Output Section
            TargetOutputSection(
                targetOutput = uiState.targetOutput,
                hasilOutput = uiState.hasilOutput,
                onTargetChange = { viewModel.updateTargetOutput(it) },
                onHasilChange = { viewModel.updateHasilOutput(it) }
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

            // Location Section
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

            // Participants Section
            ParticipantsSection(
                pesertaKegiatan = uiState.pesertaKegiatan,
                jumlahPeserta = uiState.jumlahPeserta,
                onPesertaChange = { viewModel.updatePesertaKegiatan(it) },
                onJumlahChange = { viewModel.updateJumlahPeserta(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Problems & Solutions Section
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

            // Link Reference Section
            LinkSection(
                linkReferensi = uiState.linkReferensi,
                onLinkChange = { viewModel.updateLinkReferensi(it) }
            )

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
                    "Informasi Penting:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "• Pastikan Anda sudah melakukan absensi untuk tanggal kegiatan\n" +
                            "• Field bertanda * wajib diisi\n" +
                            "• Anda dapat menyimpan sebagai Draft atau langsung Mengajukan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
// TODO bikin halaman template
@Composable
private fun ErrorAlert(
    message: String,
    requiresAttendance: Boolean,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (requiresAttendance) "Belum Absen" else "Error",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
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
                    Icon(Icons.Default.Send, contentDescription = null)
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
    } catch (e: Exception) {
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