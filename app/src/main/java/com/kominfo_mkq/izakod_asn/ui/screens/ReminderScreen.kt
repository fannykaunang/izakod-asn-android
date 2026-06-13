package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.CreateReminderRequest
import com.kominfo_mkq.izakod_asn.data.model.Reminder
import com.kominfo_mkq.izakod_asn.data.model.ReminderStats
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.SecondaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.theme.TertiaryLight
import com.kominfo_mkq.izakod_asn.ui.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReminderViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadReminders()
    }

    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            showCreateDialog = false
            viewModel.resetCreateSuccess()
        }
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Pengingat",
                subtitle = "Atur alarm kecil agar laporan tidak terlewat",
                compact = true,
                onBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = { viewModel.loadReminders() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading && !uiState.isError) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah") },
                    containerColor = PrimaryLight,
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
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

                else -> {
                    ReminderContent(
                        reminders = uiState.reminders,
                        stats = uiState.stats,
                        onCreate = { showCreateDialog = true },
                        onDelete = { reminderId -> viewModel.deleteReminder(reminderId) }
                    )
                }
            }

            if (showCreateDialog) {
                CreateReminderDialog(
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { request ->
                        viewModel.createReminder(request)
                    }
                )
            }

            if (uiState.isCreating || uiState.isDeleting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.26f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            Text(if (uiState.isCreating) "Menyimpan pengingat..." else "Menghapus pengingat...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderContent(
    reminders: List<Reminder>,
    stats: ReminderStats?,
    onCreate: () -> Unit,
    onDelete: (Int) -> Unit
) {
    val nextReminder = reminders.firstOrNull { it.isActive == 1 } ?: reminders.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ReminderHeroCard(
                nextReminder = nextReminder,
                total = reminders.size,
                onCreate = onCreate
            )
        }

        item {
            ReminderStatsSection(stats = stats, fallbackTotal = reminders.size)
        }

        if (reminders.isEmpty()) {
            item {
                EmptyReminders(onCreate = onCreate)
            }
        } else {
            item {
                SectionHeader(
                    title = "Daftar Pengingat",
                    subtitle = "${reminders.size} pengingat tersimpan"
                )
            }

            items(reminders, key = { it.reminderId }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun ReminderHeroCard(
    nextReminder: Reminder?,
    total: Int,
    onCreate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBubble(
                    icon = Icons.Default.NotificationsActive,
                    color = PrimaryLight,
                    size = 54.dp
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (total > 0) "Pengingat sudah siap" else "Belum ada pengingat",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = if (total > 0) {
                            "Aplikasi akan membantu mengingatkan rutinitas kerja penting."
                        } else {
                            "Buat pengingat untuk laporan harian, target, atau agenda pribadi."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBubble(
                        icon = if (nextReminder != null) Icons.Default.AccessTime else Icons.Default.AddAlarm,
                        color = if (nextReminder != null) reminderAccentColor(nextReminder.tipeReminder) else StatusPending,
                        size = 42.dp
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = nextReminder?.judulReminder ?: "Buat pengingat pertama",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = nextReminder?.let { reminderScheduleLabel(it) }
                                ?: "Pilih jam dan pola pengingat yang mudah diikuti.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    FilledTonalButton(
                        onClick = onCreate,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Tambah")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderStatsSection(
    stats: ReminderStats?,
    fallbackTotal: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(
            title = "Ringkasan",
            subtitle = "Status pengingat yang sudah dibuat"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReminderStatCard(
                title = "Total",
                value = (stats?.total ?: fallbackTotal).toString(),
                icon = Icons.Default.Alarm,
                color = PrimaryLight,
                modifier = Modifier.weight(1f)
            )
            ReminderStatCard(
                title = "Aktif",
                value = stats?.active ?: "0",
                icon = Icons.Default.ToggleOn,
                color = StatusApproved,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ReminderStatCard(
                title = "Harian",
                value = stats?.harian ?: "0",
                icon = Icons.Default.Today,
                color = SecondaryLight,
                modifier = Modifier.weight(1f)
            )
            ReminderStatCard(
                title = "Bulanan",
                value = stats?.bulanan ?: "0",
                icon = Icons.Default.CalendarMonth,
                color = TertiaryLight,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReminderStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBubble(icon = icon, color = color, size = 38.dp)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = color
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderCard(reminder: Reminder, onDelete: (Int) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val accent = reminderAccentColor(reminder.tipeReminder)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                IconBubble(
                    icon = reminderTypeIcon(reminder.tipeReminder),
                    color = accent,
                    size = 48.dp
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReminderBadge(text = reminder.tipeReminder, color = accent)
                        ReminderBadge(
                            text = if (reminder.isActive == 1) "Aktif" else "Nonaktif",
                            color = if (reminder.isActive == 1) StatusApproved else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = reminder.judulReminder,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!reminder.pesanReminder.isNullOrBlank()) {
                        Text(
                            text = reminder.pesanReminder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = StatusRejected
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReminderInfoPill(
                    icon = Icons.Default.AccessTime,
                    label = "Jam",
                    value = reminder.waktuReminder.take(5),
                    modifier = Modifier.weight(1f)
                )
                ReminderInfoPill(
                    icon = Icons.Default.EventRepeat,
                    label = "Pola",
                    value = reminderShortSchedule(reminder),
                    modifier = Modifier.weight(1f)
                )
            }

            if (reminder.tipeReminder == "Mingguan" && !reminder.hariDalamMinggu.isNullOrEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reminder.hariDalamMinggu.forEach { hari ->
                        ReminderBadge(text = hari.take(3), color = accent)
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = StatusRejected,
                    modifier = Modifier.size(34.dp)
                )
            },
            title = {
                Text(
                    "Hapus Pengingat?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    "Pengingat \"${reminder.judulReminder}\" akan dihapus permanen.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(reminder.reminderId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejected)
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

@Composable
private fun RemindersLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Memuat pengingat...",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun RemindersErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IconBubble(icon = Icons.Default.ErrorOutline, color = StatusRejected, size = 62.dp)
                Text(
                    text = "Pengingat gagal dimuat",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onRetry, shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Coba Lagi")
                }
            }
        }
    }
}

@Composable
private fun EmptyReminders(onCreate: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            IconBubble(icon = Icons.Default.AlarmOff, color = StatusPending, size = 72.dp)
            Text(
                text = "Belum ada pengingat",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Buat pengingat untuk membantu rutinitas laporan, target, atau pekerjaan berkala.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onCreate, shape = RoundedCornerShape(18.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Buat Pengingat")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Buat Pengingat",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    "Isi judul, pola, dan jam pengingat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(scrollState)
            ) {
                OutlinedTextField(
                    value = judul,
                    onValueChange = { judul = it },
                    label = { Text("Judul pengingat") },
                    placeholder = { Text("Contoh: Buat laporan kegiatan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = judul.isNotBlank() && judul.length < 3
                )

                OutlinedTextField(
                    value = pesan,
                    onValueChange = { pesan = it },
                    label = { Text("Catatan tambahan") },
                    placeholder = { Text("Contoh: Jangan lupa tautkan ke target bulan ini") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Pola pengingat",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Harian", "Mingguan", "Bulanan", "Sekali").forEach { option ->
                            FilterChip(
                                selected = tipe == option,
                                onClick = {
                                    tipe = option
                                    if (option != "Mingguan") selectedDays = emptySet()
                                    if (option != "Sekali") tanggal = ""
                                },
                                label = { Text(option) },
                                leadingIcon = if (tipe == option) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }

                WaktuReminderPicker(
                    selectedTime = waktu,
                    onTimeSelected = { waktu = it }
                )

                if (tipe == "Mingguan") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Pilih hari",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (selectedDays.isEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
                                "Pilih minimal 1 hari.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

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
                        judulReminder = judul.trim(),
                        pesanReminder = pesan.trim().ifEmpty { null },
                        tipeReminder = tipe,
                        waktuReminder = waktu,
                        hariDalamMinggu = if (tipe == "Mingguan") selectedDays.toList() else null,
                        tanggalSpesifik = if (tipe == "Sekali") tanggal else null,
                        isActive = true
                    )
                    onConfirm(request)
                },
                enabled = isFormValid(judul, tipe, waktu, selectedDays, tanggal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

private fun isFormValid(
    judul: String,
    tipe: String,
    waktu: String,
    selectedDays: Set<String>,
    tanggal: String
): Boolean {
    if (judul.trim().length < 3) return false
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEach { day ->
            FilterChip(
                selected = selectedDays.contains(day),
                onClick = { onDayToggle(day) },
                label = { Text(day.take(3)) }
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedTipe,
            onValueChange = {},
            readOnly = true,
            label = { Text("Pola pengingat") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
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
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(reminderAccentColor(tipe))
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
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedDate.ifEmpty { "Pilih tanggal" },
        onValueChange = {},
        readOnly = true,
        label = { Text("Tanggal pengingat") },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Pilih tanggal")
            }
        },
        modifier = modifier.fillMaxWidth()
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
                            onDateSelected(formatMillisToDate(millis))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaktuReminderPicker(
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedTime.ifEmpty { "Pilih waktu" },
        onValueChange = {},
        readOnly = true,
        label = { Text("Jam pengingat") },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.AccessTime, contentDescription = "Pilih waktu")
            }
        },
        modifier = modifier.fillMaxWidth()
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
                            Locale.getDefault(),
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

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IconBubble(icon: ImageVector, color: Color, size: androidx.compose.ui.unit.Dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(size / 3),
        color = color.copy(alpha = 0.14f),
        contentColor = color
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size * 0.52f)
            )
        }
    }
}

@Composable
private fun ReminderBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.14f),
        contentColor = color
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1
        )
    }
}

@Composable
private fun ReminderInfoPill(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun reminderAccentColor(type: String): Color {
    return when (type) {
        "Harian" -> PrimaryLight
        "Mingguan" -> SecondaryLight
        "Bulanan" -> TertiaryLight
        "Sekali" -> StatusRevised
        else -> StatusPending
    }
}

private fun reminderTypeIcon(type: String): ImageVector {
    return when (type) {
        "Harian" -> Icons.Default.Today
        "Mingguan" -> Icons.Default.DateRange
        "Bulanan" -> Icons.Default.CalendarMonth
        "Sekali" -> Icons.Default.Event
        else -> Icons.Default.Alarm
    }
}

private fun reminderShortSchedule(reminder: Reminder): String {
    return when (reminder.tipeReminder) {
        "Mingguan" -> reminder.hariDalamMinggu?.joinToString(", ") { it.take(3) } ?: "Mingguan"
        "Sekali" -> reminder.tanggalSpesifik ?: "Sekali"
        else -> reminder.tipeReminder
    }
}

private fun reminderScheduleLabel(reminder: Reminder): String {
    val time = reminder.waktuReminder.take(5)
    return when (reminder.tipeReminder) {
        "Harian" -> "Setiap hari pukul $time"
        "Mingguan" -> "Setiap ${reminder.hariDalamMinggu?.joinToString(", ") ?: "minggu"} pukul $time"
        "Bulanan" -> "Setiap bulan pukul $time"
        "Sekali" -> "Tanggal ${reminder.tanggalSpesifik ?: "-"} pukul $time"
        else -> "$time - ${reminder.tipeReminder}"
    }
}

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

private fun parseTime(timeString: String): Pair<Int, Int> {
    return try {
        if (timeString.contains(":")) {
            val parts = timeString.split(":")
            Pair(parts[0].toInt(), parts[1].toInt())
        } else {
            Pair(8, 0)
        }
    } catch (e: Exception) {
        Pair(8, 0)
    }
}
