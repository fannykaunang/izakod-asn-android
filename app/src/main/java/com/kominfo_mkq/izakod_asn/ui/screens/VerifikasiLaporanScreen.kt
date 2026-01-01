package com.kominfo_mkq.izakod_asn.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusPending
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.viewmodel.VerifikasiLaporanViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * VerifikasiLaporanScreen - Screen untuk atasan verifikasi laporan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifikasiLaporanScreen(
    laporanId: String,
    onNavigateBack: () -> Unit,
    viewModel: VerifikasiLaporanViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showVerifikasiDialog by remember { mutableStateOf(false) }
    var showRevisiDialog by remember { mutableStateOf(false) }
    var showTolakDialog by remember { mutableStateOf(false) }

    // Load laporan on screen open
    LaunchedEffect(laporanId) {
        viewModel.loadLaporan(laporanId.toInt())
    }

    // Show toast on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            Toast.makeText(context, uiState.successMessage ?: "Berhasil", Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    // Show toast on error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Verifikasi Laporan",
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
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                VerifikasiLaporanLoadingContent()
            }
            uiState.isError -> {
                VerifikasiLaporanErrorContent(
                    message = uiState.errorMessage ?: "Terjadi kesalahan",
                    onRetry = { viewModel.loadLaporan(laporanId.toInt()) }
                )
            }
            uiState.laporan != null -> {
                val laporan = uiState.laporan!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Status Info
                    StatusInfoCard(status = laporan.statusLaporan)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Informasi Pegawai
                    PegawaiInfoSection(
                        nama = laporan.pegawaiNama ?: "-",
                        nip = laporan.pegawaiNip ?: "-",
                        skpd = laporan.skpd ?: "-"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Informasi Kegiatan
                    KegiatanInfoSection(laporan)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Detail Kegiatan
                    DetailKegiatanSection(laporan)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Verifikasi Actions (only if can verify)
                    if (uiState.canVerify) {
                        VerifikasiActions(
                            isLoading = uiState.isSubmitting,
                            onTerima = { showVerifikasiDialog = true },
                            onRevisi = { showRevisiDialog = true },
                            onTolak = { showTolakDialog = true }
                        )
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    "Anda tidak memiliki akses untuk memverifikasi laporan ini",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Verifikasi Dialog (Terima)
    if (showVerifikasiDialog) {
        VerifikasiDialog(
            onDismiss = { showVerifikasiDialog = false },
            onConfirm = { rating, catatan ->
                viewModel.verifikasiLaporan(
                    laporanId = laporanId.toInt(),
                    status = "Diverifikasi",
                    rating = rating,
                    catatan = catatan
                )
                showVerifikasiDialog = false
            }
        )
    }

    if (showRevisiDialog) {
        RevisiDialog(
            onDismiss = { showRevisiDialog = false },
            onConfirm = { catatan ->
                viewModel.verifikasiLaporan(
                    laporanId = laporanId.toInt(),
                    status = "Revisi",
                    rating = null,
                    catatan = catatan
                )
                showRevisiDialog = false
            }
        )
    }

    // Tolak Dialog
    if (showTolakDialog) {
        TolakDialog(
            onDismiss = { showTolakDialog = false },
            onConfirm = { catatan ->
                viewModel.verifikasiLaporan(
                    laporanId = laporanId.toInt(),
                    status = "Ditolak",
                    rating = null,
                    catatan = catatan
                )
                showTolakDialog = false
            }
        )
    }
}

@Composable
private fun StatusInfoCard(status: String) {
    val (containerColor, textColor, icon) = when (status) {
        "Draft" -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Edit
        )
        "Diajukan" -> Triple(
            StatusPending.copy(alpha = 0.15f),  // ✅ Amber background
            StatusPending.copy(alpha = 0.9f),   // ✅ Amber text
            Icons.Default.Schedule
        )
        "Diverifikasi" -> Triple(
            StatusApproved.copy(alpha = 0.15f), // ✅ Green background
            StatusApproved.copy(alpha = 0.9f),  // ✅ Green text
            Icons.Default.CheckCircle
        )
        "Ditolak" -> Triple(
            StatusRejected.copy(alpha = 0.15f), // ✅ Red background
            StatusRejected.copy(alpha = 0.9f),  // ✅ Red text
            Icons.Default.Cancel
        )
        "Revisi" -> Triple(  // ✅ Add Revisi status
            StatusRevised.copy(alpha = 0.15f),  // ✅ Orange background
            StatusRevised.copy(alpha = 0.9f),   // ✅ Orange text
            Icons.Default.Edit
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Info
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor)
            Column {
                Text(
                    "Status Laporan",
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor
                )
                Text(
                    status,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun PegawaiInfoSection(nama: String, nip: String, skpd: String) {
    DetailCard(title = "Informasi Pegawai", icon = Icons.Default.Person) {
        DetailRow(label = "Nama", value = nama)
        DetailRow(label = "NIP", value = nip)
        DetailRow(label = "SKPD", value = skpd)
    }
}

@Composable
private fun KegiatanInfoSection(laporan: com.kominfo_mkq.izakod_asn.data.model.LaporanDetail) {
    DetailCard(title = "Informasi Kegiatan", icon = Icons.Default.CalendarToday) {
        DetailRow(label = "Tanggal", value = formatDate(laporan.tanggalKegiatan))
        DetailRow(label = "Kategori", value = laporan.kategoriNama ?: "-")
        DetailRow(label = "Nama Kegiatan", value = laporan.namaKegiatan)

        if (laporan.waktuMulai.isNotEmpty()) {
            DetailRow(
                label = "Waktu",
                value = "${formatTime(laporan.waktuMulai)} - ${formatTime(laporan.waktuSelesai)}"
            )
        }

        if (laporan.lokasiKegiatan != null) {
            DetailRow(label = "Lokasi", value = laporan.lokasiKegiatan)
        }
    }
}

@Composable
private fun DetailKegiatanSection(laporan: com.kominfo_mkq.izakod_asn.data.model.LaporanDetail) {
    DetailCard(title = "Detail Kegiatan", icon = Icons.Default.Description) {
        DetailRow(label = "Deskripsi", value = laporan.deskripsiKegiatan, multiline = true)

        if (laporan.targetOutput != null) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Target Output", value = laporan.targetOutput, multiline = true)
        }

        if (laporan.hasilOutput != null) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Hasil Output", value = laporan.hasilOutput, multiline = true)
        }

        if (laporan.pesertaKegiatan != null) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Peserta", value = laporan.pesertaKegiatan)
        }

        if (laporan.kendala != null) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Kendala", value = laporan.kendala, multiline = true)
        }

        if (laporan.solusi != null) {
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(label = "Solusi", value = laporan.solusi, multiline = true)
        }
    }
}

@Composable
private fun VerifikasiActions(
    isLoading: Boolean,
    onTerima: () -> Unit,
    onRevisi: () -> Unit,
    onTolak: () -> Unit
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
                "Tindakan Verifikasi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            // Terima Button
            Button(
                onClick = onTerima,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Terima & Verifikasi")
            }

            // Revisi Button
            OutlinedButton(
                onClick = onRevisi,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Minta Revisi")
            }

            // Tolak Button
            OutlinedButton(
                onClick = onTolak,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tolak Laporan")
            }
        }
    }
}

@Composable
private fun VerifikasiDialog(
    onDismiss: () -> Unit,
    onConfirm: (rating: Int, catatan: String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var catatan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // ✅ Rating Section with Google Play style
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Rating Kualitas Laporan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // ✅ Star Rating with Numbers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { value ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { rating = value }
                                    .padding(4.dp)
                            ) {
                                // ✅ Star Icon (Filled or Outlined)
                                Icon(
                                    imageVector = if (rating >= value) {
                                        Icons.Default.Star  // Filled star
                                    } else {
                                        Icons.Default.StarBorder  // Outlined star
                                    },
                                    contentDescription = "Rating $value",
                                    modifier = Modifier.size(40.dp),
                                    tint = if (rating >= value) {
                                        Color(0xFFFFB300)  // ✅ Yellow/Amber
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    }
                                )

                                // ✅ Number below star
                                Text(
                                    text = value.toString(),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (rating == value) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (rating == value) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // ✅ Rating description
                    Text(
                        text = when (rating) {
                            1 -> "Sangat Kurang"
                            2 -> "Kurang"
                            3 -> "Cukup"
                            4 -> "Baik"
                            5 -> "Sangat Baik"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFFFFB300),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // ✅ Catatan Section
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan Verifikasi (Opsional)") },
                    placeholder = { Text("Tambahkan catatan...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rating, catatan) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verifikasi")
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

@Composable
private fun RevisiDialog(
    onDismiss: () -> Unit,
    onConfirm: (catatan: String) -> Unit
) {
    var catatan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Minta Revisi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Berikan catatan revisi untuk perbaikan laporan:",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan Revisi *") },
                    placeholder = { Text("Jelaskan poin yang perlu diperbaiki...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    isError = catatan.isBlank()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(catatan) },
                enabled = catatan.isNotBlank()
            ) {
                Text("Kirim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
private fun TolakDialog(
    onDismiss: () -> Unit,
    onConfirm: (catatan: String) -> Unit
) {
    var catatan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tolak Laporan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Berikan alasan penolakan laporan ini:",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Alasan Penolakan *") },
                    placeholder = { Text("Jelaskan alasan penolakan...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    isError = catatan.isBlank()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(catatan) },
                enabled = catatan.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Tolak")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// Helper Composables
@Composable
private fun DetailCard(
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
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
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

            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, multiline: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (multiline) Int.MAX_VALUE else 1
        )
    }
}

@Composable
private fun VerifikasiLaporanLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun VerifikasiLaporanErrorContent(message: String, onRetry: () -> Unit) {
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
                Text("Coba Lagi")
            }
        }
    }
}

// Helper functions
private fun formatDate(dateString: String): String {
    return try {
        val datePart = dateString.split("T")[0]
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val date = inputFormat.parse(datePart)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.split("T")[0]
    }
}

private fun formatTime(timeString: String): String {
    return timeString.take(5) // "HH:mm:ss" -> "HH:mm"
}