package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.ui.theme.*
import com.kominfo_mkq.izakod_asn.data.model.CreateReminderRequest
import com.kominfo_mkq.izakod_asn.data.model.Reminder
import com.kominfo_mkq.izakod_asn.data.model.ReminderStats
import com.kominfo_mkq.izakod_asn.ui.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ✅ MAIN REMINDER SCREEN - Complete with list, stats, and FAB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Load reminders when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadReminders()
    }

    // Handle create success
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            showCreateDialog = false
            viewModel.resetCreateSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Pengingat",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading && !uiState.isError) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = PrimaryLight
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Tambah Reminder",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                RemindersLoadingContent()
            }
            uiState.isError -> {
                RemindersErrorContent(
                    message = uiState.errorMessage ?: "Terjadi kesalahan",
                    onRetry = { viewModel.loadReminders() }
                )
            }
            uiState.reminders.isEmpty() -> {
                EmptyReminders()
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stats section
                    item {
                        ReminderStatsSection(stats = uiState.stats)
                    }

                    // Reminder list
                    items(uiState.reminders, key = { it.reminderId }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onDelete = { reminderId ->
                                viewModel.deleteReminder(reminderId)  // ✅ Call delete
                            }
                        )
                    }
                }
            }
        }

        // Create dialog
        if (showCreateDialog) {
            CreateReminderDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { request ->
                    viewModel.createReminder(request)
                }
            )
        }

        // Show loading overlay when creating
        if (uiState.isCreating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

/**
 * ✅ STATS SECTION
 */
@Composable
fun ReminderStatsSection(stats: ReminderStats?) {
    stats ?: return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Statistik",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReminderStatCard("Total", stats.total.toString(), PrimaryLight, Modifier.weight(1f))
            ReminderStatCard("Aktif", stats.active, StatusApproved, Modifier.weight(1f))
            ReminderStatCard("Harian", stats.harian, SecondaryLight, Modifier.weight(1f))
        }
    }
}

@Composable
fun ReminderStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RemindersLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Memuat pengingat...")
        }
    }
}

@Composable
private fun RemindersErrorContent(message: String, onRetry: () -> Unit) {
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
            Text(message, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
private fun EmptyReminders() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.AlarmOff,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Belum ada pengingat",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Tambahkan pengingat dengan tombol + di bawah",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderCard(reminder: Reminder, onDelete: (Int) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (reminder.tipeReminder) {
                        "Harian" -> PrimaryLight.copy(alpha = 0.2f)
                        "Mingguan" -> SecondaryLight.copy(alpha = 0.2f)
                        "Bulanan" -> TertiaryLight.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        reminder.tipeReminder,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // ✅ Delete button
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                reminder.judulReminder,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            // Time
            Row {
                Icon(Icons.Default.AccessTime, null, Modifier.size(16.dp))
                Text(reminder.waktuReminder.take(5))  // "HH:MM"
            }

            // Days (for Mingguan)
            if (reminder.tipeReminder == "Mingguan") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reminder.hariDalamMinggu.orEmpty().forEach { hari ->
                        AssistChip(
                            onClick = { /* kosongin kalau hanya label */ },
                            label = { Text(hari) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }

            // Toggle active
            Switch(
                checked = reminder.isActive == 1,
                onCheckedChange = { /* TODO: Update reminder */ }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Hapus Reminder?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus reminder \"${reminder.judulReminder}\"? Tindakan ini tidak dapat dibatalkan.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(reminder.reminderId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderDialog(
    onDismiss: () -> Unit,
    onConfirm: (CreateReminderRequest) -> Unit
) {
    var judul by remember { mutableStateOf("") }
    var pesan by remember { mutableStateOf("") }
    var tipe by remember { mutableStateOf("Harian") }
    var waktu by remember { mutableStateOf("08:00") }
    var selectedDays by remember { mutableStateOf<Set<String>>(emptySet()) }
    var tanggal by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Tambah Reminder",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(scrollState)
            ) {
                // ✅ Judul
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul Reminder *") },
                    placeholder = { Text("Buat Laporan Kegiatan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = judul.isNotBlank() && judul.length < 3
                )

                // ✅ Pesan
                OutlinedTextField(
                    value = pesan,
                    onValueChange = { pesan = it },
                    label = { Text("Pesan Reminder (Opsional)") },
                    placeholder = { Text("Jangan lupa buat laporan hari ini...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // ✅ Tipe Reminder
                TipeReminderSelector(
                    selectedTipe = tipe,
                    onTipeSelected = {
                        tipe = it
                        // Reset fields when type changes
                        if (it != "Mingguan") selectedDays = emptySet()
                        if (it != "Sekali") tanggal = ""
                    }
                )

                // ✅ Waktu Reminder
                WaktuReminderPicker(
                    selectedTime = waktu,
                    onTimeSelected = { waktu = it }
                )

                // ✅ Hari dalam Minggu (only for Mingguan)
                if (tipe == "Mingguan") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Pilih Hari *",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selectedDays.isEmpty()) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        DaySelector(
                            selectedDays = selectedDays,
                            onDayToggle = { day ->
                                selectedDays = if (selectedDays.contains(day)) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            }
                        )
                        if (selectedDays.isEmpty()) {
                            Text(
                                "Pilih minimal 1 hari",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // ✅ Tanggal Spesifik (only for Sekali)
                if (tipe == "Sekali") {
                    TanggalSpesifikPicker(
                        selectedDate = tanggal,
                        onDateSelected = { tanggal = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val request = CreateReminderRequest(
                        judulReminder = judul,
                        pesanReminder = pesan.ifEmpty { null },
                        tipeReminder = tipe,
                        waktuReminder = waktu,
                        hariDalamMinggu = if (tipe == "Mingguan") {
                            selectedDays.toList()
                        } else null,
                        tanggalSpesifik = if (tipe == "Sekali") tanggal else null,
                        isActive = true
                    )
                    onConfirm(request)
                },
                enabled = isFormValid(judul, tipe, waktu, selectedDays, tanggal)
            ) {
                Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

// ✅ Form validation
private fun isFormValid(
    judul: String,
    tipe: String,
    waktu: String,
    selectedDays: Set<String>,
    tanggal: String
): Boolean {
    if (judul.length < 3) return false
    if (!waktu.matches(Regex("^([01]\\d|2[0-3]):[0-5]\\d$"))) return false
    if (tipe == "Mingguan" && selectedDays.isEmpty()) return false
    if (tipe == "Sekali" && tanggal.isEmpty()) return false
    return true
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DaySelector(
    selectedDays: Set<String>,
    onDayToggle: (String) -> Unit
) {
    val days = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { day ->
            FilterChip(
                selected = selectedDays.contains(day),
                onClick = { onDayToggle(day) },
                label = { Text(day.take(3)) }  // Sen, Sel, Rab, ...
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipeReminderSelector(
    selectedTipe: String,
    onTipeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val tipeOptions = listOf("Harian", "Mingguan", "Bulanan", "Sekali")

    // ✅ Color mapping - Define inside composable
    val tipeColors = mapOf(
        "Harian" to PrimaryLight,
        "Mingguan" to SecondaryLight,
        "Bulanan" to TertiaryLight,
        "Sekali" to StatusRevised
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedTipe,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tipe Reminder *") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            tipeOptions.forEach { tipe ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color indicator
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(tipeColors[tipe] ?: Color.Gray)
                            )
                            Text(tipe)
                        }
                    },
                    onClick = {
                        onTipeSelected(tipe)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TanggalSpesifikPicker(
    selectedDate: String,  // Format: "YYYY-MM-DD"
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate.ifEmpty { "Pilih tanggal" },
        onValueChange = {},
        readOnly = true,
        label = { Text("Tanggal Spesifik *") },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.CalendarToday, "Pilih tanggal")
            }
        },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors()
    )

    if (showDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (selectedDate.isNotEmpty()) {
                parseDateToMillis(selectedDate)
            } else {
                System.currentTimeMillis()
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formattedDate = formatMillisToDate(millis)
                            onDateSelected(formattedDate)
                        }
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// Helper functions
private fun parseDateToMillis(dateString: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        format.parse(dateString)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

private fun formatMillisToDate(millis: Long): String {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaktuReminderPicker(
    selectedTime: String,  // Format: "HH:MM"
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedTime.ifEmpty { "Pilih waktu" },
        onValueChange = {},
        readOnly = true,
        label = { Text("Waktu Reminder *") },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.AccessTime, "Pilih waktu")
            }
        },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors()
    )

    if (showDialog) {
        val currentTime = parseTime(selectedTime)
        val timePickerState = rememberTimePickerState(
            initialHour = currentTime.first,
            initialMinute = currentTime.second,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formattedTime = String.format(
                            "%02d:%02d",
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        onTimeSelected(formattedTime)
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

// Helper function
private fun parseTime(timeString: String): Pair<Int, Int> {
    return try {
        if (timeString.contains(":")) {
            val parts = timeString.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } else {
            Pair(8, 0)  // Default 08:00
        }
    } catch (e: Exception) {
        Pair(8, 0)
    }
}
