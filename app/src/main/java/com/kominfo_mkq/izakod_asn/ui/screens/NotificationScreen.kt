package com.kominfo_mkq.izakod_asn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kominfo_mkq.izakod_asn.data.model.Notifikasi
import com.kominfo_mkq.izakod_asn.ui.components.IZAKODHeaderBar
import com.kominfo_mkq.izakod_asn.ui.theme.PrimaryLight
import com.kominfo_mkq.izakod_asn.ui.theme.StatusApproved
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRejected
import com.kominfo_mkq.izakod_asn.ui.theme.StatusRevised
import com.kominfo_mkq.izakod_asn.ui.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onNavigateBack: () -> Unit,
    onNotificationClick: (Int) -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load notifications when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Notifikasi",
                subtitle = if (uiState.unreadCount > 0) "${uiState.unreadCount} belum dibaca" else null,
                onBack = onNavigateBack,
                actions = {
                    if (uiState.notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.loadNotifications() }) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                NotificationLoadingContent()
            }
            uiState.isError -> {
                NotificationErrorContent(
                    message = uiState.errorMessage ?: "Terjadi kesalahan",
                    onRetry = { viewModel.loadNotifications() }
                )
            }
            uiState.notifications.isEmpty() -> {
                EmptyNotifications()
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.notifications, key = { it.notifikasiId }) { notif ->
                        NotificationCard(
                            notification = notif,
                            onClick = { onNotificationClick(notif.notifikasiId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationDetailScreen(
    notificationId: Int,
    onNavigateBack: () -> Unit,
    onOpenLaporan: (Int) -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    val notification = uiState.notifications.firstOrNull { it.notifikasiId == notificationId }

    LaunchedEffect(notification?.notifikasiId, notification?.isRead) {
        if (notification != null && notification.isRead == 0) {
            viewModel.markAsRead(notification.notifikasiId)
        }
    }

    Scaffold(
        topBar = {
            IZAKODHeaderBar(
                title = "Detail Notifikasi",
                subtitle = notification?.tipeNotifikasi,
                onBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.loadNotifications() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    NotificationLoadingContent()
                }
            }

            uiState.isError -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    NotificationErrorContent(
                        message = uiState.errorMessage ?: "Terjadi kesalahan",
                        onRetry = { viewModel.loadNotifications() }
                    )
                }
            }

            notification == null -> {
                NotificationNotFoundContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    onRetry = { viewModel.loadNotifications() }
                )
            }

            else -> {
                NotificationDetailContent(
                    modifier = Modifier.padding(paddingValues),
                    notification = notification,
                    onOpenLaporan = onOpenLaporan
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: Notifikasi,
    onClick: () -> Unit
) {
    val (icon, iconColor, bgColor) = notificationVisuals(notification.tipeNotifikasi)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead == 0) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.judul,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (notification.isRead == 0) {
                                FontWeight.Bold
                            } else {
                                FontWeight.SemiBold
                            }
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (notification.isRead == 0) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PrimaryLight)
                        )
                    }
                }

                // Message preview (first 2 lines of WhatsApp message)
                val cleanMessage = notification.pesan
                    .filterLegacyVerificationHeader()
                    .replace("*", "")
                    .replace("_", "")
                    .trim()
                    .split("\n")
                    .filter { it.isNotBlank() }
                    .take(3)
                    .joinToString(" • ")

                Text(
                    text = notification.cleanMessage(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Time
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = formatRelativeTime(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    if (notification.actionRequired == 1) {
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = StatusRevised.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "Perlu Tindakan",
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusRevised,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationDetailContent(
    modifier: Modifier = Modifier,
    notification: Notifikasi,
    onOpenLaporan: (Int) -> Unit
) {
    val (icon, iconColor, bgColor) = notificationVisuals(notification.tipeNotifikasi)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(bgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = notification.judul,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                NotificationStatusChip(notification = notification)
                                if (notification.actionRequired == 1) {
                                    NotificationActionChip()
                                }
                            }
                        }
                    }

                    Text(
                        text = notification.cleanMessage(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    NotificationInfoRow(
                        label = "Jenis",
                        value = notification.tipeNotifikasi.ifBlank { "Notifikasi" }
                    )
                    NotificationInfoRow(
                        label = "Waktu",
                        value = formatNotificationDateTime(notification.createdAt)
                    )
                    NotificationInfoRow(
                        label = "Status",
                        value = if (notification.isRead == 0) "Belum dibaca" else "Sudah dibaca"
                    )
                    if (notification.isRead == 1 && !notification.tanggalDibaca.isNullOrBlank()) {
                        NotificationInfoRow(
                            label = "Dibaca",
                            value = formatNotificationDateTime(notification.tanggalDibaca)
                        )
                    }
                    if (!notification.linkTujuan.isNullOrBlank()) {
                        NotificationInfoRow(
                            label = "Tujuan",
                            value = notification.linkTujuan
                        )
                    }
                }
            }
        }

        if (notification.laporanId != null) {
            item {
                Button(
                    onClick = { onOpenLaporan(notification.laporanId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Buka Laporan Terkait")
                }
            }
        }
    }
}

@Composable
private fun NotificationStatusChip(notification: Notifikasi) {
    val color = if (notification.isRead == 0) PrimaryLight else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Text(
            text = if (notification.isRead == 0) "Baru" else "Dibaca",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun NotificationActionChip() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = StatusRevised.copy(alpha = 0.16f)
    ) {
        Text(
            text = "Perlu tindakan",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = StatusRevised
        )
    }
}

@Composable
private fun NotificationInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NotificationNotFoundContent(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
            Text(
                text = "Notifikasi tidak ditemukan",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Coba muat ulang daftar notifikasi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
private fun NotificationLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Memuat notifikasi...")
        }
    }
}

@Composable
private fun NotificationErrorContent(message: String, onRetry: () -> Unit) {
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
private fun EmptyNotifications() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Tidak ada notifikasi",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Notifikasi akan muncul di sini",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// Helper function to format relative time
private fun formatRelativeTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString) ?: return dateString

        val now = System.currentTimeMillis()
        val diff = now - date.time

        val minutes = diff / (1000 * 60)
        val hours = diff / (1000 * 60 * 60)
        val days = diff / (1000 * 60 * 60 * 24)

        when {
            minutes < 1 -> "Baru saja"
            minutes < 60 -> "$minutes menit yang lalu"
            hours < 24 -> "$hours jam yang lalu"
            days < 7 -> "$days hari yang lalu"
            else -> {
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                outputFormat.format(date)
            }
        }
    } catch (_: Exception) {
        dateString
    }
}

@Composable
private fun notificationVisuals(
    tipeNotifikasi: String
): Triple<androidx.compose.ui.graphics.vector.ImageVector, androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> {
    return when (tipeNotifikasi) {
        "Verifikasi" -> Triple(
            Icons.Default.CheckCircle,
            StatusApproved,
            StatusApproved.copy(alpha = 0.1f)
        )
        "Penolakan" -> Triple(
            Icons.Default.Cancel,
            StatusRejected,
            StatusRejected.copy(alpha = 0.1f)
        )
        "Komentar" -> Triple(
            Icons.Default.Edit,
            StatusRevised,
            StatusRevised.copy(alpha = 0.1f)
        )
        else -> Triple(
            Icons.Default.Notifications,
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    }
}

private fun Notifikasi.cleanMessage(): String {
    return pesan
        .filterLegacyVerificationHeader()
        .replace("*", "")
        .replace("_", "")
        .trim()
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n")
        .ifBlank { pesan.trim().ifBlank { "-" } }
}

private fun String.filterLegacyVerificationHeader(): String {
    return split("\n")
        .filterNot { it.isLegacyVerificationHeader() }
        .joinToString("\n")
}

private fun String.isLegacyVerificationHeader(): Boolean {
    val normalized = replace("*", "")
        .replace("_", "")
        .trim()
        .uppercase(Locale.ROOT)

    return normalized.contains("NOTIFIKASI") &&
            normalized.contains("VERIFIKASI") &&
            normalized.contains("LAPORAN")
}

private fun formatNotificationDateTime(dateString: String): String {
    val parsed = parseNotificationDate(dateString) ?: return dateString
    return SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(parsed)
}

private fun parseNotificationDate(dateString: String): java.util.Date? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    for (pattern in patterns) {
        try {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            if (pattern.endsWith("'Z'")) {
                formatter.timeZone = TimeZone.getTimeZone("UTC")
            }
            return formatter.parse(dateString)
        } catch (_: Exception) {
            // Try the next known server timestamp shape.
        }
    }

    return null
}
